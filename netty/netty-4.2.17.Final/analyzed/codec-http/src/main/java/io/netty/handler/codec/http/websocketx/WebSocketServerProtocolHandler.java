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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;

import java.util.List;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig.DEFAULT_HANDSHAKE_TIMEOUT_MILLIS;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * WebSocket 服务端协议处理器：负责握手升级、控制帧（Close/Ping/Pong）及帧转发。
 * <p>Text/Binary 数据帧交给下游业务处理器；仅运行 WebSocket 服务时可直接使用本类。
 * 若需同时处理普通 HTTP 与 WebSocket，参见 {@code io.netty.example.http.websocketx.server.WebSocketServer}。
 * <p>握手完成后可通过 {@link ChannelInboundHandler#userEventTriggered} 接收
 * {@link HandshakeComplete} 用户事件，获取请求 URI 与协商子协议等信息。
 */
public class WebSocketServerProtocolHandler extends WebSocketProtocolHandler {

    /**
     * 服务端握手状态通知事件。
     */
    public enum ServerHandshakeStateEvent {
        /**
         * 握手已成功完成，通道已升级为 WebSocket。
         *
         * @deprecated in favor of {@link HandshakeComplete} class,
         * it provides extra information about the handshake
         */
        @Deprecated
        HANDSHAKE_COMPLETE,

        /**
         * 握手超时。
         */
        HANDSHAKE_TIMEOUT
    }

    /**
     * 握手成功完成时触发的用户事件，携带请求 URI、头与协商子协议。
     */
    public static final class HandshakeComplete {
        private final String requestUri;
        private final HttpHeaders requestHeaders;
        private final String selectedSubprotocol;

        public HandshakeComplete(String requestUri, HttpHeaders requestHeaders, String selectedSubprotocol) {
            this.requestUri = requestUri;
            this.requestHeaders = requestHeaders;
            this.selectedSubprotocol = selectedSubprotocol;
        }

        /** 返回握手请求的 URI。 */
        public String requestUri() {
            return requestUri;
        }

        /** 返回握手请求的 HTTP 头。 */
        public HttpHeaders requestHeaders() {
            return requestHeaders;
        }

        /** 返回协商选定的子协议。 */
        public String selectedSubprotocol() {
            return selectedSubprotocol;
        }
    }

    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY =
            AttributeKey.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");

    private final WebSocketServerProtocolConfig serverConfig;

    /**
     * 基于 {@link WebSocketServerProtocolConfig} 创建服务端协议处理器。
     *
     * @param serverConfig
     *            Server protocol configuration.
     */
    public WebSocketServerProtocolHandler(WebSocketServerProtocolConfig serverConfig) {
        super(checkNotNull(serverConfig, "serverConfig").dropPongFrames(),
              serverConfig.sendCloseFrame(),
              serverConfig.forceCloseTimeoutMillis()
        );
        this.serverConfig = serverConfig;
    }

    /** 使用默认握手超时创建处理器。 */
    public WebSocketServerProtocolHandler(String websocketPath) {
        this(websocketPath, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, long handshakeTimeoutMillis) {
        this(websocketPath, false, handshakeTimeoutMillis);
    }

    public WebSocketServerProtocolHandler(String websocketPath, boolean checkStartsWith) {
        this(websocketPath, checkStartsWith, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, boolean checkStartsWith, long handshakeTimeoutMillis) {
        this(websocketPath, null, false, 65536, false, checkStartsWith, handshakeTimeoutMillis);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols) {
        this(websocketPath, subprotocols, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, long handshakeTimeoutMillis) {
        this(websocketPath, subprotocols, false, handshakeTimeoutMillis);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions) {
        this(websocketPath, subprotocols, allowExtensions, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions,
                                          long handshakeTimeoutMillis) {
        this(websocketPath, subprotocols, allowExtensions, 65536, handshakeTimeoutMillis);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols,
                                          boolean allowExtensions, int maxFrameSize) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols,
                                          boolean allowExtensions, int maxFrameSize, long handshakeTimeoutMillis) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, false, handshakeTimeoutMillis);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols,
            boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch,
             DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions,
                                          int maxFrameSize, boolean allowMaskMismatch, long handshakeTimeoutMillis) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch, false,
             handshakeTimeoutMillis);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols,
            boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch, boolean checkStartsWith) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch, checkStartsWith,
             DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols,
                                          boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch,
                                          boolean checkStartsWith, long handshakeTimeoutMillis) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch, checkStartsWith, true,
             handshakeTimeoutMillis);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols,
                                          boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch,
                                          boolean checkStartsWith, boolean dropPongFrames) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch, checkStartsWith,
             dropPongFrames, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions,
                                          int maxFrameSize, boolean allowMaskMismatch, boolean checkStartsWith,
                                          boolean dropPongFrames, long handshakeTimeoutMillis) {
        this(websocketPath, subprotocols, checkStartsWith, dropPongFrames, handshakeTimeoutMillis,
            WebSocketDecoderConfig.newBuilder()
                .maxFramePayloadLength(maxFrameSize)
                .allowMaskMismatch(allowMaskMismatch)
                .allowExtensions(allowExtensions)
                .build());
    }

    public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean checkStartsWith,
                                          boolean dropPongFrames, long handshakeTimeoutMillis,
                                          WebSocketDecoderConfig decoderConfig) {
        this(WebSocketServerProtocolConfig.newBuilder()
            .websocketPath(websocketPath)
            .subprotocols(subprotocols)
            .checkStartsWith(checkStartsWith)
            .handshakeTimeoutMillis(handshakeTimeoutMillis)
            .dropPongFrames(dropPongFrames)
            .decoderConfig(decoderConfig)
            .build());
    }

    /** 安装握手处理器与可选 UTF-8 校验器到管道。 */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelPipeline cp = ctx.pipeline();
        if (cp.get(WebSocketServerProtocolHandshakeHandler.class) == null) {
            // Add the WebSocketHandshakeHandler before this one.
            cp.addBefore(ctx.name(), WebSocketServerProtocolHandshakeHandler.class.getName(),
                    new WebSocketServerProtocolHandshakeHandler(serverConfig));
        }
        if (serverConfig.decoderConfig().withUTF8Validator() && cp.get(Utf8FrameValidator.class) == null) {
            // Add the UFT8 checking before this one.
            cp.addBefore(ctx.name(), Utf8FrameValidator.class.getName(),
                    new Utf8FrameValidator(serverConfig.decoderConfig().closeOnProtocolViolation()));
        }
    }

    /** 若配置为自行处理 Close 帧，则委托握手器完成关闭握手。 */
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (serverConfig.handleCloseFrames() && frame instanceof CloseWebSocketFrame) {
            WebSocketServerHandshaker handshaker = getHandshaker(ctx.channel());
            if (handshaker != null) {
                frame.retain();
                ChannelPromise promise = ctx.newPromise();
                closeSent(promise);
                handshaker.close(ctx, (CloseWebSocketFrame) frame, promise);
            } else {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }
        super.decode(ctx, frame, out);
    }

    @Override
    protected WebSocketServerHandshakeException buildHandshakeException(String message) {
        return new WebSocketServerHandshakeException(message);
    }

    /** 握手失败时返回 400 并关闭连接。 */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof WebSocketHandshakeException) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(cause.getMessage().getBytes()));
            ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.fireExceptionCaught(cause);
            ctx.close();
        }
    }

    /** 从通道属性读取当前 {@link WebSocketServerHandshaker}。 */
    static WebSocketServerHandshaker getHandshaker(Channel channel) {
        return channel.attr(HANDSHAKER_ATTR_KEY).get();
    }

    /** 将握手器存入通道属性，供后续 Close 等操作使用。 */
    static void setHandshaker(Channel channel, WebSocketServerHandshaker handshaker) {
        channel.attr(HANDSHAKER_ATTR_KEY).set(handshaker);
    }
}
