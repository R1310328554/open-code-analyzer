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
package org.keycloak.authorization.authzen;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.wellknown.WellKnownProvider;
import org.keycloak.wellknown.WellKnownProviderFactory;

/**
 * AuthZen 配置端点的 {@link WellKnownProviderFactory} 实现。
 *
 * <p>在启用 {@link Feature#AUTHZEN} 特性时，向 well-known 元数据暴露 OpenID AuthZen 授权相关配置。</p>
 */
public class AuthZenWellKnownProviderFactory implements WellKnownProviderFactory, EnvironmentDependentProviderFactory {

    /** Well-known 提供方标识符。 */
    public static final String PROVIDER_ID = "authzen-configuration";

    @Override
    /** 为当前会话创建 {@link AuthZenWellKnownProvider} 实例。 */
    public WellKnownProvider create(KeycloakSession session) {
        return new AuthZenWellKnownProvider(session);
    }

    @Override
    /** 工厂初始化（当前无额外配置）。 */
    public void init(Config.Scope config) {
    }

    @Override
    /** 会话工厂就绪后的后置初始化钩子。 */
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    /** 释放工厂资源（当前无操作）。 */
    public void close() {
    }

    @Override
    /** 返回 {@link #PROVIDER_ID}。 */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 该提供方应通过服务器元数据对外可见。 */
    public boolean isAvailableViaServerMetadata() {
        return true;
    }

    @Override
    /** 仅当 AuthZen 特性启用时注册此工厂。 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Feature.AUTHZEN);
    }
}
