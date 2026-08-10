/*
 * Copyright 2018 The Netty Project
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
package io.netty.handler.ssl;

import io.netty.util.ReferenceCounted;

import java.security.cert.X509Certificate;

/**
 * Holds references to the native key-material that is used by OpenSSL.
 *
 * <p>封装 OpenSSL 握手所需的 native 密钥材料：Java 侧证书链、{@code STACK_OF(X509)} 与 {@code EVP_PKEY}
 * 指针；实现 {@link ReferenceCounted} 以便与 {@link ReferenceCountedOpenSslEngine} 共享所有权。</p>
 */
interface OpenSslKeyMaterial extends ReferenceCounted {

    /**
     * Returns the configured {@link X509Certificate}s.
     *
     * <p>返回配置的 Java 证书链副本，供 SSLEngine/TrustManager 等 API 使用。</p>
     */
    X509Certificate[] certificateChain();

    /**
     * Returns the pointer to the {@code STACK_OF(X509)} which holds the certificate chain.
     *
     * <p>返回 native 证书栈指针，直接传给 OpenSSL 握手逻辑。</p>
     */
    long certificateChainAddress();

    /**
     * Returns the pointer to the {@code EVP_PKEY}.
     *
     * <p>返回 native 私钥 {@code EVP_PKEY*} 地址。</p>
     */
    long privateKeyAddress();

    @Override
    OpenSslKeyMaterial retain();

    @Override
    OpenSslKeyMaterial retain(int increment);

    @Override
    OpenSslKeyMaterial touch();

    @Override
    OpenSslKeyMaterial touch(Object hint);

    @Override
    boolean release();

    @Override
    boolean release(int decrement);
}
