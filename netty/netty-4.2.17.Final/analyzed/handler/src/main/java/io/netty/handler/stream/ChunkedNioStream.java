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
package io.netty.handler.stream;

import static io.netty.util.internal.ObjectUtil.checkPositive;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * 从 {@link ReadableByteChannel} 按块读取数据的 {@link ChunkedInput}。
 * <p>通道须为<strong>阻塞模式</strong>；不支持非阻塞通道。</p>
 */
public class ChunkedNioStream implements ChunkedInput<ByteBuf> {

    /** 可读字节通道。 */
    private final ReadableByteChannel in;

    /** 每块最大字节数。 */
    private final int chunkSize;
    /** 已传输字节累计。 */
    private long offset;

    /**
     * 复用的读缓冲（position 表示未刷出数据长度）。
     */
    private final ByteBuffer byteBuffer;

    /**
     * 从通道创建实例（默认块大小）。
     */
    public ChunkedNioStream(ReadableByteChannel in) {
        this(in, ChunkedStream.DEFAULT_CHUNK_SIZE);
    }

    /**
     * 从通道创建实例。
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedNioStream(ReadableByteChannel in, int chunkSize) {
        this.in = checkNotNull(in, "in");
        this.chunkSize = checkPositive(chunkSize, "chunkSize");
        byteBuffer = ByteBuffer.allocate(chunkSize);
    }

    /**
     * 返回已传输字节数。
     */
    public long transferredBytes() {
        return offset;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        if (byteBuffer.position() > 0) {
            // 上次 readChunk 未刷完，缓冲中仍有数据
            return false;
        }
        if (in.isOpen()) {
            // 预读一块以判断是否到达 EOF（不 rewind）
            int b = in.read(byteBuffer);
            if (b < 0) {
                return true;
            } else {
                offset += b;
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        in.close();
    }

    @Deprecated
    @Override
    public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
        if (isEndOfInput()) {
            return null;
        }
        // 此时 byteBuffer 中至少有一部分数据
        int readBytes = byteBuffer.position();
        for (;;) {
            int localReadBytes = in.read(byteBuffer);
            if (localReadBytes < 0) {
                break;
            }
            readBytes += localReadBytes;
            offset += localReadBytes;
            if (readBytes == chunkSize) {
                break;
            }
        }
        byteBuffer.flip();
        boolean release = true;
        ByteBuf buffer = allocator.buffer(byteBuffer.remaining());
        try {
            buffer.writeBytes(byteBuffer);
            byteBuffer.clear();
            release = false;
            return buffer;
        } finally {
            if (release) {
                buffer.release();
            }
        }
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public long progress() {
        return offset;
    }
}
