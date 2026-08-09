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
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_GROUP_RUN_LENGTH;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_MAX_ALPHABET_SIZE;

/**
 * Bzip2 Huffman 编码阶段的解码器：维护多张 Canonical 表，每 50 符号切换选择器。
 */
final class Bzip2HuffmanStageDecoder {
    /** 位级输入读取器。 */
    private final Bzip2BitReader reader;

    /** 每 50 个符号一组所使用的 Huffman 表编号。 */
    byte[] selectors;

    /**
     * The minimum code length for each Huffman table.
     */
    private final int[] minimumLengths;

    /**
     * An array of values for each Huffman table that must be subtracted from the numerical value of
     * 给定码长的 Canonical 码索引需减去的基值。
     */
    private final int[][] codeBases;

    /**
     * An array of values for each Huffman table that gives the highest numerical value of a Huffman
     * 给定码长的 Huffman 码数值上界。
     */
    private final int[][] codeLimits;

    /**
     * 各表从 Canonical 码索引到输出符号的映射。
     */
    private final int[][] codeSymbols;

    /** 当前 50 符号组使用的 Huffman 表索引。 */
    private int currentTable;

    /** 当前组在 selectors 数组中的索引。 */
    private int groupIndex = -1;

    /** 当前组内已解码符号计数；每 50 个切换表。 */
     */
    private int groupPosition = -1;

    /**
     * 使用的 Huffman 表总数（2..6）。
     */
    final int totalTables;

    /**
     * 字母表大小（各表一致）。
     */
    final int alphabetSize;

    /**
     * 解码 Huffman 表选择器用的 MTF 表。
     */
    final Bzip2MoveToFrontTable tableMTF = new Bzip2MoveToFrontTable();

    // 输入缓冲读尽时保存的中间解析状态
    int currentSelector;

    /**
     * The Canonical Huffman code lengths for each table.
     */
    final byte[][] tableCodeLengths;

    // For saving state if end of current ByteBuf was reached
    int currentGroup;
    int currentLength = -1;
    int currentAlpha;
    boolean modifyLength;

    Bzip2HuffmanStageDecoder(final Bzip2BitReader reader, final int totalTables, final int alphabetSize) {
        this.reader = reader;
        this.totalTables = totalTables;
        this.alphabetSize = alphabetSize;

        minimumLengths = new int[totalTables];
        codeBases = new int[totalTables][HUFFMAN_DECODE_MAX_CODE_LENGTH + 2];
        codeLimits = new int[totalTables][HUFFMAN_DECODE_MAX_CODE_LENGTH + 1];
        codeSymbols = new int[totalTables][HUFFMAN_MAX_ALPHABET_SIZE];
        tableCodeLengths = new byte[totalTables][HUFFMAN_MAX_ALPHABET_SIZE];
    }

    /**
     * 根据各表 Canonical 码长构建解码用的基址、上界与符号映射。
     */
    void createHuffmanDecodingTables() {
        final int alphabetSize = this.alphabetSize;

        for (int table = 0; table < tableCodeLengths.length; table++) {
            final int[] tableBases = codeBases[table];
            final int[] tableLimits = codeLimits[table];
            final int[] tableSymbols = codeSymbols[table];
            final byte[] codeLengths = tableCodeLengths[table];

            int minimumLength = HUFFMAN_DECODE_MAX_CODE_LENGTH;
            int maximumLength = 0;

            // 求本表最短与最长码长
            for (int i = 0; i < alphabetSize; i++) {
                final byte currLength = codeLengths[i];
                maximumLength = Math.max(currLength, maximumLength);
                minimumLength = Math.min(currLength, minimumLength);
            }
            minimumLengths[table] = minimumLength;

            // 统计各码长符号数量
            for (int i = 0; i < alphabetSize; i++) {
                tableBases[codeLengths[i] + 1]++;
            }
            for (int i = 1, b = tableBases[0]; i < HUFFMAN_DECODE_MAX_CODE_LENGTH + 2; i++) {
                b += tableBases[i];
                tableBases[i] = b;
            }

            // 计算各码长的 Canonical 码区间
            for (int i = minimumLength, code = 0; i <= maximumLength; i++) {
                int base = code;
                code += tableBases[i + 1] - tableBases[i];
                tableBases[i] = base - tableBases[i];
                tableLimits[i] = code - 1;
                code <<= 1;
            }

            // 填充 Canonical 索引到符号的映射
            for (int bitLength = minimumLength, codeIndex = 0; bitLength <= maximumLength; bitLength++) {
                for (int symbol = 0; symbol < alphabetSize; symbol++) {
                    if (codeLengths[symbol] == bitLength) {
                        tableSymbols[codeIndex++] = symbol;
                    }
                }
            }
        }

        currentTable = selectors[0];
    }

    /**
     * 解码并返回下一个 Huffman 符号。
     * @return 解码得到的符号
     */
    int nextSymbol() {
        // 每 50 符号切换到下一选择器指定的表
        if (++groupPosition % HUFFMAN_GROUP_RUN_LENGTH == 0) {
            groupIndex++;
            if (groupIndex == selectors.length) {
                throw new DecompressionException("error decoding block");
            }
            currentTable = selectors[groupIndex] & 0xff;
        }

        final Bzip2BitReader reader = this.reader;
        final int currentTable = this.currentTable;
        final int[] tableLimits = codeLimits[currentTable];
        final int[] tableBases = codeBases[currentTable];
        final int[] tableSymbols = codeSymbols[currentTable];
        int codeLength = minimumLengths[currentTable];

        // 从最短码长起逐位读取直至识别完整码字
        int codeBits = reader.readBits(codeLength);
        for (; codeLength <= HUFFMAN_DECODE_MAX_CODE_LENGTH; codeLength++) {
            if (codeBits <= tableLimits[codeLength]) {
                // 将 Canonical 码转换为符号索引并返回
                return tableSymbols[codeBits - tableBases[codeLength]];
            }
            codeBits = codeBits << 1 | reader.readBits(1);
        }

        throw new DecompressionException("a valid code was not recognised");
    }
}
