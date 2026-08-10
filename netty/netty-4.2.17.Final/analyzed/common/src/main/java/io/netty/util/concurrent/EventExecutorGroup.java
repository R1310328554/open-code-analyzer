/*
 * Copyright 2012 The Netty Project
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
package io.netty.util.concurrent;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@link EventExecutorGroup} is responsible for providing the {@link EventExecutor}'s to use
 * via its {@link #next()} method. Besides this, it is also responsible for handling their
 * life-cycle and allows shutting them down in a global fashion.
 *
 * <p>EventExecutor 组：通过 {@link #next()} 分配 executor，统一管理生命周期与优雅关闭，
 * 并扩展 {@link ScheduledExecutorService} 以支持 Netty 风格的 {@link Future} 返回值。</p>
 */
public interface EventExecutorGroup extends ScheduledExecutorService, Iterable<EventExecutor> {

    /**
     * Returns {@code true} if and only if all {@link EventExecutor}s managed by this {@link EventExecutorGroup}
     * are being {@linkplain #shutdownGracefully() shut down gracefully} or was {@linkplain #isShutdown() shut down}.
     *
     * <p>是否正在或已经完成优雅关闭流程。</p>
     */
    boolean isShuttingDown();

    /**
     * Shortcut method for {@link #shutdownGracefully(long, long, TimeUnit)} with sensible default values.
     *
     * @return the {@link #terminationFuture()}
     *
     * <p>使用默认 quietPeriod/timeout 的优雅关闭快捷方法。</p>
     */
    Future<?> shutdownGracefully();

    /**
     * Signals this executor that the caller wants the executor to be shut down.  Once this method is called,
     * {@link #isShuttingDown()} starts to return {@code true}, and the executor prepares to shut itself down.
     * Unlike {@link #shutdown()}, graceful shutdown ensures that no tasks are submitted for <i>'the quiet period'</i>
     * (usually a couple seconds) before it shuts itself down.  If a task is submitted during the quiet period,
     * it is guaranteed to be accepted and the quiet period will start over.
     *
     * @param quietPeriod the quiet period as described in the documentation
     * @param timeout     the maximum amount of time to wait until the executor is {@linkplain #shutdown()}
     *                    regardless if a task was submitted during the quiet period
     * @param unit        the unit of {@code quietPeriod} and {@code timeout}
     *
     * @return the {@link #terminationFuture()}
     *
     * <p>优雅关闭：quietPeriod 内无新任务提交才开始关闭；quiet 期间若有新任务则重新计时；
     * timeout 为最长等待时间。返回 {@link #terminationFuture()} 以便等待完全终止。</p>
     */
    Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit);

    /**
     * Returns the {@link Future} which is notified when all {@link EventExecutor}s managed by this
     * {@link EventExecutorGroup} have been terminated.
     *
     * <p>所有子 executor 终止完成后会通知的 Future。</p>
     */
    Future<?> terminationFuture();

    /**
     * @deprecated {@link #shutdownGracefully(long, long, TimeUnit)} or {@link #shutdownGracefully()} instead.
     */
    @Override
    @Deprecated
    void shutdown();

    /**
     * @deprecated {@link #shutdownGracefully(long, long, TimeUnit)} or {@link #shutdownGracefully()} instead.
     */
    @Override
    @Deprecated
    List<Runnable> shutdownNow();

    /**
     * Returns one of the {@link EventExecutor}s managed by this {@link EventExecutorGroup}.
     *
     * <p>按组内策略返回下一个 {@link EventExecutor}（如轮询）。</p>
     */
    EventExecutor next();

    @Override
    Iterator<EventExecutor> iterator();

    @Override
    Future<?> submit(Runnable task);

    @Override
    <T> Future<T> submit(Runnable task, T result);

    @Override
    <T> Future<T> submit(Callable<T> task);

    /**
     * The ticker for this executor. Usually the {@link #schedule} methods will follow the
     * {@link Ticker#systemTicker() system ticker} (i.e. {@link System#nanoTime()}), but especially for testing it is
     * sometimes useful to have more control over the ticker. In that case, this method will be overridden. Code that
     * schedules tasks on this executor should use this ticker in order to stay consistent with the executor (e.g. not
     * be surprised by scheduled tasks running "early").
     *
     * @return The ticker for this scheduler
     *
     * <p>本组使用的时钟源；默认同 {@link Ticker#systemTicker()}。测试时可替换为 {@link MockTicker}，
     * 调度逻辑应与此 ticker 保持一致。</p>
     */
    default Ticker ticker() {
        return Ticker.systemTicker();
    }

    @Override
    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    @Override
    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    @Override
    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    @Override
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
