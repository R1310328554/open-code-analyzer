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

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.AsyncIteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 分布式锁看门狗续期任务的抽象基类，实现 Netty {@link TimerTask}。
 * <p>
 * 在锁租约到期前周期性调用 {@link #renew} 延长 Redis 键 TTL；
 * 集群模式下按 slot 分组批量续期，单机/主从则直接遍历锁名。
 * {@link AtomicBoolean} {@code running} 保证同一时刻仅有一个定时链在跑。
 *
 * @author Nikita Koksharov
 *
 */
abstract class RenewalTask implements TimerTask {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /** 异步命令执行器，用于发送续期 Redis 命令。 */
    final CommandAsyncExecutor executor;

    /** 续期定时链是否在运行；{@link #tryRun()} CAS 抢占。 */
    AtomicBoolean running = new AtomicBoolean();

    /** 集群模式：slot → 该槽位下待续期的原始锁名集合。 */
    final Map<Integer, Set<String>> slot2names = new ConcurrentHashMap<>();
    /** 原始锁名 → 续期条目（含线程 id 与展示用 lockName）。 */
    final Map<String, LockEntry> name2entry = new ConcurrentHashMap<>();

    /** 内部锁租约时长（毫秒），通常来自 lockWatchdogTimeout。 */
    final long internalLockLeaseTime;
    /** 单次 renew 批处理的锁名数量上限。 */
    final int chunkSize;

    /** CAS 将 running 从 false 置 true，成功表示可启动/继续定时链。 */
    boolean tryRun() {
        return running.compareAndSet(false, true);
    }

    /** 停止续期定时链（running=false）。 */
    void stop() {
        running.set(false);
    }

    /** 按 lockWatchdogTimeout/3 延迟再次调度自身；running 为 false 则不再调度。 */
    public void schedule() {
        if (!running.get()) {
            return;
        }

        long internalLockLeaseTime = executor.getServiceManager().getCfg().getLockWatchdogTimeout();
        executor.getServiceManager().newTimeout(this, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);
    }

    RenewalTask(long internalLockLeaseTime,
                    CommandAsyncExecutor executor, int chunkSize) {
        this.executor = executor;
        this.internalLockLeaseTime = internalLockLeaseTime;
        this.chunkSize = chunkSize;
    }

    /** 执行一轮续期：无条目则立即完成；集群走 slot 迭代，否则直接 renew 全部锁名。 */
    final CompletionStage<Void> execute() {
        if (name2entry.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        if (!executor.getServiceManager().isClusterSetup()) {
            return renew(name2entry.keySet().iterator(), chunkSize);
        }

        return renewSlots(slot2names.values().iterator(), chunkSize);
    }

    private CompletionStage<Void> renewSlots(Iterator<Set<String>> iter, int chunkSize) {
        return AsyncIteratorUtils.forEachAsync(iter,
                names -> renew(names.iterator(), chunkSize));
    }

    /** 子类实现：对 iter 中锁名分批发送续期命令。 */
    abstract CompletionStage<Void> renew(Iterator<String> iter, int chunkSize);

    /** 解锁时移除指定线程的续期条目；无剩余线程则从 slot/name 映射中删除并可能 stop 定时链。 */
    void cancelExpirationRenewal(String name, Long threadId) {
        LockEntry newTask = name2entry.compute(name, (unused, task) -> {
            if (task == null) {
                return null;
            }

            if (threadId != null) {
                task.removeThreadId(threadId);
            }

            // threadId 为 null 或该锁已无持有线程：彻底移除续期条目
            if (threadId == null || task.hasNoThreads()) {
                if (executor.getServiceManager().isClusterSetup()) {
                    int slot = executor.getConnectionManager().calcSlot(name);
                    slot2names.computeIfPresent(slot, (k, v) -> {
                        v.remove(name);
                        if (v.isEmpty()) {
                            return null;
                        }
                        return v;
                    });
                }
                return null;
            }
            return task;
        });

        if (newTask == null) {
            if (!name2entry.isEmpty()) {
                return;
            }

            stop();

            if (!name2entry.isEmpty() && tryRun()) {
                schedule();
            }
        }
    }

    /** 加锁成功后注册续期：合并同 rawName 的多线程，首次加锁时 tryRun 并 schedule。 */
    final void add(String rawName, String lockName, long threadId, LockEntry entry) {
        name2entry.compute(rawName, (k, oldEntry) -> {
            addSlotName(rawName);

            LockEntry returnEntry = entry;
            if (oldEntry != null) {
                oldEntry.addThreadId(threadId, lockName);
                returnEntry = oldEntry;
            } else {
                if (tryRun()) {
                    schedule();
                }
            }
            return returnEntry;
        });

    }

    /** 集群模式下将 rawName 登记到对应 slot 的 name 集合。 */
    void addSlotName(String rawName) {
        if (!executor.getServiceManager().isClusterSetup()) {
            return;
        }

        int slot = executor.getConnectionManager().calcSlot(rawName);
        slot2names.compute(slot, (k, v) -> {
            if (v == null) {
                v = Collections.newSetFromMap(new ConcurrentHashMap<>());
            }
            v.add(rawName);
            return v;
        });
    }
    
    /** TimerTask 回调：shutdown 中则跳过；execute 完成后无论成败都 schedule 下一轮（失败打日志）。 */
    @Override
    public void run(Timeout timeout) {
        if (executor.getServiceManager().isShuttingDown()) {
            return;
        }

        CompletionStage<Void> future = execute();
        future.whenComplete((result, e) -> {
            if (e != null) {
                log.error("Can't update locks {} expiration", name2entry.keySet(), e);
                schedule();
                return;
            }

            schedule();
        });
    }

}
