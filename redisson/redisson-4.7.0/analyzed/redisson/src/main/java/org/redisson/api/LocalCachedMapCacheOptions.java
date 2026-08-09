/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapLoaderAsync;
import org.redisson.api.map.MapWriter;
import org.redisson.api.map.MapWriterAsync;

import java.util.concurrent.TimeUnit;

/**
 * {@link RLocalCachedMapCache} 本地缓存 Map 配置选项（含逐条 TTL）。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class LocalCachedMapCacheOptions<K, V> extends MapCacheOptions<K, V> {

    /**
     * 避免本地缓存出现过期数据的策略。
     * 处理 Map 实例断开连接一段时间后的缓存一致性问题。
     *
     */
    public enum ReconnectionStrategy {

        /**
         * 不进行重连处理。
         */
        NONE,

        /**
         * Map 实例断连后清空本地缓存。
         */
        CLEAR,

        /**
         * 将失效条目哈希写入失效日志并保留 10 分钟。
         * 若断连时间少于 10 分钟，则移除已记录失效哈希对应的缓存键；
         * 否则清空整个本地缓存。
         * （见上条）
         */
        LOAD

    }

    public enum SyncStrategy {

        /**
         * No synchronizations on map changes.
         */
        NONE,

        /**
         * Invalidate local cache entry across all LocalCachedMap instances on map entry change. Broadcasts map entry hash (16 bytes) to all instances.
         */
        INVALIDATE,

        /**
         * Update local cache entry across all LocalCachedMap instances on map entry change. Broadcasts full map entry state (Key and Value objects) to all instances.
         */
        UPDATE

    }

    public enum EvictionPolicy {

        /**
         * Local cache without eviction.
         */
        NONE,

        /**
         * Least Recently Used local cache eviction policy.
         */
        LRU,

        /**
         * Least Frequently Used local cache eviction policy.
         */
        LFU,

        /**
         * Local cache  eviction policy with Soft Reference used for values.
         * 所有引用最终由 GC 回收。
         */
        SOFT,

        /**
         * Local cache eviction policy with Weak Reference used for values.
         * 所有引用最终由 GC 回收。
         */
        WEAK
    };

    public enum CacheProvider {

        REDISSON,

        CAFFEINE

    }

    public enum StoreMode {

        /**
         * Store data only in local cache.
         */
        LOCALCACHE,

        /**
         * Store data only in both Redis and local cache.
         */
        LOCALCACHE_REDIS

    }

    private ReconnectionStrategy reconnectionStrategy;
    private SyncStrategy syncStrategy;
    private EvictionPolicy evictionPolicy;
    private int cacheSize;
    private long timeToLiveInMillis;
    private long maxIdleInMillis;
    private CacheProvider cacheProvider;
    private StoreMode storeMode;
    private boolean storeCacheMiss;
    private boolean useKeyEventsPattern;

    protected LocalCachedMapCacheOptions() {
    }

    protected LocalCachedMapCacheOptions(LocalCachedMapCacheOptions<K, V> copy) {
        this.reconnectionStrategy = copy.reconnectionStrategy;
        this.syncStrategy = copy.syncStrategy;
        this.evictionPolicy = copy.evictionPolicy;
        this.cacheSize = copy.cacheSize;
        this.timeToLiveInMillis = copy.timeToLiveInMillis;
        this.maxIdleInMillis = copy.maxIdleInMillis;
        this.cacheProvider = copy.cacheProvider;
        this.storeMode = copy.storeMode;
        this.storeCacheMiss = copy.storeCacheMiss;
    }
    
    /**
     * 创建带默认选项的 LocalCachedMapOptions 实例。
     * <p>
     * 等价于：
     * <pre>
     *     new LocalCachedMapOptions()
     *      .cacheSize(0).timeToLive(0).maxIdle(0)
     *      .evictionPolicy(EvictionPolicy.NONE)
     *      .reconnectionStrategy(ReconnectionStrategy.NONE)
     *      .cacheProvider(CacheProvider.REDISSON)
     *      .syncStrategy(SyncStrategy.INVALIDATE)
     *      .storeCacheMiss(false);
     * </pre>
     * 
     * @param <K> 键类型
     * @param <V> 值类型
     * 
     * @return LocalCachedMapOptions 实例
     * 
     */
    public static <K, V> LocalCachedMapCacheOptions<K, V> defaults() {
        return new LocalCachedMapCacheOptions<K, V>()
                    .cacheSize(0).timeToLive(0).maxIdle(0)
                    .evictionPolicy(EvictionPolicy.NONE)
                    .reconnectionStrategy(ReconnectionStrategy.NONE)
                    .cacheProvider(CacheProvider.REDISSON)
                    .storeMode(StoreMode.LOCALCACHE_REDIS)
                    .syncStrategy(SyncStrategy.INVALIDATE)
                    .storeCacheMiss(false)
                    .useKeyEventsPattern(true);
    }

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public long getTimeToLiveInMillis() {
        return timeToLiveInMillis;
    }

    public long getMaxIdleInMillis() {
        return maxIdleInMillis;
    }

    /**
     * 定义本地缓存容量。
     * <p>
     * 容量为 {@code 0} 表示无界缓存。
     * <p>
     * 容量为 {@code -1} 表示始终为空、不存储数据。
     * 
     * @param cacheSize 缓存容量
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> cacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }
    
    public ReconnectionStrategy getReconnectionStrategy() {
        return reconnectionStrategy;
    }
    
    public SyncStrategy getSyncStrategy() {
        return syncStrategy;
    }

    /**
     * 定义 Redis 连接失败后加载遗漏本地缓存更新的策略。
     *
     * @param reconnectionStrategy 重连策略
     *          <p>{@code CLEAR} — 断连一段时间后清空本地缓存。
     *          <p>{@code LOAD} — 失效条目哈希写入 10 分钟失效日志；断连不足 10 分钟时移除对应缓存键，否则清空全部本地缓存。
     *          <p>{@code NONE} — 默认；不做重连处理。
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> reconnectionStrategy(ReconnectionStrategy reconnectionStrategy) {
        if (reconnectionStrategy == null) {
            throw new NullPointerException("reconnectionStrategy can't be null");
        }

        this.reconnectionStrategy = reconnectionStrategy;
        return this;
    }

    /**
     * 定义本地缓存同步策略。
     *
     * @param syncStrategy 同步策略
     *          <p>{@code INVALIDATE} — 默认；Map 条目变更时使所有节点本地缓存失效。
     *          <p>{@code UPDATE} — Map 条目变更时在所有节点插入/更新缓存条目。
     *          <p>{@code NONE} — Map 变更时不做缓存同步。
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> syncStrategy(SyncStrategy syncStrategy) {
        if (syncStrategy == null) {
            throw new NullPointerException("syncStrategy can't be null");
        }

        this.syncStrategy = syncStrategy;
        return this;
    }
    
    /**
     * 定义本地缓存淘汰策略。
     * 
     * @param evictionPolicy 淘汰策略
     *         <p>{@code LRU} — 最近最少使用（LRU）淘汰。
     *         <p>{@code LFU} — 最不经常使用（LFU）淘汰。
     *         <p>{@code SOFT} — 软引用缓存；JVM 内存不足时 GC 回收条目。
     *         <p>{@code WEAK} — 弱引用缓存；条目变为弱可达时 GC 回收。
     *         <p>{@code NONE} — 不使用淘汰策略，但 timeToLive 与 maxIdleTime 仍生效。
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> evictionPolicy(EvictionPolicy evictionPolicy) {
        if (evictionPolicy == null) {
            throw new NullPointerException("evictionPolicy can't be null");
        }
        this.evictionPolicy = evictionPolicy;
        return this;
    }
    
    /**
     * 定义本地缓存每条目的存活时间（毫秒）。
     * 值为 {@code 0} 表示不应用超时。
     * 
     * @param timeToLiveInMillis 存活时间（毫秒）
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> timeToLive(long timeToLiveInMillis) {
        this.timeToLiveInMillis = timeToLiveInMillis;
        return this;
    }

    /**
     * 定义本地缓存每条目的存活时间。
     * 值为 {@code 0} 表示不应用超时。
     * 
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> timeToLive(long timeToLive, TimeUnit timeUnit) {
        return timeToLive(timeUnit.toMillis(timeToLive));
    }

    /**
     * 定义本地缓存每条目的最大空闲时间（毫秒）。
     * 值为 {@code 0} 表示不应用超时。
     * 
     * @param maxIdleInMillis 最大空闲时间（毫秒）
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> maxIdle(long maxIdleInMillis) {
        this.maxIdleInMillis = maxIdleInMillis;
        return this;
    }

    /**
     * 定义本地缓存每条目的最大空闲时间。
     * 值为 {@code 0} 表示不应用超时。
     * 
     * @param maxIdle 最大空闲时间
     * @param timeUnit 时间单位
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> maxIdle(long maxIdle, TimeUnit timeUnit) {
        return maxIdle(timeUnit.toMillis(maxIdle));
    }

    public StoreMode getStoreMode() {
        return storeMode;
    }

    /**
     * 定义缓存数据的存储模式。
     *
     * @param storeMode 存储模式
     *         <p>{@code LOCALCACHE} — 仅存储在本地缓存。
     *         <p>{@code LOCALCACHE_REDIS} — 同时存储在 Redis 与本地缓存。
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> storeMode(StoreMode storeMode) {
        this.storeMode = storeMode;
        return this;
    }

    /**
     * 指定本地缓存存储提供方。
     *
     * @param cacheProvider 缓存提供方
     *         <p>{@code REDISSON} — 使用 Redisson 内置实现。
     *         <p>{@code CAFFEINE} — 使用 Caffeine 实现。
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> cacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
        return this;
    }

    public boolean isStoreCacheMiss() {
        return this.storeCacheMiss;
    }

    /**
     * 是否将缓存未命中（cache miss）结果存入本地缓存。
     *
     * @param storeCacheMiss - whether to store a cache miss into the local cache
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> storeCacheMiss(boolean storeCacheMiss) {
        this.storeCacheMiss = storeCacheMiss;
        return this;
    }

    public boolean isUseKeyEventsPattern() {
        return useKeyEventsPattern;
    }

    /**
     * Defines whether to use __keyevent pattern topic to listen for expired events.
     *
     * @param useKeyEventsPattern - whether to use __keyevent pattern topic
     * @return LocalCachedMapOptions 实例
     */
    public LocalCachedMapCacheOptions<K, V> useKeyEventsPattern(boolean useKeyEventsPattern) {
        this.useKeyEventsPattern = useKeyEventsPattern;
        return this;
    }

    @Override
    public LocalCachedMapCacheOptions<K, V> writeBehindBatchSize(int writeBehindBatchSize) {
        return (LocalCachedMapCacheOptions<K, V>) super.writeBehindBatchSize(writeBehindBatchSize);
    }
    
    @Override
    public LocalCachedMapCacheOptions<K, V> writeBehindDelay(int writeBehindDelay) {
        return (LocalCachedMapCacheOptions<K, V>) super.writeBehindDelay(writeBehindDelay);
    }
    
    @Override
    public LocalCachedMapCacheOptions<K, V> writer(MapWriter<K, V> writer) {
        return (LocalCachedMapCacheOptions<K, V>) super.writer(writer);
    }

    @Override
    public LocalCachedMapCacheOptions<K, V> writerAsync(MapWriterAsync<K, V> writer) {
        return (LocalCachedMapCacheOptions<K, V>) super.writerAsync(writer);
    }

    @Override
    public LocalCachedMapCacheOptions<K, V> writeMode(WriteMode writeMode) {
        return (LocalCachedMapCacheOptions<K, V>) super.writeMode(writeMode);
    }
    
    @Override
    public LocalCachedMapCacheOptions<K, V> loader(MapLoader<K, V> loader) {
        return (LocalCachedMapCacheOptions<K, V>) super.loader(loader);
    }

    @Override
    public LocalCachedMapCacheOptions<K, V> loaderAsync(MapLoaderAsync<K, V> loaderAsync) {
        return (LocalCachedMapCacheOptions<K, V>) super.loaderAsync(loaderAsync);
    }
}
