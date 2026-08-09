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

/**
 * HTTP Cookie 接口，定义 name、value、domain、path、maxAge 等属性。
 * <p>
 * 实现类通常为 {@link DefaultCookie}；编解码见 {@link ClientCookieDecoder} 等。
 */
public interface Cookie extends Comparable<Cookie> {

    /** MaxAge 未指定时的常量值（{@link Long#MIN_VALUE}）。 */

    long UNDEFINED_MAX_AGE = Long.MIN_VALUE;

    /**
     * 返回 Cookie 名称。
     * @return The name of this {@link Cookie}
     */
    String name();

    /**
     * 返回 Cookie 值。
     * @return The value of this {@link Cookie}
     */
    String value();

    /**
     * 设置 Cookie 值。
     * @param value The value to set
     */
    void setValue(String value);

    /**
     * 原始 Set-Cookie 中 value 是否用双引号包裹。
     * @return If the value of this {@link Cookie} is to be wrapped
     */
    boolean wrap();

    /**
     * 设置编码时 value 是否用双引号包裹。
     * @param wrap true if wrap
     */
    void setWrap(boolean wrap);

    /**
     * 返回 Cookie 的 Domain 属性。
     * @return The domain of this {@link Cookie}
     */
    String domain();

    /**
     * 设置 Domain 属性。
     * @param domain The domain to use
     */
    void setDomain(String domain);

    /**
     * 返回 Cookie 的 Path 属性。
     * @return The {@link Cookie}'s path
     */
    String path();

    /**
     * 设置 Path 属性。
     * @param path The path to use for this {@link Cookie}
     */
    void setPath(String path);

    /**
     * 返回 Max-Age（秒）；未指定时返回 {@link Cookie#UNDEFINED_MAX_AGE}。
     * @return The maximum age of this {@link Cookie}
     */
    long maxAge();

    /**
     * 设置 Max-Age（秒）；{@code 0} 立即过期；{@link Cookie#UNDEFINED_MAX_AGE} 为会话 Cookie。
     * @param maxAge The maximum age of this {@link Cookie} in seconds
     */
    void setMaxAge(long maxAge);

    /**
     * 是否设置了 Secure 属性（仅 HTTPS 发送）。
     * @return True if this {@link Cookie} is secure, otherwise false
     */
    boolean isSecure();

    /**
     * 设置 Secure 属性。
     * @param secure True if this {@link Cookie} is to be secure, otherwise false
     */
    void setSecure(boolean secure);

    /**
     * 是否 HttpOnly（禁止客户端脚本访问，需浏览器支持）。
     * @return True if this {@link Cookie} is HTTP-only or false if it isn't
     */
    boolean isHttpOnly();

    /**
     * 设置 HttpOnly 属性。
     * @param httpOnly True if the {@link Cookie} is HTTP only, otherwise false.
     */
    void setHttpOnly(boolean httpOnly);
}
