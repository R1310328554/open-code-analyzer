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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static io.netty.handler.codec.compression.Snappy.validateChecksum;

/**
 * 解码 Snappy 分帧格式压缩的 {@link ByteBuf}。
 * <p>
 * 格式说明见 <a href="https://github.com/google/snappy/blob/master/framing_format.txt">Snappy framing format</a>。
 * <p>
 * 默认关闭各分块校验和以提升性能；若更重视数据完整性，请使用
 * {@link #SnappyFrameDecoder(boolean)} 并将参数设为 {@code true}。
 */
public class SnappyFrameDecoder extends ByteToMessageDecoder {

    /** Snappy 分帧块类型。 */
    private enum ChunkType {
        STREAM_IDENTIFIER,
        COMPRESSED_DATA,
        UNCOMPRESSED_DATA,
        RESERVED_UNSKIPPABLE,
        RESERVED_SKIPPABLE
    }

    private static final int SNAPPY_IDENTIFIER_LEN = 6;
    // See https://github.com/google/snappy/blob/1.1.9/framing_format.txt#L95
    private static final int MAX_UNCOMPRESSED_DATA_SIZE = 65536 + 4;
    // 未压缩块：4 字节掩码校验和 + 原始数据。
    private static final int MIN_UNCOMPRESSED_DATA_SIZE = 4;
    // See https://github.com/google/snappy/blob/1.1.9/framing_format.txt#L82
    private static final int MAX_DECOMPRESSED_DATA_SIZE = 65536;
    // See https://github.com/google/snappy/blob/1.1.9/framing_format.txt#L82
    private static final int MAX_COMPRESSED_CHUNK_SIZE = 16777216 - 1;
    // 压缩块：4 字节掩码校验和 + Snappy 压缩流。
    private static final int MIN_COMPRESSED_CHUNK_SIZE = 5;

    private final Snappy snappy = new Snappy();
    private final boolean validateChecksums;

    private boolean started;
    private boolean corrupted;
    private int numBytesToSkip;

    /** 创建 Snappy 分帧解码器，默认不校验分块校验和；需校验时使用 {@link #SnappyFrameDecoder(boolean)}。 */
    public SnappyFrameDecoder() {
        this(false);
    }

    /**
     * 创建 Snappy 分帧解码器，按参数决定是否校验分块校验和。
     *
     * @param validateChecksums
     *        为 {@code true} 时校验解压数据与校验和是否一致，不一致则抛出 {@link DecompressionException}
     */
    public SnappyFrameDecoder(boolean validateChecksums) {
        this.validateChecksums = validateChecksums;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (corrupted) {
            in.skipBytes(in.readableBytes());
            return;
        }

        if (numBytesToSkip != 0) {
            // 上一块为 RESERVED_SKIPPABLE，尚有字节待跳过。
            int skipBytes = Math.min(numBytesToSkip, in.readableBytes());
            in.skipBytes(skipBytes);
            numBytesToSkip -= skipBytes;

            // 数据不足，等待更多输入。
            return;
        }

        try {
            int idx = in.readerIndex();
            final int inSize = in.readableBytes();
            if (inSize < 4) {
                // 至少需要 1 字节块类型 + 3 字节长度才能继续解析
                return;
            }

            final int chunkTypeVal = in.getUnsignedByte(idx);
            final ChunkType chunkType = mapChunkType((byte) chunkTypeVal);
            final int chunkLength = in.getUnsignedMediumLE(idx + 1);

            switch (chunkType) {
                case STREAM_IDENTIFIER:
                    if (chunkLength != SNAPPY_IDENTIFIER_LEN) {
                        throw new DecompressionException("Unexpected length of stream identifier: " + chunkLength);
                    }

                    if (inSize < 4 + SNAPPY_IDENTIFIER_LEN) {
                        break;
                    }

                    in.skipBytes(4);
                    int offset = in.readerIndex();
                    in.skipBytes(SNAPPY_IDENTIFIER_LEN);

                    checkByte(in.getByte(offset++), (byte) 's');
                    checkByte(in.getByte(offset++), (byte) 'N');
                    checkByte(in.getByte(offset++), (byte) 'a');
                    checkByte(in.getByte(offset++), (byte) 'P');
                    checkByte(in.getByte(offset++), (byte) 'p');
                    checkByte(in.getByte(offset), (byte) 'Y');

                    started = true;
                    break;
                case RESERVED_SKIPPABLE:
                    if (!started) {
                        throw new DecompressionException("Received RESERVED_SKIPPABLE tag before STREAM_IDENTIFIER");
                    }

                    in.skipBytes(4);

                    int skipBytes = Math.min(chunkLength, in.readableBytes());
                    in.skipBytes(skipBytes);
                    if (skipBytes != chunkLength) {
                        // 输入不足，记录剩余待跳过字节数
                        numBytesToSkip = chunkLength - skipBytes;
                    }
                    break;
                case RESERVED_UNSKIPPABLE:
                    // 规范要求：不可跳过的保留块必须立即报错
                    throw new DecompressionException(
                            "Found reserved unskippable chunk type: 0x" + Integer.toHexString(chunkTypeVal));
                case UNCOMPRESSED_DATA:
                    if (!started) {
                        throw new DecompressionException("Received UNCOMPRESSED_DATA tag before STREAM_IDENTIFIER");
                    }
                    if (chunkLength > MAX_UNCOMPRESSED_DATA_SIZE) {
                        throw new DecompressionException("Received UNCOMPRESSED_DATA larger than " +
                                MAX_UNCOMPRESSED_DATA_SIZE + " bytes");
                    }
                    if (chunkLength < MIN_UNCOMPRESSED_DATA_SIZE) {
                        throw new DecompressionException("Received UNCOMPRESSED_DATA with invalid chunk length: " +
                                chunkLength);
                    }

                    if (inSize < 4 + chunkLength) {
                        return;
                    }

                    in.skipBytes(4);
                    if (validateChecksums) {
                        int checksum = in.readIntLE();
                        validateChecksum(checksum, in, in.readerIndex(), chunkLength - 4);
                    } else {
                        in.skipBytes(4);
                    }
                    out.add(in.readRetainedSlice(chunkLength - 4));
                    break;
                case COMPRESSED_DATA:
                    if (!started) {
                        throw new DecompressionException("Received COMPRESSED_DATA tag before STREAM_IDENTIFIER");
                    }

                    if (chunkLength > MAX_COMPRESSED_CHUNK_SIZE) {
                        throw new DecompressionException("Received COMPRESSED_DATA that contains" +
                                " chunk that exceeds " + MAX_COMPRESSED_CHUNK_SIZE + " bytes");
                    }
                    if (chunkLength < MIN_COMPRESSED_CHUNK_SIZE) {
                        throw new DecompressionException("Received COMPRESSED_DATA with invalid chunk length: " +
                                chunkLength);
                    }

                    if (inSize < 4 + chunkLength) {
                        return;
                    }

                    in.skipBytes(4);
                    int checksum = in.readIntLE();

                    int uncompressedSize = snappy.getPreamble(in);
                    if (uncompressedSize > MAX_DECOMPRESSED_DATA_SIZE) {
                        throw new DecompressionException("Received COMPRESSED_DATA that contains" +
                                " uncompressed data that exceeds " + MAX_DECOMPRESSED_DATA_SIZE + " bytes");
                    }

                    ByteBuf uncompressed = ctx.alloc().buffer(uncompressedSize, MAX_DECOMPRESSED_DATA_SIZE);
                    try {
                        if (validateChecksums) {
                            int oldWriterIndex = in.writerIndex();
                            try {
                                in.writerIndex(in.readerIndex() + chunkLength - 4);
                                snappy.decode(in, uncompressed);
                            } finally {
                                in.writerIndex(oldWriterIndex);
                            }
                            validateChecksum(checksum, uncompressed, 0, uncompressed.writerIndex());
                        } else {
                            snappy.decode(in.readSlice(chunkLength - 4), uncompressed);
                        }
                        out.add(uncompressed);
                        uncompressed = null;
                    } finally {
                        if (uncompressed != null) {
                            uncompressed.release();
                        }
                    }
                    snappy.reset();
                    break;
            }
        } catch (Exception e) {
            corrupted = true;
            throw e;
        }
    }

    private static void checkByte(byte actual, byte expect) {
        if (actual != expect) {
            throw new DecompressionException("Unexpected stream identifier contents. Mismatched snappy " +
                    "protocol version?");
        }
    }

    /**
     * 根据类型标签字节解析分块类型。
     *
     * @param type The tag byte extracted from the stream
     * @return 对应的 {@link ChunkType}，无法识别时返回 {@link ChunkType#RESERVED_UNSKIPPABLE}
     */
    private static ChunkType mapChunkType(byte type) {
        if (type == 0) {
            return ChunkType.COMPRESSED_DATA;
        } else if (type == 1) {
            return ChunkType.UNCOMPRESSED_DATA;
        } else if (type == (byte) 0xff) {
            return ChunkType.STREAM_IDENTIFIER;
        } else if ((type & 0x80) == 0x80) {
            return ChunkType.RESERVED_SKIPPABLE;
        } else {
            return ChunkType.RESERVED_UNSKIPPABLE;
        }
    }
}
