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

package org.keycloak.organization.authentication.authenticators.browser;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.IdentityProviderAuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.WebAuthnConditionalUIAuthenticator;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

/**
 * 组织身份优先登录认证器的 {@link AuthenticatorFactory}，工厂 ID 为 {@code organization}。
 * <p>创建 {@link OrganizationAuthenticator}，在组织功能启用时按域名/成员关系自动重定向 IdP；可通过 {@link #REQUIRES_USER_MEMBERSHIP} 强制用户必须是组织成员。</p>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class OrganizationAuthenticatorFactory extends IdentityProviderAuthenticatorFactory implements EnvironmentDependentProviderFactory {

    /** 工厂标识 {@code organization}。 */
    public static final String ID = "organization";
    /** 配置键：是否要求用户必须是组织成员。 */
    public static final String REQUIRES_USER_MEMBERSHIP = "requiresUserMembership";

    @Override
    /** @return 工厂 ID {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Organization Identity-First Login";
    }

    @Override
    /** @return 管理控制台帮助文本 */
    public String getHelpText() {
        return "If organizations are enabled, automatically redirects users to the corresponding identity provider.";
    }

    @Override
    /** 创建 {@link OrganizationAuthenticator} 实例。 */
    public Authenticator create(KeycloakSession session) {
        return new OrganizationAuthenticator(session);
    }

    @Override
    /** @return 组织特性启用时可用 */
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.ORGANIZATION);
    }

    @Override
    /** @return 认证器配置属性（含 requiresUserMembership） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.singletonList(new ProviderConfigProperty(REQUIRES_USER_MEMBERSHIP, "Requires user membership", "Enforces that users authenticating in the scope of an organization are members. If not a member, the user won't be able to proceed authenticating to the realm", BOOLEAN_TYPE, null));
    }

    @Override
    /** @return 可选参考类别（启用通行密钥时包含无密码 WebAuthn） */
    public Set<String> getOptionalReferenceCategories(KeycloakSession session) {
        return WebAuthnConditionalUIAuthenticator.isPasskeysEnabled(session)
                ? Collections.singleton(WebAuthnCredentialModel.TYPE_PASSWORDLESS)
                : super.getOptionalReferenceCategories(session);
    }
}
