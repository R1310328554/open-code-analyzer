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
package org.redisson.eviction;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.redisson.command.CommandAsyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * 过期清理定时任务抽象基类，实现 Netty {@link TimerTask}。
 * <p>
 * 根据最近三次清理数量自适应调整 delay（5 秒～maxDelay），
 * 清理量大时缩短间隔，长期无过期则拉长间隔。
 *
 * @author Nikita Koksharov
 *
 */
abstract class EvictionTask implements TimerTask {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /** 最近最多保留 2 次清理数量的历史，用于自适应 delay。 */
    final Deque<Integer> sizeHistory = new ArrayDeque<>();
    /** 最小清理间隔（秒），来自 Config.minCleanUpDelay。 */
    final int minDelay;
    /** 最大清理间隔（秒），来自 Config.maxCleanUpDelay。 */
    final int maxDelay;
    /** 单次清理 key 数量阈值，来自 Config.cleanUpKeysAmount。 */
    final int keysLimit;
    
    /** 当前调度间隔（秒），运行时动态调整。 */
    int delay = 5;

    /** 异步命令执行器。 */
    final CommandAsyncExecutor executor;

    /** Netty 定时器超时句柄，cancel 时使用。 */
    volatile Timeout timeout;
    /** 任务是否已取消。 */
    volatile boolean cancelled;

    /** 从 Config 读取 min/max delay 与 keysLimit，初始 delay 设为 minDelay。 */
    EvictionTask(CommandAsyncExecutor executor) {
        super();
        this.executor = executor;
        this.minDelay = executor.getServiceManager().getCfg().getMinCleanUpDelay();
        this.maxDelay = executor.getServiceManager().getCfg().getMaxCleanUpDelay();
        this.keysLimit = executor.getServiceManager().getCfg().getCleanUpKeysAmount();
        this.delay = minDelay;
    }

    /** 按当前 delay 在 Netty 定时器中注册下一次执行。 */
    public void schedule() {
        timeout = executor.getServiceManager().newTimeout(this, delay, TimeUnit.SECONDS);
    }

    /** 取消定时器并标记任务为已取消。 */
    public void cancel() {
        timeout.cancel();
        cancelled = true;
    }

    /** 执行一次清理，返回本次删除/处理的条目数（-1 表示需立即重试）。 */
    abstract CompletionStage<Integer> execute();
    
    /** 被清理 Redis 结构的名称，用于日志。 */
    abstract String getName();
    
    /** 定时触发：执行清理并根据结果调整 delay 后重新 schedule。 */
    @Override
    public void run(Timeout timeout) {
        if (cancelled || executor.getServiceManager().isShuttingDown()) {
            return;
        }

        CompletionStage<Integer> future = execute();
        future.whenComplete((size, e) -> {
            if (e != null) {
                log.error("Unable to evict elements for '{}'", getName(), e);
                schedule();
                return;
            }

            log.debug("{} elements evicted. Object name: {}", size, getName());
            
            if (size == -1) {
                schedule();
                return;
            }

            // 连续三次清理量递减：过期压力降低，适当拉长间隔
            if (sizeHistory.size() == 2) {
                if (sizeHistory.peekFirst() > sizeHistory.peekLast()
                        && sizeHistory.peekLast() > size) {
                    delay = Math.min(maxDelay, (int) (delay*1.5));
                }

//                    if (sizeHistory.peekFirst() < sizeHistory.peekLast()
//                            && sizeHistory.peekLast() < size) {
//                        prevDelay = Math.max(minDelay, prevDelay/2);
//                    }

                // 三次数量相同：达到 keysLimit 则缩短间隔；连续为零则拉长间隔
                if (sizeHistory.peekFirst().intValue() == sizeHistory.peekLast()
                        && sizeHistory.peekLast().intValue() == size) {
                    if (size >= keysLimit) {
                        delay = Math.max(minDelay, delay/4);
                    }
                    if (size == 0) {
                        delay = Math.min(maxDelay, (int) (delay*1.5));
                    }
                }

                sizeHistory.pollFirst();
            }

            sizeHistory.add(size);
            schedule();
        });
    }

}
