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
 * 按钮式认证器工厂，注册展示 HTML 提交按钮的测试认证器。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PushButtonAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    /** 提供者在 SPI 中的标识符。 */
    public static final String PROVIDER_ID = "push-button-authenticator";
    /** 单例认证器实例。 */
    private static final PushButtonAuthenticator SINGLETON = new PushButtonAuthenticator();

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 返回共享的 {@link PushButtonAuthenticator} 单例。 */
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

    /** {@inheritDoc} 该认证器无需额外配置项。 */
    @Override
    public boolean isConfigurable() {
        return false;
    }

    /** {@inheritDoc} 说明只需点击按钮即可登录。 */
    @Override
    public String getHelpText() {
        return "Just press the button to login.";
    }

    /** {@inheritDoc} 管理控制台展示名称。 */
    @Override
    public String getDisplayType() {
        return "TEST: Button Login";
    }

    @Override
    public String getReferenceCategory() {
        return "Button Login";
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

    /** 空配置属性列表。 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    /** {@inheritDoc} 返回空列表，无可配置属性。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }


}
