/*
 * Copyright 2023 The Netty Project
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

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * {@link HttpObjectDecoder} 及其子类的可配置行为对象。
 * <p>
 * 可变且 {@link Cloneable}，用于设置行/头/块大小限制、chunked 支持、
 * 头校验、RFC 9112 严格模式等解码策略。
 */
public final class HttpDecoderConfig implements Cloneable {
    private int maxChunkSize = HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE;
    private boolean chunkedSupported = HttpObjectDecoder.DEFAULT_CHUNKED_SUPPORTED;
    private boolean allowPartialChunks = HttpObjectDecoder.DEFAULT_ALLOW_PARTIAL_CHUNKS;
    private HttpHeadersFactory headersFactory = DefaultHttpHeadersFactory.headersFactory();
    private HttpHeadersFactory trailersFactory = DefaultHttpHeadersFactory.trailersFactory();
    private boolean allowDuplicateContentLengths = HttpObjectDecoder.DEFAULT_ALLOW_DUPLICATE_CONTENT_LENGTHS;
    private int maxInitialLineLength = HttpObjectDecoder.DEFAULT_MAX_INITIAL_LINE_LENGTH;
    private int maxHeaderSize = HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE;
    private int initialBufferSize = HttpObjectDecoder.DEFAULT_INITIAL_BUFFER_SIZE;
    private boolean strictLineParsing = HttpObjectDecoder.DEFAULT_STRICT_LINE_PARSING;
    private boolean useRfc9112TransferEncoding = HttpObjectDecoder.RFC9112_TRANSFER_ENCODING;

    public int getInitialBufferSize() {
        return initialBufferSize;
    }

    /**
     * 设置解析 HTTP 起始行与头字段时的初始临时缓冲区大小。
     *
     * @param initialBufferSize The buffer size in bytes.
     * @return This decoder config.
     */
    public HttpDecoderConfig setInitialBufferSize(int initialBufferSize) {
        checkPositive(initialBufferSize, "initialBufferSize");
        this.initialBufferSize = initialBufferSize;
        return this;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    /**
     * 设置 HTTP 起始行（请求行/状态行）最大长度，限制内存占用。
     * You would typically set this to the same value as {@link #setMaxHeaderSize(int)}.
     *
     * @param maxInitialLineLength The maximum length, in bytes.
     * @return This decoder config.
     */
    public HttpDecoderConfig setMaxInitialLineLength(int maxInitialLineLength) {
        checkPositive(maxInitialLineLength, "maxInitialLineLength");
        this.maxInitialLineLength = maxInitialLineLength;
        return this;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    /**
     * 设置所有头字段累计最大长度（非单行限制）。
     * The limit applies to the sum of all the headers, so it applies equally to many short header-lines,
     * or fewer but longer header lines.
     * <p>
     * You would typically set this to the same value as {@link #setMaxInitialLineLength(int)}.
     *
     * @param maxHeaderSize The maximum length, in bytes.
     * @return This decoder config.
     */
    public HttpDecoderConfig setMaxHeaderSize(int maxHeaderSize) {
        checkPositive(maxHeaderSize, "maxHeaderSize");
        this.maxHeaderSize = maxHeaderSize;
        return this;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    /**
     * 设置向下游传递的单个 {@link HttpContent} 最大字节数。
     * This sets the limit, in bytes, at which Netty will send a chunk down the pipeline.
     *
     * @param maxChunkSize The maximum chunk size, in bytes.
     * @return This decoder config.
     */
    public HttpDecoderConfig setMaxChunkSize(int maxChunkSize) {
        checkPositive(maxChunkSize, "maxChunkSize");
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    public boolean isChunkedSupported() {
        return chunkedSupported;
    }

    /**
     * 是否支持 {@code Transfer-Encoding: chunked} 分块传输。
     *
     * @param chunkedSupported if {@code false}, then a {@code Transfer-Encoding: Chunked} header will produce an error,
     * instead of a stream of chunks.
     * @return This decoder config.
     */
    public HttpDecoderConfig setChunkedSupported(boolean chunkedSupported) {
        this.chunkedSupported = chunkedSupported;
        return this;
    }

    public boolean isAllowPartialChunks() {
        return allowPartialChunks;
    }

    /**
     * 输入缓冲不足时是否允许将单个 chunk 拆成多条 {@link HttpContent}。
     *
     * @param allowPartialChunks set to {@code false} to only allow sending whole chunks down the pipeline.
     * @return This decoder config.
     */
    public HttpDecoderConfig setAllowPartialChunks(boolean allowPartialChunks) {
        this.allowPartialChunks = allowPartialChunks;
        return this;
    }

    public HttpHeadersFactory getHeadersFactory() {
        return headersFactory;
    }

    /**
     * Set the {@link HttpHeadersFactory} to use when creating new HTTP headers objects.
     * The default headers factory is {@link DefaultHttpHeadersFactory#headersFactory()}.
     * <p>
     * For the purpose of {@link #clone()}, it is assumed that the factory is either immutable, or can otherwise be
     * shared across different decoders and decoder configs.
     *
     * @param headersFactory The header factory to use.
     * @return This decoder config.
     */
    public HttpDecoderConfig setHeadersFactory(HttpHeadersFactory headersFactory) {
        checkNotNull(headersFactory, "headersFactory");
        this.headersFactory = headersFactory;
        return this;
    }

    public boolean isAllowDuplicateContentLengths() {
        return allowDuplicateContentLengths;
    }

    /**
     * 是否允许多个 Content-Length 头（默认禁止，防请求/响应拆分攻击）。
     *
     * @param allowDuplicateContentLengths set to {@code true} to allow multiple content length headers.
     * @return This decoder config.
     */
    public HttpDecoderConfig setAllowDuplicateContentLengths(boolean allowDuplicateContentLengths) {
        this.allowDuplicateContentLengths = allowDuplicateContentLengths;
        return this;
    }

    /**
     * 启用/禁用头名与头值校验（默认启用，防 CRLF 注入）。
     * This works by changing the configured {@linkplain #setHeadersFactory(HttpHeadersFactory) header factory}
     * and {@linkplain #setTrailersFactory(HttpHeadersFactory) trailer factory}.
     * <p>
     * You usually want header validation enabled (which is the default) in order to prevent request-/response-splitting
     * attacks.
     *
     * @param validateHeaders set to {@code false} to disable header validation.
     * @return This decoder config.
     */
    public HttpDecoderConfig setValidateHeaders(boolean validateHeaders) {
        DefaultHttpHeadersFactory noValidation = DefaultHttpHeadersFactory.headersFactory().withValidation(false);
        headersFactory = validateHeaders ? DefaultHttpHeadersFactory.headersFactory() : noValidation;
        trailersFactory = validateHeaders ? DefaultHttpHeadersFactory.trailersFactory() : noValidation;
        return this;
    }

    public HttpHeadersFactory getTrailersFactory() {
        return trailersFactory;
    }

    /**
     * Set the {@link HttpHeadersFactory} used to create HTTP trailers.
     * This differs from {@link #setHeadersFactory(HttpHeadersFactory)} in that trailers have different validation
     * requirements.
     * The default trailer factory is {@link DefaultHttpHeadersFactory#headersFactory()}.
     * <p>
     * For the purpose of {@link #clone()}, it is assumed that the factory is either immutable, or can otherwise be
     * shared across different decoders and decoder configs.
     *
     * @param trailersFactory The headers factory to use for creating trailers.
     * @return This decoder config.
     */
    public HttpDecoderConfig setTrailersFactory(HttpHeadersFactory trailersFactory) {
        checkNotNull(trailersFactory, "trailersFactory");
        this.trailersFactory = trailersFactory;
        return this;
    }

    public boolean isStrictLineParsing() {
        return strictLineParsing;
    }

    /**
     * RFC 9112 严格行解析：{@code true} 时强制 CR LF 分隔起始行与头字段。
     * <p>
     * Parsing leniencies can increase compatibility with a wider range of implementations, but can also cause
     * security vulnerabilities, when multiple systems disagree on the meaning of leniently parsed messages.
     * <p>
     * When <em>strict line parsing</em> is enabled ({@code true}), then Netty will enforce that start- and header
     * field-lines MUST be separated by a CR LF octet pair, and will produce messages with failed
     * {@link io.netty.handler.codec.DecoderResult}s.
     * Additionally, Netty will enforce that only CR LF characters precede the initial line, if any.
     * <p>
     * When <em>strict line parsing</em> is disabled ({@code false}), then Netty will accept lone LF octets as line
     * separators for the start- and header field-lines.
     * Additionally, Netty will ignore any ISO control and line separator characters prior to the initial line.
     * <p>
     * See <a href="https://datatracker.ietf.org/doc/html/rfc9112#name-message-format">RFC 9112 Section 2.1</a> and
     * <a href="https://datatracker.ietf.org/doc/html/rfc9112#section-2.2-6">RFC 9112 Section 2.2</a>.
     * @param strictLineParsing Whether <em>strict line parsing</em> should be enabled ({@code true}),
     * or not ({@code false}).
     * @return This decoder config.
     */
    public HttpDecoderConfig setStrictLineParsing(boolean strictLineParsing) {
        this.strictLineParsing = strictLineParsing;
        return this;
    }

    public boolean isUseRfc9112TransferEncoding() {
        return useRfc9112TransferEncoding;
    }

    /**
     * RFC 9112 比 RFC 7230 更严格：禁止同一消息同时含 Transfer-Encoding 与 Content-Length；
     * 默认 {@code true} 时此类消息将被<em>拒绝</em>。
     * <p>
     * When this setting is set to {@code false}, it restores the RFC 7230 behavior of instead removing any
     * {@code Content-Length} headers when {@code Transfer-Encoding} headers are present.
     * @param useRfc9112TransferEncoding Whether to reject messages with both {@code Transfer-Encoding} and
     *                                   {@code Content-Length} headers.
     * @return This decoder config.
     * @see HttpObjectDecoder#handleTransferEncodingChunkedWithContentLength(HttpMessage)
     */
    public HttpDecoderConfig setUseRfc9112TransferEncoding(boolean useRfc9112TransferEncoding) {
        this.useRfc9112TransferEncoding = useRfc9112TransferEncoding;
        return this;
    }

    @Override
    public HttpDecoderConfig clone() {
        try {
            return (HttpDecoderConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
