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
package org.redisson;

import org.redisson.api.*;
import org.redisson.api.map.event.MapEntryListener;
import org.redisson.api.mapreduce.RMapReduce;
import org.redisson.client.codec.Codec;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class MapCacheNativeWrapper<K, V> implements RMapCache<K, V>, Supplier<RMap<K, V>> {

    /** 底层 Native MapCache 实例。 */
    private final RMapCacheNative<K, V> cache;

    public MapCacheNativeWrapper(RMapCacheNative<K, V> cache) {
        this.cache = cache;
    }

    /** 返回底层 Native MapCache 实例。 */
    @Override
    public RMap<K, V> get() {
        return cache;
    }

    /** 获取 WithLease。 */
    @Override
    public LeaseGetResult<V> getWithLease(K key, Duration leaseTimeToLive) {
        throw new UnsupportedOperationException("getWithLease method is not supported in native cache");
    }

    /** 异步获取 WithLease 对象或执行 WithLease 操作。 */
    @Override
    public RFuture<LeaseGetResult<V>> getWithLeaseAsync(K key, Duration leaseTimeToLive) {
        throw new UnsupportedOperationException("getWithLeaseAsync method is not supported in native cache");
    }

    /** removeWithLease：移除操作。 */
    @Override
    public boolean removeWithLease(K key) {
        throw new UnsupportedOperationException("removeWithLease method is not supported in native cache");
    }

    /** 异步执行 removeWithLease。 */
    @Override
    public RFuture<Boolean> removeWithLeaseAsync(K key) {
        throw new UnsupportedOperationException("removeWithLeaseAsync method is not supported in native cache");
    }

    /** putWithLease：Native Map 不支持 Lease 语义，调用抛出异常。 */
    @Override
    public boolean putWithLease(K key, V value, String leaseToken) {
        throw new UnsupportedOperationException("putWithLease method is not supported in native cache");
    }

    /** 异步执行 putWithLease。 */
    @Override
    public RFuture<Boolean> putWithLeaseAsync(K key, V value, String leaseToken) {
        throw new UnsupportedOperationException("putWithLease method is not supported in native cache");
    }

    /** putWithLease：Native Map 不支持 Lease 语义，调用抛出异常。 */
    @Override
    public boolean putWithLease(K key, V value, Duration ttl, String leaseToken) {
        throw new UnsupportedOperationException("putWithLease method is not supported in native cache");
    }

    /** 异步执行 putWithLease。 */
    @Override
    public RFuture<Boolean> putWithLeaseAsync(K key, V value, Duration ttl, String leaseToken) {
        throw new UnsupportedOperationException("putWithLease method is not supported in native cache");
    }

    /** putWithLease：Native Map 不支持 Lease 语义，调用抛出异常。 */
    @Override
    public boolean putWithLease(K key, V value, Duration ttl, Duration maxIdleTime, String leaseToken) {
        throw new UnsupportedOperationException("putWithLease method is not supported in native cache");
    }

    /** 异步执行 putWithLease。 */
    @Override
    public RFuture<Boolean> putWithLeaseAsync(K key, V value, Duration ttl, Duration maxIdleTime, String leaseToken) {
        throw new UnsupportedOperationException("putWithLease method is not supported in native cache");
    }

    /** 设置MaxSize。 */
    @Override
    public void setMaxSize(int maxSize) {
    }

    /** 设置MaxSize。 */
    @Override
    public void setMaxSize(int maxSize, EvictionMode mode) {
    }

    /** 委托底层 Native MapCache 执行 trySetMaxSize。 */
    @Override
    public boolean trySetMaxSize(int maxSize) {
        return false;
    }

    /** 委托底层 Native MapCache 执行 trySetMaxSize。 */
    @Override
    public boolean trySetMaxSize(int maxSize, EvictionMode mode) {
        return false;
    }

    /** 委托底层 Native MapCache 执行 computeIfAbsent。 */
    @Override
    public V computeIfAbsent(K key, Duration ttl, Function<? super K, ? extends V> mappingFunction) {
        return null;
    }

    /** 委托底层 Native MapCache 执行 compute。 */
    @Override
    public V compute(K key, Duration ttl, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    /** 获取 WithTTLOnly。 */
    @Override
    public V getWithTTLOnly(K key) {
        return cache.get(key);
    }

    /** 获取 AllWithTTLOnly。 */
    @Override
    public Map<K, V> getAllWithTTLOnly(Set<K> keys) {
        return cache.getAll(keys);
    }

    /** 快速写入属性到 Redis Map。 */
    @Override
    public boolean fastPut(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        return cache.fastPut(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 仅当键不存在时写入。 */
    @Override
    public V putIfAbsent(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        return cache.putIfAbsent(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 是否包含指定键。 */
    @Override
    public boolean containsKey(Object key) {
        return cache.containsKey(key);
    }

    /** 清空全部元素。 */
    @Override
    public void clear() {
        cache.clear();
    }

    /** 委托底层 Native MapCache 执行 fastRemove。 */
    @Override
    public long fastRemove(K... keys) {
        return cache.fastRemove(keys);
    }

    /** 委托底层 Native MapCache 执行 destroy。 */
    @Override
    public void destroy() {
        cache.destroy();
    }

    /** 写入键值（Map/Cache）。 */
    @Override
    public V put(K key, V value, long ttl, TimeUnit ttlUnit) {
        return cache.put(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 写入键值（Map/Cache）。 */
    @Override
    public V put(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 快速写入属性到 Redis Map。 */
    @Override
    public boolean fastPut(K key, V value, long ttl, TimeUnit ttlUnit) {
        return cache.fastPut(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 仅当键不存在时写入。 */
    @Override
    public V putIfAbsent(K key, V value, long ttl, TimeUnit ttlUnit) {
        return cache.putIfAbsent(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 委托底层 Native MapCache 执行 fastPutIfAbsent。 */
    @Override
    public boolean fastPutIfAbsent(K key, V value, long ttl, TimeUnit ttlUnit) {
        return cache.fastPutIfAbsent(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 委托底层 Native MapCache 执行 fastPutIfAbsent。 */
    @Override
    public boolean fastPutIfAbsent(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 委托底层 Native MapCache 执行 putAll。 */
    @Override
    public void putAll(Map<? extends K, ? extends V> map, long ttl, TimeUnit ttlUnit) {
        cache.putAll(map, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 委托底层 Native MapCache 执行 putAll。 */
    @Override
    public void putAll(Map<? extends K, ? extends V> map, long ttl, TimeUnit ttlUnit,
                       long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 委托底层 Native MapCache 执行 updateEntryExpiration。 */
    @Override
    public boolean updateEntryExpiration(K key, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 委托底层 Native MapCache 执行 expireEntry。 */
    @Override
    public boolean expireEntry(K key, Duration ttl, Duration maxIdleTime) {
        throw new UnsupportedOperationException();
    }

    /** 委托底层 Native MapCache 执行 expireEntries。 */
    @Override
    public int expireEntries(Set<K> keys, Duration ttl, Duration maxIdleTime) {
        throw new UnsupportedOperationException();
    }

    /** 委托底层 Native MapCache 执行 expireEntryIfNotSet。 */
    @Override
    public boolean expireEntryIfNotSet(K key, Duration ttl, Duration maxIdleTime) {
        throw new UnsupportedOperationException();
    }

    /** 委托底层 Native MapCache 执行 expireEntriesIfNotSet。 */
    @Override
    public int expireEntriesIfNotSet(Set<K> keys, Duration ttl, Duration maxIdleTime) {
        throw new UnsupportedOperationException();
    }

    /** addListener：添加操作。 */
    @Override
    public int addListener(ObjectListener listener) {
        return cache.addListener(listener);
    }

    /** 异步执行 put。 */
    @Override
    public RFuture<V> putAsync(K key, V value, long ttl, TimeUnit ttlUnit) {
        return cache.putAsync(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 异步执行 put。 */
    @Override
    public RFuture<V> putAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 fastPut。 */
    @Override
    public RFuture<Boolean> fastPutAsync(K key, V value, long ttl, TimeUnit ttlUnit) {
        return cache.fastPutAsync(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 异步执行 fastPut。 */
    @Override
    public RFuture<Boolean> fastPutAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 设置MaxSizeAsync。 */
    @Override
    public RFuture<Void> setMaxSizeAsync(int maxSize) {
        throw new UnsupportedOperationException();
    }

    /** 设置MaxSizeAsync。 */
    @Override
    public RFuture<Void> setMaxSizeAsync(int maxSize, EvictionMode mode) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 trySetMaxSize。 */
    @Override
    public RFuture<Boolean> trySetMaxSizeAsync(int maxSize) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 trySetMaxSize。 */
    @Override
    public RFuture<Boolean> trySetMaxSizeAsync(int maxSize, EvictionMode mode) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 putIfAbsent。 */
    @Override
    public RFuture<V> putIfAbsentAsync(K key, V value, long ttl, TimeUnit ttlUnit) {
        return cache.putIfAbsentAsync(key, value, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 异步执行 putIfAbsent。 */
    @Override
    public RFuture<V> putIfAbsentAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 fastPutIfAbsent。 */
    @Override
    public RFuture<Boolean> fastPutIfAbsentAsync(K key, V value, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 computeIfAbsent。 */
    @Override
    public RFuture<V> computeIfAbsentAsync(K key, Duration ttl, Function<? super K, ? extends V> mappingFunction) {
        return cache.computeIfAbsentAsync(key, mappingFunction);
    }

    /** 异步执行 compute。 */
    @Override
    public RFuture<V> computeAsync(K key, Duration ttl, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return cache.computeAsync(key, remappingFunction);
    }

    /** 异步执行 updateEntryExpiration。 */
    @Override
    public RFuture<Boolean> updateEntryExpirationAsync(K key, long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 expireEntry。 */
    @Override
    public RFuture<Boolean> expireEntryAsync(K key, Duration ttl, Duration maxIdleTime) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 expireEntries。 */
    @Override
    public RFuture<Integer> expireEntriesAsync(Set<K> keys, Duration ttl, Duration maxIdleTime) {
        throw new UnsupportedOperationException();
    }

    /** 异步获取 WithTTLOnly 对象或执行 WithTTLOnly 操作。 */
    @Override
    public RFuture<V> getWithTTLOnlyAsync(K key) {
        return cache.getAsync(key);
    }

    /** 异步获取 AllWithTTLOnly 对象或执行 AllWithTTLOnly 操作。 */
    @Override
    public RFuture<Map<K, V>> getAllWithTTLOnlyAsync(Set<K> keys) {
        return cache.getAllAsync(keys);
    }

    /** 异步执行 putAll。 */
    @Override
    public RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, long ttl, TimeUnit ttlUnit) {
        return cache.putAllAsync(map, Duration.ofMillis(ttlUnit.toMillis(ttl)));
    }

    /** 异步执行 putAll。 */
    @Override
    public RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, long ttl, TimeUnit ttlUnit,
                                     long maxIdleTime, TimeUnit maxIdleUnit) {
        throw new UnsupportedOperationException();
    }

    // RMap methods

    /** 委托底层 Native MapCache 执行 loadAll。 */
    @Override
    public void loadAll(boolean replaceExistingValues, int parallelism) {
        cache.loadAll(replaceExistingValues, parallelism);
    }

    /** 委托底层 Native MapCache 执行 loadAll。 */
    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, int parallelism) {
        cache.loadAll(keys, replaceExistingValues, parallelism);
    }

    /** 返回底层 Native MapCache 实例。 */
    @Override
    public V get(Object key) {
        return cache.get(key);
    }

    /** 写入键值（Map/Cache）。 */
    @Override
    public V put(K key, V value) {
        return cache.put(key, value);
    }

    /** 移除键或元素。 */
    @Override
    public V remove(Object key) {
        return cache.remove(key);
    }

    /** 委托底层 Native MapCache 执行 putAll。 */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        cache.putAll(m);
    }

    /** 委托底层 Native MapCache 执行 putAll。 */
    @Override
    public void putAll(Map<? extends K, ? extends V> map, int batchSize) {
        cache.putAll(map, batchSize);
    }

    /** 返回键集合视图。 */
    @Override
    public Set<K> keySet() {
        return cache.keySet();
    }

    /** 返回键集合视图。 */
    @Override
    public Set<K> keySet(int count) {
        return cache.keySet(count);
    }

    /** 返回键集合视图。 */
    @Override
    public Set<K> keySet(String pattern, int count) {
        return cache.keySet(pattern, count);
    }

    /** 返回键集合视图。 */
    @Override
    public Set<K> keySet(String pattern) {
        return cache.keySet(pattern);
    }

    /** 返回值集合视图。 */
    @Override
    public Collection<V> values() {
        return cache.values();
    }

    /** 返回值集合视图。 */
    @Override
    public Collection<V> values(String keyPattern) {
        return cache.values(keyPattern);
    }

    /** 返回值集合视图。 */
    @Override
    public Collection<V> values(String keyPattern, int count) {
        return cache.values(keyPattern, count);
    }

    /** 返回值集合视图。 */
    @Override
    public Collection<V> values(int count) {
        return cache.values(count);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return cache.entrySet();
    }

    /** 返回条目集合视图。 */
    @Override
    public Set<Entry<K, V>> entrySet(String keyPattern) {
        return cache.entrySet(keyPattern);
    }

    /** 返回条目集合视图。 */
    @Override
    public Set<Entry<K, V>> entrySet(String keyPattern, int count) {
        return cache.entrySet(keyPattern, count);
    }

    /** 返回条目集合视图。 */
    @Override
    public Set<Entry<K, V>> entrySet(int count) {
        return cache.entrySet(count);
    }

    /** 快速写入属性到 Redis Map。 */
    @Override
    public boolean fastPut(K key, V value) {
        return cache.fastPut(key, value);
    }

    /** 委托底层 Native MapCache 执行 fastReplace。 */
    @Override
    public boolean fastReplace(K key, V value) {
        return cache.fastReplace(key, value);
    }

    /** 仅当键不存在时写入。 */
    @Override
    public V putIfAbsent(K key, V value) {
        return cache.putIfAbsent(key, value);
    }

    /** 委托底层 Native MapCache 执行 putIfExists。 */
    @Override
    public V putIfExists(K key, V value) {
        return cache.putIfExists(key, value);
    }

    /** 委托底层 Native MapCache 执行 randomKeys。 */
    @Override
    public Set<K> randomKeys(int count) {
        return cache.randomKeys(count);
    }

    /** 委托底层 Native MapCache 执行 randomEntries。 */
    @Override
    public Map<K, V> randomEntries(int count) {
        return cache.randomEntries(count);
    }

    /** 创建 MapReduce 任务入口。 */
    @Override
    public <KOut, VOut> RMapReduce<K, V, KOut, VOut> mapReduce() {
        return cache.mapReduce();
    }

    /** 获取 CountDownLatch。 */
    @Override
    public RCountDownLatch getCountDownLatch(K key) {
        return cache.getCountDownLatch(key);
    }

    /** 获取 PermitExpirableSemaphore。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(K key) {
        return cache.getPermitExpirableSemaphore(key);
    }

    /** 获取 Semaphore。 */
    @Override
    public RSemaphore getSemaphore(K key) {
        return cache.getSemaphore(key);
    }

    /** 获取公平锁。 */
    @Override
    public RLock getFairLock(K key) {
        return cache.getFairLock(key);
    }

    /** 获取读写锁。 */
    @Override
    public RReadWriteLock getReadWriteLock(K key) {
        return cache.getReadWriteLock(key);
    }

    /** 获取分布式锁。 */
    @Override
    public RLock getLock(K key) {
        return cache.getLock(key);
    }

    /** 委托底层 Native MapCache 执行 valueSize。 */
    @Override
    public int valueSize(K key) {
        return cache.valueSize(key);
    }

    /** addAndGet：添加操作。 */
    @Override
    public V addAndGet(K key, Number delta) {
        return cache.addAndGet(key, delta);
    }

    /** 委托底层 Native MapCache 执行 fastPutIfAbsent。 */
    @Override
    public boolean fastPutIfAbsent(K key, V value) {
        return cache.fastPutIfAbsent(key, value);
    }

    /** 委托底层 Native MapCache 执行 fastPutIfExists。 */
    @Override
    public boolean fastPutIfExists(K key, V value) {
        return cache.fastPutIfExists(key, value);
    }

    /** 一次性读取全部键。 */
    @Override
    public Set<K> readAllKeySet() {
        return cache.readAllKeySet();
    }

    /** 一次性读取全部值。 */
    @Override
    public Collection<V> readAllValues() {
        return cache.readAllValues();
    }

    /** 一次性读取全部条目。 */
    @Override
    public Set<Entry<K, V>> readAllEntrySet() {
        return cache.readAllEntrySet();
    }

    /** 一次性读取全部 Map 条目。 */
    @Override
    public Map<K, V> readAllMap() {
        return cache.readAllMap();
    }

    /** 移除键或元素。 */
    @Override
    public boolean remove(Object key, Object value) {
        return cache.remove(key, value);
    }

    /** 委托底层 Native MapCache 执行 replace。 */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return cache.replace(key, oldValue, newValue);
    }

    /** 委托底层 Native MapCache 执行 replace。 */
    @Override
    public V replace(K key, V value) {
        return cache.replace(key, value);
    }

    /** 异步获取  对象或执行  操作。 */
    @Override
    public RFuture<V> getAsync(Object key) {
        return cache.getAsync((K) key);
    }

    /** 异步执行 put。 */
    @Override
    public RFuture<V> putAsync(K key, V value) {
        return cache.putAsync(key, value);
    }

    /** 异步执行 remove。 */
    @Override
    public RFuture<V> removeAsync(Object key) {
        return cache.removeAsync((K) key);
    }

    /** 异步执行 fastPut。 */
    @Override
    public RFuture<Boolean> fastPutAsync(K key, V value) {
        return cache.fastPutAsync(key, value);
    }

    /** 异步执行 fastReplace。 */
    @Override
    public RFuture<Boolean> fastReplaceAsync(K key, V value) {
        return cache.fastReplaceAsync(key, value);
    }

    /** 异步执行 putIfAbsent。 */
    @Override
    public RFuture<V> putIfAbsentAsync(K key, V value) {
        return cache.putIfAbsentAsync(key, value);
    }

    /** 异步执行 putIfExists。 */
    @Override
    public RFuture<V> putIfExistsAsync(K key, V value) {
        return cache.putIfExistsAsync(key, value);
    }

    /** 异步执行 fastPutIfAbsent。 */
    @Override
    public RFuture<Boolean> fastPutIfAbsentAsync(K key, V value) {
        return cache.fastPutIfAbsentAsync(key, value);
    }

    /** 异步执行 fastPutIfExists。 */
    @Override
    public RFuture<Boolean> fastPutIfExistsAsync(K key, V value) {
        return cache.fastPutIfExistsAsync(key, value);
    }

    /** 异步执行 readAllKeySet。 */
    @Override
    public RFuture<Set<K>> readAllKeySetAsync() {
        return cache.readAllKeySetAsync();
    }

    /** 异步执行 readAllValues。 */
    @Override
    public RFuture<Collection<V>> readAllValuesAsync() {
        return cache.readAllValuesAsync();
    }

    /** 异步执行 readAllEntrySet。 */
    @Override
    public RFuture<Set<Entry<K, V>>> readAllEntrySetAsync() {
        return cache.readAllEntrySetAsync();
    }

    /** 异步执行 readAllKeySet。 */
    @Override
    public RFuture<Set<K>> readAllKeySetAsync(String keyPattern) {
        return cache.readAllKeySetAsync(keyPattern);
    }

    /** 异步执行 readAllValues。 */
    @Override
    public RFuture<Collection<V>> readAllValuesAsync(String keyPattern) {
        return cache.readAllValuesAsync(keyPattern);
    }

    /** 异步执行 readAllEntrySet。 */
    @Override
    public RFuture<Set<Entry<K, V>>> readAllEntrySetAsync(String keyPattern) {
        return cache.readAllEntrySetAsync(keyPattern);
    }


    /** 异步执行 readAllMap。 */
    @Override
    public RFuture<Map<K, V>> readAllMapAsync() {
        return cache.readAllMapAsync();
    }

    /** 异步执行 remove。 */
    @Override
    public RFuture<Boolean> removeAsync(Object key, Object value) {
        return cache.removeAsync(key, value);
    }

    /** 异步执行 replace。 */
    @Override
    public RFuture<Boolean> replaceAsync(K key, V oldValue, V newValue) {
        return cache.replaceAsync(key, oldValue, newValue);
    }

    /** 异步执行 replace。 */
    @Override
    public RFuture<V> replaceAsync(K key, V value) {
        return cache.replaceAsync(key, value);
    }

    // RExpirable methods

    /** 委托底层 Native MapCache 执行 expire。 */
    @Override
    public boolean expire(long timeToLive, TimeUnit timeUnit) {
        return cache.expire(timeToLive, timeUnit);
    }

    /** 委托底层 Native MapCache 执行 expireAt。 */
    @Override
    public boolean expireAt(long timestamp) {
        return cache.expireAt(timestamp);
    }

    /** 委托底层 Native MapCache 执行 expireAt。 */
    @Override
    public boolean expireAt(Date timestamp) {
        return cache.expireAt(timestamp);
    }

    /** 委托底层 Native MapCache 执行 expire。 */
    @Override
    public boolean expire(Instant time) {
        return cache.expire(time);
    }

    /** 委托底层 Native MapCache 执行 expireIfSet。 */
    @Override
    public boolean expireIfSet(Instant time) {
        return cache.expireIfSet(time);
    }

    /** 委托底层 Native MapCache 执行 expireIfNotSet。 */
    @Override
    public boolean expireIfNotSet(Instant time) {
        return cache.expireIfNotSet(time);
    }

    /** 委托底层 Native MapCache 执行 expireIfGreater。 */
    @Override
    public boolean expireIfGreater(Instant time) {
        return cache.expireIfGreater(time);
    }

    /** 委托底层 Native MapCache 执行 expireIfLess。 */
    @Override
    public boolean expireIfLess(Instant time) {
        return cache.expireIfLess(time);
    }

    /** 委托底层 Native MapCache 执行 expire。 */
    @Override
    public boolean expire(Duration duration) {
        return cache.expire(duration);
    }

    /** 委托底层 Native MapCache 执行 expireIfSet。 */
    @Override
    public boolean expireIfSet(Duration duration) {
        return cache.expireIfSet(duration);
    }

    /** 委托底层 Native MapCache 执行 expireIfNotSet。 */
    @Override
    public boolean expireIfNotSet(Duration duration) {
        return cache.expireIfNotSet(duration);
    }

    /** 委托底层 Native MapCache 执行 expireIfGreater。 */
    @Override
    public boolean expireIfGreater(Duration duration) {
        return cache.expireIfGreater(duration);
    }

    /** 委托底层 Native MapCache 执行 expireIfLess。 */
    @Override
    public boolean expireIfLess(Duration duration) {
        return cache.expireIfLess(duration);
    }

    /** 委托底层 Native MapCache 执行 clearExpire。 */
    @Override
    public boolean clearExpire() {
        return cache.clearExpire();
    }

    /** 委托底层 Native MapCache 执行 remainTimeToLive。 */
    @Override
    public long remainTimeToLive() {
        return cache.remainTimeToLive();
    }

    /** 获取 ExpireTime。 */
    @Override
    public long getExpireTime() {
        return cache.getExpireTime();
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit) {
        return cache.expireAsync(timeToLive, timeUnit);
    }

    /** 异步执行 expireAt。 */
    @Override
    public RFuture<Boolean> expireAtAsync(Date timestamp) {
        return cache.expireAtAsync(timestamp);
    }

    /** 异步执行 expireAt。 */
    @Override
    public RFuture<Boolean> expireAtAsync(long timestamp) {
        return cache.expireAtAsync(timestamp);
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(Instant time) {
        return cache.expireAsync(time);
    }

    /** 异步执行 expireIfSet。 */
    @Override
    public RFuture<Boolean> expireIfSetAsync(Instant time) {
        return cache.expireIfSetAsync(time);
    }

    /** 异步执行 expireIfNotSet。 */
    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Instant time) {
        return cache.expireIfNotSetAsync(time);
    }

    /** 异步执行 expireIfGreater。 */
    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Instant time) {
        return cache.expireIfGreaterAsync(time);
    }

    /** 异步执行 expireIfLess。 */
    @Override
    public RFuture<Boolean> expireIfLessAsync(Instant time) {
        return cache.expireIfLessAsync(time);
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(Duration duration) {
        return cache.expireAsync(duration);
    }

    /** 异步执行 expireIfSet。 */
    @Override
    public RFuture<Boolean> expireIfSetAsync(Duration duration) {
        return cache.expireIfSetAsync(duration);
    }

    /** 异步执行 expireIfNotSet。 */
    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Duration duration) {
        return cache.expireIfNotSetAsync(duration);
    }

    /** 异步执行 expireIfGreater。 */
    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Duration duration) {
        return cache.expireIfGreaterAsync(duration);
    }

    /** 异步执行 expireIfLess。 */
    @Override
    public RFuture<Boolean> expireIfLessAsync(Duration duration) {
        return cache.expireIfLessAsync(duration);
    }

    /** 异步执行 clearExpire。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return cache.clearExpireAsync();
    }

    /** 异步执行 remainTimeToLive。 */
    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        return cache.remainTimeToLiveAsync();
    }

    /** 异步获取 ExpireTime 对象或执行 ExpireTime 操作。 */
    @Override
    public RFuture<Long> getExpireTimeAsync() {
        return cache.getExpireTimeAsync();
    }

    // RObject methods

    /** 获取 Name。 */
    @Override
    public String getName() {
        return cache.getName();
    }

    /** 重命名 Redis 键。 */
    @Override
    public void rename(String newName) {
        cache.rename(newName);
    }

    /** 委托底层 Native MapCache 执行 renamenx。 */
    @Override
    public boolean renamenx(String newName) {
        return cache.renamenx(newName);
    }

    /** 是否Exists。 */
    @Override
    public boolean isExists() {
        return cache.isExists();
    }

    /** 获取 Codec。 */
    @Override
    public Codec getCodec() {
        return cache.getCodec();
    }

    /** 获取 IdleTime。 */
    @Override
    public Long getIdleTime() {
        return cache.getIdleTime();
    }

    /** 获取 ReferenceCount。 */
    @Override
    public int getReferenceCount() {
        return cache.getReferenceCount();
    }

    /** 获取 AccessFrequency。 */
    @Override
    public int getAccessFrequency() {
        return cache.getAccessFrequency();
    }

    /** 获取 InternalEncoding。 */
    @Override
    public ObjectEncoding getInternalEncoding() {
        return cache.getInternalEncoding();
    }

    /** 委托底层 Native MapCache 执行 sizeInMemory。 */
    @Override
    public long sizeInMemory() {
        return cache.sizeInMemory();
    }

    /** 从 DUMP 数据恢复键。 */
    @Override
    public void restore(byte[] state) {
        cache.restore(state);
    }

    /** 从 DUMP 数据恢复键。 */
    @Override
    public void restore(byte[] state, long timeToLive, TimeUnit timeUnit) {
        cache.restore(state, timeToLive, timeUnit);
    }

    /** 委托底层 Native MapCache 执行 restoreAndReplace。 */
    @Override
    public void restoreAndReplace(byte[] state) {
        cache.restoreAndReplace(state);
    }

    /** 委托底层 Native MapCache 执行 restoreAndReplace。 */
    @Override
    public void restoreAndReplace(byte[] state, long timeToLive, TimeUnit timeUnit) {
        cache.restoreAndReplace(state, timeToLive, timeUnit);
    }

    /** 序列化键值。 */
    @Override
    public byte[] dump() {
        return cache.dump();
    }

    /** 更新键的最后访问时间。 */
    @Override
    public boolean touch() {
        return cache.touch();
    }

    /** 将键迁移到另一 Redis 实例。 */
    @Override
    public void migrate(String host, int port, int database, long timeout) {
        cache.migrate(host, port, database, timeout);
    }

    /** 复制键到目标名称。 */
    @Override
    public void copy(String host, int port, int database, long timeout) {
        cache.copy(host, port, database, timeout);
    }

    /** 复制键到目标名称。 */
    @Override
    public boolean copy(String destination) {
        return cache.copy(destination);
    }

    /** 复制键到目标名称。 */
    @Override
    public boolean copy(String destination, int database) {
        return cache.copy(destination, database);
    }

    /** 委托底层 Native MapCache 执行 copyAndReplace。 */
    @Override
    public boolean copyAndReplace(String destination) {
        return cache.copyAndReplace(destination);
    }

    /** 委托底层 Native MapCache 执行 copyAndReplace。 */
    @Override
    public boolean copyAndReplace(String destination, int database) {
        return cache.copyAndReplace(destination, database);
    }

    /** 将键移动到指定数据库。 */
    @Override
    public boolean move(int database) {
        return cache.move(database);
    }

    /** 删除键或 Session。 */
    @Override
    public boolean delete() {
        return cache.delete();
    }

    /** 异步删除键。 */
    @Override
    public boolean unlink() {
        return cache.unlink();
    }

    /** 返回列表/集合/过滤器当前元素数量。 */
    @Override
    public int size() {
        return cache.size();
    }

    /** addListener：添加操作。 */
    @Override
    public int addListener(MapEntryListener listener) {
        throw new UnsupportedOperationException();
    }

    /** 是否为空。 */
    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    /** 是否包含指定值。 */
    @Override
    public boolean containsValue(Object value) {
        return cache.containsValue(value);
    }

    /** 获取 All。 */
    @Override
    public Map<K, V> getAll(Set<K> keys) {
        return cache.getAll(keys);
    }

    /** 异步执行 rename。 */
    @Override
    public RFuture<Void> renameAsync(String newName) {
        return cache.renameAsync(newName);
    }

    /** 异步执行 renamenx。 */
    @Override
    public RFuture<Boolean> renamenxAsync(String newName) {
        return cache.renamenxAsync(newName);
    }

    /** 是否ExistsAsync。 */
    @Override
    public RFuture<Boolean> isExistsAsync() {
        return cache.isExistsAsync();
    }

    /** 异步执行 addListener。 */
    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        return cache.addListenerAsync(listener);
    }

    /** 异步执行 removeListener。 */
    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        return cache.removeListenerAsync(listenerId);
    }

    /** 异步获取 IdleTime 对象或执行 IdleTime 操作。 */
    @Override
    public RFuture<Long> getIdleTimeAsync() {
        return cache.getIdleTimeAsync();
    }

    /** 异步获取 ReferenceCount 对象或执行 ReferenceCount 操作。 */
    @Override
    public RFuture<Integer> getReferenceCountAsync() {
        return cache.getReferenceCountAsync();
    }

    /** 异步获取 AccessFrequency 对象或执行 AccessFrequency 操作。 */
    @Override
    public RFuture<Integer> getAccessFrequencyAsync() {
        return cache.getAccessFrequencyAsync();
    }

    /** 异步获取 InternalEncoding 对象或执行 InternalEncoding 操作。 */
    @Override
    public RFuture<ObjectEncoding> getInternalEncodingAsync() {
        return cache.getInternalEncodingAsync();
    }

    /** 异步执行 sizeInMemory。 */
    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        return cache.sizeInMemoryAsync();
    }

    /** 异步执行 restore。 */
    @Override
    public RFuture<Void> restoreAsync(byte[] state) {
        return cache.restoreAsync(state);
    }

    /** 异步执行 restore。 */
    @Override
    public RFuture<Void> restoreAsync(byte[] state, long timeToLive, TimeUnit timeUnit) {
        return cache.restoreAsync(state, timeToLive, timeUnit);
    }

    /** 异步执行 restoreAndReplace。 */
    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] state) {
        return cache.restoreAsync(state);
    }

    /** 异步执行 restoreAndReplace。 */
    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] state, long timeToLive, TimeUnit timeUnit) {
        return cache.restoreAsync(state, timeToLive, timeUnit);
    }

    /** 异步执行 dump。 */
    @Override
    public RFuture<byte[]> dumpAsync() {
        return cache.dumpAsync();
    }

    /** 异步执行 touch。 */
    @Override
    public RFuture<Boolean> touchAsync() {
        return cache.touchAsync();
    }

    /** 异步执行 migrate。 */
    @Override
    public RFuture<Void> migrateAsync(String host, int port, int database, long timeout) {
        return cache.migrateAsync(host, port, database, timeout);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Void> copyAsync(String host, int port, int database, long timeout) {
        return cache.copyAsync(host, port, database, timeout);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Boolean> copyAsync(String destination) {
        return cache.copyAsync(destination);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Boolean> copyAsync(String destination, int database) {
        return cache.copyAsync(destination, database);
    }

    /** 异步执行 copyAndReplace。 */
    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination) {
        return cache.copyAndReplaceAsync(destination);
    }

    /** 异步执行 copyAndReplace。 */
    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination, int database) {
        return cache.copyAndReplaceAsync(destination, database);
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<Boolean> moveAsync(int database) {
        return cache.moveAsync(database);
    }

    /** 异步执行 delete。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return cache.deleteAsync();
    }

    /** 异步执行 unlink。 */
    @Override
    public RFuture<Boolean> unlinkAsync() {
        return cache.unlinkAsync();
    }

    /** 异步返回元素数量。 */
    @Override
    public RFuture<Integer> sizeAsync() {
        return cache.sizeAsync();
    }

    /** 异步执行 values。 */
    @Override
    public AsyncIterator<V> valuesAsync() {
        return cache.valuesAsync();
    }

    /** 异步执行 values。 */
    @Override
    public AsyncIterator<V> valuesAsync(String keyPattern) {
        return cache.valuesAsync(keyPattern);
    }

    /** 异步执行 values。 */
    @Override
    public AsyncIterator<V> valuesAsync(String keyPattern, int count) {
        return cache.valuesAsync(keyPattern, count);
    }

    /** 异步执行 values。 */
    @Override
    public AsyncIterator<V> valuesAsync(int count) {
        return cache.valuesAsync(count);
    }

    /** 异步执行 keys。 */
    @Override
    public AsyncIterator<K> keysAsync() {
        return cache.keysAsync();
    }

    /** 异步执行 keys。 */
    @Override
    public AsyncIterator<K> keysAsync(int count) {
        return cache.keysAsync(count);
    }

    /** 异步执行 entrySet。 */
    @Override
    public AsyncIterator<Entry<K, V>> entrySetAsync() {
        return cache.entrySetAsync();
    }

    /** 异步执行 entrySet。 */
    @Override
    public AsyncIterator<Entry<K, V>> entrySetAsync(String keyPattern) {
        return cache.entrySetAsync(keyPattern);
    }

    /** 异步执行 entrySet。 */
    @Override
    public AsyncIterator<Entry<K, V>> entrySetAsync(String keyPattern, int count) {
        return cache.entrySetAsync(keyPattern, count);
    }

    /** 异步执行 entrySet。 */
    @Override
    public AsyncIterator<Entry<K, V>> entrySetAsync(int count) {
        return cache.entrySetAsync(count);
    }

    /** 异步执行 fastRemove。 */
    @Override
    public RFuture<Long> fastRemoveAsync(K... keys) {
        return cache.fastRemoveAsync(keys);
    }

    /** 异步执行 remainTimeToLive。 */
    @Override
    public RFuture<Long> remainTimeToLiveAsync(K key) {
        return cache.remainTimeToLiveAsync(key);
    }

    /** 异步执行 addListener。 */
    @Override
    public RFuture<Integer> addListenerAsync(MapEntryListener listener) {
        throw new UnsupportedOperationException();
    }

    /** 异步执行 merge。 */
    @Override
    public RFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return cache.mergeAsync(key, value, remappingFunction);
    }

    /** 异步执行 compute。 */
    @Override
    public RFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return cache.computeAsync(key, remappingFunction);
    }

    /** 异步执行 computeIfAbsent。 */
    @Override
    public RFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction) {
        return cache.computeIfAbsentAsync(key, mappingFunction);
    }

    /** 异步执行 computeIfPresent。 */
    @Override
    public RFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return cache.computeIfPresentAsync(key, remappingFunction);
    }

    /** 异步执行 loadAll。 */
    @Override
    public RFuture<Void> loadAllAsync(boolean replaceExistingValues, int parallelism) {
        return cache.loadAllAsync(replaceExistingValues, parallelism);
    }

    /** 异步执行 loadAll。 */
    @Override
    public RFuture<Void> loadAllAsync(Set<? extends K> keys, boolean replaceExistingValues, int parallelism) {
        return cache.loadAllAsync(keys, replaceExistingValues, parallelism);
    }

    /** 异步执行 valueSize。 */
    @Override
    public RFuture<Integer> valueSizeAsync(K key) {
        return cache.valueSizeAsync(key);
    }

    /** 异步获取 All 对象或执行 All 操作。 */
    @Override
    public RFuture<Map<K, V>> getAllAsync(Set<K> keys) {
        return cache.getAllAsync(keys);
    }

    /** 异步执行 putAll。 */
    @Override
    public RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map) {
        return cache.putAllAsync(map);
    }

    /** 异步执行 putAll。 */
    @Override
    public RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, int batchSize) {
        return cache.putAllAsync(map, batchSize);
    }

    /** 异步执行 randomKeys。 */
    @Override
    public RFuture<Set<K>> randomKeysAsync(int count) {
        return cache.randomKeysAsync(count);
    }

    /** 异步执行 randomEntries。 */
    @Override
    public RFuture<Map<K, V>> randomEntriesAsync(int count) {
        return cache.randomEntriesAsync(count);
    }

    /** 异步执行 addAndGet。 */
    @Override
    public RFuture<V> addAndGetAsync(K key, Number delta) {
        return cache.addAndGetAsync(key, delta);
    }

    /** 异步执行 clear。 */
    @Override
    public RFuture<Boolean> clearAsync() {
        return cache.clearAsync();
    }

    /** 异步执行 containsKey。 */
    @Override
    public RFuture<Boolean> containsKeyAsync(Object key) {
        return cache.containsKeyAsync(key);
    }

    /** 异步执行 containsValue。 */
    @Override
    public RFuture<Boolean> containsValueAsync(Object value) {
        return cache.containsValueAsync(value);
    }

    /** removeListener：移除操作。 */
    @Override
    public void removeListener(int listenerId) {
        cache.removeListener(listenerId);
    }

    /** 委托底层 Native MapCache 执行 remainTimeToLive。 */
    @Override
    public long remainTimeToLive(K key) {
        return cache.remainTimeToLive(key);
    }


}
