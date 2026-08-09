/*
 * Copyright 2021 The Netty Project
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

import io.netty.util.concurrent.Future;

import javax.net.ssl.SSLEngine;

/**
 * 异步私钥操作接口：签名与解密在 Java 侧异步完成，通过 {@link Future} 回传结果。
 * <p>
 * 适用于 HSM、远程 KMS 等无法同步访问私钥的场景。
 */
public interface BoringSSLAsyncPrivateKeyMethod {
    int SSL_SIGN_RSA_PKCS1_SHA1 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA1;
    int SSL_SIGN_RSA_PKCS1_SHA256 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA256;
    int SSL_SIGN_RSA_PKCS1_SHA384 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA384;
    int SSL_SIGN_RSA_PKCS1_SHA512 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA512;
    int SSL_SIGN_ECDSA_SHA1 = BoringSSLPrivateKeyMethod.SSL_SIGN_ECDSA_SHA1;
    int SSL_SIGN_ECDSA_SECP256R1_SHA256 = BoringSSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP256R1_SHA256;
    int SSL_SIGN_ECDSA_SECP384R1_SHA384 = BoringSSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP384R1_SHA384;
    int SSL_SIGN_ECDSA_SECP521R1_SHA512 = BoringSSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP521R1_SHA512;
    int SSL_SIGN_RSA_PSS_RSAE_SHA256 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA256;
    int SSL_SIGN_RSA_PSS_RSAE_SHA384 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA384;
    int SSL_SIGN_RSA_PSS_RSAE_SHA512 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA512;
    int SSL_SIGN_ED25519 = BoringSSLPrivateKeyMethod.SSL_SIGN_ED25519;
    int SSL_SIGN_RSA_PKCS1_MD5_SHA1 = BoringSSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_MD5_SHA1;

    /**
     * 使用指定算法对摘要签名，完成后通过 {@link Future} 通知签名结果字节。
     *
     * @param engine                the {@link SSLEngine}
     * @param signatureAlgorithm    the algorithm to use for signing
     * @param input                 the digest itself
     * @return                      the {@link Future} that will be notified with the signed data
     *                              (must not be {@code null}) when the operation completes.
     */
    Future<byte[]> sign(SSLEngine engine, int signatureAlgorithm, byte[] input);

    /**
     * 使用私钥解密输入数据，完成后通过 {@link Future} 通知解密结果字节。
     *
     * @param engine                the {@link SSLEngine}
     * @param input                 the input which should be decrypted
     * @return                      the {@link Future} that will be notified with the decrypted data
     *                              (must not be {@code null}) when the operation completes.
     */
    Future<byte[]> decrypt(SSLEngine engine, byte[] input);
}
