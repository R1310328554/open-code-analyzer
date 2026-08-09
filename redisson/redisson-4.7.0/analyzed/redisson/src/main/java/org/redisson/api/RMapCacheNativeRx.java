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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
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
public interface RMapCacheNativeRx<K, V> extends RMapRx<K, V>, RDestroyable {

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
    Maybe<V> put(K key, V value, Duration ttl);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time - time expire date
     * @return 先前关联的值
     */
    Maybe<V> put(K key, V value, Instant time);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     * <p>
     * Works faster than usual {@link #put(Object, Object, Duration)}
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
    Single<Boolean> fastPut(K key, V value, Duration ttl);

    /**
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     * <p>
     * Works faster than usual {@link #put(Object, Object, Duration)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time - time expire date
     *
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash and the value was updated.
     */
    Single<Boolean> fastPut(K key, V value, Instant time);

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
    Maybe<V> putIfAbsent(K key, V value, Duration ttl);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time - time expire date
     *
     * @return current associated value
     */
    Maybe<V> putIfAbsent(K key, V value, Instant time);

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
    Maybe<V> putIfExist(K key, V value, Duration ttl);

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
    Maybe<V> putIfExist(K key, V value, Instant time);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * Works faster than usual {@link #putIfAbsent(Object, Object, Duration)}
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
    Single<Boolean> fastPutIfAbsent(K key, V value, Duration ttl);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     * <p>
     * Works faster than usual {@link #putIfAbsent(Object, Object, Duration)}
     * as it not returns previous value.
     *
     * @param key 映射键
     * @param value 映射值
     * @param time - time expire date
     *
     * @return <code>true</code> if key is a new key in the hash and value was set.
     *         <code>false</code> if key already exists in the hash
     */
    Single<Boolean> fastPutIfAbsent(K key, V value, Instant time);

    /**
     * 返回指定键对应条目的剩余 TTL。
     *
     * @param key map key
     * @return time in milliseconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    Single<Long> remainTimeToLive(K key);

    /**
     * 返回指定键集合对应条目的剩余 TTL 映射。
     *
     * @param keys map keys
     * @return Time to live mapped by key.
     *          Time in milliseconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    Single<Map<K, Long>> remainTimeToLive(Set<K> keys);

    /**
     * 请改用 {@link #putAll(PutArgs)}。
     *
     * @param map - mappings to be stored in this map
     * @param ttl - time to live for all key\value entries.
     *              If <code>0</code> then stores infinitely.
     */
    @Deprecated
    Completable putAll(java.util.Map<? extends K, ? extends V> map, Duration ttl);

    /**
     * 存储 {@code args} 中指定的 Map 条目。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     * <p>
     * If {@link MapWriter} is defined then new map entries will be stored in write-through mode.
     *
     * @param args put arguments
     */
    Completable putAll(PutArgs<K, V> args);

    /**
     * 仅当全部指定键已存在时存储条目。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param args put arguments
     * @return {@code true} if all entries were set, {@code false} otherwise
     */
    Single<Boolean> putIfAllKeysExist(PutArgs<K, V> args);

    /**
     * 仅当全部指定键不存在时存储条目。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param args put arguments
     * @return {@code true} if all entries were set, {@code false} otherwise
     */
    Single<Boolean> putIfAllKeysAbsent(PutArgs<K, V> args);

    /**
     * 清除指定键条目的过期时间。
     *
     * @param key map key
     * @return <code>true</code> if timeout was removed
     *         <code>false</code> if entry does not have an associated timeout
     *         <code>null</code> if entry does not exist
     */
    Maybe<Boolean> clearExpire(K key);

    /**
     * 清除指定键集合条目的过期时间。
     *
     * @param keys map keys
     * @return Boolean mapped by key.
     *         <code>true</code> if timeout was removed
     *         <code>false</code> if entry does not have an associated timeout
     *         <code>null</code> if entry does not exist
     */
    Single<Map<K, Boolean>> clearExpire(Set<K> keys);

    /**
     * 更新指定键条目的 TTL。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already expired or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param ttl time to live for key\value entry.
     *              If <code>0</code> then time to live doesn't affect entry expiration.
     * <p>
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already expired or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    Single<Boolean> expireEntry(K key, Duration ttl);

    /**
     * 更新指定键条目的 TTL。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already expired or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already expired or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    Single<Boolean> expireEntry(K key, Instant time);

    /**
     * 为指定键的条目设置 TTL。
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
     * if <code>ttl</code> params are equal to <code>0</code>
     * then entry stores infinitely.
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    Single<Boolean> expireEntryIfNotSet(K key, Duration ttl);

    /**
     * 为指定键的条目设置 TTL。
     * If these parameters weren't set before.
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    Single<Boolean> expireEntryIfNotSet(K key, Instant time);

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
    Single<Boolean> expireEntryIfGreater(K key, Duration ttl);


    /**
     * 仅当新 TTL 大于已有 TTL 时为指定键条目设置过期时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    Single<Boolean> expireEntryIfGreater(K key, Instant time);

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
    Single<Boolean> expireEntryIfLess(K key, Duration ttl);

    /**
     * 仅当新 TTL 小于已有 TTL 时为指定键条目设置过期时间。
     * Entry expires when specified time to live was reached.
     * <p>
     * Returns <code>false</code> if entry already has expiration time or doesn't exist,
     * otherwise returns <code>true</code>.
     *
     * @param key map key
     * @param time time expire date
     * <p>
     *
     * @return returns <code>false</code> if entry already has expiration time or doesn't exist,
     *         otherwise returns <code>true</code>.
     */
    Single<Boolean> expireEntryIfLess(K key, Instant time);

    /**
     * 更新指定键集合条目的 TTL。
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
    Single<Integer> expireEntries(Set<K> keys, Duration ttl);

    /**
     * 更新指定键集合条目的 TTL。
     * Entries expires when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time time expire date for key\value entries.
     * <p>
     *
     * @return amount of updated entries.
     */
    Single<Integer> expireEntries(Set<K> keys, Instant time);

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
    Single<Integer> expireEntriesIfGreater(Set<K> keys, Duration ttl);

    /**
     * 仅当新 TTL 大于已有 TTL 时为指定键集合条目设置过期时间。
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time time expire date for key\value entry.
     * <p>
     *
     * @return amount of updated entries.
     */
    Single<Integer> expireEntriesIfGreater(Set<K> keys, Instant time);

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
    Single<Integer> expireEntriesIfLess(Set<K> keys, Duration ttl);

    /**
     * 仅当新 TTL 小于已有 TTL 时为指定键集合条目设置过期时间。
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time time expire date for key\value entry.
     * <p>
     *
     * @return amount of updated entries.
     */
    Single<Integer> expireEntriesIfLess(Set<K> keys, Instant time);

    /**
     * 为指定键集合的条目设置 TTL。
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
    Single<Integer> expireEntriesIfNotSet(Set<K> keys, Duration ttl);

    /**
     * 为指定键集合的条目设置 TTL。
     * If these parameters weren't set before.
     * Entries expire when specified time to live was reached.
     * <p>
     * Returns amount of updated entries.
     *
     * @param keys map keys
     * @param time time expire date for key\value entry.
     * <p>
     *
     * @return amount of updated entries.
     */
    Single<Integer> expireEntriesIfNotSet(Set<K> keys, Instant time);

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
    Single<Integer> addListener(ObjectListener listener);

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
    Maybe<V> computeIfAbsent(K key, Duration ttl, Function<? super K, ? extends V> mappingFunction);

    /**
     * If the specified key is not already associated
     * with a value, attempts to compute its value using the given mapping function and enters it into this map .
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param time - time expire date
     * @param mappingFunction the mapping function to compute a value
     * @return current associated value
     */
    Maybe<V> computeIfAbsent(K key, Instant time, Function<? super K, ? extends V> mappingFunction);

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
    Maybe<V> compute(K key, Duration ttl, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    /**
     * 根据键及其当前映射值计算新映射。
     * <p>
     * 存储键值对并设置 TTL。
     * Entry expires after specified time to live.
     *
     * @param key 映射键
     * @param time - time expire date
     * @param remappingFunction - function to compute a value
     * @return the new value associated with the specified key, or {@code null} if none
     */
    Maybe<V> compute(K key, Instant time, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
}
