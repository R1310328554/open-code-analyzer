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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.FileRegion;
import io.netty.util.internal.ObjectUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

/**
 * 通过 NIO {@link FileChannel} 按块从文件读取数据的 {@link ChunkedInput}。
 * <p>
 * 若操作系统支持
 * <a href="https://en.wikipedia.org/wiki/Zero-copy">零拷贝文件传输</a>
 * （如 {@code sendfile()}），可考虑使用 {@link FileRegion}。
 */
public class ChunkedNioFile implements ChunkedInput<ByteBuf> {

    /** 文件通道。 */
    private final FileChannel in;
    /** 传输起始偏移。 */
    private final long startOffset;
    /** 传输结束偏移（不含）。 */
    private final long endOffset;
    /** 每块最大字节数。 */
    private final int chunkSize;
    /** 当前读指针。 */
    private long offset;

    /**
     * 从 File 创建实例。
     */
    public ChunkedNioFile(File in) throws IOException {
        this(new RandomAccessFile(in, "r").getChannel());
    }

    /**
     * 从 File 创建实例并指定块大小。
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedNioFile(File in, int chunkSize) throws IOException {
        this(new RandomAccessFile(in, "r").getChannel(), chunkSize);
    }

    /**
     * 从 FileChannel 创建实例（默认块大小）。
     */
    public ChunkedNioFile(FileChannel in) throws IOException {
        this(in, ChunkedStream.DEFAULT_CHUNK_SIZE);
    }

    /**
     * 从 FileChannel 创建实例。
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedNioFile(FileChannel in, int chunkSize) throws IOException {
        this(in, 0, in.size(), chunkSize);
    }

    /**
     * 从 FileChannel 指定区间创建实例。
     *
     * @param offset the offset of the file where the transfer begins
     * @param length the number of bytes to transfer
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedNioFile(FileChannel in, long offset, long length, int chunkSize)
            throws IOException {
        ObjectUtil.checkNotNull(in, "in");
        ObjectUtil.checkPositiveOrZero(offset, "offset");
        ObjectUtil.checkPositiveOrZero(length, "length");
        ObjectUtil.checkPositive(chunkSize, "chunkSize");
        if (!in.isOpen()) {
            throw new ClosedChannelException();
        }
        this.in = in;
        this.chunkSize = chunkSize;
        this.offset = startOffset = offset;
        endOffset = offset + length;
    }

    /**
     * 返回传输起始偏移。
     */
    public long startOffset() {
        return startOffset;
    }

    /**
     * 返回传输结束偏移（不含）。
     */
    public long endOffset() {
        return endOffset;
    }

    /**
     * 返回当前读指针。
     */
    public long currentOffset() {
        return offset;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        return !(offset < endOffset && in.isOpen());
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
        long offset = this.offset;
        if (offset >= endOffset) {
            return null;
        }

        int chunkSize = (int) Math.min(this.chunkSize, endOffset - offset);
        ByteBuf buffer = allocator.buffer(chunkSize);
        boolean release = true;
        try {
            int readBytes = 0;
            for (;;) {
                int localReadBytes = buffer.writeBytes(in, offset + readBytes, chunkSize - readBytes);
                if (localReadBytes < 0) {
                    break;
                }
                readBytes += localReadBytes;
                if (readBytes == chunkSize) {
                    break;
                }
            }
            this.offset += readBytes;
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
        return endOffset - startOffset;
    }

    @Override
    public long progress() {
        return offset - startOffset;
    }
}
