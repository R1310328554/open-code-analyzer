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

import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.cookie.CookieHeaderNames.SameSite;

import java.util.Date;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 客户端 RFC6265 Cookie 解码器，用于解析 Set-Cookie 响应头。
 * <p>
 * 通过 {@link Cookie#setWrap(boolean)} 记录原始值是否带引号，
 * 以便原样回传 Origin 服务器。
 *
 * @see ClientCookieEncoder
 */
public final class ClientCookieDecoder extends CookieDecoder {

    /** 严格模式：校验 name/value 符合 RFC6265 字符范围。 */

    public static final ClientCookieDecoder STRICT = new ClientCookieDecoder(true);

    /** 宽松模式：不校验 name/value 字符。 */

    public static final ClientCookieDecoder LAX = new ClientCookieDecoder(false);

    private ClientCookieDecoder(boolean strict) {
        super(strict);
    }

    /**
     * 将 Set-Cookie 头值解码为 {@link Cookie}；空串返回 {@code null}。
     * @return the decoded {@link Cookie}
     */
    public Cookie decode(String header) {
        final int headerLen = checkNotNull(header, "header").length();

        if (headerLen == 0) {
            return null;
        }

        CookieBuilder cookieBuilder = null;

        loop: for (int i = 0;;) {

            // 跳过空白与分隔符
            for (;;) {
                if (i == headerLen) {
                    break loop;
                }
                char c = header.charAt(i);
                if (c == ',') {
                    // 单头多 Cookie 已废弃，现代浏览器只解析第一个
                    break loop;

                } else if (c == '\t' || c == '\n' || c == 0x0b || c == '\f'
                        || c == '\r' || c == ' ' || c == ';') {
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
                    // NAME;（无值，直到分号）
                    nameEnd = i;
                    valueBegin = valueEnd = -1;
                    break;

                } else if (curChar == '=') {
                    // NAME=VALUE 形式
                    nameEnd = i;
                    i++;
                    if (i == headerLen) {
                        // NAME=（等号后无内容，空值）
                        valueBegin = valueEnd = 0;
                        break;
                    }

                    valueBegin = i;
                    // NAME=VALUE; 到下一分号或串尾
                    int semiPos = header.indexOf(';', i);
                    valueEnd = i = semiPos > 0 ? semiPos : headerLen;
                    break;
                } else {
                    i++;
                }

                if (i == headerLen) {
                    // NAME（无等号，到串尾）
                    nameEnd = headerLen;
                    valueBegin = valueEnd = -1;
                    break;
                }
            }

            if (valueEnd > 0 && header.charAt(valueEnd - 1) == ',') {
                // 旧式多 Cookie 逗号分隔符，跳过
                valueEnd--;
            }

            if (cookieBuilder == null) {
                // 首个 name-value 对 → 创建 Cookie
                DefaultCookie cookie = initCookie(header, nameBegin, nameEnd, valueBegin, valueEnd);

                if (cookie == null) {
                    return null;
                }

                cookieBuilder = new CookieBuilder(cookie, header);
            } else {
                // 后续键值 → Cookie 属性
                cookieBuilder.appendAttribute(nameBegin, nameEnd, valueBegin, valueEnd);
            }
        }
        return cookieBuilder != null ? cookieBuilder.cookie() : null;
    }

    private static class CookieBuilder {

        private final String header;
        private final DefaultCookie cookie;
        private String domain;
        private String path;
        private long maxAge = Long.MIN_VALUE;
        private int expiresStart;
        private int expiresEnd;
        private boolean secure;
        private boolean httpOnly;
        private SameSite sameSite;
        private boolean partitioned;

        CookieBuilder(DefaultCookie cookie, String header) {
            this.cookie = cookie;
            this.header = header;
        }

        private long mergeMaxAgeAndExpires() {
            // Max-Age 优先于 Expires
            if (maxAge != Long.MIN_VALUE) {
                return maxAge;
            } else if (isValueDefined(expiresStart, expiresEnd)) {
                Date expiresDate = DateFormatter.parseHttpDate(header, expiresStart, expiresEnd);
                if (expiresDate != null) {
                    long maxAgeMillis = expiresDate.getTime() - System.currentTimeMillis();
                    return maxAgeMillis / 1000 + (maxAgeMillis % 1000 != 0 ? 1 : 0);
                }
            }
            return Long.MIN_VALUE;
        }

        Cookie cookie() {
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setMaxAge(mergeMaxAgeAndExpires());
            cookie.setSecure(secure);
            cookie.setHttpOnly(httpOnly);
            cookie.setSameSite(sameSite);
            cookie.setPartitioned(partitioned);
            return cookie;
        }

        /**
         * 解析并存储键值对；首对为 name/value，未知属性名静默忽略。
         *
         * @param keyStart
         *            where the key starts in the header
         * @param keyEnd
         *            where the key ends in the header
         * @param valueStart
         *            where the value starts in the header
         * @param valueEnd
         *            where the value ends in the header
         */
        void appendAttribute(int keyStart, int keyEnd, int valueStart, int valueEnd) {
            int length = keyEnd - keyStart;

            if (length == 4) {
                parse4(keyStart, valueStart, valueEnd);
            } else if (length == 6) {
                parse6(keyStart, valueStart, valueEnd);
            } else if (length == 7) {
                parse7(keyStart, valueStart, valueEnd);
            } else if (length == 8) {
                parse8(keyStart, valueStart, valueEnd);
            } else if (length == 11) {
                parse11(keyStart);
            }
        }

        private void parse4(int nameStart, int valueStart, int valueEnd) {
            if (header.regionMatches(true, nameStart, CookieHeaderNames.PATH, 0, 4)) {
                path = computeValue(valueStart, valueEnd);
            }
        }

        private void parse6(int nameStart, int valueStart, int valueEnd) {
            if (header.regionMatches(true, nameStart, CookieHeaderNames.DOMAIN, 0, 5)) {
                domain = computeValue(valueStart, valueEnd);
            } else if (header.regionMatches(true, nameStart, CookieHeaderNames.SECURE, 0, 5)) {
                secure = true;
            }
        }

        private void setMaxAge(String value) {
            try {
                maxAge = Math.max(Long.parseLong(value), 0L);
            } catch (NumberFormatException e1) {
                // 解析失败视为会话 Cookie
            }
        }

        private void parse7(int nameStart, int valueStart, int valueEnd) {
            if (header.regionMatches(true, nameStart, CookieHeaderNames.EXPIRES, 0, 7)) {
                expiresStart = valueStart;
                expiresEnd = valueEnd;
            } else if (header.regionMatches(true, nameStart, CookieHeaderNames.MAX_AGE, 0, 7)) {
                setMaxAge(computeValue(valueStart, valueEnd));
            }
        }

        private void parse8(int nameStart, int valueStart, int valueEnd) {
            if (header.regionMatches(true, nameStart, CookieHeaderNames.HTTPONLY, 0, 8)) {
                httpOnly = true;
            } else if (header.regionMatches(true, nameStart, CookieHeaderNames.SAMESITE, 0, 8)) {
                sameSite = SameSite.of(computeValue(valueStart, valueEnd));
            }
        }

        private void parse11(int nameStart) {
            if (header.regionMatches(true, nameStart, CookieHeaderNames.PARTITIONED, 0, 11)) {
                partitioned = true;
            }
        }

        private static boolean isValueDefined(int valueStart, int valueEnd) {
            return valueStart != -1 && valueStart != valueEnd;
        }

        private String computeValue(int valueStart, int valueEnd) {
            return isValueDefined(valueStart, valueEnd) ? header.substring(valueStart, valueEnd) : null;
        }
    }
}
