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

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.List;

/**
 * {@link Http2StreamFrame} 与 {@link HttpObject} 双向适配器，使基于 HTTP/1 语义的 handler 可在 HTTP/2 流通道上复用。
 * <p>通常与 {@link Http2MultiplexCodec} 配合；无 Content-Length 时默认转为 chunked 编码，除非整条消息仅含单个头部帧。
 */
@Sharable
public class Http2StreamFrameToHttpObjectCodec extends MessageToMessageCodec<Http2StreamFrame, HttpObject> {

    private static final AttributeKey<HttpScheme> SCHEME_ATTR_KEY =
        AttributeKey.valueOf(HttpScheme.class, "STREAMFRAMECODEC_SCHEME");

    private final boolean isServer;
    private final boolean validateHeaders;

    public Http2StreamFrameToHttpObjectCodec(final boolean isServer,
                                             final boolean validateHeaders) {
        super(Http2StreamFrame.class, HttpObject.class);
        this.isServer = isServer;
        this.validateHeaders = validateHeaders;
    }

    public Http2StreamFrameToHttpObjectCodec(final boolean isServer) {
        this(isServer, true);
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return msg instanceof Http2HeadersFrame || msg instanceof Http2DataFrame;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Http2StreamFrame frame, List<Object> out) throws Exception {
        if (frame instanceof Http2HeadersFrame) {
            Http2HeadersFrame headersFrame = (Http2HeadersFrame) frame;
            Http2Headers headers = headersFrame.headers();
            Http2FrameStream stream = headersFrame.stream();
            int id = stream == null ? 0 : stream.id();

            final CharSequence status = headers.status();

            // 1xx 响应（除 101）：Http2HeadersFrame 的 isEndStream 为 false，但需解码为 FullHttpResponse 以兼容 HttpObjectAggregator
            if (null != status && isInformationalResponseHeaderFrame(status)) {
                final FullHttpMessage fullMsg = newFullMessage(id, headers, ctx.alloc());
                out.add(fullMsg);
                return;
            }

            if (headersFrame.isEndStream()) {
                if (headers.method() == null && status == null) {
                    // 仅 trailers、无正文
                    LastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER, validateHeaders);
                    HttpConversionUtil.addHttp2ToHttpHeaders(id, headers, last.trailingHeaders(),
                                                             HttpVersion.HTTP_1_1, true, true);
                    out.add(last);
                } else {
                    FullHttpMessage full = newFullMessage(id, headers, ctx.alloc());
                    out.add(full);
                }
            } else {
                HttpMessage req = newMessage(id, headers);
                if ((status == null || !isContentAlwaysEmpty(status)) && !HttpUtil.isContentLengthSet(req)) {
                    req.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
                }
                out.add(req);
            }
        } else if (frame instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame) frame;
            if (dataFrame.isEndStream()) {
                out.add(new DefaultLastHttpContent(dataFrame.content().retain(), validateHeaders));
            } else {
                out.add(new DefaultHttpContent(dataFrame.content().retain()));
            }
        }
    }

    private void encodeLastContent(LastHttpContent last, List<Object> out) {
        boolean needFiller = !(last instanceof FullHttpMessage) && last.trailingHeaders().isEmpty();
        if (last.content().isReadable() || needFiller) {
            out.add(new DefaultHttp2DataFrame(last.content().retain(), last.trailingHeaders().isEmpty()));
        }
        if (!last.trailingHeaders().isEmpty()) {
            Http2Headers headers = HttpConversionUtil.toHttp2Headers(last.trailingHeaders(), validateHeaders);
            out.add(new DefaultHttp2HeadersFrame(headers, true));
        }
    }

    /**
     * Encode from an {@link HttpObject} to an {@link Http2StreamFrame}. This method will
     * be called for each written message that can be handled by this encoder.
     *
     * NOTE: 100-Continue responses that are NOT {@link FullHttpResponse} will be rejected.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this handler belongs to
     * @param obj           the {@link HttpObject} message to encode
     * @param out           the {@link List} into which the encoded msg should be added
     *                      needs to do some kind of aggregation
     * @throws Exception    is thrown if an error occurs
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, HttpObject obj, List<Object> out) throws Exception {
        // 1xx（除 101）编码为 isEndStream=false 的 HEADERS 帧
        if (obj instanceof HttpResponse) {
            final HttpResponse res = (HttpResponse) obj;
            final HttpResponseStatus status = res.status();
            final int code = status.code();
            final HttpStatusClass statusClass = status.codeClass();
            if (statusClass == HttpStatusClass.INFORMATIONAL && code != 101) {
                if (res instanceof FullHttpResponse) {
                    final Http2Headers headers = toHttp2Headers(ctx, res);
                    out.add(new DefaultHttp2HeadersFrame(headers, false));
                    return;
                } else {
                    throw new EncoderException(status + " must be a FullHttpResponse");
                }
            }
        }

        if (obj instanceof HttpMessage) {
            Http2Headers headers = toHttp2Headers(ctx, (HttpMessage) obj);
            boolean noMoreFrames = false;
            if (obj instanceof FullHttpMessage) {
                FullHttpMessage full = (FullHttpMessage) obj;
                noMoreFrames = !full.content().isReadable() && full.trailingHeaders().isEmpty();
            }

            out.add(new DefaultHttp2HeadersFrame(headers, noMoreFrames));
        }

        if (obj instanceof LastHttpContent) {
            LastHttpContent last = (LastHttpContent) obj;
            encodeLastContent(last, out);
        } else if (obj instanceof HttpContent) {
            HttpContent cont = (HttpContent) obj;
            out.add(new DefaultHttp2DataFrame(cont.content().retain(), false));
        }
    }

    private Http2Headers toHttp2Headers(final ChannelHandlerContext ctx, final HttpMessage msg) {
        if (msg instanceof HttpRequest) {
            msg.headers().set(
                    HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(),
                    connectionScheme(ctx));
        }

        return HttpConversionUtil.toHttp2Headers(msg, validateHeaders);
    }

    private HttpMessage newMessage(final int id,
                                   final Http2Headers headers) throws Http2Exception {
        return isServer ?
                HttpConversionUtil.toHttpRequest(id, headers, validateHeaders) :
                HttpConversionUtil.toHttpResponse(id, headers, validateHeaders);
    }

    private FullHttpMessage newFullMessage(final int id,
                                           final Http2Headers headers,
                                           final ByteBufAllocator alloc) throws Http2Exception {
        return isServer ?
                HttpConversionUtil.toFullHttpRequest(id, headers, alloc, validateHeaders) :
                HttpConversionUtil.toFullHttpResponse(id, headers, alloc, validateHeaders);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);

        // 通常在 Http2StreamChannel 上使用；此时 TLS 握手应已完成，检查父 pipeline 中的 SslHandler 即可推断 scheme
        final Attribute<HttpScheme> schemeAttribute = connectionSchemeAttribute(ctx);
        if (schemeAttribute.get() == null) {
            final HttpScheme scheme = isSsl(ctx) ? HttpScheme.HTTPS : HttpScheme.HTTP;
            schemeAttribute.set(scheme);
        }
    }

    protected boolean isSsl(final ChannelHandlerContext ctx) {
        final Channel connChannel = connectionChannel(ctx);
        return null != connChannel.pipeline().get(SslHandler.class);
    }

    private static HttpScheme connectionScheme(ChannelHandlerContext ctx) {
        final HttpScheme scheme = connectionSchemeAttribute(ctx).get();
        return scheme == null ? HttpScheme.HTTP : scheme;
    }

    private static Attribute<HttpScheme> connectionSchemeAttribute(ChannelHandlerContext ctx) {
        final Channel ch = connectionChannel(ctx);
        return ch.attr(SCHEME_ATTR_KEY);
    }

    private static Channel connectionChannel(ChannelHandlerContext ctx) {
        final Channel ch = ctx.channel();
        return ch instanceof Http2StreamChannel ? ch.parent() : ch;
    }

    /** 1xx 状态码（101 除外）以 HEADERS 帧传输。 */
    private static boolean isInformationalResponseHeaderFrame(CharSequence status) {
        if (status.length() == 3) {
            char char0 = status.charAt(0);
            char char1 = status.charAt(1);
            char char2 = status.charAt(2);
            return char0 == '1'
                && char1 >= '0' && char1 <= '9'
                && char2 >= '0' && char2 <= '9' && char2 != '1';
        }
        return false;
    }

    /*
     * RFC 9113 §8.1.1：204/304 响应不得携带正文
     */
    private static boolean isContentAlwaysEmpty(CharSequence status) {
        if (status.length() == 3) {
            char char0 = status.charAt(0);
            char char1 = status.charAt(1);
            char char2 = status.charAt(2);
            return (char0 == '2' || char0 == '3')
                && char1 == '0'
                && char2 == '4';
        }
        return false;
    }
}
