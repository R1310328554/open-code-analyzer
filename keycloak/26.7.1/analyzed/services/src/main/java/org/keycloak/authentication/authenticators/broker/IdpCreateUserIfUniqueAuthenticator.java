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

package org.keycloak.authentication.authenticators.broker;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.light.LightweightUserAdapter;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;

import org.jboss.logging.Logger;

import static org.keycloak.authentication.actiontoken.idpverifyemail.IdpVerifyAccountLinkActionTokenHandler.runIfUserVerified;
import static org.keycloak.broker.provider.AbstractIdentityProvider.BROKER_REGISTERED_NEW_USER;

/**
 * 唯一时创建用户认证器：检测邮箱/用户名是否重复，无重复则创建本地用户（或临时用户）并写入 IdP 属性；有重复则记录供后续步骤处理。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdpCreateUserIfUniqueAuthenticator extends AbstractIdpAuthenticator {

    private static Logger logger = Logger.getLogger(IdpCreateUserIfUniqueAuthenticator.class);


    @Override
    /** 本认证器无表单动作，空实现。 */
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
    }

    @Override
    /** 检测重复、创建用户或记录 EXISTING_USER_INFO 并展示冲突错误。 */
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        if (context.getAuthenticationSession().getAuthNote(EXISTING_USER_INFO) != null) {
            context.attempted();
            return;
        }

        String username = getUsername(context, serializedCtx, brokerContext);
        if (username == null || username.trim().isEmpty()) {
            ServicesLogger.LOGGER.resetFlow(realm.isRegistrationEmailAsUsername() ? "Email" : "Username");
            context.getAuthenticationSession().setAuthNote(ENFORCE_UPDATE_PROFILE, "true");
            context.resetFlow();
            return;
        }

        IdentityProviderModel broker = brokerContext.getIdpConfig();
        ExistingUserInfo duplication = broker.isTransientUsers() ? null : checkExistingUser(context, username, serializedCtx, brokerContext);

        UserModel federatedUser = null;
        if (broker.isTransientUsers()) {
            logger.debugf("Transient brokering requested. Recording user details for account '%s' and from identity provider '%s' .",
                    username, broker.getAlias());

            federatedUser = new LightweightUserAdapter(session, context.getAuthenticationSession().getParentSession().getId());
            federatedUser.setUsername(username);
        } else if (duplication == null) {
            logger.debugf("No duplication detected. Creating account for user '%s' and linking with identity provider '%s' .",
                    username, broker.getAlias());

            federatedUser = session.users().addUser(realm, username);
        }

        if (federatedUser != null) {
            federatedUser.setEnabled(true);

            for (Map.Entry<String, List<String>> attr : serializedCtx.getAttributes().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                if (!UserModel.USERNAME.equalsIgnoreCase(attr.getKey())) {
                    federatedUser.setAttribute(attr.getKey(), attr.getValue());
                }
            }

            AuthenticatorConfigModel config = context.getAuthenticatorConfig();
            if (config != null && Boolean.parseBoolean(config.getConfig().get(IdpCreateUserIfUniqueAuthenticatorFactory.REQUIRE_PASSWORD_UPDATE_AFTER_REGISTRATION))) {
                logger.debugf("User '%s' required to update password", federatedUser.getUsername());
                federatedUser.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            }

            userRegisteredSuccess(context, federatedUser, serializedCtx, brokerContext);

            context.setUser(federatedUser);
            context.getAuthenticationSession().setAuthNote(BROKER_REGISTERED_NEW_USER, "true");
            context.success();
        } else if (duplication != null) {
            UserModel user = session.users().getUserById(realm, duplication.getExistingUserId());

            if (runIfUserVerified(session, user, broker, brokerContext.getBrokerUserId(),
                    () -> {
                        context.setUser(user);
                        context.success();
                    })) {
                return;
            }

            logger.debugf("Duplication detected. There is already existing user with %s '%s' .",
                    duplication.getDuplicateAttributeName(), duplication.getDuplicateAttributeValue());

            // 记录重复用户信息，供后续认证器处理
            context.getAuthenticationSession().setAuthNote(EXISTING_USER_INFO, duplication.serialize());
            // 仅当执行项为 REQUIRED 时才向用户展示错误页
            if (context.getExecution().isRequired()) {
                Response challengeResponse = context.form()
                        .setError(Messages.FEDERATED_IDENTITY_EXISTS, duplication.getDuplicateAttributeName(), duplication.getDuplicateAttributeValue())
                        .createErrorPage(Response.Status.CONFLICT);
                context.challenge(challengeResponse);
                context.getEvent()
                        .user(duplication.getExistingUserId())
                        .detail("existing_" + duplication.getDuplicateAttributeName(), duplication.getDuplicateAttributeValue())
                        .removeDetail(Details.AUTH_METHOD)
                        .removeDetail(Details.AUTH_TYPE)
                        .error(Errors.FEDERATED_IDENTITY_EXISTS);
                return;
            }

            context.attempted();
        }
    }

    /** 按邮箱（若不允许重复）和用户名检测是否已存在本地用户，子类可覆盖扩展规则。 */
    // Could be overriden to detect duplication based on other criterias (firstName, lastName, ...)
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        if (brokerContext.getEmail() != null && !context.getRealm().isDuplicateEmailsAllowed()) {
            UserModel existingUser = context.getSession().users().getUserByEmail(context.getRealm(), brokerContext.getEmail());
            if (existingUser != null) {
                return new ExistingUserInfo(existingUser.getId(), UserModel.EMAIL, existingUser.getEmail());
            }
        }

        UserModel existingUser = context.getSession().users().getUserByUsername(context.getRealm(), username);
        if (existingUser != null) {
            return new ExistingUserInfo(existingUser.getId(), UserModel.USERNAME, existingUser.getUsername());
        }

        return null;
    }

    /** 根据领域配置返回用于注册的用户名（邮箱或 model 用户名）。 */
    protected String getUsername(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        RealmModel realm = context.getRealm();
        return realm.isRegistrationEmailAsUsername() ? brokerContext.getEmail() : brokerContext.getModelUsername();
    }


    /** 新用户通过社交/IdP 注册成功后的回调钩子，子类可覆盖。 */
    // Empty method by default. This exists, so subclass can override and add callback after new user is registered through social
    protected void userRegisteredSuccess(AuthenticationFlowContext context, UserModel registeredUser, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

    }


    @Override
    /** @return 本步骤不要求上下文中已有用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** @return 始终已配置（对所有用户适用） */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

}
