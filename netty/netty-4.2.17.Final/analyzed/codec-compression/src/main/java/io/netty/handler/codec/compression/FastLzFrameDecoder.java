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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import static io.netty.handler.codec.compression.FastLz.BLOCK_TYPE_COMPRESSED;
import static io.netty.handler.codec.compression.FastLz.BLOCK_WITH_CHECKSUM;
import static io.netty.handler.codec.compression.FastLz.MAGIC_NUMBER;
import static io.netty.handler.codec.compression.FastLz.decompress;

/**
 * 使用 FastLZ 算法解压 {@link FastLzFrameEncoder} 编码的 {@link ByteBuf}。
 * 格式见 <a href="https://github.com/netty/netty/issues/2750">FastLZ format</a>。
 */
public class FastLzFrameDecoder extends ByteToMessageDecoder {
    /** 解压状态机。 */
    private enum State {
        INIT_BLOCK,
        INIT_BLOCK_PARAMS,
        DECOMPRESS_DATA,
        CORRUPTED
    }

    private State currentState = State.INIT_BLOCK;

    /** 可选的块校验和计算器。 */
    private final ByteBufChecksum checksum;

    /** 当前块压缩数据长度。 */
    private int chunkLength;

    /** 当前块解压后原始长度；未压缩块等于 {@link #chunkLength}。 */
    private int originalLength;

    /** 当前块是否经 FastLZ 压缩。 */
    private boolean isCompressed;

    /** 当前块是否携带校验和字段。 */
    private boolean hasChecksum;

    /** 当前块期望的校验和值（若启用校验）。 */
    private int currentChecksum;

    /** 创建不校验校验和的最快 FastLZ 解码器。 */
    public FastLzFrameDecoder() {
        this(false);
    }

    /**
     * 创建 FastLZ 解码器并按需校验块校验和。
     *
     * @param validateChecksums
     *        为 {@code true} 时校验解压数据与块头校验和，不匹配则抛 {@link DecompressionException}；
     *        默认使用 {@link java.util.zip.Adler32}。
     */
    public FastLzFrameDecoder(boolean validateChecksums) {
        this(validateChecksums ? new Adler32() : null);
    }

    /**
     * 使用自定义 {@link Checksum} 创建 FastLZ 解码器。
     *
     * @param checksum
     *        块完整性校验器；{@code null} 表示不校验。
     */
    public FastLzFrameDecoder(Checksum checksum) {
        this.checksum = checksum == null ? null : ByteBufChecksum.wrapChecksum(checksum);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (currentState) {
            case INIT_BLOCK:
                if (in.readableBytes() < 4) {
                    break;
                }

                final int magic = in.readUnsignedMedium();
                if (magic != MAGIC_NUMBER) {
                    throw new DecompressionException("unexpected block identifier");
                }

                final byte options = in.readByte();
                isCompressed = (options & 0x01) == BLOCK_TYPE_COMPRESSED;
                hasChecksum = (options & 0x10) == BLOCK_WITH_CHECKSUM;

                currentState = State.INIT_BLOCK_PARAMS;
                // fall through
            case INIT_BLOCK_PARAMS:
                if (in.readableBytes() < 2 + (isCompressed ? 2 : 0) + (hasChecksum ? 4 : 0)) {
                    break;
                }
                currentChecksum = hasChecksum ? in.readInt() : 0;
                chunkLength = in.readUnsignedShort();
                originalLength = isCompressed ? in.readUnsignedShort() : chunkLength;

                currentState = State.DECOMPRESS_DATA;
                // fall through
            case DECOMPRESS_DATA:
                final int chunkLength = this.chunkLength;
                if (in.readableBytes() < chunkLength) {
                    break;
                }

                final int idx = in.readerIndex();
                final int originalLength = this.originalLength;

                ByteBuf output = null;

                try {
                    if (isCompressed) {

                        output = ctx.alloc().buffer(originalLength);
                        int outputOffset = output.writerIndex();
                        final int decompressedBytes = decompress(in, idx, chunkLength,
                                output, outputOffset, originalLength);
                        if (originalLength != decompressedBytes) {
                            throw new DecompressionException(String.format(
                                    "stream corrupted: originalLength(%d) and actual length(%d) mismatch",
                                    originalLength, decompressedBytes));
                        }
                        output.writerIndex(output.writerIndex() + decompressedBytes);
                    } else {
                        output = in.retainedSlice(idx, chunkLength);
                    }

                    final ByteBufChecksum checksum = this.checksum;
                    if (hasChecksum && checksum != null) {
                        checksum.reset();
                        checksum.update(output, output.readerIndex(), output.readableBytes());
                        final int checksumResult = (int) checksum.getValue();
                        if (checksumResult != currentChecksum) {
                            throw new DecompressionException(String.format(
                                    "stream corrupted: mismatching checksum: %d (expected: %d)",
                                    checksumResult, currentChecksum));
                        }
                    }

                    if (output.readableBytes() > 0) {
                        out.add(output);
                    } else {
                        output.release();
                    }
                    output = null;
                    in.skipBytes(chunkLength);

                    currentState = State.INIT_BLOCK;
                } finally {
                    if (output != null) {
                        output.release();
                    }
                }
                break;
            case CORRUPTED:
                in.skipBytes(in.readableBytes());
                break;
            default:
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            currentState = State.CORRUPTED;
            throw e;
        }
    }
}
