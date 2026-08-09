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
import java.util.concurrent.locks.Lock;

/**
 * 基于 Redis 的 {@link java.util.concurrent.locks.Lock} 实现。
 * <p>支持可重入分布式锁，并提供租约（leaseTime）自动释放能力。
 *
 * @author Nikita Koksharov
 */
public interface RLock extends Lock, RLockAsync, RObservable {

    /**
     * 返回对象名称。
     *
     * @return 对象名称
     */
    String getName();
    
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
     * @throws InterruptedException - 线程被中断时
     */
    void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException;

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
     * @throws InterruptedException - 线程被中断时
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

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
     *
     */
    void lock(long leaseTime, TimeUnit unit);

    /**
     * 强制解锁，不校验当前持有者。
     *
     * @return 见方法说明
     *          otherwise <code>false</code>
     */
    boolean forceUnlock();

    /**
     * 检查锁是否已被任意线程持有。
     *
     * @return 见方法说明
     */
    boolean isLocked();

    /**
     * 检查锁是否由指定 {@code threadId} 的线程持有。
     *
     * @param threadId 线程 ID
     * @return 见方法说明
     *          otherwise <code>false</code>
     */
    boolean isHeldByThread(long threadId);

    /**
     * 检查当前线程是否持有该锁。
     *
     * @return 见方法说明
     * otherwise <code>false</code>
     */
    boolean isHeldByCurrentThread();

    /**
     * 返回当前线程对该锁的重入持有次数。
     *
     * @return 重入次数；未持锁时为 0
     */
    int getHoldCount();

    /**
     * 返回锁的剩余存活时间（毫秒）。
     *
     * @return 剩余毫秒数
     *          -2 if the lock does not exist.
     *          -1 if the lock exists but has no associated expire.
     */
    long remainTimeToLive();
    
}
