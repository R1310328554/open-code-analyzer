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
package org.keycloak.testsuite.authentication;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * <p>延迟认证器工厂：创建在认证步骤中休眠若干毫秒后再成功的测试认证器。</p>
 *
 * @author rmartinc
 */
public class DelayedAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    /** 提供者在 SPI 中的标识符。 */
    public static final String PROVIDER_ID = "delayed-authenticator";

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 创建新的 {@link DelayedAuthenticator} 实例。 */
    @Override
    public Authenticator create(KeycloakSession session) {
        return new DelayedAuthenticator();
    }

    /** {@inheritDoc} 支持 REQUIRED、ALTERNATIVE 与 DISABLED 三种执行要求。 */
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    /** {@inheritDoc} 支持通过管理控制台配置延迟时间。 */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    /** {@inheritDoc} 帮助文本说明该认证器会延迟认证若干毫秒。 */
    @Override
    public String getHelpText() {
        return "Just delay the autentication some millis.";
    }

    /** {@inheritDoc} 管理控制台展示名称。 */
    @Override
    public String getDisplayType() {
        return "TEST: Delayed authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "Delayed authenticator";
    }

    @Override
    public void init(Config.Scope config) {
        // 无操作
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // 无操作
    }

    @Override
    public void close() {
        // 无操作
    }

    /** {@inheritDoc} 提供可配置的延迟毫秒数属性。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("delay")
                .label("Delay time in millis")
                .helpText("The delayed time to apply in the authentication.")
                .type(ProviderConfigProperty.INTEGER_TYPE)
                .defaultValue("1000")
                .add()
                .build();
    }
}
