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
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.cookie.CookieUtil.add;
import static io.netty.handler.codec.http.cookie.CookieUtil.addQuoted;
import static io.netty.handler.codec.http.cookie.CookieUtil.stringBuilder;
import static io.netty.handler.codec.http.cookie.CookieUtil.stripTrailingSeparator;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 服务端 RFC6265 Set-Cookie 编码器，将 {@link Cookie} 序列化为响应头值。
 * <p>Netty 将 Expires 与 Max-Age 合并为单一字段，编码时仅输出 Max-Age；多个 cookie 须分多条 Set-Cookie 头发送。
 *
 * <pre>
 * // Example
 * {@link HttpResponse} res = ...;
 * res.setHeader("Set-Cookie", {@link ServerCookieEncoder}.encode("JSESSIONID", "1234"));
 * </pre>
 *
 * @see ServerCookieDecoder
 */
public final class ServerCookieEncoder extends CookieEncoder {

    /**
     * 严格模式：校验字符集，批量编码时同名 cookie 仅保留最后一个。
     */
    public static final ServerCookieEncoder STRICT = new ServerCookieEncoder(true);

    /**
     * 宽松模式：不校验字符，允许输出多个同名 cookie。
     */
    public static final ServerCookieEncoder LAX = new ServerCookieEncoder(false);

    private ServerCookieEncoder(boolean strict) {
        super(strict);
    }

    /**
     * 将 name/value 编码为单条 Set-Cookie 头值。
     *
     * @param name the cookie name
     * @param value the cookie value
     * @return a single Set-Cookie header value
     */
    public String encode(String name, String value) {
        return encode(new DefaultCookie(name, value));
    }

    /**
     * 编码完整 {@link Cookie}（含 Path、Domain、Secure、SameSite 等属性）。
     *
     * @param cookie the cookie
     * @return a single Set-Cookie header value
     */
    public String encode(Cookie cookie) {
        final String name = checkNotNull(cookie, "cookie").name();
        final String value = cookie.value() != null ? cookie.value() : "";

        validateCookie(name, value);

        StringBuilder buf = stringBuilder();

        if (cookie.wrap()) {
            addQuoted(buf, name, value);
        } else {
            add(buf, name, value);
        }

        if (cookie.maxAge() != Long.MIN_VALUE) {
            add(buf, CookieHeaderNames.MAX_AGE, cookie.maxAge());
            Date expires = new Date(cookie.maxAge() * 1000 + System.currentTimeMillis());
            buf.append(CookieHeaderNames.EXPIRES);
            buf.append('=');
            DateFormatter.append(expires, buf);
            buf.append(';');
            buf.append(HttpConstants.SP_CHAR);
        }

        if (cookie.path() != null) {
            add(buf, CookieHeaderNames.PATH, cookie.path());
        }

        if (cookie.domain() != null) {
            add(buf, CookieHeaderNames.DOMAIN, cookie.domain());
        }
        if (cookie.isSecure()) {
            add(buf, CookieHeaderNames.SECURE);
        }
        if (cookie.isHttpOnly()) {
            add(buf, CookieHeaderNames.HTTPONLY);
        }
        if (cookie instanceof DefaultCookie) {
            DefaultCookie c = (DefaultCookie) cookie;
            if (c.sameSite() != null) {
                add(buf, CookieHeaderNames.SAMESITE, c.sameSite().name());
            }
            if (c.isPartitioned()) {
                add(buf, CookieHeaderNames.PARTITIONED);
            }
        }

        return stripTrailingSeparator(buf);
    }

    /** 严格模式下按名称去重，仅保留每个 name 最后一次出现的编码结果。
     *
     * @param encoded The list of encoded cookies.
     * @param nameToLastIndex A map from cookie name to index of last cookie instance.
     * @return The encoded list with all but the last instance of a named cookie.
     */
    private static List<String> dedup(List<String> encoded, Map<String, Integer> nameToLastIndex) {
        boolean[] isLastInstance = new boolean[encoded.size()];
        for (int idx : nameToLastIndex.values()) {
            isLastInstance[idx] = true;
        }
        List<String> dedupd = new ArrayList<String>(nameToLastIndex.size());
        for (int i = 0, n = encoded.size(); i < n; i++) {
            if (isLastInstance[i]) {
                dedupd.add(encoded.get(i));
            }
        }
        return dedupd;
    }

    /**
     * 批量编码 cookie 数组为 Set-Cookie 头值列表。
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Cookie... cookies) {
        if (checkNotNull(cookies, "cookies").length == 0) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>(cookies.length);
        Map<String, Integer> nameToIndex = strict && cookies.length > 1 ? new HashMap<String, Integer>() : null;
        boolean hasDupdName = false;
        for (int i = 0; i < cookies.length; i++) {
            Cookie c = cookies[i];
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.name(), i) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Collection<? extends Cookie> cookies) {
        if (checkNotNull(cookies, "cookies").isEmpty()) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>(cookies.size());
        Map<String, Integer> nameToIndex = strict && cookies.size() > 1 ? new HashMap<String, Integer>() : null;
        int i = 0;
        boolean hasDupdName = false;
        for (Cookie c : cookies) {
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.name(), i++) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Iterable<? extends Cookie> cookies) {
        Iterator<? extends Cookie> cookiesIt = checkNotNull(cookies, "cookies").iterator();
        if (!cookiesIt.hasNext()) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>();
        Cookie firstCookie = cookiesIt.next();
        Map<String, Integer> nameToIndex = strict && cookiesIt.hasNext() ? new HashMap<String, Integer>() : null;
        int i = 0;
        encoded.add(encode(firstCookie));
        boolean hasDupdName = nameToIndex != null && nameToIndex.put(firstCookie.name(), i++) != null;
        while (cookiesIt.hasNext()) {
            Cookie c = cookiesIt.next();
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.name(), i++) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }
}
