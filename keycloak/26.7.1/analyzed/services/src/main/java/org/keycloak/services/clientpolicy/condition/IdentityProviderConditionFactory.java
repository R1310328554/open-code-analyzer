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

package org.keycloak.services.clientpolicy.condition;

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 身份提供方条件工厂：注册 {@link IdentityProviderCondition}。
 * <p>定义 IdP 别名列表，检查客户端策略上下文中的别名是否属于该列表。</p>
 * <p>Condition that defines a list of Identity Provider aliases and checks if the
 * alias in the client policy context is (or is not) part of that list.</p>
 *
 * @author rmartinc
 */
public class IdentityProviderConditionFactory extends AbstractClientPolicyConditionProviderFactory {

    /** SPI 提供者标识符：identity-provider-alias */
    public static final String PROVIDER_ID = "identity-provider-alias";
    /** 配置键：身份提供方别名列表 */
    public static final String IDENTITY_PROVIDERS_ALIASES = "identity_provider_aliases";

    /** {@inheritDoc} 创建 {@link IdentityProviderCondition} 实例 */
    @Override
    public ClientPolicyConditionProvider create(KeycloakSession session) {
        return new IdentityProviderCondition(session);
    }

    /** {@inheritDoc} @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 检查请求涉及的 IdP 是否在配置范围内 */
    @Override
    public String getHelpText() {
        return """
               Condition that checks the Identity Provider that is involved in the client request.
               Only applies to operations in which an IdP is involved (for example JWT Authorization grant).
               """;
    }

    /** {@inheritDoc} 返回 IdP 别名多选配置项（必填） */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> properties = ProviderConfigurationBuilder.create()
                .property()
                .name(IDENTITY_PROVIDERS_ALIASES)
                .type(ProviderConfigProperty.IDENTITY_PROVIDER_MULTI_LIST_TYPE)
                .label("Identity provider aliases")
                .helpText("List of Identity Provider aliases to take into consideration for the condition.")
                .required(Boolean.TRUE)
                .add()
                .build();
        addCommonConfigProperties(properties);
        return properties;
    }

}
