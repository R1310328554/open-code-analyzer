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

package org.keycloak.authentication.authenticators.resetcred;

import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.DefaultActionTokenKey;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import org.jboss.logging.Logger;

/**
 * 重置凭证「选择用户」认证器：展示密码重置表单让用户输入用户名，或在 SSO/Action Token/IdP 再认证场景下自动预选用户。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResetCredentialChooseUser implements Authenticator, AuthenticatorFactory {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(ResetCredentialChooseUser.class);

    /** Provider ID：reset-credentials-choose-user。 */
    public static final String PROVIDER_ID = "reset-credentials-choose-user";
    /** 认证会话备注键：标记用户已成功选定。 */
    public static final String RESET_CREDENTIAL_USER_CHOSEN = "RESET_CREDENTIAL_USER_CHOSEN";

    @Override
    /** 根据 IdP/Action Token/SSO 上下文展示或跳过用户选择表单。 */
    public void authenticate(AuthenticationFlowContext context) {
        String existingUserId = context.getAuthenticationSession().getAuthNote(AbstractIdpAuthenticator.EXISTING_USER_INFO);
        if (existingUserId != null) {
            UserModel existingUser = AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession());

            logger.debugf("Forget-password triggered when reauthenticating user after first broker login. Prefilling reset-credential-choose-user screen with user '%s' ", existingUser.getUsername());
            context.setUser(existingUser);
            Response challenge = context.form().createPasswordReset();
            context.challenge(challenge);
            return;
        }

        String actionTokenUserId = context.getAuthenticationSession().getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);
        if (actionTokenUserId != null) {
            UserModel existingUser = context.getSession().users().getUserById(context.getRealm(), actionTokenUserId);

            // Action token logics handles checks for user ID validity and user being enabled

            logger.debugf("Forget-password triggered when reauthenticating user after authentication via action token. Skipping reset-credential-choose-user screen and using user '%s' ", existingUser.getUsername());
            context.setUser(existingUser);
            context.success();
            return;
        }

        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(context.getSession(), context.getRealm(), true);
        // 存在 SSO 会话时跳过用户选择
        if (authResult != null) {
            context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, authResult.user().getUsername());
            context.setUser(authResult.user());
            context.success();
            return;
        }

        Response challenge = context.form().createPasswordReset();
        context.challenge(challenge);
    }

    @Override
    /** 处理用户提交的用户名，查找用户但不泄露账户是否存在。 */
    public void action(AuthenticationFlowContext context) {
        EventBuilder event = context.getEvent();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = formData.getFirst("username");
        if (username == null || username.isEmpty()) {
            event.error(Errors.USERNAME_MISSING);
            Response challenge = context.form()
                    .addError(new FormMessage(Validation.FIELD_USERNAME, Messages.MISSING_USERNAME))
                    .createPasswordReset();
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        username = username.trim();

        RealmModel realm = context.getRealm();
        UserModel user = context.getSession().users().getUserByUsername(realm, username);
        if (user == null && realm.isLoginWithEmailAllowed() && username.contains("@")) {
            user =  context.getSession().users().getUserByEmail(realm, username);
        }

        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        // 防止用户名枚举：出错时继续流程但不设置用户
        // null 用户会通知后续执行步骤本次选择失败
        if (user == null) {
            event.clone()
                    .detail(Details.USERNAME, username)
                    .error(Errors.USER_NOT_FOUND);
            context.clearUser();
        } else if (!user.isEnabled()) {
            event.clone()
                    .detail(Details.USERNAME, username)
                    .user(user).error(Errors.USER_DISABLED);
            context.clearUser();
        } else {
            context.getAuthenticationSession().setAuthNote(RESET_CREDENTIAL_USER_CHOSEN, "true");
            context.setUser(user);
        }

        context.success();
    }

    @Override
    /** @return 本步骤负责识别用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Choose User";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    /** @return 帮助说明：选择要重置凭证的用户 */
    public String getHelpText() {
        return "Choose a user to reset credentials for";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }
}
