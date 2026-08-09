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

/**
 * HTTP/2 HEADERS 帧抽象，携带完整 HPACK 解码后的头列表。
 */
public interface Http2HeadersFrame extends Http2StreamFrame {

    /**
     * 完整头列表；CONTINUATION 分片由 codec 自动拼接。
     */
    Http2Headers headers();

    /**
     * 帧 padding 字节数，非负且小于 256。
     */
    int padding();

    /**
     * 是否设置 END_STREAM 标志（本帧同时结束该流）。
     */
    boolean isEndStream();
}
