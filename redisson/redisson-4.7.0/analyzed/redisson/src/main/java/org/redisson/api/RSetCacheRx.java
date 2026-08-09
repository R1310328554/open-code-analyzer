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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;

/**
 * RSetCache 的 RxJava3 API。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public interface RSetCacheRx<V> extends RCollectionRx<V>, RDestroyable {

    /**
     * 返回与 {@code value} 关联的 {@link RPermitExpirableSemaphore} 实例
     * 
     * @param value 集合元素值
     * @return 可过期许可信号量
     */
    RPermitExpirableSemaphoreRx getPermitExpirableSemaphore(V value);

    /**
     * 返回与 {@code value} 关联的 {@link RSemaphore} 实例
     * 
     * @param value 集合元素值
     * @return 信号量
     */
    RSemaphoreRx getSemaphore(V value);
    
    /**
     * 返回与 {@code value} 关联的公平 {@link RLock} 实例
     * 
     * @param value 集合元素值
     * @return 分布式锁
     */
    RLockRx getFairLock(V value);
    
    /**
     * 返回与 {@code value} 关联的 {@link RReadWriteLock} 实例
     * 
     * @param value 集合元素值
     * @return 读写锁
     */
    RReadWriteLockRx getReadWriteLock(V value);
    
    /**
     * 返回与 {@code value} 关联的 {@link RLock} 实例
     * 
     * @param value 集合元素值
     * @return 分布式锁
     */
    RLockRx getLock(V value);
    
    /**
     * 存储元素并设置存活时间（TTL）；到期后自动过期。
     *
     * @param value 待添加元素
     * @param ttl 键值条目存活时间；{@code 0} 表示永不过期
     * @param unit 时间单位
     * @return 新增成功则为 true；元素已存在则为 false
     */
    Single<Boolean> add(V value, long ttl, TimeUnit unit);

    /**
     * 返回缓存中元素数量。
     * 因清理非实时，计数可能包含已过期但未删除的元素。
     *
     */
    @Override
    Single<Integer> size();

    /**
     * 一次性读取全部元素。
     *
     * @return 元素值s
     */
    Single<Set<V>> readAll();

    /**
     * 仅当全部元素均不在集合中时尝试添加。
     *
     * @param values 待添加元素
     * @return 全部添加成功则为 true，否则 false
     */
    Single<Boolean> tryAdd(V... values);

    /**
     * 请改用 {@link #addIfAbsent(Map)}。
     *
     * @param values 待添加元素
     * @param ttl 元素存活时间；{@code 0} 表示永不过期
     * @param unit 时间单位
     * @return 全部添加成功则为 true，否则 false
     */
    @Deprecated
    Single<Boolean> tryAdd(long ttl, TimeUnit unit, V... values);
    /**
     * 仅当全部元素此前均不存在时批量添加。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return <code>true</code> if elements added and <code>false</code> if not.
     */
    Single<Boolean> addIfAbsent(Map<V, Duration> objects);

    /**
     * 仅当元素此前不存在时添加到集合。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    Single<Boolean> addIfAbsent(Duration ttl, V object);

    /**
     * 仅当元素已存在时更新其 TTL 并保留在集合中。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    Single<Boolean> addIfExists(Duration ttl, V object);

    /**
     * 仅当新 TTL 小于已存在元素当前 TTL 时更新。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    Single<Boolean> addIfLess(Duration ttl, V object);

    /**
     * 仅当新 TTL 大于已存在元素当前 TTL 时更新。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    Single<Boolean> addIfGreater(Duration ttl, V object);

    /**
     * 批量添加元素；Map 键为元素、值为对应 TTL。
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量（不含已存在元素）
     */
    Single<Integer> addAll(Map<V, Duration> objects);

    /**
     * 批量添加仅当元素此前均不存在时生效的元素。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    Single<Integer> addAllIfAbsent(Map<V, Duration> objects);

    /**
     * 批量添加仅当元素已存在时更新 TTL 的条目。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    Single<Integer> addAllIfExist(Map<V, Duration> objects);

    /**
     * 批量添加仅当新 TTL 大于已存在元素当前 TTL 时生效。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    Single<Integer> addAllIfGreater(Map<V, Duration> objects);

    /**
     * 批量添加仅当新 TTL 小于已存在元素当前 TTL 时生效。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    Single<Integer> addAllIfLess(Map<V, Duration> objects);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.SetAddListener
     * @see org.redisson.api.listener.SetRemoveListener
     * @see org.redisson.api.listener.SetExpiredListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    Single<Integer> addListener(ObjectListener listener);

}
