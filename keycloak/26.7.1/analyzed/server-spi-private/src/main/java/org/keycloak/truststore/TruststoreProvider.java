/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.truststore;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.x500.X500Principal;

import org.keycloak.common.enums.HostnameVerificationPolicy;
import org.keycloak.provider.Provider;

/**
 * 信任库提供者：提供 TLS/HTTPS 与 mTLS 所需的信任库、证书链与 {@link SSLSocketFactory}。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public interface TruststoreProvider extends Provider {

    /** @return 主机名验证策略 */
    HostnameVerificationPolicy getPolicy();

    /** @return 基于信任库配置的 SSL 套接字工厂 */
    SSLSocketFactory getSSLSocketFactory();

    /** @return 通用信任库 KeyStore */
    KeyStore getTruststore();

    /**
     * @return 信任库中的根证书，键为 X500Principal
     */
    Map<X500Principal, List<X509Certificate>> getRootCertificates();

    /**
     * @return 信任库中的中间证书，键为 X500Principal
     */
    Map<X500Principal, List<X509Certificate>> getIntermediateCertificates();

    /**
     * 返回 mTLS 专用信任库。
     * Returns the truststore used for mTLS
     * @return The mTLS truststore
     */
    KeyStore getHttpsTruststore();

    /** @return HTTPS/mTLS 信任库中的根证书映射 */
    Map<X500Principal, List<X509Certificate>> getHttpsRootCertificates();

    /** @return HTTPS/mTLS 信任库中的中间证书映射 */
    Map<X500Principal, List<X509Certificate>> getHttpsIntermediateCertificates();
}
