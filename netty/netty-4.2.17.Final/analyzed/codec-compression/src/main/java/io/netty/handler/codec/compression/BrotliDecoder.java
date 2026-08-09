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

import com.aayushatharva.brotli4j.decoder.DecoderJNI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.ObjectUtil;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 将 Brotli 格式压缩的 {@link ByteBuf} 解压为明文。
 * <p>
 * 参见 <a href="https://github.com/google/brotli">brotli</a>。
 */
public final class BrotliDecoder extends ByteToMessageDecoder {

    private static final int DEFAULT_MAX_FORWARD_BYTES = CompressionUtil.DEFAULT_MAX_FORWARD_BYTES;
    private static final int DEFAULT_INPUT_BUFFER_SIZE = 8 * 1024;

    /** 内部解压状态机。 */
    private enum State {
        /** 流已结束。 */ DONE,
        /** 需要更多输入。 */ NEEDS_MORE_INPUT,
        /** 数据损坏或解码错误。 */ ERROR
    }

    static {
        try {
            Brotli.ensureAvailability();
        } catch (Throwable throwable) {
            throw new ExceptionInInitializerError(throwable);
        }
    }

    private final int inputBufferSize;
    private final int outputBufferSize;
    private DecoderJNI.Wrapper decoder;
    private boolean destroyed;
    private boolean needsRead;
    private ByteBuf accumBuffer;

    /**
     * 使用默认 8KB 输入缓冲区创建解码器。
     */
    public BrotliDecoder() {
        this(DEFAULT_INPUT_BUFFER_SIZE);
    }

    /**
     * 指定输入缓冲区大小（字节）创建解码器。
     * @param inputBufferSize desired size of the input buffer in bytes
     */
    public BrotliDecoder(int inputBufferSize) {
        this(inputBufferSize == 0 ? DEFAULT_INPUT_BUFFER_SIZE : inputBufferSize, DEFAULT_MAX_FORWARD_BYTES);
    }

    /**
     * 同时指定输入与输出缓冲区上限；超出输出上限时分块向下游传递。
     * @param inputBufferSize desired size of the input buffer in bytes
     * @param outputBufferSize desired max size of the output buffer in bytes
     *                         (produce multiple output buffers if exceeded)
     */
    public BrotliDecoder(int inputBufferSize, int outputBufferSize) {
        this.inputBufferSize = ObjectUtil.checkPositive(inputBufferSize, "inputBufferSize");
        this.outputBufferSize = ObjectUtil.checkPositive(outputBufferSize, "outputBufferSize");
    }

    /**
     * 按 {@code maxAllocation} 限制单次向下游输出的解压块大小；
     * 输入缓冲区仍使用默认值。
     *
     * @param maxAllocation maximum size, in bytes, of each decompressed output
     *                      buffer forwarded downstream; if {@code 0}, the
     *                      decoder's default output cap is used.
     */
    public static BrotliDecoder newDecoderWithMaxAllocation(int maxAllocation) {
        ObjectUtil.checkPositiveOrZero(maxAllocation, "maxAllocation");
        return maxAllocation > 0 ?
                new BrotliDecoder(DEFAULT_INPUT_BUFFER_SIZE, maxAllocation) :
                new BrotliDecoder();
    }

    private void forwardOutput(ChannelHandlerContext ctx) {
        ByteBuffer nativeBuffer = decoder.pull(outputBufferSize);
        // nativeBuffer 指向 brotli 内部缓冲，须复制到 Netty ByteBuf
        int remaining = nativeBuffer.remaining();
        if (accumBuffer == null) {
            accumBuffer = ctx.alloc().buffer(remaining);
        }
        accumBuffer.writeBytes(nativeBuffer);
        needsRead = false;
        if (accumBuffer.readableBytes() >= outputBufferSize) {
            ctx.fireChannelRead(accumBuffer);
            accumBuffer = null;
        }
    }

    private void flushAccumBuffer(ChannelHandlerContext ctx) {
        if (accumBuffer != null && accumBuffer.isReadable()) {
            ctx.fireChannelRead(accumBuffer);
        } else if (accumBuffer != null) {
            accumBuffer.release();
        }
        accumBuffer = null;
    }

    private State decompress(ChannelHandlerContext ctx, ByteBuf input) {
        for (;;) {
            switch (decoder.getStatus()) {
                case DONE:
                    return State.DONE;

                case OK:
                    decoder.push(0);
                    break;

                case NEEDS_MORE_INPUT:
                    while (decoder.hasOutput()) {
                        forwardOutput(ctx);
                    }

                    if (!input.isReadable()) {
                        return State.NEEDS_MORE_INPUT;
                    }

                    ByteBuffer decoderInputBuffer = decoder.getInputBuffer();
                    decoderInputBuffer.clear();
                    int readBytes = readBytes(input, decoderInputBuffer);
                    decoder.push(readBytes);
                    break;

                case NEEDS_MORE_OUTPUT:
                    forwardOutput(ctx);
                    break;

                default:
                    return State.ERROR;
            }
        }
    }

    private static int readBytes(ByteBuf in, ByteBuffer dest) {
        int limit = Math.min(in.readableBytes(), dest.remaining());
        ByteBuffer slice = dest.slice();
        slice.limit(limit);
        in.readBytes(slice);
        dest.position(dest.position() + limit);
        return limit;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        decoder = new DecoderJNI.Wrapper(inputBufferSize);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        needsRead = true;
        if (destroyed) {
            // 流已结束，丢弃后续输入
            in.skipBytes(in.readableBytes());
            return;
        }

        if (!in.isReadable()) {
            return;
        }

        try {
            State state = decompress(ctx, in);
            if (state == State.DONE) {
                destroy();
            } else if (state == State.ERROR) {
                throw new DecompressionException("Brotli stream corrupted");
            }
        } catch (Exception e) {
            destroy();
            throw e;
        } finally {
            flushAccumBuffer(ctx);
        }
    }

    private void destroy() {
        if (!destroyed) {
            destroyed = true;
            decoder.destroy();
        }
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        try {
            destroy();
        } finally {
            super.handlerRemoved0(ctx);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            destroy();
        } finally {
            super.channelInactive(ctx);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 必要时丢弃 cumulation 中已读字节
        discardSomeReadBytes();

        if (needsRead && !ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
        ctx.fireChannelReadComplete();
    }
}
