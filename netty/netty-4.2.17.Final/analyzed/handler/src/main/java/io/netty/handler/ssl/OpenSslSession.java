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
package io.netty.handler.ssl;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

/**
 * {@link SSLSession} sub-type that is used by our native implementation.
 *
 * <p>Netty OpenSSL 引擎使用的 {@link SSLSession} 子类型：提供无异常的对端证书探测及 named group 查询，
 * 并返回 {@link OpenSslSessionContext}。</p>
 */
public interface OpenSslSession extends SSLSession {

    /**
     * Returns true if the peer has provided certificates during the handshake.
     * <p>
     * This method is similar to {@link SSLSession#getPeerCertificates()} but it does not throw a
     * {@link SSLPeerUnverifiedException} if no certs are provided, making it more efficient to check if a mTLS
     * connection is used.
     *
     * <p>握手阶段对端是否已提供证书；相比 {@link #getPeerCertificates()} 不会在无证书时抛
     * {@link SSLPeerUnverifiedException}，便于快速判断是否 mTLS。</p>
     *
     * @return true if peer certificates are available.
     */
    boolean hasPeerCertificates();

    /**
     * Returns the used named group or {@code null} if none was used or if it can't be determined.
     *
     * <p>密钥交换使用的 named group（如 x25519、secp256r1）；无法确定时返回 {@code null}。</p>
     *
     * @return  the named group
     */
    default String getNamedGroup() {
        return null;
    }

    @Override
    OpenSslSessionContext getSessionContext();
}
