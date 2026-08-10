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

/**
 * 按块从文件读取数据的 {@link ChunkedInput}。
 * <p>
 * 若操作系统支持
 * <a href="https://en.wikipedia.org/wiki/Zero-copy">零拷贝文件传输</a>
 * （如 {@code sendfile()}），可考虑使用 {@link FileRegion}。
 */
public class ChunkedFile implements ChunkedInput<ByteBuf> {

    /** 底层随机访问文件。 */
    private final RandomAccessFile file;
    /** 传输起始偏移。 */
    private final long startOffset;
    /** 传输结束偏移（不含）。 */
    private final long endOffset;
    /** 每次 readChunk 读取的最大字节数。 */
    private final int chunkSize;
    /** 当前读指针位置。 */
    private long offset;

    /**
     * 从指定文件创建实例（默认块大小）。
     */
    public ChunkedFile(File file) throws IOException {
        this(file, ChunkedStream.DEFAULT_CHUNK_SIZE);
    }

    /**
     * 从指定文件创建实例。
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedFile(File file, int chunkSize) throws IOException {
        this(new RandomAccessFile(file, "r"), chunkSize);
    }

    /**
     * 从 RandomAccessFile 创建实例（默认块大小）。
     */
    public ChunkedFile(RandomAccessFile file) throws IOException {
        this(file, ChunkedStream.DEFAULT_CHUNK_SIZE);
    }

    /**
     * 从 RandomAccessFile 创建实例。
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedFile(RandomAccessFile file, int chunkSize) throws IOException {
        this(file, 0, file.length(), chunkSize);
    }

    /**
     * 从文件指定区间创建实例。
     *
     * @param offset the offset of the file where the transfer begins
     * @param length the number of bytes to transfer
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedFile(RandomAccessFile file, long offset, long length, int chunkSize) throws IOException {
        ObjectUtil.checkNotNull(file, "file");
        ObjectUtil.checkPositiveOrZero(offset, "offset");
        ObjectUtil.checkPositiveOrZero(length, "length");
        ObjectUtil.checkPositive(chunkSize, "chunkSize");

        this.file = file;
        this.offset = startOffset = offset;
        this.endOffset = offset + length;
        this.chunkSize = chunkSize;

        file.seek(offset);
    }

    /**
     * 返回传输起始文件偏移。
     */
    public long startOffset() {
        return startOffset;
    }

    /**
     * 返回传输结束文件偏移（不含）。
     */
    public long endOffset() {
        return endOffset;
    }

    /**
     * 返回当前读指针位置。
     */
    public long currentOffset() {
        return offset;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        return !(offset < endOffset && file.getChannel().isOpen());
    }

    @Override
    public void close() throws Exception {
        file.close();
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
        // 堆缓冲区可直接 readFully 到 array，避免额外拷贝

        ByteBuf buf = allocator.heapBuffer(chunkSize);
        boolean release = true;
        try {
            file.readFully(buf.array(), buf.arrayOffset(), chunkSize);
            buf.writerIndex(chunkSize);
            this.offset = offset + chunkSize;
            release = false;
            return buf;
        } finally {
            if (release) {
                buf.release();
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
