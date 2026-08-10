/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.executor;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link UseLightweightAccessTokenExecutor} 的 Provider 工厂。
 * <p>无额外配置项；在客户端策略匹配时启用轻量级访问令牌。</p>
 */
public class UseLightweightAccessTokenExecutorFactory implements ClientPolicyExecutorProviderFactory {
    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "use-lightweight-access-token";

    /** @return 执行器说明（英文原文保留） */
    @Override
    public String getHelpText() {
        return "Use lightweight access token";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} 创建执行器实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new UseLightweightAccessTokenExecutor(session);
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

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 始终支持 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return true;
    }
}
