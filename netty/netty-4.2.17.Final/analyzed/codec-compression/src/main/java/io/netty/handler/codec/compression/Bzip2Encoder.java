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
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.PromiseNotifier;

import static io.netty.handler.codec.compression.Bzip2Constants.BASE_BLOCK_SIZE;
import static io.netty.handler.codec.compression.Bzip2Constants.END_OF_STREAM_MAGIC_1;
import static io.netty.handler.codec.compression.Bzip2Constants.END_OF_STREAM_MAGIC_2;
import static io.netty.handler.codec.compression.Bzip2Constants.MAGIC_NUMBER;
import static io.netty.handler.codec.compression.Bzip2Constants.MAX_BLOCK_SIZE;
import static io.netty.handler.codec.compression.Bzip2Constants.MIN_BLOCK_SIZE;

/**
 * 使用 Bzip2 算法压缩 {@link ByteBuf} 的 {@link MessageToByteEncoder}。
 * 按块累积数据，块满或流关闭时写出压缩块与流尾。
 *
 * 参见 <a href="https://en.wikipedia.org/wiki/Bzip2">Bzip2</a>。
 */
public class Bzip2Encoder extends MessageToByteEncoder<ByteBuf> {
    /** 编码器当前状态。 */
    private enum State {
        INIT,
        INIT_BLOCK,
        WRITE_DATA,
        CLOSE_BLOCK
    }

    private State currentState = State.INIT;

    /** 块内位级写入器。 */
    private final Bzip2BitWriter writer = new Bzip2BitWriter();

    /** 流声明的最大块大小（解压后游程解码前的上限）。 */
    private final int streamBlockSize;

    /** 已压缩所有块的合并 CRC。 */
    private int streamCRC;

    /** 当前块的块级压缩器。 */
    private Bzip2BlockCompressor blockCompressor;

    /** 压缩流是否已结束（{@code true} 表示已写出流尾）。 */
    private volatile boolean finished;

    /** 关联的 {@link ChannelHandlerContext}，供 {@link #close()} 使用。 */
    private volatile ChannelHandlerContext ctx;

    /** 使用最大块大小（900000 字节，乘数 9）创建编码器。 */
    public Bzip2Encoder() {
        this(MAX_BLOCK_SIZE);
    }

    /**
     * Creates a new bzip2 encoder with the specified {@code blockSizeMultiplier}.
     * @param blockSizeMultiplier
     *        The Bzip2 block size as a multiple of 100,000 bytes (minimum {@code 1}, maximum {@code 9}).
     *        块越大占用内存越多，但压缩率通常更好；一般推荐 {@code 9}。
     */
    public Bzip2Encoder(final int blockSizeMultiplier) {
        super(ByteBuf.class);
        if (blockSizeMultiplier < MIN_BLOCK_SIZE || blockSizeMultiplier > MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "blockSizeMultiplier: " + blockSizeMultiplier + " (expected: 1-9)");
        }
        streamBlockSize = blockSizeMultiplier * BASE_BLOCK_SIZE;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        if (finished) {
            out.writeBytes(in);
            return;
        }

        for (;;) {
            switch (currentState) {
                case INIT:
                    out.ensureWritable(4);
                    out.writeMedium(MAGIC_NUMBER);
                    out.writeByte('0' + streamBlockSize / BASE_BLOCK_SIZE);
                    currentState = State.INIT_BLOCK;
                    // fall through
                case INIT_BLOCK:
                    blockCompressor = new Bzip2BlockCompressor(writer, streamBlockSize);
                    currentState = State.WRITE_DATA;
                    // fall through
                case WRITE_DATA:
                    if (!in.isReadable()) {
                        return;
                    }
                    Bzip2BlockCompressor blockCompressor = this.blockCompressor;
                    final int length = Math.min(in.readableBytes(), blockCompressor.availableSize());
                    final int bytesWritten = blockCompressor.write(in, in.readerIndex(), length);
                    in.skipBytes(bytesWritten);
                    if (!blockCompressor.isFull()) {
                        if (in.isReadable()) {
                            break;
                        } else {
                            return;
                        }
                    }
                    currentState = State.CLOSE_BLOCK;
                    // fall through
                case CLOSE_BLOCK:
                    closeBlock(out);
                    currentState = State.INIT_BLOCK;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * 关闭当前块并更新 {@link #streamCRC}。
     */
    private void closeBlock(ByteBuf out) {
        final Bzip2BlockCompressor blockCompressor = this.blockCompressor;
        if (!blockCompressor.isEmpty()) {
            blockCompressor.close(out);
            final int blockCRC = blockCompressor.crc();
            streamCRC = (streamCRC << 1 | streamCRC >>> 31) ^ blockCRC;
        }
    }

    /**
     * Returns {@code true} if and only if the end of the compressed stream has been reached.
     */
    public boolean isClosed() {
        return finished;
    }

    /**
     * 结束编码并写出 Bzip2 流尾；完成后通知返回的 {@link ChannelFuture}。
     */
    public ChannelFuture close() {
        return close(ctx().newPromise());
    }

    /**
     * 结束编码；完成后通知并返回给定的 {@link ChannelFuture}。
     */
    public ChannelFuture close(final ChannelPromise promise) {
        ChannelHandlerContext ctx = ctx();
        EventExecutor executor = ctx.executor();
        if (executor.inEventLoop()) {
            return finishEncode(ctx, promise);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ChannelFuture f = finishEncode(ctx(), promise);
                    PromiseNotifier.cascade(f, promise);
                }
            });
            return promise;
        }
    }

    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ChannelFuture f = finishEncode(ctx, ctx.newPromise());
        EncoderUtil.closeAfterFinishEncode(ctx, f, promise);
    }

    private ChannelFuture finishEncode(final ChannelHandlerContext ctx, ChannelPromise promise) {
        if (finished) {
            promise.setSuccess();
            return promise;
        }
        finished = true;

        final ByteBuf footer = ctx.alloc().buffer();
        closeBlock(footer);

        final int streamCRC = this.streamCRC;
        final Bzip2BitWriter writer = this.writer;
        try {
            writer.writeBits(footer, 24, END_OF_STREAM_MAGIC_1);
            writer.writeBits(footer, 24, END_OF_STREAM_MAGIC_2);
            writer.writeInt(footer, streamCRC);
            writer.flush(footer);
        } finally {
            blockCompressor = null;
        }
        return ctx.writeAndFlush(footer, promise);
    }

    private ChannelHandlerContext ctx() {
        ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            throw new IllegalStateException("not added to a pipeline");
        }
        return ctx;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }
}
