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

import io.reactivex.rxjava3.core.Single;

/**
 * 元素为 {@link String} 的字典序有序集合（lex sorted set）RxJava API。
 * <p>各方法返回 {@link Single}；基于 Redis lex 范围命令。
 *
 * @author Nikita Koksharov
 */
public interface RLexSortedSetRx extends RScoredSortedSetRx<String>, RCollectionRx<String> {

    /**
     * 移除 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 移除的元素数量
     */
    Single<Integer> removeRange(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive);

    /**
     * 移除从 {@code fromElement} 开始的尾部 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @return 移除的元素数量
     */
    Single<Integer> removeRangeTail(String fromElement, boolean fromInclusive);

    /**
     * 移除以 {@code toElement} 结束的头部 lex 区间元素。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 移除的元素数量
     */
    Single<Integer> removeRangeHead(String toElement, boolean toInclusive);

    /**
     * 统计从 {@code fromElement} 开始的尾部 lex 区间元素数量。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @return 元素数量
     */
    Single<Integer> countTail(String fromElement, boolean fromInclusive);

    /**
     * 统计以 {@code toElement} 结束的头部 lex 区间元素数量。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素数量
     */
    Single<Integer> countHead(String toElement, boolean toInclusive);

    /**
     * 返回从 {@code fromElement} 开始的尾部 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @return 元素集合
     */
    Single<Collection<String>> rangeTail(String fromElement, boolean fromInclusive);

    /**
     * 返回以 {@code toElement} 结束的头部 lex 区间元素。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素集合
     */
    Single<Collection<String>> rangeHead(String toElement, boolean toInclusive);

    /**
     * 返回 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素集合
     */
    Single<Collection<String>> range(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive);

    /**
     * 返回从 {@code fromElement} 开始的尾部 lex 区间元素。
     * 结果集合受 {@code count} 限制，从 {@code offset} 起返回。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param offset 结果集合偏移量
     * @param count 返回数量
     * @return 元素集合
     */
    Single<Collection<String>> rangeTail(String fromElement, boolean fromInclusive, int offset, int count);

    /**
     * 返回以 {@code toElement} 结束的头部 lex 区间元素。
     * 结果集合受 {@code count} 限制，从 {@code offset} 起返回。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @param offset 结果集合偏移量
     * @param count 返回数量
     * @return 元素集合
     */
    Single<Collection<String>> rangeHead(String toElement, boolean toInclusive, int offset, int count);

    /**
     * 返回 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。
     * 结果集合受 {@code count} 限制，从 {@code offset} 起返回。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @param offset 结果集合偏移量
     * @param count 返回数量
     * @return 元素集合
     */
    Single<Collection<String>> range(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive, int offset, int count);

    /**
     * 统计 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素数量。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素数量
     */
    Single<Integer> count(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive);

}
