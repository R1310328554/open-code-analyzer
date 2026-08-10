/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.rar;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link AuthorizationDetailsProcessor} 的 SPI 工厂接口。
 *
 * @author <a href="mailto:Forkim.Akwichek@adorsys.com">Forkim Akwichek</a>
 */
public interface AuthorizationDetailsProcessorFactory extends ProviderFactory<AuthorizationDetailsProcessor<?>> {

    /** 创建 {@link AuthorizationDetailsProcessor} 实例。 */
    @Override
    AuthorizationDetailsProcessor<?> create(KeycloakSession session);

    @Override
    default void postInit(KeycloakSessionFactory factory) {
        // 默认无 postInit 逻辑
    }

    @Override
    default void close() {
        // Default implementation does nothing
    }
}
