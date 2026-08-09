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
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.zip.Adler32;
import java.util.zip.Checksum;

import static io.netty.handler.codec.compression.FastLz.BLOCK_TYPE_COMPRESSED;
import static io.netty.handler.codec.compression.FastLz.BLOCK_TYPE_NON_COMPRESSED;
import static io.netty.handler.codec.compression.FastLz.BLOCK_WITHOUT_CHECKSUM;
import static io.netty.handler.codec.compression.FastLz.BLOCK_WITH_CHECKSUM;
import static io.netty.handler.codec.compression.FastLz.CHECKSUM_OFFSET;
import static io.netty.handler.codec.compression.FastLz.LEVEL_1;
import static io.netty.handler.codec.compression.FastLz.LEVEL_2;
import static io.netty.handler.codec.compression.FastLz.LEVEL_AUTO;
import static io.netty.handler.codec.compression.FastLz.MAGIC_NUMBER;
import static io.netty.handler.codec.compression.FastLz.MAX_CHUNK_LENGTH;
import static io.netty.handler.codec.compression.FastLz.MIN_LENGTH_TO_COMPRESSION;
import static io.netty.handler.codec.compression.FastLz.OPTIONS_OFFSET;
import static io.netty.handler.codec.compression.FastLz.calculateOutputBufferLength;
import static io.netty.handler.codec.compression.FastLz.compress;

/**
 * 使用 FastLZ 算法压缩 {@link ByteBuf} 并按 Netty 帧格式输出。
 * 格式见 <a href="https://github.com/netty/netty/issues/2750">FastLZ format</a>。
 */
public class FastLzFrameEncoder extends MessageToByteEncoder<ByteBuf> {
    /** 压缩级别（{@link FastLz#LEVEL_AUTO} / {@link FastLz#LEVEL_1} / {@link FastLz#LEVEL_2}）。 */
    private final int level;

    /** 可选的块校验和计算器。 */
    private final ByteBufChecksum checksum;

    /** 创建无校验和、自动选择压缩级别的 FastLZ 编码器。 */
    public FastLzFrameEncoder() {
        this(LEVEL_AUTO, null);
    }

    /**
     * 指定压缩级别、不计算校验和。
     *
     * @param level supports only these values:
     *        0 - 按输入长度自动选级；1 - 最快；2 - 更高压缩率。
     */
    public FastLzFrameEncoder(int level) {
        this(level, null);
    }

    /**
     * 自动选级并按需为每块写入 Adler32 校验和。
     *
     * @param validateChecksums
     *        为 {@code true} 时在块头写入校验和，默认 {@link java.util.zip.Adler32}。
     */
    public FastLzFrameEncoder(boolean validateChecksums) {
        this(LEVEL_AUTO, validateChecksums ? new Adler32() : null);
    }

    /**
     * 指定压缩级别与块校验计算器。
     *
     * @param level supports only these values:
     *        0 - 自动；1 - 最快；2 - 更高压缩率。
     * @param checksum
     *        块校验器；{@code null} 表示不写校验和。
     */
    public FastLzFrameEncoder(int level, Checksum checksum) {
        super(ByteBuf.class);
        if (level != LEVEL_AUTO && level != LEVEL_1 && level != LEVEL_2) {
            throw new IllegalArgumentException(String.format(
                    "level: %d (expected: %d or %d or %d)", level, LEVEL_AUTO, LEVEL_1, LEVEL_2));
        }
        this.level = level;
        this.checksum = checksum == null ? null : ByteBufChecksum.wrapChecksum(checksum);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        final ByteBufChecksum checksum = this.checksum;

        for (;;) {
            if (!in.isReadable()) {
                return;
            }
            final int idx = in.readerIndex();
            final int length = Math.min(in.readableBytes(), MAX_CHUNK_LENGTH);

            final int outputIdx = out.writerIndex();
            out.setMedium(outputIdx, MAGIC_NUMBER);
            int outputOffset = outputIdx + CHECKSUM_OFFSET + (checksum != null ? 4 : 0);

            final byte blockType;
            final int chunkLength;
            if (length < MIN_LENGTH_TO_COMPRESSION) {
                blockType = BLOCK_TYPE_NON_COMPRESSED;

                out.ensureWritable(outputOffset + 2 + length);
                final int outputPtr = outputOffset + 2;

                if (checksum != null) {
                    checksum.reset();
                    checksum.update(in, idx, length);
                    out.setInt(outputIdx + CHECKSUM_OFFSET, (int) checksum.getValue());
                }
                out.setBytes(outputPtr, in, idx, length);
                chunkLength = length;
            } else {
                // 尝试 FastLZ 压缩，失败则原样存储
                if (checksum != null) {
                    checksum.reset();
                    checksum.update(in, idx, length);
                    out.setInt(outputIdx + CHECKSUM_OFFSET, (int) checksum.getValue());
                }

                final int maxOutputLength = calculateOutputBufferLength(length);
                out.ensureWritable(outputOffset + 4 + maxOutputLength);
                final int outputPtr = outputOffset + 4;
                final int compressedLength = compress(in, in.readerIndex(), length, out, outputPtr, level);

                if (compressedLength < length) {
                    blockType = BLOCK_TYPE_COMPRESSED;
                    chunkLength = compressedLength;

                    out.setShort(outputOffset, chunkLength);
                    outputOffset += 2;
                } else {
                    blockType = BLOCK_TYPE_NON_COMPRESSED;
                    out.setBytes(outputOffset + 2, in, idx, length);
                    chunkLength = length;
                }
            }
            out.setShort(outputOffset, length);

            out.setByte(outputIdx + OPTIONS_OFFSET,
                    blockType | (checksum != null ? BLOCK_WITH_CHECKSUM : BLOCK_WITHOUT_CHECKSUM));
            out.writerIndex(outputOffset + 2 + chunkLength);
            in.skipBytes(length);
        }
    }
}
