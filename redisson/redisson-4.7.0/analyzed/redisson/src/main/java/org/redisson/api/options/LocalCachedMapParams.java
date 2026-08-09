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
package org.redisson.api.options;

import java.time.Duration;

/**
 * {@link LocalCachedMapOptions} 的可变配置实现，保存本地缓存 Map 的全部运行时参数。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public final class LocalCachedMapParams<K, V> extends BaseMapOptions<LocalCachedMapOptions<K, V>, K, V> implements LocalCachedMapOptions<K, V> {

    private final String name;
    private ReconnectionStrategy reconnectionStrategy = ReconnectionStrategy.NONE;
    private SyncStrategy syncStrategy = SyncStrategy.INVALIDATE;
    private EvictionPolicy evictionPolicy = EvictionPolicy.NONE;
    private int cacheSize;
    private long timeToLiveInMillis;
    private long maxIdleInMillis;
    private CacheProvider cacheProvider = CacheProvider.REDISSON;
    private StoreMode storeMode = StoreMode.LOCALCACHE_REDIS;
    private boolean storeCacheMiss;

    private ExpirationEventPolicy expirationEventPolicy = ExpirationEventPolicy.SUBSCRIBE_WITH_KEYEVENT_PATTERN;

    private boolean useObjectAsCacheKey;
    private boolean useTopicPattern;

    LocalCachedMapParams(String name) {
        this.name = name;
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
     * 设置本地缓存容量。
     * <p>
     * 若 size 为 <code>0</code>，则本地缓存无上限。
     * <p>
     * 若 size 为 <code>-1</code>，则本地缓存始终为空且不存储数据。
     * 
     * @param cacheSize 缓存容量
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> cacheSize(int cacheSize) {
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
     * 设置 Redis 连接失败后补加载本地缓存变更的策略。
     *
     * @param reconnectionStrategy
     *          <p><code>CLEAR</code> - Map 断连一段时间后清空本地缓存。
     *          <p><code>LOAD</code> - 将失效条目哈希写入日志保留 10 分钟；断连不足 10 分钟则移除对应键，否则清空整个缓存。
     *          <p><code>NONE</code> - 默认，不处理重连。
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> reconnectionStrategy(ReconnectionStrategy reconnectionStrategy) {
        if (reconnectionStrategy == null) {
            throw new NullPointerException("reconnectionStrategy can't be null");
        }

        this.reconnectionStrategy = reconnectionStrategy;
        return this;
    }

    /**
     * 设置本地缓存跨实例同步策略。
     *
     * @param syncStrategy
     *          <p><code>INVALIDATE</code> - 默认，Map 条目变更时失效所有实例的本地缓存条目。
     *          <p><code>UPDATE</code> - Map 条目变更时更新所有实例的本地缓存条目。
     *          <p><code>NONE</code> - Map 变更时不进行同步。
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> syncStrategy(SyncStrategy syncStrategy) {
        if (syncStrategy == null) {
            throw new NullPointerException("syncStrategy can't be null");
        }

        this.syncStrategy = syncStrategy;
        return this;
    }
    
    /**
     * 设置本地缓存淘汰策略。
     * 
     * @param evictionPolicy
     *         <p><code>LRU</code> - 使用 LRU（最近最少使用）淘汰策略。
     *         <p><code>LFU</code> - 使用 LFU（最不经常使用）淘汰策略。
     *         <p><code>SOFT</code> - 值使用软引用，JVM 内存不足时由 GC 淘汰。
     *         <p><code>WEAK</code> - 值使用弱引用，变为弱可达时由 GC 淘汰。
     *         <p><code>NONE</code> - 不使用淘汰策略，但 timeToLive 与 maxIdleTime 仍生效。
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> evictionPolicy(EvictionPolicy evictionPolicy) {
        if (evictionPolicy == null) {
            throw new NullPointerException("evictionPolicy can't be null");
        }
        this.evictionPolicy = evictionPolicy;
        return this;
    }
    
    /**
     * 设置本地缓存中每条 Map 条目的存活时间（毫秒）。
     * 若值为 <code>0</code> 则不应用超时。
     * 
     * @param ttl 存活时间
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> timeToLive(Duration ttl) {
        this.timeToLiveInMillis = ttl.toMillis();
        return this;
    }

    /**
     * 设置本地缓存中每条 Map 条目的最大空闲时间（毫秒）。
     * 若值为 <code>0</code> 则不应用超时。
     * 
     * @param idleTime 最大空闲时间
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> maxIdle(Duration idleTime) {
        this.maxIdleInMillis = idleTime.toMillis();
        return this;
    }

    public StoreMode getStoreMode() {
        return storeMode;
    }

    /**
     * 设置缓存数据的存储模式。
     *
     * @param storeMode
     *         <p><code>LOCALCACHE</code> - 数据仅存储在本地缓存。
     *         <p><code>LOCALCACHE_REDIS</code> - 数据同时存储在 Redis 与本地缓存。
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> storeMode(StoreMode storeMode) {
        this.storeMode = storeMode;
        return this;
    }

    /**
     * 设置用作本地缓存存储的实现提供方。
     *
     * @param cacheProvider
     *         <p><code>REDISSON</code> - 使用 Redisson 内置实现。
     *         <p><code>CAFFEINE</code> - 使用 Caffeine 实现。
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> cacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
        return this;
    }

    public boolean isStoreCacheMiss() {
        return this.storeCacheMiss;
    }

    /**
     * 设置是否将缓存未命中（cache miss）也写入本地缓存。
     *
     * @param storeCacheMiss 是否缓存未命中结果
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> storeCacheMiss(boolean storeCacheMiss) {
        this.storeCacheMiss = storeCacheMiss;
        return this;
    }

    /**
     * 设置是否通过 __keyevent 模式主题监听过期事件。
     *
     * @param useKeyEventsPattern 是否使用 __keyevent 模式主题
     * @return LocalCachedMapOptions 配置实例
     */
    public LocalCachedMapParams<K, V> useKeyEventsPattern(boolean useKeyEventsPattern) {
        if (useKeyEventsPattern) {
            this.expirationEventPolicy = ExpirationEventPolicy.SUBSCRIBE_WITH_KEYEVENT_PATTERN;
        } else {
            this.expirationEventPolicy = ExpirationEventPolicy.SUBSCRIBE_WITH_KEYSPACE_CHANNEL;
        }
        return this;
    }

    @Override
    public LocalCachedMapOptions<K, V> expirationEventPolicy(ExpirationEventPolicy expirationEventPolicy) {
        this.expirationEventPolicy = expirationEventPolicy;
        return this;
    }

    @Override
    public LocalCachedMapOptions<K, V> useObjectAsCacheKey(boolean useObjectAsCacheKey) {
        this.useObjectAsCacheKey = useObjectAsCacheKey;
        return this;
    }

    @Override
    public LocalCachedMapOptions<K, V> useTopicPattern(boolean value) {
        this.useTopicPattern = value;
        return this;
    }

    public ExpirationEventPolicy getExpirationEventPolicy() {
        return expirationEventPolicy;
    }

    public String getName() {
        return name;
    }

    public boolean isUseObjectAsCacheKey() {
        return useObjectAsCacheKey;
    }

    public boolean isUseTopicPattern() {
        return useTopicPattern;
    }
}
