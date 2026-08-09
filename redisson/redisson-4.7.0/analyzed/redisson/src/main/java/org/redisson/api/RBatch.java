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

import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;

/**
 * Redis 管道/批处理接口。
     * <p>
通过本接口获取的对象，其方法调用会进入批处理队列，稍后通过 {@code execute()} 或 {@code executeAsync()} 一次性提交。
 * <p>
 * 通过本接口获取的对象，其全部方法调用 
 * 会进入独立批处理队列，可稍后
 * 通过 {@code execute()} 或 {@code executeAsync()} 执行。
 *
 *
 * @author Nikita Koksharov
 *
 */
public interface RBatch {

    /**
     * 按名称返回 {@link RArray} 批处理对象。
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param name 对象名称
     * @return {@link RArray} 对象
     */
    <V> RArrayAsync<V> getArray(String name);

    /**
     * 按名称返回 {@link RArray} 批处理对象
     * 并使用指定 {@code codec} 编解码值。
     * <p>
     * 需要 <b>Redis 8.8 及以上</b>。
     *
     * @param <V> 值类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return {@link RArray} 对象
     */
    <V> RArrayAsync<V> getArray(String name, Codec codec);

    /**
     * 按名称返回原生布隆过滤器 {@link RBloomFilterNative} 批处理对象。
     * 覆盖 BF.* 布隆过滤器命令。
     *
     * @param <T> 对象类型
     * @param name 对象名称
     * @return {@link RBloomFilterNative} 对象
     */
    <T> RBloomFilterNativeAsync<T> getBloomFilterNative(String name);

    /**
     * 按名称返回原生布隆过滤器批处理对象
     * 并使用指定 {@code codec} 编解码对象。
     * 覆盖 BF.* 布隆过滤器命令。
     *
     * @param <T> 对象类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return {@link RBloomFilterNative} 对象
     */
    <T> RBloomFilterNativeAsync<T> getBloomFilterNative(String name, Codec codec);

    /**
     * Returns cuckoo filter instance by <code>name</code>.
     *
     * @param <V> type of value
     * @param name name of object
     * @return CuckooFilter object
     */
    <V> RCuckooFilterAsync<V> getCuckooFilter(String name);

    /**
     * Returns cuckoo filter instance by <code>name</code>
     * 并使用指定 {@code codec} 编解码值。
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec 值编解码器
     * @return CuckooFilter object
     */
    <V> RCuckooFilterAsync<V> getCuckooFilter(String name, Codec codec);

    /**
     * 按名称返回 Top-K 草图批处理对象。
     *
     * @param <V> type of value
     * @param name name of object
     * @return TopK object
     */
    <V> RTopKAsync<V> getTopK(String name);

    /**
     * 按名称返回 Top-K 草图批处理对象
     * 并使用指定 {@code codec} 编解码值。
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec 值编解码器
     * @return TopK object
     */
    <V> RTopKAsync<V> getTopK(String name, Codec codec);

    /**
     * Returns t-digest instance by <code>name</code>.
     *
     * @param name name of object
     * @return TDigest object
     */
    RTDigestAsync getTDigest(String name);

    /**
     * Returns vector set instance by name.
     * Stores vectors and associated elements in a set optimized for similarity search.
     *
     * @return RVectorSet object
     */
    RVectorSetAsync getVectorSet(String name);

    /**
     * Returns stream instance by <code>name</code>
     * 
     * @param <K> type of key
     * @param <V> type of value
     * @param name of stream
     * @return RStream object
     */
    <K, V> RStreamAsync<K, V> getStream(String name);
    
    /**
     * Returns stream instance by <code>name</code>
     * using provided <code>codec</code> for entries.
     * 
     * @param <K> type of key
     * @param <V> type of value
     * @param name - name of stream
     * @param codec - codec for entry
     * @return RStream object
     */
    <K, V> RStreamAsync<K, V> getStream(String name, Codec codec);
    
    /**
     * Returns geospatial items holder instance by <code>name</code>.
     * 
     * @param <V> type of object
     * @param name 对象名称
     * @return Geo object
     */
    <V> RGeoAsync<V> getGeo(String name);

    /**
     * Returns geospatial items holder instance by <code>name</code>
     * using provided codec for geospatial members.
     *
     * @param <V> type of value
     * @param name 对象名称
     * @param codec - codec for value
     * @return Geo object
     */
    <V> RGeoAsync<V> getGeo(String name, Codec codec);
    
    /**
     * 按名称返回 Set Multimap 批处理对象。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @return Multimap object
     */
    <K, V> RMultimapAsync<K, V> getSetMultimap(String name);

    /**
     * 按名称返回 Set Multimap 批处理对象
     * using provided codec for both map keys and values.
     * 
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @param codec - provided codec
     * @return Multimap object
     */
    <K, V> RMultimapAsync<K, V> getSetMultimap(String name, Codec codec);
    
    /**
     * 按名称返回 Set Multimap 批处理对象。
     * Supports key-entry eviction with a given TTL value.
     * 
     * <p>若无需逐条 TTL，优先使用普通 SetMultimap {@link #getSetMultimap(String)}。</p>
     * 
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @return SetMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getSetMultimapCache(String name);

    /**
     * 按名称返回 Set Multimap 批处理对象
     * using provided codec for both map keys and values.
     * Supports key-entry eviction with a given TTL value.
     * 
     * <p>若无需逐条 TTL，优先使用普通 SetMultimap {@link #getSetMultimap(String, Codec)}。</p>
     * 
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @param codec - provided codec
     * @return SetMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getSetMultimapCache(String name, Codec codec);
    
    /**
     * Returns set-based cache instance by <code>name</code>.
     * Uses map (value_hash, value) under the hood for minimal memory consumption.
     * Supports value eviction with a given TTL value.
     *
     * <p>若无需逐条 TTL，优先使用普通 Set {@link #getSet(String, Codec)}。</p>
     *
     * @param <V> type of value
     * @param name 对象名称
     * @return SetCache object
     */
    <V> RSetCacheAsync<V> getSetCache(String name);

    /**
     * Returns set-based cache instance by <code>name</code>
     * 并使用指定 {@code codec} 编解码值。
     * Uses map (value_hash, value) under the hood for minimal memory consumption.
     * Supports value eviction with a given TTL value.
     *
     * <p>若无需逐条 TTL，优先使用普通 Set {@link #getSet(String, Codec)}。</p>
     *
     * @param <V> type of value
     * @param name 对象名称
     * @param codec 值编解码器
     * @return SetCache object
     */
    <V> RSetCacheAsync<V> getSetCache(String name, Codec codec);

    /**
     * Returns map-based cache instance by <code>name</code>
     * using provided <code>codec</code> for both cache keys and values.
     * Supports entry eviction with a given TTL value.
     *
     * <p>若无需逐条 TTL，优先使用普通 Map {@link #getMap(String, Codec)}。</p>
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @param codec - codec for keys and values
     * @return MapCache object
     */
    <K, V> RMapCacheAsync<K, V> getMapCache(String name, Codec codec);

    /**
     * Returns map-based cache instance by <code>name</code>.
     * Supports entry eviction with a given TTL value.
     *
     * <p>若无需逐条 TTL，优先使用普通 Map {@link #getMap(String)}。</p>
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @return MapCache object
     */
    <K, V> RMapCacheAsync<K, V> getMapCache(String name);

    /**
     * Returns map instance by name.
     * Supports entry eviction with a given TTL.
     * <p>
     * 需要 <b>Redis 7.4.0+</b> 或 <b>Valkey 9.0.0+</b>。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @return Map object
     */
    <K, V> RMapCacheNativeAsync<K, V> getMapCacheNative(String name);

    /**
     * Returns map instance by name
     * using provided codec for both map keys and values.
     * Supports entry eviction with a given TTL.
     * <p>
     * 需要 <b>Redis 7.4.0+</b> 或 <b>Valkey 9.0.0+</b>。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for keys and values
     * @return Map object
     */
    <K, V> RMapCacheNativeAsync<K, V> getMapCacheNative(String name, Codec codec);

    /**
     * Returns object holder by <code>name</code>
     *
     * @param <V> type of object
     * @param name 对象名称
     * @return Bucket object
     */
    <V> RBucketAsync<V> getBucket(String name);

    <V> RBucketAsync<V> getBucket(String name, Codec codec);

    /**
     * 按名称与 codec 返回 JSON 数据持有者批处理对象。
     *
     * @see org.redisson.codec.JacksonCodec
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec 值编解码器
     * @return JsonBucket object
     */
    <V> RJsonBucketAsync<V> getJsonBucket(String name, JsonCodec codec);

    /**
     * 返回 HyperLogLog 批处理对象
     *
     * @param <V> type of object
     * @param name 对象名称
     * @return HyperLogLog object
     */
    <V> RHyperLogLogAsync<V> getHyperLogLog(String name);

    <V> RHyperLogLogAsync<V> getHyperLogLog(String name, Codec codec);

    /**
     * Returns list instance by name.
     *
     * @param <V> type of object
     * @param name 对象名称
     * @return List object
     */
    <V> RListAsync<V> getList(String name);

    <V> RListAsync<V> getList(String name, Codec codec);

    /**
     * 按名称返回 List Multimap 批处理对象。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @return ListMultimap object
     */
    <K, V> RMultimapAsync<K, V> getListMultimap(String name);

    /**
     * 按名称返回 List Multimap 批处理对象
     * using provided codec for both map keys and values.
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @param codec - codec for keys and values
     * @return ListMultimap object
     */
    <K, V> RMultimapAsync<K, V> getListMultimap(String name, Codec codec);
    
    /**
     * 按名称返回 List Multimap 批处理对象。
     * Supports key-entry eviction with a given TTL value.
     * 
     * <p>若无需逐条 TTL，优先使用普通 SetMultimap {@link #getSetMultimap(String)}。</p>
     * 
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @return ListMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getListMultimapCache(String name);
    
    /**
     * 按名称返回 List Multimap 批处理对象
     * using provided codec for both map keys and values.
     * Supports key-entry eviction with a given TTL value.
     * 
     * <p>若无需逐条 TTL，优先使用普通 SetMultimap {@link #getSetMultimap(String, Codec)}。</p>
     * 
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @param codec - codec for keys and values
     * @return ListMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getListMultimapCache(String name, Codec codec);

    /**
     * 按名称返回 List Multimap 批处理对象。
     * Supports key-entry eviction with a given TTL value.
     * Stores insertion order and allows duplicates for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * 需要 <b>Redis 7.4.0+</b> 或 <b>Valkey 9.0.0+</b>。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @return ListMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getListMultimapCacheNative(String name);

    /**
     * 按名称返回 List Multimap 批处理对象
     * using provided codec for both map keys and values.
     * Supports key-entry eviction with a given TTL value.
     * Stores insertion order and allows duplicates for values mapped to key.
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * 需要 <b>Redis 7.4.0+</b> 或 <b>Valkey 9.0.0+</b>。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for keys and values
     * @return ListMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getListMultimapCacheNative(String name, Codec codec);

    /**
     * 按名称返回 Set Multimap 批处理对象。
     * Supports key-entry eviction with a given TTL value.
     * 同一 key 下不允许重复值。
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * 需要 <b>Redis 7.4.0+</b> 或 <b>Valkey 9.0.0+</b>。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @return SetMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getSetMultimapCacheNative(String name);

    /**
     * 按名称返回 Set Multimap 批处理对象
     * using provided codec for both map keys and values.
     * Supports key-entry eviction with a given TTL value.
     * 同一 key 下不允许重复值。
     * <p>
     * Uses Redis native commands for entry expiration and not a scheduled eviction task.
     * <p>
     * 需要 <b>Redis 7.4.0+</b> 或 <b>Valkey 9.0.0+</b>。
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for keys and values
     * @return SetMultimapCache object
     */
    <K, V> RMultimapCacheAsync<K, V> getSetMultimapCacheNative(String name, Codec codec);

    /**
     * Returns map instance by name.
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name 对象名称
     * @return Map object
     */
    <K, V> RMapAsync<K, V> getMap(String name);

    <K, V> RMapAsync<K, V> getMap(String name, Codec codec);

    /**
     * Returns set instance by name.
     *
     * @param <V> type of value
     * @param name 对象名称
     * @return Set object
     */
    <V> RSetAsync<V> getSet(String name);

    <V> RSetAsync<V> getSet(String name, Codec codec);

    /**
     * Returns topic instance by name.
     *
     * @param name 对象名称
     * @return Topic object
     */
    RTopicAsync getTopic(String name);

    RTopicAsync getTopic(String name, Codec codec);

    /**
     * 按名称返回分片 Topic 批处理对象。
     * <p>
     * 消息投递给连接同一 Topic 的监听器。
     * <p>
     *
     * @param name 对象名称
     * @return Topic object
     */
    RShardedTopicAsync getShardedTopic(String name);

    /**
     * 按名称与消息 codec 返回分片 Topic 批处理对象。
     * <p>
     * 消息投递给连接同一 Topic 的监听器。
     * <p>
     *
     * @param name 对象名称
     * @param codec - codec for message
     * @return Topic object
     */
    RShardedTopicAsync getShardedTopic(String name, Codec codec);

    /**
     * Returns queue instance by name.
     *
     * @param <V> type of value
     * @param name 对象名称
     * @return Queue object
     */
    <V> RQueueAsync<V> getQueue(String name);

    <V> RQueueAsync<V> getQueue(String name, Codec codec);

    /**
     * Returns blocking queue instance by name.
     *
     * @param <V> type of value
     * @param name 对象名称
     * @return BlockingQueue object
     */
    <V> RBlockingQueueAsync<V> getBlockingQueue(String name);

    <V> RBlockingQueueAsync<V> getBlockingQueue(String name, Codec codec);

    /**
     * Returns deque instance by name.
     *
     * @param <V> type of value
     * @param name 对象名称
     * @return Deque object
     */
    <V> RDequeAsync<V> getDeque(String name);

    <V> RDequeAsync<V> getDeque(String name, Codec codec);

    /**
     * Returns blocking deque instance by name.
     * 
     * @param <V> type of value
     * @param name 对象名称
     * @return BlockingDeque object
     */
    <V> RBlockingDequeAsync<V> getBlockingDeque(String name);

    <V> RBlockingDequeAsync<V> getBlockingDeque(String name, Codec codec);

    /**
     * 按名称返回 {@link RAtomicLong} 批处理对象。
     *
     * @param name 对象名称
     * @return AtomicLong object
     */
    RAtomicLongAsync getAtomicLong(String name);

    /**
     * 按名称返回 {@link RAtomicDouble} 批处理对象。
     *
     * @param name 对象名称
     * @return AtomicDouble object
     */
    RAtomicDoubleAsync getAtomicDouble(String name);

    /**
     * 按名称返回 Redis 有序集合批处理对象
     * 
     * @param <V> type of value
     * @param name 对象名称
     * @return ScoredSortedSet object
     */
    <V> RScoredSortedSetAsync<V> getScoredSortedSet(String name);

    <V> RScoredSortedSetAsync<V> getScoredSortedSet(String name, Codec codec);

    /**
     * 按名称返回基于字符串的有序集合批处理对象
     * 添加时所有元素使用相同分数插入，
     * in order to force lexicographical ordering
     *
     * @param name 对象名称
     * @return LexSortedSet object
     */
    RLexSortedSetAsync getLexSortedSet(String name);

    /**
     * Returns bitSet instance by name.
     *
     * @param name 对象名称
     * @return BitSet object
     */
    RBitSetAsync getBitSet(String name);

    /**
     * Returns script operations object
     *
     * @return Script object
     */
    RScriptAsync getScript();

    /**
     * Returns script operations object using provided codec.
     * 
     * @param codec - codec for params and result
     * @return Script object
     */
    RScriptAsync getScript(Codec codec);

    /**
     * Returns interface for Redis Function feature
     *
     * @return function object
     */
    RFunctionAsync getFunction();

    /**
     * Returns interface for Redis Function feature using provided codec
     *
     * @param codec - codec for params and result
     * @return function interface
     */
    RFunctionAsync getFunction(Codec codec);

    /**
     * Returns keys operations.
     * 每个 Redis/Redisson 对象对应独立键
     *
     * @return Keys object
     */
    RKeysAsync getKeys();

    /**
     * 返回 RediSearch 模块 API 批处理对象
     *
     * @return RSearchAsync object
     */
    RSearchAsync getSearch();

    /**
     * 使用指定属性值 codec 返回 RediSearch API 批处理对象。
     *
     * @param codec codec for entry
     * @return RSearchAsync object
     */
    RSearchAsync getSearch(Codec codec);

    /**
     * 执行批处理队列中累积的全部命令。
     * <p>
     * 集群模式下按 slot 分组执行。
     * and may be executed on different servers. Thus command execution order could be changed
     *
     * @return List with result object for each command
     * @throws RedisException in case of any error
     *
     */
    BatchResult<?> execute() throws RedisException;

    /**
     * 异步执行批处理队列中累积的全部命令。
     * <p>
     * 集群配置下按 slot 分组。
     * so may be executed on different servers. Thus command execution order could be changed
     *
     * @return List with result object for each command
     */
    RFuture<BatchResult<?>> executeAsync();

    /**
     * 丢弃批处理命令并释放参数编码缓冲区。
     */
    void discard();

    /**
     * 丢弃批处理命令并释放参数编码缓冲区。
     *
     * @return 无返回值
     */
    RFuture<Void> discardAsync();


}
