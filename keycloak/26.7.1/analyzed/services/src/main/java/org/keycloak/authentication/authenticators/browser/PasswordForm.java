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

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

/**
 * 密码表单认证器，在用户名已确定后单独展示密码输入页并校验密码凭证；支持 Passkeys 条件式 UI 与已用无密码凭证认证时的跳过逻辑。
 */
public class PasswordForm extends UsernamePasswordForm implements CredentialValidator<PasswordCredentialProvider> {

    /** @param session 当前 Keycloak 会话 */
    public PasswordForm(KeycloakSession session) {
        super(session);
    }

    @Override
    /** 仅校验密码字段（用户名已由前置步骤确定）。 */
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validatePassword(context, context.getUser(), formData, false);
    }

    @Override
    /** 若已用无密码凭证认证则直接成功，否则展示密码登录表单。 */
    public void authenticate(AuthenticationFlowContext context) {
        if (alreadyAuthenticatedUsingPasswordlessCredential(context)) {
            context.success();
            return;
        }

        // 启用 Passkeys 时填充 WebAuthn 表单上下文
        if (isConditionalPasskeysEnabled(context.getUser())) {
            webauthnAuth.fillContextForm(context);
        }

        Response challengeResponse = context.form().createLoginPassword();
        context.challenge(challengeResponse);
    }

    @Override
    /** @return 用户是否已配置密码、Passkeys 或已通过无密码凭证认证 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().isConfiguredFor(getCredentialProvider(session).getType())
                || (isConditionalPasskeysEnabled(user))
                || alreadyAuthenticatedUsingPasswordlessCredential(session.getContext().getAuthenticationSession());
    }

    @Override
    /** @return 本步骤要求上下文中已有用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    /** @return 仅含密码字段的登录表单 */
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginPassword();
    }

    @Override
    /** @return 密码校验失败时的默认错误消息键 */
    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        return Messages.INVALID_PASSWORD;
    }

    @Override
    /** @return 密码凭证提供者 */
    public PasswordCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (PasswordCredentialProvider)session.getProvider(CredentialProvider.class, "keycloak-password");
    }
}
