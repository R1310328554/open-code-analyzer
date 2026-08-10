/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.quarkus.runtime.integration.web;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.x509.X509ClientCertificateLookup;
import org.keycloak.services.x509.X509ClientCertificateLookupFactory;

/**
 * Quarkus 环境下 {@link X509ClientCertificateLookup} 的 SPI 工厂，注册 id 为 {@code quarkus} 的实现。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class VertxClientCertificateLookupFactory implements X509ClientCertificateLookupFactory {

    private static X509ClientCertificateLookup SINGLETON;

    /** {@inheritDoc} 返回进程级单例查找器。 */
    @Override
    public X509ClientCertificateLookup create(KeycloakSession session) {
        return SINGLETON;
    }

    /** {@inheritDoc} 初始化 Vert.x 证书查找单例。 */
    @Override
    public void init(Config.Scope config) {
        SINGLETON = new VertxClientCertificateLookup();
    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 无额外资源需释放。 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 工厂标识符 {@code quarkus}。 */
    @Override
    public String getId() {
        return "quarkus";
    }

    /** {@inheritDoc} 相对其他 X509 查找实现的排序权重。 */
    @Override
    public int order() {
        return 100;
    }
}
