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

import java.time.Duration;
import java.util.*;

import org.redisson.api.mapreduce.RCollectionMapReduce;

/**
 * Redis Sorted Set 同步 API；元素按 {@link Comparator} 排序。
 * <p>支持阻塞弹出、MapReduce 及跨应用共享迭代器。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RSortedSet<V> extends SortedSet<V>, RExpirable {

    /**
     * 返回与当前有序集合关联的 {@link RCollectionMapReduce} 实例。
     * 
     * @param <KOut> 输出键类型
     * @param <VOut> 输出值类型
     * @return MapReduce 实例
     */
    <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce();

    Collection<V> readAll();
    
    RFuture<Collection<V>> readAllAsync();

    /**
     * 移除并返回最小（队首）元素；集合为空时返回 {@code null}。
     *
     * @return 队首元素；空集合时为 {@code null}
     */
    V pollFirst();

    /**
     * 移除并返回最小（队首）的多个元素。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param count 元素数量
     * @return 队首元素集合
     */
    Collection<V> pollFirst(int count);

    /**
     * 移除并返回最小（队首）元素；集合为空时返回 {@code null}。
     *
     * @param duration 最长等待时间
     * @return 队首元素；空集合时为 {@code null}
     */
    V pollFirst(Duration duration);

    /**
     * 在指定等待时间内移除并返回多个最小（队首）元素。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count elements amount
     * @return 队首元素列表
     */
    List<V> pollFirst(Duration duration, int count);

    /**
     * 移除并返回最小（队首）元素；集合为空时返回 {@code null}。
     *
     * @return 队首元素；空集合时为 {@code null}
     */
    RFuture<V> pollFirstAsync();

    /**
     * 移除并返回最小（队首）的多个元素。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param count 元素数量
     * @return 队首元素集合
     */
    RFuture<Collection<V>> pollFirstAsync(int count);

    /**
     * 移除并返回最小（队首）元素；集合为空时返回 {@code null}。
     *
     * @param duration 最长等待时间
     * @return 队首元素；空集合时为 {@code null}
     */
    RFuture<V> pollFirstAsync(Duration duration);

    /**
     * 在指定等待时间内移除并返回多个最小（队首）元素。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count elements amount
     * @return 队首元素列表
     */
    RFuture<List<V>> pollFirstAsync(Duration duration, int count);

    /**
     * 移除并返回最大（队尾）元素；集合为空时返回 {@code null}。
     *
     * @return 队尾元素；空集合时为 {@code null}
     */
    V pollLast();

    /**
     * 移除并返回最大（队尾）的多个元素。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param count 元素数量
     * @return 队尾元素集合
     */
    Collection<V> pollLast(int count);

    /**
     * 移除并返回最大（队尾）元素；集合为空时返回 {@code null}。
     *
     * @param duration 最长等待时间
     * @return the tail element,
     *         or {@code null} if this sorted set is empty
     */
    V pollLast(Duration duration);

    /**
     * 在指定等待时间内移除并返回多个最大（队尾）元素。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count elements amount
     * @return 队尾元素列表
     */
    List<V> pollLast(Duration duration, int count);

    /**
     * 移除并返回最大（队尾）元素；集合为空时返回 {@code null}。
     *
     * @return 队尾元素；空集合时为 {@code null}
     */
    RFuture<V> pollLastAsync();

    /**
     * 移除并返回最大（队尾）的多个元素。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param count 元素数量
     * @return 队尾元素集合
     */
    RFuture<Collection<V>> pollLastAsync(int count);

    /**
     * 移除并返回最大（队尾）元素；集合为空时返回 {@code null}。
     *
     * @param duration 最长等待时间
     * @return the tail element,
     *         or {@code null} if this sorted set is empty
     */
    RFuture<V> pollLastAsync(Duration duration);

    /**
     * 在指定等待时间内移除并返回多个最大（队尾）元素。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count elements amount
     * @return 队尾元素列表
     */
    RFuture<List<V>> pollLastAsync(Duration duration, int count);

    RFuture<Boolean> addAsync(V value);
    
    RFuture<Boolean> removeAsync(Object value);
    
    /**
     * 仅当当前有序集合为空时设置新的比较器。
     *
     * @param comparator 元素比较器
     * @return 设置成功则为 true，否则 false
     */
    boolean trySetComparator(Comparator<? super V> comparator);

    /**
     * 返回可在多应用间共享的元素迭代器；同一对象多次调用本方法共享同一游标。
     * 需独立游标请使用 {@linkplain RList#distributedIterator(String, int)}。
     * @param count 批次大小
     * @return 共享元素迭代器
     */
    Iterator<V> distributedIterator(int count);

    /**
     * 返回匹配指定模式的元素迭代器，可在多应用间共享。
     * 迭代器名称须与集合名称解析到同一 hash slot。
     * @param count 批次大小
     * @param iteratorName 保存游标的 Redis 对象名
     * @return 共享元素迭代器
     */
    Iterator<V> distributedIterator(String iteratorName, int count);

}
