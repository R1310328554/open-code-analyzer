/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.saml.profile.ecp.authenticator;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link HttpBasicAuthenticator} 的认证器工厂：注册 HTTP Basic 执行步骤供 ECP flow 使用。
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class HttpBasicAuthenticatorFactory implements AuthenticatorFactory {

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "http-basic-authenticator";

    /** {@inheritDoc} 控制台显示名：HTTP Basic Authentication */
    @Override
    public String getDisplayType() {
        return "HTTP Basic Authentication";
    }

    /** {@inheritDoc} 引用类别：basic */
    @Override
    public String getReferenceCategory() {
        return "basic";
    }

    /** {@inheritDoc} 无可配置属性 */
    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            Requirement.ALTERNATIVE,
            Requirement.CONDITIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    /** {@inheritDoc} 支持 REQUIRED/ALTERNATIVE/CONDITIONAL/DISABLED */
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    /** {@inheritDoc} 不允许 per-user 设置 */
    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    /** {@inheritDoc} 校验 Authorization HTTP 头中的用户名与密码 */
    @Override
    public String getHelpText() {
        return "Validates username and password from Authorization HTTP header";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    /** {@inheritDoc} 创建 {@link HttpBasicAuthenticator} 实例 */
    @Override
    public Authenticator create(final KeycloakSession session) {
        return new HttpBasicAuthenticator();
    }

    @Override
    public void init(final Config.Scope config) {

    }

    @Override
    public void postInit(final KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
