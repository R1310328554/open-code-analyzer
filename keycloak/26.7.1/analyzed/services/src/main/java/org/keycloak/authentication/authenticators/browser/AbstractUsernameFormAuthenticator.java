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

import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import static org.keycloak.services.validation.Validation.FIELD_PASSWORD;
import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

/**
 * 用户名表单认证器抽象基类：提供用户名查找、密码校验、暴力破解锁定与重复用户处理等通用逻辑。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractUsernameFormAuthenticator extends AbstractFormAuthenticator {

    /** 认证会话 note：用户尝试登录时输入的用户名。 */
    public static final String ATTEMPTED_USERNAME = "ATTEMPTED_USERNAME";

    /**
     * 认证会话 note：为 true 时隐藏用户名字段；通常与 {@link #ATTEMPTED_USERNAME} 配合，允许用户重新选择用户名。
     * An authentication session not to indicate that the username field should be hidden.
     * This note is usually set together with {@link #ATTEMPTED_USERNAME} to indicated that the
     * user can restart the flow by choosing a different username.
     * It should be set by authenticators that happen before this authenticator in the flow so that the original intent
     * is kept when this authenticator is executed on subsequent requests.
     */
    /** 认证会话 note：是否隐藏用户名字段。 */
    public static final String USERNAME_HIDDEN = "USERNAME_HIDDEN";
    /** 认证会话 note：标记会话因暴力破解等原因已失效。 */
    public static final String SESSION_INVALID = "SESSION_INVALID";

    /** 认证会话 note：本步骤前上下文中已设置用户时，密码错误后不清除用户。 */
    public static final String USER_SET_BEFORE_USERNAME_PASSWORD_AUTH = "USER_SET_BEFORE_USERNAME_PASSWORD_AUTH";

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    protected Response challenge(AuthenticationFlowContext context, String error) {
        return challenge(context, error, null);
    }

    /** 构建带错误信息的用户名/密码登录挑战页。 */
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());

        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        if (Boolean.parseBoolean(authenticationSession.getAuthNote(USERNAME_HIDDEN))) {
            // 用户名隐藏时，错误显示在密码字段
            field = FIELD_PASSWORD;
        }

        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        return createLoginForm(form);
    }

    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsernamePassword();
    }

    protected String disabledByBruteForceError(String error) {
        if(Errors.USER_TEMPORARILY_DISABLED.equals(error)) {
            return Messages.ACCOUNT_TEMPORARILY_DISABLED;
        }
        return Messages.ACCOUNT_PERMANENTLY_DISABLED;
    }

    protected String disabledByBruteForceFieldError(){
        return FIELD_USERNAME;
    }

    protected Response setDuplicateUserChallenge(AuthenticationFlowContext context, String eventError, String loginFormError, AuthenticationFlowError authenticatorError) {
        context.getEvent().error(eventError);
        Response challengeResponse = context.form()
                .setError(loginFormError).createLoginUsernamePassword();
        context.failureChallenge(authenticatorError, challengeResponse);
        return challengeResponse;
    }

    public void testInvalidUser(AuthenticationFlowContext context, UserModel user) {
        if (user == null) {
            AuthenticatorUtils.dummyHash(context);
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
        }
    }

    public boolean enabledUser(AuthenticationFlowContext context, UserModel user) {
        if (isDisabledByBruteForce(context, user)) return false;
        if (!user.isEnabled()) {
            context.getEvent().user(user);
            context.getEvent().error(Errors.USER_DISABLED);
            Response challengeResponse = challenge(context, Messages.ACCOUNT_DISABLED);
            context.forceChallenge(challengeResponse);
            return false;
        }
        return true;
    }


    /** 查找用户、校验密码并确认账户可用。 */
    public boolean validateUserAndPassword(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData)  {
        UserModel user = getUser(context, inputData);
        boolean shouldClearUserFromCtxAfterBadPassword = !isUserAlreadySetBeforeUsernamePasswordAuth(context);
        return user != null && validatePassword(context, user, inputData, shouldClearUserFromCtxAfterBadPassword) && validateUser(context, user, inputData);
    }

    public boolean validateUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        UserModel user = getUser(context, inputData);
        return user != null && validateUser(context, user, inputData);
    }

    private UserModel getUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        if (isUserAlreadySetBeforeUsernamePasswordAuth(context)) {
            // 若前置步骤已设置用户，直接从上下文获取
            UserModel user = context.getUser();
            testInvalidUser(context, user);
            return user;
        } else {
            // 常规登录：由本步骤根据表单用户名建立用户身份
            context.clearUser();
            return getUserFromForm(context, inputData);
        }
    }

    private UserModel getUserFromForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (username == null || username.isEmpty()) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return null;
        }

        // 去除用户名首尾空白
        username = username.trim();

        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);

            // 联邦导入时可能发生重复
            if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
                setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS, AuthenticationFlowError.INVALID_USER);
            } else {
                setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS, AuthenticationFlowError.INVALID_USER);
            }
            return user;
        }

        testInvalidUser(context, user);
        return user;
    }

    private boolean validateUser(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData) {
        if (!enabledUser(context, user)) {
            return false;
        }
        // 用户已预设时密码表单无 rememberMe，不覆盖前置步骤保存的 note
        if (!isUserAlreadySetBeforeUsernamePasswordAuth(context)) {
            AuthenticatorUtils.processRememberMe(context, inputData);
        }
        context.setUser(user);
        return true;
    }

    public boolean validatePassword(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData, boolean clearUser) {
        String password = inputData.getFirst(CredentialRepresentation.PASSWORD);
        if (password == null || password.isEmpty()) {
            return badPasswordHandler(context, user, clearUser,true);
        }

        if (isDisabledByBruteForce(context, user)) return false;

        if (password != null && !password.isEmpty() && user.credentialManager().isValid(UserCredentialModel.password(password))) {
            context.getAuthenticationSession().setAuthNote(AuthenticationManager.PASSWORD_VALIDATED, "true");
            return true;
        } else {
            return badPasswordHandler(context, user, clearUser,false);
        }
    }

    /** 处理密码错误或为空：记录事件、设置挑战并可选清除用户。 */
    private boolean badPasswordHandler(AuthenticationFlowContext context, UserModel user, boolean clearUser,boolean isEmptyPassword) {
        context.getEvent().user(user);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);

        AuthenticatorUtils.setupReauthenticationInUsernamePasswordFormError(context);

        Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_PASSWORD);
        if(isEmptyPassword) {
            context.forceChallenge(challengeResponse);
        }else{
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
        }

        if (clearUser) {
            context.clearUser();
        }
        return false;
    }

    protected boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {
        String bruteForceError = AuthenticatorUtils.getDisabledByBruteForceEventError(context, user);
        if (bruteForceError != null) {
            context.getEvent().user(user);
            context.getEvent().error(bruteForceError);
            Response challengeResponse = challenge(context, disabledByBruteForceError(bruteForceError), disabledByBruteForceFieldError());
            context.forceChallenge(challengeResponse);
            return true;
        }
        return false;
    }

    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        return Messages.INVALID_USER;
    }

    protected boolean isUserAlreadySetBeforeUsernamePasswordAuth(AuthenticationFlowContext context) {
        String userSet = context.getAuthenticationSession().getAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
        return Boolean.parseBoolean(userSet);
    }
}
