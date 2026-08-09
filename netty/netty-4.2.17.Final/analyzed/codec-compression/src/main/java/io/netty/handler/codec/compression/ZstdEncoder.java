/*
 * Copyright 2021 The Netty Project
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

import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.internal.ObjectUtil;
import java.nio.ByteBuffer;

import static io.netty.handler.codec.compression.ZstdConstants.DEFAULT_COMPRESSION_LEVEL;
import static io.netty.handler.codec.compression.ZstdConstants.MIN_COMPRESSION_LEVEL;
import static io.netty.handler.codec.compression.ZstdConstants.MAX_COMPRESSION_LEVEL;
import static io.netty.handler.codec.compression.ZstdConstants.DEFAULT_BLOCK_SIZE;
import static io.netty.handler.codec.compression.ZstdConstants.DEFAULT_MAX_ENCODE_SIZE;

/**
 * 使用 Zstandard 算法压缩 {@link ByteBuf}。
 * 参见 <a href="https://facebook.github.io/zstd">Zstandard</a>。
 */
public final class ZstdEncoder extends MessageToByteEncoder<ByteBuf> {
    // 不用 static，以便在类加载失败时仍能加载本类
    {
        try {
            io.netty.handler.codec.compression.Zstd.ensureAvailability();
        } catch (Throwable throwable) {
            throw new ExceptionInInitializerError(throwable);
        }
    }
    private final int blockSize;
    private final int compressionLevel;
    private final int maxEncodeSize;
    private ByteBuf buffer;

    /**
     * 创建默认 Zstd 编码器。
     *
     * 默认构造使用默认 BLOCK_SIZE 与 MAX_BLOCK_SIZE；
     * 若需自定义，请使用 {@link ZstdEncoder(int,int)}。
     */
    public ZstdEncoder() {
        this(DEFAULT_COMPRESSION_LEVEL, DEFAULT_BLOCK_SIZE, DEFAULT_MAX_ENCODE_SIZE);
    }

    /**
     * 创建指定压缩级别的 Zstd 编码器。
     *  @param  compressionLevel
     *            压缩级别
     */
    public ZstdEncoder(int compressionLevel) {
        this(compressionLevel, DEFAULT_BLOCK_SIZE, DEFAULT_MAX_ENCODE_SIZE);
    }

    /**
     * 创建指定块大小与最大编码尺寸的 Zstd 编码器。
     *  @param  blockSize
     *            用于分块压缩的块大小
     *  @param  maxEncodeSize
     *            单块压缩结果允许的最大字节数
     */
    public ZstdEncoder(int blockSize, int maxEncodeSize) {
        this(DEFAULT_COMPRESSION_LEVEL, blockSize, maxEncodeSize);
    }

    /**
     * @param  blockSize
     *           分块压缩的块大小
     * @param  maxEncodeSize
     *           单块压缩结果允许的最大字节数
     * @param  compressionLevel
     *           压缩级别
     */
    public ZstdEncoder(int compressionLevel, int blockSize, int maxEncodeSize) {
        super(ByteBuf.class, true);
        this.compressionLevel = ObjectUtil.checkInRange(compressionLevel,
                MIN_COMPRESSION_LEVEL, MAX_COMPRESSION_LEVEL, "compressionLevel");
        this.blockSize = ObjectUtil.checkPositive(blockSize, "blockSize");
        this.maxEncodeSize = ObjectUtil.checkPositive(maxEncodeSize, "maxEncodeSize");
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
        if (buffer == null) {
            throw new IllegalStateException("not added to a pipeline," +
                    "or has been removed,buffer is null");
        }

        int remaining = msg.readableBytes() + buffer.readableBytes();

        // 快速溢出检查
        if (remaining < 0) {
            throw new EncoderException("too much data to allocate a buffer for compression");
        }

        long bufferSize = 0;
        while (remaining > 0) {
            int curSize = Math.min(blockSize, remaining);
            remaining -= curSize;
            // 用 Zstd.compressBound 估算最大压缩后尺寸
            bufferSize = Math.max(bufferSize, Zstd.compressBound(curSize));
        }

        if (bufferSize > maxEncodeSize || 0 > bufferSize) {
            throw new EncoderException("requested encode buffer size (" + bufferSize + " bytes) exceeds " +
                    "the maximum allowable size (" + maxEncodeSize + " bytes)");
        }

        return ctx.alloc().directBuffer((int) bufferSize);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        if (buffer == null) {
            throw new IllegalStateException("not added to a pipeline," +
                    "or has been removed,buffer is null");
        }

        final ByteBuf buffer = this.buffer;
        int length;
        while ((length = in.readableBytes()) > 0) {
            final int nextChunkSize = Math.min(length, buffer.writableBytes());
            in.readBytes(buffer, nextChunkSize);

            if (!buffer.isWritable()) {
                flushBufferedData(out);
            }
        }
        // 块小于 blockSize 时刷出缓冲中剩余数据
        if (buffer.isReadable()) {
            flushBufferedData(out);
        }
    }

    private void flushBufferedData(ByteBuf out) {
        final int flushableBytes = buffer.readableBytes();
        if (flushableBytes == 0) {
            return;
        }

        final int bufSize = (int) Zstd.compressBound(flushableBytes);
        out.ensureWritable(bufSize);
        final int idx = out.writerIndex();
        int compressedLength;
        try {
            ByteBuffer outNioBuffer = out.internalNioBuffer(idx, out.writableBytes());
            compressedLength = Zstd.compress(
                    outNioBuffer,
                    buffer.internalNioBuffer(buffer.readerIndex(), flushableBytes),
                    compressionLevel);
        } catch (Exception e) {
            throw new CompressionException(e);
        }

        out.writerIndex(idx + compressedLength);
        buffer.clear();
    }

    @Override
    public void flush(final ChannelHandlerContext ctx) {
        if (buffer != null && buffer.isReadable()) {
            final ByteBuf buf = allocateBuffer(ctx, Unpooled.EMPTY_BUFFER, isPreferDirect());
            flushBufferedData(buf);
            ctx.write(buf);
        }
        ctx.flush();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buffer = ctx.alloc().directBuffer(blockSize);
        buffer.clear();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        if (buffer != null) {
            buffer.release();
            buffer = null;
        }
    }
}
