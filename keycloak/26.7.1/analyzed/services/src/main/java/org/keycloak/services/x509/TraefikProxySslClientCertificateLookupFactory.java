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

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Traefik 反向代理 X.509 客户端证书查找工厂。
 *
 * <p>创建 {@link TraefikProxySslClientCertificateLookup} 实例，从 Traefik
 * {@code PassTLSClientCert} 中间件转发的 HTTP 头中提取客户端证书。</p>
 *
 * @see TraefikProxySslClientCertificateLookup
 */
public class TraefikProxySslClientCertificateLookupFactory implements X509ClientCertificateLookupFactory {

    /** 提供者标识符：{@code traefik}。 */
    private static final String PROVIDER = "traefik";

    /** Traefik 转发客户端证书的 HTTP 头名称。 */
    public static final String HTTP_HEADER_CLIENT_CERT = "X-Forwarded-Tls-Client-Cert";

    /** 链长度上限配置键。 */
    protected final static String HTTP_HEADER_CERT_CHAIN_LENGTH = "certificateChainLength";
    /** 链长度上限默认值。 */
    protected final static int HTTP_HEADER_CERT_CHAIN_LENGTH_DEFAULT = 1;

    /** 配置的链长度上限。 */
    protected int certificateChainLength;

    /** {@inheritDoc} 读取链长度配置。 */
    @Override
    public void init(Config.Scope config) {
        certificateChainLength = config.getInt(HTTP_HEADER_CERT_CHAIN_LENGTH, HTTP_HEADER_CERT_CHAIN_LENGTH_DEFAULT);
        if (certificateChainLength < 0) {
            throw new IllegalArgumentException("certificateChainLength must be greater or equal to zero");
        }
    }

    /** {@inheritDoc} 创建 Traefik 查找实例。 */
    @Override
    public X509ClientCertificateLookup create(KeycloakSession session) {
        return new TraefikProxySslClientCertificateLookup(certificateChainLength);
    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // intentionally left blank
    }

    /** {@inheritDoc} 无资源需释放。 */
    @Override
    public void close() {
        // intentionally left blank
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER}。 */
    @Override
    public String getId() {
        return PROVIDER;
    }
}
