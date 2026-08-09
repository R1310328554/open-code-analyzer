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
package org.redisson.renewal;

import org.redisson.command.CommandAsyncExecutor;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 分布式锁看门狗续期调度器（单例式引用）：
 * 分别为普通锁、快速联锁与读锁维护独立的 {@link LockTask} 实例，
 * 从配置读取 {@code lockWatchdogTimeout} 与 {@code lockWatchdogBatchSize}。
 * <p>
 * 加锁成功后调用 {@link #renewLock} 等注册续期；解锁时调用对应 cancel 方法。
 *
 * @author Nikita Koksharov
 *
 */
public final class LockRenewalScheduler {

    /** 普通互斥锁续期任务引用。 */
    private final AtomicReference<LockTask> reference = new AtomicReference<>();
    /** 快速联锁续期任务引用。 */
    private final AtomicReference<FastMultilockTask> multilockReference = new AtomicReference<>();
    /** 读写锁读锁续期任务引用。 */
    private final AtomicReference<ReadLockTask> readLockReference = new AtomicReference<>();
    /** 底层 Redis 命令执行器。 */
    private final CommandAsyncExecutor executor;

    /** 每批续期的锁数量上限。 */
    private final int batchSize;
    /** 看门狗 lease 时间（毫秒）。 */
    private final long internalLockLeaseTime;

    /** 从全局配置初始化 lease 时间与批大小。 */
    public LockRenewalScheduler(CommandAsyncExecutor executor) {
        this.executor = executor;
        this.internalLockLeaseTime = executor.getServiceManager().getCfg().getLockWatchdogTimeout();
        this.batchSize = executor.getServiceManager().getCfg().getLockWatchdogBatchSize();
    }

    /** 注册读锁续期（含 key 前缀用于超时键）。 */
    public void renewReadLock(String name, Long threadId, String lockName, String keyPrefix) {
        readLockReference.compareAndSet(null, new ReadLockTask(internalLockLeaseTime, executor, batchSize));
        ReadLockTask task = readLockReference.get();
        task.add(name, lockName, threadId, keyPrefix);
    }

    /** 注册快速联锁续期。 */
    public void renewFastMultiLock(String name, Long threadId, String lockName, Collection<String> fields) {
        multilockReference.compareAndSet(null, new FastMultilockTask(internalLockLeaseTime, executor));
        FastMultilockTask task = multilockReference.get();
        task.add(name, lockName, threadId, fields);
    }

    /** 注册普通互斥锁续期。 */
    public void renewLock(String name, Long threadId, String lockName) {
        reference.compareAndSet(null, new LockTask(internalLockLeaseTime, executor, batchSize));
        LockTask task = reference.get();
        task.add(name, lockName, threadId);
    }

    /** 取消读锁续期注册。 */
    public void cancelReadLockRenewal(String name, Long threadId) {
        ReadLockTask rtask = readLockReference.get();
        if (rtask != null) {
            rtask.cancelExpirationRenewal(name, threadId);
        }
    }

    /** 取消联锁续期注册。 */
    public void cancelFastMultilockRenewl(String name, Long threadId) {
        FastMultilockTask mtask = multilockReference.get();
        if (mtask != null) {
            mtask.cancelExpirationRenewal(name, threadId);
        }
    }

    /** 取消普通锁续期注册。 */
    public void cancelLockRenewal(String name, Long threadId) {
        LockTask task = reference.get();
        if (task != null) {
            task.cancelExpirationRenewal(name, threadId);
        }
    }

}
