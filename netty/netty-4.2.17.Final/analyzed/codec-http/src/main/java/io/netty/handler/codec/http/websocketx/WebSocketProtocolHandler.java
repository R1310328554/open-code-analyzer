/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.http.websocketx;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.PromiseNotifier;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 协议通用 handler 基类：自动回复 Ping、可选丢弃 Pong、出站 Close 握手。
 * <p>客户端 {@link WebSocketClientProtocolHandler} 与服务端 {@link WebSocketServerProtocolHandler} 均继承此类。
 */
abstract class WebSocketProtocolHandler extends MessageToMessageDecoder<WebSocketFrame>
        implements ChannelOutboundHandler {

    private final boolean dropPongFrames;
    private final WebSocketCloseStatus closeStatus;
    private final long forceCloseTimeoutMillis;
    private ChannelPromise closeSent;

    /**
     * 默认丢弃入站 {@link PongWebSocketFrame}（通常由本 handler 响应 Ping 产生）。
     */
    WebSocketProtocolHandler() {
        this(true);
    }

    /**
     * 指定是否丢弃 Pong 帧。
     *
     * @param dropPongFrames
     *            {@code true} if {@link PongWebSocketFrame}s should be dropped
     */
    WebSocketProtocolHandler(boolean dropPongFrames) {
        this(dropPongFrames, null, 0L);
    }

    WebSocketProtocolHandler(boolean dropPongFrames,
                             WebSocketCloseStatus closeStatus,
                             long forceCloseTimeoutMillis) {
        super(WebSocketFrame.class);
        this.dropPongFrames = dropPongFrames;
        this.closeStatus = closeStatus;
        this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
    }

    /** Ping 自动回 Pong；可选丢弃 Pong；其余帧 retain 后下游处理 */
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame instanceof PingWebSocketFrame) {
            frame.content().retain();
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content()));
            readIfNeeded(ctx);
            return;
        }
        if (frame instanceof PongWebSocketFrame && dropPongFrames) {
            readIfNeeded(ctx);
            return;
        }

        out.add(frame.retain());
    }

    private static void readIfNeeded(ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
    }

    /** 若配置了 {@code closeStatus}，先写 {@link CloseWebSocketFrame} 再关闭通道 */
    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        if (closeStatus == null || !ctx.channel().isActive()) {
            ctx.close(promise);
        } else {
            if (closeSent == null) {
                write(ctx, new CloseWebSocketFrame(closeStatus, ctx.alloc()), ctx.newPromise());
            }
            flush(ctx);
            applyCloseSentTimeout(ctx);
            closeSent.addListener(future -> ctx.close(promise));
        }
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (closeSent != null) {
            ReferenceCountUtil.release(msg);
            promise.setFailure(new ClosedChannelException());
        } else if (msg instanceof CloseWebSocketFrame) {
            closeSent(promise.unvoid());
            ctx.write(msg).addListener(new PromiseNotifier<Void, ChannelFuture>(false, closeSent));
        } else {
            ctx.write(msg, promise);
        }
    }

    void closeSent(ChannelPromise promise) {
        closeSent = promise;
    }

    private void applyCloseSentTimeout(ChannelHandlerContext ctx) {
        if (closeSent.isDone() || forceCloseTimeoutMillis < 0) {
            return;
        }

        final Future<?> timeoutTask = ctx.executor().schedule(new Runnable() {
            @Override
            public void run() {
                if (!closeSent.isDone()) {
                    closeSent.tryFailure(buildHandshakeException("send close frame timed out"));
                }
            }
        }, forceCloseTimeoutMillis, TimeUnit.MILLISECONDS);

        closeSent.addListener(future -> timeoutTask.cancel(false));
    }

    /**
     * 子类可覆盖以返回客户端/服务端特定的握手异常类型。
     */
    protected WebSocketHandshakeException buildHandshakeException(String message) {
        return new WebSocketHandshakeException(message);
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
                     ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
            throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }
}
