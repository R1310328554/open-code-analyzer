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

package org.keycloak.authentication.authenticators.conditional;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 条件 client scope 认证器工厂：注册 {@link ConditionalClientScopeAuthenticator}。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ConditionalClientScopeAuthenticatorFactory implements ConditionalAuthenticatorFactory {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "conditional-client-scope";
    /** 配置键：要检查的 client scope 名称。 */
    public static final String CLIENT_SCOPE = "client_scope";
    /** 配置键：是否对检查结果取反。 */
    public static final String CONF_NEGATE = "negate";

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Condition - client scope";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED};
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    /** @return 条件说明：检查 client scope 是否存在于认证请求中 */
    @Override
    public String getHelpText() {
        return "Condition to evaluate if a configured client scope is present as a client scope of the client requesting authentication";
    }

    /** @return client scope 名称与 negate 配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                    .name(CLIENT_SCOPE)
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .label("Client scope name")
                    .helpText("The name of the client scope, which should be present as a client scope of the client, which is requesting authentication. If requested client scope is default client scope of the client requesting login, the condition will be evaluated to true. If requested client scope is optional client scope of the client requesting login, condition will be evaluated to true if client scope is sent by the client in the login request (EG. by the 'scope' parameter in case of OIDC/OAuth2 client login)")
                    .required(true)
                    .add()
                .property()
                    .name(CONF_NEGATE)
                    .type(ProviderConfigProperty.BOOLEAN_TYPE)
                    .label("Negate output")
                    .helpText(
                        "Apply a NOT to the check result. When this is true, then the condition will evaluate to true just if configured client scope is not present"
                    )
                    .required(true)
                    .add()
                .build();
    }

    /** @return {@link ConditionalClientScopeAuthenticator} 单例 */
    @Override
    public ConditionalAuthenticator getSingleton() {
        return ConditionalClientScopeAuthenticator.SINGLETON;
    }
}
