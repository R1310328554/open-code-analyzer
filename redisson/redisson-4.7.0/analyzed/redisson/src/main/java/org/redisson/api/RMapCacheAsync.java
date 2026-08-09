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
import org.redisson.api.map.MapWriter;
import org.redisson.api.map.event.MapEntryListener;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@link RMapCache} 异步 API；各方法返回 {@link RFuture}。
 * <p>支持逐条目 TTL、LRU 容量淘汰与 MapLoader/MapWriter。
 * <p>过期条目由 {@link org.redisson.eviction.EvictionScheduler} 异步清理。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RMapCacheAsync<K, V> extends RMapAsync<K, V>, RLeasedMapAsync<K, V> {

    /**
     * 设置 Map 最大容量并覆盖当前配置。
     * 超出容量时默认按 LRU 算法淘汰条目。
     * 
     * @param maxSize 最大容量
     * @return 无返回值
     */
    RFuture<Void> setMaxSizeAsync(int maxSize);

    /**
     * 设置 Map 最大容量并覆盖当前配置。
     * 超出容量时按指定淘汰算法移除条目。
     *
     * @param maxSize 最大容量
     * @param mode 淘汰模式
     * @return 无返回值
     */
    RFuture<Void> setMaxSizeAsync(int maxSize, EvictionMode mode);

    /**
     * 尝试设置 Map 最大容量。 
     * 超出容量时默认按 LRU 算法淘汰条目。
     *
     * @param maxSize 最大容量
     * @return 设置成功则为 true，否则 false
     */
    RFuture<Boolean> trySetMaxSizeAsync(int maxSize);

    /**
     * 尝试设置 Map 最大容量。
     * 超出容量时按指定淘汰算法移除条目。
     *
     * @param maxSize 最大容量
     * @param mode 淘汰模式
     * @return 设置成功则为 true，否则 false
     */
    RFuture<Boolean> trySetMaxSizeAsync(int maxSize, EvictionMode mode);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     * @param unit 时间单位
     * @return 先前关联的值
     */
    RFuture<V> putIfAbsentAsync(K key, V value, long ttl, TimeUnit unit);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL 与最大空闲时间。
     * Entry expires when specified time to live or max idle time has expired.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param ttlUnit - time unit
     * @param maxIdleTime - max idle time for key\value entry.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * @param maxIdleUnit - time unit
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return 先前关联的值
     */
    RFuture<V> putIfAbsentAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key map key
     * @param value map value
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     * @param unit time unit
     * @return 先前关联的值
     */
    RFuture<V> putAsync(K key, V value, long ttl, TimeUnit unit);

    /**
     * 存储键值对并设置 TTL 与最大空闲时间。
     * Entry expires when specified time to live or max idle time has expired.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key map key
     * @param value map value
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param ttlUnit time unit
     * @param maxIdleTime max idle time for key\value entry.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * @param maxIdleUnit time unit
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return 先前关联的值
     */
    RFuture<V> putAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit);

    /**
     * 将指定 {@code value} 关联到 {@code key}
     * in batch.
     * <p>
     * If {@link MapWriter} is defined then new map entries are stored in write-through mode.
     *
     * @param map mappings to be stored in this map
     * @param ttl time to live for all key\value entries.
     *              If <code>0</code> then stores infinitely.
     * @param ttlUnit time unit
     * @return 无返回值
     */
    RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, long ttl, TimeUnit ttlUnit);

    /**
     * 将指定 {@code value} 关联到 {@code key}
     * in batch.
     * <p>
     * If {@link MapWriter} is defined then new map entries are stored in write-through mode.
     *
     * @param map mappings to be stored in this map
     * @param ttl time to live for all key\value entries.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param ttlUnit time unit
     * @param maxIdleTime - max idle time for all key\value entries.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * @param maxIdleUnit time unit
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entries store infinitely.
     *
     * @return 无返回值
     */
    RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, long ttl, TimeUnit ttlUnit,
                              long maxIdleTime, TimeUnit maxIdleUnit);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     * <p>
     * Works faster than usual {@link #putAsync(Object, Object, long, TimeUnit)}
     * as it not returns previous value.
     *
     * @param key map key
     * @param value map value
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     * @param unit time unit
     * 
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and the value was updated.
     */
    RFuture<Boolean> fastPutAsync(K key, V value, long ttl, TimeUnit unit);

    /**
     * 存储键值对并设置 TTL 与最大空闲时间。
     * Entry expires when specified time to live or max idle time has expired.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     * <p>
     * Works faster than usual {@link #putAsync(Object, Object, long, TimeUnit, long, TimeUnit)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param ttlUnit - time unit
     * @param maxIdleTime - max idle time for key\value entry.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * @param maxIdleUnit - time unit
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.

     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and the value was updated.
     */
    RFuture<Boolean> fastPutAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit);
    
    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL 与最大空闲时间。
     * Entry expires when specified time to live or max idle time has expired.
     * <p>
     * Works faster than usual {@link #putIfAbsentAsync(Object, Object, long, TimeUnit, long, TimeUnit)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param ttlUnit - time unit
     * @param maxIdleTime - max idle time for key\value entry.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * @param maxIdleUnit - time unit
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash
     */
    RFuture<Boolean> fastPutIfAbsentAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit);

    /**
     * If the specified key is not already associated
     * with a value, attempts to compute its value using the given mapping function and enters it into this map .
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     * @param mappingFunction the mapping function to compute a value
     * @return current associated value
     */
    RFuture<V> computeIfAbsentAsync(K key, Duration ttl, Function<? super K, ? extends V> mappingFunction);

    /**
     * 根据键及其当前映射值计算新映射。
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     * @param remappingFunction - function to compute a value
     * @return the new value associated with the specified key, or {@code null} if none
     */
    RFuture<V> computeAsync(K key, Duration ttl, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * Use {@link #expireEntryAsync(Object, Duration, Duration)} instead.
     *
     * @param key 映射键
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param ttlUnit - time unit
     * @param maxIdleTime - max idle time for key\value entry.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * @param maxIdleUnit - time unit
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already expired or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    @Deprecated
    RFuture<Boolean> updateEntryExpirationAsync(K key, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit);

    /**
     * 更新指定键条目的 TTL 与最大空闲时间。
     * Entry expires when specified time to live or max idle time was reached.
     * <p>
     * Returns <code>false</code> if entry already expired or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key 映射键
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param maxIdleTime - max idle time for key\value entry.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already expired or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryAsync(K key, Duration ttl, Duration  maxIdleTime);

    /**
     * 更新指定键集合条目的 TTL 与最大空闲时间。
     * Entries expires when specified time to live or max idle time was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param ttl time to live for key\value entries.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * @param maxIdleTime max idle time for key\value entries.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entries are stored infinitely.
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesAsync(Set<K> keys, Duration ttl, Duration maxIdleTime);

    /**
     * 返回 {@code key} 映射的值；不存在时返回 {@code null}。
     * <p>
     * If map doesn't contain value for specified key and {@link MapLoader} is defined
     * then value will be loaded in read-through mode.
     * <p>
     * NOTE: Idle time of entry is not taken into account.
     * Entry last access time isn't modified if map limited by size.
     *
     * @param key 键
     * @return the value mapped by defined <code>key</code> or {@code null} if value is absent
     */
    RFuture<V> getWithTTLOnlyAsync(K key);

    /**
     * 返回指定 {@code keys} 对应的 Map 切片。
     * <p>
     * If map doesn't contain value/values for specified key/keys and {@link MapLoader} is defined
     * then value/values will be loaded in read-through mode.
     * <p>
     * NOTE: Idle time of entry is not taken into account.
     * Entry last access time isn't modified if map limited by size.
     *
     * @param keys map keys
     * @return Map slice
     */
    RFuture<Map<K, V>> getAllWithTTLOnlyAsync(Set<K> keys);

    /**
     * 返回缓存中的条目数量。
     * This number can reflects expired entries too
     * due to non realtime cleanup process.
     *
     */
    @Override
    RFuture<Integer> sizeAsync();
    
    /**
     * 返回指定键对应条目的剩余 TTL。 
     *
     * @param key 映射键
     * @return time in milliseconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    RFuture<Long> remainTimeToLiveAsync(K key);

    /**
     * Adds map entry listener
     *
     * @see org.redisson.api.map.event.EntryCreatedListener
     * @see org.redisson.api.map.event.EntryUpdatedListener
     * @see org.redisson.api.map.event.EntryRemovedListener
     * @see org.redisson.api.map.event.EntryExpiredListener
     *
     * @param listener - entry listener
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(MapEntryListener listener);

}
