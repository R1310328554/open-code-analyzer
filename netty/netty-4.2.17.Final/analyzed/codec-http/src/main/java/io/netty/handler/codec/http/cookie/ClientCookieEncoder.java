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

import static io.netty.handler.codec.http.cookie.CookieUtil.add;
import static io.netty.handler.codec.http.cookie.CookieUtil.addQuoted;
import static io.netty.handler.codec.http.cookie.CookieUtil.stringBuilder;
import static io.netty.handler.codec.http.cookie.CookieUtil.stripTrailingSeparator;
import static io.netty.handler.codec.http.cookie.CookieUtil.stripTrailingSeparatorOrNull;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 客户端 RFC6265 Cookie 编码器，仅发送 name=value 对到 Cookie 请求头。
 * <p>
 * 多个 Cookie 合并为单条 Cookie 头，以 {@code ; } 分隔。
 *
 * @see ClientCookieDecoder
 */
public final class ClientCookieEncoder extends CookieEncoder {

    /** 严格模式：校验字符并按 RFC6265 按 path 长度降序排序。 */

    public static final ClientCookieEncoder STRICT = new ClientCookieEncoder(true);

    /** 宽松模式：不校验，多 Cookie 保持传入顺序。 */

    public static final ClientCookieEncoder LAX = new ClientCookieEncoder(false);

    private ClientCookieEncoder(boolean strict) {
        super(strict);
    }

    /**
     * 将 name-value 编码为 Cookie 头值。
     * @param name
     *            the cookie name
     * @param value
     *            the cookie value
     * @return a Rfc6265 style Cookie header value
     */
    public String encode(String name, String value) {
        return encode(new DefaultCookie(name, value));
    }

    /**
     * 将 {@link Cookie} 编码为 Cookie 头值。
     * @param cookie the specified cookie
     * @return a Rfc6265 style Cookie header value
     */
    public String encode(Cookie cookie) {
        StringBuilder buf = stringBuilder();
        encode(buf, checkNotNull(cookie, "cookie"));
        return stripTrailingSeparator(buf);
    }

    /**
     * 按 path 长度降序排序（RFC 6265）；path 未指定视为最长。
     */
    // package-private for testing only.
    static final Comparator<Cookie> COOKIE_COMPARATOR = new Comparator<Cookie>() {
        @Override
        public int compare(Cookie c1, Cookie c2) {
            String path1 = c1.path();
            String path2 = c2.path();
            // path 未指定时视为 Integer.MAX_VALUE（优先于有 path 的 Cookie）
            int len1 = path1 == null ? Integer.MAX_VALUE : path1.length();
            int len2 = path2 == null ? Integer.MAX_VALUE : path2.length();

            // 稳定排序保留同 path 长度下的创建顺序
            return len2 - len1;
        }
    };

    /**
     * 将多个 Cookie 编码为单条 Cookie 头值；无 Cookie 时返回 {@code null}。
     * @param cookies
     *            some cookies
     * @return a Rfc6265 style Cookie header value, null if no cookies are passed.
     */
    public String encode(Cookie... cookies) {
        if (checkNotNull(cookies, "cookies").length == 0) {
            return null;
        }

        StringBuilder buf = stringBuilder();
        if (strict) {
            if (cookies.length == 1) {
                encode(buf, cookies[0]);
            } else {
                Cookie[] cookiesSorted = Arrays.copyOf(cookies, cookies.length);
                Arrays.sort(cookiesSorted, COOKIE_COMPARATOR);
                for (Cookie c : cookiesSorted) {
                    encode(buf, c);
                }
            }
        } else {
            for (Cookie c : cookies) {
                encode(buf, c);
            }
        }
        return stripTrailingSeparatorOrNull(buf);
    }

    /**
     * 将 Collection 中 Cookie 编码为单条 Cookie 头值。
     * @param cookies
     *            some cookies
     * @return a Rfc6265 style Cookie header value, null if no cookies are passed.
     */
    public String encode(Collection<? extends Cookie> cookies) {
        if (checkNotNull(cookies, "cookies").isEmpty()) {
            return null;
        }

        StringBuilder buf = stringBuilder();
        if (strict) {
            if (cookies.size() == 1) {
                encode(buf, cookies.iterator().next());
            } else {
                Cookie[] cookiesSorted = cookies.toArray(new Cookie[0]);
                Arrays.sort(cookiesSorted, COOKIE_COMPARATOR);
                for (Cookie c : cookiesSorted) {
                    encode(buf, c);
                }
            }
        } else {
            for (Cookie c : cookies) {
                encode(buf, c);
            }
        }
        return stripTrailingSeparatorOrNull(buf);
    }

    /**
     * 将 Iterable 中 Cookie 编码为单条 Cookie 头值。
     * @param cookies some cookies
     * @return a Rfc6265 style Cookie header value, null if no cookies are passed.
     */
    public String encode(Iterable<? extends Cookie> cookies) {
        Iterator<? extends Cookie> cookiesIt = checkNotNull(cookies, "cookies").iterator();
        if (!cookiesIt.hasNext()) {
            return null;
        }

        StringBuilder buf = stringBuilder();
        if (strict) {
            Cookie firstCookie = cookiesIt.next();
            if (!cookiesIt.hasNext()) {
                encode(buf, firstCookie);
            } else {
                List<Cookie> cookiesList = InternalThreadLocalMap.get().arrayList();
                cookiesList.add(firstCookie);
                while (cookiesIt.hasNext()) {
                    cookiesList.add(cookiesIt.next());
                }
                Cookie[] cookiesSorted = cookiesList.toArray(new Cookie[0]);
                Arrays.sort(cookiesSorted, COOKIE_COMPARATOR);
                for (Cookie c : cookiesSorted) {
                    encode(buf, c);
                }
            }
        } else {
            while (cookiesIt.hasNext()) {
                encode(buf, cookiesIt.next());
            }
        }
        return stripTrailingSeparatorOrNull(buf);
    }

    private void encode(StringBuilder buf, Cookie c) {
        final String name = c.name();
        final String value = c.value() != null ? c.value() : "";

        validateCookie(name, value);

        if (c.wrap()) {
            addQuoted(buf, name, value);
        } else {
            add(buf, name, value);
        }
    }
}
