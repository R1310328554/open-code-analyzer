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

package org.keycloak.authentication.authenticators.browser;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

/**
 * 身份提供方重定向认证器工厂：注册 {@link IdentityProviderAuthenticator}，可配置默认 IdP alias。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class IdentityProviderAuthenticatorFactory implements AuthenticatorFactory {
    protected static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.ALTERNATIVE, AuthenticationExecutionModel.Requirement.DISABLED
    };

    /** Provider ID：identity-provider-redirector。 */
    public static final String PROVIDER_ID = "identity-provider-redirector";
    /** 配置项：默认身份提供方 alias。 */
    public static final String DEFAULT_PROVIDER = "defaultProvider";

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Identity Provider Redirector";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    /** @return 帮助说明：重定向到默认 IdP 或 kc_idp_hint 指定的 IdP */
    public String getHelpText() {
        return "Redirects to default Identity Provider or Identity Provider specified with kc_idp_hint query parameter";
    }

    @Override
    /** @return 默认 IdP alias 配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty rep = new ProviderConfigProperty(DEFAULT_PROVIDER, "Default Identity Provider", "To automatically redirect to an identity provider set to the alias of the identity provider", STRING_TYPE, null);
        return Collections.singletonList(rep);
    }

    @Override
    /** @return 新建 {@link IdentityProviderAuthenticator} 实例 */
    public Authenticator create(KeycloakSession session) {
        return new IdentityProviderAuthenticator();
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

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
