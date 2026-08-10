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
package io.netty.handler.timeout;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.Channel.Unsafe;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Ticker;
import io.netty.util.internal.ObjectUtil;

import java.util.concurrent.TimeUnit;

/**
 * 空闲状态检测：当 {@link Channel} 在指定时间内未读、未写或两者皆未活动时触发 {@link IdleStateEvent}。
 * <p>通过 EventLoop 定时任务周期性检查 {@link #lastReadTime}/{@link #lastWriteTime}，
 * 由下游 handler 在 {@code userEventTriggered} 中处理事件（如心跳、断连）。</p>
 *
 * <h3>Supported idle states</h3>
 * <table border="1">
 * <tr>
 * <th>Property</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code readerIdleTime}</td>
 * <td>an {@link IdleStateEvent} whose state is {@link IdleState#READER_IDLE}
 *     will be triggered when no read was performed for the specified period of
 *     time.  Specify {@code 0} to disable.</td>
 * </tr>
 * <tr>
 * <td>{@code writerIdleTime}</td>
 * <td>an {@link IdleStateEvent} whose state is {@link IdleState#WRITER_IDLE}
 *     will be triggered when no write was performed for the specified period of
 *     time.  Specify {@code 0} to disable.</td>
 * </tr>
 * <tr>
 * <td>{@code allIdleTime}</td>
 * <td>an {@link IdleStateEvent} whose state is {@link IdleState#ALL_IDLE}
 *     will be triggered when neither read nor write was performed for the
 *     specified period of time.  Specify {@code 0} to disable.</td>
 * </tr>
 * </table>
 *
 * <pre>
 * // An example that sends a ping message when there is no outbound traffic
 * // for 30 seconds.  The connection is closed when there is no inbound traffic
 * // for 60 seconds.
 *
 * public class MyChannelInitializer extends {@link ChannelInitializer}&lt;{@link Channel}&gt; {
 *     {@code @Override}
 *     public void initChannel({@link Channel} channel) {
 *         channel.pipeline().addLast("idleStateHandler", new {@link IdleStateHandler}(60, 30, 0));
 *         channel.pipeline().addLast("myHandler", new MyHandler());
 *     }
 * }
 *
 * // Handler should handle the {@link IdleStateEvent} triggered by {@link IdleStateHandler}.
 * public class MyHandler extends {@link ChannelDuplexHandler} {
 *     {@code @Override}
 *     public void userEventTriggered({@link ChannelHandlerContext} ctx, {@link Object} evt) throws {@link Exception} {
 *         if (evt instanceof {@link IdleStateEvent}) {
 *             {@link IdleStateEvent} e = ({@link IdleStateEvent}) evt;
 *             if (e.state() == {@link IdleState}.READER_IDLE) {
 *                 ctx.close();
 *             } else if (e.state() == {@link IdleState}.WRITER_IDLE) {
 *                 ctx.writeAndFlush(new PingMessage());
 *             }
 *         }
 *     }
 * }
 *
 * {@link ServerBootstrap} bootstrap = ...;
 * ...
 * bootstrap.childHandler(new MyChannelInitializer());
 * ...
 * </pre>
 *
 * @see ReadTimeoutHandler
 * @see WriteTimeoutHandler
 */
public class IdleStateHandler extends ChannelDuplexHandler {
    /** 超时下限（纳秒），避免过短定时器。 */
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    /** 是否将出站缓冲变化视为“写活动”（见 {@link #hasOutputChanged}）。 */
    private final boolean observeOutput;
    /** 读空闲阈值（纳秒）；0 禁用。 */
    private final long readerIdleTimeNanos;
    /** 写空闲阈值（纳秒）；0 禁用。 */
    private final long writerIdleTimeNanos;
    /** 读写皆空闲阈值（纳秒）；0 禁用。 */
    private final long allIdleTimeNanos;

    private Ticker ticker = Ticker.systemTicker();

    private Future<?> readerIdleTimeout;
    private long lastReadTime;
    /** 是否为该 channel 首次读空闲事件。 */
    private boolean firstReaderIdleEvent = true;

    private Future<?> writerIdleTimeout;
    private long lastWriteTime;
    private boolean firstWriterIdleEvent = true;

    private Future<?> allIdleTimeout;
    private boolean firstAllIdleEvent = true;

    private byte state;
    private static final byte ST_INITIALIZED = 1;
    private static final byte ST_DESTROYED = 2;

    /** 是否正在读（channelRead 与 channelReadComplete 之间）。 */
    private boolean reading;

    /** observeOutput 模式下用于检测出站缓冲是否变化。 */
    private long lastChangeCheckTimeStamp;
    private int lastMessageHashCode;
    private long lastPendingWriteBytes;
    private long lastFlushProgress;
    // 复用同一 listener，避免每次 write 分配对象
    private final ChannelFutureListener writeListener = future -> {
        lastWriteTime = ticker.nanoTime();
        firstWriterIdleEvent = firstAllIdleEvent = true;
    };

    /**
     * 创建会触发 {@link IdleStateEvent} 的实例（时间单位为秒）。
     *
     * @param readerIdleTimeSeconds
     *        an {@link IdleStateEvent} whose state is {@link IdleState#READER_IDLE}
     *        will be triggered when no read was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param writerIdleTimeSeconds
     *        an {@link IdleStateEvent} whose state is {@link IdleState#WRITER_IDLE}
     *        will be triggered when no write was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param allIdleTimeSeconds
     *        an {@link IdleStateEvent} whose state is {@link IdleState#ALL_IDLE}
     *        will be triggered when neither read nor write was performed for
     *        the specified period of time.  Specify {@code 0} to disable.
     */
    public IdleStateHandler(
            int readerIdleTimeSeconds,
            int writerIdleTimeSeconds,
            int allIdleTimeSeconds) {

        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds,
             TimeUnit.SECONDS);
    }

    /**
     * @see #IdleStateHandler(boolean, long, long, long, TimeUnit)
     */
    public IdleStateHandler(
            long readerIdleTime, long writerIdleTime, long allIdleTime,
            TimeUnit unit) {
        this(false, readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    /**
     * 创建会触发 {@link IdleStateEvent} 的实例。
     *
     * @param observeOutput
     *        whether or not the consumption of {@code bytes} should be taken into
     *        consideration when assessing write idleness. The default is {@code false}.
     * @param readerIdleTime
     *        an {@link IdleStateEvent} whose state is {@link IdleState#READER_IDLE}
     *        will be triggered when no read was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param writerIdleTime
     *        an {@link IdleStateEvent} whose state is {@link IdleState#WRITER_IDLE}
     *        will be triggered when no write was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param allIdleTime
     *        an {@link IdleStateEvent} whose state is {@link IdleState#ALL_IDLE}
     *        will be triggered when neither read nor write was performed for
     *        the specified period of time.  Specify {@code 0} to disable.
     * @param unit
     *        the {@link TimeUnit} of {@code readerIdleTime},
     *        {@code writeIdleTime}, and {@code allIdleTime}
     */
    public IdleStateHandler(boolean observeOutput,
            long readerIdleTime, long writerIdleTime, long allIdleTime,
            TimeUnit unit) {
        ObjectUtil.checkNotNull(unit, "unit");

        this.observeOutput = observeOutput;

        if (readerIdleTime <= 0) {
            readerIdleTimeNanos = 0;
        } else {
            readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS);
        }
        if (writerIdleTime <= 0) {
            writerIdleTimeNanos = 0;
        } else {
            writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS);
        }
        if (allIdleTime <= 0) {
            allIdleTimeNanos = 0;
        } else {
            allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS);
        }
    }

    /**
     * 返回构造时设置的读空闲时间（毫秒）。
     *
     */
    public long getReaderIdleTimeInMillis() {
        return TimeUnit.NANOSECONDS.toMillis(readerIdleTimeNanos);
    }

    /**
     * 返回构造时设置的写空闲时间（毫秒）。
     *
     */
    public long getWriterIdleTimeInMillis() {
        return TimeUnit.NANOSECONDS.toMillis(writerIdleTimeNanos);
    }

    /**
     * 返回构造时设置的全局空闲时间（毫秒）。
     *
     */
    public long getAllIdleTimeInMillis() {
        return TimeUnit.NANOSECONDS.toMillis(allIdleTimeNanos);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ticker = ctx.executor().ticker();
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            // channelActive 已触发，须在此初始化定时器
            initialize(ctx);
        } else {
            // 尚未 channelActive，由 channelActive 初始化
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        destroy();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // channel 已 active 时尽早启动定时器
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 仅在本 handler 先于 channelActive 加入 pipeline 时由此初始化
        initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            reading = true;
            firstReaderIdleEvent = firstAllIdleEvent = true;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if ((readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) && reading) {
            lastReadTime = ticker.nanoTime();
            reading = false;
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 仅配置读超时时允许 void promise
        if (writerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            ctx.write(msg, promise.unvoid()).addListener(writeListener);
        } else {
            ctx.write(msg, promise);
        }
    }

    /**
     * 重置读空闲计时；<b>必须在</b> EventLoop 线程调用（本 handler 非线程安全）。
     */
    public void resetReadTimeout() {
        if (readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            lastReadTime = ticker.nanoTime();
            reading = false;
            firstReaderIdleEvent = firstAllIdleEvent = true;
        }
    }

    /**
     * 重置写空闲计时；<b>必须在</b> EventLoop 线程调用。
     */
    public void resetWriteTimeout() {
        if (writerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            lastWriteTime = ticker.nanoTime();
            firstWriterIdleEvent = firstAllIdleEvent = true;
        }
    }

    private void initialize(ChannelHandlerContext ctx) {
        // 避免 destroy 与 schedule 竞态，见 https://github.com/netty/netty/issues/143
        switch (state) {
        case 1:
        case 2:
            return;
        default:
             break;
        }

        state = ST_INITIALIZED;
        initOutputChanged(ctx);

        lastReadTime = lastWriteTime = ticker.nanoTime();
        if (readerIdleTimeNanos > 0) {
            readerIdleTimeout = schedule(ctx, new ReaderIdleTimeoutTask(ctx),
                    readerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (writerIdleTimeNanos > 0) {
            writerIdleTimeout = schedule(ctx, new WriterIdleTimeoutTask(ctx),
                    writerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (allIdleTimeNanos > 0) {
            allIdleTimeout = schedule(ctx, new AllIdleTimeoutTask(ctx),
                    allIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * 供测试覆盖的调度入口。
     */
    Future<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);
    }

    private void destroy() {
        state = ST_DESTROYED;

        if (readerIdleTimeout != null) {
            readerIdleTimeout.cancel(false);
            readerIdleTimeout = null;
        }
        if (writerIdleTimeout != null) {
            writerIdleTimeout.cancel(false);
            writerIdleTimeout = null;
        }
        if (allIdleTimeout != null) {
            allIdleTimeout.cancel(false);
            allIdleTimeout = null;
        }
    }

    /**
     * 空闲事件回调；默认通过 {@link ChannelHandlerContext#fireUserEventTriggered(Object)} 向下游传播。
     */
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * 根据空闲类型与是否首次，返回对应的 {@link IdleStateEvent} 单例或模板。
     */
    protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
        switch (state) {
            case ALL_IDLE:
                return first ? IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT : IdleStateEvent.ALL_IDLE_STATE_EVENT;
            case READER_IDLE:
                return first ? IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT : IdleStateEvent.READER_IDLE_STATE_EVENT;
            case WRITER_IDLE:
                return first ? IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT : IdleStateEvent.WRITER_IDLE_STATE_EVENT;
            default:
                throw new IllegalArgumentException("Unhandled: state=" + state + ", first=" + first);
        }
    }

    /**
     * @see #hasOutputChanged(ChannelHandlerContext, boolean)
     */
    private void initOutputChanged(ChannelHandlerContext ctx) {
        if (observeOutput) {
            Channel channel = ctx.channel();
            Unsafe unsafe = channel.unsafe();
            ChannelOutboundBuffer buf = unsafe.outboundBuffer();

            if (buf != null) {
                lastMessageHashCode = System.identityHashCode(buf.current());
                lastPendingWriteBytes = buf.totalPendingWriteBytes();
                lastFlushProgress = buf.currentProgress();
            }
        }
    }

    /**
     * 当 {@link #observeOutput} 为 true 时，检测两次调用间 {@link ChannelOutboundBuffer} 是否发生变化
     * （用于区分“已 write 但尚未 flush 到网络”与真正写空闲）。
     *
     * https://github.com/netty/netty/issues/6150
     */
    private boolean hasOutputChanged(ChannelHandlerContext ctx, boolean first) {
        if (observeOutput) {

            // write 的 promise 完成可视为消息级变化，进而推断字节级活动
            if (lastChangeCheckTimeStamp != lastWriteTime) {
                lastChangeCheckTimeStamp = lastWriteTime;

                // 非首次调用才视为变化
                if (!first) {
                    return true;
                }
            }

            Channel channel = ctx.channel();
            Unsafe unsafe = channel.unsafe();
            ChannelOutboundBuffer buf = unsafe.outboundBuffer();

            if (buf != null) {
                int messageHashCode = System.identityHashCode(buf.current());
                long pendingWriteBytes = buf.totalPendingWriteBytes();

                if (messageHashCode != lastMessageHashCode || pendingWriteBytes != lastPendingWriteBytes) {
                    lastMessageHashCode = messageHashCode;
                    lastPendingWriteBytes = pendingWriteBytes;

                    if (!first) {
                        return true;
                    }
                }

                long flushProgress = buf.currentProgress();
                if (flushProgress != lastFlushProgress) {
                    lastFlushProgress = flushProgress;
                    return !first;
                }
            }
        }

        return false;
    }

    /** 读/写/全空闲定时任务的抽象基类。 */
    private abstract static class AbstractIdleTask implements Runnable {

        private final ChannelHandlerContext ctx;

        AbstractIdleTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }

            run(ctx);
        }

        protected abstract void run(ChannelHandlerContext ctx);
    }

    private final class ReaderIdleTimeoutTask extends AbstractIdleTask {

        ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(ChannelHandlerContext ctx) {
            long nextDelay = readerIdleTimeNanos;
            if (!reading) {
                nextDelay -= ticker.nanoTime() - lastReadTime;
            }

            if (nextDelay <= 0) {
                // 读空闲：重新调度并通知下游
                readerIdleTimeout = schedule(ctx, this, readerIdleTimeNanos, TimeUnit.NANOSECONDS);

                boolean first = firstReaderIdleEvent;
                firstReaderIdleEvent = false;

                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.READER_IDLE, first);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 读活动发生在超时前：缩短下次检查间隔
                readerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class WriterIdleTimeoutTask extends AbstractIdleTask {

        WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(ChannelHandlerContext ctx) {

            long lastWriteTime = IdleStateHandler.this.lastWriteTime;
            long nextDelay = writerIdleTimeNanos - (ticker.nanoTime() - lastWriteTime);
            if (nextDelay <= 0) {
                // 写空闲：重新调度并通知（observeOutput 时可能因缓冲变化而跳过）
                writerIdleTimeout = schedule(ctx, this, writerIdleTimeNanos, TimeUnit.NANOSECONDS);

                boolean first = firstWriterIdleEvent;
                firstWriterIdleEvent = false;

                try {
                    if (hasOutputChanged(ctx, first)) {
                        return;
                    }

                    IdleStateEvent event = newIdleStateEvent(IdleState.WRITER_IDLE, first);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 写活动发生在超时前
                writerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class AllIdleTimeoutTask extends AbstractIdleTask {

        AllIdleTimeoutTask(ChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(ChannelHandlerContext ctx) {

            long nextDelay = allIdleTimeNanos;
            if (!reading) {
                nextDelay -= ticker.nanoTime() - Math.max(lastReadTime, lastWriteTime);
            }
            if (nextDelay <= 0) {
                // 读写皆空闲
                allIdleTimeout = schedule(ctx, this, allIdleTimeNanos, TimeUnit.NANOSECONDS);

                boolean first = firstAllIdleEvent;
                firstAllIdleEvent = false;

                try {
                    if (hasOutputChanged(ctx, first)) {
                        return;
                    }

                    IdleStateEvent event = newIdleStateEvent(IdleState.ALL_IDLE, first);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 读或写活动在超时前发生
                allIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }
}
