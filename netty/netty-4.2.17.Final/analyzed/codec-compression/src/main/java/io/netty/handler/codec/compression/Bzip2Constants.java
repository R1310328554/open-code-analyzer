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

/**
 * {@link Bzip2Encoder} 与 {@link Bzip2Decoder} 共用的 Bzip2 格式常量。
 */
final class Bzip2Constants {

    /** Bzip2 流标识魔数（"BZh"）。 */
    static final int MAGIC_NUMBER = 'B' << 16 | 'Z' << 8 | 'h';

    /** 块头魔数，对应 π 的 BCD 表示。 */
    static final int BLOCK_HEADER_MAGIC_1 = 0x314159;
    static final int BLOCK_HEADER_MAGIC_2 = 0x265359;

    /** 流结束魔数，对应 √π 的 BCD 表示。 */
    static final int END_OF_STREAM_MAGIC_1 = 0x177245;
    static final int END_OF_STREAM_MAGIC_2 = 0x385090;

    /** 块大小基数（100000 字节）。 */
    static final int BASE_BLOCK_SIZE = 100000;

    /** 块大小乘数的最小值与最大值，需乘以 {@link Bzip2Constants#BASE_BLOCK_SIZE}。 */
    static final int MIN_BLOCK_SIZE = 1;
    static final int MAX_BLOCK_SIZE = 9;

    static final int MAX_BLOCK_LENGTH = MAX_BLOCK_SIZE * BASE_BLOCK_SIZE;

    /** Huffman 字母表最大符号数。 */
    static final int HUFFMAN_MAX_ALPHABET_SIZE = 258;

    /** 编码器生成的最长 Huffman 码长。 */
    static final int HUFFMAN_ENCODE_MAX_CODE_LENGTH = 20;

    /** 解码器接受的最长 Huffman 码长。 */
    static final int HUFFMAN_DECODE_MAX_CODE_LENGTH = 23;

    /** 用于游程编码的 Huffman 特殊符号 RUNA/RUNB。 */
    static final int HUFFMAN_SYMBOL_RUNA = 0;
    static final int HUFFMAN_SYMBOL_RUNB = 1;

    /** 符号使用位图中每个分组的字节数（16）。 */
    static final int HUFFMAN_SYMBOL_RANGE_SIZE = 16;

    /** MTF 编码的 Huffman 表选择器零终止位串最大长度。 */
    static final int HUFFMAN_SELECTOR_LIST_MAX_LENGTH = 6;

    /** 解码多少个符号后切换 Huffman 表。 */
    static final int HUFFMAN_GROUP_RUN_LENGTH = 50;

    /** Huffman 表选择器的最大数量。 */
    static final int MAX_SELECTORS = 2 + 900000 / HUFFMAN_GROUP_RUN_LENGTH; // 18002

    /** 备选 Huffman 表的最少数量。 */
    static final int HUFFMAN_MINIMUM_TABLES = 2;

    /** 备选 Huffman 表的最大数量。 */
    static final int HUFFMAN_MAXIMUM_TABLES = 6;

    /** 工具类，禁止实例化。 */
    private Bzip2Constants() { }
}
