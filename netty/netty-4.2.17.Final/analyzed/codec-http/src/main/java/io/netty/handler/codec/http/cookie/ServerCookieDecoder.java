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

import static io.netty.util.internal.ObjectUtil.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 服务端侧 RFC6265 Cookie 请求头解码器，从 {@code Cookie} 头解析 name=value 对。
 * <p>仅填充名称与值，不解析 Path/Domain 等 Set-Cookie 专有属性；兼容 RFC2965 但忽略其扩展字段。
 *
 * @see ServerCookieEncoder
 */
public final class ServerCookieDecoder extends CookieDecoder {

    private static final String RFC2965_VERSION = "$Version";

    private static final String RFC2965_PATH = "$" + CookieHeaderNames.PATH;

    private static final String RFC2965_DOMAIN = "$" + CookieHeaderNames.DOMAIN;

    private static final String RFC2965_PORT = "$Port";

    /**
     * 严格模式：校验 name/value 字符是否符合 RFC6265 允许范围。
     */
    public static final ServerCookieDecoder STRICT = new ServerCookieDecoder(true);

    /**
     * 宽松模式：不校验 name/value 字符。
     */
    public static final ServerCookieDecoder LAX = new ServerCookieDecoder(false);

    private ServerCookieDecoder(boolean strict) {
        super(strict);
    }

    /**
     * 解码 Cookie 头并返回列表，保留同名 cookie 的全部实例（与 {@link #decode(String)} 的 Set 去重不同）。
     *
     * @return the decoded {@link Cookie}
     */
    public List<Cookie> decodeAll(String header) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        decode(cookies, header);
        return Collections.unmodifiableList(cookies);
    }

    /**
     * 解码 Cookie 头为 {@link TreeSet}，同名 cookie 按 {@link Cookie#compareTo} 只保留一个。
     *
     * @return the decoded {@link Cookie}
     */
    public Set<Cookie> decode(String header) {
        Set<Cookie> cookies = new TreeSet<Cookie>();
        decode(cookies, header);
        return cookies;
    }

    /**
     * Decodes the specified {@code Cookie} HTTP header value into a {@link Cookie}.
     */
    private void decode(Collection<? super Cookie> cookies, String header) {
        final int headerLen = checkNotNull(header, "header").length();

        if (headerLen == 0) {
            return;
        }

        int i = 0;

        boolean rfc2965Style = false;
        if (header.regionMatches(true, 0, RFC2965_VERSION, 0, RFC2965_VERSION.length())) {
            // RFC2965 风格：跳过 $Version 及其值
            i = header.indexOf(';') + 1;
            rfc2965Style = true;
        }

        loop: for (;;) {

            // Skip spaces and separators.
            for (;;) {
                if (i == headerLen) {
                    break loop;
                }
                char c = header.charAt(i);
                if (c == '\t' || c == '\n' || c == 0x0b || c == '\f'
                        || c == '\r' || c == ' ' || c == ',' || c == ';') {
                    i++;
                    continue;
                }
                break;
            }

            int nameBegin = i;
            int nameEnd;
            int valueBegin;
            int valueEnd;

            for (;;) {

                char curChar = header.charAt(i);
                if (curChar == ';') {
                    // NAME; (no value till ';')
                    nameEnd = i;
                    valueBegin = valueEnd = -1;
                    break;

                } else if (curChar == '=') {
                    // NAME=VALUE
                    nameEnd = i;
                    i++;
                    if (i == headerLen) {
                        // NAME= (empty value, i.e. nothing after '=')
                        valueBegin = valueEnd = 0;
                        break;
                    }

                    valueBegin = i;
                    // NAME=VALUE;
                    int semiPos = header.indexOf(';', i);
                    valueEnd = i = semiPos > 0 ? semiPos : headerLen;
                    break;
                } else {
                    i++;
                }

                if (i == headerLen) {
                    // NAME (no value till the end of string)
                    nameEnd = headerLen;
                    valueBegin = valueEnd = -1;
                    break;
                }
            }

            if (rfc2965Style && (header.regionMatches(nameBegin, RFC2965_PATH, 0, RFC2965_PATH.length()) ||
                    header.regionMatches(nameBegin, RFC2965_DOMAIN, 0, RFC2965_DOMAIN.length()) ||
                    header.regionMatches(nameBegin, RFC2965_PORT, 0, RFC2965_PORT.length()))) {

                // 忽略 RFC2965 废弃字段 $Path/$Domain/$Port
                continue;
            }

            DefaultCookie cookie = initCookie(header, nameBegin, nameEnd, valueBegin, valueEnd);
            if (cookie != null) {
                cookies.add(cookie);
            }
        }
    }
}
