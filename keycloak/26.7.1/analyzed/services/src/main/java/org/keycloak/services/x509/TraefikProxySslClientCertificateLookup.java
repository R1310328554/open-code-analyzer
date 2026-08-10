/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.x509;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import org.keycloak.common.util.PemException;
import org.keycloak.common.util.PemUtils;
import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;

/**
 * Traefik 反向代理 SSL 客户端证书查找实现。
 *
 * <p>从 Traefik {@code PassTLSClientCert} 中间件（{@code pem: true}）转发的
 * {@code X-Forwarded-Tls-Client-Cert} HTTP 头中提取 URL 编码 PEM 格式的客户端证书及中间 CA 证书。</p>
 *
 * <p>Traefik 配置示例：</p>
 * <pre>
 * [http.middlewares.my-tls-client-cert.passTLSClientCert]
 *   [http.middlewares.my-tls-client-cert.passTLSClientCert.pem]
 *     pem = true
 * </pre>
 *
 * @see <a href="https://doc.traefik.io/traefik/middlewares/http/passtlsclientcert/">Traefik PassTLSClientCert middleware</a>
 */
public class TraefikProxySslClientCertificateLookup implements X509ClientCertificateLookup {

    private static final Logger log = Logger.getLogger(TraefikProxySslClientCertificateLookup.class);

    /** 最多加载的链证书数量（不含叶子证书时的额外限制）。 */
    protected int certificateChainLength;

    /**
     * 构造 Traefik 证书查找器。
     *
     * @param certificateChainLength 链长度上限
     */
    public TraefikProxySslClientCertificateLookup(int certificateChainLength) {
        this.certificateChainLength = certificateChainLength;
    }

    /** {@inheritDoc} 从 Traefik 转发的逗号分隔 PEM 头中解析证书链。 */
    @Override
    public X509Certificate[] getCertificateChain(HttpRequest httpRequest) throws GeneralSecurityException {
        if (!httpRequest.isProxyTrusted()) {
            log.warnf("HTTP header \"%s\" is not trusted", TraefikProxySslClientCertificateLookupFactory.HTTP_HEADER_CLIENT_CERT);
            return null;
        }

        String headerValue = httpRequest.getHttpHeaders().getRequestHeaders().getFirst(TraefikProxySslClientCertificateLookupFactory.HTTP_HEADER_CLIENT_CERT);

        if (headerValue == null || headerValue.isBlank()) {
            log.warnf("HTTP header \"%s\" is empty", TraefikProxySslClientCertificateLookupFactory.HTTP_HEADER_CLIENT_CERT);
            return new X509Certificate[0];
        }

        try {
            // 逗号分隔的多个 PEM 块，限制总数为 certificateChainLength + 1（含叶子证书）
            X509Certificate[] certs = Stream.of(headerValue.split(",")).map(PemUtils::decodeCertificate)
                    .limit(certificateChainLength + 1).toArray(X509Certificate[]::new);
            if (certs.length == 0) {
                log.warnf("HTTP header \"%s\" does not contain any valid X.509 certificates", TraefikProxySslClientCertificateLookupFactory.HTTP_HEADER_CLIENT_CERT);
            } else {
                log.debugf("Found %d X.509 certificate(s) in \"%s\" HTTP header", certs.length, TraefikProxySslClientCertificateLookupFactory.HTTP_HEADER_CLIENT_CERT);
            }
            return certs;
        } catch (PemException e) {
            throw new GeneralSecurityException(e);
        }
    }

    /** {@inheritDoc} 无状态实现，无需释放资源。 */
    @Override
    public void close() {
        // intentionally left blank
    }
}
