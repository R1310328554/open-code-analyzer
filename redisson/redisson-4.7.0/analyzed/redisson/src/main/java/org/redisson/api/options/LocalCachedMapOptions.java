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
 * {@link org.redisson.api.RLocalCachedMap} 本地缓存 Map 的配置选项。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface LocalCachedMapOptions<K, V> extends ExMapOptions<LocalCachedMapOptions<K, V>, K, V> {
    
    /**
     * 重连后避免本地缓存出现过期数据的策略。
     * 适用于 Map 实例与 Redis 断开连接一段时间后再恢复的场景。
     *
     */
    enum ReconnectionStrategy {
        
        /** 不处理重连场景。 */
        NONE,
        
        /** Map 实例断连后清空本地缓存。 */
        CLEAR,
        
        /** 将被淘汰条目的哈希写入失效日志并保留 10 分钟。
         * 若 LocalCachedMap 断连不足 10 分钟，则移除日志中对应键；
         * 否则清空整个本地缓存。 */
        LOAD
        
    }
    
    enum SyncStrategy {
        
        /** Map 变更时不进行跨实例同步。 */
        NONE,
        
        /** Map 条目变更时向所有 LocalCachedMap 实例广播 16 字节条目哈希以失效本地缓存。 */
        INVALIDATE,
        
        /** Map 条目变更时向所有 LocalCachedMap 实例广播完整键值以更新本地缓存。 */
        UPDATE
        
    }
    
    enum EvictionPolicy {
        
        /** 本地缓存不使用淘汰策略。 */
        NONE, 
        
        /** 本地缓存采用 LRU（最近最少使用）淘汰策略。 */
        LRU, 
        
        /** 本地缓存采用 LFU（最不经常使用）淘汰策略。 */
        LFU, 
        
        /** 本地缓存值使用软引用，内存不足时由 GC 回收。 */
        SOFT, 

        /** 本地缓存值使用弱引用，变为弱可达时由 GC 回收。 */
        WEAK
    };

    enum CacheProvider {

        REDISSON,

        CAFFEINE

    }

    enum StoreMode {

        /** 数据仅存储在本地缓存中。 */
        LOCALCACHE,

        /** 数据同时存储在 Redis 与本地缓存中。 */
        LOCALCACHE_REDIS

    }

    enum ExpirationEventPolicy {

        /** 不订阅键过期事件。 */
        DONT_SUBSCRIBE,

        /** 通过 __keyevent@*:expired 模式订阅过期事件。 */
        SUBSCRIBE_WITH_KEYEVENT_PATTERN,

        /** 通过 __keyspace@N__:name 频道订阅过期事件。 */
        SUBSCRIBE_WITH_KEYSPACE_CHANNEL

    }

    /**
     * 按对象实例名称创建配置。
     *
     * @param name 对象实例名称
     * @return 配置实例
     */
    static <K, V> LocalCachedMapOptions<K, V> name(String name) {
        return new LocalCachedMapParams<>(name);
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
    LocalCachedMapOptions<K, V> cacheSize(int cacheSize);
    
    /**
     * 设置 Redis 连接失败后补加载本地缓存变更的策略。
     *
     * @param reconnectionStrategy
     *          <p><code>CLEAR</code> - Map 断连一段时间后清空本地缓存。
     *          <p><code>LOAD</code> - 将失效条目哈希写入日志保留 10 分钟；断连不足 10 分钟则移除对应键，否则清空整个缓存。
     *          <p><code>NONE</code> - 默认，不处理重连。
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> reconnectionStrategy(ReconnectionStrategy reconnectionStrategy);

    /**
     * 设置本地缓存跨实例同步策略。
     *
     * @param syncStrategy
     *          <p><code>INVALIDATE</code> - 默认，Map 条目变更时失效所有实例的本地缓存条目。
     *          <p><code>UPDATE</code> - Map 条目变更时更新所有实例的本地缓存条目。
     *          <p><code>NONE</code> - Map 变更时不进行同步。
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> syncStrategy(SyncStrategy syncStrategy);
    
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
    LocalCachedMapOptions<K, V> evictionPolicy(EvictionPolicy evictionPolicy);
    
    /**
     * 设置本地缓存中每条 Map 条目的存活时间（毫秒）。
     * 若值为 <code>0</code> 则不应用超时。
     * 
     * @param ttl 存活时间
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> timeToLive(Duration ttl);

    /**
     * 设置本地缓存中每条 Map 条目的最大空闲时间（毫秒）。
     * 若值为 <code>0</code> 则不应用超时。
     * 
     * @param idleTime 最大空闲时间
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> maxIdle(Duration idleTime);

    /**
     * 设置缓存数据的存储模式。
     *
     * @param storeMode <p><code>LOCALCACHE</code> - 数据仅存储在本地缓存。
     *                  <p><code>LOCALCACHE_REDIS</code> - 数据同时存储在 Redis 与本地缓存。
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> storeMode(StoreMode storeMode);

    /**
     * 设置用作本地缓存存储的实现提供方。
     *
     * @param cacheProvider <p><code>REDISSON</code> - 使用 Redisson 内置实现。
     *                      <p><code>CAFFEINE</code> - 使用 Caffeine 实现。
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> cacheProvider(CacheProvider cacheProvider);

    /**
     * 设置是否将缓存未命中（cache miss）也写入本地缓存。
     *
     * @param storeCacheMiss 是否缓存未命中结果
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> storeCacheMiss(boolean storeCacheMiss);

    /**
     * 已废弃，请改用 {@link #expirationEventPolicy(ExpirationEventPolicy)}。
     *
     * @param useKeyEventsPattern 是否使用 __keyevent 模式主题
     * @return LocalCachedMapOptions 配置实例
     */
    @Deprecated
    LocalCachedMapOptions<K, V> useKeyEventsPattern(boolean useKeyEventsPattern);

    /**
     * 设置如何监听 Redis 在本实例键被删除/过期时发送的过期事件。
     *
     * @param expirationEventPolicy 过期事件订阅策略
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> expirationEventPolicy(ExpirationEventPolicy expirationEventPolicy);

    /**
     * 设置是否将对象键的 CacheKey 存入本地缓存。<br>
     * 仅当 {@link #cacheProvider(CacheProvider)} 不为 CAFFEINE 时生效。
     *
     * @param useObjectAsCacheKey 是否以 CacheKey 形式缓存对象键
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> useObjectAsCacheKey(boolean useObjectAsCacheKey);

    /**
     * 设置是否使用全局主题模式监听器，
     * 对同一 Redisson 实例下所有本地缓存实例生效。
     *
     * @param value 是否启用全局主题模式监听
     * @return LocalCachedMapOptions 配置实例
     */
    LocalCachedMapOptions<K, V> useTopicPattern(boolean value);

}
