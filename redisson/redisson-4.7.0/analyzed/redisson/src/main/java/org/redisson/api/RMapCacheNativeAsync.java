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

import java.time.Instant;
import org.redisson.api.map.MapWriter;
import org.redisson.api.map.PutArgs;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 基于 Redis 原生过期语义的 {@link RMapCacheNative} API。
 * <p>条目 TTL 由 Redis 服务器维护，无需定时扫描淘汰任务。
 * <p>需要 <b>Redis 7.4.0 及以上</b>。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RMapCacheNativeAsync<K, V> extends RMapAsync<K, V> {

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     * @return 先前关联的值
     */
    RFuture<V> putAsync(K key, V value, Duration ttl);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time expire date
     * @return 先前关联的值
     */
    RFuture<V> putAsync(K key, V value, Instant time);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     * <p>
     * Works faster than usual {@link #putAsync(Object, Object, Duration)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     *
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and the value was updated.
     */
    RFuture<Boolean> fastPutAsync(K key, V value, Duration ttl);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     * <p>
     * Works faster than usual {@link #putAsync(Object, Object, Duration)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time expire date
     *
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and the value was updated.
     */
    RFuture<Boolean> fastPutAsync(K key, V value, Instant time);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     *
     * @return current associated value
     */
    RFuture<V> putIfAbsentAsync(K key, V value, Duration ttl);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time expire date
     *
     * @return current associated value
     */
    RFuture<V> putIfAbsentAsync(K key, V value, Instant time);

    /**
     * Stores the specified {@code value} mapped by {@code key}
     * only if mapping already exists.
     * <p>
     * Specified time to live starts from the moment this method call was completed.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live
     * @return 先前关联的值
     *         or {@code null} if key doesn't exist
     */
    RFuture<V> putIfExistAsync(K key, V value, Duration ttl);

    /**
     * Stores the specified {@code value} mapped by {@code key}
     * only if mapping already exists.
     * <p>
     * Entry expires at specified instant.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time - expiration instant
     * @return 先前关联的值
     *         or {@code null} if key doesn't exist
     */
    RFuture<V> putIfExistAsync(K key, V value, Instant time);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * Works faster than usual {@link #putIfAbsentAsync(Object, Object, Duration)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param ttl - time to live for key\value entry.
     *              If <code>0</code> then stores infinitely.
     *
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash
     */
    RFuture<Boolean> fastPutIfAbsentAsync(K key, V value, Duration ttl);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * Works faster than usual {@link #putIfAbsentAsync(Object, Object, Duration)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time expire date
     *
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash
     */
    RFuture<Boolean> fastPutIfAbsentAsync(K key, V value, Instant time);

    /**
     * 返回指定键对应条目的剩余 TTL。
     *
     * @param key 映射键
     * @return time in milliseconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    RFuture<Long> remainTimeToLiveAsync(K key);

    RFuture<Map<K, Long>> remainTimeToLiveAsync(Set<K> keys);

    /**
     * Use {@link #putAllAsync(PutArgs)} method instead.
     *
     * @param map - mappings to be stored in this map
     * @param ttl - time to live for all key\value entries.
     *              If <code>0</code> then stores infinitely.
     */
    @Deprecated
    RFuture<Void> putAllAsync(java.util.Map<? extends K, ? extends V> map, Duration ttl);

    /**
     * 存储 {@code args} 中指定的 Map 条目。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     * <p>
     * If {@link MapWriter} is defined then new map entries will be stored in write-through mode.
     *
     * @param args put arguments
     */
    RFuture<Void> putAllAsync(PutArgs<K, V> args);

    /**
     * 仅当全部指定键已存在时存储条目。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param args put arguments
     * @return {@code true} if all entries were set, {@code false} otherwise
     */
    RFuture<Boolean> putIfAllKeysExistAsync(PutArgs<K, V> args);

    /**
     * 仅当全部指定键不存在时存储条目。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param args put arguments
     * @return {@code true} if all entries were set, {@code false} otherwise
     */
    RFuture<Boolean> putIfAllKeysAbsentAsync(PutArgs<K, V> args);

    /**
     * 清除指定键条目的过期时间。
     *
     * @param key map key
     * @return <code>true</code> if timeout was removed
     *         <code>false</code> if entry does not have an associated timeout
     *         <code>null</code> if entry does not exist
     */
    RFuture<Boolean> clearExpireAsync(K key);

    /**
     * 清除指定键集合条目的过期时间。
     *
     * @param keys map keys
     * @return Boolean mapped by key.
     *         <code>true</code> if timeout was removed
     *         <code>false</code> if entry does not have an associated timeout
     *         <code>null</code> if entry does not exist
     */
    RFuture<Map<K, Boolean>> clearExpireAsync(Set<K> keys);

    /**
     * 更新指定键条目的 TTL 与最大空闲时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already expired or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already expired or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryAsync(K key, Duration ttl);

    /**
     * 更新指定键条目的 TTL 与最大空闲时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already expired or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already expired or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryAsync(K key, Instant time);

    /**
     * 为指定键条目设置 TTL 与最大空闲时间。
     * If these parameters weren't set before.
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryIfNotSetAsync(K key, Duration ttl);

    /**
     * 为指定键条目设置 TTL 与最大空闲时间。
     * If these parameters weren't set before.
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryIfNotSetAsync(K key, Instant time);

    /**
     * 仅当新 TTL 大于已有 TTL 时为指定键条目设置过期时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryIfGreaterAsync(K key, Duration ttl);

    /**
     * 仅当新 TTL 大于已有 TTL 时为指定键条目设置过期时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryIfGreaterAsync(K key, Instant time);

    /**
     * 仅当新 TTL 小于已有 TTL 时为指定键条目设置过期时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryIfLessAsync(K key, Duration ttl);

    /**
     * 仅当新 TTL 小于已有 TTL 时为指定键条目设置过期时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    RFuture<Boolean> expireEntryIfLessAsync(K key, Instant time);

    /**
     * 更新指定键集合条目的 TTL 与最大空闲时间。
     * Entries expires when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param ttl time to live for key\value entries.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entries are stored infinitely.
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesAsync(Set<K> keys, Duration ttl);

    /**
     * 更新指定键集合条目的 TTL 与最大空闲时间。
     * Entries expires when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time expire date
     * <p>
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesAsync(Set<K> keys, Instant time);

    /**
     * 为指定键集合条目设置 TTL 与最大空闲时间。
     * If these parameters weren't set before.
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesIfNotSetAsync(Set<K> keys, Duration ttl);

    /**
     * 为指定键集合条目设置 TTL 与最大空闲时间。
     * If these parameters weren't set before.
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time expire date
     * <p>
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesIfNotSetAsync(Set<K> keys, Instant time);

    /**
     * 仅当新 TTL 大于已有 TTL 时为指定键集合条目设置过期时间。
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesIfGreaterAsync(Set<K> keys, Duration ttl);

    /**
     * 仅当新 TTL 大于已有 TTL 时为指定键集合条目设置过期时间。
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time expire date
     * <p>
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesIfGreaterAsync(Set<K> keys, Instant time);

    /**
     * 仅当新 TTL 小于已有 TTL 时为指定键集合条目设置过期时间。
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesIfLessAsync(Set<K> keys, Duration ttl);

    /**
     * 仅当新 TTL 小于已有 TTL 时为指定键集合条目设置过期时间。
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time expire date
     * <p>
     *
     * @return amount of updated entries.
     */
    RFuture<Integer> expireEntriesIfLessAsync(Set<K> keys, Instant time);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.MapPutListener
     * @see org.redisson.api.listener.MapRemoveListener
     * @see org.redisson.api.listener.MapExpiredListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

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
     * If the specified key is not already associated
     * with a value, attempts to compute its value using the given mapping function and enters it into this map .
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param time expire date
     * @param mappingFunction the mapping function to compute a value
     * @return current associated value
     */
    RFuture<V> computeIfAbsentAsync(K key, Instant time, Function<? super K, ? extends V> mappingFunction);


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
     * 根据键及其当前映射值计算新映射。
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param time expire date
     * @param remappingFunction - function to compute a value
     * @return the new value associated with the specified key, or {@code null} if none
     */
    RFuture<V> computeAsync(K key, Instant time, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
}
