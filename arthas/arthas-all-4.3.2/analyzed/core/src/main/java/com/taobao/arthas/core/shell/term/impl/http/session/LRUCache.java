package com.taobao.arthas.core.shell.term.impl.http.session;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;

/**
 * 基于 {@link LinkedHashMap} 的 LRU（最近最少使用）缓存。
 *
 * <p>
 * 容量固定为 {@code cacheSize}；满员时新增条目会淘汰最久未访问项。
 *
 * <p>
 * 线程安全：所有公开方法均 {@code synchronized}。
 *
 * <p>
 * Author: Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland<br>
 * Multi-licensed: EPL / LGPL / GPL / AL / BSD.
 */
public class LRUCache<K, V> {

    private static final float hashTableLoadFactor = 0.75f;

    private LinkedHashMap<K, V> map;
    private int cacheSize;

    /**
     * 创建指定容量的 LRU 缓存。
     *
     * @param cacheSize 最多保留的条目数
     */
    public LRUCache(int cacheSize) {
        this.cacheSize = cacheSize;
        int hashTableCapacity = (int) Math.ceil(cacheSize / hashTableLoadFactor) + 1;
        map = new LinkedHashMap<K, V>(hashTableCapacity, hashTableLoadFactor, true) {
            // (an anonymous inner class)
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.cacheSize;
            }
        };
    }

    /**
     * 读取条目并将其标记为最近使用（MRU）。
     *
     * @param key 键
     * @return 关联值，不存在则 null
     */
    public synchronized V get(K key) {
        return map.get(key);
    }

    /**
     * 写入条目；键已存在则替换。缓存满时淘汰 LRU 项。
     *
     * @param key   键
     * @param value 值
     */
    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    /** 清空缓存 */
    public synchronized void clear() {
        map.clear();
    }

    /**
     * 返回当前已占用的条目数。
     *
     * @return 缓存内条目数量
     */
    public synchronized int usedEntries() {
        return map.size();
    }

    /**
     * 返回缓存条目的副本集合。
     *
     * @return 条目快照
     */
    public synchronized Collection<Map.Entry<K, V>> getAll() {
        return new ArrayList<Map.Entry<K, V>>(map.entrySet());
    }

} // end class LRUCache
