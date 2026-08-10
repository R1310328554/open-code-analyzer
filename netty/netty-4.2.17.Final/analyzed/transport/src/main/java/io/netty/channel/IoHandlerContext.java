/*
 * Copyright 2024 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import io.netty.util.concurrent.ThreadAwareExecutor;

/**
 * The context for an {@link IoHandler} that is run by an {@link ThreadAwareExecutor}.
 * All methods  <strong>MUST</strong> be executed on the {@link ThreadAwareExecutor} thread
 * (which means {@link ThreadAwareExecutor#isExecutorThread(Thread)} (Thread)} must return {@code true}).
 * <p>由 {@link ThreadAwareExecutor} 驱动 {@link IoHandler} 时的运行上下文；
 * 所有方法必须在执行器线程上调用。</p>
 */
public interface IoHandlerContext {
    /**
     * Returns {@code true} if blocking for IO is allowed or if we should try to do a non-blocking request for IO to be
     * ready.
     * <p>若允许阻塞等待 I/O 则返回 {@code true}，否则应使用非阻塞方式探测就绪。</p>
     *
     * @return {@code true} if allowed, {@code false} otherwise.
     */
    boolean canBlock();

    /**
     * Returns the amount of time left until the scheduled task with the closest deadline should run.
     * <p>返回到最近截止定时任务的剩余纳秒数。</p>
     *
     * @param currentTimeNanos  the current nanos.
     * @return                  nanos
     */
    long delayNanos(long currentTimeNanos);

    /**
     * Returns the absolute point in time at which the next
     * closest scheduled task should run or {@code -1} if nothing is scheduled to run.
     * <p>返回下一最近定时任务的绝对截止时间（纳秒），无调度任务时返回 {@code -1}。</p>
     *
     * @return deadline.
     */
    long deadlineNanos();

    /**
     * Reports the amount of time in nanoseconds that was spent actively processing I/O events.
     * <p>
     * This metric is needed for the dynamic, utilization-based auto-scaling feature
     * in {@link MultithreadEventExecutorGroup}. The reported time
     * allows the auto-scaler to accurately measure the I/O workload of an event loop.
     * <p>
     * {@code IoHandler} implementations should measure the time spent in their event processing
     * logic and report the duration via this method. This should only include time spent
     * actively handling ready I/O events and should <strong>not</strong> include time spent blocking or
     * waiting for I/O (e.g., in an {@code epoll_wait}) call.
     * <p>
     * The default implementation of this method is a no-op. Failing to override it in an
     * {@link IoHandlerContext} that supports auto-scaling will result in the I/O utilization
     * being perceived as zero.
     * <p>上报主动处理 I/O 事件所耗纳秒数，供 {@link MultithreadEventExecutorGroup} 动态扩缩容使用。
     * 仅计就绪事件的处理时间，不含 {@code epoll_wait} 等阻塞等待。默认空实现；未覆盖时利用率视为零。</p>
     *
     * @param activeNanos The duration in nanoseconds of active, non-blocking I/O work.
     */
    default void reportActiveIoTime(long activeNanos) {
        // no-op
    }

    /**
     * Returns {@code true} if the I/O handler should measure and report its active I/O time.
     * This is used as a guard to avoid the overhead of calling {@link System#nanoTime()}
     * when the feature is not in use.
     * <p>若应测量并上报活跃 I/O 时间则返回 {@code true}，用于在未启用功能时避免
     * {@link System#nanoTime()} 开销。</p>
     *
     * @return {@code true} if active I/O time should be reported, {@code false} otherwise.
     */
    default boolean shouldReportActiveIoTime() {
        return false;
    }
}
