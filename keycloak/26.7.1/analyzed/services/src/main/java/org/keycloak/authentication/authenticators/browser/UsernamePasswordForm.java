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

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.WebAuthnConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;

import static org.keycloak.authentication.authenticators.resetcred.ResetCredentialChooseUser.RESET_CREDENTIAL_USER_CHOSEN;

/**
 * 用户名密码表单认证器，展示用户名与密码联合登录页，支持 login_hint、记住我、Passkeys 条件式 UI 及重置密码流程中的用户清除逻辑。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UsernamePasswordForm extends AbstractUsernameFormAuthenticator implements Authenticator {

    /** 条件式 UI WebAuthn/Passkeys 认证器委托（无会话构造时为 null）。 */
    protected final WebAuthnConditionalUIAuthenticator webauthnAuth;

    /** 无 WebAuthn 委托的默认构造器。 */
    public UsernamePasswordForm() {
        webauthnAuth = null;
    }

    /** @param session 当前 Keycloak 会话，用于创建 WebAuthn 条件式 UI 委托 */
    public UsernamePasswordForm(KeycloakSession session) {
        webauthnAuth = new WebAuthnConditionalUIAuthenticator(session, (context) -> createLoginForm(context.form()));
    }

    @Override
    /** 处理取消、WebAuthn 表单提交或常规用户名密码校验。 */
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        } else if (webauthnAuth != null && webauthnAuth.isPasskeysEnabled()
                && (formData.containsKey(WebAuthnConstants.AUTHENTICATOR_DATA) || formData.containsKey(WebAuthnConstants.ERROR))) {
            // WebAuthn 表单提交，委派 webauthn 认证器处理
            webauthnAuth.action(context);
            return;
        } else if (!validateForm(context, formData)) {
            // 常规用户名密码表单校验
            return;
        }
        context.success(PasswordCredentialModel.TYPE);
    }

    /** 校验用户名与密码。 */
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validateUserAndPassword(context, formData);
    }

    /** @return 当前认证会话是否已通过 Passkeys 无密码凭证认证 */
    protected boolean alreadyAuthenticatedUsingPasswordlessCredential(AuthenticationFlowContext context) {
        return alreadyAuthenticatedUsingPasswordlessCredential(context.getAuthenticationSession());
    }

    /** @return 指定认证会话是否已通过 Passkeys 无密码凭证认证 */
    protected boolean alreadyAuthenticatedUsingPasswordlessCredential(AuthenticationSessionModel authSession) {
        // 检查是否已通过 Passkeys 无密码方式完成认证
        return webauthnAuth != null && webauthnAuth.isPasskeysEnabled()
                && AuthenticatorUtil.getAuthnCredentials(authSession).contains(webauthnAuth.getCredentialType());
    }

    @Override
    /** 构建用户名密码登录挑战，处理 login_hint、记住我及 Passkeys 条件式 UI。 */
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

        clearUserIfComingFromResetPassword(context);
        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getSession());

        if (context.getUser() != null) {
            if (alreadyAuthenticatedUsingPasswordlessCredential(context)) {
                // 若已通过 Passkeys 无密码 WebAuthn 认证则直接成功
                context.success();
                return;
            }

            LoginFormsProvider form = context.form();
            form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
            form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true);
            context.getAuthenticationSession().setAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
            if (loginHint != null || rememberMeUsername != null) {
                if (loginHint != null) {
                    formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
                } else {
                    formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                    formData.add("rememberMe", "on");
                }
            }
        }
        // 启用 Passkeys 时填充 WebAuthn 表单上下文
        if (isConditionalPasskeysEnabled(context.getUser())) {
            webauthnAuth.fillContextForm(context);
        }
        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    /** 若来自重置密码流程则清除已选用户及对应 note。 */
    private void clearUserIfComingFromResetPassword(AuthenticationFlowContext context) {
        if ("true".equals(context.getAuthenticationSession().getAuthNote(RESET_CREDENTIAL_USER_CHOSEN))) {
            context.clearUser();
            context.getAuthenticationSession().removeAuthNote(RESET_CREDENTIAL_USER_CHOSEN);
        }
    }

    @Override
    /** @return 本步骤不要求上下文中已有用户 */
    public boolean requiresUser() {
        return false;
    }

    /** @return 用户名密码联合登录表单挑战响应 */
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();
        if (!formData.isEmpty()) forms.setFormData(formData);

        return forms.createLoginUsernamePassword();
    }

    @Override
    /** 展示带错误信息的挑战页，启用 Passkeys 时填充 WebAuthn 上下文。 */
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        if (isConditionalPasskeysEnabled(context.getUser())) {
            // 可能时填充 WebAuthn 表单上下文
            webauthnAuth.fillContextForm(context);
        }
        return super.challenge(context, error, field);
    }

    @Override
    /** @return 始终已配置（此方法不会被调用） */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // 不会被调用
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
    }

    @Override
    public void close() {

    }

    /** @return WebAuthn 委托可用且（无用户或用户已配置 Passkeys） */
    protected boolean isConditionalPasskeysEnabled(UserModel currentUser) {
        return webauthnAuth != null && webauthnAuth.isPasskeysEnabled() &&
                (currentUser == null || currentUser.credentialManager().isConfiguredFor(webauthnAuth.getCredentialType()));
    }

}
