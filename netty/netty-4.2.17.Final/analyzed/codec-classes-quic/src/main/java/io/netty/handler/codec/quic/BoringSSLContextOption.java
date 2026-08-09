/*
 * Copyright 2024 The Netty Project
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
package io.netty.handler.codec.quic;

import io.netty.handler.ssl.SslContextOption;

import java.util.Map;
import java.util.Set;

/**
 * BoringSSL 专有的 {@link SslContextOption} 配置项。
 *
 * @param <T>   the type of the value.
 */
public final class BoringSSLContextOption<T> extends SslContextOption<T> {
    private BoringSSLContextOption(String name) {
        super(name);
    }

    /**
     * 设置 TLS 密钥交换组（曲线）列表，覆盖 {@code -Djdk.tls.namedGroups}。
     * <p>
     * See <a href="https://github.com/google/boringssl/blob/master/include/openssl/ssl.h#L2632">
     *     SSL_CTX_set1_groups_list</a>.
     */
    public static final BoringSSLContextOption<String[]> GROUPS = new BoringSSLContextOption<>("GROUPS");

    /**
     * 设置 TLS 握手使用的签名算法列表。
     * <p>
     * See <a href="https://github.com/google/boringssl/blob/master/include/openssl/ssl.h#L5166">
     *     SSL_CTX_set1_sigalgs</a>.
     */
    public static final BoringSSLContextOption<String[]> SIGNATURE_ALGORITHMS =
            new BoringSSLContextOption<>("SIGNATURE_ALGORITHMS");

    /**
     * 配置 {@link BoringSSLCertificateCallback} 支持的客户端密钥/证书类型集合。
     */
    public static final BoringSSLContextOption<Set<String>> CLIENT_KEY_TYPES =
            new BoringSSLContextOption<>("CLIENT_KEY_TYPES");

    /**
     * 配置 {@link BoringSSLCertificateCallback} 服务端 authMethod → 密钥类型 映射。
     */
    public static final BoringSSLContextOption<Map<String, String>> SERVER_KEY_TYPES =
            new BoringSSLContextOption<>("SERVER_KEY_TYPES");
}
