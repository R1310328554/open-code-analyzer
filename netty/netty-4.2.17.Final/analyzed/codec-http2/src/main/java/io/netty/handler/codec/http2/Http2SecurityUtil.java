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
package io.netty.handler.codec.http2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * HTTP/2 专用 TLS 安全工具：提供符合 RFC 7540 的推荐密码套件列表。
 */
public final class Http2SecurityUtil {
    /**
     * 推荐密码套件列表，来源为 <a
     * href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html">SunJSSE Supported
     * Ciphers</a> 与 <a
     * href="https://wiki.mozilla.org/Security/Server_Side_TLS#Modern_compatibility">Mozilla Modern Cipher
     * Suites</a>，并剔除 <a
     * href="https://tools.ietf.org/html/rfc7540#section-9.2.2">HTTP/2 规范</a> 禁止的套件。
     *
     * <p>根据 <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html">
     * JSSE 文档</a>，TLS RFC 中以 {@code TLS_} 为前缀的名称与 JSSE 中以 {@code SSL_} 为前缀的套件功能等价；
     * 此处仅列出 {@code TLS_} 变体，由调用方按需映射。
     */
    public static final List<String> CIPHERS;

    /**
     * Mozilla Modern 中间兼容套件，减去 <a href="https://tools.ietf.org/html/rfc7540#appendix-A">HTTP/2 RFC 附录 A</a>
     * 列出的黑名单套件。
     */
    private static final List<String> CIPHERS_JAVA_MOZILLA_MODERN_SECURITY = Collections.unmodifiableList(Arrays
            .asList(
            /* openssl = ECDHE-ECDSA-AES128-GCM-SHA256 */
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",

            /* REQUIRED BY HTTP/2 SPEC */
            /* openssl = ECDHE-RSA-AES128-GCM-SHA256 */
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            /* REQUIRED BY HTTP/2 SPEC */

            /* openssl = ECDHE-ECDSA-AES256-GCM-SHA384 */
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            /* openssl = ECDHE-RSA-AES256-GCM-SHA384 */
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            /* openssl = ECDHE-ECDSA-CHACHA20-POLY1305 */
            "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
            /* openssl = ECDHE-RSA-CHACHA20-POLY1305 */
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",

            /* TLS 1.3 ciphers */
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256"
            ));

    static {
        // 防御性拷贝，避免外部修改内部列表
        CIPHERS = Collections.unmodifiableList(new ArrayList<String>(CIPHERS_JAVA_MOZILLA_MODERN_SECURITY));
    }

    private Http2SecurityUtil() { }
}
