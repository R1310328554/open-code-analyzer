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

import org.redisson.client.codec.Codec;

/**
 * 客户端侧读缓存（Client-side caching）API。
 * <p>在 Redis 6 跟踪模式下本地缓存热点对象，减少网络往返。
 *
 * @author Nikita Koksharov
 */
public interface RClientSideCaching extends RDestroyable {

    /**
     * 按名称获取 {@link RBucket} 实例（客户端侧缓存）。
     *
     * @param <V> type of value
     * @param name 名称
     * @return Bucket object
     */
    <V> RBucket<V> getBucket(String name);

    /**
     * Returns object holder instance by name
     * using provided codec for object.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for values
     * @return Bucket object
     */
    <V> RBucket<V> getBucket(String name, Codec codec);

    /**
     * Returns stream instance by <code>name</code>
     * <p>
     * Requires <b>Redis 5.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name of stream
     * @return RStream object
     */
    <K, V> RStream<K, V> getStream(String name);

    /**
     * Returns stream instance by <code>name</code>
     * using provided <code>codec</code> for entries.
     * <p>
     * Requires <b>Redis 5.0.0 and higher.</b>
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of stream
     * @param codec codec for entry
     * @return RStream object
     */
    <K, V> RStream<K, V> getStream(String name, Codec codec);

    /**
     * 按名称获取 {@link RSet} 实例。
     *
     * @param <V> type of value
     * @param name 名称
     * @return Set object
     */
    <V> RSet<V> getSet(String name);

    /**
     * Returns set instance by name
     * using provided codec for set objects.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for values
     * @return Set object
     */
    <V> RSet<V> getSet(String name, Codec codec);

    /**
     * Returns map instance by name.
     * <p>
     * <strong>
     * NOTE: client side caching feature invalidates whole Map per entry change which is ineffective.
     * Use local cached <a href="https://redisson.org/docs/data-and-services/collections/#eviction-local-cache-and-data-partitioning">Map</a>, <a href="https://redisson.org/docs/data-and-services/collections/#local-cache">JSON Store</a> instead.
     * </strong>
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @return Map object
     */
    <K, V> RMap<K, V> getMap(String name);

    /**
     * Returns map instance by name
     * using provided codec for both map keys and values.
     * <p>
     * <strong>
     * NOTE: client side caching feature invalidates whole Map per entry change which is ineffective.
     * Use local cached <a href="https://redisson.org/docs/data-and-services/collections/#eviction-local-cache-and-data-partitioning">Map</a>, <a href="https://redisson.org/docs/data-and-services/collections/#local-cache">JSON Store</a> instead.
     * </strong>
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for keys and values
     * @return Map object
     */
    <K, V> RMap<K, V> getMap(String name, Codec codec);

    /**
     * Returns Redis Sorted Set instance by name.
     * This sorted set sorts objects by object score.
     *
     * @param <V> type of value
     * @param name name of object
     * @return ScoredSortedSet object
     */
    <V> RScoredSortedSet<V> getScoredSortedSet(String name);

    /**
     * Returns Redis Sorted Set instance by name
     * using provided codec for sorted set objects.
     * This sorted set sorts objects by object score.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for values
     * @return ScoredSortedSet object
     */
    <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec);

    /**
     * 按名称获取 {@link RList} 实例。
     *
     * @param <V> type of value
     * @param name 名称
     * @return List object
     */
    <V> RList<V> getList(String name);

    /**
     * Returns list instance by name
     * using provided codec for list objects.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for values
     * @return List object
     */
    <V> RList<V> getList(String name, Codec codec);

    /**
     * 按名称获取无界 {@link RQueue} 实例。
     *
     * @param <V> type of value
     * @param name 名称
     * @return queue object
     */
    <V> RQueue<V> getQueue(String name);

    /**
     * Returns unbounded queue instance by name
     * using provided codec for queue objects.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for message
     * @return Queue object
     */
    <V> RQueue<V> getQueue(String name, Codec codec);

    /**
     * 按名称获取无界 {@link RDeque} 实例。
     *
     * @param <V> type of value
     * @param name 名称
     * @return Deque object
     */
    <V> RDeque<V> getDeque(String name);

    /**
     * Returns unbounded deque instance by name
     * using provided codec for deque objects.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for values
     * @return Deque object
     */
    <V> RDeque<V> getDeque(String name, Codec codec);

    /**
     * 按名称获取无界 {@link RBlockingQueue} 实例。
     *
     * @param <V> type of value
     * @param name 名称
     * @return BlockingQueue object
     */
    <V> RBlockingQueue<V> getBlockingQueue(String name);

    /**
     * Returns unbounded blocking queue instance by name
     * using provided codec for queue objects.
     *
     * @param <V> type of value
     * @param name name of queue
     * @param codec queue objects codec
     * @return BlockingQueue object
     */
    <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec);

    /**
     * 按名称获取无界 {@link RBlockingDeque} 实例。
     *
     * @param <V> type of value
     * @param name 名称
     * @return BlockingDeque object
     */
    <V> RBlockingDeque<V> getBlockingDeque(String name);

    /**
     * Returns unbounded blocking deque instance by name
     * using provided codec for deque objects.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec deque objects codec
     * @return BlockingDeque object
     */
    <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec);

    /**
     * 按名称获取 {@link RGeo} 地理空间容器实例。
     *
     * @param <V> type of value
     * @param name 名称
     * @return Geo object
     */
    <V> RGeo<V> getGeo(String name);

    /**
     * Returns geospatial items holder instance by <code>name</code>
     * using provided codec for geospatial members.
     *
     * @param <V> type of value
     * @param name name of object
     * @param codec codec for value
     * @return Geo object
     */
    <V> RGeo<V> getGeo(String name, Codec codec);

}
