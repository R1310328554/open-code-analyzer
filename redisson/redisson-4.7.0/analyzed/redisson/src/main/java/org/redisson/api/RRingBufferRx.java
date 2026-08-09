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

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 环形缓冲区队列的 RxJava3 API；容量满时从队首淘汰最旧元素。
 * <p>
 * 队列已满时再添加元素会移除队首元素。
 * <p>
 * 使用前须先通过 {@link #trySetCapacity(int)} 初始化容量。
 * 
 * @author Nikita Koksharov
 *
 * @param <V> 元素类型
 */
public interface RRingBufferRx<V> extends RQueueRx<V> {

    /**
     * 仅当容量尚未设置时设置队列容量。
     *
     * @param capacity - queue capacity
     * @return 设置成功则为 <code>true</code>，容量已存在则为 <code>false</code>
     */
    Single<Boolean> trySetCapacity(int capacity);

    /**
     * Sets capacity of the queue and overrides current value.
     * Trims queue if previous capacity value was greater than new.
     *
     * @param capacity - queue capacity
     */
    Completable setCapacity(int capacity);

    /**
     * Returns remaining capacity of this queue
     * 
     * @return remaining capacity
     */
    Single<Integer> remainingCapacity();
    
    /**
     * Returns capacity of this queue
     * 
     * @return queue capacity
     */
    Single<Integer> capacity();

    /**
     * Returns the newest (most recently added) elements of this buffer.
     * At most <code>count</code> elements are returned, ordered from oldest
     * to newest (same order as {@link #readAll()}). Doesn't remove elements.
     * <p>
     * If <code>count</code> is greater than the current size, all elements are returned.
     *
     * @param count - maximum number of elements to return
     * @return list of the newest elements, or an empty list if this buffer is
     *         empty or <code>count</code> is non-positive
     */
    Single<List<V>> readNewest(int count);

    /**
     * Returns the oldest elements of this buffer.
     * At most <code>count</code> elements are returned, ordered from oldest
     * to newest (same order as {@link #readAll()}). Doesn't remove elements.
     * <p>
     * If <code>count</code> is greater than the current size, all elements are returned.
     *
     * @param count - maximum number of elements to return
     * @return list of the oldest elements, or an empty list if this buffer is
     *         empty or <code>count</code> is non-positive
     */
    Single<List<V>> readOldest(int count);

    /**
     * Retrieves, but doesn't remove, the newest (most recently added) element of this buffer,
     * or completes empty if this buffer is empty.
     *
     * @return the newest element, or empty if this buffer is empty
     */
    /** RxJava 查看缓冲区最新元素（不移除）。 */
    Maybe<V> peekLast();
    
}
