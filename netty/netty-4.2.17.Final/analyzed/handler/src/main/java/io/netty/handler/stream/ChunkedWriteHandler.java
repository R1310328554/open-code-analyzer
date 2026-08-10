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
package io.netty.handler.stream;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 支持异步分块写出大数据流的 {@link ChannelHandler}，避免一次性占用大量内存或 {@link OutOfMemoryError}。
 * <p>
 * 文件传输等场景若自行实现 {@link ChannelHandler} 需维护复杂状态；本类负责分块读取与写出调度。
 * <p>
 * 用法：在 pipeline 中加入 {@link ChunkedWriteHandler}，然后 {@code write} 一个 {@link ChunkedInput}：
 * <pre>
 * {@link ChannelPipeline} p = ...;
 * p.addLast("streamer", <b>new {@link ChunkedWriteHandler}()</b>);
 * p.addLast("handler", new MyHandler());
 * </pre>
 * <pre>
 * {@link Channel} ch = ...;
 * ch.write(new {@link ChunkedFile}(new File("video.mkv"));
 * </pre>
 *
 * <h3>间歇产生分块的流</h3>
 *
 * 部分 {@link ChunkedInput} 仅在特定事件或时机产生下一块，{@link ChunkedInput#readChunk} 可能暂时返回 {@code null}，
 * 此时传输会挂起；新数据就绪后须调用 {@link #resumeTransfer()} 恢复。
 */
public class ChunkedWriteHandler extends ChannelDuplexHandler {

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(ChunkedWriteHandler.class);

    /** 待写出消息队列。 */
    private Queue<PendingWrite> queue;
    /** 本 Handler 所在上下文。 */
    private volatile ChannelHandlerContext ctx;

    public ChunkedWriteHandler() {
    }

    /**
     * @deprecated use {@link #ChunkedWriteHandler()}
     */
    @Deprecated
    public ChunkedWriteHandler(int maxPendingWrites) {
        checkPositive(maxPendingWrites, "maxPendingWrites");
    }

    private void allocateQueue() {
        if (queue == null) {
            queue = new ArrayDeque<PendingWrite>();
        }
    }

    private boolean queueIsEmpty() {
        return queue == null || queue.isEmpty();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    /**
     * 继续从当前 {@link ChunkedInput} 读取并写出分块。
     */
    public void resumeTransfer() {
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            return;
        }
        if (ctx.executor().inEventLoop()) {
            resumeTransfer0(ctx);
        } else {
            // 在下一事件循环轮次恢复传输
            ctx.executor().execute(new Runnable() {

                @Override
                public void run() {
                    resumeTransfer0(ctx);
                }
            });
        }
    }

    private void resumeTransfer0(ChannelHandlerContext ctx) {
        try {
            doFlush(ctx);
        } catch (Exception e) {
            logger.warn("Unexpected exception while sending chunks.", e);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!queueIsEmpty() || msg instanceof ChunkedInput) {
            allocateQueue();
            queue.add(new PendingWrite(msg, promise));
        } else {
            ctx.write(msg, promise);
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        doFlush(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        doFlush(ctx);
        ctx.fireChannelInactive();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            // 通道可写时继续 flush
            doFlush(ctx);
        }
        ctx.fireChannelWritabilityChanged();
    }

    private void discard(Throwable cause) {
        if (queueIsEmpty()) {
            return;
        }
        for (;;) {
            PendingWrite currentWrite = queue.poll();

            if (currentWrite == null) {
                break;
            }
            Object message = currentWrite.msg;
            if (message instanceof ChunkedInput) {
                ChunkedInput<?> in = (ChunkedInput<?>) message;
                boolean endOfInput;
                long inputLength;
                try {
                    endOfInput = in.isEndOfInput();
                    inputLength = in.length();
                    closeInput(in);
                } catch (Exception e) {
                    closeInput(in);
                    currentWrite.fail(e);
                    logger.warn("ChunkedInput failed", e);
                    continue;
                }

                if (!endOfInput) {
                    if (cause == null) {
                        cause = new ClosedChannelException();
                    }
                    currentWrite.fail(cause);
                } else {
                    currentWrite.success(inputLength);
                }
            } else {
                if (cause == null) {
                    cause = new ClosedChannelException();
                }
                currentWrite.fail(cause);
            }
        }
    }

    private void doFlush(final ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        if (!channel.isActive()) {
            // 丢弃队列后仍须 flush，以便未入队的 writeAndFlush 能失败其 Promise
            discard(null);
            ctx.flush();
            return;
        }

        if (queueIsEmpty()) {
            ctx.flush();
            return;
        }

        boolean requiresFlush = true;
        ByteBufAllocator allocator = ctx.alloc();
        while (channel.isWritable()) {
            final PendingWrite currentWrite = queue.peek();

            if (currentWrite == null) {
                break;
            }

            if (currentWrite.promise.isDone()) {
                // 写已失败等情况下 Promise 已结束但分块未消费完（如 HttpChunkedInput 包装源）
                // See https://github.com/netty/netty/issues/8700.
                queue.remove();
                continue;
            }

            final Object pendingMessage = currentWrite.msg;

            if (pendingMessage instanceof ChunkedInput) {
                final ChunkedInput<?> chunks = (ChunkedInput<?>) pendingMessage;
                boolean endOfInput;
                boolean suspend;
                Object message = null;
                try {
                    message = chunks.readChunk(allocator);
                    endOfInput = chunks.isEndOfInput();
                    // 未到末尾且 readChunk 返回 null 时需挂起等待 resumeTransfer
                    suspend = message == null && !endOfInput;

                } catch (final Throwable t) {
                    queue.remove();

                    if (message != null) {
                        ReferenceCountUtil.release(message);
                    }

                    closeInput(chunks);
                    currentWrite.fail(t);
                    break;
                }

                if (suspend) {
                    // readChunk 返回 null 且未 EOF：等待更多数据，不写也不通知
                    break;
                }

                if (message == null) {
                    // 最后一块为空时写出 EMPTY_BUFFER
                    // See https://github.com/netty/netty/issues/1671
                    message = Unpooled.EMPTY_BUFFER;
                }

                if (endOfInput) {
                    // writeAndFlush 可能触发再次 touch 队列，故先 remove
                    queue.remove();
                }
                // 每块 flush 以控制内存占用
                ChannelFuture f = ctx.writeAndFlush(message);
                if (endOfInput) {
                    if (f.isDone()) {
                        handleEndOfInputFuture(f, chunks, currentWrite);
                    } else {
                        // 写完成后再关闭输入（分块可能绑定须在写出后才能释放的资源）
                        // See https://github.com/netty/netty/issues/303
                        f.addListener((ChannelFutureListener) future ->
                                handleEndOfInputFuture(future, chunks, currentWrite));
                    }
                } else {
                    final boolean resume = !channel.isWritable();
                    if (f.isDone()) {
                        handleFuture(f, chunks, currentWrite, resume);
                    } else {
                        f.addListener((ChannelFutureListener) future ->
                                handleFuture(future, chunks, currentWrite, resume));
                    }
                }
                requiresFlush = false;
            } else {
                queue.remove();
                ctx.write(pendingMessage, currentWrite.promise);
                requiresFlush = true;
            }

            if (!channel.isActive()) {
                discard(new ClosedChannelException());
                break;
            }
        }

        if (requiresFlush) {
            ctx.flush();
        }
    }

    private static void handleEndOfInputFuture(ChannelFuture future, ChunkedInput<?> input, PendingWrite currentWrite) {
        if (!future.isSuccess()) {
            closeInput(input);
            currentWrite.fail(future.cause());
        } else {
            // 关闭前读取进度与长度
            long inputProgress = input.progress();
            long inputLength = input.length();
            closeInput(input);
            currentWrite.progress(inputProgress, inputLength);
            currentWrite.success(inputLength);
        }
    }

    private void handleFuture(ChannelFuture future, ChunkedInput<?> input, PendingWrite currentWrite, boolean resume) {
        if (!future.isSuccess()) {
            closeInput(input);
            currentWrite.fail(future.cause());
        } else {
            currentWrite.progress(input.progress(), input.length());
            if (resume && future.channel().isWritable()) {
                resumeTransfer();
            }
        }
    }

    private static void closeInput(ChunkedInput<?> chunks) {
        try {
            chunks.close();
        } catch (Throwable t) {
            logger.warn("Failed to close a ChunkedInput.", t);
        }
    }

    /** 队列中待写出的消息及其 Promise。 */
    private static final class PendingWrite {
        final Object msg;
        final ChannelPromise promise;

        PendingWrite(Object msg, ChannelPromise promise) {
            this.msg = msg;
            this.promise = promise;
        }

        void fail(Throwable cause) {
            ReferenceCountUtil.release(msg);
            promise.tryFailure(cause);
        }

        void success(long total) {
            if (promise.isDone()) {
                // Promise 已结束则无需再通知进度
                return;
            }
            progress(total, total);
            promise.trySuccess();
        }

        void progress(long progress, long total) {
            if (promise instanceof ChannelProgressivePromise) {
                ((ChannelProgressivePromise) promise).tryProgress(progress, total);
            }
        }
    }
}
