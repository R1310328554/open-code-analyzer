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

import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;

import java.util.List;

/**
 * 基于 JZlib 库的 {@link ByteBuf} 解压缩解码器，使用 deflate 算法。
 */
public class JZlibDecoder extends ZlibDecoder {

    private final Inflater z = new Inflater();
    private byte[] dictionary;
    private static final int DEFAULT_MAX_FORWARD_BYTES = CompressionUtil.DEFAULT_MAX_FORWARD_BYTES;
    private final int maxForwardBytes;
    private boolean needsRead;
    private volatile boolean finished;

    /**
     * 使用默认封装格式（{@link ZlibWrapper#ZLIB}）创建实例。
     *
     * @throws DecompressionException 若 zlib 初始化失败
     * @deprecated 请使用 {@link JZlibDecoder#JZlibDecoder(int)}。
     */
    @Deprecated
    public JZlibDecoder() {
        this(ZlibWrapper.ZLIB, 0);
    }

    /**
     * 使用默认封装格式（{@link ZlibWrapper#ZLIB}）及指定最大缓冲分配创建实例。
     *
     * @param maxAllocation
     *          解压缩缓冲区的最大尺寸，须 &gt;= 0。
     *          为 0 时由 {@link ByteBufAllocator} 决定上限。
     *
     * @throws DecompressionException 若 zlib 初始化失败
     */
    public JZlibDecoder(int maxAllocation) {
        this(ZlibWrapper.ZLIB, maxAllocation);
    }

    /**
     * 使用指定封装格式创建实例。
     *
     * @throws DecompressionException 若 zlib 初始化失败
     * @deprecated 请使用 {@link JZlibDecoder#JZlibDecoder(ZlibWrapper, int)}。
     */
    @Deprecated
    public JZlibDecoder(ZlibWrapper wrapper) {
        this(wrapper, 0);
    }

    /**
     * 使用指定封装格式及最大缓冲分配创建实例。
     *
     * @param maxAllocation
     *          解压缩缓冲区的最大尺寸，须 &gt;= 0。
     *          为 0 时由 {@link ByteBufAllocator} 决定上限。
     *
     * @throws DecompressionException 若 zlib 初始化失败
     */
    public JZlibDecoder(ZlibWrapper wrapper, int maxAllocation) {
        super(maxAllocation);
        this.maxForwardBytes = maxAllocation > 0 ? maxAllocation : DEFAULT_MAX_FORWARD_BYTES;

        ObjectUtil.checkNotNull(wrapper, "wrapper");

        int resultCode = z.init(ZlibUtil.convertWrapperType(wrapper));
        if (resultCode != JZlib.Z_OK) {
            ZlibUtil.fail(z, "initialization failure", resultCode);
        }
    }

    /**
     * 使用指定预置字典创建实例。封装格式固定为 {@link ZlibWrapper#ZLIB}，
     * 因为只有该格式支持预置字典。
     *
     * @throws DecompressionException 若 zlib 初始化失败
     * @deprecated 请使用 {@link JZlibDecoder#JZlibDecoder(byte[], int)}。
     */
    @Deprecated
    public JZlibDecoder(byte[] dictionary) {
        this(dictionary, 0);
    }

    /**
     * 使用指定预置字典及最大缓冲分配创建实例。封装格式固定为 {@link ZlibWrapper#ZLIB}。
     *
     * @param maxAllocation
     *          解压缩缓冲区的最大尺寸，须 &gt;= 0。
     *          为 0 时由 {@link ByteBufAllocator} 决定上限。
     *
     * @throws DecompressionException 若 zlib 初始化失败
     */
    public JZlibDecoder(byte[] dictionary, int maxAllocation) {
        super(maxAllocation);
        this.maxForwardBytes = maxAllocation > 0 ? maxAllocation : DEFAULT_MAX_FORWARD_BYTES;
        this.dictionary = ObjectUtil.checkNotNull(dictionary, "dictionary");
        int resultCode;
        resultCode = z.inflateInit(JZlib.W_ZLIB);
        if (resultCode != JZlib.Z_OK) {
            ZlibUtil.fail(z, "initialization failure", resultCode);
        }
    }

    /**
     * 当且仅当已到达压缩流末尾时返回 {@code true}。
     */
    @Override
    public boolean isClosed() {
        return finished;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        needsRead = true;
        if (finished) {
            // 已完成时跳过后续到达的数据
            in.skipBytes(in.readableBytes());
            return;
        }

        final int inputLength = in.readableBytes();
        if (inputLength == 0) {
            return;
        }

        try {
            // 配置输入缓冲
            z.avail_in = inputLength;
            if (in.hasArray()) {
                z.next_in = in.array();
                z.next_in_index = in.arrayOffset() + in.readerIndex();
            } else {
                byte[] array = new byte[inputLength];
                in.getBytes(in.readerIndex(), array);
                z.next_in = array;
                z.next_in_index = 0;
            }
            final int oldNextInIndex = z.next_in_index;

            // 配置输出缓冲
            ByteBuf decompressed = prepareDecompressBuffer(ctx, null, inputLength << 1);

            try {
                loop: for (;;) {
                    decompressed = prepareDecompressBuffer(ctx, decompressed, z.avail_in << 1);
                    z.avail_out = decompressed.writableBytes();
                    z.next_out = decompressed.array();
                    z.next_out_index = decompressed.arrayOffset() + decompressed.writerIndex();
                    int oldNextOutIndex = z.next_out_index;

                    // 将输入解压到输出缓冲
                    int resultCode = z.inflate(JZlib.Z_SYNC_FLUSH);
                    int outputLength = z.next_out_index - oldNextOutIndex;
                    if (outputLength > 0) {
                        decompressed.writerIndex(decompressed.writerIndex() + outputLength);
                        if (maxAllocation == 0 && decompressed.readableBytes() >= maxForwardBytes) {
                            // 未限制最大分配时，缓冲达到阈值后直接向下游转发
                            ByteBuf buffer = decompressed;
                            decompressed = null;
                            needsRead = false;
                            ctx.fireChannelRead(buffer);
                        }
                    }

                    switch (resultCode) {
                    case JZlib.Z_NEED_DICT:
                        if (dictionary == null) {
                            ZlibUtil.fail(z, "decompression failure", resultCode);
                        } else {
                            resultCode = z.inflateSetDictionary(dictionary, dictionary.length);
                            if (resultCode != JZlib.Z_OK) {
                                ZlibUtil.fail(z, "failed to set the dictionary", resultCode);
                            }
                        }
                        break;
                    case JZlib.Z_STREAM_END:
                        finished = true; // 不再继续解码
                        z.inflateEnd();
                        break loop;
                    case JZlib.Z_OK:
                        break;
                    case JZlib.Z_BUF_ERROR:
                        if (z.avail_in <= 0) {
                            break loop;
                        }
                        break;
                    default:
                        ZlibUtil.fail(z, "decompression failure", resultCode);
                    }
                }
            } finally {
                in.skipBytes(z.next_in_index - oldNextInIndex);
                if (decompressed != null) {
                    if (decompressed.isReadable()) {
                        needsRead = false;
                        ctx.fireChannelRead(decompressed);
                    } else {
                        decompressed.release();
                    }
                }
            }
        } finally {
            // 显式解除对外部数组的引用，提示 VM 这些字节数组为临时分配
            // 以便复用调用栈（现代 VM 是否仍做此优化尚不确定）
            z.next_in = null;
            z.next_out = null;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 必要时丢弃累积缓冲中已读字节
        discardSomeReadBytes();

        if (needsRead && !ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    protected void decompressionBufferExhausted(ByteBuf buffer) {
        finished = true;
    }
}
