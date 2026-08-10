/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.ai.index;

import com.alibaba.nacos.ai.config.McpCacheIndexProperties;
import com.alibaba.nacos.ai.model.mcp.McpServerIndexData;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Memory-based MCP cache index implementation with optimized locking.
 * <p>基于内存的 MCP 缓存索引，采用读写锁 + LRU 双向链表，支持容量上限、TTL 过期与定时清理。</p>
 *
 * <p>
 * TODO This Memory cache might include some design issues:
 * <ul>
 *     <li>
 *         1. The read method in cache include LRU operation(write), which means read lock can't intercept write operation
 *          in multiple threads reading and cause thread-safe problem.
 *     </li>
 *     <li>
 *         2. For solve problem 1. Use {@code synchronized} wrapper {@link #removeFromLru} and {@link #moveToHead} method,
 *         which may cause the read operation performance will be affected in high qps.
 *     </li>
 *     <li>
 *         3. The next consider it whether keep the LRU behavior in next versions when qps improved. If keep it, the LRU cache should
 *         be re-designed or use stabled high performance LRU cache such as guava.
 *     </li>
 * </ul>
 * </p>
 *
 * @author misselvexu
 */
public class MemoryMcpCacheIndex implements McpCacheIndex {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMcpCacheIndex.class);
    
    private static final int DEFAULT_SHUTDOWN_TIMEOUT_SECONDS = 5;
    
    /** 缓存配置：最大容量、过期时间、清理间隔等。 */
    private final McpCacheIndexProperties properties;
    
    /** MCP ID → 缓存节点（含索引数据与 LRU 链表指针）。 */
    private final ConcurrentHashMap<String, CacheNode> idToEntry;
    
    /** 「namespaceId::mcpName」→ MCP ID 的名称映射表。 */
    private final ConcurrentHashMap<String, String> nameKeyToId;
    
    private final CacheNode head;
    
    private final CacheNode tail;
    
    private final ReentrantReadWriteLock lock;
    
    private final ReentrantReadWriteLock.ReadLock readLock;
    
    private final ReentrantReadWriteLock.WriteLock writeLock;
    
    private final AtomicLong hitCount;
    
    private final AtomicLong missCount;
    
    private final AtomicLong evictionCount;
    
    private final ScheduledExecutorService cleanupScheduler;
    
    private volatile boolean shutdown = false;
    
    public MemoryMcpCacheIndex(McpCacheIndexProperties properties) {
        this.properties = properties;
        
        // 初始化 ID 与名称映射存储
        this.idToEntry = new ConcurrentHashMap<>(properties.getMaxSize());
        this.nameKeyToId = new ConcurrentHashMap<>();
        
        // 初始化 LRU 哨兵双向链表（head/tail）
        this.head = new CacheNode("", null, 0);
        this.tail = new CacheNode("", null, 0);
        this.head.next = this.tail;
        this.tail.prev = this.head;
        
        // 初始化读写锁，读路径更新 LRU 位置
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        
        // 初始化命中/未命中/驱逐计数器
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);
        this.evictionCount = new AtomicLong(0);
        
        // 启动后台清理调度线程
        this.cleanupScheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "mcp-cache-cleanup");
            t.setDaemon(true);
            return t;
        }, new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 按配置间隔定期扫描过期条目
        this.cleanupScheduler.scheduleWithFixedDelay(this::cleanupExpiredEntries,
            properties.getCleanupIntervalSeconds(), properties.getCleanupIntervalSeconds(),
            TimeUnit.SECONDS);
    }
    
    @Override
    public String getMcpId(String namespaceId, String mcpName) {
        if (StringUtils.isBlank(namespaceId) || StringUtils.isBlank(mcpName)) {
            return null;
        }
        
        String key = buildNameKey(namespaceId, mcpName);
        readLock.lock();
        try {
            String id = nameKeyToId.get(key);
            if (id == null) {
                missCount.incrementAndGet();
                return null;
            }
            
            CacheNode node = idToEntry.get(id);
            if (node == null || node.isExpired(properties.getExpireTimeSeconds())) {
                // 清理已失效的名称映射
                nameKeyToId.remove(key, id);
                if (node != null) {
                    removeFromLru(node);
                    idToEntry.remove(id, node);
                }
                missCount.incrementAndGet();
                return null;
            }
            
            // 命中后将节点移至 LRU 链表头部
            moveToHead(node);
            hitCount.incrementAndGet();
            return id;
        } finally {
            readLock.unlock();
        }
    }
    
    @Override
    public McpServerIndexData getMcpServerByName(String namespaceId, String mcpName) {
        String id = getMcpId(namespaceId, mcpName);
        if (id == null) {
            return null;
        }
        return getMcpServerById(id);
    }
    
    @Override
    public McpServerIndexData getMcpServerById(String mcpId) {
        if (StringUtils.isBlank(mcpId)) {
            return null;
        }
        
        readLock.lock();
        try {
            CacheNode node = idToEntry.get(mcpId);
            if (node == null || node.isExpired(properties.getExpireTimeSeconds())) {
                if (node != null) {
                    removeFromLru(node);
                    idToEntry.remove(mcpId, node);
                    cleanupInvalidMappings(mcpId);
                }
                missCount.incrementAndGet();
                return null;
            }
            
            // Update LRU position
            moveToHead(node);
            hitCount.incrementAndGet();
            return node.data;
        } finally {
            readLock.unlock();
        }
    }
    
    @Override
    public void updateIndex(String namespaceId, String mcpName, String mcpId) {
        if (StringUtils.isBlank(namespaceId) || StringUtils.isBlank(mcpName)
            || StringUtils.isBlank(mcpId)) {
            return;
        }
        
        McpServerIndexData data = McpServerIndexData.newIndexData(mcpId, namespaceId);
        CacheNode newNode = new CacheNode(mcpId, data, System.currentTimeMillis() / 1000);
        
        writeLock.lock();
        try {
            CacheNode oldNode = idToEntry.put(mcpId, newNode);
            if (oldNode != null) {
                // 更新索引时移除旧 LRU 节点
                removeFromLru(oldNode);
            }
            
            // 新节点插入 LRU 链表头部
            addToHead(newNode);
            
            // 超出 maxSize 时循环驱逐最久未使用条目
            while (idToEntry.size() > properties.getMaxSize()) {
                evictLeastRecentlyUsed();
            }
            
            // 同步更新名称→ID 映射
            String key = buildNameKey(namespaceId, mcpName);
            nameKeyToId.put(key, mcpId);
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public void removeIndex(String namespaceId, String mcpName) {
        if (StringUtils.isBlank(namespaceId) || StringUtils.isBlank(mcpName)) {
            return;
        }
        
        writeLock.lock();
        try {
            String key = buildNameKey(namespaceId, mcpName);
            String id = nameKeyToId.remove(key);
            if (id != null) {
                CacheNode node = idToEntry.remove(id);
                if (node != null) {
                    removeFromLru(node);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public void removeIndex(String mcpId) {
        if (StringUtils.isBlank(mcpId)) {
            return;
        }
        
        writeLock.lock();
        try {
            CacheNode node = idToEntry.remove(mcpId);
            if (node != null) {
                removeFromLru(node);
            }
            cleanupInvalidMappings(mcpId);
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public void clear() {
        writeLock.lock();
        try {
            idToEntry.clear();
            nameKeyToId.clear();
            head.next = tail;
            tail.prev = head;
        } finally {
            writeLock.unlock();
        }
        
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
    }
    
    @Override
    public int getSize() {
        return idToEntry.size();
    }
    
    @Override
    public CacheStats getStats() {
        return new CacheStats(hitCount.get(), missCount.get(), evictionCount.get(), getSize());
    }
    
    /**
     * Shuts down the cache and cleans up resources.
     * <p>关闭清理调度器、等待线程结束并清空全部缓存。</p>
     */
    public void shutdown() {
        if (!shutdown) {
            shutdown = true;
            cleanupScheduler.shutdown();
            try {
                if (!cleanupScheduler.awaitTermination(DEFAULT_SHUTDOWN_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS)) {
                    cleanupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            clear();
        }
    }
    
    /** 构造名称缓存键：{@code namespaceId::mcpName}。 */
    private String buildNameKey(String namespaceId, String mcpName) {
        return namespaceId + "::" + mcpName;
    }
    
    /** 删除指向指定 MCP ID 的全部名称映射。 */
    private void cleanupInvalidMappings(String mcpId) {
        nameKeyToId.entrySet().removeIf(entry -> mcpId.equals(entry.getValue()));
    }
    
    /** 定时任务：扫描并移除 TTL 过期的缓存节点。 */
    private void cleanupExpiredEntries() {
        if (shutdown) {
            return;
        }
        
        try {
            Iterator<Map.Entry<String, CacheNode>> iterator = idToEntry.entrySet().iterator();
            
            while (iterator.hasNext()) {
                Map.Entry<String, CacheNode> entry = iterator.next();
                CacheNode node = entry.getValue();
                
                if (node.isExpired(properties.getExpireTimeSeconds())) {
                    iterator.remove();
                    removeFromLru(node);
                    cleanupInvalidMappings(entry.getKey());
                    evictionCount.incrementAndGet();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Clean up expired mcp id and name cache failed.", e);
        }
    }
    
    /** 驱逐 LRU 链表尾部（最久未使用）的条目。 */
    private void evictLeastRecentlyUsed() {
        CacheNode last = tail.prev;
        if (last != head) {
            CacheNode removed = idToEntry.remove(last.key);
            if (removed != null) {
                removeFromLru(last);
                cleanupInvalidMappings(last.key);
                evictionCount.incrementAndGet();
            }
        }
    }
    
    /** 将节点插入 LRU 链表头部（哨兵 head 之后）。 */
    private void addToHead(CacheNode node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    /** 从 LRU 链表中摘除节点（synchronized 保证与 moveToHead 互斥）。 */
    private synchronized void removeFromLru(CacheNode node) {
        if (node.prev != null && node.next != null) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
    }
    
    /** 将节点移至 LRU 链表头部，表示最近访问。 */
    private synchronized void moveToHead(CacheNode node) {
        // 先从当前位置摘除
        if (node.prev != null && node.next != null) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        // 再插入链表头部
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    // 内部类：LRU 缓存节点
    
    /** LRU 缓存节点，持有 MCP ID、索引数据与链表指针。 */
    private static class CacheNode {
        
        final String key;
        
        final McpServerIndexData data;
        
        final long createTimeSeconds;
        
        volatile CacheNode prev;
        
        volatile CacheNode next;
        
        CacheNode(String key, McpServerIndexData data, long createTimeSeconds) {
            this.key = key;
            this.data = data;
            this.createTimeSeconds = createTimeSeconds;
        }
        
        /** 判断节点是否已超过配置的 TTL（秒）；expireTimeSeconds≤0 表示永不过期。 */
        boolean isExpired(long expireTimeSeconds) {
            if (expireTimeSeconds <= 0) {
                return false;
            }
            long currentTimeSeconds = System.currentTimeMillis() / 1000;
            return (currentTimeSeconds - createTimeSeconds) >= expireTimeSeconds;
        }
    }
}
