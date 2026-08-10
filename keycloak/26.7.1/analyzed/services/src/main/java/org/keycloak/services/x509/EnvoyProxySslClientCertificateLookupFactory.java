/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Envoy 反向代理 X.509 客户端证书查找工厂。
 * <p>创建 {@link EnvoyProxySslClientCertificateLookup} 实例，从 Envoy 的
 * {@code x-forwarded-client-cert} 头解析客户端证书。</p>
 */
public class EnvoyProxySslClientCertificateLookupFactory implements X509ClientCertificateLookupFactory {

    /** 提供者标识符：{@code envoy}。 */
    private final static String PROVIDER = "envoy";

    /** {@inheritDoc} 每次创建新的查找实例。 */
    @Override
    public X509ClientCertificateLookup create(KeycloakSession session) {
        return new EnvoyProxySslClientCertificateLookup();
    }

    /** {@inheritDoc} 无配置项需初始化。 */
    @Override
    public void init(Scope config) {
    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 无资源需释放。 */
    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER}。 */
    @Override
    public String getId() {
        return PROVIDER;
    }
}
