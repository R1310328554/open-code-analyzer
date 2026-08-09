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
package io.netty.handler.codec.rtsp;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectDecoder;

import static io.netty.handler.codec.rtsp.RtspDecoder.DEFAULT_MAX_CONTENT_LENGTH;

/**
 * 将 {@link ByteBuf} 解码为 {@link HttpMessage} 形式的 RTSP 消息（旧版抽象基类）。
 * <p>
 * <h3>防止内存过度占用的参数</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>首行最大长度，超出则抛出 {@link TooLongFrameException}。</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>头字段总长度上限，超出则抛出 {@link TooLongFrameException}。</td>
 * </tr>
 * <tr>
 * <td>{@code maxContentLength}</td>
 * <td>消息体最大长度，超出则抛出 {@link TooLongFrameException}。</td>
 * </tr>
 * </table>
 *
 * @deprecated 请改用 {@link RtspDecoder}。
 */
@Deprecated
public abstract class RtspObjectDecoder extends HttpObjectDecoder {

    /**
     * 使用默认长度限制创建解码器。
     */
    protected RtspObjectDecoder() {
        this(DEFAULT_MAX_INITIAL_LINE_LENGTH, DEFAULT_MAX_HEADER_SIZE, DEFAULT_MAX_CONTENT_LENGTH);
    }

    /**
     * Creates a new instance with the specified parameters.
     */
    protected RtspObjectDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength) {
        super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false);
    }

    protected RtspObjectDecoder(
            int maxInitialLineLength, int maxHeaderSize, int maxContentLength, boolean validateHeaders) {
        super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false, validateHeaders);
    }

    @Override
    protected boolean isContentAlwaysEmpty(HttpMessage msg) {
        // RTSP：缺少 Content-Length 时视为零长度消息体
        boolean empty = super.isContentAlwaysEmpty(msg);
        if (empty) {
            return true;
        }
        if (!msg.headers().contains(RtspHeaderNames.CONTENT_LENGTH)) {
            return true;
        }
        return empty;
    }
}
