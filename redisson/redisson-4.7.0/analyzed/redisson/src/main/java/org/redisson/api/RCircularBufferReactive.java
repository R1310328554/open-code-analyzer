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

import org.redisson.api.annotation.EmptyAsAbsent;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

/**
 * Reactive interface for {@link RCircularBuffer}.
 *
 * @param <V> value type
 *
 * @author Nikita Koksharov
 *
 */
public interface RCircularBufferReactive<V> extends RExpirableReactive {

    /**
     * 仅当尚未设置容量时初始化缓冲区容量。
     *
     * @param capacity 缓冲区容量
     * @return 见方法说明
     *         {@code false} if capacity already set
     */
    Mono<Boolean> trySetCapacity(int capacity);

    /**
     * Sets capacity of this buffer and overrides the current value.
     *
     * @param capacity buffer capacity
     * @return void
     */
    Mono<Void> setCapacity(int capacity);

    /**
     * 返回缓冲区容量。
     *
     * @return 缓冲区容量；未设置时为 {@code 0}
     */
    Mono<Integer> capacity();

    /**
     * 返回剩余可写入容量（再写入多少元素才会开始淘汰最旧值）。
     *
     * @return 剩余容量
     */
    Mono<Integer> remainingCapacity();

    /**
     * 将指定值追加到缓冲区尾部。
     *
     * @param value 值
     * @return 见方法说明
     */
    Mono<Boolean> add(V value);

    /**
     * 按迭代顺序批量追加值到尾部。
     *
     * @param values 值集合
     * @return 见方法说明
     */
    Mono<Boolean> addAll(Collection<? extends V> values);

    /**
     * Writes the specified values into a ring of the given {@code size} and
     * (re)configures this buffer capacity to {@code size}.
     *
     * @param size ring size, becomes the new buffer capacity
     * @param values values to write, must contain at least one value
     * @return array index where the last value was written
     */
    Mono<Long> set(int size, V... values);

    /**
     * 返回指定环槽下标处的值。
     *
     * @param index 环槽下标
     * @return value stored at the specified ring index, or {@code null} if absent
     */
    Mono<V> get(long index);

    /**
     * 返回最近追加的 {@code count} 个值。
     *
     * @param count 返回数量
     * @param reverse 排序方向
     *                otherwise in insertion order (oldest-first)
     * @return 最近追加的值列表
     */
    @EmptyAsAbsent
    Mono<List<V>> lastItems(int count, boolean reverse);

    /**
     * 返回指定环槽下标区间（含端点）内的值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return 指定区间内的值列表
     */
    @EmptyAsAbsent
    Mono<List<V>> range(long startIndex, long endIndex);

    /**
     * 按插入顺序（最旧在前）返回所有保留值。
     *
     * @return 全部保留值
     */
    @EmptyAsAbsent
    Mono<List<V>> readAll();

    /**
     * 返回当前存储的值数量。
     *
     * @return 已存储值数量
     */
    Mono<Integer> size();

    /**
     * 返回当前存储数值的总和。
     *
     * @return sum of values, or {@code null} if the buffer is empty
     */
    Mono<Double> sum();

    /**
     * 返回指定环槽下标区间内数值的总和。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return sum of values
     */
    Mono<Double> sum(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的最小值。
     *
     * @return minimum value, or {@code null} if the buffer is empty
     */
    Mono<Double> min();

    /**
     * 返回指定环槽下标区间内的最小值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return minimum value
     */
    Mono<Double> min(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的最大值。
     *
     * @return maximum value, or {@code null} if the buffer is empty
     */
    Mono<Double> max();

    /**
     * 返回指定环槽下标区间内的最大值。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return maximum value
     */
    Mono<Double> max(long startIndex, long endIndex);

    /**
     * 清空所有值但保留已配置的容量。
     *
     * @return void
     */
    Mono<Void> clear();

    /**
     * 若缓冲区无任何值则返回 {@code true}。
     *
     * @return 见方法说明
     */
    Mono<Boolean> isEmpty();

    /**
     * 若缓冲区已满则返回 {@code true}。
     *
     * @return 见方法说明
     */
    Mono<Boolean> isFull();

    /**
     * 返回最近追加的值但不移除。
     *
     * @return the newest value, or {@code null} if this buffer is empty
     */
    Mono<V> peekLast();

    /**
     * 返回最旧保留值但不移除。
     *
     * @return the oldest value, or {@code null} if this buffer is empty
     */
    Mono<V> peekFirst();

    /**
     * 返回指定多个环槽下标处的值。
     *
     * @param indexes 环槽下标集合
     * @return 指定下标处的值列表
     */
    @EmptyAsAbsent
    Mono<List<V>> get(long... indexes);

    /**
     * 返回当前存储中与指定值相等的元素个数。
     *
     * @param value 值
     * @return 匹配元素个数
     */
    Mono<Long> count(V value);

    /**
     * 若缓冲区包含指定值则返回 {@code true}。
     *
     * @param value 值
     * @return 见方法说明
     */
    Mono<Boolean> contains(V value);

    /**
     * 返回当前存储数值的平均值。
     *
     * @return average value, or {@code null} if the buffer is empty
     */
    Mono<Double> average();

    /**
     * 返回当前存储数值的按位与结果。
     *
     * @return bitwise AND result, or {@code null} if the buffer is empty
     */
    Mono<Long> bitAnd();

    /**
     * 返回指定环槽下标区间内数值的按位与结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise AND result
     */
    Mono<Long> bitAnd(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的按位或结果。
     *
     * @return bitwise OR result, or {@code null} if the buffer is empty
     */
    Mono<Long> bitOr();

    /**
     * 返回指定环槽下标区间内数值的按位或结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise OR result
     */
    Mono<Long> bitOr(long startIndex, long endIndex);

    /**
     * 返回当前存储数值的按位异或结果。
     *
     * @return bitwise XOR result, or {@code null} if the buffer is empty
     */
    Mono<Long> bitXor();

    /**
     * 返回指定环槽下标区间内数值的按位异或结果。
     *
     * @param startIndex 起始环槽下标
     * @param endIndex 结束环槽下标
     * @return bitwise XOR result
     */
    Mono<Long> bitXor(long startIndex, long endIndex);

}
