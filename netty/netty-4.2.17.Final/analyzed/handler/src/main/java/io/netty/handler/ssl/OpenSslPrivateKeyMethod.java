/*
 * Copyright 2019 The Netty Project
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

import io.netty.internal.tcnative.SSLPrivateKeyMethod;
import io.netty.util.internal.UnstableApi;

import javax.net.ssl.SSLEngine;

/**
 * Allow to customize private key signing / decrypting (when using RSA). Only supported when using BoringSSL atm.
 *
 * <p>自定义私钥签名与 RSA 解密回调，供 OpenSSL 在无法直接使用 {@code EVP_PKEY} 时调用；
 * 当前主要支持 BoringSSL。算法常量与 tcnative {@link SSLPrivateKeyMethod} 对齐。</p>
 */
@UnstableApi
public interface OpenSslPrivateKeyMethod {
    /** RSA PKCS#1 SHA-1 签名算法 ID。 */
    int SSL_SIGN_RSA_PKCS1_SHA1 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA1;
    int SSL_SIGN_RSA_PKCS1_SHA256 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA256;
    int SSL_SIGN_RSA_PKCS1_SHA384 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA384;
    int SSL_SIGN_RSA_PKCS1_SHA512 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA512;
    int SSL_SIGN_ECDSA_SHA1 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SHA1;
    int SSL_SIGN_ECDSA_SECP256R1_SHA256 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP256R1_SHA256;
    int SSL_SIGN_ECDSA_SECP384R1_SHA384 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP384R1_SHA384;
    int SSL_SIGN_ECDSA_SECP521R1_SHA512 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP521R1_SHA512;
    int SSL_SIGN_RSA_PSS_RSAE_SHA256 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA256;
    int SSL_SIGN_RSA_PSS_RSAE_SHA384 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA384;
    int SSL_SIGN_RSA_PSS_RSAE_SHA512 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA512;
    int SSL_SIGN_ED25519 = SSLPrivateKeyMethod.SSL_SIGN_ED25519;
    int SSL_SIGN_RSA_PKCS1_MD5_SHA1 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_MD5_SHA1;

    /**
     * Signs the input with the given key and returns the signed bytes.
     *
     * <p>使用指定签名算法对握手 digest 签名；返回值不得为 {@code null}。</p>
     *
     * @param engine                the {@link SSLEngine}
     * @param signatureAlgorithm    the algorithm to use for signing
     * @param input                 the digest itself
     * @return                      the signed data (must not be {@code null})
     * @throws Exception            thrown if an error is encountered during the signing
     */
    byte[] sign(SSLEngine engine, int signatureAlgorithm, byte[] input) throws Exception;

    /**
     * Decrypts the input with the given key and returns the decrypted bytes.
     *
     * <p>RSA 私钥解密（如 TLS 密钥交换中的 encrypted premaster secret）。</p>
     *
     * @param engine                the {@link SSLEngine}
     * @param input                 the input which should be decrypted
     * @return                      the decrypted data (must not be {@code null})
     * @throws Exception            thrown if an error is encountered during the decrypting
     */
    byte[] decrypt(SSLEngine engine, byte[] input) throws Exception;
}
