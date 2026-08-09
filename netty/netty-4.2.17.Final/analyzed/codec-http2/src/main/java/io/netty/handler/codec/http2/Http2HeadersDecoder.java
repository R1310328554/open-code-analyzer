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

/**
 * HPACK 头块解码器：将二进制头块还原为 {@link Http2Headers}。
 * <p>解码过程维护动态表状态，须与对端编码器通过 SETTINGS 同步表大小。
 */
public interface Http2HeadersDecoder {
    /**
     * {@link Http2HeadersDecoder} 的配置项。
     */
    interface Configuration {
        /**
         * 设置 HPACK 动态表最大容量，对应 SETTINGS_HEADER_TABLE_SIZE。
         * This method should only be called by Netty (not users) as a result of a receiving a {@code SETTINGS} frame.
         */
        void maxHeaderTableSize(long max) throws Http2Exception;

        /**
         * 返回当前 HPACK 动态表最大容量；初始值须为 {@link Http2CodecUtil#DEFAULT_HEADER_TABLE_SIZE}。
         */
        long maxHeaderTableSize();

        /**
         * 配置单组头的最大字节数及触发 GOAWAY 的上限。
         * <p>
         * This method should only be called by Netty (not users) as a result of a receiving a {@code SETTINGS} frame.
         * @param max <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">SETTINGS_MAX_HEADER_LIST_SIZE</a>.
         *      If this limit is exceeded the implementation should attempt to keep the HPACK header tables up to date
         *      by processing data from the peer, but a {@code RST_STREAM} frame will be sent for the offending stream.
         * @param goAwayMax Must be {@code >= max}. A {@code GO_AWAY} frame will be generated if this limit is exceeded
         *                  for any particular stream.
         * @throws Http2Exception if limits exceed the RFC's boundaries or {@code max > goAwayMax}.
         */
        void maxHeaderListSize(long max, long goAwayMax) throws Http2Exception;

        /**
         * 返回 SETTINGS_MAX_HEADER_LIST_SIZE 当前值。
         */
        long maxHeaderListSize();

        /**
         * 返回触发 GOAWAY 的头列表大小上限，{@code <= maxHeaderListSize()}。
         */
        long maxHeaderListSizeGoAway();
    }

    /**
     * 解码 HPACK 头块并返回 {@link Http2Headers}。
     *
     * @param streamId 所属流 ID，用于错误报告
     * @param headerBlock HPACK 编码的二进制头块
     */
    Http2Headers decodeHeaders(int streamId, ByteBuf headerBlock) throws Http2Exception;

    /**
     * 获取本解码器的配置对象。
     */
    Configuration configuration();
}
