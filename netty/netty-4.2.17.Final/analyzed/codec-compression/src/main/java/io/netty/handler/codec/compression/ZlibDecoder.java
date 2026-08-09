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
package io.netty.handler.codec.compression;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Zlib/Deflate 解压抽象基类，子类实现 JDK 或 JZlib 后端。
 */
public abstract class ZlibDecoder extends ByteToMessageDecoder {

    /** 解压输出缓冲允许的最大容量（0 表示由分配器决定）。 */
    protected final int maxAllocation;

    /** 等价于 {@link #ZlibDecoder(int)}，{@code maxAllocation = 0}。 */
    public ZlibDecoder() {
        this(0);
    }

    /**
     * 构造 Zlib 解码器。
     * @param maxAllocation
     *          Maximum size of the decompression buffer. Must be &gt;= 0.
     *          If zero, maximum size is decided by the {@link ByteBufAllocator}.
     */
    public ZlibDecoder(int maxAllocation) {
        this.maxAllocation = checkPositiveOrZero(maxAllocation, "maxAllocation");
    }

    /** @return 压缩流是否已结束。 */
    public abstract boolean isClosed();

    /**
     * 分配或扩展解压缓冲，不超过 {@link #maxAllocation}；无法继续扩展时调用
     * {@link #decompressionBufferExhausted(ByteBuf)} 并抛出 {@link DecompressionException}。
     */
    protected ByteBuf prepareDecompressBuffer(ChannelHandlerContext ctx, ByteBuf buffer, int preferredSize) {
        if (buffer == null) {
            if (maxAllocation == 0) {
                return ctx.alloc().heapBuffer(preferredSize);
            }

            return ctx.alloc().heapBuffer(Math.min(preferredSize, maxAllocation), maxAllocation);
        }

        // 尽可能扩展缓冲；仅当完全无法扩展时才抛异常，保证在 maxAllocation 下做最后一次解压尝试
        if (buffer.ensureWritable(preferredSize, true) == 1) {
            // 调用 decompressionBufferExhausted 前 duplicate 缓冲，避免子类误将已满缓冲写入输出
            decompressionBufferExhausted(buffer.duplicate());
            buffer.skipBytes(buffer.readableBytes());
            throw new DecompressionException("Decompression buffer has reached maximum size: " + buffer.maxCapacity());
        }

        return buffer;
    }

    /**
     * 解压缓冲无法继续扩展时的钩子；默认空实现，子类可记录已解压数据等。
     */
    protected void decompressionBufferExhausted(ByteBuf buffer) {
    }

}
