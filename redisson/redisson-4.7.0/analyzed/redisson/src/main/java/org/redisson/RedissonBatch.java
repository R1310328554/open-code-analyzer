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
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;
import org.redisson.eviction.EvictionScheduler;

/**
 * {@link RBatch} 实现：将多条 Redis 命令聚合为单次网络往返（管道/批处理）。
 * <p>通过 {@link CommandBatchService} 延迟执行并在 {@link #execute()} 时一次性提交。
 *
 * @author Nikita Koksharov
 */
public class RedissonBatch implements RBatch {

    /** 淘汰调度器（MapCache 等对象使用）。 */
    private final EvictionScheduler evictionScheduler;
    /** 批处理命令执行服务。 */
    private final CommandBatchService executorService;

    public RedissonBatch(EvictionScheduler evictionScheduler, CommandAsyncExecutor executor, BatchOptions options) {
        this.executorService = executor.createCommandBatchService(options);
        this.evictionScheduler = evictionScheduler;
    }

    /** 获取 Array。 */
    @Override
    public <V> RArrayAsync<V> getArray(String name) {
        return new RedissonArray<V>(executorService, name);
    }

    /** 获取 Array。 */
    @Override
    public <V> RArrayAsync<V> getArray(String name, Codec codec) {
        return new RedissonArray<V>(codec, executorService, name);
    }

    /** 获取 Bucket。 */
    @Override
    public <V> RBucketAsync<V> getBucket(String name) {
        return new RedissonBucket<V>(executorService, name);
    }

    /** 获取 Bucket。 */
    @Override
    public <V> RBucketAsync<V> getBucket(String name, Codec codec) {
        return new RedissonBucket<V>(codec, executorService, name);
    }

    /** 获取 JsonBucket。 */
    @Override
    public <V> RJsonBucketAsync<V> getJsonBucket(String name, JsonCodec codec) {
        return new RedissonJsonBucket<>(codec, executorService, name);
    }

    /** 获取 HyperLogLog。 */
    @Override
    public <V> RHyperLogLogAsync<V> getHyperLogLog(String name) {
        return new RedissonHyperLogLog<V>(executorService, name);
    }

    /** 获取 HyperLogLog。 */
    @Override
    public <V> RHyperLogLogAsync<V> getHyperLogLog(String name, Codec codec) {
        return new RedissonHyperLogLog<V>(codec, executorService, name);
    }

    /** 获取 List。 */
    @Override
    public <V> RListAsync<V> getList(String name) {
        return new RedissonList<V>(executorService, name, null);
    }

    /** 获取 List。 */
    @Override
    public <V> RListAsync<V> getList(String name, Codec codec) {
        return new RedissonList<V>(codec, executorService, name, null);
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    @Override
    public <K, V> RMapAsync<K, V> getMap(String name) {
        return new RedissonMap<K, V>(executorService, name, null, null, null);
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    @Override
    public <K, V> RMapAsync<K, V> getMap(String name, Codec codec) {
        return new RedissonMap<K, V>(codec, executorService, name, null, null, null);
    }

    /** 获取 Set。 */
    @Override
    public <V> RSetAsync<V> getSet(String name) {
        return new RedissonSet<V>(executorService, name, null);
    }

    /** 获取 Set。 */
    @Override
    public <V> RSetAsync<V> getSet(String name, Codec codec) {
        return new RedissonSet<V>(codec, executorService, name, null);
    }

    /** 返回 Session 集群同步 Topic。 */
    @Override
    public RTopicAsync getTopic(String name) {
        return new RedissonTopic(executorService, name);
    }

    /** 返回 Session 集群同步 Topic。 */
    @Override
    public RTopicAsync getTopic(String name, Codec codec) {
        return new RedissonTopic(codec, executorService, name);
    }

    /** 获取 ShardedTopic。 */
    @Override
    public RShardedTopicAsync getShardedTopic(String name) {
        return new RedissonShardedTopic(executorService, name);
    }

    /** 获取 ShardedTopic。 */
    @Override
    public RShardedTopicAsync getShardedTopic(String name, Codec codec) {
        return new RedissonShardedTopic(codec, executorService, name);
    }

    /** 获取 Queue。 */
    @Override
    public <V> RQueueAsync<V> getQueue(String name) {
        return new RedissonQueue<V>(executorService, name, null);
    }

    /** 获取 Queue。 */
    @Override
    public <V> RQueueAsync<V> getQueue(String name, Codec codec) {
        return new RedissonQueue<V>(codec, executorService, name, null);
    }

    /** 获取 BlockingQueue。 */
    @Override
    public <V> RBlockingQueueAsync<V> getBlockingQueue(String name) {
        return new RedissonBlockingQueue<V>(executorService, name, null);
    }

    /** 获取 BlockingQueue。 */
    @Override
    public <V> RBlockingQueueAsync<V> getBlockingQueue(String name, Codec codec) {
        return new RedissonBlockingQueue<V>(codec, executorService, name, null);
    }

    /** 获取 BlockingDeque。 */
    @Override
    public <V> RBlockingDequeAsync<V> getBlockingDeque(String name) {
        return new RedissonBlockingDeque<V>(executorService, name, null);
    }

    /** 获取 BlockingDeque。 */
    @Override
    public <V> RBlockingDequeAsync<V> getBlockingDeque(String name, Codec codec) {
        return new RedissonBlockingDeque<V>(codec, executorService, name, null);
    }

    /** 获取 Deque。 */
    @Override
    public <V> RDequeAsync<V> getDeque(String name) {
        return new RedissonDeque<V>(executorService, name, null);
    }

    /** 获取 Deque。 */
    @Override
    public <V> RDequeAsync<V> getDeque(String name, Codec codec) {
        return new RedissonDeque<V>(codec, executorService, name, null);
    }

    /** 获取 AtomicLong。 */
    @Override
    public RAtomicLongAsync getAtomicLong(String name) {
        return new RedissonAtomicLong(executorService, name);
    }

    /** 获取 AtomicDouble。 */
    @Override
    public RAtomicDoubleAsync getAtomicDouble(String name) {
        return new RedissonAtomicDouble(executorService, name);
    }

    /** 获取 ScoredSortedSet。 */
    @Override
    public <V> RScoredSortedSetAsync<V> getScoredSortedSet(String name) {
        return new RedissonScoredSortedSet<V>(executorService, name, null);
    }

    /** 获取 ScoredSortedSet。 */
    @Override
    public <V> RScoredSortedSetAsync<V> getScoredSortedSet(String name, Codec codec) {
        return new RedissonScoredSortedSet<V>(codec, executorService, name, null);
    }

    /** 获取 LexSortedSet。 */
    @Override
    public RLexSortedSetAsync getLexSortedSet(String name) {
        return new RedissonLexSortedSet(executorService, name, null);
    }

    /** 获取 BitSet。 */
    @Override
    public RBitSetAsync getBitSet(String name) {
        return new RedissonBitSet(executorService, name);
    }

    /** 获取 MapCache。 */
    @Override
    public <K, V> RMapCacheAsync<K, V> getMapCache(String name, Codec codec) {
        return new RedissonMapCache<K, V>(codec, evictionScheduler, executorService, name, null, null, null);
    }

    /** 获取 MapCache。 */
    @Override
    public <K, V> RMapCacheAsync<K, V> getMapCache(String name) {
        return new RedissonMapCache<K, V>(evictionScheduler, executorService, name, null, null, null);
    }

    /** 获取 MapCacheNative。 */
    @Override
    public <K, V> RMapCacheNativeAsync<K, V> getMapCacheNative(String name) {
        return new RedissonMapCacheNative<>(executorService, name, null, null, null);
    }

    /** 获取 MapCacheNative。 */
    @Override
    public <K, V> RMapCacheNativeAsync<K, V> getMapCacheNative(String name, Codec codec) {
        return new RedissonMapCacheNative<>(codec, executorService, name, null, null, null);
    }

    /** 获取 Script。 */
    @Override
    public RScriptAsync getScript() {
        return new RedissonScript(executorService);
    }
    
    /** 获取 Script。 */
    @Override
    public RScriptAsync getScript(Codec codec) {
        return new RedissonScript(executorService, codec);
    }

    /** 获取 Function。 */
    @Override
    public RFunctionAsync getFunction() {
        return new RedissonFuction(executorService);
    }

    /** 获取 Function。 */
    @Override
    public RFunctionAsync getFunction(Codec codec) {
        return new RedissonFuction(executorService, codec);
    }

    /** 返回全部键（慎用）。 */
    @Override
    public RKeysAsync getKeys() {
        return new RedissonKeys(executorService);
    }

    /** 获取 Search。 */
    @Override
    public RSearchAsync getSearch() {
        return new RedissonSearch(executorService);
    }

    /** 获取 Search。 */
    @Override
    public RSearchAsync getSearch(Codec codec) {
        return new RedissonSearch(codec, executorService);
    }

    /** 获取 SetCache。 */
    @Override
    public <V> RSetCacheAsync<V> getSetCache(String name) {
        return new RedissonSetCache<V>(evictionScheduler, executorService, name, null);
    }

    /** 获取 SetCache。 */
    @Override
    public <V> RSetCacheAsync<V> getSetCache(String name, Codec codec) {
        return new RedissonSetCache<V>(codec, evictionScheduler, executorService, name, null);
    }

    /** 提交批处理并返回各命令结果。 */
    @Override
    public BatchResult<?> execute() {
        return executorService.execute();
    }

    /** 异步提交批处理命令。 */
    @Override
    public RFuture<BatchResult<?>> executeAsync() {
        return executorService.executeAsync();
    }

    /** 丢弃批处理队列中的命令。 */
    @Override
    public void discard() {
        executorService.discard();
    }

    /** 异步执行 discard。 */
    @Override
    public RFuture<Void> discardAsync() {
        return executorService.discardAsync();
    }

    /** 获取 SetMultimap。 */
    @Override
    public <K, V> RMultimapAsync<K, V> getSetMultimap(String name) {
        return new RedissonSetMultimap<K, V>(executorService, name);
    }

    /** 获取 SetMultimap。 */
    @Override
    public <K, V> RMultimapAsync<K, V> getSetMultimap(String name, Codec codec) {
        return new RedissonSetMultimap<K, V>(codec, executorService, name);
    }

    /** 获取 ListMultimap。 */
    @Override
    public <K, V> RMultimapAsync<K, V> getListMultimap(String name) {
        return new RedissonListMultimap<K, V>(executorService, name);
    }

    /** 获取 ListMultimap。 */
    @Override
    public <K, V> RMultimapAsync<K, V> getListMultimap(String name, Codec codec) {
        return new RedissonListMultimap<K, V>(codec, executorService, name);
    }

    /** 获取 Geo。 */
    @Override
    public <V> RGeoAsync<V> getGeo(String name) {
        return new RedissonGeo<V>(executorService, name, null);
    }
    
    /** 获取 Geo。 */
    @Override
    public <V> RGeoAsync<V> getGeo(String name, Codec codec) {
        return new RedissonGeo<V>(codec, executorService, name, null);
    }
    
    /** 获取 SetMultimapCache。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getSetMultimapCache(String name) {
        return new RedissonSetMultimapCache<K, V>(evictionScheduler, executorService, name);
    }
    
    /** 获取 SetMultimapCache。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getSetMultimapCache(String name, Codec codec) {
        return new RedissonSetMultimapCache<K, V>(evictionScheduler, codec, executorService, name);
    }

    /** 获取 ListMultimapCache。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getListMultimapCache(String name) {
        return new RedissonListMultimapCache<>(evictionScheduler, executorService, name);
    }
    
    /** 获取 ListMultimapCache。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getListMultimapCache(String name, Codec codec) {
        return new RedissonListMultimapCache<>(evictionScheduler, codec, executorService, name);
    }

    /** 获取 ListMultimapCacheNative。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getListMultimapCacheNative(String name) {
        return new RedissonListMultimapCacheNative<>(executorService, name);
    }

    /** 获取 ListMultimapCacheNative。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getListMultimapCacheNative(String name, Codec codec) {
        return new RedissonListMultimapCacheNative<>(codec, executorService, name);
    }

    /** 获取 SetMultimapCacheNative。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getSetMultimapCacheNative(String name) {
        return new RedissonSetMultimapCacheNative<>(executorService, name);
    }

    /** 获取 SetMultimapCacheNative。 */
    @Override
    public <K, V> RMultimapCacheAsync<K, V> getSetMultimapCacheNative(String name, Codec codec) {
        return new RedissonSetMultimapCacheNative<>(codec, executorService, name);
    }

    /** 获取 Stream。 */
    @Override
    public <K, V> RStreamAsync<K, V> getStream(String name) {
        return new RedissonStream<>(executorService, name);
    }

    /** 获取 Stream。 */
    @Override
    public <K, V> RStreamAsync<K, V> getStream(String name, Codec codec) {  
        return new RedissonStream<>(codec, executorService, name);
    }

    /** 获取 BloomFilterNative。 */
    @Override
    public <T> RBloomFilterNativeAsync<T> getBloomFilterNative(String name) {
        return new RedissonBloomFilterNative<>(executorService, name);
    }

    /** 获取 BloomFilterNative。 */
    @Override
    public <T> RBloomFilterNativeAsync<T> getBloomFilterNative(String name, Codec codec) {
        return new RedissonBloomFilterNative<>(codec, executorService, name);
    }

    /** 获取 CuckooFilter。 */
    @Override
    public <V> RCuckooFilterAsync<V> getCuckooFilter(String name) {
        return getCuckooFilter(name, null);
    }

    /** 获取 CuckooFilter。 */
    @Override
    public <V> RCuckooFilterAsync<V> getCuckooFilter(String name, Codec codec) {
        return new RedissonCuckooFilter<V>(codec, executorService, name);
    }

    /** 获取 TDigest。 */
    @Override
    public RTDigestAsync getTDigest(String name) {
        return new RedissonTDigest(executorService, name);
    }

    /** 获取 TopK。 */
    @Override
    public <V> RTopKAsync<V> getTopK(String name) {
        return getTopK(name, null);
    }

    /** 获取 TopK。 */
    @Override
    public <V> RTopKAsync<V> getTopK(String name, Codec codec) {
        return new RedissonTopK<V>(codec, executorService, name);
    }

    /** 获取 VectorSet。 */
    @Override
    public RVectorSetAsync getVectorSet(String name) {
        return new RedissonVectorSet(executorService, name);
    }


}
