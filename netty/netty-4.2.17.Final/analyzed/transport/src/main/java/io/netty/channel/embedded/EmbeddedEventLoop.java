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
package io.netty.channel.embedded;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.AbstractScheduledEventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.MockTicker;
import io.netty.util.concurrent.Ticker;
import io.netty.util.internal.ObjectUtil;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * {@link EmbeddedChannel} 使用的轻量 {@link EventLoop}，任务由测试代码显式 {@link #runTasks()} 驱动。
 * <p>始终 {@link #inEventLoop()} 为 {@code true}；不支持 shutdown/termination。</p>
 */
final class EmbeddedEventLoop extends AbstractScheduledEventExecutor implements EventLoop {
    /** 时间源，供调度任务与 {@link EmbeddedChannel} 时间控制使用 */
    private final Ticker ticker;

    /** 普通（非调度）任务队列 */
    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>(2);

    EmbeddedEventLoop(Ticker ticker) {
        this.ticker = ticker;
    }

    @Override
    public EventLoopGroup parent() {
        return (EventLoopGroup) super.parent();
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    /** 将任务入队，等待 {@link #runTasks()} 执行 */
    @Override
    public void execute(Runnable command) {
        tasks.add(ObjectUtil.checkNotNull(command, "command"));
    }

    /** 依次执行并清空普通任务队列 */
    void runTasks() {
        for (;;) {
            Runnable task = tasks.poll();
            if (task == null) {
                break;
            }

            task.run();
        }
    }

    /** 是否存在尚未执行的普通任务 */
    boolean hasPendingNormalTasks() {
        return !tasks.isEmpty();
    }

    /** 执行所有到期调度任务，返回下一调度任务的延迟（纳秒） */
    long runScheduledTasks() {
        long time = getCurrentTimeNanos();
        for (;;) {
            Runnable task = pollScheduledTask(time);
            if (task == null) {
                return nextScheduledTaskNano();
            }

            task.run();
        }
    }

    long nextScheduledTask() {
        return nextScheduledTaskNano();
    }

    @Override
    public Ticker ticker() {
        return ticker;
    }

    @Override
    protected long getCurrentTimeNanos() {
        return ticker.nanoTime();
    }

    @Override
    protected void cancelScheduledTasks() {
        super.cancelScheduledTasks();
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<?> terminationFuture() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return register(new DefaultChannelPromise(channel, this));
    }

    @Override
    public ChannelFuture register(ChannelPromise promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        promise.channel().unsafe().register(this, promise);
        return promise;
    }

    @Deprecated
    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        channel.unsafe().register(this, promise);
        return promise;
    }

    /** 嵌入式测试 loop 中任意线程均视为在事件循环内 */
    @Override
    public boolean inEventLoop() {
        return true;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return true;
    }

    /**
     * Ticker that implements the old {@link EmbeddedChannel} time freezing mechanics.
     * <p>支持 {@link #freezeTime()} / {@link #advance(long, TimeUnit)} 的 Mock 时间源，
     * 供嵌入式 channel 测试控制调度任务触发时机。</p>
     */
    static final class FreezableTicker implements MockTicker {
        private final Ticker unfrozen = Ticker.systemTicker();
        /**
         * When time is not {@link #timeFrozen frozen}, the base time to subtract from {@link System#nanoTime()}. When
         * time is frozen, this variable is unused.
         * <p>未冻结时从系统时间减去的基准偏移。</p>
         */
        private long startTime;
        /**
         * When time is frozen, the timestamp returned by {@link #getCurrentTimeNanos()}. When unfrozen, this is unused.
         * <p>时间冻结时返回的固定纳秒时间戳。</p>
         */
        private long frozenTimestamp;
        /**
         * Whether time is currently frozen.
         * <p>当前是否处于冻结状态。</p>
         */
        private boolean timeFrozen;

        /** 推进虚拟时钟；冻结时直接增加 frozenTimestamp，否则调整 startTime */
        @Override
        public void advance(long amount, TimeUnit unit) {
            long nanos = unit.toNanos(amount);
            if (timeFrozen) {
                frozenTimestamp += nanos;
            } else {
                // startTime is subtracted from nanoTime, so increasing the startTime will advance
                // getCurrentTimeNanos
                startTime -= nanos;
            }
        }

        @Override
        public long nanoTime() {
            if (timeFrozen) {
                return frozenTimestamp;
            }
            return unfrozen.nanoTime() - startTime;
        }

        @Override
        public void sleep(long delay, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException("Sleeping is not supported by the default ticker for " +
                    "EmbeddedEventLoop. Please use a different ticker implementation if you require sleep support.");
        }

        /** 冻结当前虚拟时间 */
        public void freezeTime() {
            if (!timeFrozen) {
                frozenTimestamp = nanoTime();
                timeFrozen = true;
            }
        }

        /** 解除冻结，使虚拟时间从冻结点继续流逝 */
        public void unfreezeTime() {
            if (timeFrozen) {
                // we want getCurrentTimeNanos to continue right where frozenTimestamp left off:
                // nanoTime = unfrozen.nanoTime - startTime = frozenTimestamp
                // then solve for startTime
                startTime = unfrozen.nanoTime() - frozenTimestamp;
                timeFrozen = false;
            }
        }
    }
}
