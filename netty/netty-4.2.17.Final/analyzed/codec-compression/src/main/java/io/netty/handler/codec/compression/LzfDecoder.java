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

import com.ning.compress.BufferRecycler;
import com.ning.compress.lzf.ChunkDecoder;
import com.ning.compress.lzf.LZFChunk;
import com.ning.compress.lzf.util.ChunkDecoderFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.ning.compress.lzf.LZFChunk.BLOCK_TYPE_COMPRESSED;
import static com.ning.compress.lzf.LZFChunk.BLOCK_TYPE_NON_COMPRESSED;
import static com.ning.compress.lzf.LZFChunk.BYTE_V;
import static com.ning.compress.lzf.LZFChunk.BYTE_Z;
import static com.ning.compress.lzf.LZFChunk.HEADER_LEN_NOT_COMPRESSED;

/**
 * 解压 LZF 格式 {@link ByteBuf} 的解码器。
 *
 * 格式说明见 <a href="http://oldhome.schmorp.de/marc/liblzf.html">liblzf</a>
 * 与 <a href="https://github.com/ning/compress/wiki/LZFFormat">LZF 规范</a>。
 */
public class LzfDecoder extends ByteToMessageDecoder {
    /** 当前解压状态机阶段。 */
    private enum State {
        INIT_BLOCK,
        INIT_ORIGINAL_LENGTH,
        DECOMPRESS_DATA,
        CORRUPTED
    }

    private State currentState = State.INIT_BLOCK;

    /** LZF 块魔数（"ZV"）。 */
    private static final short MAGIC_NUMBER = BYTE_Z << 8 | BYTE_V;

    /** 底层块解码器。 */
    private ChunkDecoder decoder;

    /** 缓冲回收器。 */
    private BufferRecycler recycler;

    /** 当前块压缩数据长度。 */
    private int chunkLength;

    /** 当前块原始长度；未压缩块时等于 {@link #chunkLength}。 */
    private int originalLength;

    /** 当前块是否为压缩块。 */
    private boolean isCompressed;

    /**
     * Creates a new LZF decoder with the most optimal available methods for underlying data access.
     * It will "unsafe" instance if one can be used on current JVM.
     * 通常可安全使用；非标准平台可改用 {@link #LzfDecoder(boolean)} 并传入 {@code true}。
     */
    public LzfDecoder() {
        this(false);
    }

    /**
     * Creates a new LZF decoder with specified decoding instance.
     *
     * @param safeInstance
     *        为 {@code true} 时使用纯 JDK 的安全解码器；
     *        否则尝试使用基于 {@link sun.misc.Unsafe} 的优化实现。
     */
    public LzfDecoder(boolean safeInstance) {
        decoder = safeInstance ?
                ChunkDecoderFactory.safeInstance()
              : ChunkDecoderFactory.optimalInstance();

        recycler = BufferRecycler.instance();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (currentState) {
            case INIT_BLOCK:
                if (in.readableBytes() < HEADER_LEN_NOT_COMPRESSED) {
                    break;
                }
                final int magic = in.readUnsignedShort();
                if (magic != MAGIC_NUMBER) {
                    throw new DecompressionException("unexpected block identifier");
                }

                final int type = in.readByte();
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
                chunkLength = in.readUnsignedShort();

                // chunkLength 理论上不会超过 MAX_CHUNK_LEN，此处便于调试
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
                if (in.readableBytes() < 2) {
                    break;
                }
                originalLength = in.readUnsignedShort();

                // originalLength can never exceed MAX_CHUNK_LEN as MAX_CHUNK_LEN is 64kb and readUnsignedShort can
                // never return anything bigger as well. Let's add some check any way to make things easier in terms
                // of debugging if we ever hit this because of an bug.
                if (originalLength > LZFChunk.MAX_CHUNK_LEN) {
                    throw new DecompressionException(String.format(
                            "original length exceeds maximum: %d (expected: =< %d)",
                            chunkLength, LZFChunk.MAX_CHUNK_LEN));
                }

                currentState = State.DECOMPRESS_DATA;
                // fall through
            case DECOMPRESS_DATA:
                final int chunkLength = this.chunkLength;
                if (in.readableBytes() < chunkLength) {
                    break;
                }
                final int originalLength = this.originalLength;

                if (isCompressed) {
                    final int idx = in.readerIndex();

                    final byte[] inputArray;
                    final int inPos;
                    if (in.hasArray()) {
                        inputArray = in.array();
                        inPos = in.arrayOffset() + idx;
                    } else {
                        inputArray = recycler.allocInputBuffer(chunkLength);
                        in.getBytes(idx, inputArray, 0, chunkLength);
                        inPos = 0;
                    }

                    ByteBuf uncompressed = ctx.alloc().heapBuffer(originalLength, originalLength);
                    final byte[] outputArray;
                    final int outPos;
                    if (uncompressed.hasArray()) {
                        outputArray = uncompressed.array();
                        outPos = uncompressed.arrayOffset() + uncompressed.writerIndex();
                    } else {
                        outputArray = new byte[originalLength];
                        outPos = 0;
                    }

                    boolean success = false;
                    try {
                        decoder.decodeChunk(
                                inputArray, inPos, inPos + chunkLength,
                                outputArray, outPos, outPos + originalLength);
                        if (uncompressed.hasArray()) {
                            uncompressed.writerIndex(uncompressed.writerIndex() + originalLength);
                        } else {
                            uncompressed.writeBytes(outputArray);
                        }
                        out.add(uncompressed);
                        in.skipBytes(chunkLength);
                        success = true;
                    } finally {
                        if (!success) {
                            uncompressed.release();
                        }
                    }

                    if (!in.hasArray()) {
                        recycler.releaseInputBuffer(inputArray);
                    }
                } else if (chunkLength > 0) {
                    out.add(in.readRetainedSlice(chunkLength));
                }

                currentState = State.INIT_BLOCK;
                break;
            case CORRUPTED:
                in.skipBytes(in.readableBytes());
                break;
            default:
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            currentState = State.CORRUPTED;
            decoder = null;
            recycler = null;
            throw e;
        }
    }
}
