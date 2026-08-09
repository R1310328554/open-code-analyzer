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
package io.netty.handler.codec.spdy;

/**
 * SPDY 协议的 HEADERS 帧：在已打开的流上携带附加头块（如 trailing headers）。
 * <p>与 SYN_STREAM/SYN_REPLY 不同，HEADERS 仅补充或结束某条流上的头信息，
 * 常与 DATA 帧配合传递 HTTP trailer。
 */
public interface SpdyHeadersFrame extends SpdyStreamFrame {

    /**
     * 若头块校验失败（非法名值对等），返回 {@code true}。
     * 对端应回复 {@code PROTOCOL_ERROR} 的 RST_STREAM。
     */
    boolean isInvalid();

    /**
     * 将本头块标记为无效，供解码器在发现协议违规时设置。
     */
    SpdyHeadersFrame setInvalid();

    /**
     * 若因长度限制导致头块被截断，返回 {@code true}。
     */
    boolean isTruncated();

    /**
     * 标记本头块已被截断（超出允许的最大头块大小）。
     */
    SpdyHeadersFrame setTruncated();

    /**
     * 返回本帧携带的 {@link SpdyHeaders} 头集合。
     */
    SpdyHeaders headers();

    @Override
    SpdyHeadersFrame setStreamId(int streamID);

    @Override
    SpdyHeadersFrame setLast(boolean last);
}
