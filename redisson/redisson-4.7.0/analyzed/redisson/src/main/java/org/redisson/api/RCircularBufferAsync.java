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
 * Async interface for {@link RCircularBuffer}.
 *
 * @param <V> value type
 *
 * @author Nikita Koksharov
 *
 */
public interface RCircularBufferAsync<V> extends RExpirableAsync {

    /**
     * 仅当尚未设置容量时初始化缓冲区容量。
     *
     * @param capacity 缓冲区容量
     * @return 见方法说明
     *         {@code false} if capacity already set
     */
    RFuture<Boolean> trySetCapacityAsync(int capacity);

    /**
     * Sets capacity of this buffer and overrides the current value.
     * <p>
     * The new capacity is applied to the underlying ring on the next write
     * operation ({@link #addAsync(Object)}, {@link #addAllAsync(Collection)} or
     * {@link #setAsync(int, Object[])}), at which point the ring is resized and
     * the oldest values that no longer fit are discarded.
     *
     * @param capacity buffer capacity
     * @return void
     */
    RFuture<Void> setCapacityAsync(int capacity);

    /**
     * 返回缓冲区容量。
     *
     * @return 缓冲区容量；未设置时为 {@code 0}
     */
    RFuture<Integer> capacityAsync();

    /**
     * Returns the remaining capacity of this buffer, that is the number of
     * values that can be added before the oldest values start being evicted.
     *
     * @return remaining capacity
     */
    RFuture<Integer> remainingCapacityAsync();

    /**
     * 将指定值追加到缓冲区尾部。
     * <p>
     * If the buffer is full the oldest value is overwritten.
     * Buffer capacity must be defined through {@link #trySetCapacityAsync(int)},
     * {@link #setCapacityAsync(int)} or {@link #setAsync(int, Object[])} before usage.
     *
     * @param value 值
     * @return 见方法说明
     */
    RFuture<Boolean> addAsync(V value);

    /**
     * 按迭代顺序批量追加值到尾部。
     * <p>
     * Values wrap around and overwrite the oldest values once the buffer is full.
     * Buffer capacity must be defined through {@link #trySetCapacityAsync(int)},
     * {@link #setCapacityAsync(int)} or {@link #setAsync(int, Object[])} before usage.
     *
     * @param values 值集合
     * @return 见方法说明
     */
    RFuture<Boolean> addAllAsync(Collection<? extends V> values);

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
    RFuture<Long> setAsync(int size, V... values);

    /**
     * 返回指定环槽下标处的值。
     *
     * @param index 环槽下标
     * @return value stored at the specified ring index, or {@code null} if absent
     */
    RFuture<V> getAsync(long index);

    /**
     * 返回最近追加的 {@code count} 个值。
     *
     * @param count 返回数量
     * @param reverse 排序方向
     *                otherwise in insertion order (oldest-first)
     * @return 最近追加的值列表
     */
    RFuture<List<V>> lastItemsAsync(int count, boolean reverse);

    /**
     * 返回指定环槽下标区间（含端点）内的值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return 指定区间内的值列表
     */
    RFuture<List<V>> rangeAsync(long startIndex, long endIndex);

    /**
     * 按插入顺序（最旧在前）返回所有保留值。
     *
     * @return 全部保留值
     */
    RFuture<List<V>> readAllAsync();

    /**
     * 返回当前存储的值数量。
     *
     * @return 已存储值数量
     */
    RFuture<Integer> sizeAsync();

    /**
     * 返回当前存储数值的总和。
     *
     * @return sum of values, or {@code null} if the buffer is empty
     */
    RFuture<Double> sumAsync();

    /**
     * 返回指定环槽下标区间内数值的总和。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return sum of values
     */
    RFuture<Double> sumAsync(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的最小值。
     *
     * @return minimum value, or {@code null} if the buffer is empty
     */
    RFuture<Double> minAsync();

    /**
     * 返回指定环槽下标区间内的最小值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return minimum value
     */
    RFuture<Double> minAsync(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的最大值。
     *
     * @return maximum value, or {@code null} if the buffer is empty
     */
    RFuture<Double> maxAsync();

    /**
     * 返回指定环槽下标区间内的最大值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return maximum value
     */
    RFuture<Double> maxAsync(long startIndex, long endIndex);

    /**
     * 清空所有值但保留已配置的容量。
     *
     * @return void
     */
    RFuture<Void> clearAsync();

    /**
     * 若缓冲区无任何值则返回 {@code true}。
     *
     * @return 见方法说明
     */
    RFuture<Boolean> isEmptyAsync();

    /**
     * Returns {@code true} if this buffer is full, that is the next {@code add}
     * will overwrite the oldest value.
     *
     * @return {@code true} if this buffer is full
     */
    RFuture<Boolean> isFullAsync();

    /**
     * 返回最近追加的值但不移除。
     *
     * @return the newest value, or {@code null} if this buffer is empty
     */
    RFuture<V> peekLastAsync();

    /**
     * Returns the oldest retained value without removing it, that is the value
     * that will be overwritten next.
     *
     * @return the oldest value, or {@code null} if this buffer is empty
     */
    RFuture<V> peekFirstAsync();

    /**
     * 返回指定多个环槽下标处的值。
     *
     * @param indexes 环槽下标集合
     * @return 指定下标处的值列表
     */
    RFuture<List<V>> getAsync(long... indexes);

    /**
     * 返回当前存储中与指定值相等的元素个数。
     *
     * @param value 值
     * @return 匹配元素个数
     */
    RFuture<Long> countAsync(V value);

    /**
     * 若缓冲区包含指定值则返回 {@code true}。
     *
     * @param value 值
     * @return 见方法说明
     */
    RFuture<Boolean> containsAsync(V value);

    /**
     * 返回当前存储数值的平均值。
     *
     * @return average value, or {@code null} if the buffer is empty
     */
    RFuture<Double> averageAsync();

    /**
     * 返回当前存储数值的按位与结果。
     *
     * @return bitwise AND result, or {@code null} if the buffer is empty
     */
    RFuture<Long> bitAndAsync();

    /**
     * 返回指定环槽下标区间内数值的按位与结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise AND result
     */
    RFuture<Long> bitAndAsync(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的按位或结果。
     *
     * @return bitwise OR result, or {@code null} if the buffer is empty
     */
    RFuture<Long> bitOrAsync();

    /**
     * 返回指定环槽下标区间内数值的按位或结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise OR result
     */
    RFuture<Long> bitOrAsync(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的按位异或结果。
     *
     * @return bitwise XOR result, or {@code null} if the buffer is empty
     */
    RFuture<Long> bitXorAsync();

    /**
     * 返回指定环槽下标区间内数值的按位异或结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise XOR result
     */
    RFuture<Long> bitXorAsync(long startIndex, long endIndex);

}
