/*
 * Copyright 2012 The Netty Project
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
package io.netty.util.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.*;

/**
 * String utility class.
 *
 * <p>字符串工具类：十六进制编解码、CSV 转义、类名简化、空白与代理字符处理等。</p>
 */
public final class StringUtil {

    /** 空字符串常量。 */
    public static final String EMPTY_STRING = "";
    /** 平台换行符，来自系统属性 line.separator。 */
    public static final String NEWLINE = SystemPropertyUtil.get("line.separator", "\n");

    /** 双引号字符。 */
    public static final char DOUBLE_QUOTE = '\"';
    /** 逗号字符。 */
    public static final char COMMA = ',';
    /** 换行符 LF。 */
    public static final char LINE_FEED = '\n';
    /** 回车符 CR。 */
    public static final char CARRIAGE_RETURN = '\r';
    /** 制表符。 */
    public static final char TAB = '\t';
    /** 空格字符（RFC7230 OWS 之一）。 */
    public static final char SPACE = 0x20;

    private static final String[] BYTE2HEX_PAD = new String[256];
    private static final String[] BYTE2HEX_NOPAD = new String[256];
    private static final byte[] HEX2B;

    /**
     * 2 - Quote character at beginning and end.
     * 5 - Extra allowance for anticipated escape characters that may be added.
     
 *
 * <p>CSV 转义预留字符数：首尾引号 2 个 + 预期转义字符 5 个。</p>
 */
    private static final int CSV_NUMBER_ESCAPE_CHARACTERS = 2 + 5;
    private static final char PACKAGE_SEPARATOR_CHAR = '.';

    static {
        // 预生成字节到两位十六进制字符串的查找表。
        for (int i = 0; i < BYTE2HEX_PAD.length; i++) {
            String str = Integer.toHexString(i);
            BYTE2HEX_PAD[i] = i > 0xf ? str : ('0' + str);
            BYTE2HEX_NOPAD[i] = str;
        }
        // 预生成十六进制字符到数值的查找表：
        // 表长为 Character.MAX_VALUE+1，用 char 作索引时 JVM 可省略边界检查
        // （以 char 为索引访问）。
        HEX2B = new byte[Character.MAX_VALUE + 1];
        Arrays.fill(HEX2B, (byte) -1);
        HEX2B['0'] = 0;
        HEX2B['1'] = 1;
        HEX2B['2'] = 2;
        HEX2B['3'] = 3;
        HEX2B['4'] = 4;
        HEX2B['5'] = 5;
        HEX2B['6'] = 6;
        HEX2B['7'] = 7;
        HEX2B['8'] = 8;
        HEX2B['9'] = 9;
        HEX2B['A'] = 10;
        HEX2B['B'] = 11;
        HEX2B['C'] = 12;
        HEX2B['D'] = 13;
        HEX2B['E'] = 14;
        HEX2B['F'] = 15;
        HEX2B['a'] = 10;
        HEX2B['b'] = 11;
        HEX2B['c'] = 12;
        HEX2B['d'] = 13;
        HEX2B['e'] = 14;
        HEX2B['f'] = 15;
    }

    private StringUtil() {
        // 工具类禁止实例化。
    }

    /**
     * Get the item after one char delim if the delim is found (else null).
     * This operation is a simplified and optimized
     * version of {@link String#split(String, int)}.
     
 *
 * <p>找到分隔符则返回其后子串，否则 null；比 split 更轻量。</p>
 */
    public static String substringAfter(String value, char delim) {
        int pos = value.indexOf(delim);
        if (pos >= 0) {
            return value.substring(pos + 1);
        }
        return null;
    }

    /**
     * Get the item before one char delim if the delim is found (else null).
     * This operation is a simplified and optimized
     * version of {@link String#split(String, int)}.
     
 *
 * <p>找到分隔符则返回其前子串，否则 null。</p>
 */
    public static String substringBefore(String value, char delim) {
        int pos = value.indexOf(delim);
        if (pos >= 0) {
            return value.substring(0, pos);
        }
        return null;
    }

    /**
     * Checks if two strings have the same suffix of specified length
     *
     * @param s   string
     * @param p   string
     * @param len length of the common suffix
     * @return true if both s and p are not null and both have the same suffix. Otherwise - false
     
 *
 * <p>判断两字符串是否拥有相同长度的公共后缀。</p>
 */
    public static boolean commonSuffixOfLength(String s, String p, int len) {
        return s != null && p != null && len >= 0 && s.regionMatches(s.length() - len, p, p.length() - len, len);
    }

    /**
     * Converts the specified byte value into a 2-digit hexadecimal integer.
     
 *
 * <p>将字节转为两位十六进制字符串（不足补零）。</p>
 */
    public static String byteToHexStringPadded(int value) {
        return BYTE2HEX_PAD[value & 0xff];
    }

    /**
     * Converts the specified byte value into a 2-digit hexadecimal integer and appends it to the specified buffer.
     
 *
 * <p>将十六进制结果追加到 Appendable 缓冲区。</p>
 */
    public static <T extends Appendable> T byteToHexStringPadded(T buf, int value) {
        try {
            buf.append(byteToHexStringPadded(value));
        } catch (IOException e) {
            PlatformDependent.throwException(e);
        }
        return buf;
    }

    /**
     * Converts the specified byte array into a hexadecimal value.
     
 *
 * <p>将字节数组转为十六进制字符串。</p>
 */
    public static String toHexStringPadded(byte[] src) {
        return toHexStringPadded(src, 0, src.length);
    }

    /**
     * Converts the specified byte array into a hexadecimal value.
     */
    public static String toHexStringPadded(byte[] src, int offset, int length) {
        return toHexStringPadded(new StringBuilder(length << 1), src, offset, length).toString();
    }

    /**
     * Converts the specified byte array into a hexadecimal value and appends it to the specified buffer.
     */
    public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src) {
        return toHexStringPadded(dst, src, 0, src.length);
    }

    /**
     * Converts the specified byte array into a hexadecimal value and appends it to the specified buffer.
     */
    public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src, int offset, int length) {
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            byteToHexStringPadded(dst, src[i]);
        }
        return dst;
    }

    /**
     * Converts the specified byte value into a hexadecimal integer.
     */
    public static String byteToHexString(int value) {
        return BYTE2HEX_NOPAD[value & 0xff];
    }

    /**
     * Converts the specified byte value into a hexadecimal integer and appends it to the specified buffer.
     */
    public static <T extends Appendable> T byteToHexString(T buf, int value) {
        try {
            buf.append(byteToHexString(value));
        } catch (IOException e) {
            PlatformDependent.throwException(e);
        }
        return buf;
    }

    /**
     * Converts the specified byte array into a hexadecimal value.
     */
    public static String toHexString(byte[] src) {
        return toHexString(src, 0, src.length);
    }

    /**
     * Converts the specified byte array into a hexadecimal value.
     */
    public static String toHexString(byte[] src, int offset, int length) {
        return toHexString(new StringBuilder(length << 1), src, offset, length).toString();
    }

    /**
     * Converts the specified byte array into a hexadecimal value and appends it to the specified buffer.
     */
    public static <T extends Appendable> T toHexString(T dst, byte[] src) {
        return toHexString(dst, src, 0, src.length);
    }

    /**
     * Converts the specified byte array into a hexadecimal value and appends it to the specified buffer.
     */
    public static <T extends Appendable> T toHexString(T dst, byte[] src, int offset, int length) {
        assert length >= 0;
        if (length == 0) {
            return dst;
        }

        final int end = offset + length;
        final int endMinusOne = end - 1;
        int i;

        // 跳过前导零字节。
        for (i = offset; i < endMinusOne; i++) {
            if (src[i] != 0) {
                break;
            }
        }

        byteToHexString(dst, src[i++]);
        int remaining = end - i;
        toHexStringPadded(dst, src, i, remaining);

        return dst;
    }

    /**
     * Helper to decode half of a hexadecimal number from a string.
     * @param c The ASCII character of the hexadecimal number to decode.
     * Must be in the range {@code [0-9a-fA-F]}.
     * @return The hexadecimal value represented in the ASCII character
     * given, or {@code -1} if the character is invalid.
     
 *
 * <p>从字符解码半个十六进制位，非法返回 -1。</p>
 */
    public static int decodeHexNibble(final char c) {
        // 不用 Character.digit()，因其覆盖更宽字符集（含全角拉丁字母）。
        // 此处仅支持标准 ASCII 十六进制字符。
        return HEX2B[c];
    }

    /**
     * Helper to decode half of a hexadecimal number from a string.
     * @param b The ASCII character of the hexadecimal number to decode.
     * Must be in the range {@code [0-9a-fA-F]}.
     * @return The hexadecimal value represented in the ASCII character
     * given, or {@code -1} if the character is invalid.
     */
    public static int decodeHexNibble(final byte b) {
        // 不用 Character.digit()，因其覆盖更宽字符集（含全角拉丁字母）。
        // 此处仅支持标准 ASCII 十六进制字符。
        return HEX2B[b];
    }

    /**
     * Decode a 2-digit hex byte from within a string.
     
 *
 * <p>从字符串指定位置解码一个字节（两个十六进制字符）。</p>
 */
    public static byte decodeHexByte(CharSequence s, int pos) {
        int hi = decodeHexNibble(s.charAt(pos));
        int lo = decodeHexNibble(s.charAt(pos + 1));
        if (hi == -1 || lo == -1) {
            throw new IllegalArgumentException(String.format(
                    "invalid hex byte '%s' at index %d of '%s'", s.subSequence(pos, pos + 2), pos, s));
        }
        return (byte) ((hi << 4) + lo);
    }

    /**
     * Decodes part of a string with <a href="https://en.wikipedia.org/wiki/Hex_dump">hex dump</a>
     *
     * @param hexDump a {@link CharSequence} which contains the hex dump
     * @param fromIndex start of hex dump in {@code hexDump}
     * @param length hex string length
     
 *
 * <p>解码 hex dump 字符串的指定片段为字节数组。</p>
 */
    public static byte[] decodeHexDump(CharSequence hexDump, int fromIndex, int length) {
        if (length < 0 || (length & 1) != 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        if (length == 0) {
            return EmptyArrays.EMPTY_BYTES;
        }
        byte[] bytes = new byte[length >>> 1];
        for (int i = 0; i < length; i += 2) {
            bytes[i >>> 1] = decodeHexByte(hexDump, fromIndex + i);
        }
        return bytes;
    }

    /**
     * Decodes a <a href="https://en.wikipedia.org/wiki/Hex_dump">hex dump</a>
     
 *
 * <p>解码完整 hex dump 字符串。</p>
 */
    public static byte[] decodeHexDump(CharSequence hexDump) {
        return decodeHexDump(hexDump, 0, hexDump.length());
    }

    /**
     * Generates a class name from a {@link Class}. Similar to {@link Class#getName()}, but null-safe.
     
 *
 * <p>获取对象类名，null 时返回 "null_object"。</p>
 */
    public static String className(Object o) {
        if (o == null) {
            return "null_object";
        } else {
            return o.getClass().getName();
        }
    }

    /**
     * The shortcut to {@link #simpleClassName(Class) simpleClassName(o.getClass())}.
     
 *
 * <p>获取对象简单类名，null 时返回 "null_object"。</p>
 */
    public static String simpleClassName(Object o) {
        if (o == null) {
            return "null_object";
        } else {
            return simpleClassName(o.getClass());
        }
    }

    /**
     * Generates a simplified name from a {@link Class}.  Similar to {@link Class#getSimpleName()}, but it works fine
     * with anonymous classes.
     
 *
 * <p>获取简单类名，匿名类场景比 Class#getSimpleName 更可靠。</p>
 */
    public static String simpleClassName(Class<?> clazz) {
        String className = checkNotNull(clazz, "clazz").getName();
        final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (lastDotIdx > -1) {
            return className.substring(lastDotIdx + 1);
        }
        return className;
    }

    /**
     * Escapes the specified value, if necessary according to
     * <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>.
     *
     * @param value The value which will be escaped according to
     *              <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>
     * @return {@link CharSequence} the escaped value if necessary, or the value unchanged
     
 *
 * <p>按 RFC-4180 对 CSV 字段进行必要转义。</p>
 */
    public static CharSequence escapeCsv(CharSequence value) {
        return escapeCsv(value, false);
    }

    /**
     * Escapes the specified value, if necessary according to
     * <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>.
     *
     * @param value          The value which will be escaped according to
     *                       <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>
     * @param trimWhiteSpace The value will first be trimmed of its optional white-space characters,
     *                       according to <a href="https://tools.ietf.org/html/rfc7230#section-7">RFC-7230</a>
     * @return {@link CharSequence} the escaped value if necessary, or the value unchanged
     */
    public static CharSequence escapeCsv(CharSequence value, boolean trimWhiteSpace) {
        int length = checkNotNull(value, "value").length();
        int start;
        int last;
        if (trimWhiteSpace) {
            start = indexOfFirstNonOwsChar(value, length);
            last = indexOfLastNonOwsChar(value, start, length);
        } else {
            start = 0;
            last = length - 1;
        }
        if (start > last) {
            return EMPTY_STRING;
        }

        int firstUnescapedSpecial = -1;
        boolean quoted = false;
        if (isDoubleQuote(value.charAt(start))) {
            quoted = isDoubleQuote(value.charAt(last)) && last > start;
            if (quoted) {
                start++;
                last--;
            } else {
                firstUnescapedSpecial = start;
            }
        }

        if (firstUnescapedSpecial < 0) {
            if (quoted) {
                for (int i = start; i <= last; i++) {
                    if (isDoubleQuote(value.charAt(i))) {
                        if (i == last || !isDoubleQuote(value.charAt(i + 1))) {
                            firstUnescapedSpecial = i;
                            break;
                        }
                        i++;
                    }
                }
            } else {
                for (int i = start; i <= last; i++) {
                    char c = value.charAt(i);
                    if (c == LINE_FEED || c == CARRIAGE_RETURN || c == COMMA) {
                        firstUnescapedSpecial = i;
                        break;
                    }
                    if (isDoubleQuote(c)) {
                        if (i == last || !isDoubleQuote(value.charAt(i + 1))) {
                            firstUnescapedSpecial = i;
                            break;
                        }
                        i++;
                    }
                }
            }

            if (firstUnescapedSpecial < 0) {
                // 无特殊字符或已全部转义：尽量复用原子串以减少 GC。
                // 多数情况直接返回原 CharSequence 子序列；
                // 仅在必须时才通过 StringBuilder 构造新串。
                return quoted? value.subSequence(start - 1, last + 2) : value.subSequence(start, last + 1);
            }
        }

        StringBuilder result = new StringBuilder(last - start + 1 + CSV_NUMBER_ESCAPE_CHARACTERS);
        result.append(DOUBLE_QUOTE).append(value, start, firstUnescapedSpecial);
        for (int i = firstUnescapedSpecial; i <= last; i++) {
            char c = value.charAt(i);
            if (isDoubleQuote(c)) {
                result.append(DOUBLE_QUOTE);
                if (i < last && isDoubleQuote(value.charAt(i + 1))) {
                    i++;
                }
            }
            result.append(c);
        }
        return result.append(DOUBLE_QUOTE);
    }

    /**
     * Unescapes the specified escaped CSV field, if necessary according to
     * <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>.
     *
     * @param value The escaped CSV field which will be unescaped according to
     *              <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>
     * @return {@link CharSequence} the unescaped value if necessary, or the value unchanged
     
 *
 * <p>按 RFC-4180 反转义 CSV 字段。</p>
 */
    public static CharSequence unescapeCsv(CharSequence value) {
        int length = checkNotNull(value, "value").length();
        if (length == 0) {
            return value;
        }
        int last = length - 1;
        boolean quoted = isDoubleQuote(value.charAt(0)) && isDoubleQuote(value.charAt(last)) && length != 1;
        if (!quoted) {
            validateCsvFormat(value);
            return value;
        }
        StringBuilder unescaped = InternalThreadLocalMap.get().stringBuilder();
        for (int i = 1; i < last; i++) {
            char current = value.charAt(i);
            if (current == DOUBLE_QUOTE) {
                if (isDoubleQuote(value.charAt(i + 1)) && (i + 1) != last) {
                    // 连续双引号表示转义的一个引号
                    // 跳过配对的第二个引号
                    i++;
                } else {
                    // 孤立引号或末尾未配对引号：格式非法
                    throw newInvalidEscapedCsvFieldException(value, i);
                }
            }
            unescaped.append(current);
        }
        return unescaped.toString();
    }

    /**
     * Unescapes the specified escaped CSV fields according to
     * <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>.
     *
     * @param value A string with multiple CSV escaped fields which will be unescaped according to
     *              <a href="https://tools.ietf.org/html/rfc4180#section-2">RFC-4180</a>
     * @return {@link List} the list of unescaped fields
     
 *
 * <p>按 RFC-4180 反转义含多个字段的 CSV 字符串，返回字段列表。</p>
 */
    public static List<CharSequence> unescapeCsvFields(CharSequence value) {
        List<CharSequence> unescaped = new ArrayList<CharSequence>(2);
        StringBuilder current = InternalThreadLocalMap.get().stringBuilder();
        boolean quoted = false;
        int last = value.length() - 1;
        for (int i = 0; i <= last; i++) {
            char c = value.charAt(i);
            if (quoted) {
                switch (c) {
                    case DOUBLE_QUOTE:
                        if (i == last) {
                            // 最后一个引号字段：追加并返回
                            unescaped.add(current.toString());
                            return unescaped;
                        }
                        char next = value.charAt(++i);
                        if (next == DOUBLE_QUOTE) {
                            // 连续两个双引号解码为一个引号
                            current.append(DOUBLE_QUOTE);
                            break;
                        }
                        if (next == COMMA) {
                            // 字段结束，开始解析下一字段
                            quoted = false;
                            unescaped.add(current.toString());
                            current.setLength(0);
                            break;
                        }
                        // 引号后非逗号/引号：非法
                        throw newInvalidEscapedCsvFieldException(value, i - 1);
                    default:
                        current.append(c);
                }
            } else {
                switch (c) {
                    case COMMA:
                        // 逗号分隔：提交当前字段
                        unescaped.add(current.toString());
                        current.setLength(0);
                        break;
                    case DOUBLE_QUOTE:
                        if (current.length() == 0) {
                            quoted = true;
                            break;
                        }
                        // 未用引号包裹却出现双引号
                        // 继续落入下一 case
                    case LINE_FEED:
                        // 继续落入下一 case
                    case CARRIAGE_RETURN:
                        // 换行/回车/逗号等特殊字符未加引号包裹
                        throw newInvalidEscapedCsvFieldException(value, i);
                    default:
                        current.append(c);
                }
            }
        }
        if (quoted) {
            throw newInvalidEscapedCsvFieldException(value, last);
        }
        unescaped.add(current.toString());
        return unescaped;
    }

    /**
     * Validate if {@code value} is a valid csv field without double-quotes.
     *
     * @throws IllegalArgumentException if {@code value} needs to be encoded with double-quotes.
     
 *
 * <p>校验未加引号的 CSV 字段格式，含特殊字符则抛异常。</p>
 */
    private static void validateCsvFormat(CharSequence value) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
            switch (value.charAt(i)) {
                case DOUBLE_QUOTE:
                case LINE_FEED:
                case CARRIAGE_RETURN:
                case COMMA:
                    // 含特殊字符的 CSV 字段必须用双引号包裹
                    throw newInvalidEscapedCsvFieldException(value, i);
                default:
            }
        }
    }

    private static IllegalArgumentException newInvalidEscapedCsvFieldException(CharSequence value, int index) {
        return new IllegalArgumentException("invalid escaped CSV field: " + value + " index: " + index);
    }

    /**
     * Get the length of a string, {@code null} input is considered {@code 0} length.
     
 *
 * <p>字符串长度，null 视为 0。</p>
 */
    public static int length(String s) {
        return s == null ? 0 : s.length();
    }

    /**
     * Determine if a string is {@code null} or {@link String#isEmpty()} returns {@code true}.
     
 *
 * <p>判断字符串是否为 null 或 empty。</p>
 */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Find the index of the first non-white space character in {@code s} starting at {@code offset}.
     *
     * @param seq    The string to search.
     * @param offset The offset to start searching at.
     * @return the index of the first non-white space character or &lt;{@code -1} if none was found.
     
 *
 * <p>从 offset 起找首个非空白字符索引，无则 -1。</p>
 */
    public static int indexOfNonWhiteSpace(CharSequence seq, int offset) {
        for (; offset < seq.length(); ++offset) {
            if (!Character.isWhitespace(seq.charAt(offset))) {
                return offset;
            }
        }
        return -1;
    }

    /**
     * Find the index of the first white space character in {@code s} starting at {@code offset}.
     *
     * @param seq    The string to search.
     * @param offset The offset to start searching at.
     * @return the index of the first white space character or &lt;{@code -1} if none was found.
     
 *
 * <p>从 offset 起找首个空白字符索引，无则 -1。</p>
 */
    public static int indexOfWhiteSpace(CharSequence seq, int offset) {
        for (; offset < seq.length(); ++offset) {
            if (Character.isWhitespace(seq.charAt(offset))) {
                return offset;
            }
        }
        return -1;
    }

    /**
     * Determine if {@code c} lies within the range of values defined for
     * <a href="https://unicode.org/glossary/#surrogate_code_point">Surrogate Code Point</a>.
     *
     * @param c the character to check.
     * @return {@code true} if {@code c} lies within the range of values defined for
     * <a href="https://unicode.org/glossary/#surrogate_code_point">Surrogate Code Point</a>. {@code false} otherwise.
     
 *
 * <p>判断字符是否为 Unicode 代理码点（surrogate）。</p>
 */
    public static boolean isSurrogate(char c) {
        return c >= '\uD800' && c <= '\uDFFF';
    }

    private static boolean isDoubleQuote(char c) {
        return c == DOUBLE_QUOTE;
    }

    /**
     * Determine if the string {@code s} ends with the char {@code c}.
     *
     * @param s the string to test
     * @param c the tested char
     * @return true if {@code s} ends with the char {@code c}
     
 *
 * <p>判断字符串是否以指定字符结尾。</p>
 */
    public static boolean endsWith(CharSequence s, char c) {
        int len = s.length();
        return len > 0 && s.charAt(len - 1) == c;
    }

    /**
     * Trim optional white-space characters from the specified value,
     * according to <a href="https://tools.ietf.org/html/rfc7230#section-7">RFC-7230</a>.
     *
     * @param value the value to trim
     * @return {@link CharSequence} the trimmed value if necessary, or the value unchanged
     
 *
 * <p>按 RFC-7230 修剪可选空白（OWS：空格与制表符）。</p>
 */
    public static CharSequence trimOws(CharSequence value) {
        final int length = value.length();
        if (length == 0) {
            return value;
        }
        int start = indexOfFirstNonOwsChar(value, length);
        int end = indexOfLastNonOwsChar(value, start, length);
        return start == 0 && end == length - 1 ? value : value.subSequence(start, end + 1);
    }

    /**
     * Returns a char sequence that contains all {@code elements} joined by a given separator.
     *
     * @param separator for each element
     * @param elements to join together
     *
     * @return a char sequence joined by a given separator.
     
 *
 * <p>用分隔符连接 Iterable 中的 CharSequence。</p>
 */
    public static CharSequence join(CharSequence separator, Iterable<? extends CharSequence> elements) {
        ObjectUtil.checkNotNull(separator, "separator");
        ObjectUtil.checkNotNull(elements, "elements");

        Iterator<? extends CharSequence> iterator = elements.iterator();
        if (!iterator.hasNext()) {
            return EMPTY_STRING;
        }

        CharSequence firstElement = iterator.next();
        if (!iterator.hasNext()) {
            return firstElement;
        }

        StringBuilder builder = new StringBuilder(firstElement);
        do {
            builder.append(separator).append(iterator.next());
        } while (iterator.hasNext());

        return builder;
    }

    /**
     * @return {@code length} if no OWS is found.
     
 *
 * <p>返回首个非 OWS 字符索引，无 OWS 则返回 length。</p>
 */
    private static int indexOfFirstNonOwsChar(CharSequence value, int length) {
        int i = 0;
        while (i < length && isOws(value.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * @return {@code start} if no OWS is found.
     
 *
 * <p>返回最后一个非 OWS 字符索引，无 OWS 则返回 start。</p>
 */
    private static int indexOfLastNonOwsChar(CharSequence value, int start, int length) {
        int i = length - 1;
        while (i > start && isOws(value.charAt(i))) {
            i--;
        }
        return i;
    }

    private static boolean isOws(char c) {
        return c == SPACE || c == TAB;
    }

}
