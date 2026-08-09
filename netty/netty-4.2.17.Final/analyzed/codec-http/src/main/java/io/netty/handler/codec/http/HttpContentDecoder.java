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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * HTTP 内容解压缩抽象基类，通过 {@link EmbeddedChannel} 解码 {@link HttpContent}。
 * <p>
 * {@link #newContentDecoder(String)} 按 Content-Encoding 创建解码器；
 * 不支持时返回 {@code null} 透传。解码后更新 Content-Encoding/Length 头。
 * 须放在 {@link HttpObjectDecoder} 之后。
 * <p>
 * 子类实现参考 {@link HttpContentDecompressor}。
 */
public abstract class HttpContentDecoder extends MessageToMessageDecoder<HttpObject> {

    static final String IDENTITY = HttpHeaderValues.IDENTITY.toString();

    protected ChannelHandlerContext ctx;
    private EmbeddedChannel decoder;
    private boolean continueResponse;
    private boolean needRead = true;
    private ByteBufForwarder forwarder;

    public HttpContentDecoder() {
        super(HttpObject.class);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        needRead = true;
        if (msg instanceof HttpResponse && ((HttpResponse) msg).status().code() == 100) {

            if (!(msg instanceof LastHttpContent)) {
                continueResponse = true;
            }
            // 100 Continue 响应须透传，不参与解压
            needRead = false;
            ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
            return;
        }

        if (continueResponse) {
            if (msg instanceof LastHttpContent) {
                continueResponse = false;
            }
            // 100-continue response must be passed through.
            needRead = false;
            ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
            return;
        }

        if (msg instanceof HttpMessage) {
            cleanup();
            final HttpMessage message = (HttpMessage) msg;
            final HttpHeaders headers = message.headers();

            // 从 Content-Encoding 或 Transfer-Encoding 确定编码方式
            String contentEncoding = headers.get(HttpHeaderNames.CONTENT_ENCODING);
            if (contentEncoding != null) {
                contentEncoding = contentEncoding.trim();
            } else {
                String transferEncoding = headers.get(HttpHeaderNames.TRANSFER_ENCODING);
                if (transferEncoding != null) {
                    int idx = transferEncoding.indexOf(',');
                    if (idx != -1) {
                        contentEncoding = transferEncoding.substring(0, idx).trim();
                    } else {
                        contentEncoding = transferEncoding.trim();
                    }
                } else {
                    contentEncoding = IDENTITY;
                }
            }
            decoder = newContentDecoder(contentEncoding);

            if (decoder == null) {
                if (message instanceof HttpContent) {
                    ((HttpContent) message).retain();
                }
                needRead = false;
                ctx.fireChannelRead(message);
                return;
            }
            decoder.pipeline().addLast(forwarder);
            // 解码完成前无法确定长度，移除 Content-Length 并改用 chunked
            // the correct value can be set only after all chunks are processed/decoded.
            // If buffering is not an issue, add HttpObjectAggregator down the chain, it will set the header.
            // Otherwise, rely on LastHttpContent message.
            if (headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
                headers.remove(HttpHeaderNames.CONTENT_LENGTH);
                headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            }
            // 可能已是 chunked 或 EOF 终止
            // See https://github.com/netty/netty/issues/5892

            // set new content encoding,
            CharSequence targetContentEncoding = getTargetContentEncoding(contentEncoding);
            if (HttpHeaderValues.IDENTITY.contentEquals(targetContentEncoding)) {
                // 目标编码为 identity 时不设置 Content-Encoding（RFC 2616 §14.11）
                // as per: https://tools.ietf.org/html/rfc2616#section-14.11
                headers.remove(HttpHeaderNames.CONTENT_ENCODING);
            } else {
                headers.set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
            }

            if (message instanceof HttpContent) {
                // FullHttpMessage 时先向下游 fire 仅含头的副本，正文随后解码
                // Output headers only; data part will be decoded below.
                // Note: "copy" object must not be an instance of LastHttpContent class,
                // as this would (erroneously) indicate the end of the HttpMessage to other handlers.
                HttpMessage copy;
                if (message instanceof HttpRequest) {
                    HttpRequest r = (HttpRequest) message; // HttpRequest or FullHttpRequest
                    copy = new DefaultHttpRequest(r.protocolVersion(), r.method(), r.uri());
                } else if (message instanceof HttpResponse) {
                    HttpResponse r = (HttpResponse) message; // HttpResponse or FullHttpResponse
                    copy = new DefaultHttpResponse(r.protocolVersion(), r.status());
                } else {
                    throw new CodecException("Object of class " + message.getClass().getName() +
                                             " is not an HttpRequest or HttpResponse");
                }
                copy.headers().set(message.headers());
                copy.setDecoderResult(message.decoderResult());
                needRead = false;
                ctx.fireChannelRead(copy);
            } else {
                needRead = false;
                ctx.fireChannelRead(message);
            }
        }

        if (msg instanceof HttpContent) {
            final HttpContent c = (HttpContent) msg;
            if (decoder == null) {
                needRead = false;
                ctx.fireChannelRead(c.retain());
            } else {
                // retain 后写入 EmbeddedChannel，写出时由 forwarder 包装为 HttpContent
                decoder.writeInbound(c.content().retain());

                if (c instanceof LastHttpContent) {
                    boolean notEmpty = decoder.finish();
                    decoder = null;
                    assert !notEmpty;
                    LastHttpContent last = (LastHttpContent) c;
                    // 最后一个 HttpContent 到达，发出 EMPTY 或带 trailer 的 LastHttpContent
                    // the last product on closure,
                    HttpHeaders headers = last.trailingHeaders();
                    needRead = false;
                    if (headers.isEmpty()) {
                        ctx.fireChannelRead(LastHttpContent.EMPTY_LAST_CONTENT);
                    } else {
                        ctx.fireChannelRead(new ComposedLastHttpContent(headers, DecoderResult.SUCCESS));
                    }
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        boolean needRead = this.needRead;
        this.needRead = true;

        try {
            ctx.fireChannelReadComplete();
        } finally {
            if (needRead && !ctx.channel().config().isAutoRead()) {
                ctx.read();
            }
        }
    }

    /**
     * 按 Content-Encoding 创建解码用 {@link EmbeddedChannel}；不支持则返回 {@code null}。
     *
     * @param contentEncoding the value of the {@code "Content-Encoding"} header
     * @return a new {@link EmbeddedChannel} if the specified encoding is supported.
     *         {@code null} otherwise (alternatively, you can throw an exception
     *         to block unknown encoding).
     */
    protected abstract EmbeddedChannel newContentDecoder(String contentEncoding) throws Exception;

    /**
     * Returns the expected content encoding of the decoded content.
     * This getMethod returns {@code "identity"} by default, which is the case for
     * most decoders.
     *
     * @param contentEncoding the value of the {@code "Content-Encoding"} header
     * @return the expected content encoding of the new content
     */
    protected String getTargetContentEncoding(
            @SuppressWarnings("UnusedParameters") String contentEncoding) throws Exception {
        return IDENTITY;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        cleanupSafely(ctx);
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cleanupSafely(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        forwarder = new ByteBufForwarder(ctx);
        super.handlerAdded(ctx);
    }

    private void cleanup() {
        if (decoder != null) {
            // 清理上次未正常关闭的 decoder
            boolean nonEmpty = decoder.finishAndReleaseAll();
            decoder = null;
            assert !nonEmpty;
        }
    }

    private void cleanupSafely(ChannelHandlerContext ctx) {
        try {
            cleanup();
        } catch (Throwable cause) {
            // If cleanup throws any error we need to propagate it through the pipeline
            // so we don't fail to propagate pipeline events.
            ctx.fireExceptionCaught(cause);
        }
    }

    private final class ByteBufForwarder extends ChannelInboundHandlerAdapter {

        private final ChannelHandlerContext targetCtx;

        ByteBufForwarder(ChannelHandlerContext targetCtx) {
            this.targetCtx = targetCtx;
        }

        @Override
        public boolean isSharable() {
            // 标记 sharable：同一实例会被加入多个 EmbeddedChannel
            return true;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            if (!buf.isReadable()) {
                buf.release();
                return;
            }
            needRead = false;
            targetCtx.fireChannelRead(new DefaultHttpContent(buf));
        }
    }
}
