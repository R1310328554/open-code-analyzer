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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 基于 Redis 的 {@link java.util.Set} RxJava3 API；支持集合运算、随机元素与分布式锁绑定。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 元素类型
 */
public interface RSetRx<V> extends RCollectionRx<V>, RSortableRx<Set<V>> {

    /**
     * 添加指定集合中的全部元素，返回实际新增数量。
     *
     * @param c 待添加元素集合
     * @return 新增元素数量
     */
    Single<Integer> addAllCounted(Collection<? extends V> c);

    /**
     * 移除指定集合中的全部元素，返回实际删除数量。
     *
     * @param c 待添加元素集合
     * @return 删除元素数量
     */
    Single<Integer> removeAllCounted(Collection<? extends V> c);

    /**
     * 返回与 {@code value} 关联的可过期许可信号量 {@link RPermitExpirableSemaphore}。
     * 
     * @param value 集合元素值
     * @return 可过期许可信号量
     */
    RPermitExpirableSemaphoreRx getPermitExpirableSemaphore(V value);

    /**
     * 返回与 {@code value} 关联的信号量 {@link RSemaphore}。
     * 
     * @param value 集合元素值
     * @return 信号量
     */
    RSemaphoreRx getSemaphore(V value);
    
    /**
     * 返回与 {@code value} 关联的公平分布式锁 {@link RLock}。
     * 
     * @param value 集合元素值
     * @return 分布式锁
     */
    RLockRx getFairLock(V value);
    
    /**
     * 返回与 {@code value} 关联的读写锁 {@link RReadWriteLock}。
     * 
     * @param value 集合元素值
     * @return 读写锁
     */
    RReadWriteLockRx getReadWriteLock(V value);
    
    /**
     * 返回与 {@code value} 关联的分布式锁 {@link RLock}。
     * 
     * @param value 集合元素值
     * @return 分布式锁
     */
    RLockRx getLock(V value);
    
    /**
     * 返回分批拉取元素的迭代器；批次大小由 {@code count} 指定。
     * 
     * @param count 每批元素数量
     * @return 元素迭代器
     */
    Flowable<V> iterator(int count);
    
    /**
     * 返回分批拉取元素的迭代器；批次大小由 {@code count} 指定。
     * If pattern is not null then only elements match this pattern are loaded.
     * 
     * @param pattern 匹配模式
     * @param count 每批元素数量
     * @return 元素迭代器
     */
    Flowable<V> iterator(String pattern, int count);
    
    /**
     * 返回元素迭代器；{@code pattern} 非空时仅加载匹配元素。
     * 
     * @param pattern 匹配模式
     * @return 元素迭代器
     */
    Flowable<V> iterator(String pattern);
    
    /**
     * 随机移除并返回至多 {@code amount} 个元素。
     *
     * @param amount 随机元素数量
     * @return 随机元素集合
     */
    Maybe<Set<V>> removeRandom(int amount);
    
    /**
     * 随机移除并返回一个元素。
     *
     * @return 随机元素
     */
    Maybe<V> removeRandom();

    /**
     * 随机返回一个元素（不移除）。
     *
     * @return 随机元素
     */
    Maybe<V> random();

    /**
     * 随机返回一个元素（不移除）。s from set limited by <code>count</code>
     *
     * @param count 返回元素数量上限
     * @return 随机元素集合
     */
    Maybe<Set<V>> random(int count);


    /**
     * 将成员从当前集合移动到目标集合。
     *
     * @param destination 目标集合名称
     * @param member 待移动成员
     * @return 移动成功则为 true；元素不存在或未执行则为 false
     */
    Single<Boolean> move(String destination, V member);

    /**
     * 一次性读取全部元素。
     *
     * @return 元素集合
     */
    Maybe<Set<V>> readAll();
    
    /**
     * 对指定集合与当前集合求并集，结果写入当前集合（覆盖已有内容）。
     *
     * @param names 参与运算的集合名称
     * @return 并集写入后的集合大小
     */
    Single<Integer> union(String... names);

    /**
     * 对指定集合与当前集合求并集，返回结果但不修改当前集合。
     *
     * @param names 参与运算的集合名称
     * @return 并集写入后的集合大小
     */
    Maybe<Set<V>> readUnion(String... names);

    /**
     * 统计当前集合与指定集合并集的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @return 元素数量
     */
    Single<Integer> countUnion(String... names);

    /**
     * 统计当前集合与指定集合并集的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @param limit 并集运算数量上限
     * @return 元素数量
     */
    Single<Integer> countUnion(int limit, String... names);

    /**
     * 通过 HyperLogLog 近似统计并集元素数量（标准误差约 0.81%）。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @return 近似元素数量
     */
    Single<Integer> countUnionApprox(String... names);

    /**
     * 通过 HyperLogLog 近似统计并集元素数量（标准误差约 0.81%）。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @param limit 并集运算数量上限
     * @return 近似元素数量
     */
    Single<Integer> countUnionApprox(int limit, String... names);
    
    /**
     * 对指定集合与当前集合求差集，结果写入当前集合（覆盖已有内容）。
     *
     * @param names 参与运算的集合名称
     * @return 差集写入后的集合大小
     */
    Single<Integer> diff(String... names);
    
    /**
     * 对指定集合与当前集合求差集，返回结果但不修改当前集合。
     * 
     * @param names 参与运算的集合名称
     * @return 元素集合
     */
    Maybe<Set<V>> readDiff(String... names);

    /**
     * 统计当前集合与指定集合差集的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @return 元素数量
     */
    Single<Integer> countDiff(String... names);

    /**
     * 统计当前集合与指定集合差集的元素数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @param limit 差集运算数量上限
     * @return 元素数量
     */
    Single<Integer> countDiff(int limit, String... names);
    
    /**
     * 对指定集合与当前集合求交集，结果写入当前集合（覆盖已有内容）。
     *
     * @param names 参与运算的集合名称
     * @return 交集写入后的集合大小
     */
    Single<Integer> intersection(String... names);

    /**
     * 统计当前集合与指定集合交集的元素数量。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @return 元素数量
     */
    Single<Integer> countIntersection(String... names);

    /**
     * 统计当前集合与指定集合交集的元素数量。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param names 参与运算的集合名称
     * @param limit 交集运算数量上限
     * @return 元素数量
     */
    Single<Integer> countIntersection(int limit, String... names);

    /**
     * 对指定集合与当前集合求交集，返回结果但不修改当前集合。
     *
     * @param names 参与运算的集合名称
     * @return 元素集合
     */
    Maybe<Set<V>> readIntersection(String... names);

    /**
     * 仅当所有元素均不在集合中时尝试批量添加。
     *
     * @param values 待添加元素
     * @return 全部添加成功则为 true，否则 false
     */
    Single<Boolean> tryAdd(V... values);

    /**
     * 检查指定集合中哪些元素存在于当前集合，返回存在的元素。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param c 待检查集合
     * @return 存在于当前集合的元素
     */
    Maybe<Set<V>> containsEach(Collection<V> c);

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
    Single<Integer> addListener(ObjectListener listener);

}
