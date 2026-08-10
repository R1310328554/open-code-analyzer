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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.credential.PasswordCredentialProviderFactory;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.DefaultActionTokenKey;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.StorageId;

import org.jboss.logging.Logger;

/**
 * 重置凭证邮件发送认证器：向用户邮箱发送含 Action Token 的密码重置链接，并统一以「邮件已发送」消息防止用户名/邮箱枚举。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResetCredentialEmail implements Authenticator, AuthenticatorFactory {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(ResetCredentialEmail.class);

    /** Provider ID：reset-credential-email。 */
    public static final String PROVIDER_ID = "reset-credential-email";
    /** 配置键：重置后是否强制重新登录。 */
    public static final String FORCE_LOGIN = "force-login";
    /** force-login 选项值：仅联邦用户强制重新登录。 */
    public static final String FEDERATED_OPTION = "only-federated";

    @Override
    /** 生成重置令牌链接并发送邮件；用户不存在或无邮箱时仍显示成功消息。 */
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        // 防止用户名枚举：无法获取用户时仍显示「邮件已发送」
        // 以成功消息结束当前分支
        if (user == null) {
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
            return;
        }

        String actionTokenUserId = authenticationSession.getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);
        if (actionTokenUserId != null && Objects.equals(user.getId(), actionTokenUserId)) {
            logger.debugf("Forget-password triggered when reauthenticating user after authentication via action token. Skipping " + PROVIDER_ID + " screen and using user '%s' ", user.getUsername());
            if (forceLogin(context.getAuthenticatorConfig(), user)) {
                // force end of auth session after the required actions
                context.getAuthenticationSession().setAuthNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS, "true");
            }
            context.success();
            return;
        }


        EventBuilder event = context.getEvent();
        // 防止用户名枚举：邮箱无效时仍显示成功消息
        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            event.user(user)
                    .detail(Details.USERNAME, username)
                    .error(Errors.INVALID_EMAIL);

            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
            return;
        }

        int validityInSecs = context.getRealm().getActionTokenGeneratedByUserLifespan(ResetCredentialsActionToken.TOKEN_TYPE);
        int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;

        // 将 Action Token 序列化后嵌入重置链接的查询参数
        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authenticationSession).getEncodedId();
        ResetCredentialsActionToken token = new ResetCredentialsActionToken(user.getId(), user.getEmail(), absoluteExpirationInSecs, authSessionEncodedId, authenticationSession.getClient().getClientId());
        String link = UriBuilder
          .fromUri(context.getActionTokenUrl(token.serialize(context.getSession(), context.getRealm(), context.getUriInfo())))
          .build()
          .toString();
        long expirationInMinutes = TimeUnit.SECONDS.toMinutes(validityInSecs);
        try {
            context.getSession().getProvider(EmailTemplateProvider.class).setRealm(context.getRealm()).setUser(user).setAuthenticationSession(authenticationSession).sendPasswordReset(link, expirationInMinutes);

            event.clone().event(EventType.SEND_RESET_PASSWORD)
                         .user(user)
                         .detail(Details.USERNAME, username)
                         .detail(Details.EMAIL, user.getEmail()).detail(Details.CODE_ID, authenticationSession.getParentSession().getId()).success();
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
        } catch (EmailException e) {
            event.clone().event(EventType.SEND_RESET_PASSWORD)
                    .detail(Details.REASON, e.getMessage())
                    .detail(Details.USERNAME, username)
                    .user(user)
                    .error(Errors.EMAIL_SEND_FAILED);
            ServicesLogger.LOGGER.failedToSendPwdResetEmail(e);
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
        }
    }

    /** @return 用户密码凭证最后修改时间戳（毫秒），无密码时返回 null */
    public static Long getLastChangedTimestamp(KeycloakSession session, RealmModel realm, UserModel user) {
        // TODO(hmlnarik)：扩展以支持非密码凭证类型
        PasswordCredentialProvider passwordProvider = (PasswordCredentialProvider) session.getProvider(CredentialProvider.class, PasswordCredentialProviderFactory.PROVIDER_ID);
        CredentialModel password = passwordProvider.getPassword(realm, user);

        return password == null ? null : password.getCreatedDate();
    }

    @Override
    /** 用户点击邮件链接后标记邮箱已验证并继续流程。 */
    public void action(AuthenticationFlowContext context) {
        context.getUser().setEmailVerified(true);
        context.success();
    }

    @Override
    /** @return 邮件发送步骤不强制前置用户（允许 null 用户防枚举） */
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
        return "Send Reset Email";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
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
    /** @return 帮助说明：向用户发送重置邮件并等待响应 */
    public String getHelpText() {
        return "Send email to user and wait for response.";
    }

    @Override
    /** @return 重置后是否强制登录的配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(FORCE_LOGIN)
                .label("Force login after reset")
                .helpText(
                        """
                        If this property is true, the user needs to login again after the reset credentials.
                        If this property is false, the user will be automatically logged in after the succesful
                        reset credentials when the same authentication session is used.
                        If this property is only-federated (default), only federated users will be forced to login again,
                        users stored in the internal database will be logged in if using the same authentication session.
                        """
                )
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString(), FEDERATED_OPTION))
                .defaultValue(FEDERATED_OPTION)
                .add()
                .build();
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

    /** 根据 force-login 配置及用户存储类型决定是否重置后强制登录。 */
    private boolean forceLogin(AuthenticatorConfigModel config, UserModel user) {
        final String forceLogin = config != null? config.getConfig().get(FORCE_LOGIN) : null;
        if (forceLogin == null || FEDERATED_OPTION.equalsIgnoreCase(forceLogin)) {
            // 默认 only-federated：仅联邦用户强制重新登录
            return !StorageId.isLocalStorage(user.getId()) || user.isFederated();
        } else if (Boolean.TRUE.toString().equalsIgnoreCase(forceLogin)) {
            return Boolean.TRUE;
        } else if (Boolean.FALSE.toString().equalsIgnoreCase(forceLogin)) {
            return Boolean.FALSE;
        } else {
            logger.warnf("Invalid value for force-login option: %s", forceLogin);
            throw new AuthenticationFlowException("Invalid value for force-login option: " + forceLogin, AuthenticationFlowError.INTERNAL_ERROR);
        }
    }
}
