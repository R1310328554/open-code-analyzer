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

/**
 * 环形缓冲区队列的异步 API；容量满时从队首淘汰最旧元素。
 * <p>
 * 队列已满时再添加元素会移除队首元素。
 * <p>
 * 使用前须先通过 {@link #trySetCapacityAsync(int)} 初始化容量。
 * 
 * @author Nikita Koksharov
 *
 * @param <V> 元素类型
 */
public interface RRingBufferAsync<V> extends RQueueAsync<V> {

    /**
     * 仅当容量尚未设置时异步设置队列容量。
     *
     * @param capacity - queue capacity
     * @return 设置成功则为 <code>true</code>，容量已存在则为 <code>false</code>
     */
    RFuture<Boolean> trySetCapacityAsync(int capacity);

    /**
     * Sets capacity of the queue and overrides current value.
     * Trims queue if previous capacity value was greater than new.
     *
     * @param capacity - queue capacity
     */
    RFuture<Void> setCapacityAsync(int capacity);

    /**
     * Returns remaining capacity of this queue
     * 
     * @return remaining capacity
     */
    RFuture<Integer> remainingCapacityAsync();
    
    /**
     * Returns capacity of this queue
     * 
     * @return queue capacity
     */
    RFuture<Integer> capacityAsync();

    /**
     * Returns the newest (most recently added) elements of this buffer.
     * At most <code>count</code> elements are returned, ordered from oldest
     * to newest (same order as {@link #readAllAsync()}). Doesn't remove elements.
     * <p>
     * If <code>count</code> is greater than the current size, all elements are returned.
     *
     * @param count - maximum number of elements to return
     * @return list of the newest elements, or an empty list if this buffer is
     *         empty or <code>count</code> is non-positive
     */
    RFuture<List<V>> readNewestAsync(int count);

    /**
     * Returns the oldest elements of this buffer.
     * At most <code>count</code> elements are returned, ordered from oldest
     * to newest (same order as {@link #readAllAsync()}). Doesn't remove elements.
     * <p>
     * If <code>count</code> is greater than the current size, all elements are returned.
     *
     * @param count - maximum number of elements to return
     * @return list of the oldest elements, or an empty list if this buffer is
     *         empty or <code>count</code> is non-positive
     */
    RFuture<List<V>> readOldestAsync(int count);

    /**
     * Retrieves, but doesn't remove, the newest (most recently added) element of this buffer,
     * or returns <code>null</code> if this buffer is empty.
     *
     * @return the newest element, or <code>null</code> if this buffer is empty
     */
    /** 异步查看缓冲区最新元素（不移除）。 */
    RFuture<V> peekLastAsync();
    
}
