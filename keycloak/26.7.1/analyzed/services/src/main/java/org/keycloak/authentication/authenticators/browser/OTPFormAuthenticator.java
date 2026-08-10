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

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.requiredactions.UpdateTotp;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.OTPCredentialProvider;
import org.keycloak.credential.OTPCredentialProviderFactory;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * OTP 表单认证器：展示 TOTP 输入页并校验一次性密码，继承 {@link AbstractUsernameFormAuthenticator} 的用户状态处理。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OTPFormAuthenticator extends AbstractUsernameFormAuthenticator implements Authenticator, CredentialValidator<OTPCredentialProvider> {

    /** Freemarker 属性名：当前选中的 OTP 凭证 ID。 */
    public static final String SELECTED_OTP_CREDENTIAL_ID = "selectedOtpCredentialId";

    /** 无 userLabel 的 OTP 凭证在 UI 中显示的占位标签。 */
    public static final String UNNAMED = "unnamed";


    @Override
    /** 处理 OTP 表单提交。 */
    public void action(AuthenticationFlowContext context) {
        validateOTP(context);
    }


    @Override
    /** 展示 OTP 输入挑战页。 */
    public void authenticate(AuthenticationFlowContext context) {
        Response challengeResponse = challenge(context, null);
        context.challenge(challengeResponse);
    }


    /** 校验用户提交的 TOTP；成功则标记 OTP 凭证类型并 success。 */
    public void validateOTP(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();

        String otp = inputData.getFirst("otp");

        String credentialId = inputData.getFirst("selectedCredentialId");

        if (credentialId == null || credentialId.isEmpty()) {
            OTPCredentialModel defaultOtpCredential = getCredentialProvider(context.getSession())
                    .getDefaultCredential(context.getSession(), context.getRealm(), context.getUser());
            credentialId = defaultOtpCredential==null ? "" : defaultOtpCredential.getId();
        }
        context.getEvent().detail(Details.SELECTED_CREDENTIAL_ID, credentialId);

        context.form().setAttribute(SELECTED_OTP_CREDENTIAL_ID, credentialId);

        UserModel userModel = context.getUser();
        boolean userEnabled = enabledUser(context, userModel);
        // 暴力破解锁定可能已解除，需清除会话失效 note
        if (userEnabled) {
            context.getAuthenticationSession().removeAuthNote(AbstractUsernameFormAuthenticator.SESSION_INVALID);
        }
        if("true".equals(context.getAuthenticationSession().getAuthNote(AbstractUsernameFormAuthenticator.SESSION_INVALID))) {
            context.getEvent().user(context.getUser()).error(Errors.INVALID_AUTHENTICATION_SESSION);
            // challenge already set by calling enabledUser() above
            return;
        }
        if (!userEnabled) {
            // 错误已在 enabledUser/isDisabledByBruteForce 中设置
            context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.SESSION_INVALID, "true");
            return;
        }

        if (otp == null) {
            Response challengeResponse = challenge(context,null);
            context.challenge(challengeResponse);
            return;
        }
        boolean valid = context.getUser().credentialManager().isValid(new UserCredentialModel(credentialId, getCredentialProvider(context.getSession()).getType(), otp));
        if (!valid) {
            context.getEvent().user(userModel)
                    .error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = challenge(context, Messages.INVALID_TOTP, Validation.FIELD_OTP_CODE);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
            return;
        }
        context.success(OTPCredentialModel.TYPE);
    }

    @Override
    /** @return 需要上下文中已有用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    protected String disabledByBruteForceError(String error) {
        if(Errors.USER_TEMPORARILY_DISABLED.equals(error)) {
            return Messages.ACCOUNT_TEMPORARILY_DISABLED_TOTP;
        }
        return Messages.ACCOUNT_PERMANENTLY_DISABLED_TOTP;
    }

    @Override
    protected String disabledByBruteForceFieldError() {
        return Validation.FIELD_OTP_CODE;
    }

    @Override
    /** @return TOTP 登录表单响应 */
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginTotp();
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().isConfiguredFor(getCredentialProvider(session).getType());
    }

    @Override
    /** 若用户未配置 TOTP，添加 CONFIGURE_TOTP 必需操作。 */
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        AuthenticationSessionModel authenticationSession = session.getContext().getAuthenticationSession();
        if (!authenticationSession.getRequiredActions().contains(UserModel.RequiredAction.CONFIGURE_TOTP.name())) {
            authenticationSession.addRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP);
        }
    }

    public List<RequiredActionFactory> getRequiredActions(KeycloakSession session) {
        return Collections.singletonList((UpdateTotp)session.getKeycloakSessionFactory().getProviderFactory(RequiredActionProvider.class, UserModel.RequiredAction.CONFIGURE_TOTP.name()));
    }

    @Override
    public void close() {

    }

    @Override
    /** @return OTP 凭证 Provider */
    public OTPCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (OTPCredentialProvider)session.getProvider(CredentialProvider.class, OTPCredentialProviderFactory.PROVIDER_ID);
    }

}
