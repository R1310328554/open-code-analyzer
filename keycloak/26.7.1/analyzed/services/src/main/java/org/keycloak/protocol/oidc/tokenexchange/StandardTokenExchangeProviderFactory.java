/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
 * 标准（RFC 8693）令牌交换提供者工厂。
 * <p>标识为 {@code standard}，在启用 {@code TOKEN_EXCHANGE_STANDARD_V2} 特性时可用；优先级高于 V1 工厂。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class StandardTokenExchangeProviderFactory implements TokenExchangeProviderFactory, EnvironmentDependentProviderFactory {

    /** @param session Keycloak 会话 @return {@link StandardTokenExchangeProvider} 实例 */
    @Override
    public TokenExchangeProvider create(KeycloakSession session) {
        return new StandardTokenExchangeProvider();
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

    /** @return 工厂标识 {@code standard} */
    @Override
    public String getId() {
        return "standard";
    }

    /** 需启用 TOKEN_EXCHANGE_STANDARD_V2 特性 @return 是否支持 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.TOKEN_EXCHANGE_STANDARD_V2);
    }

    /** @return 优先级 10（高于 V1，两者均启用时优先选用） */
    @Override
    public int order() {
        // 优先级高于 V1，两者同时启用时优先使用标准交换
        return 10;
    }
}
