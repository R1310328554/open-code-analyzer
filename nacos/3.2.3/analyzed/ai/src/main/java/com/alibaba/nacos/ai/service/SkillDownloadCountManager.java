/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.service;

import com.alibaba.nacos.ai.event.SkillDownloadEvent;
import com.alibaba.nacos.ai.service.repository.AiResourcePersistService;
import com.alibaba.nacos.ai.service.repository.AiResourceVersionPersistService;
import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Accumulates skill download events in memory and flushes to DB periodically. Each node maintains its own counter and
 * uses atomic SQL ({@code download_count = download_count + N}) for cluster safety.
 *
 * <p>This class subscribes to {@link SkillDownloadEvent} via {@link NotifyCenter}.</p>
 * <p>内存累积 Skill 下载事件，定时批量刷入数据库；各节点独立计数，SQL 原子递增保证集群安全。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
@Component
public class SkillDownloadCountManager extends SmartSubscriber {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SkillDownloadCountManager.class);
    
    /** 资源类型常量：Skill。 */
    private static final String RESOURCE_TYPE_SKILL = "skill";
    
    /**
     * Flush interval in seconds.
     * <p>内存计数刷入数据库的定时间隔（秒）。</p>
     */
    private static final long FLUSH_INTERVAL_SECONDS = 10;
    
    /**
     * In-memory counter. Key = composite key (ns + name + version), Value = accumulated count.
     * <p>内存计数器：键为命名空间+名称+版本，值为累计下载次数。</p>
     */
    private final ConcurrentHashMap<DownloadCountKey, AtomicLong> counterMap =
        new ConcurrentHashMap<>();
    
    /** AI 资源持久化，更新资源级总下载量。 */
    private final AiResourcePersistService aiResourcePersistService;
    
    /** AI 资源版本持久化，更新版本级下载量。 */
    private final AiResourceVersionPersistService aiResourceVersionPersistService;
    
    /** 定时刷盘调度器。 */
    private final ScheduledExecutorService scheduler;
    
    public SkillDownloadCountManager(AiResourcePersistService aiResourcePersistService,
        AiResourceVersionPersistService aiResourceVersionPersistService) {
        this.aiResourcePersistService = aiResourcePersistService;
        this.aiResourceVersionPersistService = aiResourceVersionPersistService;
        this.scheduler = ExecutorFactory.newSingleScheduledExecutorService(
            r -> {
                Thread t = new Thread(r, "com.alibaba.nacos.ai.download-count-flusher");
                t.setDaemon(true);
                return t;
            });
        this.scheduler.scheduleWithFixedDelay(this::flush, FLUSH_INTERVAL_SECONDS,
            FLUSH_INTERVAL_SECONDS,
            TimeUnit.SECONDS);
        NotifyCenter.registerSubscriber(this);
    }
    
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        return Collections.singletonList(SkillDownloadEvent.class);
    }
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof SkillDownloadEvent downloadEvent) {
            DownloadCountKey key =
                new DownloadCountKey(downloadEvent.getNamespaceId(), downloadEvent.getName(),
                    downloadEvent.getVersion());
            counterMap.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        }
    }
    
    /**
     * Swap and flush all accumulated counts to DB.
     * <p>快照并清零内存计数，原子递增写入版本级与资源级下载量。</p>
     */
    private void flush() {
        if (counterMap.isEmpty()) {
            return;
        }
        // 快照并清零：getAndSet(0) 取出累计值，0 则移除键防泄漏
        // Remove entries with 0 to avoid memory leak from deleted skills.
        for (Map.Entry<DownloadCountKey, AtomicLong> entry : counterMap.entrySet()) {
            long count = entry.getValue().getAndSet(0);
            if (count == 0) {
                counterMap.remove(entry.getKey());
                continue;
            }
            DownloadCountKey key = entry.getKey();
            try {
                // 递增版本级下载计数
                aiResourceVersionPersistService.incrementDownloadCount(key.namespaceId, key.name,
                    RESOURCE_TYPE_SKILL,
                    key.version, count);
                // 递增资源级总下载计数
                aiResourcePersistService.incrementDownloadCount(key.namespaceId, key.name,
                    RESOURCE_TYPE_SKILL, count);
            } catch (Exception e) {
                LOGGER.warn("Failed to flush download count for {}@{}: {}", key.name, key.version,
                    e.getMessage());
                // 刷盘失败时将计数加回，下次重试
                counterMap.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(count);
            }
        }
    }
    
    /**
     * Flush remaining counts and shut down the scheduler.
     * <p>关闭前刷盘剩余计数并注销事件订阅。</p>
     */
    @PreDestroy
    public void shutdown() {
        // 关闭前先刷盘剩余下载计数
        try {
            flush();
        } catch (Exception e) {
            LOGGER.warn("Failed to flush download counts on shutdown: {}", e.getMessage());
        }
        scheduler.shutdown();
        NotifyCenter.deregisterSubscriber(this);
    }
    
    /**
     * Composite key for download counter.
     * <p>下载计数复合键：namespaceId + name + version。</p>
     */
    private static final class DownloadCountKey {
        
        private final String namespaceId;
        
        private final String name;
        
        private final String version;
        
        DownloadCountKey(String namespaceId, String name, String version) {
            this.namespaceId = namespaceId;
            this.name = name;
            this.version = version;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DownloadCountKey that)) {
                return false;
            }
            return Objects.equals(namespaceId, that.namespaceId) && Objects.equals(name, that.name)
                && Objects.equals(version, that.version);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(namespaceId, name, version);
        }
    }
}
