/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.util;


import java.util.Arrays;

/**
 * Base58 编码工具，将字节序列或比特币地址等数据编码为字母数字字符串。
 * <p>
 * 注意：此实现与 Flickr 等平台使用的 base58 不同，请勿混淆。
 * <p>
 * 也可考虑使用 {@code org.bitcoinj.core.EncodedPrivateKey}，
 * 它支持地址中常见的前缀与后缀字节校验。
 * <p>
 * Satoshi 选择 base-58 而非标准 base-64 的原因：
 * <ul>
 * <li>避免 0、O、I、l 等在某些字体中外观相同的字符，防止账户号视觉混淆。</li>
 * <li>含非字母数字字符的字符串不易被接受为账户号。</li>
 * <li>纯字母数字串在邮件中通常不会因缺少标点而无法自动换行。</li>
 * <li>双击可选中整个账户号（全为字母数字时视为一个单词）。</li>
 * </ul>
 * <p>
 * 编解码时间复杂度为 O(n&sup2;)，不适合大数据量。
 * <p>
 * 编码思路：将数据字节视为 base-256 大整数，转换为 base-58 数字，
 * 保留前导零个数（数学运算中会丢失），最后映射为字母数字 ASCII 字符。
 * <p>
 * 已将 bitcoinj 的 AddressFormatException 替换为 IllegalArgumentException；
 * 移除了比特币地址专用的 encodeChecked、decodeChecked 功能。
 */
public class Base58 {
    /** Base58 字母表（不含 0、O、I、l）。 */
    public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char ENCODED_ZERO = ALPHABET[0];
    /** 字符到 Base58 数字的反向索引表。 */
    private static final int[] INDEXES = new int[128];
    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    /**
     * 将字节数组编码为 Base58 字符串（不附加校验和）。
     *
     * @param input 待编码的字节数组
     * @return Base58 编码字符串
     */
    public static String encode(byte[] input) {
        if (input.length == 0) {
            return "";
        }
        // 统计前导零字节
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            ++zeros;
        }
        // 将 base-256 数字转换为 base-58 数字（并映射为 ASCII 字符）
        input = Arrays.copyOf(input, input.length); // 原地修改需要副本
        char[] encoded = new char[input.length * 2]; // 上界估计
        int outputStart = encoded.length;
        for (int inputStart = zeros; inputStart < input.length; ) {
            encoded[--outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
            if (input[inputStart] == 0) {
                ++inputStart; // 优化：跳过前导零
            }
        }
        // 保留与输入相同数量的前导编码零
        while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart;
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO;
        }
        // 返回编码字符串（含前导零）
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

    /**
     * 将 Base58 字符串解码为原始字节数组。
     *
     * @param input Base58 编码字符串
     * @return 解码后的字节数组
     * @throws AddressFormatException 若输入不是合法的 Base58 字符串
     */
    public static byte[] decode(String input) {
        if (input.isEmpty()) {
            return new byte[0];
        }
        // 将 Base58 ASCII 字符转换为 base-58 字节序列
        byte[] input58 = new byte[input.length()];
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            int digit = c < 128 ? INDEXES[c] : -1;
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid character at index: " + i);
            }
            input58[i] = (byte) digit;
        }
        // 统计前导零
        int zeros = 0;
        while (zeros < input58.length && input58[zeros] == 0) {
            ++zeros;
        }
        // 将 base-58 数字转换为 base-256 数字
        byte[] decoded = new byte[input.length()];
        int outputStart = decoded.length;
        for (int inputStart = zeros; inputStart < input58.length; ) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
            if (input58[inputStart] == 0) {
                ++inputStart; // 优化：跳过前导零
            }
        }
        // 忽略计算过程中额外添加的前导零
        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            ++outputStart;
        }
        // 返回解码数据（含原始前导零数量）
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
    }

    /**
     * 对以指定进制表示的字节数组做除法，商写回原数组，返回余数。
     *
     * @param number 被除数（字节数组，每位为一个数字）
     * @param firstDigit 数组中第一个非零数字的索引（用于跳过前导零优化）
     * @param base 数字的进制（最大 256）
     * @param divisor 除数（最大 256）
     * @return 除法余数
     */
    private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        // 按指定进制的长除法
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }
}
