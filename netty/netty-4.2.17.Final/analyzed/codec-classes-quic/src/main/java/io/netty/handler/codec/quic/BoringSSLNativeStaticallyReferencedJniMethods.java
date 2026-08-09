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

/**
 * 在类加载阶段通过 JNI 读取的 BoringSSL/OpenSSL 常量。
 * <p>
 * 必须在静态初始化时引用，以便 native 库在加载时注册这些符号；供证书校验与签名算法枚举使用。
 */
final class BoringSSLNativeStaticallyReferencedJniMethods {
    // SSL 证书校验模式
    static native int ssl_verify_none();
    static native int ssl_verify_peer();
    static native int ssl_verify_fail_if_no_peer_cert();

    // X509 校验结果码
    static native int x509_v_ok();
    static native int x509_v_err_cert_has_expired();
    static native int x509_v_err_cert_not_yet_valid();
    static native int x509_v_err_cert_revoked();
    static native int x509_v_err_unspecified();

    // TLS 签名算法标识（与 BoringSSL SSL_SIGN_* 对应）
    static native int ssl_sign_rsa_pkcs_sha1();
    static native int ssl_sign_rsa_pkcs_sha256();
    static native int ssl_sign_rsa_pkcs_sha384();
    static native int ssl_sign_rsa_pkcs_sha512();
    static native int ssl_sign_ecdsa_pkcs_sha1();
    static native int ssl_sign_ecdsa_secp256r1_sha256();
    static native int ssl_sign_ecdsa_secp384r1_sha384();
    static native int ssl_sign_ecdsa_secp521r1_sha512();
    static native int ssl_sign_rsa_pss_rsae_sha256();
    static native int ssl_sign_rsa_pss_rsae_sha384();
    static native int ssl_sign_rsa_pss_rsae_sha512();
    static native int ssl_sign_ed25519();
    static native int ssl_sign_rsa_pkcs1_md5_sha1();

    private BoringSSLNativeStaticallyReferencedJniMethods() { }
}
