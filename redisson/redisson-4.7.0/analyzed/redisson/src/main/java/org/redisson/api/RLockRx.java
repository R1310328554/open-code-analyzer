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

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * 分布式锁 RxJava3 API。
 * <p>各方法返回 {@link Single} 或 {@link Completable}。
 *
 * @author Nikita Koksharov
 */
public interface RLockRx extends RObservableRx {

    /**
     * 返回对象名称。
     *
     * @return 对象名称
     */
    String getName();
    
    /**
     * 强制解锁，不校验当前持有者。
     *
     * @return 见方法说明
     *          otherwise <code>false</code>
     */
    Single<Boolean> forceUnlock();

    /**
     * 释放锁。
     * 
     * @return 无返回值
     */
    Completable unlock();

    /**
     * 释放锁；若锁非指定 {@code threadId} 持有则抛出 {@link IllegalMonitorStateException}。
     * 锁非指定 {@code threadId} 持有时抛出。
     * 
     * @param threadId 线程 ID
     * @return 无返回值
     */
    Completable unlock(long threadId);

    /**
     * 尝试获取锁。
     * 
     * @return 见方法说明
     */
    Single<Boolean> tryLock();

    /**
     * 获取锁，必要时阻塞等待直至可用。
     * 必要时阻塞等待直至锁可用。
     *
     * @return 无返回值
     */
    Completable lock();

    /**
     * 以指定 {@code threadId} 获取锁，必要时阻塞等待。
     * 必要时阻塞等待直至锁可用。
     * 
     * @param threadId 线程 ID
     * @return 无返回值
     */
    Completable lock(long threadId);

    /**
     * 以指定 {@code leaseTime} 租约获取锁，到期自动释放。
     * 必要时阻塞等待直至锁可用。
     *
     * 锁将在 {@code leaseTime} 到期后自动释放。
     *
     * @param leaseTime 锁租约时长
     *        if it hasn't already been released by invoking <code>unlock</code>.
     *        If leaseTime is -1, hold the lock until explicitly unlocked.
     * @param unit 时间单位
     * @return 无返回值
     */
    Completable lock(long leaseTime, TimeUnit unit);

    /**
     * 以指定 {@code leaseTime} 租约和 {@code threadId} 获取锁。
     * 必要时阻塞等待直至锁可用。
     *
     * 锁将在 {@code leaseTime} 到期后自动释放。
     *
     * @param leaseTime 锁租约时长
     *        if it hasn't already been released by invoking <code>unlock</code>.
     *        If leaseTime is -1, hold the lock until explicitly unlocked.
     * @param unit 时间单位
     * @param threadId 线程 ID
     * @return 无返回值
     */
    Completable lock(long leaseTime, TimeUnit unit, long threadId);

    /**
     * 以指定 {@code threadId} 尝试获取锁。
     * 
     * @param threadId 线程 ID
     * @return 见方法说明
     */
    Single<Boolean> tryLock(long threadId);

    /**
     * 尝试获取锁。
     * 在 {@code waitTime} 内阻塞等待直至锁可用。
     *
     * @param waitTime 最大等待时长
     * @param unit 时间单位
     * @return 见方法说明
     *          otherwise <code>false</code> if lock is already set.
     */
    Single<Boolean> tryLock(long waitTime, TimeUnit unit);

    /**
     * 在 {@code waitTime} 内尝试以 {@code leaseTime} 租约获取锁。
     * 在 {@code waitTime} 内阻塞等待直至锁可用。
     *
     * 锁将在 {@code leaseTime} 到期后自动释放。
     *
     * @param waitTime 最大等待时长
     * @param leaseTime 锁租约时长
     * @param unit 时间单位
     * @return 见方法说明
     *          otherwise <code>false</code> if lock is already set.
     */
    Single<Boolean> tryLock(long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 以指定 {@code threadId} 在 {@code waitTime} 内尝试以 {@code leaseTime} 租约获取锁。
     * 在 {@code waitTime} 内阻塞等待直至锁可用。
     *
     * 锁将在 {@code leaseTime} 到期后自动释放。
     * 
     * @param threadId 线程 ID
     * @param waitTime 最大等待时长
     * @param leaseTime 锁租约时长
     * @param unit 时间单位
     * @return 见方法说明
     */
    Single<Boolean> tryLock(long waitTime, long leaseTime, TimeUnit unit, long threadId);

    /**
     * 返回当前线程对该锁的重入持有次数。
     *
     * @return 重入次数；未持锁时为 0
     */
    Single<Integer> getHoldCount();
    
    /**
     * 检查锁是否已被任意线程持有。
     *
     * @return 见方法说明
     */
    Single<Boolean> isLocked();

    /**
     * 检查锁是否由指定 {@code threadId} 的线程持有。
     *
     * @param threadId 线程 ID
     * @return 见方法说明
     *          otherwise <code>false</code>
     */
    Single<Boolean> isHeldByThread(long threadId);

    /**
     * 返回锁的剩余存活时间（毫秒）。
     *
     * @return 剩余毫秒数
     *          -2 if the lock does not exist.
     *          -1 if the lock exists but has no associated expire.
     */
    Single<Long> remainTimeToLive();
    
}
