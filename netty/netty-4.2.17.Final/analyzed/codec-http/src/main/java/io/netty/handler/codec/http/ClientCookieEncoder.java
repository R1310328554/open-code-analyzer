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

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;

/**
 * 客户端侧 RFC6265 兼容 Cookie 编码器，仅发送 name=value 对。
 * <p>
 * 若存在 {@link Cookie#rawValue()} 则优先使用；否则使用未加引号的 {@link Cookie#value()}。
 * 多个 Cookie 应合并为单个 {@code Cookie} 请求头。
 *
 * <pre>
 * // 示例
 * {@link HttpRequest} req = ...;
 * res.setHeader("Cookie", {@link ClientCookieEncoder}.encode("JSESSIONID", "1234"));
 * </pre>
 *
 * @see ClientCookieDecoder
 */
@Deprecated
public final class ClientCookieEncoder {

    /**
     * 将指定 name/value 编码为 Cookie 请求头值。
     *
     * @param name the cookie name
     * @param value the cookie value
     * @return a Rfc6265 style Cookie header value
     */
    @Deprecated
    public static String encode(String name, String value) {
        return io.netty.handler.codec.http.cookie.ClientCookieEncoder.LAX.encode(name, value);
    }

    /**
     * 将单个 {@link Cookie} 编码为 Cookie 请求头值。
     *
     * @param cookie the specified cookie
     * @return a Rfc6265 style Cookie header value
     */
    @Deprecated
    public static String encode(Cookie cookie) {
        return io.netty.handler.codec.http.cookie.ClientCookieEncoder.LAX.encode(cookie);
    }

    /**
     * 将多个 Cookie 编码为单个 Cookie 请求头值。
     *
     * @param cookies some cookies
     * @return a Rfc6265 style Cookie header value, null if no cookies are passed.
     */
    @Deprecated
    public static String encode(Cookie... cookies) {
        return io.netty.handler.codec.http.cookie.ClientCookieEncoder.LAX.encode(cookies);
    }

    /**
     * 将 Iterable 中的 Cookie 编码为单个请求头值。
     *
     * @param cookies some cookies
     * @return a Rfc6265 style Cookie header value, null if no cookies are passed.
     */
    @Deprecated
    public static String encode(Iterable<Cookie> cookies) {
        return io.netty.handler.codec.http.cookie.ClientCookieEncoder.LAX.encode(cookies);
    }

    private ClientCookieEncoder() {
        // 工具类禁止实例化
    }
}
