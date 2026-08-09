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
import io.netty.util.internal.ObjectUtil;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.Checksum;

import static io.netty.handler.codec.compression.Lz4Constants.BLOCK_TYPE_COMPRESSED;
import static io.netty.handler.codec.compression.Lz4Constants.BLOCK_TYPE_NON_COMPRESSED;
import static io.netty.handler.codec.compression.Lz4Constants.COMPRESSION_LEVEL_BASE;
import static io.netty.handler.codec.compression.Lz4Constants.DEFAULT_SEED;
import static io.netty.handler.codec.compression.Lz4Constants.HEADER_LENGTH;
import static io.netty.handler.codec.compression.Lz4Constants.MAGIC_NUMBER;
import static io.netty.handler.codec.compression.Lz4Constants.MAX_BLOCK_SIZE;

/**
 * 解压 LZ4 帧格式的 {@link ByteBuf}。
 *
 * 格式说明见 <a href="https://github.com/Cyan4973/lz4">LZ4 项目</a>
 * 与 <a href="https://fastcompression.blogspot.ru/2011/05/lz4-explained.html">LZ4 块格式</a>。
 *
 * 原生 LZ4 块不含压缩/原始长度，本实现采用
 * <a href="https://github.com/idelpivnitskiy/lz4-java">LZ4 Java</a> 的扩展帧格式。
 *
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *     * * * * * * * * * *
 *  * Magic * Token *  Compressed *  Decompressed *  Checksum *  +  *  LZ4 compressed *
 *  *       *       *    length   *     length    *           *     *      block      *
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *     * * * * * * * * * *
 */
public class Lz4FrameDecoder extends ByteToMessageDecoder {
    private final int maxDecompressedLength;
    /** 当前流解析状态。 */
    private enum State {
        INIT_BLOCK,
        DECOMPRESS_DATA,
        FINISHED,
        CORRUPTED
    }

    private State currentState = State.INIT_BLOCK;

    /** 底层 LZ4 安全解压器。 */
    private LZ4SafeDecompressor decompressor;

    /** 底层块校验计算器（可为 null）。 */
    private ByteBufChecksum checksum;

    /** 当前块的类型标志。 */
    private int blockType;

    /** 当前块压缩数据长度。 */
    private int compressedLength;

    /** 当前块解压后长度。 */
    private int decompressedLength;

    /** 当前块头部记录的校验和。 */
    private int currentChecksum;

    /**
     * Creates the fastest LZ4 decoder.
     *
     * 默认关闭块头校验和以提升性能；若更重视完整性，
     * 请使用 {@link #Lz4FrameDecoder(boolean)} 并传入 {@code true}。
     */
    public Lz4FrameDecoder() {
        this(false);
    }

    /**
     * Creates a LZ4 decoder with fastest decoder instance available on your machine.
     *
     * @param validateChecksums  为 {@code true} 时校验块头 checksum 与解压数据是否一致，
     *                           不一致则抛出 {@link DecompressionException}
     */
    public Lz4FrameDecoder(boolean validateChecksums) {
        this(LZ4Factory.fastestInstance(), validateChecksums);
    }

    /**
     * Creates a LZ4 decoder with fastest decoder instance available on your machine.
     *
     * @param validateChecksums  if {@code true}, the checksum field will be validated against the actual
     *                           uncompressed data, and if the checksums do not match, a suitable
     *                           {@link DecompressionException} will be thrown
     * @param maxDecompressedLength 单块解压后最大长度；{@code 0} 表示默认 32MB。
     */
    public Lz4FrameDecoder(boolean validateChecksums, int maxDecompressedLength) {
        this(LZ4Factory.fastestInstance(), validateChecksums ? new Lz4XXHash32(DEFAULT_SEED) : null,
                maxDecompressedLength);
    }

    /**
     * Creates a new LZ4 decoder with customizable implementation.
     *
     * @param factory            user customizable {@link LZ4Factory} instance
     *                           which may be JNI bindings to the original C implementation, a pure Java implementation
     *                           or a Java implementation that uses the {@link sun.misc.Unsafe}
     * @param validateChecksums  if {@code true}, the checksum field will be validated against the actual
     *                           uncompressed data, and if the checksums do not match, a suitable
     *                           {@link DecompressionException} will be thrown. In this case encoder will use
     *                           xxhash hashing for Java, based on Yann Collet's work available at
     *                           <a href="https://github.com/Cyan4973/xxHash">Github</a>.
     */
    public Lz4FrameDecoder(LZ4Factory factory, boolean validateChecksums) {
        this(factory, validateChecksums ? new Lz4XXHash32(DEFAULT_SEED) : null);
    }

    /**
     * Creates a new customizable LZ4 decoder.
     *
     * @param factory   user customizable {@link LZ4Factory} instance
     *                  which may be JNI bindings to the original C implementation, a pure Java implementation
     *                  or a Java implementation that uses the {@link sun.misc.Unsafe}
     * @param checksum  the {@link Checksum} instance to use to check data for integrity.
     *                  You may set {@code null} if you do not want to validate checksum of each block
     */
    public Lz4FrameDecoder(LZ4Factory factory, Checksum checksum) {
        this(factory, checksum, MAX_BLOCK_SIZE);
    }

    /**
     * Creates a new customizable LZ4 decoder.
     *
     * @param factory   user customizable {@link LZ4Factory} instance
     *                  which may be JNI bindings to the original C implementation, a pure Java implementation
     *                  or a Java implementation that uses the {@link sun.misc.Unsafe}
     * @param checksum  the {@link Checksum} instance to use to check data for integrity.
     *                  You may set {@code null} if you do not want to validate checksum of each block
     * @param maxDecompressedLength
     *                  maximum length of the decompressed block. If {@code 0} is given it uses {@code 32MB} by default.
     */
    public Lz4FrameDecoder(LZ4Factory factory, Checksum checksum, int maxDecompressedLength) {
        decompressor = ObjectUtil.checkNotNull(factory, "factory").safeDecompressor();
        this.checksum = checksum == null ? null : ByteBufChecksum.wrapChecksum(checksum);
        this.maxDecompressedLength = maxDecompressedLength == 0 ? MAX_BLOCK_SIZE :
                ObjectUtil.checkInRange(maxDecompressedLength, 0, MAX_BLOCK_SIZE, "maxDecompressedLength");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (currentState) {
            case INIT_BLOCK:
                if (in.readableBytes() < HEADER_LENGTH) {
                    break;
                }
                final long magic = in.readLong();
                if (magic != MAGIC_NUMBER) {
                    throw new DecompressionException("unexpected block identifier");
                }

                final int token = in.readByte();
                final int compressionLevel = (token & 0x0F) + COMPRESSION_LEVEL_BASE;
                int blockType = token & 0xF0;

                int compressedLength = Integer.reverseBytes(in.readInt());
                if (compressedLength < 0 || compressedLength > MAX_BLOCK_SIZE) {
                    throw new DecompressionException(String.format(
                            "invalid compressedLength: %d (expected: 0-%d)",
                            compressedLength, MAX_BLOCK_SIZE));
                }

                int decompressedLength = Integer.reverseBytes(in.readInt());
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

                int currentChecksum = Integer.reverseBytes(in.readInt());
                if (decompressedLength == 0 && compressedLength == 0) {
                    if (currentChecksum != 0) {
                        throw new DecompressionException("stream corrupted: checksum error");
                    }
                    currentState = State.FINISHED;
                    decompressor = null;
                    checksum = null;
                    break;
                }

                this.blockType = blockType;
                this.compressedLength = compressedLength;
                this.decompressedLength = decompressedLength;
                this.currentChecksum = currentChecksum;

                currentState = State.DECOMPRESS_DATA;
                // fall through
            case DECOMPRESS_DATA:
                blockType = this.blockType;
                compressedLength = this.compressedLength;
                decompressedLength = this.decompressedLength;
                currentChecksum = this.currentChecksum;

                if (in.readableBytes() < compressedLength) {
                    break;
                }

                final ByteBufChecksum checksum = this.checksum;
                ByteBuf uncompressed = null;

                try {
                    switch (blockType) {
                        case BLOCK_TYPE_NON_COMPRESSED:
                            // 未压缩块直接透传，readerIndex 在 switch 外统一推进
                            uncompressed = in.retainedSlice(in.readerIndex(), decompressedLength);
                            break;
                        case BLOCK_TYPE_COMPRESSED:
                            uncompressed = ctx.alloc().buffer(decompressedLength, decompressedLength);

                            ByteBuffer source = CompressionUtil.safeNioBuffer(
                                    in, in.readerIndex(), compressedLength);
                            ByteBuffer destination = uncompressed.internalNioBuffer(
                                    uncompressed.writerIndex(), decompressedLength);
                            int actualDecompressedLength = decompressor.decompress(
                                    source, source.position(), compressedLength,
                                    destination, destination.position(), decompressedLength);
                            if (actualDecompressedLength != decompressedLength) {
                                throw new DecompressionException(String.format(
                                        "stream corrupted: decompressedLength(%d) and " +
                                                "actualDecompressedLength(%d) mismatch",
                                        decompressedLength, actualDecompressedLength));
                            }
                            // 更新 writerIndex 反映已解压字节数
                            uncompressed.writerIndex(uncompressed.writerIndex() + decompressedLength);
                            break;
                        default:
                            throw new DecompressionException(String.format(
                                    "unexpected blockType: %d (expected: %d or %d)",
                                    blockType, BLOCK_TYPE_NON_COMPRESSED, BLOCK_TYPE_COMPRESSED));
                    }
                    // 处理完毕后跳过已消费输入
                    in.skipBytes(compressedLength);

                    if (checksum != null) {
                        CompressionUtil.checkChecksum(checksum, uncompressed, currentChecksum);
                    }
                    out.add(uncompressed);
                    uncompressed = null;
                    currentState = State.INIT_BLOCK;
                } catch (LZ4Exception e) {
                    throw new DecompressionException(e);
                } finally {
                    if (uncompressed != null) {
                        uncompressed.release();
                    }
                }
                break;
            case FINISHED:
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

    /**
     * 当且仅当已到达压缩流末尾时返回 {@code true}。
     */
    public boolean isClosed() {
        return currentState == State.FINISHED;
    }
}
