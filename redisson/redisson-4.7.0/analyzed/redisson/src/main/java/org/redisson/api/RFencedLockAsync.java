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

/**
 * 支持可重入的 Redis 栅栏锁（Fenced Lock）异步 API。
 * <p>每次成功加锁递增 fencing token；受保护服务应校验新 token 不小于上次值，
 * 否则拒绝操作，以防过期锁持有者写入。
 *
 * @author Nikita Koksharov
 */
public interface RFencedLockAsync extends RLockAsync {

    /**
     * 返回当前 fencing token。
     *
     * @return fencing token
     */
    RFuture<Long> getTokenAsync();

    /**
     * 阻塞获取锁并返回递增后的 fencing token。
     * Waits if necessary until lock became available.
     *
     * @return fencing token
     */
    RFuture<Long> lockAndGetTokenAsync();

    /**
     * 带租约阻塞获取锁并返回递增后的 fencing token；租约到期自动释放。
     * returns increased fencing token.
     * Waits if necessary until lock became available.
     * <p>
     * Lock will be released automatically after defined <code>leaseTime</code> interval.
     *
     * @param leaseTime 锁租约时长
     *        if it hasn't already been released by invoking <code>unlock</code>.
     *        If leaseTime is -1, hold the lock until explicitly unlocked.
     * @param unit 时间单位
     *
     * @return fencing token
     */
    RFuture<Long> lockAndGetTokenAsync(long leaseTime, TimeUnit unit);

    /**
     * 尝试非阻塞获取锁；成功则返回递增后的 fencing token，否则返回 {@code null}。
     *
     * @return 成功则返回 fencing token，否则 null
     */
    RFuture<Long> tryLockAndGetTokenAsync();

    /**
     * 在指定等待时间内尝试获取锁并返回递增后的 fencing token。
     * Waits up to defined <code>waitTime</code> if necessary until the lock became available.
     *
     * @param waitTime 最大等待时间
     * @param unit 时间单位
     * @return 成功则返回 fencing token，否则 null
     *          otherwise <code>null</code> if lock is already set.
     */
    RFuture<Long> tryLockAndGetTokenAsync(long waitTime, TimeUnit unit);

    /**
     * 在指定等待时间内带租约尝试获取锁并返回递增后的 fencing token。
     * and returns increased fencing token.
     * Waits up to defined <code>waitTime</code> if necessary until the lock became available.
     * <p>
     * Lock will be released automatically after defined <code>leaseTime</code> interval.
     *
     * @param waitTime 最大等待时间
     * @param leaseTime 锁租约时长
     * @param unit 时间单位
     * @return 成功则返回 fencing token，否则 null
     *          otherwise <code>null</code> if lock is already set.
     */
    RFuture<Long> tryLockAndGetTokenAsync(long waitTime, long leaseTime, TimeUnit unit);

}
