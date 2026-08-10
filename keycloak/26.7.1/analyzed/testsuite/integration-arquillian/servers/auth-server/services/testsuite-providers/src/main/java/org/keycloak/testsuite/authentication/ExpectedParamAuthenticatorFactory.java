/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.authentication;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 期望参数认证器工厂，用于注册校验 OIDC 查询参数的测试认证器。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ExpectedParamAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    /** 提供者在 SPI 中的标识符。 */
    public static final String PROVIDER_ID = "expected-param-authenticator";

    /** 单例认证器实例。 */
    private static final ExpectedParamAuthenticator SINGLETON = new ExpectedParamAuthenticator();

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 返回共享的 {@link ExpectedParamAuthenticator} 单例。 */
    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    /** 支持的认证执行要求选项。 */
    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    /** {@inheritDoc} 返回 {@link #REQUIREMENT_CHOICES}。 */
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    /** {@inheritDoc} 支持配置期望参数值与自动登录用户。 */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    /** {@inheritDoc} 说明需发送匹配 {@code foo} 查询参数才能通过认证。 */
    @Override
    public String getHelpText() {
        return "You will be approved if you send query string parameter 'foo' with expected value.";
    }

    /** {@inheritDoc} 管理控制台展示名称。 */
    @Override
    public String getDisplayType() {
        return "TEST: Expected Parameter";
    }

    @Override
    public String getReferenceCategory() {
        return "Expected Parameter";
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

    /** 静态配置属性列表，包含期望值与自动登录用户两项。 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ExpectedParamAuthenticator.EXPECTED_VALUE);
        property.setLabel("Expected query parameter value");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Expected value of query parameter foo. Authenticator will success if request to OIDC authz endpoint has this parameter");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(ExpectedParamAuthenticator.LOGGED_USER);
        property.setLabel("Automatically logged user");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("This user will be successfully authenticated automatically when present");
        configProperties.add(property);
    }


    /** {@inheritDoc} 返回期望参数值与自动登录用户的配置项。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }


}
