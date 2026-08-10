/*
 * Copyright 2016 The Netty Project
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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.channel.socket.ChannelOutputShutdownEvent;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.util.ReferenceCounted;

import java.util.ArrayDeque;
import java.util.Queue;

import static io.netty.handler.codec.http2.AbstractHttp2StreamChannel.CHANNEL_INPUT_SHUTDOWN_READ_COMPLETE_VISITOR;
import static io.netty.handler.codec.http2.AbstractHttp2StreamChannel.CHANNEL_OUTPUT_SHUTDOWN_EVENT_VISITOR;
import static io.netty.handler.codec.http2.AbstractHttp2StreamChannel.SSL_CLOSE_COMPLETION_EVENT_VISITOR;
import static io.netty.handler.codec.http2.Http2CodecUtil.HTTP_UPGRADE_STREAM_ID;
import static io.netty.handler.codec.http2.Http2Error.INTERNAL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;

/**
 * HTTP/2 多路复用编解码器（旧版一体式）：继承 {@link Http2FrameCodec}，为每条流创建子 {@link Channel}。
 *
 * <p>应用通过子 channel 收发 {@link Http2StreamFrame}，不能直接写 {@link ByteBuf}；到达 pipeline 头部的写操作
 * 由本 handler 直接处理。{@link Http2GoAwayFrame}、{@link Http2ResetFrame} 等以 user event 通知子 channel；
 * 连接级帧（如 {@link Http2SettingsFrame}）同时向下游传播。出站流通过 {@link Http2StreamChannelBootstrap} 创建。
 *
 * <h3>引用计数</h3>
 * 携带 {@link ByteBuf} 的帧在传播前会 {@link ReferenceCounted#retain()}，应用消费后须 release。
 *
 * <h3>Channel 生命周期</h3>
 * 子 channel 注册后即 active，但 HTTP/2 流需成功收发 {@link Http2HeadersFrame} 后才真正激活；
 * 超出最大并发流数时子 channel 收异常并关闭。
 *
 * <h3>可写性与流控</h3>
 * 子 channel 可写性反映流级出站窗口；仅 {@link Http2DataFrame} 受 HTTP/2 流控约束。
 *
 * @deprecated use {@link Http2FrameCodecBuilder} together with {@link Http2MultiplexHandler}.
 */
@Deprecated
public class Http2MultiplexCodec extends Http2FrameCodec {

    /** 远端发起的新入站流使用的 handler（须 {@link ChannelHandler.Sharable}） */
    private final ChannelHandler inboundStreamHandler;
    /** HTTP/1.1 升级至 HTTP/2 时 stream 1 使用的 handler（仅客户端） */
    private final ChannelHandler upgradeStreamHandler;
    /** 待触发 readComplete 的子 channel 队列，合并 flush 以降低 syscall */
    private final Queue<AbstractHttp2StreamChannel> readCompletePendingQueue =
            new MaxCapacityQueue<AbstractHttp2StreamChannel>(new ArrayDeque<AbstractHttp2StreamChannel>(8),
                    // Choose 100 which is what is used most of the times as default.
                    Http2CodecUtil.SMALLEST_MAX_CONCURRENT_STREAMS);

    /** 父 channel 是否处于 read 回调中，子 channel 据此决定是否 auto-read */
    private boolean parentReadInProgress;
    /** 子 channel 本地 id 递增计数器 */
    private int idCount;

    // 子 channel 可能跨线程访问，须 volatile
    volatile ChannelHandlerContext ctx;

    Http2MultiplexCodec(Http2ConnectionEncoder encoder,
                        Http2ConnectionDecoder decoder,
                        Http2Settings initialSettings,
                        ChannelHandler inboundStreamHandler,
                        ChannelHandler upgradeStreamHandler, boolean decoupleCloseAndGoAway, boolean flushPreface) {
        super(encoder, decoder, initialSettings, decoupleCloseAndGoAway, flushPreface);
        this.inboundStreamHandler = inboundStreamHandler;
        this.upgradeStreamHandler = upgradeStreamHandler;
    }

    @Override
    public void onHttpClientUpgrade() throws Http2Exception {
        // We must have an upgrade handler or else we can't handle the stream
        if (upgradeStreamHandler == null) {
            throw connectionError(INTERNAL_ERROR, "Client is misconfigured for upgrade requests");
        }
        // Creates the Http2Stream in the Connection.
        super.onHttpClientUpgrade();
    }

    @Override
    public final void handlerAdded0(ChannelHandlerContext ctx) throws Exception {
        if (ctx.executor() != ctx.channel().eventLoop()) {
            throw new IllegalStateException("EventExecutor must be EventLoop of Channel");
        }
        this.ctx = ctx;
    }

    @Override
    public final void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved0(ctx);

        readCompletePendingQueue.clear();
    }

    /** 将流级帧路由到对应子 channel，连接级帧继续向下游传播 */
    @Override
    final void onHttp2Frame(ChannelHandlerContext ctx, Http2Frame frame) {
        if (frame instanceof Http2StreamFrame) {
            Http2StreamFrame msg = (Http2StreamFrame) frame;
            AbstractHttp2StreamChannel channel  = (AbstractHttp2StreamChannel)
                    ((DefaultHttp2FrameStream) msg.stream()).attachment;
            channel.fireChildRead(msg);
            return;
        }
        if (frame instanceof Http2GoAwayFrame) {
            onHttp2GoAwayFrame(ctx, (Http2GoAwayFrame) frame);
        }
        // Send frames down the pipeline
        ctx.fireChannelRead(frame);
    }

    /** 流状态变为 OPEN/HALF_CLOSED 时注册子 channel，CLOSED 时通知 streamClosed */
    @Override
    final void onHttp2StreamStateChanged(ChannelHandlerContext ctx, DefaultHttp2FrameStream stream) {
        switch (stream.state()) {
            case HALF_CLOSED_LOCAL:
                if (stream.id() != HTTP_UPGRADE_STREAM_ID) {
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
                final Http2MultiplexCodecStreamChannel streamChannel;
                // We need to handle upgrades special when on the client side.
                if (stream.id() == HTTP_UPGRADE_STREAM_ID && !connection().isServer()) {
                    // Add our upgrade handler to the channel and then register the channel.
                    // The register call fires the channelActive, etc.
                    assert upgradeStreamHandler != null;
                    streamChannel = new Http2MultiplexCodecStreamChannel(stream, upgradeStreamHandler);
                    streamChannel.closeOutbound();
                } else {
                    streamChannel = new Http2MultiplexCodecStreamChannel(stream, inboundStreamHandler);
                }
                ChannelFuture future = ctx.channel().eventLoop().register(streamChannel);
                if (future.isDone()) {
                    Http2MultiplexHandler.registerDone(future);
                } else {
                    future.addListener(Http2MultiplexHandler.CHILD_CHANNEL_REGISTRATION_LISTENER);
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

    // TODO: This is most likely not the best way to expose this, need to think more about it.
    final Http2StreamChannel newOutboundStream() {
        return new Http2MultiplexCodecStreamChannel(newStream(), null);
    }

    @Override
    final void onHttp2FrameStreamException(ChannelHandlerContext ctx, Http2FrameStreamException cause) {
        Http2FrameStream stream = cause.stream();
        AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel) ((DefaultHttp2FrameStream) stream).attachment;

        try {
            channel.pipeline().fireExceptionCaught(cause.getCause());
        } finally {
            // Close with the correct error that causes this stream exception.
            // See https://github.com/netty/netty/issues/13235#issuecomment-1441994672
            channel.closeWithError(cause.error());
        }
    }

    private void onHttp2GoAwayFrame(ChannelHandlerContext ctx, final Http2GoAwayFrame goAwayFrame) {
        if (goAwayFrame.lastStreamId() == Integer.MAX_VALUE) {
            // None of the streams can have an id greater than Integer.MAX_VALUE
            return;
        }
        // Notify which streams were not processed by the remote peer and are safe to retry on another connection:
        try {
            forEachActiveStream(new Http2FrameStreamVisitor() {
                @Override
                public boolean visit(Http2FrameStream stream) {
                    final int streamId = stream.id();
                    AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel)
                            ((DefaultHttp2FrameStream) stream).attachment;
                    if (streamId > goAwayFrame.lastStreamId() && connection().local().isValidStreamId(streamId)) {
                        channel.pipeline().fireUserEventTriggered(goAwayFrame.retainedDuplicate());
                    }
                    return true;
                }
            });
        } catch (Http2Exception e) {
            ctx.fireExceptionCaught(e);
            ctx.close();
        }
    }

    /** 批量触发子 channel 的 readComplete，并合并为一次 flush */
    @Override
    public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        processPendingReadCompleteQueue();
        channelReadComplete0(ctx);
    }

    private void processPendingReadCompleteQueue() {
        parentReadInProgress = true;
        try {
            // If we have many child channel we can optimize for the case when multiple call flush() in
            // channelReadComplete(...) callbacks and only do it once as otherwise we will end-up with multiple
            // write calls on the socket which is expensive.
            for (;;) {
                AbstractHttp2StreamChannel childChannel = readCompletePendingQueue.poll();
                if (childChannel == null) {
                    break;
                }
                childChannel.fireChildReadComplete();
            }
        } finally {
            parentReadInProgress = false;
            readCompletePendingQueue.clear();
            // We always flush as this is what Http2ConnectionHandler does for now.
            flush0(ctx);
        }
    }
    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        parentReadInProgress = true;
        super.channelRead(ctx, msg);
    }

    @Override
    public final void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            // While the writability state may change during iterating of the streams we just set all of the streams
            // to writable to not affect fairness. These will be "limited" by their own watermarks in any case.
            forEachActiveStream(AbstractHttp2StreamChannel.WRITABLE_VISITOR);
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    final void onUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == ChannelInputShutdownReadComplete.INSTANCE) {
            forEachActiveStream(CHANNEL_INPUT_SHUTDOWN_READ_COMPLETE_VISITOR);
        } else if (evt == ChannelOutputShutdownEvent.INSTANCE) {
            forEachActiveStream(CHANNEL_OUTPUT_SHUTDOWN_EVENT_VISITOR);
        } else if (evt == SslCloseCompletionEvent.SUCCESS) {
            forEachActiveStream(SSL_CLOSE_COMPLETION_EVENT_VISITOR);
        }
        super.onUserEventTriggered(ctx, evt);
    }

    final void flush0(ChannelHandlerContext ctx) {
        flush(ctx);
    }

    private final class Http2MultiplexCodecStreamChannel extends AbstractHttp2StreamChannel {

        Http2MultiplexCodecStreamChannel(DefaultHttp2FrameStream stream, ChannelHandler inboundHandler) {
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

        @Override
        protected ChannelFuture write0(ChannelHandlerContext ctx, Object msg) {
            ChannelPromise promise = ctx.newPromise();
            Http2MultiplexCodec.this.write(ctx, msg, promise);
            return promise;
        }

        @Override
        protected void flush0(ChannelHandlerContext ctx) {
            Http2MultiplexCodec.this.flush0(ctx);
        }
    }
}
