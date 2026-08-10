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
 *
 */

package org.keycloak.services.clienttype.impl;

import org.keycloak.Config;
import org.keycloak.client.clienttype.ClientTypeProvider;
import org.keycloak.client.clienttype.ClientTypeProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * 默认客户端类型 Provider 工厂。
 * <p>在 CLIENT_TYPES 特性启用时注册 {@link DefaultClientTypeProvider}。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientTypeProviderFactory implements ClientTypeProviderFactory, EnvironmentDependentProviderFactory {

    /** Provider 唯一标识符 */
    public static final String PROVIDER_ID = "default";

    /** {@inheritDoc} 创建 {@link DefaultClientTypeProvider} */
    @Override
    public ClientTypeProvider create(KeycloakSession session) {
        return new DefaultClientTypeProvider();
    }

    /** {@inheritDoc} 无初始化逻辑 */
    @Override
    public void init(Config.Scope config) {}

    /** {@inheritDoc} 无后置初始化 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 无资源需释放 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 仅在 CLIENT_TYPES 特性开启时可用 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CLIENT_TYPES);
    }
}