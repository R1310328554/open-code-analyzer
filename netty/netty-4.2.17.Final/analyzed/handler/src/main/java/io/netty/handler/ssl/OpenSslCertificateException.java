/*
 * Copyright 2016 The Netty Project
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

import io.netty.internal.tcnative.CertificateVerifier;

import java.security.cert.CertificateException;

/**
 * A special {@link CertificateException} which allows to specify which error code is included in the
 * SSL Record. This only work when {@link SslProvider#OPENSSL} or {@link SslProvider#OPENSSL_REFCNT} is used.
 *
 * <p>携带 OpenSSL 证书校验错误码的 {@link CertificateException}，供自定义
 * {@link ReferenceCountedOpenSslContext} 证书验证回调写入 TLS Alert；
 * 错误码须为 OpenSSL {@code X509_V_ERR_*} 常量（见 verify 手册页）。</p>
 */
public final class OpenSslCertificateException extends CertificateException {
    private static final long serialVersionUID = 5542675253797129798L;

    /** 写入 SSL 记录的 OpenSSL 验证错误码。 */
    private final int errorCode;

    /**
     * Construct a new exception with the
     * <a href="https://www.openssl.org/docs/manmaster/apps/verify.html">error code</a>.
     */
    public OpenSslCertificateException(int errorCode) {
        this((String) null, errorCode);
    }

    /**
     * Construct a new exception with the msg and
     * <a href="https://www.openssl.org/docs/manmaster/apps/verify.html">error code</a> .
     */
    public OpenSslCertificateException(String msg, int errorCode) {
        super(msg);
        this.errorCode = checkErrorCode(errorCode);
    }

    /**
     * Construct a new exception with the msg, cause and
     * <a href="https://www.openssl.org/docs/manmaster/apps/verify.html">error code</a> .
     */
    public OpenSslCertificateException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = checkErrorCode(errorCode);
    }

    /**
     * Construct a new exception with the cause and
     * <a href="https://www.openssl.org/docs/manmaster/apps/verify.html">error code</a> .
     */
    public OpenSslCertificateException(Throwable cause, int errorCode) {
        this(null, cause, errorCode);
    }

    /**
     * Return the <a href="https://www.openssl.org/docs/man1.0.2/apps/verify.html">error code</a> to use.
     * <p>返回构造时校验通过的 OpenSSL 错误码。</p>
     */
    public int errorCode() {
        return errorCode;
    }

    /**
     * 若 native 库已加载则校验 errorCode 是否为 OpenSSL 认可的 X509 验证码。
     */
    private static int checkErrorCode(int errorCode) {
        // 触发 OpenSsl 静态初始化以加载 tcnative；库不可用时跳过校验
        // Call OpenSsl.isAvailable() to ensure we try to load the native lib as CertificateVerifier.isValid(...)
        // will depend on it. If loading fails we will just skip the validation.
        if (OpenSsl.isAvailable() && !CertificateVerifier.isValid(errorCode)) {
            throw new IllegalArgumentException("errorCode '" + errorCode +
                    "' invalid, see https://www.openssl.org/docs/man1.0.2/apps/verify.html.");
        }
        return errorCode;
    }
}
