/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.protocol.oidc.tokenexchange;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.TokenExchangeProvider;
import org.keycloak.protocol.oidc.TokenExchangeProviderFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * V1 令牌交换提供者工厂。
 * <p>标识为 {@code default}，在启用 {@code TOKEN_EXCHANGE} 特性时可用；支持标准、联邦及 subject impersonation 等全部 V1 交换类型。</p>
 *
 * @author <a href="mailto:dmitryt@backbase.com">Dmitry Telegin</a>
 */
public class V1TokenExchangeProviderFactory implements TokenExchangeProviderFactory, EnvironmentDependentProviderFactory {

    /** @param session Keycloak 会话 @return {@link V1TokenExchangeProvider} 实例 */
    @Override
    public TokenExchangeProvider create(KeycloakSession session) {
        return new V1TokenExchangeProvider();
    }

    /** 初始化（无操作） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {
    }

    /** 工厂初始化后回调（无操作） @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {
    }

    /** @return 工厂标识 {@code default} */
    @Override
    public String getId() {
        return "default";
    }

    /** 需启用 TOKEN_EXCHANGE 特性 @return 是否支持 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.TOKEN_EXCHANGE);
    }
}
