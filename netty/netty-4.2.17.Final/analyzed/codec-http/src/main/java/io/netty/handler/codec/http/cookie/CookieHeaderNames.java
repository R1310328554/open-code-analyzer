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
 * Set-Cookie 头属性名常量及 SameSite 枚举。
 * <p>
 * 供 {@link ClientCookieDecoder} 等解析 Cookie 属性时使用。
 */
public final class CookieHeaderNames {
    /** Path 属性名（{@code "Path"}）。 */
    public static final String PATH = "Path";

    /** Expires 属性名（{@code "Expires"}）。 */
    public static final String EXPIRES = "Expires";

    /** Max-Age 属性名（{@code "Max-Age"}）。 */
    public static final String MAX_AGE = "Max-Age";

    /** Domain 属性名（{@code "Domain"}）。 */
    public static final String DOMAIN = "Domain";

    /** Secure 属性名（{@code "Secure"}）。 */
    public static final String SECURE = "Secure";

    /** HttpOnly 属性名（{@code "HTTPOnly"}）。 */
    public static final String HTTPONLY = "HTTPOnly";

    /** SameSite 属性名（{@code "SameSite"}）。 */
    public static final String SAMESITE = "SameSite";

    /** Partitioned 属性名（{@code "Partitioned"}，CHIPS）。 */
    public static final String PARTITIONED = "Partitioned";

    /**
     * SameSite 属性可选值（Lax / Strict / None）。
     * See <a href="https://tools.ietf.org/html/draft-ietf-httpbis-rfc6265bis-05">changes to RFC6265bis</a>
     */
    public enum SameSite {
        Lax,
        Strict,
        None;

        /**
         * 按名称（忽略大小写）解析 SameSite 枚举；无法识别时返回 {@code null}。
         * @param name value for the SameSite Attribute
         * @return enum value for the provided name or null
         */
        static SameSite of(String name) {
            if (name != null) {
                for (SameSite each : SameSite.class.getEnumConstants()) {
                    if (each.name().equalsIgnoreCase(name)) {
                        return each;
                    }
                }
            }
            return null;
        }
    }

    /** 工具类禁止实例化 */
    private CookieHeaderNames() {
        // Unused.
    }
}
