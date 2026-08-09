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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.PrematureChannelClosureException;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_ALLOW_DUPLICATE_CONTENT_LENGTHS;
import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_ALLOW_PARTIAL_CHUNKS;
import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE;
import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE;
import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_MAX_INITIAL_LINE_LENGTH;
import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_VALIDATE_HEADERS;

/**
 * 客户端 HTTP 复合编解码器，组合 {@link HttpRequestEncoder} 与 {@link HttpResponseDecoder}。
 * <p>
 * 额外维护请求/响应配对队列，处理 {@code HEAD} 与 {@code CONNECT} 等
 * {@link HttpResponseDecoder} 无法单独完成的状态管理。
 * 通道关闭时若仍有未收齐的响应，可抛出 {@link PrematureChannelClosureException}。
 * <p>
 * 建议始终启用头校验，以防 HTTP 响应拆分（CWE-113）等攻击。
 *
 * @see HttpServerCodec
 */
public final class HttpClientCodec extends CombinedChannelDuplexHandler<HttpResponseDecoder, HttpRequestEncoder>
        implements HttpClientUpgradeHandler.SourceCodec {
    public static final boolean DEFAULT_FAIL_ON_MISSING_RESPONSE = false;
    public static final boolean DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST = false;

    /** 请求方法队列，用于将响应与对应请求（HEAD/CONNECT 等）关联 */

    private final Queue<HttpMethod> queue = new ArrayDeque<HttpMethod>();
    private final boolean parseHttpAfterConnectRequest;

    /** CONNECT 成功后若为 true，后续字节透传不再按 HTTP 解码 */

    private boolean done;

    private final AtomicLong requestResponseCounter = new AtomicLong();
    private final boolean failOnMissingResponse;

    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096)}, {@code maxHeaderSize (8192)}, and
     * {@code maxChunkSize (8192)}).
     */
    public HttpClientCodec() {
        this(new HttpDecoderConfig(),
                DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST,
                DEFAULT_FAIL_ON_MISSING_RESPONSE);
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        this(new HttpDecoderConfig()
                        .setMaxInitialLineLength(maxInitialLineLength)
                        .setMaxHeaderSize(maxHeaderSize)
                        .setMaxChunkSize(maxChunkSize),
                DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST,
                DEFAULT_FAIL_ON_MISSING_RESPONSE);
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse) {
        this(new HttpDecoderConfig()
                        .setMaxInitialLineLength(maxInitialLineLength)
                        .setMaxHeaderSize(maxHeaderSize)
                        .setMaxChunkSize(maxChunkSize),
                DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST,
                failOnMissingResponse);
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpClientCodec(int, int, int, boolean)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse,
            boolean validateHeaders) {
        this(new HttpDecoderConfig()
                        .setMaxInitialLineLength(maxInitialLineLength)
                        .setMaxHeaderSize(maxHeaderSize)
                        .setMaxChunkSize(maxChunkSize)
                        .setValidateHeaders(validateHeaders),
                DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST,
                failOnMissingResponse);
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpClientCodec(HttpDecoderConfig, boolean, boolean)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse,
            boolean validateHeaders, boolean parseHttpAfterConnectRequest) {
        this(new HttpDecoderConfig()
                        .setMaxInitialLineLength(maxInitialLineLength)
                        .setMaxHeaderSize(maxHeaderSize)
                        .setMaxChunkSize(maxChunkSize)
                        .setValidateHeaders(validateHeaders),
                parseHttpAfterConnectRequest,
                failOnMissingResponse);
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpClientCodec(HttpDecoderConfig, boolean, boolean)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse,
            boolean validateHeaders, int initialBufferSize) {
        this(new HttpDecoderConfig()
                        .setMaxInitialLineLength(maxInitialLineLength)
                        .setMaxHeaderSize(maxHeaderSize)
                        .setMaxChunkSize(maxChunkSize)
                        .setValidateHeaders(validateHeaders)
                        .setInitialBufferSize(initialBufferSize),
                DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST,
                failOnMissingResponse);
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpClientCodec(HttpDecoderConfig, boolean, boolean)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse,
            boolean validateHeaders, int initialBufferSize, boolean parseHttpAfterConnectRequest) {
        this(new HttpDecoderConfig()
                        .setMaxInitialLineLength(maxInitialLineLength)
                        .setMaxHeaderSize(maxHeaderSize)
                        .setMaxChunkSize(maxChunkSize)
                        .setValidateHeaders(validateHeaders)
                        .setInitialBufferSize(initialBufferSize),
                parseHttpAfterConnectRequest,
                failOnMissingResponse);
    }
    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpClientCodec(HttpDecoderConfig, boolean, boolean)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse,
            boolean validateHeaders, int initialBufferSize, boolean parseHttpAfterConnectRequest,
            boolean allowDuplicateContentLengths) {
        this(new HttpDecoderConfig()
                        .setMaxInitialLineLength(maxInitialLineLength)
                        .setMaxHeaderSize(maxHeaderSize)
                        .setMaxChunkSize(maxChunkSize)
                        .setValidateHeaders(validateHeaders)
                        .setInitialBufferSize(initialBufferSize)
                        .setAllowDuplicateContentLengths(allowDuplicateContentLengths),
                parseHttpAfterConnectRequest,
                failOnMissingResponse);
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpClientCodec(HttpDecoderConfig, boolean, boolean)}
     * constructor, to always enable header validation.
     */
    @Deprecated
    public HttpClientCodec(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse,
            boolean validateHeaders, int initialBufferSize, boolean parseHttpAfterConnectRequest,
            boolean allowDuplicateContentLengths, boolean allowPartialChunks) {
        this(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxChunkSize)
                .setValidateHeaders(validateHeaders)
                .setInitialBufferSize(initialBufferSize)
                .setAllowDuplicateContentLengths(allowDuplicateContentLengths)
                .setAllowPartialChunks(allowPartialChunks),
                parseHttpAfterConnectRequest,
                failOnMissingResponse);
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public HttpClientCodec(
            HttpDecoderConfig config, boolean parseHttpAfterConnectRequest, boolean failOnMissingResponse) {
        init(new Decoder(config), new Encoder());
        this.parseHttpAfterConnectRequest = parseHttpAfterConnectRequest;
        this.failOnMissingResponse = failOnMissingResponse;
    }

    /**
     * 准备从 HTTP 升级到另一协议，禁用 {@link Encoder} 的 HTTP 编码。
     */
    @Override
    public void prepareUpgradeFrom(ChannelHandlerContext ctx) {
        ((Encoder) outboundHandler()).upgraded = true;
    }

    /**
     * 完成协议升级，从 pipeline 移除本编解码器。
     */
    @Override
    public void upgradeFrom(ChannelHandlerContext ctx) {
        final ChannelPipeline p = ctx.pipeline();
        p.remove(this);
    }

    public void setSingleDecode(boolean singleDecode) {
        inboundHandler().setSingleDecode(singleDecode);
    }

    public boolean isSingleDecode() {
        return inboundHandler().isSingleDecode();
    }

    private final class Encoder extends HttpRequestEncoder {

        boolean upgraded;

        @Override
        protected void encode(
                ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {

            if (upgraded) {
                // 升级后透传原始消息，Encoder 已禁用 HTTP 编码
                out.add(msg);
                return;
            }

            if (msg instanceof HttpRequest) {
                queue.offer(((HttpRequest) msg).method());
            }

            super.encode(ctx, msg, out);

            if (failOnMissingResponse && !done) {
                // failOnMissingResponse 模式下，LastHttpContent 到达时递增计数
                if (msg instanceof LastHttpContent) {
                    // increment as its the last chunk
                    requestResponseCounter.incrementAndGet();
                }
            }
        }
    }

    private final class Decoder extends HttpResponseDecoder {
        Decoder(HttpDecoderConfig config) {
            super(config);
        }

        @Override
        protected void decode(
                ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
            if (done) {
                int readable = actualReadableBytes();
                if (readable == 0) {
                    // done 模式下无剩余可读字节则直接返回
                    // https://github.com/netty/netty/issues/1159
                    return;
                }
                out.add(buffer.readBytes(readable));
            } else {
                int oldSize = out.size();
                super.decode(ctx, buffer, out);
                if (failOnMissingResponse) {
                    int size = out.size();
                    for (int i = oldSize; i < size; i++) {
                        decrement(out.get(i));
                    }
                }
            }
        }

        private void decrement(Object msg) {
            if (msg == null) {
                return;
            }

            // LastHttpContent 到达时递减未配对响应计数
            if (msg instanceof LastHttpContent) {
                requestResponseCounter.decrementAndGet();
            }
        }

        @Override
        protected boolean isContentAlwaysEmpty(HttpMessage msg) {
            final HttpResponseStatus status = ((HttpResponse) msg).status();
            final HttpStatusClass statusClass = status.codeClass();
            final int statusCode = status.code();
            if (statusClass == HttpStatusClass.INFORMATIONAL) {
                // 1xx 信息性响应不参与请求/响应配对
                // Just delegate to super method which has all the needed handling.
                return super.isContentAlwaysEmpty(msg);
            }

            // 从队列取出与当前响应对应的请求方法
            HttpMethod method = queue.poll();

            // 对端可能违规多发响应，method 为 null 时跳过特殊处理
            if (method != null) {
                char firstChar = method.name().charAt(0);
                switch (firstChar) {
                    case 'H':
                        // RFC2616 §4.3：HEAD 响应不得包含消息体
                        if (HttpMethod.HEAD.equals(method)) {
                            return true;

                            // The following code was inserted to work around the servers
                            // that behave incorrectly.  It has been commented out
                            // because it does not work with well behaving servers.
                            // Please note, even if the 'Transfer-Encoding: chunked'
                            // header exists in the HEAD response, the response should
                            // have absolutely no content.
                            //
                            //// Interesting edge case:
                            //// Some poorly implemented servers will send a zero-byte
                            //// chunk if Transfer-Encoding of the response is 'chunked'.
                            ////
                            //// return !msg.isChunked();
                        }
                        break;
                    case 'C':
                        // CONNECT 成功（200）时响应体为空，可能切换为透传模式
                        if (statusCode == 200) {
                            if (HttpMethod.CONNECT.equals(method)) {
                                // Proxy connection established - Parse HTTP only if configured by
                                // parseHttpAfterConnectRequest, else pass through.
                                if (!parseHttpAfterConnectRequest) {
                                    done = true;
                                    queue.clear();
                                }
                                return true;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            return super.isContentAlwaysEmpty(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx)
                throws Exception {
            super.channelInactive(ctx);

            if (failOnMissingResponse) {
                long missingResponses = requestResponseCounter.get();
                if (missingResponses > 0) {
                    ctx.fireExceptionCaught(new PrematureChannelClosureException(
                            "channel gone inactive with " + missingResponses +
                            " missing response(s)"));
                }
            }
        }
    }
}
