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

import org.redisson.api.mapreduce.RCollectionMapReduce;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 基于 Redis 的 {@link java.util.Set} 同步 API {@link RSet}。
 * <p>封装 SADD/SREM、SMEMBERS、SINTER/SUNION/SDIFF、SSCAN 及 per-member 锁等操作。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RSet<V> extends Set<V>, RExpirable, RSetAsync<V>, RSortable<Set<V>> {

    /**
     * 添加all elements contained in the specified collection.。
     * Returns number of added elements.
     *
     * @param c 待添加元素集合
     * @return number of added elements
     */
    int addAllCounted(Collection<? extends V> c);

    /**
     * 移除all elements contained in the specified collection.。
     * Returns number of removed elements.
     *
     * @param c 待添加元素集合
     * @return number of removed elements
     */
    int removeAllCounted(Collection<? extends V> c);

    /**
     * 返回<code>RCountDownLatch</code> instance associated with <code>value</code>。
     * 
     * @param value Set 成员值
     * @return RCountDownLatch object
     */
    RCountDownLatch getCountDownLatch(V value);
    
    /**
     * 返回与 {@code value} 关联的 {@link RPermitExpirableSemaphore} 实例
     * 
     * @param value Set 成员值
     * @return RPermitExpirableSemaphore object
     */
    RPermitExpirableSemaphore getPermitExpirableSemaphore(V value);

    /**
     * 返回与 {@code value} 关联的 {@link RSemaphore} 实例
     * 
     * @param value Set 成员值
     * @return RSemaphore object
     */
    RSemaphore getSemaphore(V value);
    
    /**
     * 返回与 {@code value} 关联的公平 {@link RLock} 实例
     * 
     * @param value Set 成员值
     * @return RLock object
     */
    RLock getFairLock(V value);
    
    /**
     * 返回与 {@code value} 关联的 {@link RReadWriteLock} 实例
     * 
     * @param value Set 成员值
     * @return RReadWriteLock object
     */
    RReadWriteLock getReadWriteLock(V value);
    
    /**
     * 返回与 {@code value} 关联的 {@link RLock} 实例
     * 
     * @param value Set 成员值
     * @return RLock object
     */
    RLock getLock(V value);
    
    /**
     * 返回stream of elements fetches elements in a batch.。
     * Batch size is defined by <code>count</code> param.
     * 
     * @param count - size of elements batch
     * @return stream of elements
     */
    Stream<V> stream(int count);
    
    /**
     * 返回stream of elements fetches elements in a batch.。
     * Batch size is defined by <code>count</code> param.
     * If pattern is not null then only elements match this pattern are loaded.
     * 
     * @param pattern - search pattern
     * @param count - size of elements batch
     * @return stream of elements
     */
    Stream<V> stream(String pattern, int count);
    
    /**
     * 返回stream of elements.。
     * If pattern is not null then only elements match this pattern are loaded.
     * 
     * @param pattern - search pattern
     * @return stream of elements
     */
    Stream<V> stream(String pattern);
    
    /**
     * 返回elements iterator fetches elements in a batch.。
     * Batch size is defined by <code>count</code> param.
     * 
     * @param count - size of elements batch
     * @return iterator
     */
    Iterator<V> iterator(int count);
    
    /**
     * 返回elements iterator fetches elements in a batch.。
     * Batch size is defined by <code>count</code> param.
     * If pattern is not null then only elements match this pattern are loaded.
     * 
     * @param pattern - search pattern
     * @param count - size of elements batch
     * @return iterator
     */
    Iterator<V> iterator(String pattern, int count);
    
    /**
     * 返回elements iterator.。
     * If <code>pattern</code> is not null then only elements match this pattern are loaded.
     * 
     * @param pattern - search pattern
     * @return iterator
     */
    Iterator<V> iterator(String pattern);

    /**
     * 返回element iterator that can be shared across multiple applications.。
     * Creating multiple iterators on the same object with this method will result in a single shared iterator.
     * See {@linkplain RSet#distributedIterator(String, String, int)} for creating different iterators.
     * @param count batch size
     * @return shared elements iterator
     */
    Iterator<V> distributedIterator(int count);

    /**
     * 返回iterator over elements that match specified pattern. Iterator can be shared across multiple applications.。
     * Creating multiple iterators on the same object with this method will result in a single shared iterator.
     * See {@linkplain RSet#distributedIterator(String, String, int)} for creating different iterators.
     * @param pattern element pattern
     * @return shared elements iterator
     */
    Iterator<V> distributedIterator(String pattern);

    /**
     * 返回iterator over elements that match specified pattern. Iterator can be shared across multiple applications.。
     * Creating multiple iterators on the same object with this method will result in a single shared iterator.
     * Iterator name must be resolved to the same hash slot as set name.
     * @param pattern element pattern
     * @param count batch size
     * @param iteratorName redis object name to which cursor will be saved
     * @return shared elements iterator
     */
    Iterator<V> distributedIterator(String iteratorName, String pattern, int count);

    /**
     * 返回<code>RMapReduce</code> object associated with this object。
     * 
     * @param <KOut> output key
     * @param <VOut> output value
     * @return MapReduce instance
     */
    <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce();
    
    /**
     * 移除and returns random elements limited by <code>amount</code>。
     * 
     * @param amount of random elements
     * @return random elements
     */
    Set<V> removeRandom(int amount);
    
    /**
     * 移除and returns random element。
     *
     * @return random element
     */
    V removeRandom();

    /**
     * 返回random element。
     *
     * @return random element
     */
    V random();

    /**
     * 异步从集合中随机返回至多 {@code count} 个元素
     *
     * @param count - values amount to return
     * @return random elements
     */
    Set<V> random(int count);
    
    /**
     * Set 相关操作：Move a member from this set to the given destination set in.。
     *
     * @param destination the destination set
     * @param member the member to move
     * @return true if the element is moved, false if the element is not a
     * member of this set or no operation was performed
     */
    boolean move(String destination, V member);

    /**
     * 一次性读取全部元素。
     *
     * @return values
     */
    Set<V> readAll();

    /**
     * Set 相关操作：Union sets specified by name and write to current set.。
     * If current set already exists, it is overwritten.
     *
     * @param names - name of sets
     * @return size of union
     */
    int union(String... names);

    /**
     * Set 相关操作：Union sets specified by name with current set。
     * without current set state change.
     * 
     * @param names - name of sets
     * @return values
     */
    Set<V> readUnion(String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * <p>
     * Requires <b>Redis 8.10.0 and higher.</b>
     *
     * @param names - name of sets
     * @return amount of elements
     */
    Integer countUnion(String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * <p>
     * Requires <b>Redis 8.10.0 and higher.</b>
     *
     * @param names - name of sets
     * @param limit - sets union limit
     * @return amount of elements
     */
    Integer countUnion(int limit, String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * Returns an approximate value computed through HyperLogLog with
     * 0.81% standard error。 of an exact one.
     * <p>
     * Requires <b>Redis 8.10.0 and higher.</b>
     *
     * @param names - name of sets
     * @return approximate amount of elements
     */
    Integer countUnionApprox(String... names);

    /**
     * 统计指定集合与当前集合并集结果的元素数量。
     * Returns an approximate value computed through HyperLogLog with
     * 0.81% standard error。 of an exact one.
     * <p>
     * Requires <b>Redis 8.10.0 and higher.</b>
     *
     * @param names - name of sets
     * @param limit - sets union limit
     * @return approximate amount of elements
     */
    Integer countUnionApprox(int limit, String... names);

    /**
     * Set 相关操作：Diff sets specified by name and write to current set.。
     * If current set already exists, it is overwritten.
     *
     * @param names - name of sets
     * @return values
     */
    int diff(String... names);

    /**
     * Set 相关操作：Diff sets specified by name with current set.。
     * Without current set state change.
     * 
     * @param names - name of sets
     * @return values
     */

    Set<V> readDiff(String... names);

    /**
     * 统计指定集合与当前集合差集结果的元素数量。
     * <p>
     * Requires <b>Redis 8.10.0 and higher.</b>
     *
     * @param names - name of sets
     * @return amount of elements
     */
    Integer countDiff(String... names);

    /**
     * 统计指定集合与当前集合差集结果的元素数量。
     * <p>
     * Requires <b>Redis 8.10.0 and higher.</b>
     *
     * @param names - name of sets
     * @param limit - sets difference limit
     * @return amount of elements
     */
    Integer countDiff(int limit, String... names);

    /**
     * Set 相关操作：Intersection sets specified by name and write to current set.。
     * If current set already exists, it is overwritten.
     *
     * @param names - name of sets
     * @return size of intersection
     */
    int intersection(String... names);

    /**
     * Set 相关操作：Intersection sets specified by name with current set。
     * without current set state change.
     * 
     * @param names - name of sets
     * @return values
     */
    Set<V> readIntersection(String... names);

    /**
     * 统计指定集合与当前集合交集结果的元素数量。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param names - name of sets
     * @return amount of elements
     */
    Integer countIntersection(String... names);

    /**
     * 统计指定集合与当前集合交集结果的元素数量。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param names - name of sets
     * @param limit - sets intersection limit
     * @return amount of elements
     */
    Integer countIntersection(int limit, String... names);

    /**
     * 仅当全部元素均不在集合中时尝试添加。
     *
     * @param values - values to add
     * @return <code>true</code> if elements successfully added,
     *          otherwise <code>false</code>.
     */
    boolean tryAdd(V... values);

    /**
     * 检查if each element is contained in the specified collection.。
     * Returns contained elements.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param c - collection to check
     * @return contained elements
     */
    Set<V> containsEach(Collection<V> c);

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
    int addListener(ObjectListener listener);

}
