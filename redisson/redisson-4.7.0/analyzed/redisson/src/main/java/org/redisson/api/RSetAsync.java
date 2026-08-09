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
import java.util.Set;

/**
 * Redis Set 异步 API。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public interface RSetAsync<V> extends RCollectionAsync<V>, RSortableAsync<Set<V>> {

    /**
     * 异步从集合中随机移除并返回多个元素
     * 
     * @param amount 随机元素数量
     * @return 随机元素集合
     */
    RFuture<Set<V>> removeRandomAsync(int amount);
    
    /**
     * 异步从集合中随机移除并返回一个元素
     * 
     * @return 元素值
     */
    RFuture<V> removeRandomAsync();

    /**
     * 异步从集合中随机返回一个元素
     * 
     * @return 元素值
     */
    RFuture<V> randomAsync();
    
    /**
     * 异步从集合中随机返回至多 {@code count} 个元素
     *
     * @param count 返回元素数量上限
     * @return 元素值
     */
    RFuture<Set<V>> randomAsync(int count);

    /**
     * 异步将成员从当前集合移动到目标集合。
     *
     * @param destination 目标集合名称
     * @param member 待移动成员
     * @return 移动成功则为 true；元素不存在或未执行则为 false
     */
    RFuture<Boolean> moveAsync(String destination, V member);

    /**
     * 一次性读取全部元素。
     *
     * @return 元素值s
     */
    RFuture<Set<V>> readAllAsync();

    /**
     * 将指定名称集合与当前集合作并（并集）并写回当前集合；若当前集合已存在则覆盖。
     *
     * @param names 参与运算的集合名称
     * @return 并集写入后的集合大小
     */
    RFuture<Integer> unionAsync(String... names);

    /**
     * 计算指定名称集合与当前集合的并集并返回结果，不修改当前集合。
     *
     * @param names 参与运算的集合名称
     * @return 元素值s
     */
    RFuture<Set<V>> readUnionAsync(String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @return 元素数量
     */
    RFuture<Integer> countUnionAsync(String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @param limit 并集运算数量上限
     * @return 元素数量
     */
    RFuture<Integer> countUnionAsync(int limit, String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * Returns an approximate value computed through HyperLogLog with
     * 0.81% standard error instead of an exact one.
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @return 近似元素数量
     */
    RFuture<Integer> countUnionApproxAsync(String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * Returns an approximate value computed through HyperLogLog with
     * 0.81% standard error instead of an exact one.
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @param limit 并集运算数量上限
     * @return 近似元素数量
     */
    RFuture<Integer> countUnionApproxAsync(int limit, String... names);

    /**
     * 将指定名称集合与当前集合求差集并写回当前集合；若当前集合已存在则覆盖。
     *
     * @param names 参与运算的集合名称
     * @return 差集写入后的集合大小
     */
    RFuture<Integer> diffAsync(String... names);

    /**
     * 计算指定名称集合与当前集合的差集并返回结果，不修改当前集合。
     * 
     * @param names 参与运算的集合名称
     * @return 元素值s
     */
    RFuture<Set<V>> readDiffAsync(String... names);

    /**
     * 统计指定集合与当前集合差集结果的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @return 元素数量
     */
    RFuture<Integer> countDiffAsync(String... names);

    /**
     * 统计指定集合与当前集合差集结果的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @param limit 差集运算数量上限
     * @return 元素数量
     */
    RFuture<Integer> countDiffAsync(int limit, String... names);

    /**
     * 将指定名称集合与当前集合求交集并写回当前集合；若当前集合已存在则覆盖。
     *
     * @param names 参与运算的集合名称
     * @return 交集写入后的集合大小
     */
    RFuture<Integer> intersectionAsync(String... names);

    /**
     * 计算指定名称集合与当前集合的交集并返回结果，不修改当前集合。
     * 
     * @param names 参与运算的集合名称
     * @return 元素值s
     */
    RFuture<Set<V>> readIntersectionAsync(String... names);

    /**
     * 统计指定集合与当前集合交集结果的元素数量。
     * <p>
     * Requires <b>Redis 7.0.0 and higher.</b>
     *
     * @param names 参与运算的集合名称
     * @return 元素数量
     */
    RFuture<Integer> countIntersectionAsync(String... names);

    /**
     * 统计指定集合与当前集合交集结果的元素数量。
     * <p>
     * Requires <b>Redis 7.0.0 and higher.</b>
     *
     * @param names 参与运算的集合名称
     * @param limit 交集运算数量上限
     * @return 元素数量
     */
    RFuture<Integer> countIntersectionAsync(int limit, String... names);

    /**
     * 仅当全部元素均不在集合中时尝试添加。
     *
     * @param values 待添加元素
     * @return 全部添加成功则为 true，否则 false
     */
    RFuture<Boolean> tryAddAsync(V... values);

    /**
     * 添加集合中全部元素并返回实际新增数量。
     *
     * @param c 元素集合
     * @return 新增元素数量
     */
    RFuture<Integer> addAllCountedAsync(Collection<? extends V> c);

    /**
     * 移除集合中全部指定元素并返回实际删除数量。
     *
     * @param c 元素集合
     * @return 删除元素数量
     */
    RFuture<Integer> removeAllCountedAsync(Collection<? extends V> c);

    /**
     * 检查指定集合中哪些元素存在于当前集合，返回存在的元素集合。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param c 待检查集合
     * @return 存在于当前集合的元素
     */
    RFuture<Set<V>> containsEachAsync(Collection<V> c);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.SetAddListener
     * @see org.redisson.api.listener.SetRemoveListener
     * @see org.redisson.api.listener.SetRemoveRandomListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

    /**
     * 返回分批加载元素的异步迭代器；批次大小由 {@code count} 指定。
     *
     * @return 异步可迭代对象
     */
    AsyncIterator<V> iteratorAsync();

    /**
     * 返回分批加载元素的异步迭代器；批次大小由 {@code count} 指定。
     *
     * @param count 每批元素数量
     * @return 异步可迭代对象
     */
    AsyncIterator<V> iteratorAsync(int count);

}
