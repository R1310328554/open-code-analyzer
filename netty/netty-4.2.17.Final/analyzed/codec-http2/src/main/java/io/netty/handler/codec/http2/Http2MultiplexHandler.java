/*
 * Copyright 2019 The Netty Project
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
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.channel.socket.ChannelOutputShutdownEvent;
import io.netty.handler.codec.http2.Http2FrameCodec.DefaultHttp2FrameStream;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ObjectUtil;

import java.util.ArrayDeque;
import java.util.Queue;
import javax.net.ssl.SSLException;

import static io.netty.handler.codec.http2.AbstractHttp2StreamChannel.CHANNEL_INPUT_SHUTDOWN_READ_COMPLETE_VISITOR;
import static io.netty.handler.codec.http2.AbstractHttp2StreamChannel.CHANNEL_OUTPUT_SHUTDOWN_EVENT_VISITOR;
import static io.netty.handler.codec.http2.AbstractHttp2StreamChannel.SSL_CLOSE_COMPLETION_EVENT_VISITOR;
import static io.netty.handler.codec.http2.Http2Error.INTERNAL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;

/**
 * HTTP/2 多路复用 handler（推荐方案）：须与 {@link Http2FrameCodec} 配合，为每条流创建 {@link Http2StreamChannel}。
 *
 * <p>流级帧路由到子 channel；{@link Http2ResetFrame}、{@link Http2PriorityFrame} 以 user event 传递（不受
 * auto-read 抑制）。{@link Http2WindowUpdateFrame} 被吞掉不向上游暴露。连接级事件继续沿 pipeline 传播。
 *
 * <h3>关闭流</h3>
 * 关闭 {@link Http2StreamChannel} 会按需发送 {@link Http2Error#CANCEL} 的 {@link Http2ResetFrame}；
 * 需指定其他错误码时传播 {@link Http2FrameStreamException} 或直接写 {@link Http2ResetFrame}。
 */
public final class Http2MultiplexHandler extends Http2ChannelDuplexHandler {

    /** 子 channel 注册完成后的回调，失败则关闭子 channel */
    static final ChannelFutureListener CHILD_CHANNEL_REGISTRATION_LISTENER = Http2MultiplexHandler::registerDone;

    /** 远端发起的新入站流 handler */
    private final ChannelHandler inboundStreamHandler;
    /** HTTP/1.1 升级流 handler（仅客户端 stream 1） */
    private final ChannelHandler upgradeStreamHandler;
    /** 待 batch 触发 readComplete 的子 channel 队列 */
    private final Queue<AbstractHttp2StreamChannel> readCompletePendingQueue =
            new MaxCapacityQueue<AbstractHttp2StreamChannel>(new ArrayDeque<AbstractHttp2StreamChannel>(8),
                    // Choose 100 which is what is used most of the times as default.
                    Http2CodecUtil.SMALLEST_MAX_CONCURRENT_STREAMS);

    private boolean parentReadInProgress;
    private int idCount;

    // Need to be volatile as accessed from within the Http2MultiplexHandlerStreamChannel in a multi-threaded fashion.
    private volatile ChannelHandlerContext ctx;

    /**
     * 创建实例，入站流使用单一 handler。
     *
     * @param inboundStreamHandler the {@link ChannelHandler} that will be added to the {@link ChannelPipeline} of
     *                             the {@link Channel}s created for new inbound streams.
     */
    public Http2MultiplexHandler(ChannelHandler inboundStreamHandler) {
        this(inboundStreamHandler, null);
    }

    /**
     * 创建实例，可单独指定 HTTP/1.1 升级流 handler。
     *
     * @param inboundStreamHandler the {@link ChannelHandler} that will be added to the {@link ChannelPipeline} of
     *                             the {@link Channel}s created for new inbound streams.
     * @param upgradeStreamHandler the {@link ChannelHandler} that will be added to the {@link ChannelPipeline} of the
     *                             upgraded {@link Channel}.
     */
    public Http2MultiplexHandler(ChannelHandler inboundStreamHandler, ChannelHandler upgradeStreamHandler) {
        this.inboundStreamHandler = ObjectUtil.checkNotNull(inboundStreamHandler, "inboundStreamHandler");
        this.upgradeStreamHandler = upgradeStreamHandler;
    }

    static void registerDone(ChannelFuture future) {
        // Handle any errors that occurred on the local thread while registering. Even though
        // failures can happen after this point, they will be handled by the channel by closing the
        // childChannel.
        if (!future.isSuccess()) {
            Channel childChannel = future.channel();
            if (childChannel.isRegistered()) {
                childChannel.close();
            } else {
                childChannel.unsafe().closeForcibly();
            }
        }
    }

    @Override
    protected void handlerAdded0(ChannelHandlerContext ctx) {
        if (ctx.executor() != ctx.channel().eventLoop()) {
            throw new IllegalStateException("EventExecutor must be EventLoop of Channel");
        }
        this.ctx = ctx;
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) {
        readCompletePendingQueue.clear();
    }

    /** 将流级帧分派到子 channel；WINDOW_UPDATE 仅用于内部流控，不暴露给应用 */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        parentReadInProgress = true;
        if (msg instanceof Http2StreamFrame) {
            if (msg instanceof Http2WindowUpdateFrame) {
                return;
            }
            Http2StreamFrame streamFrame = (Http2StreamFrame) msg;
            DefaultHttp2FrameStream s =
                    (DefaultHttp2FrameStream) streamFrame.stream();

            AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel) s.attachment;
            if (msg instanceof Http2ResetFrame || msg instanceof Http2PriorityFrame) {
                // Reset and Priority frames needs to be propagated via user events as these are not flow-controlled and
                // so must not be controlled by suppressing channel.read() on the child channel.
                channel.pipeline().fireUserEventTriggered(msg);

                // RST frames will also trigger closing of the streams which then will call
                // AbstractHttp2StreamChannel.streamClosed()
            } else {
                channel.fireChildRead(streamFrame);
            }
            return;
        }

        if (msg instanceof Http2GoAwayFrame) {
            // goaway frames will also trigger closing of the streams which then will call
            // AbstractHttp2StreamChannel.streamClosed()
            onHttp2GoAwayFrame(ctx, (Http2GoAwayFrame) msg);
        }

        // Send everything down the pipeline
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            // While the writability state may change during iterating of the streams we just set all of the streams
            // to writable to not affect fairness. These will be "limited" by their own watermarks in any case.
            forEachActiveStream(AbstractHttp2StreamChannel.WRITABLE_VISITOR);
        }

        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof Http2FrameStreamEvent) {
            Http2FrameStreamEvent event = (Http2FrameStreamEvent) evt;
            DefaultHttp2FrameStream stream = (DefaultHttp2FrameStream) event.stream();
            if (event.type() == Http2FrameStreamEvent.Type.State) {
                switch (stream.state()) {
                    case HALF_CLOSED_LOCAL:
                        if (stream.id() != Http2CodecUtil.HTTP_UPGRADE_STREAM_ID) {
                            // Ignore everything which was not caused by an upgrade
                            break;
                        }
                        // fall-through
                    case HALF_CLOSED_REMOTE:
                        // fall-through
                    case OPEN:
                        if (stream.attachment != null) {
                            // ignore if child channel was already created.
                            break;
                        }
                        final AbstractHttp2StreamChannel ch;
                        // We need to handle upgrades special when on the client side.
                        if (stream.id() == Http2CodecUtil.HTTP_UPGRADE_STREAM_ID && !isServer(ctx)) {
                            // We must have an upgrade handler or else we can't handle the stream
                            if (upgradeStreamHandler == null) {
                                throw connectionError(INTERNAL_ERROR,
                                        "Client is misconfigured for upgrade requests");
                            }
                            ch = new Http2MultiplexHandlerStreamChannel(stream, upgradeStreamHandler);
                            ch.closeOutbound();
                        } else {
                            ch = new Http2MultiplexHandlerStreamChannel(stream, inboundStreamHandler);
                        }
                        ChannelFuture future = ctx.channel().eventLoop().register(ch);
                        if (future.isDone()) {
                            registerDone(future);
                        } else {
                            future.addListener(CHILD_CHANNEL_REGISTRATION_LISTENER);
                        }
                        break;
                    case CLOSED:
                        AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel) stream.attachment;
                        if (channel != null) {
                            channel.streamClosed();
                        }
                        break;
                    default:
                        // ignore for now
                        break;
                }
            }
            return;
        }
        if (evt == ChannelInputShutdownReadComplete.INSTANCE) {
            forEachActiveStream(CHANNEL_INPUT_SHUTDOWN_READ_COMPLETE_VISITOR);
        } else if (evt == ChannelOutputShutdownEvent.INSTANCE) {
            forEachActiveStream(CHANNEL_OUTPUT_SHUTDOWN_EVENT_VISITOR);
        } else if (evt == SslCloseCompletionEvent.SUCCESS) {
            forEachActiveStream(SSL_CLOSE_COMPLETION_EVENT_VISITOR);
        }
        ctx.fireUserEventTriggered(evt);
    }

    // TODO: This is most likely not the best way to expose this, need to think more about it.
    Http2StreamChannel newOutboundStream() {
        return new Http2MultiplexHandlerStreamChannel((DefaultHttp2FrameStream) newStream(), null);
    }

    /** 单流异常路由到子 channel；{@link Http2MultiplexActiveStreamsException} 广播到所有活跃流 */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (cause instanceof Http2FrameStreamException) {
            Http2FrameStreamException exception = (Http2FrameStreamException) cause;
            Http2FrameStream stream = exception.stream();
            AbstractHttp2StreamChannel childChannel = (AbstractHttp2StreamChannel)
                    ((DefaultHttp2FrameStream) stream).attachment;
            try {
                childChannel.pipeline().fireExceptionCaught(cause.getCause());
            } finally {
                // Close with the correct error that causes this stream exception.
                // See https://github.com/netty/netty/issues/13235#issuecomment-1441994672
                childChannel.closeWithError(exception.error());
            }
            return;
        }
        if (cause instanceof Http2MultiplexActiveStreamsException) {
            // Unwrap the cause that was used to create it and fire it for all the active streams.
            fireExceptionCaughtForActiveStream(cause.getCause());
            return;
        }

        if (cause.getCause() instanceof SSLException) {
            fireExceptionCaughtForActiveStream(cause);
        }
        ctx.fireExceptionCaught(cause);
    }

    private void fireExceptionCaughtForActiveStream(final Throwable cause) throws Http2Exception {
        forEachActiveStream(new Http2FrameStreamVisitor() {
            @Override
            public boolean visit(Http2FrameStream stream) {
                AbstractHttp2StreamChannel childChannel = (AbstractHttp2StreamChannel)
                        ((DefaultHttp2FrameStream) stream).attachment;
                childChannel.pipeline().fireExceptionCaught(cause);
                return true;
            }
        });
    }

    private static boolean isServer(ChannelHandlerContext ctx) {
        return ctx.channel().parent() instanceof ServerChannel;
    }

    private void onHttp2GoAwayFrame(ChannelHandlerContext ctx, final Http2GoAwayFrame goAwayFrame) {
        if (goAwayFrame.lastStreamId() == Integer.MAX_VALUE) {
            // None of the streams can have an id greater than Integer.MAX_VALUE
            return;
        }
        // Notify which streams were not processed by the remote peer and are safe to retry on another connection:
        try {
            final boolean server = isServer(ctx);
            forEachActiveStream(new Http2FrameStreamVisitor() {
                @Override
                public boolean visit(Http2FrameStream stream) {
                    final int streamId = stream.id();
                    if (streamId > goAwayFrame.lastStreamId() && Http2CodecUtil.isStreamIdValid(streamId, server)) {
                        final AbstractHttp2StreamChannel childChannel = (AbstractHttp2StreamChannel)
                                ((DefaultHttp2FrameStream) stream).attachment;
                        childChannel.pipeline().fireUserEventTriggered(goAwayFrame.retainedDuplicate());
                    }
                    return true;
                }
            });
        } catch (Http2Exception e) {
            ctx.fireExceptionCaught(e);
            ctx.close();
        }
    }

    /** 批量触发子 channel readComplete 并合并 flush */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        processPendingReadCompleteQueue();
        ctx.fireChannelReadComplete();
    }

    private void processPendingReadCompleteQueue() {
        parentReadInProgress = true;
        // If we have many child channel we can optimize for the case when multiple call flush() in
        // channelReadComplete(...) callbacks and only do it once as otherwise we will end-up with multiple
        // write calls on the socket which is expensive.
        AbstractHttp2StreamChannel childChannel = readCompletePendingQueue.poll();
        if (childChannel != null) {
            try {
                do {
                    childChannel.fireChildReadComplete();
                    childChannel = readCompletePendingQueue.poll();
                } while (childChannel != null);
            } finally {
                parentReadInProgress = false;
                readCompletePendingQueue.clear();
                ctx.flush();
            }
        } else {
            parentReadInProgress = false;
        }
    }

    private final class Http2MultiplexHandlerStreamChannel extends AbstractHttp2StreamChannel {

        Http2MultiplexHandlerStreamChannel(DefaultHttp2FrameStream stream, ChannelHandler inboundHandler) {
            super(stream, ++idCount, inboundHandler);
        }

        @Override
        protected boolean isParentReadInProgress() {
            return parentReadInProgress;
        }

        @Override
        protected void addChannelToReadCompletePendingQueue() {
            // If there is no space left in the queue, just keep on processing everything that is already
            // stored there and try again.
            while (!readCompletePendingQueue.offer(this)) {
                processPendingReadCompleteQueue();
            }
        }

        @Override
        protected ChannelHandlerContext parentContext() {
            return ctx;
        }
    }
}
