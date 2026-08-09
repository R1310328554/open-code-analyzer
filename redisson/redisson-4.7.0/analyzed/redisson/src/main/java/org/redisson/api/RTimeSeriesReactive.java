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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link RTimeSeries} Reactor 响应式 API。
 *
 * @author Nikita Koksharov
 * @param <V> 值类型
 * @param <L> 标签/元数据类型
 */
public interface RTimeSeriesReactive<V, L> extends RExpirableReactive {

    /**
     * 返回集合元素的响应式迭代流。
     *
     * @return iterator
     */
    Flux<V> iterator();

    /**
     * 添加element to this time-series collection。
     * by specified <code>timestamp</code>.
     *
     * @param timestamp 时间戳
     * @param object 对象本身
     * @return 无返回值
     */
    Mono<Void> add(long timestamp, V object);

    /**
     * 添加element with <code>label</code> to this time-series collection。
     * by specified <code>timestamp</code>.
     *
     * @param timestamp 时间戳
     * @param object 对象本身
     * @param label 对象标签
     * @return 无返回值
     */
    Mono<Void> add(long timestamp, V object, L label);

    /**
     * 添加all elements contained in the specified map to this time-series collection.。
     * Map contains of timestamp mapped by object.
     *
     * @param objects - map of elements to add
     * @return 无返回值
     */
    Mono<Void> addAll(Map<Long, V> objects);

    /**
     * 添加all entries collection to this time-series collection.。
     *
     * @param entries collection of time series entries
     * @return 无返回值
     */
    Mono<Void> addAll(Collection<TimeSeriesEntry<V, L>> entries);

    /**
     * 请改用 {@link #add(long, Object, Duration)}。（Redisson API）。
     *
     * @param timestamp - object timestamp
     * @param object - object itself
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 无返回值
     */
    @Deprecated
    Mono<Void> add(long timestamp, V object, long timeToLive, TimeUnit timeUnit);

    /**
     * 添加element to this time-series collection。
     * by specified <code>timestamp</code>.
     *
     * @param timestamp 时间戳
     * @param object 对象本身
     * @param timeToLive time to live interval
     */
    Mono<Void> add(long timestamp, V object, Duration timeToLive);

    /**
     * 添加element with <code>label</code> to this time-series collection。
     * by specified <code>timestamp</code>.
     *
     * @param timestamp 时间戳
     * @param object 对象本身
     * @param label 对象标签
     * @param timeToLive time to live interval
     * @return 无返回值
     */
    Mono<Void> add(long timestamp, V object, L label, Duration timeToLive);

    /**
     * 请改用 {@link #addAll(Map, Duration)}。（Redisson API）。
     *
     * @param objects - map of elements to add
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 无返回值
     */
    @Deprecated
    Mono<Void> addAll(Map<Long, V> objects, long timeToLive, TimeUnit timeUnit);

    /**
     * 添加all elements contained in the specified map to this time-series collection.。
     * Map contains of timestamp mapped by object.
     *
     * @param objects map of elements to add
     * @param timeToLive time to live interval
     */
    Mono<Void> addAll(Map<Long, V> objects, Duration timeToLive);

    /**
     * 添加all time series entries collection to this time-series collection.。
     * Specified time to live interval applied to all entries defined in collection.
     *
     * @param entries collection of time series entries
     * @param timeToLive time to live interval
     * @return 无返回值
     */
    Mono<Void> addAll(Collection<TimeSeriesEntry<V, L>> entries, Duration timeToLive);

    /**
     * 返回size of this set.。
     *
     * @return size
     */
    Mono<Integer> size();

    /**
     * 返回object by specified <code>timestamp</code> or <code>null</code> if it doesn't exist.。
     *
     * @param timestamp - object timestamp
     * @return object
     */
    Mono<V> get(long timestamp);

    /**
     * 返回time series entry by specified <code>timestamp</code> or <code>null</code> if it doesn't exist.。
     *
     * @param timestamp 时间戳
     * @return time series entry
     */
    Mono<TimeSeriesEntry<V, L>> getEntry(long timestamp);

    /**
     * 移除object by specified <code>timestamp</code>.。
     *
     * @param timestamp - object timestamp
     * @return <code>true</code> if an element was removed as a result of this call
     */
    Mono<Boolean> remove(long timestamp);

    /**
     * 移除and returns object by specified <code>timestamp</code>.。
     *
     * @param timestamp - object timestamp
     * @return object or <code>null</code> if it doesn't exist
     */
    Mono<V> getAndRemove(long timestamp);

    /**
     * 移除and returns entry by specified <code>timestamp</code>.。
     *
     * @param timestamp - object timestamp
     * @return entry or <code>null</code> if it doesn't exist
     */
    Mono<TimeSeriesEntry<V, L>> getAndRemoveEntry(long timestamp);

    /**
     * 移除and returns the head elements or {@code null} if this time-series collection is empty.。
     *
     * @param count - elements amount
     * @return the head element,
     *         or {@code null} if this time-series collection is empty
     */
    Mono<Collection<V>> pollFirst(int count);

    /**
     * 移除and returns head entries。
     *
     * @param count - entries amount
     * @return collection of head entries
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> pollFirstEntries(int count);

    /**
     * 移除and returns the tail elements or {@code null} if this time-series collection is empty.。
     *
     * @param count - elements amount
     * @return the tail element or {@code null} if this time-series collection is empty
     */
    Mono<Collection<V>> pollLast(int count);

    /**
     * 移除and returns tail entries。
     *
     * @param count - entries amount
     * @return collection of tail entries
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> pollLastEntries(int count);

    /**
     * 移除and returns the head element or {@code null} if this time-series collection is empty.。
     *
     * @return the head element,
     *         or {@code null} if this time-series collection is empty
     */
    Mono<V> pollFirst();

    /**
     * 移除and returns head entry or {@code null} if this time-series collection is empty.。
     *
     * @return the head entry,
     *         or {@code null} if this time-series collection is empty
     */
    Mono<TimeSeriesEntry<V, L>> pollFirstEntry();

    /**
     * 移除and returns the tail element or {@code null} if this time-series collection is empty.。
     *
     * @return the tail element or {@code null} if this time-series collection is empty
     */
    Mono<V> pollLast();

    /**
     * 移除and returns the tail entry or {@code null} if this time-series collection is empty.。
     *
     * @return the tail entry or {@code null} if this time-series collection is empty
     */
    Mono<TimeSeriesEntry<V, L>> pollLastEntry();

    /**
     * 返回the tail element or {@code null} if this time-series collection is empty.。
     *
     * @return the tail element or {@code null} if this time-series collection is empty
     */
    Mono<V> last();

    /**
     * 返回the tail entry or {@code null} if this time-series collection is empty.。
     *
     * @return the tail entry or {@code null} if this time-series collection is empty
     */
    Mono<TimeSeriesEntry<V, L>> lastEntry();

    /**
     * 返回the head element or {@code null} if this time-series collection is empty.。
     *
     * @return the head element or {@code null} if this time-series collection is empty
     */
    Mono<V> first();

    /**
     * 返回the head entry or {@code null} if this time-series collection is empty.。
     *
     * @return the head entry or {@code null} if this time-series collection is empty
     */
    Mono<TimeSeriesEntry<V, L>> firstEntry();

    /**
     * 返回timestamp of the head timestamp or {@code null} if this time-series collection is empty.。
     *
     * @return timestamp or {@code null} if this time-series collection is empty
     */
    Mono<Long> firstTimestamp();

    /**
     * 返回timestamp of the tail element or {@code null} if this time-series collection is empty.。
     *
     * @return timestamp or {@code null} if this time-series collection is empty
     */
    Mono<Long> lastTimestamp();

    /**
     * 返回the tail elements of this time-series collection.。
     *
     * @param count - elements amount
     * @return the tail elements
     */
    Mono<Collection<V>> last(int count);

    /**
     * 返回the tail entries of this time-series collection.。
     *
     * @param count - entries amount
     * @return the tail entries
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> lastEntries(int count);

    /**
     * 返回the head elements of this time-series collection.。
     *
     * @param count - elements amount
     * @return the head elements
     */
    Mono<Collection<V>> first(int count);

    /**
     * 返回the head entries of this time-series collection.。
     *
     * @param count - entries amount
     * @return the head entries
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> firstEntries(int count);

    /**
     * 移除values within timestamp range. Including boundary values.。
     *
     * @param startTimestamp 起始时间戳
     * @param endTimestamp 结束时间戳
     * @return number of removed elements
     */
    Mono<Integer> removeRange(long startTimestamp, long endTimestamp);

    /**
     * 返回ordered elements of this time-series collection within timestamp range. Including boundary values.。
     *
     * @param startTimestamp 起始时间戳
     * @param endTimestamp 结束时间戳
     * @return elements collection
     */
    Mono<Collection<V>> range(long startTimestamp, long endTimestamp);

    /**
     * 返回ordered elements of this time-series collection within timestamp range. Including boundary values.。
     *
     * @param startTimestamp start timestamp
     * @param endTimestamp end timestamp
     * @param limit result size limit
     * @return elements collection
     */
    Mono<Collection<V>> range(long startTimestamp, long endTimestamp, int limit);

    /**
     * 返回elements of this time-series collection in reverse order within timestamp range. Including boundary values.。
     *
     * @param startTimestamp 起始时间戳
     * @param endTimestamp 结束时间戳
     * @return elements collection
     */
    Mono<Collection<V>> rangeReversed(long startTimestamp, long endTimestamp);

    /**
     * 返回elements of this time-series collection in reverse order within timestamp range. Including boundary values.。
     *
     * @param startTimestamp start timestamp
     * @param endTimestamp end timestamp
     * @param limit result size limit
     * @return elements collection
     */
    Mono<Collection<V>> rangeReversed(long startTimestamp, long endTimestamp, int limit);

    /**
     * 返回ordered entries of this time-series collection within timestamp range. Including boundary values.。
     *
     * @param startTimestamp 起始时间戳
     * @param endTimestamp 结束时间戳
     * @return elements collection
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> entryRange(long startTimestamp, long endTimestamp);

    /**
     * 返回ordered entries of this time-series collection within timestamp range. Including boundary values.。
     *
     * @param startTimestamp start timestamp
     * @param endTimestamp end timestamp
     * @param limit result size limit
     * @return elements collection
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> entryRange(long startTimestamp, long endTimestamp, int limit);

    /**
     * 返回entries of this time-series collection in reverse order within timestamp range. Including boundary values.。
     *
     * @param startTimestamp 起始时间戳
     * @param endTimestamp 结束时间戳
     * @return elements collection
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> entryRangeReversed(long startTimestamp, long endTimestamp);

    /**
     * 返回entries of this time-series collection in reverse order within timestamp range. Including boundary values.。
     *
     * @param startTimestamp start timestamp
     * @param endTimestamp end timestamp
     * @param limit result size limit
     * @return elements collection
     */
    Mono<Collection<TimeSeriesEntry<V, L>>> entryRangeReversed(long startTimestamp, long endTimestamp, int limit);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.ScoredSortedSetAddListener
     * @see org.redisson.api.listener.ScoredSortedSetRemoveListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    @Override
    Mono<Integer> addListener(ObjectListener listener);


}
