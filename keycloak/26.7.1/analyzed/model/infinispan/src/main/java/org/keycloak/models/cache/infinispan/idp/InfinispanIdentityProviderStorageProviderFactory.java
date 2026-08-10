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
package org.keycloak.models.cache.infinispan.idp;

import org.keycloak.Config;
import org.keycloak.models.IdentityProviderStorageProvider;
import org.keycloak.models.IdentityProviderStorageProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Infinispan 身份提供者存储工厂的 SPI 实现。
 * <p>
 * 注册 ID 为 {@value #PROVIDER_ID}，优先级 {@code order=10}，
 * 创建带领域缓存层的 {@link InfinispanIdentityProviderStorageProvider} 实例。
 */
public class InfinispanIdentityProviderStorageProviderFactory implements IdentityProviderStorageProviderFactory<IdentityProviderStorageProvider> {

    /** SPI 提供商标识符。 */
    public static final String PROVIDER_ID = "infinispan";

    /** 创建带 Infinispan 缓存的身份提供者存储提供者。 */
    @Override
    public IdentityProviderStorageProvider create(KeycloakSession session) {
        return new InfinispanIdentityProviderStorageProvider(session);
    }

    /** 工厂初始化（当前无配置项）。 */
    @Override
    public void init(Config.Scope config) {
    }

    /** 会话工厂就绪后的后置初始化（当前无操作）。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭工厂资源（当前无操作）。 */
    @Override
    public void close() {
    }

    /** 返回 SPI 提供商标识符。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 返回工厂优先级，数值越小越优先。 */
    @Override
    public int order() {
        return 10;
    }
}
