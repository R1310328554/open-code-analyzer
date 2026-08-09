/*
 * Copyright 2015 The Netty Project
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
package io.netty.handler.codec.rtsp;

import java.util.regex.Pattern;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpDecoderConfig;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 将 {@link io.netty.buffer.ByteBuf} 解码为以 {@link HttpMessage} 表示的 RTSP 消息。
 * <p>RTSP 报文格式与 HTTP 类似，复用 {@link HttpObjectDecoder} 框架；首行以
 * {@code RTSP/x.x} 开头则为响应，否则为请求。
 * <p>
 * <h3>防止内存过度占用的参数</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>首行最大长度（如 {@code "SETUP / RTSP/1.0"} 或 {@code "RTSP/1.0 200 OK"}），
 *     超出则抛出 {@link io.netty.handler.codec.TooLongFrameException}。</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>所有头字段总长度上限，超出则抛出 {@link io.netty.handler.codec.TooLongFrameException}。</td>
 * </tr>
 * <tr>
 * <td>{@code maxContentLength}</td>
 * <td>消息体最大长度，超出则抛出 {@link io.netty.handler.codec.TooLongFrameException}。</td>
 * </tr>
 * </table>
 */
public class RtspDecoder extends HttpObjectDecoder {
    /**
     * 未知响应的状态码占位（999 Unknown）。
     */
    private static final HttpResponseStatus UNKNOWN_STATUS =
            new HttpResponseStatus(999, "Unknown");
    /** 当前正在解码的是请求（true）还是响应（false） */
    private boolean isDecodingRequest;

    /** 匹配首行 RTSP 版本串，用于区分请求与响应 */
    private static final Pattern versionPattern = Pattern.compile("RTSP/\\d\\.\\d");

    /** 默认消息体最大长度（8192 字节） */
    public static final int DEFAULT_MAX_CONTENT_LENGTH = 8192;

    /**
     * 使用默认 {@code maxInitialLineLength (4096)}、{@code maxHeaderSize (8192)}、
     * {@code maxContentLength (8192)} 创建解码器。
     */
    public RtspDecoder() {
        this(DEFAULT_MAX_INITIAL_LINE_LENGTH,
             DEFAULT_MAX_HEADER_SIZE,
             DEFAULT_MAX_CONTENT_LENGTH);
    }

    /**
     * 按指定长度限制创建解码器。
     * @param maxInitialLineLength The max allowed length of initial line
     * @param maxHeaderSize The max allowed size of header
     * @param maxContentLength The max allowed content length
     */
    public RtspDecoder(final int maxInitialLineLength,
                       final int maxHeaderSize,
                       final int maxContentLength) {
        super(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxContentLength * 2)
                .setChunkedSupported(false));
    }

    /**
     * Creates a new instance with the specified parameters.
     * @param maxInitialLineLength The max allowed length of initial line
     * @param maxHeaderSize The max allowed size of header
     * @param maxContentLength The max allowed content length
     * @param validateHeaders Set to true if headers should be validated
     * @deprecated Use the {@link #RtspDecoder(HttpDecoderConfig)} constructor instead,
     * or the {@link #RtspDecoder(int, int, int)} to always enable header validation.
     */
    @Deprecated
    public RtspDecoder(final int maxInitialLineLength,
                       final int maxHeaderSize,
                       final int maxContentLength,
                       final boolean validateHeaders) {
        super(new HttpDecoderConfig()
                .setMaxInitialLineLength(maxInitialLineLength)
                .setMaxHeaderSize(maxHeaderSize)
                .setMaxChunkSize(maxContentLength * 2)
                .setChunkedSupported(false)
                .setValidateHeaders(validateHeaders));
    }

    /**
     * 使用 {@link HttpDecoderConfig} 创建解码器；RTSP 不支持 chunked 传输。
     */
    public RtspDecoder(HttpDecoderConfig config) {
        super(config.clone()
                .setMaxChunkSize(2 * config.getMaxChunkSize())
                .setChunkedSupported(false));
    }

    @Override
    protected HttpMessage createMessage(final String[] initialLine)
            throws Exception {
        // 首元素匹配 RTSP 版本则为响应，否则为请求
        if (versionPattern.matcher(initialLine[0]).matches()) {
            isDecodingRequest = false;
            return new DefaultHttpResponse(RtspVersions.valueOf(initialLine[0]),
                new HttpResponseStatus(Integer.parseInt(initialLine[1]),
                                       initialLine[2]),
                headersFactory);
        } else {
            isDecodingRequest = true;
            return new DefaultHttpRequest(RtspVersions.valueOf(initialLine[2]),
                    RtspMethods.valueOf(initialLine[0]),
                    initialLine[1],
                    headersFactory);
        }
    }

    @Override
    protected boolean isContentAlwaysEmpty(final HttpMessage msg) {
        // RTSP 与 HTTP 不同：缺少 Content-Length 时一律视为零长度消息体
        return super.isContentAlwaysEmpty(msg) || !msg.headers().contains(RtspHeaderNames.CONTENT_LENGTH);
    }

    @Override
    protected HttpMessage createInvalidMessage() {
        if (isDecodingRequest) {
            return new DefaultFullHttpRequest(RtspVersions.RTSP_1_0,
                       RtspMethods.OPTIONS, "/bad-request", Unpooled.buffer(0), headersFactory, trailersFactory);
        } else {
            return new DefaultFullHttpResponse(
                    RtspVersions.RTSP_1_0, UNKNOWN_STATUS, Unpooled.buffer(0), headersFactory, trailersFactory);
        }
    }

    @Override
    protected boolean isDecodingRequest() {
        return isDecodingRequest;
    }
}
