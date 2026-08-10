/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.authentication.authenticators.browser;

import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.WebAuthnConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.authentication.requiredactions.WebAuthnPasswordlessRegisterFactory;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.WebAuthnPasswordlessCredentialProvider;
import org.keycloak.credential.WebAuthnPasswordlessCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.WebAuthnPolicy;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

/**
 * Authenticator for WebAuthn authentication with passwordless credential. This class is temporary and will be likely
 * removed in the future during future improvements in authentication SPI
 */
public class WebAuthnPasswordlessAuthenticator extends WebAuthnAuthenticator {

    /** @param session 当前 Keycloak 会话 */
    public WebAuthnPasswordlessAuthenticator(KeycloakSession session) {
        super(session);
    }

    @Override
    /** @return 领域无密码 WebAuthn 策略 */
    protected WebAuthnPolicy getWebAuthnPolicy(AuthenticationFlowContext context) {
        return context.getRealm().getWebAuthnPolicyPasswordless();
    }

    @Override
    /** @return 凭证类型（passwordless） */
    protected String getCredentialType() {
        return WebAuthnCredentialModel.TYPE_PASSWORDLESS;
    }

    @Override
    /** @return 无密码场景不展示已注册认证器列表 */
    protected boolean shouldDisplayAuthenticators(AuthenticationFlowContext context){
        return false;
    }

    @Override
    /** 若用户未注册无密码 WebAuthn 凭证，添加注册必需操作。 */
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // ask the user to do required action to register webauthn authenticator
        AuthenticationSessionModel authenticationSession = session.getContext().getAuthenticationSession();
        if (!authenticationSession.getRequiredActions().contains(WebAuthnPasswordlessRegisterFactory.PROVIDER_ID)) {
            authenticationSession.addRequiredAction(WebAuthnPasswordlessRegisterFactory.PROVIDER_ID);
        }
    }

    @Override
    /** @return 无密码 WebAuthn 注册必需操作工厂列表 */
    public List<RequiredActionFactory> getRequiredActions(KeycloakSession session) {
        return Collections.singletonList((WebAuthnPasswordlessRegisterFactory)session.getKeycloakSessionFactory().getProviderFactory(RequiredActionProvider.class, WebAuthnPasswordlessRegisterFactory.PROVIDER_ID));
    }


    @Override
    /** @return 无密码 WebAuthn 凭证 Provider */
    public WebAuthnPasswordlessCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (WebAuthnPasswordlessCredentialProvider)session.getProvider(CredentialProvider.class, WebAuthnPasswordlessCredentialProviderFactory.PROVIDER_ID);
    }

    @Override
    /** @return 无密码场景不要求前置用户识别 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** 处理取消、用户名输入或 WebAuthn 凭证选择，再委托父类完成验证。 */
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

        String username = formData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (StringUtil.isNotBlank(username)) {
            // 用户直接输入用户名，校验是否存在
            boolean validUsername = validateUsername(context, formData, username);
            if (!validUsername) {
                context.attempted();
                return;
            }
        } else if (!formData.containsKey(WebAuthnConstants.USER_HANDLE)) {
            // 空表单且无 WebAuthn 凭证选择
            context.attempted();
            return;
        }

        // 处理「记住我」选项
        if (formData.containsKey("rememberMe")) {
            AuthenticatorUtils.processRememberMe(context, formData);
        }

        // 用户选择了 WebAuthn 凭证，继续 WebAuthn 认证
        super.action(context);
    }

    /** 校验用户输入的用户名是否存在。 */
    protected boolean validateUsername(AuthenticationFlowContext context, MultivaluedMap<String, String> formData, String username) {
        return new UsernameForm().validateUser(context, formData);
    }

}
