/*
 * Copyright 2015 The Netty Project
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
package io.netty.handler.codec.http.cookie;

import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.BitSet;

/**
 * Cookie 编解码内部工具类：字符集校验、属性拼接与引号值处理。
 * <p>供 {@link ServerCookieEncoder}、{@link ClientCookieEncoder} 等包内类共用。
 */
final class CookieUtil {

    private static final BitSet VALID_COOKIE_NAME_OCTETS = validCookieNameOctets();

    private static final BitSet VALID_COOKIE_VALUE_OCTETS = validCookieValueOctets();

    private static final BitSet VALID_COOKIE_ATTRIBUTE_VALUE_OCTETS = validCookieAttributeValueOctets();

    /** 构建 RFC6265 允许的 cookie 名称字符位图（排除分隔符） */
    // token = 1*<any CHAR except CTLs or separators>
    // separators = "(" | ")" | "<" | ">" | "@"
    // | "," | ";" | ":" | "\" | <">
    // | "/" | "[" | "]" | "?" | "="
    // | "{" | "}" | SP | HT
    private static BitSet validCookieNameOctets() {
        BitSet bits = new BitSet();
        for (int i = 32; i < 127; i++) {
            bits.set(i);
        }
        int[] separators = new int[]
                { '(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '/', '[', ']', '?', '=', '{', '}', ' ', '\t' };
        for (int separator : separators) {
            bits.set(separator, false);
        }
        return bits;
    }

    /** 构建 cookie 值允许的 octet 位图 */
    // cookie-octet = %x21 / %x23-2B / %x2D-3A / %x3C-5B / %x5D-7E
    // US-ASCII characters excluding CTLs, whitespace, DQUOTE, comma, semicolon, and backslash
    private static BitSet validCookieValueOctets() {
        BitSet bits = new BitSet();
        bits.set(0x21);
        for (int i = 0x23; i <= 0x2B; i++) {
            bits.set(i);
        }
        for (int i = 0x2D; i <= 0x3A; i++) {
            bits.set(i);
        }
        for (int i = 0x3C; i <= 0x5B; i++) {
            bits.set(i);
        }
        for (int i = 0x5D; i <= 0x7E; i++) {
            bits.set(i);
        }
        return bits;
    }

    /** 构建 Path/Domain 等属性值允许的字符位图（不含分号） */
    // path-value        = <any CHAR except CTLs or ";">
    private static BitSet validCookieAttributeValueOctets() {
        BitSet bits = new BitSet();
        for (int i = 32; i < 127; i++) {
            bits.set(i);
        }
        bits.set(';', false);
        return bits;
    }

    /** 从线程本地缓存获取可复用的 {@link StringBuilder} */
    static StringBuilder stringBuilder() {
        return InternalThreadLocalMap.get().stringBuilder();
    }

    /**
     * 若缓冲区为空则返回 {@code null}，否则去掉末尾 {@code "; "} 分隔符。
     * @param buf a buffer where some cookies were maybe encoded
     * @return the buffer String without the trailing separator, or null if no cookie was appended.
     */
    static String stripTrailingSeparatorOrNull(StringBuilder buf) {
        return buf.length() == 0 ? null : stripTrailingSeparator(buf);
    }

    /** 移除 {@link StringBuilder} 末尾的分号与空格分隔符 */
    static String stripTrailingSeparator(StringBuilder buf) {
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 2);
        }
        return buf.toString();
    }

    /** 追加 {@code name=val; } 形式的属性 */
    static void add(StringBuilder sb, String name, long val) {
        sb.append(name);
        sb.append('=');
        sb.append(val);
        sb.append(';');
        sb.append(HttpConstants.SP_CHAR);
    }

    static void add(StringBuilder sb, String name, String val) {
        sb.append(name);
        sb.append('=');
        sb.append(val);
        sb.append(';');
        sb.append(HttpConstants.SP_CHAR);
    }

    static void add(StringBuilder sb, String name) {
        sb.append(name);
        sb.append(';');
        sb.append(HttpConstants.SP_CHAR);
    }

    /** 追加带双引号的 {@code name="val"; } 属性 */
    static void addQuoted(StringBuilder sb, String name, String val) {
        if (val == null) {
            val = "";
        }

        sb.append(name);
        sb.append('=');
        sb.append('"');
        sb.append(val);
        sb.append('"');
        sb.append(';');
        sb.append(HttpConstants.SP_CHAR);
    }

    /** 返回 cookie 名称中首个非法字符下标，合法则 {@code -1} */
    static int firstInvalidCookieNameOctet(CharSequence cs) {
        return firstInvalidOctet(cs, VALID_COOKIE_NAME_OCTETS);
    }

    static int firstInvalidCookieValueOctet(CharSequence cs) {
        return firstInvalidOctet(cs, VALID_COOKIE_VALUE_OCTETS);
    }

    static int firstInvalidOctet(CharSequence cs, BitSet bits) {
        for (int i = 0; i < cs.length(); i++) {
            char c = cs.charAt(i);
            if (!bits.get(c)) {
                return i;
            }
        }
        return -1;
    }

    /** 若值为成对双引号包裹则去引号，否则原样或 {@code null}（引号不匹配） */
    static CharSequence unwrapValue(CharSequence cs) {
        final int len = cs.length();
        if (len > 0 && cs.charAt(0) == '"') {
            if (len >= 2 && cs.charAt(len - 1) == '"') {
                // properly balanced
                return len == 2 ? "" : cs.subSequence(1, len - 1);
            } else {
                return null;
            }
        }
        return cs;
    }

    /** 校验并 trim 属性值，非法字符抛出 {@link IllegalArgumentException} */
    static String validateAttributeValue(String name, String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }
        int i = firstInvalidOctet(value, VALID_COOKIE_ATTRIBUTE_VALUE_OCTETS);
        if (i != -1) {
            throw new IllegalArgumentException(name + " contains the prohibited characters: " + value.charAt(i));
        }
        return value;
    }

    private CookieUtil() {
        // Unused
    }
}
