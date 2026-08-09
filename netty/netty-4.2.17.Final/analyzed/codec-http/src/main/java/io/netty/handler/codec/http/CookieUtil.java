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
package io.netty.handler.codec.http;

import java.util.BitSet;

/**
 * Cookie 名称/值合法性校验与引号unwrap工具（已废弃）。
 * <p>
 * 与 {@link io.netty.handler.codec.http.cookie.CookieUtil} 重复，仅供旧版 {@link CookieDecoder} 使用。
 * @deprecated Duplicate of package private ${@link io.netty.handler.codec.http.cookie.CookieUtil}
 */
@Deprecated
final class CookieUtil {

    private static final BitSet VALID_COOKIE_VALUE_OCTETS = validCookieValueOctets();

    private static final BitSet VALID_COOKIE_NAME_OCTETS = validCookieNameOctets(VALID_COOKIE_VALUE_OCTETS);

    // 合法 Cookie 值：US-ASCII 可打印字符，排除引号、逗号、分号、反斜杠
    private static BitSet validCookieValueOctets() {
        BitSet bits = new BitSet(8);
        for (int i = 35; i < 127; i++) {
            // US-ASCII 可打印字符，排除控制字符
            bits.set(i);
        }
        bits.set('"', false);  // 排除双引号
        bits.set(',', false);  // 排除逗号
        bits.set(';', false);  // 排除分号
        bits.set('\\', false); // 排除反斜杠
        return bits;
    }

    // token 规则：不含 CTL 与分隔符的 CHAR 序列
    private static BitSet validCookieNameOctets(BitSet validCookieValueOctets) {
        BitSet bits = new BitSet(8);
        bits.or(validCookieValueOctets);
        bits.set('(', false);
        bits.set(')', false);
        bits.set('<', false);
        bits.set('>', false);
        bits.set('@', false);
        bits.set(':', false);
        bits.set('/', false);
        bits.set('[', false);
        bits.set(']', false);
        bits.set('?', false);
        bits.set('=', false);
        bits.set('{', false);
        bits.set('}', false);
        bits.set(' ', false);
        bits.set('\t', false);
        return bits;
    }

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

    static CharSequence unwrapValue(CharSequence cs) {
        final int len = cs.length();
        if (len > 0 && cs.charAt(0) == '"') {
            if (len >= 2 && cs.charAt(len - 1) == '"') {
                // 引号成对匹配
                return len == 2 ? "" : cs.subSequence(1, len - 1);
            } else {
                return null;
            }
        }
        return cs;
    }

    private CookieUtil() {
        // 工具类禁止实例化
    }
}
