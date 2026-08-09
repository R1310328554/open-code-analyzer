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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;

import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * WebSocket 客户端握手基类，负责发起 Upgrade 请求、校验响应并在 pipeline 中切换编解码器。
 */
public abstract class WebSocketClientHandshaker {

    private static final String HTTP_SCHEME_PREFIX = HttpScheme.HTTP + "://";
    private static final String HTTPS_SCHEME_PREFIX = HttpScheme.HTTPS + "://";
    protected static final int DEFAULT_FORCE_CLOSE_TIMEOUT_MILLIS = 10000;

    private final URI uri;

    private final WebSocketVersion version;

    private volatile boolean handshakeComplete;

    private volatile long forceCloseTimeoutMillis = DEFAULT_FORCE_CLOSE_TIMEOUT_MILLIS;

    private volatile int forceCloseInit;

    private static final AtomicIntegerFieldUpdater<WebSocketClientHandshaker> FORCE_CLOSE_INIT_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(WebSocketClientHandshaker.class, "forceCloseInit");

    private volatile boolean forceCloseComplete;

    private final String expectedSubprotocol;

    private volatile String actualSubprotocol;

    protected final HttpHeaders customHeaders;

    private final int maxFramePayloadLength;

    private final boolean absoluteUpgradeUrl;

    protected final boolean generateOriginHeader;

    /**
     * 基类构造器。
     *
     * @param uri
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     */
    protected WebSocketClientHandshaker(URI uri, WebSocketVersion version, String subprotocol,
                                        HttpHeaders customHeaders, int maxFramePayloadLength) {
        this(uri, version, subprotocol, customHeaders, maxFramePayloadLength, DEFAULT_FORCE_CLOSE_TIMEOUT_MILLIS);
    }

    /**
     * 基类构造器。
     *
     * @param uri
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     * @param forceCloseTimeoutMillis
     *            Close the connection if it was not closed by the server after timeout specified
     */
    protected WebSocketClientHandshaker(URI uri, WebSocketVersion version, String subprotocol,
                                        HttpHeaders customHeaders, int maxFramePayloadLength,
                                        long forceCloseTimeoutMillis) {
        this(uri, version, subprotocol, customHeaders, maxFramePayloadLength, forceCloseTimeoutMillis, false);
    }

    /**
     * 基类构造器。
     *
     * @param uri
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     * @param forceCloseTimeoutMillis
     *            Close the connection if it was not closed by the server after timeout specified
     * @param  absoluteUpgradeUrl
     *            Use an absolute url for the Upgrade request, typically when connecting through an HTTP proxy over
     *            clear HTTP
     */
    protected WebSocketClientHandshaker(URI uri, WebSocketVersion version, String subprotocol,
                                        HttpHeaders customHeaders, int maxFramePayloadLength,
                                        long forceCloseTimeoutMillis, boolean absoluteUpgradeUrl) {
        this(uri, version, subprotocol, customHeaders, maxFramePayloadLength, forceCloseTimeoutMillis,
                absoluteUpgradeUrl, true);
    }

    /**
     * 基类构造器。
     *
     * @param uri
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     * @param forceCloseTimeoutMillis
     *            Close the connection if it was not closed by the server after timeout specified
     * @param  absoluteUpgradeUrl
     *            Use an absolute url for the Upgrade request, typically when connecting through an HTTP proxy over
     *            clear HTTP
     * @param generateOriginHeader
     *            Allows to generate the `Origin`|`Sec-WebSocket-Origin` header value for handshake request
     *            according to the given webSocketURL
     */
    protected WebSocketClientHandshaker(URI uri, WebSocketVersion version, String subprotocol,
            HttpHeaders customHeaders, int maxFramePayloadLength,
            long forceCloseTimeoutMillis, boolean absoluteUpgradeUrl, boolean generateOriginHeader) {
        this.uri = uri;
        this.version = version;
        expectedSubprotocol = subprotocol;
        this.customHeaders = customHeaders;
        this.maxFramePayloadLength = maxFramePayloadLength;
        this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
        this.absoluteUpgradeUrl = absoluteUpgradeUrl;
        this.generateOriginHeader = generateOriginHeader;
    }

    /**
     * 返回 WebSocket 连接 URI，例如 {@code ws://myhost.com/path}。
     */
    public URI uri() {
        return uri;
    }

    /** 使用的 WebSocket 协议版本。 */
    public WebSocketVersion version() {
        return version;
    }

    /** 返回帧载荷的最大允许长度。 */
    public int maxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    /** 握手是否已完成。 */
    public boolean isHandshakeComplete() {
        return handshakeComplete;
    }

    private void setHandshakeComplete() {
        handshakeComplete = true;
    }

    /** 返回构造时请求的 subprotocol CSV 字符串。 */
    public String expectedSubprotocol() {
        return expectedSubprotocol;
    }

    /**
     * 返回服务端确认的 subprotocol；握手完成前不可用。
     * 未请求或未确认时为 null。
     */
    public String actualSubprotocol() {
        return actualSubprotocol;
    }

    private void setActualSubprotocol(String actualSubprotocol) {
        this.actualSubprotocol = actualSubprotocol;
    }

    public long forceCloseTimeoutMillis() {
        return forceCloseTimeoutMillis;
    }

    /**
     * 超时后是否因强制关闭而结束（仅供测试）。
     */
    protected boolean isForceCloseComplete() {
        return forceCloseComplete;
    }

    /**
     * 设置服务端未主动关闭连接时的强制关闭超时（毫秒）。
     *
     * @param forceCloseTimeoutMillis
     *            Close the connection if it was not closed by the server after timeout specified
     */
    public WebSocketClientHandshaker setForceCloseTimeoutMillis(long forceCloseTimeoutMillis) {
        this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
        return this;
    }

    /**
     * 发起 WebSocket Opening Handshake。
     *
     * @param channel
     *            Channel
     */
    public ChannelFuture handshake(Channel channel) {
        ObjectUtil.checkNotNull(channel, "channel");
        return handshake(channel, channel.newPromise());
    }

    /**
     * 发起 Opening Handshake 并在发送完成后通知 {@link ChannelPromise}。
     *
     * @param channel
     *            Channel
     * @param promise
     *            the {@link ChannelPromise} to be notified when the opening handshake is sent
     */
    public final ChannelFuture handshake(Channel channel, final ChannelPromise promise) {
        final ChannelPipeline pipeline = channel.pipeline();
        HttpResponseDecoder decoder = pipeline.get(HttpResponseDecoder.class);
        if (decoder == null) {
            HttpClientCodec codec = pipeline.get(HttpClientCodec.class);
            if (codec == null) {
               promise.setFailure(new IllegalStateException("ChannelPipeline does not contain " +
                       "an HttpResponseDecoder or HttpClientCodec"));
               return promise;
            }
        }

        if (uri.getHost() == null) {
            if (customHeaders == null || !customHeaders.contains(HttpHeaderNames.HOST)) {
                promise.setFailure(new IllegalArgumentException("Cannot generate the 'host' header value," +
                        " webSocketURI should contain host or passed through customHeaders"));
                return promise;
            }

            if (generateOriginHeader && !customHeaders.contains(HttpHeaderNames.ORIGIN)) {
                final String originName;
                if (version == WebSocketVersion.V07 || version == WebSocketVersion.V08) {
                    originName = HttpHeaderNames.SEC_WEBSOCKET_ORIGIN.toString();
                } else {
                    originName = HttpHeaderNames.ORIGIN.toString();
                }

                promise.setFailure(new IllegalArgumentException("Cannot generate the '" + originName + "' header" +
                        " value, webSocketURI should contain host or disable generateOriginHeader or pass value" +
                        " through customHeaders"));
                return promise;
            }
        }

        FullHttpRequest request = newHandshakeRequest();

        channel.writeAndFlush(request).addListener(future -> {
            if (future.isSuccess()) {
                ChannelHandlerContext ctx = pipeline.context(HttpRequestEncoder.class);
                if (ctx == null) {
                    ctx = pipeline.context(HttpClientCodec.class);
                }
                if (ctx == null) {
                    promise.setFailure(new IllegalStateException("ChannelPipeline does not contain " +
                            "an HttpRequestEncoder or HttpClientCodec"));
                    return;
                }
                pipeline.addAfter(ctx.name(), "ws-encoder", newWebSocketEncoder());

                promise.setSuccess();
            } else {
                promise.setFailure(future.cause());
            }
        });
        return promise;
    }

    /**
     * 构造并返回用于握手的 {@link FullHttpRequest}。
     */
    protected abstract FullHttpRequest newHandshakeRequest();

    /**
     * 校验响应并完成 Opening Handshake（由 {@link #handshake} 发起）。
     *
     * @param channel
     *            Channel
     * @param response
     *            HTTP response containing the closing handshake details
     */
    public final void finishHandshake(Channel channel, FullHttpResponse response) {
        verify(response);

        // 校验服务端返回的 subprotocol
        String receivedProtocol = response.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
        receivedProtocol = receivedProtocol != null ? receivedProtocol.trim() : null;
        String expectedProtocol = expectedSubprotocol != null ? expectedSubprotocol : "";
        boolean protocolValid = false;

        if (expectedProtocol.isEmpty() && receivedProtocol == null) {
            // 未请求 subprotocol 且服务端未返回
            protocolValid = true;
            setActualSubprotocol(expectedSubprotocol); // null or "" - we echo what the user requested
        } else if (!expectedProtocol.isEmpty() && receivedProtocol != null && !receivedProtocol.isEmpty()) {
            // 请求了 subprotocol 且服务端有回应，逐项匹配
            for (String protocol : expectedProtocol.split(",")) {
                if (protocol.trim().equals(receivedProtocol)) {
                    protocolValid = true;
                    setActualSubprotocol(receivedProtocol);
                    break;
                }
            }
        } // else mixed cases - which are all errors

        if (!protocolValid) {
            throw new WebSocketClientHandshakeException(String.format(
                    "Invalid subprotocol. Actual: %s. Expected one of: %s",
                    receivedProtocol, expectedSubprotocol), response);
        }

        setHandshakeComplete();

        final ChannelPipeline p = channel.pipeline();
        // 移除 HTTP 解压器（握手后不再需要）
        HttpContentDecompressor decompressor = p.get(HttpContentDecompressor.class);
        if (decompressor != null) {
            p.remove(decompressor);
        }

        // 移除聚合器（若存在）
        HttpObjectAggregator aggregator = p.get(HttpObjectAggregator.class);
        if (aggregator != null) {
            p.remove(aggregator);
        }

        ChannelHandlerContext ctx = p.context(HttpResponseDecoder.class);
        if (ctx == null) {
            ctx = p.context(HttpClientCodec.class);
            if (ctx == null) {
                throw new IllegalStateException("ChannelPipeline does not contain " +
                        "an HttpRequestEncoder or HttpClientCodec");
            }
            final HttpClientCodec codec =  (HttpClientCodec) ctx.handler();
            // 移除 codec 出站部分，用户此后可直接写 WebSocketFrame
            codec.removeOutboundHandler();

            p.addAfter(ctx.name(), "ws-decoder", newWebsocketDecoder());

            // 延迟移除 decoder，便于用户先配置 pipeline 处理 WebSocketFrame（netty#4533）
            channel.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    p.remove(codec);
                }
            });
        } else {
            if (p.get(HttpRequestEncoder.class) != null) {
                // 移除 codec 出站部分，用户此后可直接写 WebSocketFrame
                p.remove(HttpRequestEncoder.class);
            }
            final ChannelHandlerContext context = ctx;
            p.addAfter(context.name(), "ws-decoder", newWebsocketDecoder());

            // 延迟移除 decoder，便于用户先配置 pipeline 处理 WebSocketFrame（netty#4533）
            channel.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    p.remove(context.handler());
                }
            });
        }
    }

    /**
     * 处理 Opening Handshake 响应（异步聚合 body）。
     *
     * @param channel
     *            Channel
     * @param response
     *            HTTP response containing the closing handshake details
     * @return future
     *            the {@link ChannelFuture} which is notified once the handshake completes.
     */
    public final ChannelFuture processHandshake(final Channel channel, HttpResponse response) {
        return processHandshake(channel, response, channel.newPromise());
    }

    /**
     * 处理 Opening Handshake 响应并通过 {@link ChannelPromise} 通知结果。
     *
     * @param channel
     *            Channel
     * @param response
     *            HTTP response containing the closing handshake details
     * @param promise
     *            the {@link ChannelPromise} to notify once the handshake completes.
     * @return future
     *            the {@link ChannelFuture} which is notified once the handshake completes.
     */
    public final ChannelFuture processHandshake(final Channel channel, HttpResponse response,
                                                final ChannelPromise promise) {
        if (response instanceof FullHttpResponse) {
            try {
                finishHandshake(channel, (FullHttpResponse) response);
                promise.setSuccess();
            } catch (Throwable cause) {
                promise.setFailure(cause);
            }
        } else {
            ChannelPipeline p = channel.pipeline();
            ChannelHandlerContext ctx = p.context(HttpResponseDecoder.class);
            if (ctx == null) {
                ctx = p.context(HttpClientCodec.class);
                if (ctx == null) {
                    return promise.setFailure(new IllegalStateException("ChannelPipeline does not contain " +
                            "an HttpResponseDecoder or HttpClientCodec"));
                }
            }

            String aggregatorCtx = ctx.name();
            // Content-Length and Transfer-Encoding must not be sent in any response with a status code of 1xx or 204.
            if (version == WebSocketVersion.V00) {
                // V00 需聚合响应体（握手载荷通常不超过 8192 字节）
                aggregatorCtx = "httpAggregator";
                p.addAfter(ctx.name(), aggregatorCtx, new HttpObjectAggregator(8192));
            }

            p.addAfter(aggregatorCtx, "handshaker", new ChannelInboundHandlerAdapter() {

                private FullHttpResponse fullHttpResponse;

                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof HttpObject) {
                        try {
                            handleHandshakeResponse(ctx, (HttpObject) msg);
                        } finally {
                            ReferenceCountUtil.release(msg);
                        }
                    } else {
                        super.channelRead(ctx, msg);
                    }
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    // Remove ourself and fail the handshake promise.
                    ctx.pipeline().remove(this);
                    promise.setFailure(cause);
                }

                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    try {
                        // Fail promise if Channel was closed
                        if (!promise.isDone()) {
                            promise.tryFailure(new ClosedChannelException());
                        }
                        ctx.fireChannelInactive();
                    } finally {
                        releaseFullHttpResponse();
                    }
                }

                @Override
                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                    releaseFullHttpResponse();
                }

                private void handleHandshakeResponse(ChannelHandlerContext ctx, HttpObject response) {
                    if (response instanceof FullHttpResponse) {
                        ctx.pipeline().remove(this);
                        tryFinishHandshake((FullHttpResponse) response);
                        return;
                    }

                    if (response instanceof LastHttpContent) {
                        assert fullHttpResponse != null;
                        FullHttpResponse handshakeResponse = fullHttpResponse;
                        fullHttpResponse = null;
                        try {
                            ctx.pipeline().remove(this);
                            tryFinishHandshake(handshakeResponse);
                        } finally {
                            handshakeResponse.release();
                        }
                        return;
                    }

                    if (response instanceof HttpResponse) {
                        HttpResponse httpResponse = (HttpResponse) response;
                        fullHttpResponse = new DefaultFullHttpResponse(httpResponse.protocolVersion(),
                            httpResponse.status(), Unpooled.EMPTY_BUFFER, httpResponse.headers(),
                            EmptyHttpHeaders.INSTANCE);
                        if (httpResponse.decoderResult().isFailure()) {
                            fullHttpResponse.setDecoderResult(httpResponse.decoderResult());
                        }
                    }
                }

                private void tryFinishHandshake(FullHttpResponse fullHttpResponse) {
                    try {
                        finishHandshake(channel, fullHttpResponse);
                        promise.setSuccess();
                    } catch (Throwable cause) {
                        promise.setFailure(cause);
                    }
                }

                private void releaseFullHttpResponse() {
                    if (fullHttpResponse != null) {
                        fullHttpResponse.release();
                        fullHttpResponse = null;
                    }
                }
            });
            try {
                ctx.fireChannelRead(ReferenceCountUtil.retain(response));
            } catch (Throwable cause) {
                promise.setFailure(cause);
            }
        }
        return promise;
    }

    /**
     * 校验 {@link FullHttpResponse}，失败时抛出 {@link WebSocketHandshakeException}。
     */
    protected abstract void verify(FullHttpResponse response);

    /**
     * 握手完成后使用的帧解码器。
     */
    protected abstract WebSocketFrameDecoder newWebsocketDecoder();

    /**
     * 握手完成后使用的帧编码器。
     */
    protected abstract WebSocketFrameEncoder newWebSocketEncoder();

    /**
     * 执行 Closing Handshake。
     * <p>在 {@link ChannelHandler} 内调用时，优先使用
     * {@link #close(ChannelHandlerContext, CloseWebSocketFrame)}。
     *
     * @param channel
     *            Channel
     * @param frame
     *            Closing Frame that was received
     */
    public ChannelFuture close(Channel channel, CloseWebSocketFrame frame) {
        ObjectUtil.checkNotNull(channel, "channel");
        return close(channel, frame, channel.newPromise());
    }

    /**
     * 执行 Closing Handshake 并在完成时通知 {@link ChannelPromise}。
     *
     * @param channel
     *            Channel
     * @param frame
     *            Closing Frame that was received
     * @param promise
     *            the {@link ChannelPromise} to be notified when the closing handshake is done
     */
    public ChannelFuture close(Channel channel, CloseWebSocketFrame frame, ChannelPromise promise) {
        ObjectUtil.checkNotNull(channel, "channel");
        return close0(channel, channel, frame, promise);
    }

    /**
     * 执行 Closing Handshake（从 {@link ChannelHandlerContext} 调用）。
     *
     * @param ctx
     *            the {@link ChannelHandlerContext} to use.
     * @param frame
     *            Closing Frame that was received
     */
    public ChannelFuture close(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
        ObjectUtil.checkNotNull(ctx, "ctx");
        return close(ctx, frame, ctx.newPromise());
    }

    /**
     * 执行 Closing Handshake 并通过 {@link ChannelPromise} 通知结果。
     *
     * @param ctx
     *            the {@link ChannelHandlerContext} to use.
     * @param frame
     *            Closing Frame that was received
     * @param promise
     *            the {@link ChannelPromise} to be notified when the closing handshake is done
     */
    public ChannelFuture close(ChannelHandlerContext ctx, CloseWebSocketFrame frame, ChannelPromise promise) {
        ObjectUtil.checkNotNull(ctx, "ctx");
        return close0(ctx, ctx.channel(), frame, promise);
    }

    private ChannelFuture close0(final ChannelOutboundInvoker invoker, final Channel channel,
                                 CloseWebSocketFrame frame, ChannelPromise promise) {
        invoker.writeAndFlush(frame, promise);
        final long forceCloseTimeoutMillis = this.forceCloseTimeoutMillis;
        final WebSocketClientHandshaker handshaker = this;
        if (forceCloseTimeoutMillis <= 0 || !channel.isActive() || forceCloseInit != 0) {
            return promise;
        }

        promise.addListener(future -> {
            // flush 失败或并发 close 时不安排强制关闭
            if (future.isSuccess() && channel.isActive() &&
                    FORCE_CLOSE_INIT_UPDATER.compareAndSet(handshaker, 0, 1)) {
                final Future<?> forceCloseFuture = channel.eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (channel.isActive()) {
                            invoker.close();
                            forceCloseComplete = true;
                        }
                    }
                }, forceCloseTimeoutMillis, TimeUnit.MILLISECONDS);
                channel.closeFuture().addListener(f -> forceCloseFuture.cancel(false));
            }
        });
        return promise;
    }

    /**
     * 根据 {@link URI} 构造 Upgrade 请求的路径（或绝对 URL）。
     */
    protected String upgradeUrl(URI wsURL) {
        if (absoluteUpgradeUrl) {
            return wsURL.toString();
        }

        String path = wsURL.getRawPath();
        path = path == null || path.isEmpty() ? "/" : path;
        String query = wsURL.getRawQuery();
        return query != null && !query.isEmpty() ? path + '?' + query : path;
    }

    static CharSequence websocketHostValue(URI wsURL) {
        int port = wsURL.getPort();
        if (port == -1) {
            return wsURL.getHost();
        }
        String host = wsURL.getHost();
        String scheme = wsURL.getScheme();
        if (port == HttpScheme.HTTP.port()) {
            return HttpScheme.HTTP.name().contentEquals(scheme)
                    || WebSocketScheme.WS.name().contentEquals(scheme) ?
                    host : NetUtil.toSocketAddressString(host, port);
        }
        if (port == HttpScheme.HTTPS.port()) {
            return HttpScheme.HTTPS.name().contentEquals(scheme)
                    || WebSocketScheme.WSS.name().contentEquals(scheme) ?
                    host : NetUtil.toSocketAddressString(host, port);
        }

        // 非标准端口需在 Host 头中附带端口号（RFC 6454 §6.2）
        return NetUtil.toSocketAddressString(host, port);
    }

    static CharSequence websocketOriginValue(URI wsURL) {
        String scheme = wsURL.getScheme();
        final String schemePrefix;
        int port = wsURL.getPort();
        final int defaultPort;
        if (WebSocketScheme.WSS.name().contentEquals(scheme)
            || HttpScheme.HTTPS.name().contentEquals(scheme)
            || (scheme == null && port == WebSocketScheme.WSS.port())) {

            schemePrefix = HTTPS_SCHEME_PREFIX;
            defaultPort = WebSocketScheme.WSS.port();
        } else {
            schemePrefix = HTTP_SCHEME_PREFIX;
            defaultPort = WebSocketScheme.WS.port();
        }

        // Origin 的 host 须小写（RFC 6454 §4）
        String host = wsURL.getHost().toLowerCase(Locale.US);

        if (port != defaultPort && port != -1) {
            // if the port is not standard (80/443) its needed to add the port to the header.
            // See https://tools.ietf.org/html/rfc6454#section-6.2
            return schemePrefix + NetUtil.toSocketAddressString(host, port);
        }
        return schemePrefix + host;
    }
}
