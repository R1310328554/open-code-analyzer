/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.services.resources.admin.ext;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 领域管理 REST API 扩展 SPI。
 * <p>通过 {@link Spi} 为 Realm Admin API 挂载额外 JAX-RS 子资源，扩展服务器未内置的路径。</p>
 * <p>实现者可注册自定义子路径，增强 Keycloak 管理能力。</p>
 */
public class AdminRealmResourceSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "admin-realm-restapi-extension";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return AdminRealmResourceProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory<?>> getProviderFactoryClass() {
        return AdminRealmResourceProviderFactory.class;
    }
}
