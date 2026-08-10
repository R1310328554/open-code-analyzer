/*
 * Copyright 2017 Analytical Graphics, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.x509;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认 X.509 客户端证书查找工厂。
 * <p>工厂及其对应提供者直接从入站 TLS 连接中提取客户端证书及证书链（如有）。
 * 此为 Keycloak 默认使用的实现。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 4/4/2017
 */

public class DefaultClientCertificateLookupFactory implements X509ClientCertificateLookupFactory {

    /** 提供者标识符：{@code default}。 */
    private final static String PROVIDER = "default";
    /** 单例查找实例，无状态可复用。 */
    private final static X509ClientCertificateLookup SINGLETON =
            new DefaultClientCertificateLookup();

    /** {@inheritDoc} 返回共享单例实例。 */
    @Override
    public X509ClientCertificateLookup create(KeycloakSession session) {
        return SINGLETON;
    }

    /** {@inheritDoc} 默认实现无需初始化配置。 */
    @Override
    public void init(Config.Scope config) {

    }

    /** {@inheritDoc} 默认实现无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 默认实现无资源需释放。 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@link #PROVIDER}。 */
    @Override
    public String getId() {
        return PROVIDER;
    }
}
