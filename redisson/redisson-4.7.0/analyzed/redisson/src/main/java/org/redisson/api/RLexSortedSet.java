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

/**
 * 元素为 {@link String} 的字典序有序集合（lex sorted set）同步 API。
 * <p>基于 Redis {@code ZSET} 的 lex 范围命令（{@code ZRANGEBYLEX}、{@code ZLEXCOUNT} 等）。
 *
 * @author Nikita Koksharov
 */
public interface RLexSortedSet extends RLexSortedSetAsync, RSortedSet<String>, RExpirable {
    
    /**
     * 移除并返回字典序最小元素；集合为空时返回 {@code null}。
     *
     * @return the head element, 
     *         or {@code null} if this sorted set is empty
     */
    String pollFirst();

    /**
     * 移除并返回字典序最大元素；集合为空时返回 {@code null}。
     *
     * @return the tail element or {@code null} if this sorted set is empty
     */
    String pollLast();

    /**
     * 按分数从高到低返回元素的逆序排名。
     * 
     * @param o 待查元素
     * @return 排名；不存在时为 null
     */
    Integer revRank(String o);
    
    /**
     * 移除从 {@code fromElement} 开始的尾部 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @return number of elements removed
     */
    int removeRangeTail(String fromElement, boolean fromInclusive);

    /**
     * 移除以 {@code toElement} 结束的头部 lex 区间元素。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return number of elements removed
     */
    int removeRangeHead(String toElement, boolean toInclusive);

    /**
     * 移除 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return number of elements removed
     */
    int removeRange(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive);

    /**
     * 统计从 {@code fromElement} 开始的尾部 lex 区间元素数量。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @return 元素数量
     */
    int countTail(String fromElement, boolean fromInclusive);

    /**
     * 统计以 {@code toElement} 结束的头部 lex 区间元素数量。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素数量
     */
    int countHead(String toElement, boolean toInclusive);

    /**
     * 返回从 {@code fromElement} 开始的尾部 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @return 元素集合
     */
    Collection<String> rangeTail(String fromElement, boolean fromInclusive);

    /**
     * 返回以 {@code toElement} 结束的头部 lex 区间元素。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素集合
     */
    Collection<String> rangeHead(String toElement, boolean toInclusive);

    /**
     * 返回 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素集合
     */
    Collection<String> range(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive);

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
    Collection<String> rangeTail(String fromElement, boolean fromInclusive, int offset, int count);

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
    Collection<String> rangeHead(String toElement, boolean toInclusive, int offset, int count);

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
    Collection<String> range(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive, int offset, int count);

    /**
     * 逆序返回从 {@code fromElement} 开始的尾部 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @return 元素集合
     */
    Collection<String> rangeTailReversed(String fromElement, boolean fromInclusive);

    /**
     * 逆序返回以 {@code toElement} 结束的头部 lex 区间元素。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素集合
     */
    Collection<String> rangeHeadReversed(String toElement, boolean toInclusive);

    /**
     * 逆序返回 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素集合
     */
    Collection<String> rangeReversed(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive);

    /**
     * 逆序返回从 {@code fromElement} 开始的尾部 lex 区间元素。
     * 结果集合受 {@code count} 限制，从 {@code offset} 起返回。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param offset 结果集合偏移量
     * @param count 返回数量
     * @return 元素集合
     */
    Collection<String> rangeTailReversed(String fromElement, boolean fromInclusive, int offset, int count);

    /**
     * 逆序返回以 {@code toElement} 结束的头部 lex 区间元素。
     * 结果集合受 {@code count} 限制，从 {@code offset} 起返回。
     * 
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @param offset 结果集合偏移量
     * @param count 返回数量
     * @return 元素集合
     */
    Collection<String> rangeHeadReversed(String toElement, boolean toInclusive, int offset, int count);

    /**
     * 逆序返回 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。
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
    Collection<String> rangeReversed(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive, int offset, int count);
    
    /**
     * 统计 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素数量。
     * 
     * @param fromElement 起始元素
     * @param fromInclusive 起始边界是否包含
     * @param toElement 结束元素
     * @param toInclusive 结束边界是否包含
     * @return 元素数量
     */
    int count(String fromElement, boolean fromInclusive, String toElement, boolean toInclusive);

    /**
     * 返回元素在字典序中的排名。
     * 
     * @param o 待查元素
     * @return 排名；不存在时为 null
     */
    Integer rank(String o);

    /**
     * 按排名区间返回元素（下标从 0 起）。
     * {@code -1} 表示最高分，{@code -2} 表示次高分。
     * 
     * @param startIndex 起始排名下标
     * @param endIndex 结束排名下标
     * @return 元素集合
     */
    Collection<String> range(int startIndex, int endIndex);

    /**
     * 随机返回集合中的一个元素。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上.</b>
     *
     * @return 随机元素
     */
    String random();

    /**
     * 随机返回至多 {@code count} 个元素。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上.</b>
     *
     * @param count 返回数量
     * @return 随机元素集合
     */
    Collection<String> random(int count);

    /**
     * 注册 lex 有序集对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.ScoredSortedSetAddListener
     * @see org.redisson.api.listener.ScoredSortedSetRemoveListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    int addListener(ObjectListener listener);

}
