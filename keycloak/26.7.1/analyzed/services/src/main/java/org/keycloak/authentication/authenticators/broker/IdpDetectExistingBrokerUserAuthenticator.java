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

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;

import org.jboss.logging.Logger;

/**
 * 检测现有 Broker 用户认证器：要求本地已存在与 IdP 邮箱/用户名匹配的用户，不存在则返回未授权错误；存在则记录 EXISTING_USER_INFO 供后续关联步骤使用。
 */
public class IdpDetectExistingBrokerUserAuthenticator extends IdpCreateUserIfUniqueAuthenticator {

    private static final Logger logger = Logger.getLogger(IdpDetectExistingBrokerUserAuthenticator.class);

    @Override
    /** 检测重复用户：无匹配则错误页；有匹配则写入 note 并成功。 */
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        RealmModel realm = context.getRealm();

        if (context.getAuthenticationSession().getAuthNote(EXISTING_USER_INFO) != null) {
            context.attempted();
            return;
        }

        String username = getUsername(context, serializedCtx, brokerContext);
        if (username == null) {
            ServicesLogger.LOGGER.resetFlow(realm.isRegistrationEmailAsUsername() ? "Email" : "Username");
            context.getAuthenticationSession().setAuthNote(ENFORCE_UPDATE_PROFILE, "true");
            context.resetFlow();
            return;
        }

        ExistingUserInfo duplication = checkExistingUser(context, username, serializedCtx, brokerContext);

        if (duplication == null) {
            logger.errorf("The user %s should be already registered in the realm to login %s",username,  realm.getName());
            Response challengeResponse = context.form()
                    .setError(Messages.FEDERATED_IDENTITY_UNAVAILABLE, username, brokerContext.getIdpConfig().getAlias())
                    .createErrorPage(Response.Status.UNAUTHORIZED);
            context.challenge(challengeResponse);
            context.getEvent()
                    .detail("authenticator", "DetectExistingBrokerUser")
                    .removeDetail(Details.AUTH_METHOD)
                    .removeDetail(Details.AUTH_TYPE)
                    .error(Errors.USER_NOT_FOUND);

        } else {
            logger.debugf("Duplication detected. There is already existing user with %s '%s' .",
                    duplication.getDuplicateAttributeName(), duplication.getDuplicateAttributeValue());

            // 记录已存在用户，供后续认证器关联处理
            context.getAuthenticationSession().setAuthNote(EXISTING_USER_INFO, duplication.serialize());

            context.success();
        }
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
