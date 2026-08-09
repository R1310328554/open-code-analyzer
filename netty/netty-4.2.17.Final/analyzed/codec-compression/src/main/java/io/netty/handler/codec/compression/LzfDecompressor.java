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

import com.ning.compress.lzf.ChunkDecoder;
import com.ning.compress.lzf.LZFChunk;
import com.ning.compress.lzf.LZFException;
import com.ning.compress.lzf.util.ChunkDecoderFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.internal.UnstableApi;

import static com.ning.compress.lzf.LZFChunk.BLOCK_TYPE_COMPRESSED;
import static com.ning.compress.lzf.LZFChunk.BLOCK_TYPE_NON_COMPRESSED;
import static com.ning.compress.lzf.LZFChunk.BYTE_V;
import static com.ning.compress.lzf.LZFChunk.BYTE_Z;
import static com.ning.compress.lzf.LZFChunk.HEADER_LEN_NOT_COMPRESSED;

/**
 * LZF 格式的流式解压缩器。
 * <p>
 * 块格式见 liblzf 与 ning/compress 文档。
 */
@UnstableApi
public final class LzfDecompressor extends InputBufferingDecompressor {
    /** 解压状态。 */
    private enum State {
        INIT_BLOCK,
        INIT_ORIGINAL_LENGTH,
        DECOMPRESS_DATA,
        END,
    }

    private State currentState = State.INIT_BLOCK;

    /** LZF 块魔数。 */
    private static final short MAGIC_NUMBER = BYTE_Z << 8 | BYTE_V;

    private final ChunkDecoder decoder;

    /** 当前块压缩长度。 */
    private int chunkLength;

    /** 当前块原始长度。 */
    private int originalLength;

    /** 当前块是否已压缩。 */
    private boolean isCompressed;

    LzfDecompressor(Builder builder, ByteBufAllocator allocator) {
        super(allocator);
        decoder = builder.safeInstance ?
                ChunkDecoderFactory.safeInstance()
                : ChunkDecoderFactory.optimalInstance();
    }

    @Override
    void processInput(ByteBuf buf) throws DecompressionException {
        switch (currentState) {
            case INIT_BLOCK:
                if (buf.readableBytes() < HEADER_LEN_NOT_COMPRESSED) {
                    break;
                }
                final int magic = buf.readUnsignedShort();
                if (magic != MAGIC_NUMBER) {
                    throw new DecompressionException("unexpected block identifier");
                }

                final int type = buf.readByte();
                switch (type) {
                    case BLOCK_TYPE_NON_COMPRESSED:
                        isCompressed = false;
                        currentState = State.DECOMPRESS_DATA;
                        break;
                    case BLOCK_TYPE_COMPRESSED:
                        isCompressed = true;
                        currentState = State.INIT_ORIGINAL_LENGTH;
                        break;
                    default:
                        throw new DecompressionException(String.format(
                                "unknown type of chunk: %d (expected: %d or %d)",
                                type, BLOCK_TYPE_NON_COMPRESSED, BLOCK_TYPE_COMPRESSED));
                }
                chunkLength = buf.readUnsignedShort();

                // chunkLength can never exceed MAX_CHUNK_LEN as MAX_CHUNK_LEN is 64kb and readUnsignedShort can
                // never return anything bigger as well. Let's add a check anyway to make things easier in terms
                // of debugging if we ever hit this because of a bug.
                if (chunkLength > LZFChunk.MAX_CHUNK_LEN) {
                    throw new DecompressionException(String.format(
                            "chunk length exceeds maximum: %d (expected: =< %d)",
                            chunkLength, LZFChunk.MAX_CHUNK_LEN));
                }

                if (type != BLOCK_TYPE_COMPRESSED) {
                    break;
                }
                // fall through
            case INIT_ORIGINAL_LENGTH:
                if (buf.readableBytes() < 2) {
                    break;
                }
                originalLength = buf.readUnsignedShort();

                // originalLength can never exceed MAX_CHUNK_LEN as MAX_CHUNK_LEN is 64kb and readUnsignedShort can
                // never return anything bigger as well. Let's add a check anyway to make things easier in terms
                // of debugging if we ever hit this because of a bug.
                if (originalLength > LZFChunk.MAX_CHUNK_LEN) {
                    throw new DecompressionException(String.format(
                            "original length exceeds maximum: %d (expected: =< %d)",
                            originalLength, LZFChunk.MAX_CHUNK_LEN));
                }

                currentState = State.DECOMPRESS_DATA;
                // fall through
            case DECOMPRESS_DATA:

                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Status status() throws DecompressionException {
        switch (currentState) {
            case INIT_BLOCK:
            case INIT_ORIGINAL_LENGTH:
                return Status.NEED_INPUT;
            case DECOMPRESS_DATA:
                return available() < chunkLength ? Status.NEED_INPUT : Status.NEED_OUTPUT;
            case END:
                return Status.COMPLETE;
            default:
                throw new AssertionError("Unknown state: " + currentState);
        }
    }

    @Override
    public void endOfInput() throws DecompressionException {
        if (currentState != State.INIT_BLOCK || available() != 0) {
            throw new DecompressionException("Incomplete block");
        }
        currentState = State.END;
    }

    @Override
    ByteBuf processOutput(ByteBuf in) throws DecompressionException {
        final int chunkLength = this.chunkLength;
        if (in.readableBytes() < chunkLength) {
            throw new IllegalStateException("Not in state NEED_OUTPUT");
        }
        final int originalLength = this.originalLength;

        if (isCompressed) {
            ByteBuf arrayView;
            if (!in.hasArray()) {
                arrayView = allocator.heapBuffer(chunkLength, chunkLength);
                arrayView.writeBytes(in, in.readerIndex(), chunkLength);
            } else {
                arrayView = in;
            }
            final byte[] inputArray = arrayView.array();
            final int inPos = arrayView.arrayOffset() + arrayView.readerIndex();

            ByteBuf uncompressed = null;
            try {
                uncompressed = allocator.heapBuffer(originalLength, originalLength);
                final byte[] outputArray = uncompressed.array();
                final int outPos = uncompressed.arrayOffset() + uncompressed.writerIndex();
                decoder.decodeChunk(
                        inputArray, inPos, inPos + chunkLength,
                        outputArray, outPos, outPos + originalLength);
                uncompressed.writerIndex(uncompressed.writerIndex() + originalLength);
                in.skipBytes(chunkLength);
                currentState = State.INIT_BLOCK;
                ByteBuf output = uncompressed;
                uncompressed = null;
                return output;
            } catch (LZFException e) {
                throw new DecompressionException(e);
            } finally {
                if (uncompressed != null) {
                    uncompressed.release();
                }
                if (arrayView != in) {
                    arrayView.release();
                }
            }
        } else {
            currentState = State.INIT_BLOCK;
            return in.readRetainedSlice(chunkLength);
        }
    }

    @UnstableApi
    public static Builder builder() {
        return new Builder();
    }

    @UnstableApi
    public static final class Builder extends AbstractDecompressorBuilder {
        private boolean safeInstance;

        Builder() {
        }

        /**
         * 为 {@code true} 时仅使用标准 JDK 安全解码器；否则尝试 Unsafe 优化实现。
         *
         * @param safeInstance 是否强制安全实例
         * @return 本构建器
         */
        @UnstableApi
        public Builder safeInstance(boolean safeInstance) {
            this.safeInstance = safeInstance;
            return this;
        }

        @Override
        @UnstableApi
        public Decompressor build(ByteBufAllocator allocator) throws DecompressionException {
            return new DefensiveDecompressor(new LzfDecompressor(this, allocator));
        }
    }
}
