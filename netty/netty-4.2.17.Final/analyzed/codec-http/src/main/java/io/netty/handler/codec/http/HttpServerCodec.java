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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CombinedChannelDuplexHandler;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE;
import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE;
import static io.netty.handler.codec.http.HttpObjectDecoder.DEFAULT_MAX_INITIAL_LINE_LENGTH;

/**
 * 服务端 HTTP 复合编解码器，组合 {@link HttpRequestDecoder} 与 {@link HttpResponseEncoder}。
 * <p>
 * 内部维护请求方法 FIFO 队列，使 HEAD/CONNECT 响应正确判定无消息体；
 * 同时处理 TE+CL 冲突时响应后关闭连接。
 *
 * <h3>Header Validation</h3>
 *
 * 建议始终启用头校验以防 CRLF 注入。It is recommended to always enable header validation.
 * <p>
 * Without header validation, your system can become vulnerable to
 * <a href="https://cwe.mitre.org/data/definitions/113.html">
 *     CWE-113: Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Response Splitting')
 * </a>.
 * <p>
 * This recommendation stands even when both peers in the HTTP exchange are trusted,
 * as it helps with defence-in-depth.
 *
 * @see HttpClientCodec
 */
public final class HttpServerCodec extends CombinedChannelDuplexHandler<HttpRequestDecoder, HttpResponseEncoder>
        implements HttpServerUpgradeHandler.SourceCodec {

    /** 请求方法标记：HEAD */
    private static final byte METHOD_FLAG_HEAD = 1;
    /** 请求方法标记：CONNECT */
    private static final byte METHOD_FLAG_CONNECT = 2;
    /** 请求方法标记：其他方法 */
    private static final byte METHOD_FLAG_OTHER = 3;

    // We only need 2 bits per request because we distinguish:
    // 01 = HEAD, 10 = CONNECT, 11 = other
    private static final int METHOD_FLAG_BITS = 2;
    private static final int INLINE_QUEUE_CAPACITY = Long.SIZE / METHOD_FLAG_BITS; // 32

    /**
     * 请求方法 FIFO：低 2 位存最旧请求，poll 为掩码+移位，≤32 个并发请求无堆分配；
     * 超出 {@link #INLINE_QUEUE_CAPACITY} 时溢出到 {@link #methodOverflowQueue}。
     */
    private long methodQueue;
    private int methodQueueSize;
    private Queue<Byte> methodOverflowQueue;

    /**
     * 为 true 时，下一完整响应写出后关闭连接（TE+CL 冲突等场景）。
     */
    private boolean mustCloseAfterResponse;

    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096)}, {@code maxHeaderSize (8192)}, and
     * {@code maxChunkSize (8192)}).
     */
    public HttpServerCodec() {
        this(DEFAULT_MAX_INITIAL_LINE_LENGTH, DEFAULT_MAX_HEADER_SIZE, DEFAULT_MAX_CHUNK_SIZE);
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        this(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxChunkSize));
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpServerCodec(HttpDecoderConfig)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
        this(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxChunkSize)
                .setValidateHeaders(validateHeaders));
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpServerCodec(HttpDecoderConfig)} constructor, to always enable header
     * validation.
     */
    @Deprecated
    public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders,
                           int initialBufferSize) {
        this(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxChunkSize)
                .setValidateHeaders(validateHeaders)
                .setInitialBufferSize(initialBufferSize));
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpServerCodec(HttpDecoderConfig)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders,
                           int initialBufferSize, boolean allowDuplicateContentLengths) {
        this(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxChunkSize)
                .setValidateHeaders(validateHeaders)
                .setInitialBufferSize(initialBufferSize)
                .setAllowDuplicateContentLengths(allowDuplicateContentLengths));
    }

    /**
     * Creates a new instance with the specified decoder options.
     *
     * @deprecated Prefer the {@link #HttpServerCodec(HttpDecoderConfig)} constructor,
     * to always enable header validation.
     */
    @Deprecated
    public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders,
                           int initialBufferSize, boolean allowDuplicateContentLengths, boolean allowPartialChunks) {
        this(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxChunkSize)
                .setValidateHeaders(validateHeaders)
                .setInitialBufferSize(initialBufferSize)
                .setAllowDuplicateContentLengths(allowDuplicateContentLengths)
                .setAllowPartialChunks(allowPartialChunks));
    }

    /**
     * Creates a new instance with the specified decoder configuration.
     */
    public HttpServerCodec(HttpDecoderConfig config) {
        init(new HttpServerRequestDecoder(config), new HttpServerResponseEncoder());
    }

    /**
     * 从 HTTP 升级到其他协议，从 pipeline 移除本编解码器。
     */
    @Override
    public void upgradeFrom(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
    }

    private void enqueueMethod(HttpMethod method) {
        final byte flag;
        if (HttpMethod.HEAD.equals(method)) {
            flag = METHOD_FLAG_HEAD;
        } else if (HttpMethod.CONNECT.equals(method)) {
            flag = METHOD_FLAG_CONNECT;
        } else {
            flag = METHOD_FLAG_OTHER;
        }

        // 溢出队列非空时始终追加，直至完全排空
        Queue<Byte> overflowQueue = methodOverflowQueue;
        if (overflowQueue != null) {
            overflowQueue.add(flag);
            return;
        }

        if (methodQueueSize < INLINE_QUEUE_CAPACITY) {
            methodQueue |= (long) flag << (methodQueueSize << 1);
            methodQueueSize++;
        } else {
            overflowQueue = new ArrayDeque<>(4);
            overflowQueue.add(flag);
            methodOverflowQueue = overflowQueue;
        }
    }

    private byte pollMethod() {
        if (methodQueueSize != 0) {
            // 取最低 2 位作为最旧请求的方法标记
            byte flag = (byte) (methodQueue & 0x3L);
            methodQueue >>>= METHOD_FLAG_BITS;
            methodQueueSize--;
            return flag;
        }

        Queue<Byte> overflowQueue = methodOverflowQueue;
        if (overflowQueue != null) {
            Byte flag = overflowQueue.poll();
            if (overflowQueue.isEmpty()) {
                methodOverflowQueue = null;
            }
            return flag != null ? flag : METHOD_FLAG_OTHER;
        }

        return METHOD_FLAG_OTHER;
    }

    private final class HttpServerRequestDecoder extends HttpRequestDecoder {
        HttpServerRequestDecoder(HttpDecoderConfig config) {
            super(config);
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
            int oldSize = out.size();
            super.decode(ctx, buffer, out);
            int size = out.size();
            for (int i = oldSize; i < size; i++) {
                Object obj = out.get(i);
                if (obj instanceof HttpRequest) {
                    enqueueMethod(((HttpRequest) obj).method());
                }
            }
        }

        @Override
        /** 请求同时含 chunked TE 与 Content-Length 时，响应后必须关闭连接 */
        protected void handleTransferEncodingChunkedWithContentLength(HttpMessage message) {
            super.handleTransferEncodingChunkedWithContentLength(message);
            mustCloseAfterResponse = true;
        }
    }

    private final class HttpServerResponseEncoder extends HttpResponseEncoder {

        private byte methodFlag;

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (mustCloseAfterResponse && msg instanceof LastHttpContent) {
                mustCloseAfterResponse = false;
                promise = promise.unvoid().addListener(ChannelFutureListener.CLOSE);
            }
            super.write(ctx, msg, promise);
        }

        @Override
        protected void sanitizeHeadersBeforeEncode(HttpResponse msg, boolean isAlwaysEmpty) {
            if (!isAlwaysEmpty && methodFlag == METHOD_FLAG_CONNECT
                    && msg.status().codeClass() == HttpStatusClass.SUCCESS) {
                // Stripping Transfer-Encoding:
                // See https://tools.ietf.org/html/rfc7230#section-3.3.1
                msg.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
                return;
            }

            super.sanitizeHeadersBeforeEncode(msg, isAlwaysEmpty);
        }

        @Override
        protected boolean isContentAlwaysEmpty(HttpResponse msg) {
            if (msg.status().codeClass() == HttpStatusClass.INFORMATIONAL) {
                // An informational response should be excluded from paired comparison. This covers 101 as well:
                // once the protocol is switched this handler is removed from the pipeline, so the entry that is
                // left behind goes away with it. Just delegate to super method which has all the needed handling.
                return super.isContentAlwaysEmpty(msg);
            }
            // 与当前响应对应的请求若为 HEAD，则视为无消息体
            methodFlag = pollMethod();
            return methodFlag == METHOD_FLAG_HEAD || super.isContentAlwaysEmpty(msg);
        }
    }
}
