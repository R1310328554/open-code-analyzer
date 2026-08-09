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

import org.redisson.api.array.ArrayEntry;
import org.redisson.api.array.ArrayFullInfo;
import org.redisson.api.array.ArrayGrepArgs;
import org.redisson.api.array.ArrayInfo;

import java.util.List;
import java.util.Map;

/**
 * Redis Array 对象异步 API。
 * <p>方法返回 {@link RFuture}，适用于非阻塞调用。
 *
 * @param <V> 值类型
 * @author lamnt2008
 * @author Nikita Koksharov
 */
public interface RArrayAsync<V> extends RExpirableAsync {

    /**
     * 返回指定数组下标处存储的值。
     *
     * @param index 数组下标
     * @return 指定下标处存储的值
     */
    RFuture<V> getAsync(long index);

    /**
     * 若指定数组下标处已存储值则返回 {@code true}。
     *
     * @param index 数组下标
     * @return 已存储值时返回 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> isSetAsync(long index);

    /**
     * 返回本数组条目的异步迭代器，按数组下标升序返回。
     *
     * @return 异步条目迭代器
     */
    AsyncIterator<ArrayEntry<V>> iteratorAsync();

    /**
     * 返回本数组条目的异步迭代器，按升序分批拉取。
     *
     * @param count 分页大小提示，对应 {@code ARSCAN COUNT} 选项
     * @return 异步条目迭代器
     */
    AsyncIterator<ArrayEntry<V>> iteratorAsync(int count);

    /**
     * 返回指定多个数组下标处的值。
     *
     * @param indexes 数组下标集合
     * @return 指定下标处的值列表
     */
    RFuture<List<V>> getAsync(long... indexes);

    /**
     * 在指定数组下标处设置值。
     *
     * @param index 数组下标
     * @param value 要设置的值
     * @return 成功设置的值数量
     */
    RFuture<Long> setAsync(long index, V value);

    /**
     * 从指定起始下标起连续设置多个值。
     *
     * @param index 起始数组下标
     * @param values 要设置的值
     * @return 成功设置的值数量
     */
    RFuture<Long> setAsync(long index, V... values);

    /**
     * 在指定多个数组下标处设置值。
     *
     * @param entries 数组下标与值的映射
     * @return 成功设置的值数量
     */
    RFuture<Long> setAsync(Map<Long, V> entries);

    /**
     * 删除指定数组下标处的值。
     *
     * @param indexes 数组下标集合
     * @return 删除的值数量
     */
    RFuture<Long> deleteAsync(long... indexes);

    /**
     * 删除指定下标区间内的值。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 删除的值数量
     */
    RFuture<Long> deleteRangeAsync(long startIndex, long endIndex);

    /**
     * 删除多个下标区间内的值；参数为成对的起始与结束下标。
     *
     * @param startEndIndexes 成对的起始与结束下标
     * @return 删除的值数量
     */
    RFuture<Long> deleteRangesAsync(long... startEndIndexes);

    /**
     * 返回本数组中已存储值的数量。
     *
     * @return 值的数量
     */
    RFuture<Long> countAsync();

    /**
     * 返回指定下标区间内已存储值的数量。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 值的数量
     */
    RFuture<Long> countAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内与给定值相等的元素个数。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param value 待匹配的值
     * @return 匹配的元素个数
     */
    RFuture<Long> countMatchesAsync(long startIndex, long endIndex, V value);

    /**
     * 返回数组长度。
     *
     * @return 数组长度
     */
    RFuture<Long> lengthAsync();

    /**
     * 返回指定下标区间内的值列表。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 指定区间内的值列表
     */
    RFuture<List<V>> rangeAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的条目列表。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 指定区间内的条目列表
     */
    RFuture<List<ArrayEntry<V>>> scanAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的条目列表（最多返回 limit 条）。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param limit 返回条目数量上限
     * @return 指定区间内的条目列表
     */
    RFuture<List<ArrayEntry<V>>> scanAsync(long startIndex, long endIndex, long limit);

    /**
     * 从当前插入游标起，在连续下标处插入多个值。
     *
     * @param values 要插入的值
     * @return 最后一个值被插入的数组下标
     */
    RFuture<Long> insertAsync(V... values);

    /**
     * 将值写入指定大小的环形缓冲区，按环形位置连续写入并在必要时回绕。
     *
     * @param size 环形缓冲区大小
     * @param values 要插入的值
     * @return 最后一个值被插入的数组下标
     */
    RFuture<Long> ringAsync(long size, V... values);

    /**
     * 返回 {@code insertAsync(...)} 或 {@code ringAsync(...)} 使用的下一个插入下标。
     *
     * @return 下一个插入下标；游标耗尽时返回 {@code null}
     */
    RFuture<Long> nextAsync();

    /**
     * 设置当前插入游标下标。
     *
     * @param index 数组下标
     * @return 设置成功返回 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> seekAsync(long index);

    /**
     * 返回最近插入的值。
     *
     * @param count 值数量
     * @return 最近插入的值列表
     */
    RFuture<List<V>> lastItemsAsync(long count);

    /**
     * 按逆序返回最近插入的值。
     *
     * @param count 值数量
     * @return 逆序的最近插入值列表
     */
    RFuture<List<V>> lastItemsReversedAsync(long count);

    /**
     * 返回数组基本信息。
     *
     * @return 数组基本信息
     */
    RFuture<ArrayInfo> getInfoAsync();

    /**
     * 返回包含扩展统计信息的完整数组信息。
     *
     * @return 完整数组信息
     */
    RFuture<ArrayFullInfo> getFullInfoAsync();

    /**
     * 返回匹配 grep 条件的值所在下标。
     *
     * @param args grep 参数
     * @return 匹配值的下标列表
     */
    RFuture<List<Long>> grepAsync(ArrayGrepArgs args);

    /**
     * 返回指定下标区间内匹配 grep 条件的值所在下标。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param args grep 参数
     * @return 匹配值的下标列表
     */
    RFuture<List<Long>> grepAsync(long startIndex, long endIndex, ArrayGrepArgs args);

    /**
     * 返回匹配 grep 条件的条目。
     *
     * @param args grep 参数
     * @return 匹配的条目列表
     */
    RFuture<List<ArrayEntry<V>>> grepEntriesAsync(ArrayGrepArgs args);

    /**
     * 返回指定下标区间内匹配 grep 条件的条目。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @param args grep 参数
     * @return 匹配的条目列表
     */
    RFuture<List<ArrayEntry<V>>> grepEntriesAsync(long startIndex, long endIndex, ArrayGrepArgs args);

    /**
     * 返回指定下标区间内数值元素之和。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 数值之和
     */
    RFuture<Double> sumAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的最小数值。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 最小值
     */
    RFuture<Double> minAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内的最大数值。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 最大值
     */
    RFuture<Double> maxAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内数值的按位与结果。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 按位与结果
     */
    RFuture<Long> bitAndAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内数值的按位或结果。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 按位或结果
     */
    RFuture<Long> bitOrAsync(long startIndex, long endIndex);

    /**
     * 返回指定下标区间内数值的按位异或结果。
     *
     * @param startIndex 起始数组下标
     * @param endIndex 结束数组下标
     * @return 按位异或结果
     */
    RFuture<Long> bitXorAsync(long startIndex, long endIndex);

}
