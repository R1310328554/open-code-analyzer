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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent;
import io.netty.util.concurrent.Future;

import java.util.concurrent.TimeUnit;

import static io.netty.util.internal.ObjectUtil.*;

/**
 * 客户端 WebSocket 握手入站处理器：连接激活后发起 HTTP 升级，收到 101 响应后完成握手并移除自身。
 * <p>支持握手超时；通道在握手完成前关闭则失败 {@link WebSocketClientHandshakeException}。
 */
class WebSocketClientProtocolHandshakeHandler extends ChannelInboundHandlerAdapter {
    private static final long DEFAULT_HANDSHAKE_TIMEOUT_MS = 10000L;

    private final WebSocketClientHandshaker handshaker;
    private final long handshakeTimeoutMillis;
    private ChannelHandlerContext ctx;
    private ChannelPromise handshakePromise;

    /** 使用默认 10 秒握手超时 */
    WebSocketClientProtocolHandshakeHandler(WebSocketClientHandshaker handshaker) {
        this(handshaker, DEFAULT_HANDSHAKE_TIMEOUT_MS);
    }

    WebSocketClientProtocolHandshakeHandler(WebSocketClientHandshaker handshaker, long handshakeTimeoutMillis) {
        this.handshaker = handshaker;
        this.handshakeTimeoutMillis = checkPositive(handshakeTimeoutMillis, "handshakeTimeoutMillis");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        handshakePromise = ctx.newPromise();
    }

    /** 连接就绪后发起握手，成功则触发 {@link ClientHandshakeStateEvent#HANDSHAKE_ISSUED} */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
        handshaker.handshake(ctx.channel()).addListener(future -> {
            if (!future.isSuccess()) {
                handshakePromise.tryFailure(future.cause());
                ctx.fireExceptionCaught(future.cause());
            } else {
                ctx.fireUserEventTriggered(
                        ClientHandshakeStateEvent.HANDSHAKE_ISSUED);
            }
        });
        applyHandshakeTimeout();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!handshakePromise.isDone()) {
            handshakePromise.tryFailure(new WebSocketClientHandshakeException("channel closed with handshake " +
                                                                              "in progress"));
        }

        super.channelInactive(ctx);
    }

    /** 收到 {@link FullHttpResponse} 时完成握手并触发 {@link ClientHandshakeStateEvent#HANDSHAKE_COMPLETE} */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpResponse)) {
            ctx.fireChannelRead(msg);
            return;
        }

        FullHttpResponse response = (FullHttpResponse) msg;
        try {
            if (!handshaker.isHandshakeComplete()) {
                handshaker.finishHandshake(ctx.channel(), response);
                handshakePromise.trySuccess();
                ctx.fireUserEventTriggered(
                        WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE);
                ctx.pipeline().remove(this);
                return;
            }
            throw new IllegalStateException("WebSocketClientHandshaker should have been non finished yet");
        } finally {
            response.release();
        }
    }

    /** 调度握手超时任务，完成或失败时取消定时器 */
    private void applyHandshakeTimeout() {
        final ChannelPromise localHandshakePromise = handshakePromise;
        if (handshakeTimeoutMillis <= 0 || localHandshakePromise.isDone()) {
            return;
        }

        final Future<?> timeoutFuture = ctx.executor().schedule(new Runnable() {
            @Override
            public void run() {
                if (localHandshakePromise.isDone()) {
                    return;
                }

                if (localHandshakePromise.tryFailure(new WebSocketClientHandshakeException("handshake timed out"))) {
                    ctx.flush()
                       .fireUserEventTriggered(ClientHandshakeStateEvent.HANDSHAKE_TIMEOUT)
                       .close();
                }
            }
        }, handshakeTimeoutMillis, TimeUnit.MILLISECONDS);

        // 握手结束（成功或失败）时取消超时任务
        localHandshakePromise.addListener(f -> timeoutFuture.cancel(false));
    }

    /**
     * 供测试获取握手 {@link ChannelFuture}。
     *
     * @return current handshake future
     */
    ChannelFuture getHandshakeFuture() {
        return handshakePromise;
    }
}
