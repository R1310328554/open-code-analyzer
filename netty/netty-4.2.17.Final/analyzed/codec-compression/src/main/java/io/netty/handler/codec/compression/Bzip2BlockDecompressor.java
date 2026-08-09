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

import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_DECODE_MAX_CODE_LENGTH;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_SYMBOL_RUNA;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_SYMBOL_RUNB;
import static io.netty.handler.codec.compression.Bzip2Constants.MAX_BLOCK_LENGTH;

/**
 * 读取并解压单个 Bzip2 数据块。<br><br>
 *
 * 块解码包含以下阶段：<br>
 * 1. 读取块头<br>
 * 2. 读取 Huffman 表<br>
 * 3. 读取并解码 Huffman 数据 — {@link #decodeHuffmanData(Bzip2HuffmanStageDecoder)}<br>
 * 4. 游程解码[2] — {@link #decodeHuffmanData(Bzip2HuffmanStageDecoder)}<br>
 * 5. 逆 MTF 变换 — {@link #decodeHuffmanData(Bzip2HuffmanStageDecoder)}<br>
 * 6. 逆 BWT — {@link #initialiseInverseBWT()}<br>
 * 7. 游程解码[1] — {@link #read()}<br>
 * 8. 可选块去随机化 — {@link #read()}（经 {@link #decodeNextBWTByte()}）
 */
final class Bzip2BlockDecompressor {
    /** 提供位级读取的 {@link Bzip2BitReader}。 */
    private final Bzip2BitReader reader;

    /** 根据完全解码后的块字节计算 CRC。 */
    private final Crc32 crc = new Crc32();

    /** 从块头读取的当前块 CRC。 */
    private final int blockCRC;

    /** 当前块是否经随机化处理。 */
    private final boolean blockRandomised;

    /* Huffman 解码阶段 */
    /**
     * 块结束 Huffman 符号；遇到此符号即停止解码该块。
     */
    int huffmanEndOfBlockSymbol;

    /**
     * 16 字节分组的符号使用位图（哪些 16 字节区间有符号）。
     */
    int huffmanInUse16;

    /**
     * A map from Huffman symbol index to output character. Some types of data (e.g. ASCII text)
     * may contain only a limited number of byte values; Huffman symbols are only allocated to
     * 仅为未压缩数据中实际出现的字节值分配 Huffman 符号。
     */
    final byte[] huffmanSymbolMap = new byte[256];

    /* MTF 阶段 */
    /**
     * Counts of each byte value within the {@link Bzip2BlockDecompressor#huffmanSymbolMap} data.
     * 在 MTF 阶段统计，供逆 BWT 阶段使用。
     */
    private final int[] bwtByteCounts = new int[256];

    /**
     * The Burrows-Wheeler Transform processed data. Read at the Move To Front stage, consumed by the
     * 逆 Burrows-Wheeler 变换阶段使用。
     */
    private final byte[] bwtBlock;

    /**
     * 逆 BWT 后的 BWT 起始指针。
     */
    private final int bwtStartPointer;

    /* 逆 BWT 阶段 */
    /**
     * At each position contains the union of :-
     *   An output character (8 bits)
     *   A pointer from each position to its successor (24 bits, left shifted 8 bits)
     * As the pointer cannot exceed the maximum block size of 900k, 24 bits is more than enough to
     * 指针不超过 900k 块上限，24 位足够；逆 BWT 时将字符并入 int 高位，
     * 在最终解码阶段合并存储可大幅减少内存访问。
     */
    private int[] bwtMergedPointers;

    /**
     * 当前在合并 BWT 指针数组中的位置。
     */
    private int bwtCurrentMergedPointer;

    /**
     * The actual length in bytes of the current block at the Inverse Burrows Wheeler Transform
     * 阶段时的实际块长度（最终游程解码之前）。
     */
    private int bwtBlockLength;

    /**
     * 逆 BWT 阶段已解码的输出字节数。
     */
    private int bwtBytesDecoded;

    /* 游程编码与随机扰动阶段 */
    /**
     * 最近一次 RLE 解码得到的字节。
     */
    private int rleLastDecodedByte = -1;

    /**
     * The number of previous identical output bytes decoded. After 4 identical bytes, the next byte
     * 解码下一个字节作为 RLE 重复次数。
     */
    private int rleAccumulator;

    /**
     * 当前字节的 RLE 剩余重复次数；归零后解码新字节。
     */
    private int rleRepeat;

    /**
     * 随机化块时在 RNUMS 数组中的当前索引。
     */
    private int randomIndex;

    /**
     * 随机化块时当前 RNUMS 位置剩余字节数。
     */
    private int randomCount = Bzip2Rand.rNums(0) - 1;

    /**
     * MTF 变换用的移到前面表。
     */
    private final Bzip2MoveToFrontTable symbolMTF = new Bzip2MoveToFrontTable();

    // 输入比特不足时保存 Huffman/MTF 解码中间状态
    private int repeatCount;
    private int repeatIncrement = 1;
    private int mtfValue;

    Bzip2BlockDecompressor(final int blockSize, final int blockCRC, final boolean blockRandomised,
                           final int bwtStartPointer, final Bzip2BitReader reader) {

        bwtBlock = new byte[blockSize];

        this.blockCRC = blockCRC;
        this.blockRandomised = blockRandomised;
        this.bwtStartPointer = bwtStartPointer;

        this.reader = reader;
    }

    /**
     * Reads the Huffman encoded data from the input stream, performs Run-Length Decoding and
     * 并应用逆 MTF 重建 Burrows-Wheeler 变换数组。
     */
    boolean decodeHuffmanData(final Bzip2HuffmanStageDecoder huffmanDecoder) {
        final Bzip2BitReader reader = this.reader;
        final byte[] bwtBlock = this.bwtBlock;
        final byte[] huffmanSymbolMap = this.huffmanSymbolMap;
        final int streamBlockSize = this.bwtBlock.length;
        final int huffmanEndOfBlockSymbol = this.huffmanEndOfBlockSymbol;
        final int[] bwtByteCounts = this.bwtByteCounts;
        final Bzip2MoveToFrontTable symbolMTF = this.symbolMTF;

        int bwtBlockLength = this.bwtBlockLength;
        int repeatCount = this.repeatCount;
        int repeatIncrement = this.repeatIncrement;
        int mtfValue = this.mtfValue;

        for (;;) {
            if (!reader.hasReadableBits(HUFFMAN_DECODE_MAX_CODE_LENGTH)) {
                this.bwtBlockLength = bwtBlockLength;
                this.repeatCount = repeatCount;
                this.repeatIncrement = repeatIncrement;
                this.mtfValue = mtfValue;
                return false;
            }
            final int nextSymbol = huffmanDecoder.nextSymbol();

            if (nextSymbol == HUFFMAN_SYMBOL_RUNA) {
                repeatCount += repeatIncrement;
                repeatIncrement <<= 1;
            } else if (nextSymbol == HUFFMAN_SYMBOL_RUNB) {
                repeatCount += repeatIncrement << 1;
                repeatIncrement <<= 1;
            } else {
                if (repeatCount > 0) {
                    if (bwtBlockLength + repeatCount > streamBlockSize) {
                        throw new DecompressionException("block exceeds declared block size");
                    }
                    final byte nextByte = huffmanSymbolMap[mtfValue];
                    bwtByteCounts[nextByte & 0xff] += repeatCount;
                    while (--repeatCount >= 0) {
                        bwtBlock[bwtBlockLength++] = nextByte;
                    }

                    repeatCount = 0;
                    repeatIncrement = 1;
                }

                if (nextSymbol == huffmanEndOfBlockSymbol) {
                    break;
                }

                if (bwtBlockLength >= streamBlockSize) {
                    throw new DecompressionException("block exceeds declared block size");
                }

                mtfValue = symbolMTF.indexToFront(nextSymbol - 1) & 0xff;

                final byte nextByte = huffmanSymbolMap[mtfValue];
                bwtByteCounts[nextByte & 0xff]++;
                bwtBlock[bwtBlockLength++] = nextByte;
            }
        }
        if (bwtBlockLength > MAX_BLOCK_LENGTH) {
            throw new DecompressionException("block length exceeds max block length: "
                    + bwtBlockLength + " > " + MAX_BLOCK_LENGTH);
        }

        this.bwtBlockLength = bwtBlockLength;
        initialiseInverseBWT();
        return true;
    }

    /**
     * 构建逆 BWT 用的合并指针数组。
     */
    private void initialiseInverseBWT() {
        final int bwtStartPointer = this.bwtStartPointer;
        final byte[] bwtBlock  = this.bwtBlock;
        final int[] bwtMergedPointers = new int[bwtBlockLength];
        final int[] characterBase = new int[256];

        if (bwtStartPointer < 0 || bwtStartPointer >= bwtBlockLength) {
            throw new DecompressionException("start pointer invalid");
        }

        // 累积字符计数，确定各字符在 BWT 列中的起始位置
        System.arraycopy(bwtByteCounts, 0, characterBase, 1, 255);
        for (int i = 2; i <= 255; i++) {
            characterBase[i] += characterBase[i - 1];
        }

        // 合并数组式逆 BWT：字符与后继指针写入同一 int
        // 合并字符与后继指针到同一数组，最终遍历时可减少内存访问
        for (int i = 0; i < bwtBlockLength; i++) {
            int value = bwtBlock[i] & 0xff;
            bwtMergedPointers[characterBase[value]++] = (i << 8) + value;
        }

        this.bwtMergedPointers = bwtMergedPointers;
        bwtCurrentMergedPointer = bwtMergedPointers[bwtStartPointer];
    }

    /**
     * Decodes a byte from the final Run-Length Encoding stage, pulling a new byte from the
     * 必要时从 BWT 阶段拉取新字节。
     * @return 解码字节，无更多数据时返回 -1
     */
    public int read() {
        while (rleRepeat < 1) {
            if (bwtBytesDecoded == bwtBlockLength) {
                return -1;
            }

            int nextByte = decodeNextBWTByte();
            if (nextByte != rleLastDecodedByte) {
                // 新字节，重新开始 RLE 累积
                rleLastDecodedByte = nextByte;
                rleRepeat = 1;
                rleAccumulator = 1;
                crc.updateCRC(nextByte);
            } else {
                if (++rleAccumulator == 4) {
                    if (bwtBytesDecoded >= bwtBlockLength) {
                        throw new DecompressionException("malformed RLE: run-length byte missing at end of block");
                    }
                    // 累积满 4 个相同字节，读取重复次数
                    int rleRepeat = decodeNextBWTByte() + 1;
                    this.rleRepeat = rleRepeat;
                    rleAccumulator = 0;
                    crc.updateCRC(nextByte, rleRepeat);
                } else {
                    rleRepeat = 1;
                    crc.updateCRC(nextByte);
                }
            }
        }
        rleRepeat--;

        return rleLastDecodedByte;
    }

    /**
     * Decodes a byte from the Burrows-Wheeler Transform stage. If the block has randomisation
     * 则按 RNUMS 表撤销随机化（异或 1）。
     * @return 从 BWT 阶段解码的一个字节
     */
    private int decodeNextBWTByte() {
        int mergedPointer = bwtCurrentMergedPointer;
        int nextDecodedByte =  mergedPointer & 0xff;
        bwtCurrentMergedPointer = bwtMergedPointers[mergedPointer >>> 8];

        if (blockRandomised) {
            if (--randomCount == 0) {
                nextDecodedByte ^= 1;
                randomIndex = (randomIndex + 1) % 512;
                randomCount = Bzip2Rand.rNums(randomIndex);
            }
        }
        bwtBytesDecoded++;

        return nextDecodedByte;
    }

    public int blockLength() {
        return bwtBlockLength;
    }

    /**
     * Verify and return the block CRC. This method may only be called
     * 必须在块内所有字节读完后调用。
     * @return The block CRC
     */
    int checkCRC() {
        final int computedBlockCRC = crc.getCRC();
        if (blockCRC != computedBlockCRC) {
            throw new DecompressionException("block CRC error");
        }
        return computedBlockCRC;
    }
}
