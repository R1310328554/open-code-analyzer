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

import java.util.Collection;
import java.util.List;

/**
 * Fixed-capacity circular (ring) buffer backed by the Redis array type.
 * <p>
 * New writes are appended to the tail and wrap around with modular arithmetic,
 * overwriting the oldest values once the buffer is full. Each value is addressed
 * by the ring slot it was written into; evicting the oldest value is simply the
 * next write reusing its slot, so surviving values are never renumbered and an
 * absolute index keeps referring to the same value until that slot is overwritten.
 * <p>
 * This stable addressing is the main difference from the LIST-backed
 * {@link RRingBuffer}, which is kept full with {@code RPUSH} + {@code LPOP} and
 * therefore shifts every surviving element down one position on each eviction, so
 * there a given index does not denote the same value over time. Both structures
 * support relative reads such as the newest-first window
 * ({@link #lastItems(int, boolean)}); the array additionally keeps absolute slot
 * indexes valid across evictions ({@link #get(long)}), performs the
 * wrap-and-truncate in a single native command, and supports server-side
 * aggregation over the window ({@link #sum()}, {@link #min()}, {@link #max()},
 * {@link #average()}, {@link #count(Object)}).
 * <p>
 * Typical use cases are last-N alarms, recent fraud scores, access history,
 * remote logs, device events and bounded sensor histories.
 * <p>
 * Buffer capacity must be defined through {@link #trySetCapacity(int)},
 * {@link #setCapacity(int)} or {@link #set(int, Object[])} before
 * {@link #add(Object)}/{@link #addAll(Collection)} usage.
 * <p>
 * Requires <b>Redis 8.8 or higher.</b>
 *
 * @param <V> value type
 *
 * @author Nikita Koksharov
 *
 */
public interface RCircularBuffer<V> extends RExpirable, RCircularBufferAsync<V> {

    /**
     * 仅当尚未设置容量时初始化缓冲区容量。
     *
     * @param capacity 缓冲区容量
     * @return 见方法说明
     *         {@code false} if capacity already set
     */
    boolean trySetCapacity(int capacity);

    /**
     * Sets capacity of this buffer and overrides the current value.
     * <p>
     * The new capacity is applied to the underlying ring on the next write
     * operation ({@link #add(Object)}, {@link #addAll(Collection)} or
     * {@link #set(int, Object[])}), at which point the ring is resized and the
     * oldest values that no longer fit are discarded.
     *
     * @param capacity buffer capacity
     */
    void setCapacity(int capacity);

    /**
     * 返回缓冲区容量。
     *
     * @return 缓冲区容量；未设置时为 {@code 0}
     */
    int capacity();

    /**
     * Returns the remaining capacity of this buffer, that is the number of
     * values that can be added before the oldest values start being evicted.
     *
     * @return remaining capacity
     */
    int remainingCapacity();

    /**
     * 将指定值追加到缓冲区尾部。
     * <p>
     * If the buffer is full the oldest value is overwritten.
     * Buffer capacity must be defined before usage.
     *
     * @param value 值
     * @return 见方法说明
     */
    boolean add(V value);

    /**
     * 按迭代顺序批量追加值到尾部。
     * <p>
     * Values wrap around and overwrite the oldest values once the buffer is full.
     * Buffer capacity must be defined before usage.
     *
     * @param values 值集合
     * @return 见方法说明
     */
    boolean addAll(Collection<? extends V> values);

    /**
     * Writes the specified values into a ring of the given {@code size} and
     * (re)configures this buffer capacity to {@code size}.
     * <p>
     * This is a direct mapping of the native {@code ARRING} command. Values are
     * written at consecutive ring positions starting from the current insert
     * cursor and wrap around as needed.
     *
     * @param size ring size, becomes the new buffer capacity
     * @param values values to write, must contain at least one value
     * @return array index where the last value was written
     */
    long set(int size, V... values);

    /**
     * 返回指定环槽下标处的值。
     *
     * @param index 环槽下标
     * @return value stored at the specified ring index, or {@code null} if absent
     */
    V get(long index);

    /**
     * 返回最近追加的 {@code count} 个值。
     *
     * @param count 返回数量
     * @param reverse 排序方向
     *                otherwise in insertion order (oldest-first)
     * @return 最近追加的值列表
     */
    List<V> lastItems(int count, boolean reverse);

    /**
     * 返回指定环槽下标区间（含端点）内的值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return 指定区间内的值列表
     */
    List<V> range(long startIndex, long endIndex);

    /**
     * 按插入顺序（最旧在前）返回所有保留值。
     *
     * @return 全部保留值
     */
    List<V> readAll();

    /**
     * 返回当前存储的值数量。
     *
     * @return 已存储值数量
     */
    int size();

    /**
     * 返回当前存储数值的总和。
     *
     * @return sum of values, or {@code null} if the buffer is empty
     */
    Double sum();

    /**
     * 返回指定环槽下标区间内数值的总和。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return sum of values
     */
    Double sum(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的最小值。
     *
     * @return minimum value, or {@code null} if the buffer is empty
     */
    Double min();

    /**
     * 返回指定环槽下标区间内的最小值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return minimum value
     */
    Double min(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的最大值。
     *
     * @return maximum value, or {@code null} if the buffer is empty
     */
    Double max();

    /**
     * 返回指定环槽下标区间内的最大值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return maximum value
     */
    Double max(long startIndex, long endIndex);

    /**
     * 清空所有值但保留已配置的容量。
     */
    void clear();

    /**
     * 若缓冲区无任何值则返回 {@code true}。
     *
     * @return 见方法说明
     */
    boolean isEmpty();

    /**
     * Returns {@code true} if this buffer is full, that is the next {@link #add(Object)}
     * will overwrite the oldest value.
     *
     * @return {@code true} if this buffer is full
     */
    boolean isFull();

    /**
     * 返回最近追加的值但不移除。
     *
     * @return the newest value, or {@code null} if this buffer is empty
     */
    V peekLast();

    /**
     * Returns the oldest retained value without removing it, that is the value
     * that will be overwritten next.
     *
     * @return the oldest value, or {@code null} if this buffer is empty
     */
    V peekFirst();

    /**
     * 返回指定多个环槽下标处的值。
     *
     * @param indexes 环槽下标集合
     * @return 指定下标处的值列表
     */
    List<V> get(long... indexes);

    /**
     * 返回当前存储中与指定值相等的元素个数。
     *
     * @param value 值
     * @return 匹配元素个数
     */
    long count(V value);

    /**
     * 若缓冲区包含指定值则返回 {@code true}。
     *
     * @param value 值
     * @return 见方法说明
     */
    boolean contains(V value);

    /**
     * 返回当前存储数值的平均值。
     *
     * @return average value, or {@code null} if the buffer is empty
     */
    Double average();

    /**
     * 返回当前存储数值的按位与结果。
     *
     * @return bitwise AND result, or {@code null} if the buffer is empty
     */
    Long bitAnd();

    /**
     * 返回指定环槽下标区间内数值的按位与结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise AND result
     */
    Long bitAnd(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的按位或结果。
     *
     * @return bitwise OR result, or {@code null} if the buffer is empty
     */
    Long bitOr();

    /**
     * 返回指定环槽下标区间内数值的按位或结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise OR result
     */
    Long bitOr(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的按位异或结果。
     *
     * @return bitwise XOR result, or {@code null} if the buffer is empty
     */
    Long bitXor();

    /**
     * 返回指定环槽下标区间内数值的按位异或结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise XOR result
     */
    Long bitXor(long startIndex, long endIndex);

}
