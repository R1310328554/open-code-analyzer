/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.deployment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.Config;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认 {@link DeployedConfigurationsProvider} SPI 工厂。
 * <p>维护进程级 {@link ConcurrentHashMap}，使各会话共享同一份部署配置注册表。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultDeployedConfigurationsProviderFactory implements DeployedConfigurationsProviderFactory {

    /** SPI 工厂标识：{@code default}。 */
    public static final String PROVIDER_ID = "default";
    /** 跨会话共享的部署认证器配置存储。 */
    private final Map<String, AuthenticatorConfigModel> deployedAuthenticatorConfigs = new ConcurrentHashMap<>();
    @Override
    /** @param session 当前会话 @return 绑定共享映射的提供者实例 */
    public DeployedConfigurationsProvider create(KeycloakSession session) {
        return new DefaultDeployedConfigurationsProvider(deployedAuthenticatorConfigs);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    /** @return {@link #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }
}
