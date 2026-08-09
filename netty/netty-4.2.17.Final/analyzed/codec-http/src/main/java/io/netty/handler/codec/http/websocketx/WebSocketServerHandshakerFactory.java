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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.ObjectUtil;

/**
 * 根据客户端请求自动检测 WebSocket 协议版本，并创建对应 {@link WebSocketServerHandshaker} 的工厂。
 */
public class WebSocketServerHandshakerFactory {

    private final String webSocketURL;

    private final String subprotocols;

    private final WebSocketDecoderConfig decoderConfig;

    /**
     * 创建握手器工厂（默认最大帧载荷 65536 字节）。
     *
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath".
     *            Subsequent web socket frames will be sent to this URL.
     * @param subprotocols
     *            CSV of supported protocols. Null if sub protocols not supported.
     * @param allowExtensions
     *            Allow extensions to be used in the reserved bits of the web socket frame
     */
    public WebSocketServerHandshakerFactory(
            String webSocketURL, String subprotocols, boolean allowExtensions) {
        this(webSocketURL, subprotocols, allowExtensions, 65536);
    }

    /**
     * 创建握手器工厂（默认最大帧载荷 65536 字节）。
     *
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath".
     *            Subsequent web socket frames will be sent to this URL.
     * @param subprotocols
     *            CSV of supported protocols. Null if sub protocols not supported.
     * @param allowExtensions
     *            Allow extensions to be used in the reserved bits of the web socket frame
     * @param maxFramePayloadLength
     *            Maximum allowable frame payload length. Setting this value to your application's
     *            requirement may reduce denial of service attacks using long data frames.
     */
    public WebSocketServerHandshakerFactory(
            String webSocketURL, String subprotocols, boolean allowExtensions,
            int maxFramePayloadLength) {
        this(webSocketURL, subprotocols, allowExtensions, maxFramePayloadLength, false);
    }

    /**
     * 创建握手器工厂（默认最大帧载荷 65536 字节）。
     *
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath".
     *            Subsequent web socket frames will be sent to this URL.
     * @param subprotocols
     *            CSV of supported protocols. Null if sub protocols not supported.
     * @param allowExtensions
     *            Allow extensions to be used in the reserved bits of the web socket frame
     * @param maxFramePayloadLength
     *            Maximum allowable frame payload length. Setting this value to your application's
     *            requirement may reduce denial of service attacks using long data frames.
     * @param allowMaskMismatch
     *            When set to true, frames which are not masked properly according to the standard will still be
     *            accepted.
     */
    public WebSocketServerHandshakerFactory(
            String webSocketURL, String subprotocols, boolean allowExtensions,
            int maxFramePayloadLength, boolean allowMaskMismatch) {
        this(webSocketURL, subprotocols, WebSocketDecoderConfig.newBuilder()
            .allowExtensions(allowExtensions)
            .maxFramePayloadLength(maxFramePayloadLength)
            .allowMaskMismatch(allowMaskMismatch)
            .build());
    }

    /**
     * 使用自定义帧解码配置创建握手器工厂。
     *
     * @param webSocketURL
            String webSocketURL, String subprotocols, WebSocketDecoderConfig decoderConfig) {
        this.webSocketURL = webSocketURL;
        this.subprotocols = subprotocols;
        this.decoderConfig = ObjectUtil.checkNotNull(decoderConfig, "decoderConfig");
    }

    /**
     * 根据 HTTP 升级请求创建对应版本的 {@link WebSocketServerHandshaker}。
     *
     * @return A new WebSocketServerHandshaker for the requested web socket version. Null if web
     *         socket version is not supported.
     */
    public WebSocketServerHandshaker newHandshaker(HttpRequest req) {
        return resolveHandshaker0(req, webSocketURL, subprotocols, decoderConfig);
    }

    /**
     * 从 HTTP 请求头解析客户端 WebSocket 版本并创建握手器（静态方法，无需工厂实例）。
     * 行为与 {@link #newHandshaker(HttpRequest)} 相同。
     *
     * @param req
     *            The HTTP request that came from the client to upgrade the protocol to WebSocket.
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath".
     *            Subsequent web socket frames will be sent to this URL.
     * @param subprotocols
     *            CSV of supported protocols. Null if sub protocols not supported.
     * @param decoderConfig
     *            Frames decoder options.
     * @return A new {@link WebSocketServerHandshaker} for the requested web socket version. Null if web
     *         socket version is not supported.
     * @throws NullPointerException if the {@code decoderConfig} is {@code null}.
     */
    public static WebSocketServerHandshaker resolveHandshaker(HttpRequest req, String webSocketURL, String subprotocols,
                                                              WebSocketDecoderConfig decoderConfig) {
        ObjectUtil.checkNotNull(decoderConfig, "decoderConfig");
        return resolveHandshaker0(req, webSocketURL, subprotocols, decoderConfig);
    }

    private static WebSocketServerHandshaker resolveHandshaker0(HttpRequest req,
                                                                String webSocketURL,
                                                                String subprotocols,
                                                                WebSocketDecoderConfig decoderConfig) {
        CharSequence version = req.headers().get(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
        if (version != null) {
            if (version.equals(WebSocketVersion.V13.toHttpHeaderValue())) {
                // Version 13 of the wire protocol - RFC 6455 (version 17 of the draft hybi specification).
                return new WebSocketServerHandshaker13(
                        webSocketURL, subprotocols, decoderConfig);
            } else if (version.equals(WebSocketVersion.V08.toHttpHeaderValue())) {
                // Version 8 of the wire protocol - version 10 of the draft hybi specification.
                return new WebSocketServerHandshaker08(
                        webSocketURL, subprotocols, decoderConfig);
            } else if (version.equals(WebSocketVersion.V07.toHttpHeaderValue())) {
                // Version 8 of the wire protocol - version 07 of the draft hybi specification.
                return new WebSocketServerHandshaker07(
                        webSocketURL, subprotocols, decoderConfig);
            } else {
                return null;
            }
        } else {
            // Assume version 00 where version header was not specified
            return new WebSocketServerHandshaker00(webSocketURL, subprotocols, decoderConfig);
        }
    }

    /**
     * @deprecated use {@link #sendUnsupportedVersionResponse(Channel)}
     */
    @Deprecated
    public static void sendUnsupportedWebSocketVersionResponse(Channel channel) {
        sendUnsupportedVersionResponse(channel);
    }

    /**
     * 向客户端返回 426 Upgrade Required，提示支持的 WebSocket 版本。
     */
    public static ChannelFuture sendUnsupportedVersionResponse(Channel channel) {
        return sendUnsupportedVersionResponse(channel, channel.newPromise());
    }

    /**
     * 向客户端返回 426 Upgrade Required，并携带 {@link WebSocketVersion#V13} 版本提示。
     */
    public static ChannelFuture sendUnsupportedVersionResponse(Channel channel, ChannelPromise promise) {
        HttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UPGRADE_REQUIRED, channel.alloc().buffer(0));
        res.headers().set(HttpHeaderNames.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
        HttpUtil.setContentLength(res, 0);
        return channel.writeAndFlush(res, promise);
    }
}
