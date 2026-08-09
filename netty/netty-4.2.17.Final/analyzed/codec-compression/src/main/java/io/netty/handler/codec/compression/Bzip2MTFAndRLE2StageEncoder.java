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

import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_MAX_ALPHABET_SIZE;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_SYMBOL_RUNA;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_SYMBOL_RUNB;

/**
 * Bzip2 的 MTF 变换与游程编码[2] 编码器。<br>
 * 概念上两阶段独立，但合并为单次遍历更高效。
 */
final class Bzip2MTFAndRLE2StageEncoder {
    /**
     * Burrows-Wheeler 变换后的块。
     */
    private final int[] bwtBlock;

    /**
     * {@link #bwtBlock} 有效长度。
     */
    private final int bwtLength;

    /**
     * At each position, {@code true} if the byte value with that index is present within the block,
     * 否则 {@code false}。
     */
    private final boolean[] bwtValuesPresent;

    /**
     * MTF 与游程编码[2] 的输出符号序列。
     */
    private final char[] mtfBlock;

    /**
     * The actual number of values contained in the {@link #mtfBlock} array.
     */
    private int mtfLength;

    /**
     * The global frequencies of values within the {@link #mtfBlock} array.
     */
    private final int[] mtfSymbolFrequencies = new int[HUFFMAN_MAX_ALPHABET_SIZE];

    /**
     * 编码后的字母表大小。
     */
    private int alphabetSize;

    /**
     * @param bwtBlock The Burrows Wheeler Transformed block data
     * @param bwtLength The actual length of the BWT data
     * @param bwtValuesPresent The values that are present within the BWT data. For each index,
     *            {@code true} if that value is present within the data, otherwise {@code false}
     */
    Bzip2MTFAndRLE2StageEncoder(final int[] bwtBlock, final int bwtLength, final boolean[] bwtValuesPresent) {
        this.bwtBlock = bwtBlock;
        this.bwtLength = bwtLength;
        this.bwtValuesPresent = bwtValuesPresent;
        mtfBlock = new char[bwtLength + 1];
    }

    /**
     * 执行 MTF 变换与游程编码[2]（对零游程用 RUNA/RUNB 编码）。
     */
    void encode() {
        final int bwtLength = this.bwtLength;
        final boolean[] bwtValuesPresent = this.bwtValuesPresent;
        final int[] bwtBlock = this.bwtBlock;
        final char[] mtfBlock = this.mtfBlock;
        final int[] mtfSymbolFrequencies = this.mtfSymbolFrequencies;
        final byte[] huffmanSymbolMap = new byte[256];
        final Bzip2MoveToFrontTable symbolMTF = new Bzip2MoveToFrontTable();

        int totalUniqueValues = 0;
        for (int i = 0; i < huffmanSymbolMap.length; i++) {
            if (bwtValuesPresent[i]) {
                huffmanSymbolMap[i] = (byte) totalUniqueValues++;
            }
        }
        final int endOfBlockSymbol = totalUniqueValues + 1;

        int mtfIndex = 0;
        int repeatCount = 0;
        int totalRunAs = 0;
        int totalRunBs = 0;
        for (int i = 0; i < bwtLength; i++) {
            // 移到前面：记录符号在 MTF 表中的原位置
            final int mtfPosition = symbolMTF.valueToFront(huffmanSymbolMap[bwtBlock[i] & 0xff]);
            // 对连续零位置（重复 MTF 索引 0）做游程编码
            if (mtfPosition == 0) {
                repeatCount++;
            } else {
                if (repeatCount > 0) {
                    repeatCount--;
                    while (true) {
                        if ((repeatCount & 1) == 0) {
                            mtfBlock[mtfIndex++] = HUFFMAN_SYMBOL_RUNA;
                            totalRunAs++;
                        } else {
                            mtfBlock[mtfIndex++] = HUFFMAN_SYMBOL_RUNB;
                            totalRunBs++;
                        }

                        if (repeatCount <= 1) {
                            break;
                        }
                        repeatCount = (repeatCount - 2) >>> 1;
                    }
                    repeatCount = 0;
                }
                mtfBlock[mtfIndex++] = (char) (mtfPosition + 1);
                mtfSymbolFrequencies[mtfPosition + 1]++;
            }
        }

        if (repeatCount > 0) {
            repeatCount--;
            while (true) {
                if ((repeatCount & 1) == 0) {
                    mtfBlock[mtfIndex++] = HUFFMAN_SYMBOL_RUNA;
                    totalRunAs++;
                } else {
                    mtfBlock[mtfIndex++] = HUFFMAN_SYMBOL_RUNB;
                    totalRunBs++;
                }

                if (repeatCount <= 1) {
                    break;
                }
                repeatCount = (repeatCount - 2) >>> 1;
            }
        }

        mtfBlock[mtfIndex] = (char) endOfBlockSymbol;
        mtfSymbolFrequencies[endOfBlockSymbol]++;
        mtfSymbolFrequencies[HUFFMAN_SYMBOL_RUNA] += totalRunAs;
        mtfSymbolFrequencies[HUFFMAN_SYMBOL_RUNB] += totalRunBs;

        mtfLength = mtfIndex + 1;
        alphabetSize = endOfBlockSymbol + 1;
    }

    /**
     * @return 编码后的 MTF 符号数组
     */
    char[] mtfBlock() {
        return mtfBlock;
    }

    /**
     * @return MTF 块有效长度
     */
    int mtfLength() {
        return mtfLength;
    }

    /**
     * @return MTF 字母表大小
     */
    int mtfAlphabetSize() {
        return alphabetSize;
    }

    /**
     * @return 各 MTF 符号频率
     */
    int[] mtfSymbolFrequencies() {
        return mtfSymbolFrequencies;
    }
}
