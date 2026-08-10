/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authentication.authenticators.browser;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * WebAuthn 双因素认证器工厂：在启用 WEB_AUTHN 特性时注册 {@link WebAuthnAuthenticator}。
 */
public class WebAuthnAuthenticatorFactory implements AuthenticatorFactory, EnvironmentDependentProviderFactory {

    /** Provider ID：webauthn-authenticator。 */
    public static final String PROVIDER_ID = "webauthn-authenticator";

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "WebAuthn Authenticator";
    }

    @Override
    /** @return 参考类别（双因素 WebAuthn 凭证类型） */
    public String getReferenceCategory() {
        return WebAuthnCredentialModel.TYPE_TWOFACTOR;
    }

    @Override
    public boolean isConfigurable() {
        return false;
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
    /** @return 帮助说明：WebAuthn 双因素认证 */
    public String getHelpText() {
        return "Authenticator for WebAuthn. Usually used for WebAuthn two-factor authentication";
    }

   
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    /** @return 新建 {@link WebAuthnAuthenticator} 实例 */
    public Authenticator create(KeycloakSession session) {
        return new WebAuthnAuthenticator(session);
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
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 是否启用 WEB_AUTHN 特性 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WEB_AUTHN);
    }
}
