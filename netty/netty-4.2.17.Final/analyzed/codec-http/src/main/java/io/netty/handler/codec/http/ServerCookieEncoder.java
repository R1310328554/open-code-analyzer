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
package io.netty.handler.codec.http;

import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collection;
import java.util.List;

/**
 * 服务端 RFC6265 Cookie 编码器（已废弃）。
 * <p>
 * 委托 {@link io.netty.handler.codec.http.cookie.ServerCookieEncoder#LAX}；
 * Expires/MaxAge 合并后仅发送 Max-Age；多个 Cookie 须分多条 Set-Cookie 头发送。
 *
 * @see ServerCookieDecoder
 *
 * @deprecated Use {@link io.netty.handler.codec.http.cookie.ServerCookieEncoder} instead
 */
@Deprecated
public final class ServerCookieEncoder {

    /**
     * 将 name-value 编码为单条 Set-Cookie 头值。
     * @param name the cookie name
     * @param value the cookie value
     * @return a single Set-Cookie header value
     */
    @Deprecated
    public static String encode(String name, String value) {
        return io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(name, value);
    }

    /**
     * 将 {@link Cookie} 编码为单条 Set-Cookie 头值。
     * @param cookie the cookie
     * @return a single Set-Cookie header value
     */
    @Deprecated
    public static String encode(Cookie cookie) {
        return io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(cookie);
    }

    /**
     * 批量编码为多条 Set-Cookie 头值。
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    @Deprecated
    public static List<String> encode(Cookie... cookies) {
        return io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(cookies);
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    @Deprecated
    public static List<String> encode(Collection<Cookie> cookies) {
        return io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(cookies);
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    @Deprecated
    public static List<String> encode(Iterable<Cookie> cookies) {
        return io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(cookies);
    }

    /** 工具类禁止实例化 */
    private ServerCookieEncoder() {
        // Unused
    }
}
