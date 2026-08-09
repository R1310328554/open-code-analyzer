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
import io.netty.handler.codec.MessageToByteEncoder;

import static io.netty.handler.codec.compression.Snappy.calculateChecksum;

/**
 * 将 {@link ByteBuf} 按 Snappy 分帧格式压缩输出。
 * <p>
 * 格式见 <a href="https://github.com/google/snappy/blob/master/framing_format.txt">Snappy framing format</a>。
 */
public class SnappyFrameEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final short SNAPPY_SLICE_SIZE = Short.MAX_VALUE;

    /** Snappy 分帧格式允许的两种分片大小上限。 */
    private static final int SNAPPY_SLICE_JUMBO_SIZE = 65535;

    /** 尝试 Snappy 压缩的最小输入长度（低于此值直接以未压缩块输出）。 */
    private static final int MIN_COMPRESSIBLE_LENGTH = 18;

    /** 流起始标识块：类型 0xff、长度 6、ASCII 字符串 sNaPpY。 */
    private static final byte[] STREAM_START = {
        (byte) 0xff, 0x06, 0x00, 0x00, 0x73, 0x4e, 0x61, 0x50, 0x70, 0x59
    };

    public SnappyFrameEncoder() {
        this(SNAPPY_SLICE_SIZE);
    }

    /** 创建分片大小为 {@value io.netty.handler.codec.compression.SnappyFrameEncoder#SNAPPY_SLICE_JUMBO_SIZE} 的编码器。 */
    public static SnappyFrameEncoder snappyEncoderWithJumboFrames() {
        return new SnappyFrameEncoder(SNAPPY_SLICE_JUMBO_SIZE);
    }

    private SnappyFrameEncoder(int sliceSize) {
        super(ByteBuf.class);
        this.sliceSize = sliceSize;
    }

    private final Snappy snappy = new Snappy();
    private boolean started;
    private final int sliceSize;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        if (!in.isReadable()) {
            return;
        }

        if (!started) {
            started = true;
            out.writeBytes(STREAM_START);
        }

        int dataLength = in.readableBytes();
        if (dataLength > MIN_COMPRESSIBLE_LENGTH) {
            for (;;) {
                final int lengthIdx = out.writerIndex() + 1;
                if (dataLength < MIN_COMPRESSIBLE_LENGTH) {
                    ByteBuf slice = in.readSlice(dataLength);
                    writeUnencodedChunk(slice, out, dataLength);
                    break;
                }

                out.writeInt(0);
                if (dataLength > sliceSize) {
                    ByteBuf slice = in.readSlice(sliceSize);
                    calculateAndWriteChecksum(slice, out);
                    snappy.encode(slice, out, sliceSize);
                    setChunkLength(out, lengthIdx);
                    dataLength -= sliceSize;
                } else {
                    ByteBuf slice = in.readSlice(dataLength);
                    calculateAndWriteChecksum(slice, out);
                    snappy.encode(slice, out, dataLength);
                    setChunkLength(out, lengthIdx);
                    break;
                }
            }
        } else {
            writeUnencodedChunk(in, out, dataLength);
        }
    }

    private static void writeUnencodedChunk(ByteBuf in, ByteBuf out, int dataLength) {
        out.writeByte(1);
        writeChunkLength(out, dataLength + 4);
        calculateAndWriteChecksum(in, out);
        out.writeBytes(in, dataLength);
    }

    private static void setChunkLength(ByteBuf out, int lengthIdx) {
        int chunkLength = out.writerIndex() - lengthIdx - 3;
        if (chunkLength >>> 24 != 0) {
            throw new CompressionException("compressed data too large: " + chunkLength);
        }
        out.setMediumLE(lengthIdx, chunkLength);
    }

    /**
     * 向输出缓冲写入 3 字节小端块长度。
     *
     * @param out The buffer to write to
     * @param chunkLength The length to write
     */
    private static void writeChunkLength(ByteBuf out, int chunkLength) {
        out.writeMediumLE(chunkLength);
    }

    /**
     * 计算数据掩码校验和并写入 4 字节小端值。
     *
     * @param slice The data to calculate the checksum for
     * @param out The output buffer to write the checksum to
     */
    private static void calculateAndWriteChecksum(ByteBuf slice, ByteBuf out) {
        out.writeIntLE(calculateChecksum(slice));
    }
}
