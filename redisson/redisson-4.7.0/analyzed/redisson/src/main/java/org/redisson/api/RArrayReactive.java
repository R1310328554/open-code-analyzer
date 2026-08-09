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
import org.redisson.api.array.ArrayEntry;
import org.redisson.api.array.ArrayFullInfo;
import org.redisson.api.array.ArrayGrepArgs;
import org.redisson.api.array.ArrayInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Redis Array 对象 Project Reactor 响应式 API。
 * <p>读写操作返回 {@link Mono} 或 {@link Flux}。
 *
 * @param <V> 值类型
 * @author lamnt2008
 * @author Nikita Koksharov
 */
public interface RArrayReactive<V> extends RExpirableReactive {

    /**
     * 返回指定数组下标处存储的值。
     *
     * @param index 数组下标
     * @return 指定下标处存储的值
     */
    @EmptyAsAbsent
    Mono<V> get(long index);

    /**
     * 若指定数组下标处已存储值则返回 {@code true}。
     *
     * @param index 数组下标
     * @return 已存储值时返回 {@code true}，否则 {@code false}
     */
    Mono<Boolean> isSet(long index);

    /**
     * 返回本数组条目的响应式流，按数组下标升序发射。
     *
     * @return 条目 Flux
     */
    Flux<ArrayEntry<V>> iterator();

    /**
     * 返回指定多个数组下标处的值。
     *
     * @param indexes 数组下标集合
     * @return 指定下标处的值列表
     */
    Mono<List<V>> get(long... indexes);

    /**
     * 在指定数组下标处设置值。
     *
     * @param index 数组下标
     * @param value 要设置的值
     * @return 成功设置的值数量
     */
    Mono<Long> set(long index, V value);

    /**
     * 从指定起始下标起连续设置多个值。
     *
     * @param index 起始数组下标
     * @param values 要设置的值
     * @return 成功设置的值数量
     */
    Mono<Long> set(long index, V... values);

    /**
     * 在指定多个数组下标处设置值。
     *
     * @param entries 数组下标与值的映射
     * @return 成功设置的值数量
     */
    Mono<Long> set(Map<Long, V> entries);

    /**
     * 删除指定数组下标处的值。
     *
     * @param indexes 数组下标集合
     * @return 删除的值数量
     */
    Mono<Long> delete(long... indexes);

    /**
     * 删除指定下标区间内的值。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 删除的值数量
     */
    Mono<Long> deleteRange(long startIndex, long endIndex);

    /**
     * 删除多个下标区间内的值；参数为成对的起始与结束下标。
     *
     * @param startEndIndexes 成对的起始与结束下标
     * @return 删除的值数量
     */
    Mono<Long> deleteRanges(long... startEndIndexes);

    /**
     * 返回本数组中已存储值的数量。
     *
     * @return 值的数量
     */
    Mono<Long> count();

    /**
     * 返回指定下标区间内已存储值的数量。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 值的数量
     */
    Mono<Long> count(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内与给定值相等的元素个数。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param value 待匹配的值
     * @return 匹配的元素个数
     */
    Mono<Long> countMatches(long startIndex, long endIndex, V value);

    /**
     * 返回数组长度。
     *
     * @return 数组长度
     */
    Mono<Long> length();

    /**
     * 返回指定下标区间内的值列表。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 指定区间内的值列表
     */
    Mono<List<V>> range(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的条目列表。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 指定区间内的条目列表
     */
    Mono<List<ArrayEntry<V>>> scan(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的条目列表（最多返回 limit 条）。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param limit 返回条目数量上限
     * @return 指定区间内的条目列表
     */
    Mono<List<ArrayEntry<V>>> scan(long startIndex, long endIndex, long limit);

    /**
     * 从当前插入游标起，在连续下标处插入多个值。
     *
     * @param values 要插入的值
     * @return 最后一个值被插入的数组下标
     */
    Mono<Long> insert(V... values);

    /**
     * 将值写入指定大小的环形缓冲区，按环形位置连续写入并在必要时回绕。
     *
     * @param size 环形缓冲区大小
     * @param values 要插入的值
     * @return 最后一个值被插入的数组下标
     */
    Mono<Long> ring(long size, V... values);

    /**
     * 返回 {@code insert(...)} 或 {@code ring(...)} 使用的下一个插入下标。
     *
     * @return 下一个插入下标；游标耗尽时返回空 {@link Mono}
     */
    @EmptyAsAbsent
    Mono<Long> next();

    /**
     * 设置当前插入游标下标。
     *
     * @param index 数组下标
     * @return 设置成功返回 {@code true}，否则 {@code false}
     */
    Mono<Boolean> seek(long index);

    /**
     * 返回最近插入的值。
     *
     * @param count 值数量
     * @return 最近插入的值列表
     */
    Mono<List<V>> lastItems(long count);

    /**
     * 按逆序返回最近插入的值。
     *
     * @param count 值数量
     * @return 逆序的最近插入值列表
     */
    Mono<List<V>> lastItemsReversed(long count);

    /**
     * 返回数组基本信息。
     *
     * @return 数组基本信息
     */
    Mono<ArrayInfo> getInfo();

    /**
     * 返回包含扩展统计信息的完整数组信息。
     *
     * @return 完整数组信息
     */
    Mono<ArrayFullInfo> getFullInfo();

    /**
     * 返回匹配 grep 条件的值所在下标。
     *
     * @param args grep 参数
     * @return 匹配值的下标列表
     */
    Mono<List<Long>> grep(ArrayGrepArgs args);

    /**
     * 返回指定下标区间内匹配 grep 条件的值所在下标。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param args grep 参数
     * @return 匹配值的下标列表
     */
    Mono<List<Long>> grep(long startIndex, long endIndex, ArrayGrepArgs args);

    /**
     * 返回匹配 grep 条件的条目。
     *
     * @param args grep 参数
     * @return 匹配的条目列表
     */
    Mono<List<ArrayEntry<V>>> grepEntries(ArrayGrepArgs args);

    /**
     * 返回指定下标区间内匹配 grep 条件的条目。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param args grep 参数
     * @return 匹配的条目列表
     */
    Mono<List<ArrayEntry<V>>> grepEntries(long startIndex, long endIndex, ArrayGrepArgs args);

    /**
     * 返回指定下标区间内数值元素之和。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 数值之和
     */
    @EmptyAsAbsent
    Mono<Double> sum(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的最小数值。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 最小值
     */
    @EmptyAsAbsent
    Mono<Double> min(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的最大数值。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 最大值
     */
    @EmptyAsAbsent
    Mono<Double> max(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内数值的按位与结果。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 按位与结果
     */
    @EmptyAsAbsent
    Mono<Long> bitAnd(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内数值的按位或结果。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 按位或结果
     */
    @EmptyAsAbsent
    Mono<Long> bitOr(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内数值的按位异或结果。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 按位异或结果
     */
    @EmptyAsAbsent
    Mono<Long> bitXor(long startIndex, long endIndex);

}
