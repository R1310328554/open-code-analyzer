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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * SPDY 头部块编码器抽象基类：将 {@link SpdyHeadersFrame} 中的 Name/Value 对
 * 序列化并（可选）Zlib 压缩为线上格式。
 *
 * @see SpdyHeaderBlockZlibEncoder
 * @see SpdyHeaderBlockJZlibEncoder
 * @see SpdyHeaderBlockRawEncoder
 */
public abstract class SpdyHeaderBlockEncoder {

    /** 工厂方法：默认使用 {@link SpdyHeaderBlockZlibEncoder} */
    static SpdyHeaderBlockEncoder newInstance(
            SpdyVersion version, int compressionLevel, int windowBits, int memLevel) {
        return new SpdyHeaderBlockZlibEncoder(version, compressionLevel);
    }

    /** 将帧内头部编码为压缩后的 {@link ByteBuf} */
    abstract ByteBuf encode(ByteBufAllocator alloc, SpdyHeadersFrame frame) throws Exception;
    /** 连接关闭时释放压缩器资源 */
    abstract void end();
}
