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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.PromiseNotifier;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * 使用 JDK {@link Deflater} 对 {@link ByteBuf} 进行 deflate 压缩。
 */
public class JdkZlibEncoder extends ZlibEncoder {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JdkZlibEncoder.class);

    /** 压缩输出临时堆缓冲的初始大小上限，必要时仍可扩容。 */
    private static final int MAX_INITIAL_OUTPUT_BUFFER_SIZE;
    /** 将输入复制到堆上时临时缓冲的最大尺寸。 */
    private static final int MAX_INPUT_BUFFER_SIZE;

    private final ZlibWrapper wrapper;
    private final Deflater deflater;
    private volatile boolean finished;
    private volatile ChannelHandlerContext ctx;

    /* GZIP 支持 */
    private final CRC32 crc;
    private static final byte[] gzipHeader = {0x1f, (byte) 0x8b, Deflater.DEFLATED, 0, 0, 0, 0, 0, 0, 0};
    private boolean writeHeader = true;

    static {
        MAX_INITIAL_OUTPUT_BUFFER_SIZE = SystemPropertyUtil.getInt(
                "io.netty.jdkzlib.encoder.maxInitialOutputBufferSize",
                65536);
        MAX_INPUT_BUFFER_SIZE = SystemPropertyUtil.getInt(
                "io.netty.jdkzlib.encoder.maxInputBufferSize",
                65536);

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.jdkzlib.encoder.maxInitialOutputBufferSize={}", MAX_INITIAL_OUTPUT_BUFFER_SIZE);
            logger.debug("-Dio.netty.jdkzlib.encoder.maxInputBufferSize={}", MAX_INPUT_BUFFER_SIZE);
        }
    }

    /**
     * Creates a new zlib encoder with a compression level of ({@code 6})
     * and the default wrapper ({@link ZlibWrapper#ZLIB}).
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public JdkZlibEncoder() {
        this(6);
    }

    /**
     * Creates a new zlib encoder with the specified {@code compressionLevel}
     * and the default wrapper ({@link ZlibWrapper#ZLIB}).
     *
     * @param compressionLevel
     *        {@code 1} yields the fastest compression and {@code 9} yields the
     *        best compression.  {@code 0} means no compression.  The default
     *        compression level is {@code 6}.
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public JdkZlibEncoder(int compressionLevel) {
        this(ZlibWrapper.ZLIB, compressionLevel);
    }

    /**
     * Creates a new zlib encoder with a compression level of ({@code 6})
     * and the specified wrapper.
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public JdkZlibEncoder(ZlibWrapper wrapper) {
        this(wrapper, 6);
    }

    /**
     * Creates a new zlib encoder with the specified {@code compressionLevel}
     * and the specified wrapper.
     *
     * @param compressionLevel
     *        {@code 1} yields the fastest compression and {@code 9} yields the
     *        best compression.  {@code 0} means no compression.  The default
     *        compression level can be set as {@code -1} which correlates to the underlying
     *        {@link Deflater#DEFAULT_COMPRESSION} level.
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public JdkZlibEncoder(ZlibWrapper wrapper, int compressionLevel) {
        ObjectUtil.checkInRange(compressionLevel, Deflater.DEFAULT_COMPRESSION, Deflater.BEST_COMPRESSION,
                "compressionLevel");
        ObjectUtil.checkNotNull(wrapper, "wrapper");

        if (wrapper == ZlibWrapper.ZLIB_OR_NONE) {
            throw new IllegalArgumentException(
                    "wrapper '" + ZlibWrapper.ZLIB_OR_NONE + "' is not " +
                    "allowed for compression.");
        }

        this.wrapper = wrapper;
        deflater = new Deflater(compressionLevel, wrapper != ZlibWrapper.ZLIB);
        this.crc = wrapper == ZlibWrapper.GZIP ? new CRC32() : null;
    }

    /**
     * Creates a new zlib encoder with a compression level of ({@code 6})
     * and the specified preset dictionary.  The wrapper is always
     * {@link ZlibWrapper#ZLIB} because it is the only format that supports
     * the preset dictionary.
     *
     * @param dictionary  the preset dictionary
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public JdkZlibEncoder(byte[] dictionary) {
        this(6, dictionary);
    }

    /**
     * Creates a new zlib encoder with the specified {@code compressionLevel}
     * and the specified preset dictionary.  The wrapper is always
     * {@link ZlibWrapper#ZLIB} because it is the only format that supports
     * the preset dictionary.
     *
     * @param compressionLevel
     *        {@code 1} yields the fastest compression and {@code 9} yields the
     *        best compression.  {@code 0} means no compression.  The default
     *        compression level can be set as {@code -1} which correlates to the underlying
     *        {@link Deflater#DEFAULT_COMPRESSION} level.
     * @param dictionary  the preset dictionary
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public JdkZlibEncoder(int compressionLevel, byte[] dictionary) {
        ObjectUtil.checkInRange(compressionLevel, Deflater.DEFAULT_COMPRESSION, Deflater.BEST_COMPRESSION,
                "compressionLevel");
        ObjectUtil.checkNotNull(dictionary, "dictionary");

        wrapper = ZlibWrapper.ZLIB;
        deflater = new Deflater(compressionLevel);
        deflater.setDictionary(dictionary);
        crc = null;
    }

    @Override
    public ChannelFuture close() {
        return close(ctx().newPromise());
    }

    @Override
    public ChannelFuture close(final ChannelPromise promise) {
        ChannelHandlerContext ctx = ctx();
        EventExecutor executor = ctx.executor();
        if (executor.inEventLoop()) {
            return finishEncode(ctx, promise);
        } else {
            final ChannelPromise p = ctx.newPromise();
            executor.execute(() -> {
                ChannelFuture f = finishEncode(ctx(), p);
                PromiseNotifier.cascade(f, promise);
            });
            return p;
        }
    }

    private ChannelHandlerContext ctx() {
        ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            throw new IllegalStateException("not added to a pipeline");
        }
        return ctx;
    }

    @Override
    public boolean isClosed() {
        return finished;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf uncompressed, ByteBuf out) throws Exception {
        if (finished) {
            out.writeBytes(uncompressed);
            return;
        }

        int len = uncompressed.readableBytes();
        if (len == 0) {
            return;
        }

        if (uncompressed.hasArray()) {
            // 若底层为数组则无需额外复制
            encodeSome(uncompressed, out);
        } else {
            int heapBufferSize = Math.min(len, MAX_INPUT_BUFFER_SIZE);
            ByteBuf heapBuf = ctx.alloc().heapBuffer(heapBufferSize, heapBufferSize);
            try {
                while (uncompressed.isReadable()) {
                    uncompressed.readBytes(heapBuf, Math.min(heapBuf.writableBytes(), uncompressed.readableBytes()));
                    encodeSome(heapBuf, out);
                    heapBuf.clear();
                }
            } finally {
                heapBuf.release();
            }
        }
        // 清空输入引用，避免长期持有输入数组
        deflater.setInput(EmptyArrays.EMPTY_BYTES);
    }

    private void encodeSome(ByteBuf in, ByteBuf out) {
        // 此处 in 与 out 均为堆缓冲

        byte[] inAry = in.array();
        int offset = in.arrayOffset() + in.readerIndex();

        if (writeHeader) {
            writeHeader = false;
            if (wrapper == ZlibWrapper.GZIP) {
                out.writeBytes(gzipHeader);
            }
        }

        int len = in.readableBytes();
        if (wrapper == ZlibWrapper.GZIP) {
            crc.update(inAry, offset, len);
        }

        deflater.setInput(inAry, offset, len);
        for (;;) {
            deflate(out);
            if (!out.isWritable()) {
                // 缓冲不可写时扩容；不能仅依赖 needsInput，可能仍有待写出数据
                out.ensureWritable(out.writerIndex());
            } else if (deflater.needsInput()) {
                // 输入已全部消费
                break;
            }
        }
        in.skipBytes(len);
    }

    @Override
    protected final ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg,
                                           boolean preferDirect) throws Exception {
        int sizeEstimate = (int) Math.ceil(msg.readableBytes() * 1.001) + 12;
        if (writeHeader) {
            switch (wrapper) {
                case GZIP:
                    sizeEstimate += gzipHeader.length;
                    break;
                case ZLIB:
                    sizeEstimate += 2; // first two magic bytes
                    break;
                default:
                    // no op
            }
        }
        // 接近 2GB 时 sizeEstimate 可能溢出
        if (sizeEstimate < 0 || sizeEstimate > MAX_INITIAL_OUTPUT_BUFFER_SIZE) {
            // 后续仍可继续扩容
            return ctx.alloc().heapBuffer(MAX_INITIAL_OUTPUT_BUFFER_SIZE);
        }
        return ctx.alloc().heapBuffer(sizeEstimate);
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
        ByteBuf footer = ctx.alloc().heapBuffer();
        if (writeHeader && wrapper == ZlibWrapper.GZIP) {
            // 若尚未写出则先写 GZIP 头（用户未写入任何数据时）
            writeHeader = false;
            footer.writeBytes(gzipHeader);
        }

        deflater.finish();

        while (!deflater.finished()) {
            deflate(footer);
            if (!footer.isWritable()) {
                // 空间不足则先写出再继续分配
                ctx.write(footer);
                footer = ctx.alloc().heapBuffer();
            }
        }
        if (wrapper == ZlibWrapper.GZIP) {
            int crcValue = (int) crc.getValue();
            int uncBytes = deflater.getTotalIn();
            footer.writeIntLE(crcValue);
            footer.writeIntLE(uncBytes);
        }
        deflater.end();
        return ctx.writeAndFlush(footer, promise);
    }

    private void deflate(ByteBuf out) {
        int numBytes;
        do {
            int writerIndex = out.writerIndex();
            numBytes = deflater.deflate(
                    out.array(), out.arrayOffset() + writerIndex, out.writableBytes(), Deflater.SYNC_FLUSH);
            out.writerIndex(writerIndex + numBytes);
        } while (numBytes > 0);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }
}
