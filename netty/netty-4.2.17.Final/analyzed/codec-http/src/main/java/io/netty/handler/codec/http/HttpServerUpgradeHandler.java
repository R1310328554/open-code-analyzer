/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.SWITCHING_PROTOCOLS;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.AsciiString.containsAllContentEqualsIgnoreCase;
import static io.netty.util.AsciiString.containsContentEqualsIgnoreCase;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.StringUtil.COMMA;

/**
 * 服务端 HTTP 协议升级处理器（如 WebSocket、HTTP/2）。
 * <p>
 * 继承 {@link HttpObjectAggregator} 聚合含 Upgrade 头的请求；
 * 校验 CONNECTION/协议头后返回 101 并切换 pipeline，完成后移除自身。
 */
public class HttpServerUpgradeHandler extends HttpObjectAggregator {

    /** 升级前 pipeline 中的源 HTTP 编解码器（如 {@link HttpServerCodec}） */

    public interface SourceCodec {
        /**
         * Removes this codec (i.e. all associated handlers) from the pipeline.
         */
        void upgradeFrom(ChannelHandlerContext ctx);
    }

    /** 目标升级协议编解码器，负责准备 101 响应头与替换 handler */

    public interface UpgradeCodec {
        /**
         * Gets all protocol-specific headers required by this protocol for a successful upgrade.
         * Any supplied header will be required to appear in the {@link HttpHeaderNames#CONNECTION} header as well.
         */
        Collection<CharSequence> requiredUpgradeHeaders();

        /**
         * 根据升级请求准备 101 响应头；返回 {@code false} 中止升级并透传请求，
         * {@code true} 则继续调用 {@link #upgradeTo}。
         */
        boolean prepareUpgradeResponse(ChannelHandlerContext ctx, FullHttpRequest upgradeRequest,
                                    HttpHeaders upgradeHeaders);

        /**
         * Performs an HTTP protocol upgrade from the source codec. This method is responsible for
         * adding all handlers required for the new protocol.
         *
         * @param ctx the context for the current handler.
         * @param upgradeRequest the request that triggered the upgrade to this protocol.
         */
        void upgradeTo(ChannelHandlerContext ctx, FullHttpRequest upgradeRequest);
    }

    /**
     * Creates a new {@link UpgradeCodec} for the requested protocol name.
     */
    public interface UpgradeCodecFactory {
        /**
         * Invoked by {@link HttpServerUpgradeHandler} for all the requested protocol names in the order of
         * the client preference. The first non-{@code null} {@link UpgradeCodec} returned by this method
         * will be selected.
         *
         * @return a new {@link UpgradeCodec}, or {@code null} if the specified protocol name is not supported
         */
        UpgradeCodec newUpgradeCodec(CharSequence protocol);
    }

    /**
     * HTTP 升级完成时触发的用户事件，携带原始升级请求供新协议使用。
     */
    public static final class UpgradeEvent implements ReferenceCounted {
        private final CharSequence protocol;
        private final FullHttpRequest upgradeRequest;

        UpgradeEvent(CharSequence protocol, FullHttpRequest upgradeRequest) {
            this.protocol = protocol;
            this.upgradeRequest = upgradeRequest;
        }

        /**
         * The protocol that the channel has been upgraded to.
         */
        public CharSequence protocol() {
            return protocol;
        }

        /**
         * Gets the request that triggered the protocol upgrade.
         */
        public FullHttpRequest upgradeRequest() {
            return upgradeRequest;
        }

        @Override
        public int refCnt() {
            return upgradeRequest.refCnt();
        }

        @Override
        public UpgradeEvent retain() {
            upgradeRequest.retain();
            return this;
        }

        @Override
        public UpgradeEvent retain(int increment) {
            upgradeRequest.retain(increment);
            return this;
        }

        @Override
        public UpgradeEvent touch() {
            upgradeRequest.touch();
            return this;
        }

        @Override
        public UpgradeEvent touch(Object hint) {
            upgradeRequest.touch(hint);
            return this;
        }

        @Override
        public boolean release() {
            return upgradeRequest.release();
        }

        @Override
        public boolean release(int decrement) {
            return upgradeRequest.release(decrement);
        }

        @Override
        public String toString() {
            return "UpgradeEvent [protocol=" + protocol + ", upgradeRequest=" + upgradeRequest + ']';
        }
    }

    private final SourceCodec sourceCodec;
    private final UpgradeCodecFactory upgradeCodecFactory;
    private final HttpHeadersFactory headersFactory;
    private final HttpHeadersFactory trailersFactory;
    private final boolean removeAfterFirstRequest;
    private boolean handlingUpgrade;
    private boolean failedAggregationStart;

    /**
     * Constructs the upgrader with the supported codecs.
     * <p>
     * The handler instantiated by this constructor will reject an upgrade request with non-empty content.
     * It should not be a concern because an upgrade request is most likely a GET request.
     * If you have a client that sends a non-GET upgrade request, please consider using
     * {@link #HttpServerUpgradeHandler(SourceCodec, UpgradeCodecFactory, int)} to specify the maximum
     * length of the content of an upgrade request.
     * </p>
     *
     * @param sourceCodec the codec that is being used initially
     * @param upgradeCodecFactory the factory that creates a new upgrade codec
     *                            for one of the requested upgrade protocols
     */
    public HttpServerUpgradeHandler(SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory) {
        this(sourceCodec, upgradeCodecFactory, 0,
                DefaultHttpHeadersFactory.headersFactory(), DefaultHttpHeadersFactory.trailersFactory());
    }

    /**
     * Constructs the upgrader with the supported codecs.
     *
     * @param sourceCodec the codec that is being used initially
     * @param upgradeCodecFactory the factory that creates a new upgrade codec
     *                            for one of the requested upgrade protocols
     * @param maxContentLength the maximum length of the content of an upgrade request
     */
    public HttpServerUpgradeHandler(
            SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory, int maxContentLength) {
        this(sourceCodec, upgradeCodecFactory, maxContentLength,
                DefaultHttpHeadersFactory.headersFactory(), DefaultHttpHeadersFactory.trailersFactory());
    }

    /**
     * Constructs the upgrader with the supported codecs.
     *
     * @param sourceCodec the codec that is being used initially
     * @param upgradeCodecFactory the factory that creates a new upgrade codec
     *                            for one of the requested upgrade protocols
     * @param maxContentLength the maximum length of the content of an upgrade request
     * @param validateHeaders validate the header names and values of the upgrade response.
     */
    public HttpServerUpgradeHandler(SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory,
                                    int maxContentLength, boolean validateHeaders) {
        this(sourceCodec, upgradeCodecFactory, maxContentLength,
                DefaultHttpHeadersFactory.headersFactory().withValidation(validateHeaders),
                DefaultHttpHeadersFactory.trailersFactory().withValidation(validateHeaders));
    }

    /**
     * Constructs the upgrader with the supported codecs.
     *
     * @param sourceCodec the codec that is being used initially
     * @param upgradeCodecFactory the factory that creates a new upgrade codec
     *                            for one of the requested upgrade protocols
     * @param maxContentLength the maximum length of the content of an upgrade request
     * @param headersFactory The {@link HttpHeadersFactory} to use for headers.
     * The recommended default factory is {@link DefaultHttpHeadersFactory#headersFactory()}.
     * @param trailersFactory The {@link HttpHeadersFactory} to use for trailers.
     * The recommended default factory is {@link DefaultHttpHeadersFactory#trailersFactory()}.
     */
    public HttpServerUpgradeHandler(
            SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory, int maxContentLength,
            HttpHeadersFactory headersFactory, HttpHeadersFactory trailersFactory) {
        this(sourceCodec, upgradeCodecFactory, maxContentLength, headersFactory, trailersFactory, false);
    }

    /**
     * Constructs the upgrader with the supported codecs.
     *
     * @param sourceCodec the codec that is being used initially
     * @param upgradeCodecFactory the factory that creates a new upgrade codec
     *                            for one of the requested upgrade protocols
     * @param maxContentLength the maximum length of the content of an upgrade request
     * @param headersFactory The {@link HttpHeadersFactory} to use for headers.
     * The recommended default factory is {@link DefaultHttpHeadersFactory#headersFactory()}.
     * @param trailersFactory The {@link HttpHeadersFactory} to use for trailers.
     * The recommended default factory is {@link DefaultHttpHeadersFactory#trailersFactory()}.
     * @param removeAfterFirstRequest {@code true} if the handler should remove itself after the first request was
     *                                processed (even if it was not an upgrade request), {@code false} otherwise.
     */
    public HttpServerUpgradeHandler(
            SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory, int maxContentLength,
            HttpHeadersFactory headersFactory, HttpHeadersFactory trailersFactory, boolean removeAfterFirstRequest) {
        super(maxContentLength);

        this.sourceCodec = checkNotNull(sourceCodec, "sourceCodec");
        this.upgradeCodecFactory = checkNotNull(upgradeCodecFactory, "upgradeCodecFactory");
        this.headersFactory = checkNotNull(headersFactory, "headersFactory");
        this.trailersFactory = checkNotNull(trailersFactory, "trailersFactory");
        this.removeAfterFirstRequest = removeAfterFirstRequest;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out)
            throws Exception {

        if (!handlingUpgrade) {
            // 尚未处理升级：检测 Upgrade 头并决定是否进入聚合流程
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;
                if (req.headers().contains(HttpHeaderNames.UPGRADE) &&
                    shouldHandleUpgradeRequest(req)) {
                    handlingUpgrade = true;
                    failedAggregationStart = true; // reset if beginAggregation is called
                } else {
                    if (removeAfterFirstRequest) {
                        // This is not an upgrade request, just remove this handler.
                        ctx.pipeline().remove(this);
                    }
                    ReferenceCountUtil.retain(msg);
                    ctx.fireChannelRead(msg);
                    return;
                }
            } else {
                ReferenceCountUtil.retain(msg);
                ctx.fireChannelRead(msg);
                return;
            }
        }

        FullHttpRequest fullRequest;
        if (msg instanceof FullHttpRequest) {
            fullRequest = (FullHttpRequest) msg;
            ReferenceCountUtil.retain(msg);
            out.add(msg);
        } else {
            // Call the base class to handle the aggregation of the full request.
            super.decode(ctx, msg, out);
            if (out.isEmpty()) {
                if (msg instanceof LastHttpContent || failedAggregationStart) {
                    // request failed to aggregate, try with the next request
                    handlingUpgrade = false;
                    releaseCurrentMessage();
                }

                // The full request hasn't been created yet, still awaiting more data.
                return;
            }

            // Finished aggregating the full request, get it from the output list.
            assert out.size() == 1;
            handlingUpgrade = false;
            fullRequest = (FullHttpRequest) out.get(0);
        }

        if (upgrade(ctx, fullRequest)) {
            // The upgrade was successful, remove the message from the output list
            // so that it's not propagated to the next handler. This request will
            // be propagated as a user event instead.
            out.clear();
        } else if (removeAfterFirstRequest) {
            // We handle the first request and were not able to upgrade, just remove this handler.
            ctx.pipeline().remove(this);
        }

        // The upgrade did not succeed, just allow the full request to propagate to the
        // next handler.
    }

    @Override
    protected FullHttpMessage beginAggregation(HttpMessage start, ByteBuf content) throws Exception {
        failedAggregationStart = false;
        return super.beginAggregation(start, content);
    }

    /**
     * 判断是否处理该 Upgrade 请求；默认处理所有含 Upgrade 头的请求，可覆写以忽略特定协议。
     * This method will be invoked only when the request contains an {@code Upgrade} header.
     * It always returns {@code true} by default, which means any request with an {@code Upgrade} header
     * will be handled. You can override this method to ignore certain {@code Upgrade} headers, for example:
     * <pre>{@code
     * @Override
     * protected boolean isUpgradeRequest(HttpRequest req) {
     *   // Do not handle WebSocket upgrades.
     *   return !req.headers().contains(HttpHeaderNames.UPGRADE, "websocket", false);
     * }
     * }</pre>
     */
    protected boolean shouldHandleUpgradeRequest(HttpRequest req) {
        return true;
    }

    /**
     * Attempts to upgrade to the protocol(s) identified by the {@link HttpHeaderNames#UPGRADE} header (if provided
     * in the request).
     *
     * @param ctx the context for this handler.
     * @param request the HTTP request.
     * @return {@code true} if the upgrade occurred, otherwise {@code false}.
     */
    private boolean upgrade(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        // 按客户端 UPGRADE 头中的协议优先级选择首个支持的 UpgradeCodec
        final List<CharSequence> requestedProtocols = splitHeader(request.headers().get(HttpHeaderNames.UPGRADE));
        final int numRequestedProtocols = requestedProtocols.size();
        UpgradeCodec upgradeCodec = null;
        CharSequence upgradeProtocol = null;
        for (int i = 0; i < numRequestedProtocols; i ++) {
            final CharSequence p = requestedProtocols.get(i);
            final UpgradeCodec c = upgradeCodecFactory.newUpgradeCodec(p);
            if (c != null) {
                upgradeProtocol = p;
                upgradeCodec = c;
                break;
            }
        }

        if (upgradeCodec == null) {
            // None of the requested protocols are supported, don't upgrade.
            return false;
        }

        // Make sure the CONNECTION header is present.
        List<String> connectionHeaderValues = request.headers().getAll(HttpHeaderNames.CONNECTION);

        if (connectionHeaderValues == null || connectionHeaderValues.isEmpty()) {
            return false;
        }

        final StringBuilder concatenatedConnectionValue = new StringBuilder(connectionHeaderValues.size() * 10);
        for (CharSequence connectionHeaderValue : connectionHeaderValues) {
            concatenatedConnectionValue.append(connectionHeaderValue).append(COMMA);
        }
        concatenatedConnectionValue.setLength(concatenatedConnectionValue.length() - 1);

        // Make sure the CONNECTION header contains UPGRADE as well as all protocol-specific headers.
        Collection<CharSequence> requiredHeaders = upgradeCodec.requiredUpgradeHeaders();
        List<CharSequence> values = splitHeader(concatenatedConnectionValue);
        if (!containsContentEqualsIgnoreCase(values, HttpHeaderNames.UPGRADE) ||
                !containsAllContentEqualsIgnoreCase(values, requiredHeaders)) {
            return false;
        }

        // Ensure that all required protocol-specific headers are found in the request.
        for (CharSequence requiredHeader : requiredHeaders) {
            if (!request.headers().contains(requiredHeader)) {
                return false;
            }
        }

        // 先写 101 响应（旧 codec 仍在 pipeline），再切换协议并 fire UpgradeEvent
        final FullHttpResponse upgradeResponse = createUpgradeResponse(upgradeProtocol);
        if (!upgradeCodec.prepareUpgradeResponse(ctx, request, upgradeResponse.headers())) {
            return false;
        }

        // Create the user event to be fired once the upgrade completes.
        final UpgradeEvent event = new UpgradeEvent(upgradeProtocol, request);

        // After writing the upgrade response we immediately prepare the
        // pipeline for the next protocol to avoid a race between completion
        // of the write future and receiving data before the pipeline is
        // restructured.
        try {
            final ChannelFuture writeComplete = ctx.writeAndFlush(upgradeResponse);
            // Perform the upgrade to the new protocol.
            sourceCodec.upgradeFrom(ctx);
            upgradeCodec.upgradeTo(ctx, request);

            // Remove this handler from the pipeline.
            ctx.pipeline().remove(HttpServerUpgradeHandler.this);

            // Notify that the upgrade has occurred. Retain the event to offset
            // the release() in the finally block.
            ctx.fireUserEventTriggered(event.retain());

            // Add the listener last to avoid firing upgrade logic after
            // the channel is already closed since the listener may fire
            // immediately if the write failed eagerly.
            writeComplete.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } finally {
            // Release the event if the upgrade event wasn't fired.
            event.release();
        }
        return true;
    }

    /**
     * Creates the 101 Switching Protocols response message.
     */
    private FullHttpResponse createUpgradeResponse(CharSequence upgradeProtocol) {
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(
                HTTP_1_1, SWITCHING_PROTOCOLS, Unpooled.EMPTY_BUFFER, headersFactory, trailersFactory);
        res.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
        res.headers().add(HttpHeaderNames.UPGRADE, upgradeProtocol);
        return res;
    }

    /**
     * Splits a comma-separated header value. The returned set is case-insensitive and contains each
     * part with whitespace removed.
     */
    private static List<CharSequence> splitHeader(CharSequence header) {
        final StringBuilder builder = new StringBuilder(header.length());
        final List<CharSequence> protocols = new ArrayList<CharSequence>(4);
        for (int i = 0; i < header.length(); ++i) {
            char c = header.charAt(i);
            if (Character.isWhitespace(c)) {
                // Don't include any whitespace.
                continue;
            }
            if (c == ',') {
                // Add the string and reset the builder for the next protocol.
                protocols.add(builder.toString());
                builder.setLength(0);
            } else {
                builder.append(c);
            }
        }

        // Add the last protocol
        if (builder.length() > 0) {
            protocols.add(builder.toString());
        }

        return protocols;
    }
}
