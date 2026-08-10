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
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.ObjectUtil;

import java.util.concurrent.TimeUnit;

/**
 * 写超时处理器：单次 {@code write} 在指定时间内未完成时抛出 {@link WriteTimeoutException} 并关闭连接。
 * <p>为每个待完成的 {@link ChannelPromise} 调度超时任务，完成后从双向链表中移除。</p>
 *
 * <pre>
 * // The connection is closed when a write operation cannot finish in 30 seconds.
 *
 * public class MyChannelInitializer extends {@link ChannelInitializer}&lt;{@link Channel}&gt; {
 *     public void initChannel({@link Channel} channel) {
 *         channel.pipeline().addLast("writeTimeoutHandler", new {@link WriteTimeoutHandler}(30);
 *         channel.pipeline().addLast("myHandler", new MyHandler());
 *     }
 * }
 *
 * // Handler should handle the {@link WriteTimeoutException}.
 * public class MyHandler extends {@link ChannelDuplexHandler} {
 *     {@code @Override}
 *     public void exceptionCaught({@link ChannelHandlerContext} ctx, {@link Throwable} cause)
 *             throws {@link Exception} {
 *         if (cause instanceof {@link WriteTimeoutException}) {
 *             // do something
 *         } else {
 *             super.exceptionCaught(ctx, cause);
 *         }
 *     }
 * }
 *
 * {@link ServerBootstrap} bootstrap = ...;
 * ...
 * bootstrap.childHandler(new MyChannelInitializer());
 * ...
 * </pre>
 * @see ReadTimeoutHandler
 * @see IdleStateHandler
 */
public class WriteTimeoutHandler extends ChannelOutboundHandlerAdapter {
    /** 超时下限（纳秒），避免过短定时器。 */
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    /** 写超时阈值（纳秒）；0 表示禁用。 */
    private final long timeoutNanos;

    /**
     * 跟踪所有未完成写操作的超时任务（双向链表，尾指针为 {@link #lastTask}）。
     */
    private WriteTimeoutTask lastTask;

    /** 是否已因超时关闭，避免重复触发。 */
    private boolean closed;

    /**
     * 以秒为单位的写超时构造。
     *
     * @param timeoutSeconds
     *        write timeout in seconds
     */
    public WriteTimeoutHandler(int timeoutSeconds) {
        this(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 指定时间单位构造写超时。
     *
     * @param timeout
     *        write timeout
     * @param unit
     *        the {@link TimeUnit} of {@code timeout}
     */
    public WriteTimeoutHandler(long timeout, TimeUnit unit) {
        ObjectUtil.checkNotNull(unit, "unit");

        if (timeout <= 0) {
            timeoutNanos = 0;
        } else {
            timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (timeoutNanos > 0) {
            promise = promise.unvoid();
            scheduleTimeout(ctx, promise);
        }
        ctx.write(msg, promise);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        assert ctx.executor().inEventLoop();
        WriteTimeoutTask task = lastTask;
        lastTask = null;
        while (task != null) {
            assert task.ctx.executor().inEventLoop();
            task.scheduledFuture.cancel(false);
            WriteTimeoutTask prev = task.prev;
            task.prev = null;
            task.next = null;
            task = prev;
        }
    }

    /** 为本次 write 的 promise 注册超时定时器。 */
    private void scheduleTimeout(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        // 调度写超时检查
        final WriteTimeoutTask task = new WriteTimeoutTask(ctx, promise);
        task.scheduledFuture = ctx.executor().schedule(task, timeoutNanos, TimeUnit.NANOSECONDS);

        if (!task.scheduledFuture.isDone()) {
            addWriteTimeoutTask(task);

            // flush 完成时取消尚未触发的超时
            promise.addListener(task);
        }
    }

    private void addWriteTimeoutTask(WriteTimeoutTask task) {
        assert task.ctx.executor().inEventLoop();
        if (lastTask != null) {
            lastTask.next = task;
            task.prev = lastTask;
        }
        lastTask = task;
    }

    private void removeWriteTimeoutTask(WriteTimeoutTask task) {
        assert task.ctx.executor().inEventLoop();
        if (task == lastTask) {
            // 尾节点
            assert task.next == null;
            lastTask = lastTask.prev;
            if (lastTask != null) {
                lastTask.next = null;
            }
        } else if (task.prev == null && task.next == null) {
            // 已移除或未加入链表
            return;
        } else if (task.prev == null) {
            // 头节点且链表至少两个节点
            task.next.prev = null;
        } else {
            task.prev.next = task.next;
            task.next.prev = task.prev;
        }
        task.prev = null;
        task.next = null;
    }

    /**
     * 检测到写超时时回调；默认抛出 {@link WriteTimeoutException} 并关闭 channel。
     */
    protected void writeTimedOut(ChannelHandlerContext ctx) throws Exception {
        if (!closed) {
            ctx.fireExceptionCaught(WriteTimeoutException.INSTANCE);
            ctx.close();
            closed = true;
        }
    }

    /** 单次写操作的超时任务，同时作为双向链表节点。 */
    private final class WriteTimeoutTask implements Runnable, ChannelFutureListener {

        private final ChannelHandlerContext ctx;
        private final ChannelPromise promise;

        // 双向链表指针
        WriteTimeoutTask prev;
        WriteTimeoutTask next;

        Future<?> scheduledFuture;

        WriteTimeoutTask(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.ctx = ctx;
            this.promise = promise;
        }

        @Override
        public void run() {
            // 写尚未完成则触发超时；close 后 promise 会以 ClosedChannelException 失败
            // 见 https://github.com/netty/netty/issues/2159
            if (!promise.isDone()) {
                try {
                    writeTimedOut(ctx);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            }
            removeWriteTimeoutTask(this);
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            scheduledFuture.cancel(false);

            // 非 EventLoop 线程完成时需调度到 EventLoop 再改链表，避免竞态
            if (ctx.executor().inEventLoop()) {
                removeWriteTimeoutTask(this);
            } else {
                // promise 已完成，在 EventLoop 上安全移除节点
                // 修复 https://github.com/netty/netty/issues/11053
                assert promise.isDone();
                ctx.executor().execute(this);
            }
        }
    }
}
