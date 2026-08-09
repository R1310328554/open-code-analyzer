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

import org.redisson.api.options.*;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.config.Config;

import java.util.Collection;
import java.util.List;

/**
 * Reactor 风格 Redisson 客户端 {@link RedissonReactiveClient}。
 * <p>提供全部响应式分布式对象 factory 方法；
 * 参见 {@link RedissonClient} 与 {@link RedissonRxClient}。
 *
 * @author Nikita Koksharov
 */
public interface RedissonReactiveClient {

    /**
     * 返回Array instance by <code>name</code>。
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param name 实例名称
     * @return RArrayReactive object
     */
    <V> RArrayReactive<V> getArray(String name);

    /**
     * 返回Array instance by <code>name</code>。
     * using provided <code>codec</code> for values.
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param name 实例名称
     * @param codec 值编解码器
     * @return RArrayReactive object
     */
    <V> RArrayReactive<V> getArray(String name, Codec codec);

    /**
     * 返回Array instance with specified <code>options</code>.。
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param options 实例选项
     * @return RArrayReactive object
     */
    <V> RArrayReactive<V> getArray(PlainOptions options);

    /**
     * 按名称获取 {@link RTimeSeries} 实例。（Redisson API）。
     *
     * @param <V> 值类型
     * @param <L> 标签类型
     * @param name 实例名称
     * @return RTimeSeries object
     */
    <V, L> RTimeSeriesReactive<V, L> getTimeSeries(String name);

    /**
     * 按名称获取 {@link RTimeSeries} 实例。（Redisson API）。
     * using provided <code>codec</code> for values.
     *
     * @param <V> 值类型
     * @param <L> 标签类型
     * @param name 实例名称
     * @param codec 值编解码器
     * @return RTimeSeries object
     */
    <V, L> RTimeSeriesReactive<V, L> getTimeSeries(String name, Codec codec);

    /**
     * 返回time-series instance with specified <code>options</code>.。
     *
     * @param <V> 值类型
     * @param <L> 标签类型
     * @param options 实例选项
     * @return RTimeSeries object
     */
    <V, L> RTimeSeriesReactive<V, L> getTimeSeries(PlainOptions options);

    /**
     * Redis Stream 相关操作：按名称获取 {@link RStream} 实例。。
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     * 
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name of stream
     * @return RStream object
     */
    <K, V> RStreamReactive<K, V> getStream(String name);
    
    /**
     * Redis Stream 相关操作：按名称获取 {@link RStream} 实例。。
     * using provided <code>codec</code> for entries.
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     * 
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name name of stream
     * @param codec codec for entry
     * @return RStream object
     */
    <K, V> RStreamReactive<K, V> getStream(String name, Codec codec);

    /**
     * 返回time-series instance with specified <code>options</code>.。
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return RStream object
     */
    <K, V> RStreamReactive<K, V> getStream(PlainOptions options);

    /**
     * 返回API for RediSearch module。
     *
     * @return RSearch object
     */
    RSearchReactive getSearch();

    /**
     * 返回API for RediSearch module using defined codec for attribute values.。
     *
     * @return RSearch object
     */
    RSearchReactive getSearch(Codec codec);

    /**
     * 返回API for RediSearch module with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return RSearch object
     */
    RSearchReactive getSearch(OptionalOptions options);

    /**
     * 按名称获取 {@link RGeo} 地理空间容器实例。
     * 
     * @param <V> 元素类型
     * @param name 对象名称
     * @return Geo object
     */
    <V> RGeoReactive<V> getGeo(String name);
    
    /**
     * 返回geospatial items holder instance by <code>name</code>。
     * using provided codec for geospatial members.
     * 
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for value
     * @return Geo object
     */
    <V> RGeoReactive<V> getGeo(String name, Codec codec);

    /**
     * 返回geospatial items holder instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return Geo object
     */
    <V> RGeoReactive<V> getGeo(PlainOptions options);

    /**
     * 返回rate limiter instance by <code>name</code>。
     * 
     * @param name of rate limiter
     * @return RateLimiter object
     */
    RRateLimiterReactive getRateLimiter(String name);

    /**
     * 返回rate limiter instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return RateLimiter object
     */
    RRateLimiterReactive getRateLimiter(CommonOptions options);

    /**
     * 返回GCRA instance by <code>name</code>.。
     * <p>
     * Requires Redis 8.8.0 or higher.
     *
     * @param name of GCRA object
     * @return GCRA object
     */
    RGcraReactive getGcra(String name);

    /**
     * 返回GCRA instance with specified <code>options</code>.。
     * <p>
     * Requires Redis 8.8.0 or higher.
     *
     * @param options 实例选项
     * @return GCRA object
     */
    RGcraReactive getGcra(CommonOptions options);

    /**
     * 返回binary stream holder instance by <code>name</code>。
     *
     * @param name of binary stream
     * @return BinaryStream object
     */
    RBinaryStreamReactive getBinaryStream(String name);

    /**
     * 返回binary stream holder instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return BinaryStream object
     */
    RBinaryStreamReactive getBinaryStream(CommonOptions options);

    /**
     * 返回semaphore instance by name。
     *
     * @param name 对象名称
     * @return Semaphore object
     */
    RSemaphoreReactive getSemaphore(String name);

    /**
     * 返回semaphore instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return Semaphore object
     */
    RSemaphoreReactive getSemaphore(CommonOptions options);

    /**
     * 返回semaphore instance by name.。
     * Supports lease time parameter for each acquired permit.
     * 
     * @param name 对象名称
     * @return PermitExpirableSemaphore object
     */
    RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(String name);

    /**
     * 返回semaphore instance with specified <code>options</code>.。
     * Supports lease time parameter for each acquired permit.
     *
     * @param options 实例选项
     * @return PermitExpirableSemaphore object
     */
    RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(CommonOptions options);

    /**
     * 返回ReadWriteLock instance by name.。
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param name 对象名称
     * @return Lock object
     */
    RReadWriteLockReactive getReadWriteLock(String name);

    /**
     * 返回ReadWriteLock instance with specified <code>options</code>.。
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param options 实例选项
     * @return Lock object
     */
    RReadWriteLockReactive getReadWriteLock(CommonOptions options);

    /**
     * 返回Lock instance by name.。
     * <p>
     * Implements a <b>fair</b> locking so it guarantees an acquire order by threads.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     * 
     * @param name 对象名称
     * @return Lock object
     */
    RLockReactive getFairLock(String name);

    /**
     * 返回Lock instance with specified <code>options</code>.。
     * <p>
     * Implements a <b>fair</b> locking so it guarantees an acquire order by threads.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param options 实例选项
     * @return Lock object
     */
    RLockReactive getFairLock(CommonOptions options);

    /**
     * 返回a fair, non-reentrant Lock instance by name.。
     * <p>
     * Acquisition order is FIFO across all Redisson instances. Unlike
     * {@link #getFairLock(String)}, attempts by the same thread to acquire
     * the lock while it already holds it cause {@link IllegalMonitorStateException}
     * for both {@code lock()} and {@code tryLock()}.
     *
     * @param name 对象名称
     * @return Lock object
     */
    RLockReactive getNonReentrantFairLock(String name);

    /**
     * 返回a fair, non-reentrant Lock instance with specified <code>options</code>.。
     * <p>
     * Acquisition order is FIFO across all Redisson instances. Unlike
     * {@link #getFairLock(CommonOptions)}, attempts by the same thread to acquire
     * the lock while it already holds it cause {@link IllegalMonitorStateException}
     * for both {@code lock()} and {@code tryLock()}.
     *
     * @param options 实例选项
     * @return Lock object
     */
    RLockReactive getNonReentrantFairLock(CommonOptions options);

    /**
     * 返回Lock instance by name.。
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param name 对象名称
     * @return Lock object
     */
    RLockReactive getLock(String name);

    /**
     * 返回Lock instance with specified <code>options</code>.。
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param options 实例选项
     * @return Lock object
     */
    RLockReactive getLock(CommonOptions options);

    /**
     * 返回Spin lock instance by name.。
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * Lock doesn't use a pub/sub mechanism
     *
     * @param name 对象名称
     * @return Lock object
     */
    RLockReactive getSpinLock(String name);

    /**
     * 返回Spin lock instance by name with specified back off options.。
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * Lock doesn't use a pub/sub mechanism
     *
     * @param name 对象名称
     * @return Lock object
     */
    RLockReactive getSpinLock(String name, LockOptions.BackOff backOff);

    /**
     * 返回a non-reentrant Lock instance by name.。
     * <p>
     * Unlike {@link #getLock(String)}, attempts by the same thread to acquire
     * the lock while it already holds it cause {@link IllegalMonitorStateException}
     * for both {@code lock()} and {@code tryLock()}.
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantee an acquire order by threads.
     *
     * @param name 对象名称
     * @return Lock object
     */
    RLockReactive getNonReentrantLock(String name);

    /**
     * 返回a non-reentrant Lock instance with specified <code>options</code>.。
     * <p>
     * Unlike {@link #getLock(CommonOptions)}, attempts by the same thread to acquire
     * the lock while it already holds it cause {@link IllegalMonitorStateException}
     * for both {@code lock()} and {@code tryLock()}.
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantee an acquire order by threads.
     *
     * @param options 实例选项
     * @return Lock object
     */
    RLockReactive getNonReentrantLock(CommonOptions options);

    /**
     * 返回Fenced Lock by name.。
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantee an acquire order by threads.
     *
     * @param name 对象名称
     * @return Lock object
     */
    RFencedLockReactive getFencedLock(String name);

    /**
     * 返回Fenced Lock instance with specified <code>options</code>..。
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantee an acquire order by threads.
     *
     * @param options 实例选项
     * @return Lock object
     */
    RFencedLockReactive getFencedLock(CommonOptions options);

    /**
     * 返回MultiLock instance associated with specified <code>locks</code>。
     *
     * @param locks collection of locks
     * @return MultiLock object
     */
    RLockReactive getMultiLock(RLockReactive... locks);
    /**
     * 返回RedissonFasterMultiLock instance associated with specified <code>group</code> and <code>values</code>。
     *
     * @param group the group of values
     * @param values lock values
     * @return BatchLock object
     */
    RLockReactive getMultiLock(String group, Collection<Object> values);
    /*
     * Use getMultiLock(RLockReactive) method instead
     */
    @Deprecated
    RLockReactive getMultiLock(RLock... locks);
    
    /*
     * Use getMultiLock method instead. Returned instance uses Redis Slave synchronization
     */
    @Deprecated
    RLockReactive getRedLock(RLock... locks);

    /**
     * 返回CountDownLatch instance by name.。
     *
     * @param name 对象名称
     * @return CountDownLatch object
     */
    RCountDownLatchReactive getCountDownLatch(String name);

    /**
     * 返回countDownLatch instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return CountDownLatch object
     */
    RCountDownLatchReactive getCountDownLatch(CommonOptions options);

    /**
     * 返回set-based cache instance by <code>name</code>.。
     * Supports value eviction with a given TTL value.
     *
     * <p>If eviction is not required then it's better to use regular map {@link #getSet(String, Codec)}.</p>
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return SetCache object
     */
    <V> RSetCacheReactive<V> getSetCache(String name);

    /**
     * 返回set-based cache instance by <code>name</code>.。
     * Supports value eviction with a given TTL value.
     *
     * <p>If eviction is not required then it's better to use regular map {@link #getSet(String, Codec)}.</p>
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec 值编解码器
     * @return SetCache object
     */
    <V> RSetCacheReactive<V> getSetCache(String name, Codec codec);

    /**
     * 返回set-based cache instance with specified <code>options</code>.。
     * Supports value eviction with a given TTL value.
     *
     * <p>If eviction is not required then it's better to use regular map {@link #getSet(PlainOptions)}.</p>
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return SetCache object
     */
    <V> RSetCacheReactive<V> getSetCache(PlainOptions options);

    /**
     * 返回map-based cache instance by name。
     * using provided codec for both cache keys and values.
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String, Codec)}.
     *
     * @param <K> type of keys
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec 值编解码器
     * @return MapCache object
     */
    <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec);

    /**
     * 返回map-based cache instance by <code>name</code>。
     * using provided <code>codec</code> for both cache keys and values.
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String, Codec, MapOptions)}.
     * 
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name object name
     * @param codec codec for keys and values
     * @param options map options
     * @return MapCache object
     */
    @Deprecated
    <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec, MapCacheOptions<K, V> options);

    /**
     * 返回map-based cache instance by name.。
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String)}.
     *
     * @param <K> type of keys
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return MapCache object
     */
    <K, V> RMapCacheReactive<K, V> getMapCache(String name);

    /**
     * 返回map-based cache instance by name.。
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String, MapOptions)}.</p>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param options map options
     * @return MapCache object
     */
    @Deprecated
    <K, V> RMapCacheReactive<K, V> getMapCache(String name, MapCacheOptions<K, V> options);

    /**
     * 返回map-based cache instance with specified <code>options</code>.。
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(org.redisson.api.options.MapOptions)}.</p>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return MapCache object
     */
    <K, V> RMapCacheReactive<K, V> getMapCache(org.redisson.api.options.MapCacheOptions<K, V> options);

    /**
     * 返回map instance by name.。
     * Supports entry eviction with a given TTL.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @return Map object
     */
    <K, V> RMapCacheNativeReactive<K, V> getMapCacheNative(String name);

    /**
     * 返回map instance by name。
     * using provided codec for both map keys and values.
     * Supports entry eviction with a given TTL.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return Map object
     */
    <K, V> RMapCacheNativeReactive<K, V> getMapCacheNative(String name, Codec codec);

    /**
     * 返回map instance.。
     * Supports entry eviction with a given TTL.
     * Configured by the parameters of the options-object.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return Map object
     */
    <K, V> RMapCacheNativeReactive<K, V> getMapCacheNative(org.redisson.api.options.MapOptions<K, V> options);

    /**
     * 返回object holder instance by name。
     * 
     * @param <V> 元素类型
     * @param name 对象名称
     * @return Bucket object
     */
    <V> RBucketReactive<V> getBucket(String name);

    /**
     * 返回object holder instance by name。
     * using provided codec for object.
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for value
     * @return Bucket object
     */
    <V> RBucketReactive<V> getBucket(String name, Codec codec);

    /**
     * 返回object holder instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return Bucket object
     */
    <V> RBucketReactive<V> getBucket(PlainOptions options);

    /**
     * 返回interface for mass operations with Bucket objects.。
     *
     * @return Buckets
     */
    RBucketsReactive getBuckets();

    /**
     * 返回interface for mass operations with Bucket objects。
     * using provided codec for object.
     *
     * @param codec codec for bucket objects
     * @return Buckets
     */
    RBucketsReactive getBuckets(Codec codec);

    /**
     * 返回API for mass operations over Bucket objects with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return Buckets object
     */
    RBucketsReactive getBuckets(OptionalOptions options);

    /**
     * 返回interface for mass operations with Map objects.。
     *
     * @return Maps object
     */
    <K, V> RMapsReactive<K, V> getMaps();

    /**
     * 返回interface for mass operations with Map objects。
     * using provided codec for keys and values.
     *
     * @param codec codec for keys and values
     * @return Maps object
     */
    <K, V> RMapsReactive<K, V> getMaps(Codec codec);

    /**
     * 返回API for mass operations over Map objects with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return Maps object
     */
    <K, V> RMapsReactive<K, V> getMaps(OptionalOptions options);

    /**
     * 请改用 {@link #getBuckets()}。（Redisson API）。
     *
     * @param <V> 元素类型
     * @param pattern pattern for name of buckets
     * @return list of buckets 
     */
    @Deprecated
    <V> List<RBucketReactive<V>> findBuckets(String pattern);

    /**
     * 返回JSON data holder instance by name using provided codec.。
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return JsonBucket object
     */
    <V> RJsonBucketReactive<V> getJsonBucket(String name, JsonCodec codec);

    /**
     * 返回JSON data holder instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return JsonBucket object
     */
    <V> RJsonBucketReactive<V> getJsonBucket(JsonBucketOptions<V> options);
    
    /**
     * 返回API for mass operations over JsonBucket objects。
     * using provided codec for JSON object with default path.
     *
     * @param codec using provided codec for JSON object with default path.
     * @return JsonBuckets
     */
    RJsonBucketsReactive getJsonBuckets(JsonCodec codec);
    
    /**
     * 返回HyperLogLog instance by name.。
     * 
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return HyperLogLog object
     */
    <V> RHyperLogLogReactive<V> getHyperLogLog(String name);

    /**
     * 返回HyperLogLog instance by name。
     * using provided codec for hll objects.
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec codec of values
     * @return HyperLogLog object
     */
    <V> RHyperLogLogReactive<V> getHyperLogLog(String name, Codec codec);

    /**
     * 返回HyperLogLog instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return HyperLogLog object
     */
    <V> RHyperLogLogReactive<V> getHyperLogLog(PlainOptions options);

    /**
     * 返回id generator by name.。
     *
     * @param name 对象名称
     * @return IdGenerator object
     */
    RIdGeneratorReactive getIdGenerator(String name);

    /**
     * 返回id generator instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return IdGenerator object
     */
    RIdGeneratorReactive getIdGenerator(CommonOptions options);

    /**
     * 按名称获取 {@link RList} 实例。
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return List object
     */
    <V> RListReactive<V> getList(String name);

    /**
     * 返回list instance by name。
     * using provided codec for list objects.
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec 值编解码器
     * @return List object
     */
    <V> RListReactive<V> getList(String name, Codec codec);

    /**
     * 返回list instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return List object
     */
    <V> RListReactive<V> getList(PlainOptions options);

    /**
     * 返回List based Multimap instance by name.。
     * 
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @return ListMultimap object
     */
    <K, V> RListMultimapReactive<K, V> getListMultimap(String name);

    /**
     * 返回List based Multimap instance by name。
     * using provided codec for both map keys and values.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return RListMultimapReactive object
     */
    <K, V> RListMultimapReactive<K, V> getListMultimap(String name, Codec codec);

    /**
     * 返回List based Multimap instance with specified <code>options</code>.。
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return ListMultimap object
     */
    <K, V> RListMultimapReactive<K, V> getListMultimap(PlainOptions options);

    /**
     * 返回List based Multimap cache instance by name.。
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular list multimap {@link #getListMultimap(String)}.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @return RListMultimapCacheReactive object
     */
    <K, V> RListMultimapCacheReactive<K, V> getListMultimapCache(String name);

    /**
     * 返回List based Multimap cache instance by name using provided codec for both map keys and values.。
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular list multimap {@link #getListMultimap(String, Codec)}.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return RListMultimapCacheReactive object
     */
    <K, V> RListMultimapCacheReactive<K, V> getListMultimapCache(String name, Codec codec);

    /**
     * 返回List based Multimap instance by name.。
     * Supports key-entry eviction with a given TTL value.
     *
     * <p>If eviction is not required then it's better to use regular map {@link #getSetMultimap(String)}.</p>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return ListMultimapCache object
     */
    <K, V> RListMultimapCacheReactive<K, V> getListMultimapCache(PlainOptions options);

    /**
     * 返回List based Multimap instance by name.。
     * Supports key-entry eviction with a given TTL value.
     * Stores insertion order and allows duplicates for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @return ListMultimapCache object
     */
    <K, V> RListMultimapCacheNativeReactive<K, V> getListMultimapCacheNative(String name);

    /**
     * 返回List based Multimap instance by name。
     * using provided codec for both map keys and values.
     * Supports key-entry eviction with a given TTL value.
     * Stores insertion order and allows duplicates for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return ListMultimapCache object
     */
    <K, V> RListMultimapCacheNativeReactive<K, V> getListMultimapCacheNative(String name, Codec codec);

    /**
     * 返回List based Multimap instance by name.。
     * Supports key-entry eviction with a given TTL value.
     * Stores insertion order and allows duplicates for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return ListMultimapCache object
     */
    <K, V> RListMultimapCacheNativeReactive<K, V> getListMultimapCacheNative(PlainOptions options);

    /**
     * 返回Set based Multimap instance by name.。
     * 
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @return SetMultimap object
     */
    <K, V> RSetMultimapReactive<K, V> getSetMultimap(String name);

    /**
     * 返回Set based Multimap instance by name。
     * using provided codec for both map keys and values.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return SetMultimap object
     */
    <K, V> RSetMultimapReactive<K, V> getSetMultimap(String name, Codec codec);

    /**
     * 返回Set based Multimap instance with specified <code>options</code>.。
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return SetMultimap object
     */
    <K, V> RSetMultimapReactive<K, V> getSetMultimap(PlainOptions options);

    /**
     * 返回Set based Multimap cache instance by name.。
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular set multimap {@link #getSetMultimap(String)}.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @return RSetMultimapCacheReactive object
     */
    <K, V> RSetMultimapCacheReactive<K, V> getSetMultimapCache(String name);

    /**
     * 返回Set based Multimap cache instance by name using provided codec for both map keys and values.。
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular set multimap {@link #getSetMultimap(String, Codec)}.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return RSetMultimapCacheReactive object
     */
    <K, V> RSetMultimapCacheReactive<K, V> getSetMultimapCache(String name, Codec codec);

    /**
     * 返回Set based Multimap instance with specified <code>options</code>.。
     * Supports key-entry eviction with a given TTL value.
     *
     * <p>If eviction is not required then it's better to use regular map {@link #getSetMultimap(PlainOptions)}.</p>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return SetMultimapCache object
     */
    <K, V> RSetMultimapCacheReactive<K, V> getSetMultimapCache(PlainOptions options);

    /**
     * 返回Set based Multimap instance by name.。
     * Supports key-entry eviction with a given TTL value.
     * Doesn't allow duplications for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @return SetMultimapCache object
     */
    <K, V> RSetMultimapCacheNativeReactive<K, V> getSetMultimapCacheNative(String name);

    /**
     * 返回Set based Multimap instance by name。
     * using provided codec for both map keys and values.
     * Supports key-entry eviction with a given TTL value.
     * Doesn't allow duplications for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return SetMultimapCache object
     */
    <K, V> RSetMultimapCacheNativeReactive<K, V> getSetMultimapCacheNative(String name, Codec codec);

    /**
     * 返回Set based Multimap instance with specified <code>options</code>.。
     * Supports key-entry eviction with a given TTL value.
     * Doesn't allow duplications for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return SetMultimapCache object
     */
    <K, V> RSetMultimapCacheNativeReactive<K, V> getSetMultimapCacheNative(PlainOptions options);

    /**
     * 返回map instance by name.。
     *
     * @param <K> type of keys
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return Map object
     */
    <K, V> RMapReactive<K, V> getMap(String name);

    /**
     * 返回map instance by name.。
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param options map options
     * @return Map object
     */
    @Deprecated
    <K, V> RMapReactive<K, V> getMap(String name, MapOptions<K, V> options);

    /**
     * 返回map instance by name。
     * using provided codec for both map keys and values.
     *
     * @param <K> type of keys
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec codec for keys and values
     * @return Map object
     */
    <K, V> RMapReactive<K, V> getMap(String name, Codec codec);

    /**
     * 返回map instance by name。
     * using provided codec for both map keys and values.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @param options map options
     * @return Map object
     */
    @Deprecated
    <K, V> RMapReactive<K, V> getMap(String name, Codec codec, MapOptions<K, V> options);

    /**
     * 返回map instance by name.。
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return Map object
     */
    <K, V> RMapReactive<K, V> getMap(org.redisson.api.options.MapOptions<K, V> options);

    /**
     * 返回local cached map instance by name.。
     * Configured by parameters of options-object.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param options local map options
     * @return LocalCachedMap object
     */
    @Deprecated
    <K, V> RLocalCachedMapReactive<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options);

    /**
     * 返回local cached map instance by name。
     * using provided codec. Configured by parameters of options-object.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec codec for keys and values
     * @param options local map options
     * @return LocalCachedMap object
     */
    @Deprecated
    <K, V> RLocalCachedMapReactive<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options);

    /**
     * 返回local cached map instance with specified <code>options</code>.。
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param options 实例选项
     * @return LocalCachedMap object
     */
    <K, V> RLocalCachedMapReactive<K, V> getLocalCachedMap(org.redisson.api.options.LocalCachedMapOptions<K, V> options);

    /**
     * 返回local cached map cache instance by name.。
     * Configured by parameters of options-object.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name - name of object
     * @param options - local map options
     * @return LocalCachedMapCache object
     */
    <K, V> RLocalCachedMapCacheReactive<K, V> getLocalCachedMapCache(String name, LocalCachedMapCacheOptions<K, V> options);

    /**
     * 返回local cached map cache instance by name using provided codec.。
     * Configured by parameters of options-object.
     *
     * @param <K> type of key
     * @param <V> 元素类型
     * @param name - name of object
     * @param codec - codec for keys and values
     * @param options - local map options
     * @return LocalCachedMap object
     */
    <K, V> RLocalCachedMapCacheReactive<K, V> getLocalCachedMapCache(String name, Codec codec, LocalCachedMapCacheOptions<K, V> options);

    /**
     * 按名称获取 {@link RSet} 实例。
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return Set object
     */
    <V> RSetReactive<V> getSet(String name);

    /**
     * 返回set instance by name。
     * using provided codec for set objects.
     *
     * @param <V> 元素类型s
     * @param name name of set
     * @param codec 值编解码器
     * @return Set object
     */
    <V> RSetReactive<V> getSet(String name, Codec codec);

    /**
     * 返回set instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return Set object
     */
    <V> RSetReactive<V> getSet(PlainOptions options);

    /**
     * Set 相关操作：按名称获取 {@link RScoredSortedSet} 实例（按 score 排序）。。
     * This sorted set sorts objects by object score.
     * 
     * @param <V> 元素类型s
     * @param name of scored sorted set
     * @return ScoredSortedSet object
     */
    <V> RScoredSortedSetReactive<V> getScoredSortedSet(String name);

    /**
     * 返回Redis Sorted Set instance by name。
     * using provided codec for sorted set objects.
     * This sorted set sorts objects by object score.
     * 
     * @param <V> 元素类型s
     * @param name name of scored sorted set
     * @param codec 值编解码器
     * @return ScoredSortedSet object
     */
    <V> RScoredSortedSetReactive<V> getScoredSortedSet(String name, Codec codec);

    /**
     * 返回Redis Sorted Set instance with specified <code>options</code>.。
     * This sorted set sorts objects by object score.
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return ScoredSortedSet object
     */
    <V> RScoredSortedSetReactive<V> getScoredSortedSet(PlainOptions options);

    /**
     * 返回String based Redis Sorted Set instance by name。
     * All elements are inserted with the same score during addition,
     * in order to force lexicographical ordering
     *
     * @param name 对象名称
     * @return LexSortedSet object
     */
    RLexSortedSetReactive getLexSortedSet(String name);

    /**
     * 返回String based Redis Sorted Set instance with specified <code>options</code>.。
     * All elements are inserted with the same score during addition,
     * in order to force lexicographical ordering
     *
     * @param options 实例选项
     * @return LexSortedSet object
     */
    RLexSortedSetReactive getLexSortedSet(CommonOptions options);

    /**
     * 返回Sharded Topic instance by name.。
     * <p>
     * Messages are delivered to message listeners connected to the same Topic.
     * <p>
     *
     * @param name 对象名称
     * @return Topic object
     */
    RShardedTopicReactive getShardedTopic(String name);

    /**
     * 返回Sharded Topic instance by name using provided codec for messages.。
     * <p>
     * Messages are delivered to message listeners connected to the same Topic.
     * <p>
     *
     * @param name 对象名称
     * @param codec codec for message
     * @return Topic object
     */
    RShardedTopicReactive getShardedTopic(String name, Codec codec);

    /**
     * 返回Sharded Topic instance with specified <code>options</code>.。
     * <p>
     * Messages are delivered to message listeners connected to the same Topic.
     * <p>
     *
     * @param options 实例选项
     * @return Topic object
     */
    RShardedTopicReactive getShardedTopic(PlainOptions options);

    /**
     * 返回topic instance by name.。
     *
     * @param name 对象名称
     * @return Topic object
     */
    RTopicReactive getTopic(String name);

    /**
     * 返回topic instance by name。
     * using provided codec for messages.
     *
     * @param name 对象名称
     * @param codec codec for message
     * @return Topic object
     */
    RTopicReactive getTopic(String name, Codec codec);

    /**
     * 返回topic instance with specified <code>options</code>.。
     * <p>
     * Messages are delivered to message listeners connected to the same Topic.
     * <p>
     *
     * @param options 实例选项
     * @return Topic object
     */
    RTopicReactive getTopic(PlainOptions options);

    /**
     * 返回reliable topic instance by name.。
     * <p>
     * Dedicated Redis connection is allocated per instance (subscriber) of this object.
     * Messages are delivered to all listeners attached to the same Redis setup.
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param name 对象名称
     * @return ReliableTopic object
     */
    RReliableTopicReactive getReliableTopic(String name);

    /**
     * 返回reliable topic instance by name。
     * using provided codec for messages.
     * <p>
     * Dedicated Redis connection is allocated per instance (subscriber) of this object.
     * Messages are delivered to all listeners attached to the same Redis setup.
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param name 对象名称
     * @param codec codec for message
     * @return ReliableTopic object
     */
    RReliableTopicReactive getReliableTopic(String name, Codec codec);

    /**
     * 返回reliable topic instance with specified <code>options</code>.。
     * <p>
     * Dedicated Redis connection is allocated per instance (subscriber) of this object.
     * Messages are delivered to all listeners attached to the same Redis setup.
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param options 实例选项
     * @return ReliableTopic object
     */
    RReliableTopicReactive getReliableTopic(PlainOptions options);

    /**
     * 返回topic instance satisfies by pattern name.。
     *
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern of the topic
     * @return PatternTopic object
     */
    RPatternTopicReactive getPatternTopic(String pattern);

    /**
     * 返回topic instance satisfies by pattern name。
     * using provided codec for messages.
     *
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern of the topic
     * @param codec codec for message
     * @return PatternTopic object
     */
    RPatternTopicReactive getPatternTopic(String pattern, Codec codec);

    /**
     * 返回topic instance satisfies pattern name and specified <code>options</code>..。
     *
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param options 实例选项
     * @return PatterTopic object
     */
    RPatternTopicReactive getPatternTopic(PatternTopicOptions options);

    /**
     * 返回queue instance by name.。
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return Queue object
     */
    <V> RQueueReactive<V> getQueue(String name);

    /**
     * 返回queue instance by name。
     * using provided codec for queue objects.
     * 
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec 值编解码器
     * @return Queue object
     */
    <V> RQueueReactive<V> getQueue(String name, Codec codec);

    /**
     * 返回unbounded queue instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return queue object
     */
    <V> RQueueReactive<V> getQueue(PlainOptions options);

    /**
     * 返回a reliable queue instance by name.。
     * <p>
     * The reliable queue provides guaranteed message delivery through acknowledgment mechanisms
     * and synchronous replication.
     *
     * @param name the name of the queue
     * @param <V> the type of elements in this queue
     * @return Reliable queue instance
     */
    <V> RReliableQueueReactive<V> getReliableQueue(String name);

    /**
     * 返回a reliable queue instance by name and provided codec.。
     * <p>
     * The reliable queue provides guaranteed message delivery through acknowledgment mechanisms
     * and synchronous replication.
     *
     * @param name the name of the queue
     * @param codec the codec used for message serialization and deserialization
     * @param <V> the type of elements in this queue
     * @return Reliable queue instance
     */
    <V> RReliableQueueReactive<V> getReliableQueue(String name, Codec codec);

    /**
     * 返回a reliable queue instance with the specified configuration options.。
     * <p>
     * The reliable queue provides guaranteed message delivery through acknowledgment mechanisms
     * and synchronous replication.
     *
     * @param options configuration options for the reliable queue
     * @param <V> the type of elements in this queue
     * @return Reliable queue instance
     */
    <V> RReliableQueueReactive<V> getReliableQueue(PlainOptions options);

    /**
     * 返回RingBuffer based queue.。
     * 
     * @param <V> 值类型
     * @param name 对象名称
     * @return RingBuffer object
     */
    <V> RRingBufferReactive<V> getRingBuffer(String name);
    
    /**
     * 返回RingBuffer based queue.。
     * 
     * @param <V> 值类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return RingBuffer object
     */
    <V> RRingBufferReactive<V> getRingBuffer(String name, Codec codec);

    /**
     * 返回RingBuffer based queue instance with specified <code>options</code>.。
     *
     * @param <V> 值类型
     * @param options 实例选项
     * @return RingBuffer object
     */
    <V> RRingBufferReactive<V> getRingBuffer(PlainOptions options);

    /**
     * 返回circular (ring) buffer instance by <code>name</code>.。
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param name 实例名称
     * @return CircularBuffer object
     */
    <V> RCircularBufferReactive<V> getCircularBuffer(String name);

    /**
     * 返回circular (ring) buffer instance by <code>name</code>。
     * using provided <code>codec</code> for values.
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param name 实例名称
     * @param codec 值编解码器
     * @return CircularBuffer object
     */
    <V> RCircularBufferReactive<V> getCircularBuffer(String name, Codec codec);

    /**
     * 返回circular (ring) buffer instance with specified <code>options</code>.。
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param options 实例选项
     * @return CircularBuffer object
     */
    <V> RCircularBufferReactive<V> getCircularBuffer(PlainOptions options);

    /**
     * 返回blocking queue instance by name.。
     * 
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return BlockingQueue object
     */
    <V> RBlockingQueueReactive<V> getBlockingQueue(String name);

    /**
     * 返回blocking queue instance by name。
     * using provided codec for queue objects.
     * 
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec code for values
     * @return BlockingQueue object
     */
    <V> RBlockingQueueReactive<V> getBlockingQueue(String name, Codec codec);

    /**
     * 返回unbounded blocking queue instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return BlockingQueue object
     */
    <V> RBlockingQueueReactive<V> getBlockingQueue(PlainOptions options);

    /**
     * 按名称获取无界 {@link RBlockingDeque} 实例。
     * 
     * @param <V> 元素类型
     * @param name 对象名称
     * @return BlockingDeque object
     */
    <V> RBlockingDequeReactive<V> getBlockingDeque(String name);

    /**
     * 返回unbounded blocking deque instance by name。
     * using provided codec for deque objects.
     * 
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec deque objects codec
     * @return BlockingDeque object
     */
    <V> RBlockingDequeReactive<V> getBlockingDeque(String name, Codec codec);

    /**
     * 返回unbounded blocking deque instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return BlockingDeque object
     */
    <V> RBlockingDequeReactive<V> getBlockingDeque(PlainOptions options);

    /**
     * 返回transfer queue instance by name.。
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return TransferQueue object
     */
    <V> RTransferQueueReactive<V> getTransferQueue(String name);

    /**
     * 返回transfer queue instance by name。
     * using provided codec for queue objects.
     *
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec code for values
     * @return TransferQueue object
     */
    <V> RTransferQueueReactive<V> getTransferQueue(String name, Codec codec);

    /**
     * 返回transfer queue instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型s
     * @param options 实例选项
     * @return TransferQueue object
     */
    <V> RTransferQueueReactive<V> getTransferQueue(PlainOptions options);

    /**
     * 返回deque instance by name.。
     * 
     * @param <V> 元素类型s
     * @param name 对象名称
     * @return Deque object
     */
    <V> RDequeReactive<V> getDeque(String name);

    /**
     * 返回deque instance by name。
     * using provided codec for deque objects.
     * 
     * @param <V> 元素类型s
     * @param name 对象名称
     * @param codec coded for values
     * @return Deque object
     */
    <V> RDequeReactive<V> getDeque(String name, Codec codec);

    /**
     * 返回unbounded deque instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return Deque object
     */
    <V> RDequeReactive<V> getDeque(PlainOptions options);

    /**
     * 返回"atomic long" instance by name.。
     *
     * @param name of the "atomic long"
     * @return AtomicLong object
     */
    RAtomicLongReactive getAtomicLong(String name);

    /**
     * 返回atomicLong instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return AtomicLong object
     */
    RAtomicLongReactive getAtomicLong(CommonOptions options);

    /**
     * 返回"atomic double" instance by name.。
     *
     * @param name of the "atomic double"
     * @return AtomicLong object
     */
    RAtomicDoubleReactive getAtomicDouble(String name);

    /**
     * 返回atomicDouble instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return AtomicDouble object
     */
    RAtomicDoubleReactive getAtomicDouble(CommonOptions options);

    /**
     * 返回object for remote operations prefixed with the default name (redisson_remote_service)。
     * 
     * @return RemoteService object
     */
    @Deprecated
    RRemoteService getRemoteService();
    
    /**
     * 返回object for remote operations prefixed with the default name (redisson_remote_service)。
     * and uses provided codec for method arguments and result.
     * 
     * @param codec codec for response and request
     * @return RemoteService object
     */
    @Deprecated
    RRemoteService getRemoteService(Codec codec);

    /**
     * 返回object for remote operations prefixed with the specified name。
     *
     * @param name the name used as the Redis key prefix for the services
     * @return RemoteService object
     */
    RRemoteService getRemoteService(String name);
    
    /**
     * 返回object for remote operations prefixed with the specified name。
     * and uses provided codec for method arguments and result.
     *
     * @param name the name used as the Redis key prefix for the services
     * @param codec codec for response and request
     * @return RemoteService object
     */
    RRemoteService getRemoteService(String name, Codec codec);

    /**
     * 返回object for remote operations prefixed with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return RemoteService object
     */
    RRemoteService getRemoteService(PlainOptions options);

    /**
     * 返回bitSet instance by name.。
     *
     * @param name 对象名称
     * @return BitSet object
     */
    RBitSetReactive getBitSet(String name);

    /**
     * 返回bitSet instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return BitSet object
     */
    RBitSetReactive getBitSet(CommonOptions options);

    /**
     * 返回bloom filter instance by name.。
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @return BloomFilter object
     */
    <V> RBloomFilterReactive<V> getBloomFilter(String name);

    /**
     * 返回bloom filter instance by name。
     * using provided codec for objects.
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return BloomFilter object
     */
    <V> RBloomFilterReactive<V> getBloomFilter(String name, Codec codec);

    /**
     * 返回bloom filter instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return BloomFilter object
     */
    <V> RBloomFilterReactive<V> getBloomFilter(PlainOptions options);

    /**
     * 返回bloom filter native instance by name.。
     * using BF.* Commands
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @return BloomFilterNative object
     */
    <V> RBloomFilterNativeReactive<V> getBloomFilterNative(String name);

    /**
     * 返回bloom filter native instance by name。
     * using BF.* Commands
     * using provided codec for objects.
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return BloomFilterNative object
     */
    <V> RBloomFilterNativeReactive<V> getBloomFilterNative(String name, Codec codec);

    /**
     * 返回bloom filter native instance with specified <code>options</code>.。
     * using BF.* Commands
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return BloomFilterNative object
     */
    <V> RBloomFilterNativeReactive<V> getBloomFilterNative(PlainOptions options);

    /**
     * 返回cuckoo filter instance by <code>name</code>.。
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @return CuckooFilter object
     */
    <V> RCuckooFilterReactive<V> getCuckooFilter(String name);

    /**
     * 返回cuckoo filter instance by <code>name</code>。
     * using provided <code>codec</code> for values.
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return CuckooFilter object
     */
    <V> RCuckooFilterReactive<V> getCuckooFilter(String name, Codec codec);

    /**
     * 返回cuckoo filter instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return CuckooFilter object
     */
    <V> RCuckooFilterReactive<V> getCuckooFilter(PlainOptions options);

    /**
     * 返回Top-K sketch instance by <code>name</code>.。
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @return TopK object
     */
    <V> RTopKReactive<V> getTopK(String name);

    /**
     * 返回Top-K sketch instance by <code>name</code>。
     * using provided <code>codec</code> for values.
     *
     * @param <V> 元素类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return TopK object
     */
    <V> RTopKReactive<V> getTopK(String name, Codec codec);

    /**
     * 返回Top-K sketch instance with specified <code>options</code>.。
     *
     * @param <V> 元素类型
     * @param options 实例选项
     * @return TopK object
     */
    <V> RTopKReactive<V> getTopK(PlainOptions options);

    /**
     * 返回t-digest instance by <code>name</code>.。
     *
     * @param name 对象名称
     * @return TDigest object
     */
    RTDigestReactive getTDigest(String name);

    /**
     * 返回t-digest instance with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return TDigest object
     */
    RTDigestReactive getTDigest(PlainOptions options);

    /**
     * 返回interface for Redis Function feature。
     *
     * @return function object
     */
    RFunctionReactive getFunction();

    /**
     * 返回interface for Redis Function feature using provided codec。
     *
     * @param codec codec for params and result
     * @return function interface
     */
    RFunctionReactive getFunction(Codec codec);

    /**
     * 返回interface for Redis Function feature with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return function object
     */
    RFunctionReactive getFunction(OptionalOptions options);

    /**
     * 返回script operations object。
     *
     * @return Script object
     */
    RScriptReactive getScript();

    /**
     * 返回script operations object using provided codec.。
     * 
     * @param codec codec for params and result
     * @return Script object
     */
    RScriptReactive getScript(Codec codec);

    /**
     * 返回script operations object with specified <code>options</code>.。
     *
     * @param options 实例选项
     * @return Script object
     */
    RScriptReactive getScript(OptionalOptions options);

    /**
     * 返回vector set instance by name.。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b>
     *
     * @param name - name of vector set
     * @return vector set instance
     */
    RVectorSetReactive getVectorSet(String name);

    /**
     * 返回vector set instance by name with specified <code>options</code>.。
     * <p>
     * Requires <b>Redis 8.0.0 and higher.</b>
     *
     * @param options 实例选项
     * @return vector set instance
     */
    RVectorSetReactive getVectorSet(CommonOptions options);

    /**
     * 创建transaction with <b>READ_COMMITTED</b> isolation level.。
     * 
     * @param options transaction configuration
     * @return Transaction object
     */
    RTransactionReactive createTransaction(TransactionOptions options);
    
    /**
     * Return batch object which executes group of
     * command in pipeline.
     *
     * See <a href="http://redis.io/topics/pipelining">http://redis.io/topics/pipelining</a>
     *
     * @param options batch configuration
     * @return Batch object
     */
    RBatchReactive createBatch(BatchOptions options);

    /**
     * Return batch object which executes group of
     * command in pipeline.
     *
     * See <a href="http://redis.io/topics/pipelining">http://redis.io/topics/pipelining</a>
     *
     * @return Batch object
     */
    RBatchReactive createBatch();

    /**
     * 返回keys operations.。
     * Each of Redis/Redisson object associated with own key
     *
     * @return Keys object
     */
    RKeysReactive getKeys();

    /**
     * 返回interface for operations over Redis keys with specified <code>options</code>.。
     * Each of Redis/Redisson object is associated with own key.
     *
     * @return Keys object
     */
    RKeysReactive getKeys(KeysOptions options);

    /**
     * Use {@link RedissonClient#shutdown()} instead
     */
    @Deprecated
    void shutdown();

    /**
     * 返回创建客户端时使用的 {@link Config}。（Redisson API）。
     * during Redisson instance creation. Further changes on
     * this object not affect Redisson instance.
     *
     * @return Config object
     */
    Config getConfig();
    
    /**
     * 返回{@code true} if this Redisson instance has been shut down.。
     *
     * @return <code>true</code> if this Redisson instance has been shut down otherwise <code>false</code>
     */
    boolean isShutdown();

    /**
     * 返回{@code true} if this Redisson instance was started to be shutdown。
     * or was shutdown {@link #isShutdown()} already.
     *
     * @return <code>true</code> if this Redisson instance was started to be shutdown
     * or was shutdown {@link #isShutdown()} already otherwise <code>false</code>
     */
    boolean isShuttingDown();

    /**
     * 返回id of this Redisson instance。
     * 
     * @return id
     */
    String getId();
    
}
