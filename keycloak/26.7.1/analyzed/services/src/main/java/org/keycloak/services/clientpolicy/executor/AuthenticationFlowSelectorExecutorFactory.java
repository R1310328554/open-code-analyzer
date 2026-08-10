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
 */

package org.keycloak.services.clientpolicy.executor;

import java.util.Arrays;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link AuthenticationFlowSelectorExecutor} 的 SPI Factory：注册 {@code auth-flow-enforcer} 提供方。
 * <p>Admin Console 可配置认证流别名与 LOA 属性。</p>
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class AuthenticationFlowSelectorExecutorFactory implements ClientPolicyExecutorProviderFactory  {

    /** Executor 提供方 ID：{@value}。 */
    public static final String PROVIDER_ID = "auth-flow-enforcer";

    /** 配置项键：认证流别名。 */
    public static final String AUTH_FLOW_ALIAS = "auth-flow-alias";
    /** 配置项键：认证流 LOA。 */
    public static final String AUTH_FLOW_LOA = "auth-flow-loa";

    private static final ProviderConfigProperty AUTH_FLOW_ALIAS_PROPERTY = new ProviderConfigProperty(
            AUTH_FLOW_ALIAS, "Auth Flow Alias", "Insert the alias of the authentication flow",
            ProviderConfigProperty.STRING_TYPE, null);

    private static final ProviderConfigProperty AUTH_FLOW_LOA_PROPERTY = new ProviderConfigProperty(
            AUTH_FLOW_LOA, "Auth Flow Loa", "Insert the loa to enforce when the selected authentication flow is executed",
            ProviderConfigProperty.INTEGER_TYPE, 1);

    /** {@inheritDoc} 创建 Executor 实例 */
    @Override
    public AuthenticationFlowSelectorExecutor create(KeycloakSession session) {
        return new AuthenticationFlowSelectorExecutor();
    }

    /** {@inheritDoc} SPI 初始化（无额外配置） */
    @Override
    public void init(Scope config) {
    }

    /** {@inheritDoc} SPI 后置初始化 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 关闭 Factory */
    @Override
    public void close() {
    }

    /** {@inheritDoc} @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} Admin UI 帮助文本（当前为空） */
    @Override
    public String getHelpText() {
        return "";
    }

    /** {@inheritDoc} 返回认证流别名与 LOA 配置属性 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Arrays.asList(AUTH_FLOW_ALIAS_PROPERTY, AUTH_FLOW_LOA_PROPERTY);
    }

}
