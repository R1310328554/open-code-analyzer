/*
 * Copyright 2025 The Netty Project
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
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;
import java.util.zip.Checksum;

import static io.netty.handler.codec.compression.Lz4Constants.BLOCK_TYPE_COMPRESSED;
import static io.netty.handler.codec.compression.Lz4Constants.BLOCK_TYPE_NON_COMPRESSED;
import static io.netty.handler.codec.compression.Lz4Constants.COMPRESSION_LEVEL_BASE;
import static io.netty.handler.codec.compression.Lz4Constants.DEFAULT_SEED;
import static io.netty.handler.codec.compression.Lz4Constants.HEADER_LENGTH;
import static io.netty.handler.codec.compression.Lz4Constants.MAGIC_NUMBER;
import static io.netty.handler.codec.compression.Lz4Constants.MAX_BLOCK_SIZE;

/**
 * LZ4 帧格式的流式解压缩器（{@link InputBufferingDecompressor} 实现）。
 *
 * 帧布局与 {@link Lz4FrameDecoder} 相同：魔数 + Token + 长度 + 校验和 + LZ4 块。
 *
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *     * * * * * * * * * *
 *  * Magic * Token *  Compressed *  Decompressed *  Checksum *  +  *  LZ4 compressed *
 *  *       *       *    length   *     length    *           *     *      block      *
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *     * * * * * * * * * *
 */
@UnstableApi
public final class Lz4FrameDecompressor extends InputBufferingDecompressor {
    private static final int DEFAULT_MAX_DECOMPRESSED_LENGTH = 256 * 1024;

    /** 当前流状态。 */
    private enum State {
        INIT_BLOCK,
        DECOMPRESS_DATA,
        FINISHED,
    }

    private State currentState = State.INIT_BLOCK;

    /** 底层 LZ4 解压器。 */
    private LZ4SafeDecompressor decompressor;

    /** 块校验器（可选）。 */
    private ByteBufChecksum checksum;

    private final int maxDecompressedLength;

    /** 当前块类型。 */
    private int blockType;

    /** 当前块压缩长度。 */
    private int compressedLength;

    /** 当前块解压长度。 */
    private int decompressedLength;

    /** 当前块校验和。 */
    private int currentChecksum;

    Lz4FrameDecompressor(Builder builder, ByteBufAllocator allocator) {
        super(allocator);
        this.decompressor = builder.factory.safeDecompressor();
        this.checksum = builder.checksum == null ? null : ByteBufChecksum.wrapChecksum(builder.checksum);
        this.maxDecompressedLength = builder.maxDecompressedLength;
    }

    @Override
    void processInput(ByteBuf buf) throws DecompressionException {
        if (currentState != State.INIT_BLOCK) {
            return;
        }

        if (buf.readableBytes() < HEADER_LENGTH) {
            return;
        }
        final long magic = buf.readLong();
        if (magic != MAGIC_NUMBER) {
            throw new DecompressionException("unexpected block identifier");
        }

        final int token = buf.readByte();
        final int compressionLevel = (token & 0x0F) + COMPRESSION_LEVEL_BASE;
        int blockType = token & 0xF0;

        int compressedLength = Integer.reverseBytes(buf.readInt());
        if (compressedLength < 0 || compressedLength > MAX_BLOCK_SIZE) {
            throw new DecompressionException(String.format(
                    "invalid compressedLength: %d (expected: 0-%d)",
                    compressedLength, MAX_BLOCK_SIZE));
        }

        int decompressedLength = Integer.reverseBytes(buf.readInt());
        if (decompressedLength > maxDecompressedLength) {
            throw new DecompressionException(String.format(
                    "decompressedLength too large: %d (expected: 0-%d)",
                    decompressedLength, maxDecompressedLength));
        }

        final int maxLocalDecompressedLength = 1 << compressionLevel;
        if (decompressedLength < 0 || decompressedLength > maxLocalDecompressedLength) {
            throw new DecompressionException(String.format(
                    "invalid decompressedLength: %d (expected: 0-%d)",
                    decompressedLength, maxLocalDecompressedLength));
        }
        if (decompressedLength == 0 && compressedLength != 0
                || decompressedLength != 0 && compressedLength == 0
                || blockType == BLOCK_TYPE_NON_COMPRESSED && decompressedLength != compressedLength) {
            throw new DecompressionException(String.format(
                    "stream corrupted: compressedLength(%d) and decompressedLength(%d) mismatch",
                    compressedLength, decompressedLength));
        }

        int currentChecksum = Integer.reverseBytes(buf.readInt());
        if (decompressedLength == 0 && compressedLength == 0) {
            if (currentChecksum != 0) {
                throw new DecompressionException("stream corrupted: checksum error");
            }
            currentState = State.FINISHED;
            decompressor = null;
            checksum = null;
            return;
        }

        this.blockType = blockType;
        this.compressedLength = compressedLength;
        this.decompressedLength = decompressedLength;
        this.currentChecksum = currentChecksum;

        currentState = State.DECOMPRESS_DATA;
    }

    @Override
    public Status status() throws DecompressionException {
        switch (currentState) {
            case INIT_BLOCK:
                return Status.NEED_INPUT;
            case DECOMPRESS_DATA:
                return available() < compressedLength ? Status.NEED_INPUT : Status.NEED_OUTPUT;
            case FINISHED:
                return Status.COMPLETE;
            default:
                throw new AssertionError("Unexpected state: " + currentState);
        }
    }

    @Override
    public void endOfInput() throws DecompressionException {
        throw new DecompressionException("Unexpected end of input");
    }

    @Override
    ByteBuf processOutput(ByteBuf in) throws DecompressionException {
        ByteBuf uncompressed = null;
        try {
            switch (blockType) {
                case BLOCK_TYPE_NON_COMPRESSED:
                    // Just pass through, we not update the readerIndex yet as we do this outside of the
                    // switch statement.
                    uncompressed = in.retainedSlice(in.readerIndex(), decompressedLength);
                    break;
                case BLOCK_TYPE_COMPRESSED:
                    uncompressed = allocator.buffer(decompressedLength, decompressedLength);

                    ByteBuffer inBuffer = CompressionUtil.safeNioBuffer(
                            in, in.readerIndex(), compressedLength);
                    ByteBuffer outBuffer = uncompressed.internalNioBuffer(
                            uncompressed.writerIndex(), decompressedLength);
                    if (inBuffer.remaining() < compressedLength || outBuffer.remaining() < decompressedLength) {
                        throw new DecompressionException(String.format(
                                "buffer lengths too small: compressed(%d/%d), decompressed(%d/%d)",
                                inBuffer.remaining(), compressedLength,
                                outBuffer.remaining(), decompressedLength));
                    }
                    final int actualDecompressedLength;
                    try {
                        actualDecompressedLength = decompressor.decompress(
                                inBuffer, inBuffer.position(), compressedLength,
                                outBuffer, outBuffer.position(), decompressedLength);
                    } catch (LZ4Exception e) {
                        throw new DecompressionException(e);
                    }
                    if (actualDecompressedLength != decompressedLength) {
                        throw new DecompressionException(String.format(
                                "stream corrupted: decompressedLength(%d) and actual length(%d) mismatch",
                                decompressedLength, actualDecompressedLength));
                    }
                    // Update the writerIndex now to reflect what we decompressed.
                    uncompressed.writerIndex(uncompressed.writerIndex() + actualDecompressedLength);
                    break;
                default:
                    throw new DecompressionException(String.format(
                            "unexpected blockType: %d (expected: %d or %d)",
                            blockType, BLOCK_TYPE_NON_COMPRESSED, BLOCK_TYPE_COMPRESSED));
            }
            // Skip inbound bytes after we processed them.
            in.skipBytes(compressedLength);
            if (checksum != null) {
                CompressionUtil.checkChecksum(checksum, uncompressed, currentChecksum);
            }
            currentState = State.INIT_BLOCK;
            return uncompressed;
        } catch (Throwable t) {
            if (uncompressed != null) {
                uncompressed.release();
            }
            throw t;
        }
    }

    @UnstableApi
    public static Builder builder() {
        return new Builder();
    }

    @UnstableApi
    public static final class Builder extends AbstractDecompressorBuilder {
        private LZ4Factory factory = LZ4Factory.fastestInstance();
        private Checksum checksum;
        private int maxDecompressedLength = DEFAULT_MAX_DECOMPRESSED_LENGTH;

        Builder() {
        }

        /**
         * 自定义 {@link LZ4Factory}（可为 JNI、纯 Java 或 Unsafe 实现）。
         *
         * @param factory LZ4 工厂实例
         * @return 本构建器
         */
        public Builder factory(LZ4Factory factory) {
            this.factory = ObjectUtil.checkNotNull(factory, "factory");
            return this;
        }

        /**
         * 用于完整性校验的 {@link Checksum}，默认不校验。
         *
         * @param checksum 校验实例
         * @return 本构建器
         */
        public Builder checksum(Checksum checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * 启用默认 xxHash 校验。
         *
         * @return 本构建器
         */
        public Builder defaultChecksum() {
            return checksum(new Lz4XXHash32(DEFAULT_SEED));
        }

        /**
         * 设置单块解压后最大长度，默认 256 KiB；{@code 0} 表示 LZ4 上限 32 MiB。
         *
         * @param maxDecompressedLength 最大解压块长度
         * @return 本构建器
         */
        public Builder maxDecompressedLength(int maxDecompressedLength) {
            this.maxDecompressedLength = maxDecompressedLength == 0 ? MAX_BLOCK_SIZE :
                    ObjectUtil.checkInRange(maxDecompressedLength, 0, MAX_BLOCK_SIZE, "maxDecompressedLength");
            return this;
        }

        @Override
        public Decompressor build(ByteBufAllocator allocator) throws DecompressionException {
            return new DefensiveDecompressor(new Lz4FrameDecompressor(this, allocator));
        }
    }
}
