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

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2CodecUtil.SimpleChannelPromiseAggregator;
import io.netty.util.ReferenceCountUtil;

/**
 * 将出站方向的 HTTP/1.x 对象（{@link HttpMessage}、{@link HttpContent}）编码为 HTTP/2 帧。
 * <p>
 * 入站方向的 HTTP/2 → HTTP/1.x 转换见 {@link InboundHttp2ToHttpAdapter}。
 */
public class HttpToHttp2ConnectionHandler extends Http2ConnectionHandler {

    /** 是否在转换时校验 HTTP 头部合法性。 */
    private final boolean validateHeaders;
    /** 当前正在写入的 HTTP/2 流 ID（由首个 {@link HttpMessage} 确定）。 */
    private int currentStreamId;
    /** 构造时指定的默认 scheme，可自动填入伪头部。 */
    private HttpScheme httpScheme;

    protected HttpToHttp2ConnectionHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                           Http2Settings initialSettings, boolean validateHeaders) {
        super(decoder, encoder, initialSettings);
        this.validateHeaders = validateHeaders;
    }

    protected HttpToHttp2ConnectionHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                           Http2Settings initialSettings, boolean validateHeaders,
                                           boolean decoupleCloseAndGoAway) {
        this(decoder, encoder, initialSettings, validateHeaders, decoupleCloseAndGoAway, null);
    }

    protected HttpToHttp2ConnectionHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                           Http2Settings initialSettings, boolean validateHeaders,
                                           boolean decoupleCloseAndGoAway, HttpScheme httpScheme) {
        super(decoder, encoder, initialSettings, decoupleCloseAndGoAway);
        this.validateHeaders = validateHeaders;
        this.httpScheme = httpScheme;
    }

    protected HttpToHttp2ConnectionHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                           Http2Settings initialSettings, boolean validateHeaders,
                                           boolean decoupleCloseAndGoAway, boolean flushPreface,
                                           HttpScheme httpScheme) {
        super(decoder, encoder, initialSettings, decoupleCloseAndGoAway, flushPreface);
        this.validateHeaders = validateHeaders;
        this.httpScheme = httpScheme;
    }

    /**
     * 从扩展头 {@code x-http2-stream-id} 读取流 ID；未指定时由连接本地端自动分配下一个 ID。
     *
     * @param httpHeaders The HTTP/1.x headers object to look for the stream id
     * @return The stream id to use with this {@link HttpHeaders} object
     * @throws Exception If the {@code httpHeaders} object specifies an invalid stream id
     */
    private int getStreamId(HttpHeaders httpHeaders) throws Exception {
        return httpHeaders.getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(),
                                  connection().local().incrementAndGetNextStreamId());
    }

    /**
     * 拦截 {@link HttpMessage}/{@link HttpContent} 写入，拆分为 HEADERS/DATA 帧并聚合多个子 Promise。
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

        if (!(msg instanceof HttpMessage || msg instanceof HttpContent)) {
            ctx.write(msg, promise);
            return;
        }

        boolean release = true;
        SimpleChannelPromiseAggregator promiseAggregator =
                new SimpleChannelPromiseAggregator(promise, ctx.channel(), ctx.executor());
        try {
            Http2ConnectionEncoder encoder = encoder();
            boolean endStream = false;
            if (msg instanceof HttpMessage) {
                final HttpMessage httpMsg = (HttpMessage) msg;

                // 允许调用方通过扩展头显式指定 streamId
                currentStreamId = getStreamId(httpMsg.headers());

                // 构造器配置了 scheme 且头部尚未携带时，自动注入 x-http2-scheme
                if (httpScheme != null &&
                        !httpMsg.headers().contains(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text())) {
                    httpMsg.headers().set(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), httpScheme.name());
                }

                // 转换并写出 HEADERS 帧；FullHttpMessage 无正文时可在此帧上置 END_STREAM
                Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMsg, validateHeaders);
                endStream = msg instanceof FullHttpMessage && !((FullHttpMessage) msg).content().isReadable();
                writeHeaders(ctx, encoder, currentStreamId, httpMsg.headers(), http2Headers,
                        endStream, promiseAggregator);
            }

            if (!endStream && msg instanceof HttpContent) {
                boolean isLastContent = false;
                HttpHeaders trailers = EmptyHttpHeaders.INSTANCE;
                Http2Headers http2Trailers = EmptyHttp2Headers.INSTANCE;
                if (msg instanceof LastHttpContent) {
                    isLastContent = true;

                    // 将 HTTP/1.x trailing headers 转为 HTTP/2 尾部 HEADERS 帧
                    final LastHttpContent lastContent = (LastHttpContent) msg;
                    trailers = lastContent.trailingHeaders();
                    http2Trailers = HttpConversionUtil.toHttp2Headers(trailers, validateHeaders);
                }

                // 写出 DATA 帧；引用计数由 encoder 接管，故 release=false
                final ByteBuf content = ((HttpContent) msg).content();
                endStream = isLastContent && trailers.isEmpty();
                encoder.writeData(ctx, currentStreamId, content, 0, endStream, promiseAggregator.newPromise());
                release = false;

                if (!trailers.isEmpty()) {
                    // 有 trailer 时单独发一帧 HEADERS 并置 END_STREAM
                    writeHeaders(ctx, encoder, currentStreamId, trailers, http2Trailers, true, promiseAggregator);
                }
            }
        } catch (Throwable t) {
            onError(ctx, true, t);
            promiseAggregator.setFailure(t);
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
            promiseAggregator.doneAllocatingPromises();
        }
    }

    private static void writeHeaders(ChannelHandlerContext ctx, Http2ConnectionEncoder encoder, int streamId,
                                     HttpHeaders headers, Http2Headers http2Headers, boolean endStream,
                                     SimpleChannelPromiseAggregator promiseAggregator) {
        int dependencyId = headers.getInt(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), 0);
        short weight = headers.getShort(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);
        encoder.writeHeaders(ctx, streamId, http2Headers, dependencyId, weight, false,
                0, endStream, promiseAggregator.newPromise());
    }
}
