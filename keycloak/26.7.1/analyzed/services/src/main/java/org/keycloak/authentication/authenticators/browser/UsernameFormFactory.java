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
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 用户名表单认证器工厂，注册仅校验用户名的浏览器表单认证器；Passkeys 启用时可选引用无密码 WebAuthn 凭证分类。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UsernameFormFactory implements AuthenticatorFactory {

    /** 提供者 ID：auth-username-form。 */
    public static final String PROVIDER_ID = "auth-username-form";

    @Override
    /** @return 新建 {@link UsernameForm} 实例 */
    public Authenticator create(KeycloakSession session) {
        return new UsernameForm(session);
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
    /** @return 认证器提供者 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 密码凭证类型引用分类 */
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    /** @return Passkeys 启用时额外引用无密码 WebAuthn 凭证分类 */
    public Set<String> getOptionalReferenceCategories(KeycloakSession session) {
        return WebAuthnConditionalUIAuthenticator.isPasskeysEnabled(session)
                ? Collections.singleton(WebAuthnCredentialModel.TYPE_PASSWORDLESS)
                : AuthenticatorFactory.super.getOptionalReferenceCategories(session);
    }

    @Override
    /** @return 是否可配置（用户名表单不可配置） */
    public boolean isConfigurable() {
        return false;
    }

    /** 允许的执行要求：仅 REQUIRED。 */
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    /** @return 允许的执行要求选项 */
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Username Form";
    }

    @Override
    /** @return 认证器帮助说明文本 */
    public String getHelpText() {
        return "Selects a user from his username.";
    }

    @Override
    /** @return 可配置属性列表（本认证器无配置项） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    /** @return 是否允许用户自助配置 */
    public boolean isUserSetupAllowed() {
        return false;
    }

}
