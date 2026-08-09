/*
 * Copyright 2014 The Netty Project
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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * SPDY 头部块解码器抽象基类：将 Zlib 压缩的 Name/Value 块解析为 HTTP 风格头部。
 *
 * @see SpdyHeaderBlockRawDecoder
 * @see SpdyHeaderBlockZlibDecoder
 */
public abstract class SpdyHeaderBlockDecoder {

    /** 工厂方法：默认使用 {@link SpdyHeaderBlockZlibDecoder} */
    static SpdyHeaderBlockDecoder newInstance(SpdyVersion spdyVersion, int maxHeaderSize) {
        return new SpdyHeaderBlockZlibDecoder(spdyVersion, maxHeaderSize);
    }

    /**
     * 解码头部块，将 Name/Value 对填入 {@code frame}。
     * 若头部块格式非法，{@code frame} 会被标记为 invalid；
     * 此时应以 PROTOCOL_ERROR 重置该流。
     *
     * @param alloc the {@link ByteBufAllocator} which can be used to allocate new {@link ByteBuf}s
     * @param headerBlock the HeaderBlock to decode
     * @param frame the Headers frame that receives the Name/Value pairs
     * @throws Exception If the header block is malformed in a way that prevents any future
     *                   decoding of any other header blocks, an exception will be thrown.
     *                   A session error with status code PROTOCOL_ERROR must be issued.
     */
    abstract void decode(ByteBufAllocator alloc, ByteBuf headerBlock, SpdyHeadersFrame frame) throws Exception;

    /** 一个完整头部块解码结束时的收尾（重置内部状态） */
    abstract void endHeaderBlock(SpdyHeadersFrame frame) throws Exception;

    /** 连接关闭时释放资源 */
    abstract void end();
}
