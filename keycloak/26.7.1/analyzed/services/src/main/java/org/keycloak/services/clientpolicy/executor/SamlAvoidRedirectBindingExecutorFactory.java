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

package org.keycloak.services.clientpolicy.executor;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * {@link SamlAvoidRedirectBindingExecutor} 的 Provider 工厂。
 * <p>确保 SAML 响应不使用 REDIRECT 绑定，并在客户端创建/更新时强制 POST 绑定配置。</p>
 *
 * @author rmartinc
 */
public class SamlAvoidRedirectBindingExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "saml-avoid-redirect";

    /** @param session Keycloak 会话 @return 新的 SAML Redirect 避免执行器 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new SamlAvoidRedirectBindingExecutor(session);
    }

    /** 工厂初始化（无操作） */
    @Override
    public void init(Config.Scope config) {
        // 无初始化逻辑
    }

    /** 会话工厂就绪回调（无操作） */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // 无后置初始化逻辑
    }

    /** 工厂关闭钩子（无操作） */
    @Override
    public void close() {
        // 无关闭逻辑
    }

    /** @return 执行器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 执行器说明（英文原文保留） */
    @Override
    public String getHelpText() {
        return "Executor to avoid the REDIRECT binding in a SAML client.";
    }

    /** @return 无额外配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create().build();
    }
}
