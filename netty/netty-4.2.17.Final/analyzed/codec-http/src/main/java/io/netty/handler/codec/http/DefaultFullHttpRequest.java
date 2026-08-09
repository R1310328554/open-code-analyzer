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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;

import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.headersFactory;
import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link FullHttpRequest} 的默认实现。
 * <p>
 * 将请求行、头部与正文合并为一条完整 HTTP 请求消息，并持有 {@link ByteBuf} 正文及可选的 trailing headers。
 */
public class DefaultFullHttpRequest extends DefaultHttpRequest implements FullHttpRequest {
    /** 请求正文缓冲。 */
    private final ByteBuf content;
    /** chunked 编码的尾部头。 */
    private final HttpHeaders trailingHeader;

    /** 缓存 hashCode，避免在 {@link ByteBuf} 已释放时触发 {@link IllegalReferenceCountException}。 */
    private int hash;

    /** 创建无正文的完整 HTTP 请求。 */
    public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        this(httpVersion, method, uri, Unpooled.buffer(0), headersFactory(), trailersFactory());
    }

    /** 创建含指定正文的完整 HTTP 请求。 */
    public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content) {
        this(httpVersion, method, uri, content, headersFactory(), trailersFactory());
    }

    /**
     * Create a full HTTP response with the given HTTP version, method, URI, and optional validation.
     * @deprecated Use the {@link #DefaultFullHttpRequest(HttpVersion, HttpMethod, String, ByteBuf,
     * HttpHeadersFactory, HttpHeadersFactory)} constructor instead.
     */
    @Deprecated
    public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
        this(httpVersion, method, uri, Unpooled.buffer(0),
                headersFactory().withValidation(validateHeaders),
                trailersFactory().withValidation(validateHeaders));
    }

    /**
     * Create a full HTTP response with the given HTTP version, method, URI, contents, and optional validation.
     * @deprecated Use the {@link #DefaultFullHttpRequest(HttpVersion, HttpMethod, String, ByteBuf,
     * HttpHeadersFactory, HttpHeadersFactory)} constructor instead.
     */
    @Deprecated
    public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri,
                                  ByteBuf content, boolean validateHeaders) {
        this(httpVersion, method, uri, content,
                headersFactory().withValidation(validateHeaders),
                trailersFactory().withValidation(validateHeaders));
    }

    /**
     * Create a full HTTP response with the given HTTP version, method, URI, contents,
     * and factories for creating headers and trailers.
     * <p>
     * The recommended default header factory is {@link DefaultHttpHeadersFactory#headersFactory()},
     * and the recommended default trailer factory is {@link DefaultHttpHeadersFactory#trailersFactory()}.
     */
    public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri,
            ByteBuf content, HttpHeadersFactory headersFactory, HttpHeadersFactory trailersFactory) {
        this(httpVersion, method, uri, content, headersFactory.newHeaders(), trailersFactory.newHeaders());
    }

    /**
     * Create a full HTTP response with the given HTTP version, method, URI, contents, and header and trailer objects.
     */
    public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri,
            ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeader) {
        this(httpVersion, method, uri, content, headers, trailingHeader, true);
    }

    /**
     * Create a full HTTP response with the given HTTP version, method, URI, contents, and header and trailer objects.
     */
    public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri,
            ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeader, boolean validateRequestLine) {
        super(httpVersion, method, uri, headers, validateRequestLine);
        this.content = checkNotNull(content, "content");
        this.trailingHeader = checkNotNull(trailingHeader, "trailingHeader");
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return trailingHeader;
    }

    @Override
    public ByteBuf content() {
        return content;
    }

    @Override
    public int refCnt() {
        return content.refCnt();
    }

    @Override
    public FullHttpRequest retain() {
        content.retain();
        return this;
    }

    @Override
    public FullHttpRequest retain(int increment) {
        content.retain(increment);
        return this;
    }

    @Override
    public FullHttpRequest touch() {
        content.touch();
        return this;
    }

    @Override
    public FullHttpRequest touch(Object hint) {
        content.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return content.release();
    }

    @Override
    public boolean release(int decrement) {
        return content.release(decrement);
    }

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public FullHttpRequest setMethod(HttpMethod method) {
        super.setMethod(method);
        return this;
    }

    @Override
    public FullHttpRequest setUri(String uri) {
        super.setUri(uri);
        return this;
    }

    @Override
    public FullHttpRequest copy() {
        return replace(content().copy());
    }

    @Override
    public FullHttpRequest duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public FullHttpRequest retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public FullHttpRequest replace(ByteBuf content) {
        FullHttpRequest request = new DefaultFullHttpRequest(protocolVersion(), method(), uri(), content,
                headers().copy(), trailingHeaders().copy());
        request.setDecoderResult(decoderResult());
        return request;
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            if (ByteBufUtil.isAccessible(content())) {
                try {
                    hash = 31 + content().hashCode();
                } catch (IllegalReferenceCountException ignored) {
                    // 检查 refCnt 与使用 content 之间可能存在竞态
                    hash = 31;
                }
            } else {
                hash = 31;
            }
            hash = 31 * hash + trailingHeaders().hashCode();
            hash = 31 * hash + super.hashCode();
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultFullHttpRequest)) {
            return false;
        }

        DefaultFullHttpRequest other = (DefaultFullHttpRequest) o;

        return super.equals(other) &&
               content().equals(other.content()) &&
               trailingHeaders().equals(other.trailingHeaders());
    }

    @Override
    public String toString() {
        return HttpMessageUtil.appendFullRequest(new StringBuilder(256), this).toString();
    }
}
