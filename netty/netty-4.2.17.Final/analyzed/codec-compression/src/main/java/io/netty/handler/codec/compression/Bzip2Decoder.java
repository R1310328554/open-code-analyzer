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

import static io.netty.handler.codec.compression.Bzip2Constants.BASE_BLOCK_SIZE;
import static io.netty.handler.codec.compression.Bzip2Constants.BLOCK_HEADER_MAGIC_1;
import static io.netty.handler.codec.compression.Bzip2Constants.BLOCK_HEADER_MAGIC_2;
import static io.netty.handler.codec.compression.Bzip2Constants.END_OF_STREAM_MAGIC_1;
import static io.netty.handler.codec.compression.Bzip2Constants.END_OF_STREAM_MAGIC_2;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_MAXIMUM_TABLES;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_MAX_ALPHABET_SIZE;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_MINIMUM_TABLES;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_SELECTOR_LIST_MAX_LENGTH;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_SYMBOL_RANGE_SIZE;
import static io.netty.handler.codec.compression.Bzip2Constants.MAGIC_NUMBER;
import static io.netty.handler.codec.compression.Bzip2Constants.MAX_BLOCK_SIZE;
import static io.netty.handler.codec.compression.Bzip2Constants.MAX_SELECTORS;
import static io.netty.handler.codec.compression.Bzip2Constants.MIN_BLOCK_SIZE;

/**
 * 将 Bzip2 格式压缩的 {@link ByteBuf} 解压为明文。
 * 基于状态机逐块解析流头、Huffman 表与块数据。
 *
 * 参见 <a href="https://en.wikipedia.org/wiki/Bzip2">Bzip2</a>。
 */
public class Bzip2Decoder extends ByteToMessageDecoder {
    /** 当前流解析状态。 */
    private enum State {
        INIT,
        INIT_BLOCK,
        INIT_BLOCK_PARAMS,
        RECEIVE_HUFFMAN_USED_MAP,
        RECEIVE_HUFFMAN_USED_BITMAPS,
        RECEIVE_SELECTORS_NUMBER,
        RECEIVE_SELECTORS,
        RECEIVE_HUFFMAN_LENGTH,
        DECODE_HUFFMAN_DATA,
        EOF
    }
    private State currentState = State.INIT;

    /** 位级读取器，解析块内按位编码的数据。 */
    private final Bzip2BitReader reader = new Bzip2BitReader();

    /** 当前块的块级解压器。 */
    private Bzip2BlockDecompressor blockDecompressor;

    /** Bzip2 Huffman 解码阶段处理器。 */
    private Bzip2HuffmanStageDecoder huffmanStageDecoder;

    /**
     * 取值 0..9；当前块最大字节数为 100000 × 该值。
     */
    private int blockSize;

    /**
     * The CRC of the current block as read from the block header.
     */
    private int blockCRC;

    /** 至今所有已解压块的合并 CRC。 */
    private int streamCRC;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }

        final Bzip2BitReader reader = this.reader;
        reader.setByteBuf(in);

        for (;;) {
            switch (currentState) {
            case INIT:
                if (in.readableBytes() < 4) {
                    return;
                }
                int magicNumber = in.readUnsignedMedium();
                if (magicNumber != MAGIC_NUMBER) {
                    throw new DecompressionException("Unexpected stream identifier contents. Mismatched bzip2 " +
                            "protocol version?");
                }
                int blockSize = in.readByte() - '0';
                if (blockSize < MIN_BLOCK_SIZE || blockSize > MAX_BLOCK_SIZE) {
                    throw new DecompressionException("block size is invalid");
                }
                this.blockSize = blockSize * BASE_BLOCK_SIZE;

                streamCRC = 0;
                currentState = State.INIT_BLOCK;
                 // 故意 fall-through 进入下一状态
            case INIT_BLOCK:
                if (!reader.hasReadableBytes(10)) {
                    return;
                }
                // 读取块魔数或流结束标记
                final int magic1 = reader.readBits(24);
                final int magic2 = reader.readBits(24);
                if (magic1 == END_OF_STREAM_MAGIC_1 && magic2 == END_OF_STREAM_MAGIC_2) {
                    // 到达流尾，校验合并 CRC
                    final int storedCombinedCRC = reader.readInt();
                    if (storedCombinedCRC != streamCRC) {
                        throw new DecompressionException("stream CRC error");
                    }
                    currentState = State.EOF;
                    break;
                }
                if (magic1 != BLOCK_HEADER_MAGIC_1 || magic2 != BLOCK_HEADER_MAGIC_2) {
                    throw new DecompressionException("bad block header");
                }
                blockCRC = reader.readInt();
                currentState = State.INIT_BLOCK_PARAMS;
                // fall through
            case INIT_BLOCK_PARAMS:
                if (!reader.hasReadableBits(25)) {
                    return;
                }
                final boolean blockRandomised = reader.readBoolean();
                final int bwtStartPointer = reader.readBits(24);

                blockDecompressor = new Bzip2BlockDecompressor(this.blockSize, blockCRC,
                                                                blockRandomised, bwtStartPointer, reader);
                currentState = State.RECEIVE_HUFFMAN_USED_MAP;
                // fall through
            case RECEIVE_HUFFMAN_USED_MAP:
                if (!reader.hasReadableBits(16)) {
                    return;
                }
                blockDecompressor.huffmanInUse16 = reader.readBits(16);
                currentState = State.RECEIVE_HUFFMAN_USED_BITMAPS;
                // fall through
            case RECEIVE_HUFFMAN_USED_BITMAPS:
                Bzip2BlockDecompressor blockDecompressor = this.blockDecompressor;
                final int inUse16 = blockDecompressor.huffmanInUse16;
                final int bitNumber = Integer.bitCount(inUse16);
                final byte[] huffmanSymbolMap = blockDecompressor.huffmanSymbolMap;

                if (!reader.hasReadableBits(bitNumber * HUFFMAN_SYMBOL_RANGE_SIZE + 3)) {
                    return;
                }

                int huffmanSymbolCount = 0;
                if (bitNumber > 0) {
                    for (int i = 0; i < 16; i++) {
                        if ((inUse16 & 1 << 15 >>> i) != 0) {
                            for (int j = 0, k = i << 4; j < HUFFMAN_SYMBOL_RANGE_SIZE; j++, k++) {
                                if (reader.readBoolean()) {
                                    huffmanSymbolMap[huffmanSymbolCount++] = (byte) k;
                                }
                            }
                        }
                    }
                }
                blockDecompressor.huffmanEndOfBlockSymbol = huffmanSymbolCount + 1;

                int totalTables = reader.readBits(3);
                if (totalTables < HUFFMAN_MINIMUM_TABLES || totalTables > HUFFMAN_MAXIMUM_TABLES) {
                    throw new DecompressionException("incorrect huffman groups number");
                }
                int alphaSize = huffmanSymbolCount + 2;
                if (alphaSize > HUFFMAN_MAX_ALPHABET_SIZE) {
                    throw new DecompressionException("incorrect alphabet size");
                }
                huffmanStageDecoder = new Bzip2HuffmanStageDecoder(reader, totalTables, alphaSize);
                currentState = State.RECEIVE_SELECTORS_NUMBER;
                // fall through
            case RECEIVE_SELECTORS_NUMBER:
                if (!reader.hasReadableBits(15)) {
                    return;
                }
                int totalSelectors = reader.readBits(15);
                if (totalSelectors < 1 || totalSelectors > MAX_SELECTORS) {
                    throw new DecompressionException("incorrect selectors number");
                }
                huffmanStageDecoder.selectors = new byte[totalSelectors];

                currentState = State.RECEIVE_SELECTORS;
                // fall through
            case RECEIVE_SELECTORS:
                Bzip2HuffmanStageDecoder huffmanStageDecoder = this.huffmanStageDecoder;
                byte[] selectors = huffmanStageDecoder.selectors;
                totalSelectors = selectors.length;
                final Bzip2MoveToFrontTable tableMtf = huffmanStageDecoder.tableMTF;

                int currSelector;
                // 读取 MTF 编码的 Huffman 表选择器（零终止位串，长度 1..6）
                for (currSelector = huffmanStageDecoder.currentSelector;
                            currSelector < totalSelectors; currSelector++) {
                    if (!reader.hasReadableBits(HUFFMAN_SELECTOR_LIST_MAX_LENGTH)) {
                        // 当前 ByteBuf 读尽时保存选择器解析进度
                        huffmanStageDecoder.currentSelector = currSelector;
                        return;
                    }
                    int index = 0;
                    while (reader.readBoolean()) {
                        index++;
                    }
                    selectors[currSelector] = tableMtf.indexToFront(index);
                }

                currentState = State.RECEIVE_HUFFMAN_LENGTH;
                // fall through
            case RECEIVE_HUFFMAN_LENGTH:
                huffmanStageDecoder = this.huffmanStageDecoder;
                totalTables = huffmanStageDecoder.totalTables;
                final byte[][] codeLength = huffmanStageDecoder.tableCodeLengths;
                alphaSize = huffmanStageDecoder.alphabetSize;

                /* 读取各 Huffman 表的码长 */
                int currGroup;
                int currLength = huffmanStageDecoder.currentLength;
                int currAlpha = 0;
                boolean modifyLength = huffmanStageDecoder.modifyLength;
                boolean saveStateAndReturn = false;
                loop: for (currGroup = huffmanStageDecoder.currentGroup; currGroup < totalTables; currGroup++) {
                    // 读取本表第一个符号的初始码长（5 位）
                    if (!reader.hasReadableBits(5)) {
                        saveStateAndReturn = true;
                        break;
                    }
                    if (currLength < 0) {
                        currLength = reader.readBits(5);
                    }
                    for (currAlpha = huffmanStageDecoder.currentAlpha; currAlpha < alphaSize; currAlpha++) {
                        // 增量码长调整，最多 40 次
                        if (!reader.isReadable()) {
                            saveStateAndReturn = true;
                            break loop;
                        }
                        while (modifyLength || reader.readBoolean()) {  // 0=下一符号；1=调整码长
                            if (!reader.isReadable()) {
                                modifyLength = true;
                                saveStateAndReturn = true;
                                break loop;
                            }
                            // 1=码长减一；0=码长加一
                            currLength += reader.readBoolean() ? -1 : 1;
                            modifyLength = false;
                            if (!reader.isReadable()) {
                                saveStateAndReturn = true;
                                break loop;
                            }
                        }
                        codeLength[currGroup][currAlpha] = (byte) currLength;
                    }
                    currLength = -1;
                    currAlpha = huffmanStageDecoder.currentAlpha = 0;
                    modifyLength = false;
                }
                if (saveStateAndReturn) {
                    // 输入不足时保存码长解析状态
                    huffmanStageDecoder.currentGroup = currGroup;
                    huffmanStageDecoder.currentLength = currLength;
                    huffmanStageDecoder.currentAlpha = currAlpha;
                    huffmanStageDecoder.modifyLength = modifyLength;
                    return;
                }

                // 根据码长构建 Canonical Huffman 解码表
                huffmanStageDecoder.createHuffmanDecodingTables();
                currentState = State.DECODE_HUFFMAN_DATA;
                // fall through
            case DECODE_HUFFMAN_DATA:
                blockDecompressor = this.blockDecompressor;
                final int oldReaderIndex = in.readerIndex();
                final boolean decoded = blockDecompressor.decodeHuffmanData(this.huffmanStageDecoder);
                if (!decoded) {
                    return;
                }
                // 避免 bitReader 只消费少量比特而未推进 ByteBuf readerIndex 的异常
                // 因 Huffman 解码可能仅从 bitBuffer 取位，需 refill 同步底层缓冲
                if (in.readerIndex() == oldReaderIndex && in.isReadable()) {
                    reader.refill();
                }

                final int blockLength = blockDecompressor.blockLength();
                ByteBuf uncompressed = ctx.alloc().buffer(blockLength);
                try {
                    int uncByte;
                    while ((uncByte = blockDecompressor.read()) >= 0) {
                        uncompressed.writeByte(uncByte);
                    }
                    // 块数据读完，重置状态并校验块 CRC
                    currentState = State.INIT_BLOCK;
                    int currentBlockCRC = blockDecompressor.checkCRC();
                    streamCRC = (streamCRC << 1 | streamCRC >>> 31) ^ currentBlockCRC;

                    out.add(uncompressed);
                    uncompressed = null;
                } finally {
                    if (uncompressed != null) {
                        uncompressed.release();
                    }
                }
                // 立即返回以便尽快将解压块转发给用户并释放
                return;
            case EOF:
                in.skipBytes(in.readableBytes());
                return;
            default:
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Returns {@code true} if and only if the end of the compressed stream
     * 是否已到达。
     */
    public boolean isClosed() {
        return currentState == State.EOF;
    }
}
