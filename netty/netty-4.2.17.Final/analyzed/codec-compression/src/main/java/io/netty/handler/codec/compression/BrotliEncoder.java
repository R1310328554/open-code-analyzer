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

import com.aayushatharva.brotli4j.encoder.BrotliEncoderChannel;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;

/**
 * 使用 Brotli 算法压缩 {@link ByteBuf} 的 {@link MessageToByteEncoder}。
 * <p>
 * 参见 <a href="https://github.com/google/brotli">brotli</a>。
 */
@ChannelHandler.Sharable
public final class BrotliEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final AttributeKey<Writer> ATTR = AttributeKey.valueOf("BrotliEncoderWriter");

    private final Encoder.Parameters parameters;
    private final boolean isSharable;
    private Writer writer;

    /**
     * 使用 {@link BrotliOptions#DEFAULT} 创建可共享实例（{@link #isSharable()} 为 {@code true}）。
     */
    public BrotliEncoder() {
        this(BrotliOptions.DEFAULT);
    }

    /**
     * 使用指定 {@link BrotliOptions} 创建可共享编码器。
     *
     * @param brotliOptions {@link BrotliOptions} to use and
     *                      {@link #isSharable()} set to {@code true}
     */
    public BrotliEncoder(BrotliOptions brotliOptions) {
        this(brotliOptions.parameters());
    }

    /**
     * 使用 brotli4j {@link Encoder.Parameters} 创建可共享编码器。
     *
     * @param parameters {@link Encoder.Parameters} to use
     */
    public BrotliEncoder(Encoder.Parameters parameters) {
        this(parameters, true);
    }

    /**
     * <p>
     * Create a new {@link BrotliEncoder} Instance and specify
     * whether this instance will be shared with multiple pipelines or not.
     * </p>
     *
     * 若 {@link #isSharable()} 为 {@code true}，每个 Channel 在 {@link #handlerAdded} 时创建独立 {@link Writer}
     * 并存入 {@link Channel#attr}，以便多 pipeline 共享同一 Handler；但每次编码需查找 Writer，有额外开销。
     * 建议设为 {@code false} 并为每个 pipeline 创建独立 {@link BrotliEncoder} 实例。
     *
     * @param parameters {@link Encoder.Parameters} to use
     * @param isSharable Set to {@code true} if this instance is shared else set to {@code false}
     */
    public BrotliEncoder(Encoder.Parameters parameters, boolean isSharable) {
        super(ByteBuf.class);
        this.parameters = ObjectUtil.checkNotNull(parameters, "Parameters");
        this.isSharable = isSharable;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Writer writer = new Writer(parameters, ctx);
        if (isSharable) {
            ctx.channel().attr(ATTR).set(writer);
        } else {
            this.writer = writer;
        }
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        finish(ctx);
        super.handlerRemoved(ctx);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        // NO-OP
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception {
        if (!msg.isReadable()) {
            return Unpooled.EMPTY_BUFFER;
        }

        Writer writer;
        if (isSharable) {
            writer = ctx.channel().attr(ATTR).get();
        } else {
            writer = this.writer;
        }

        // Writer 为 null 表示编码器尚未就绪或已关闭
        if (writer == null) {
            return Unpooled.EMPTY_BUFFER;
        } else {
            writer.encode(msg, preferDirect);
            return writer.writableBuffer;
        }
    }

    @Override
    public boolean isSharable() {
        return isSharable;
    }

    /**
     * 结束编码、刷出尾部数据并将最终 {@link ByteBuf} 写入通道。
     *
     * @param ctx {@link ChannelHandlerContext} which we want to close
     * @throws IOException If an error occurred during closure
     */
    public void finish(ChannelHandlerContext ctx) throws IOException {
        finishEncode(ctx);
    }

    private ChannelFuture finishEncode(ChannelHandlerContext ctx) throws IOException {
        Writer writer;

        if (isSharable) {
            writer = ctx.channel().attr(ATTR).getAndSet(null);
        } else {
            writer = this.writer;
        }

        if (writer != null) {
            writer.close();
            this.writer = null;
            return writer.closeFuture;
        }
        return ctx.newSucceededFuture();
    }

    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ChannelFuture f = finishEncode(ctx);
        EncoderUtil.closeAfterFinishEncode(ctx, f, promise);
    }

    /**
     * 内部 {@link WritableByteChannel} 实现：接收 brotli4j 输出并写入 {@link ByteBuf}。
     */
    private static final class Writer implements WritableByteChannel {

        private ByteBuf writableBuffer;
        private final BrotliEncoderChannel brotliEncoderChannel;
        private final ChannelHandlerContext ctx;
        private final ChannelPromise closeFuture;
        private boolean closeInitiated;
        private boolean isClosed;

        private Writer(Encoder.Parameters parameters, ChannelHandlerContext ctx) throws IOException {
            brotliEncoderChannel = new BrotliEncoderChannel(this, parameters);
            this.ctx = ctx;
            this.closeFuture = ctx.newPromise();
        }

        private void encode(ByteBuf msg, boolean preferDirect) throws Exception {
            try {
                allocate(preferDirect);

                // 压缩并 flush；flush 触发编码器向 WritableByteChannel 写入
                // 一次 flush 对应一次 write，无竞态
                ByteBuffer nioBuffer = CompressionUtil.safeReadableNioBuffer(msg);
                int position = nioBuffer.position();
                brotliEncoderChannel.write(nioBuffer);
                msg.skipBytes(nioBuffer.position() - position);
                brotliEncoderChannel.flush();
            } catch (Exception e) {
                ReferenceCountUtil.release(msg);
                throw e;
            }
        }

        private void allocate(boolean preferDirect) {
            if (preferDirect) {
                writableBuffer = ctx.alloc().ioBuffer();
            } else {
                writableBuffer = ctx.alloc().buffer();
            }
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }

            return writableBuffer.writeBytes(src).readableBytes();
        }

        @Override
        public boolean isOpen() {
            return !isClosed;
        }

        @Override
        public void close() {
            if (closeInitiated) {
                return;
            }
            closeInitiated = true;
            ctx.executor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        finish(closeFuture);
                    } catch (IOException ex) {
                        closeFuture.setFailure(new IllegalStateException("Failed to finish encoding", ex));
                    }
                }
            });
        }

        public void finish(final ChannelPromise promise) throws IOException {
            if (!isClosed) {
                // 分配缓冲并写入编码器关闭时的尾部数据
                allocate(true);

                try {
                    brotliEncoderChannel.close();
                    isClosed = true;
                } catch (Exception ex) {
                    promise.setFailure(ex);

                    // 关闭失败时释放已分配缓冲，防止泄漏
                    ReferenceCountUtil.release(writableBuffer);
                    return;
                }

                ctx.writeAndFlush(writableBuffer, promise);
            }
        }
    }
}
