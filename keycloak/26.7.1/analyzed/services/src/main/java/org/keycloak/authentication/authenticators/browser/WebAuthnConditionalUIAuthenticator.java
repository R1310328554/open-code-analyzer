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
package org.keycloak.authentication.authenticators.browser;

import java.util.function.Function;

import jakarta.ws.rs.core.Response;

import org.keycloak.WebAuthnConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.common.Profile;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;

/**
 * WebAuthn 条件式 UI（Conditional UI）认证器：在登录页启用 passkey 自动填充，失败时保留 passkey 选项并展示用户名/密码表单。
 *
 * @author rmartinc
 */
public class WebAuthnConditionalUIAuthenticator extends WebAuthnPasswordlessAuthenticator {

    /** 认证失败时返回的挑战响应生成函数。 */
    private final Function<AuthenticationFlowContext, Response> errorChallenge;

    /**
     * @param session 当前 Keycloak 会话
     * @param errorChallenge 失败时的挑战响应生成函数
     */
    public WebAuthnConditionalUIAuthenticator(KeycloakSession session, Function<AuthenticationFlowContext, Response> errorChallenge) {
        super(session);
        this.errorChallenge = errorChallenge;
    }

    @Override
    /** 启用 WebAuthn Conditional UI 标志后调用父类填充表单。 */
    public LoginFormsProvider fillContextForm(AuthenticationFlowContext context) {
        context.form().setAttribute(WebAuthnConstants.ENABLE_WEBAUTHN_CONDITIONAL_UI, Boolean.TRUE);
        return super.fillContextForm(context);
    }

    @Override
    /** passkey 验证失败时展示错误并保持 Conditional UI 与用户名/密码表单。 */
    protected Response createErrorResponse(AuthenticationFlowContext context, final String errorCase) {
        // passkey 验证失败：展示错误并保持 passkey 选项
        context.form().setError(errorCase, "");
        context.form().setAttribute(WebAuthnConstants.ENABLE_WEBAUTHN_CONDITIONAL_UI, Boolean.TRUE);

        AuthenticatorUtils.setupReauthenticationInUsernamePasswordFormError(context);

        fillContextForm(context);
        return errorChallenge.apply(context);
    }

    /** @return 当前会话是否启用 passkey 功能 */
    public boolean isPasskeysEnabled() {
        return isPasskeysEnabled(session);
    }

    /** @return 指定会话的领域是否启用 passkey（需 PASSKEYS 特性及策略开关） */
    static public boolean isPasskeysEnabled(KeycloakSession session) {
        return Profile.isFeatureEnabled(Profile.Feature.PASSKEYS) &&
                session.getContext().getRealm() != null &&
                Boolean.TRUE.equals(session.getContext().getRealm().getWebAuthnPolicyPasswordless().isPasskeysEnabled());
    }
}
