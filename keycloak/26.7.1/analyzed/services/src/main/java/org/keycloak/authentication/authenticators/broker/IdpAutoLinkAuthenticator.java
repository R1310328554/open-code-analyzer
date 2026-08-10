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

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import org.jboss.logging.Logger;

import static org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator.getExistingUser;

/**
 * 自动关联认证器：无需额外验证，直接将已检测到的现有用户写入认证上下文并完成步骤。
 * @author <a href="mailto:Ryan.Slominski@gmail.com">Ryan Slominski</a>
 */
public class IdpAutoLinkAuthenticator extends AbstractIdpAuthenticator {

    private static Logger logger = Logger.getLogger(IdpAutoLinkAuthenticator.class);

    @Override
    /** 加载重复用户、设置到上下文并标记成功。 */
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        UserModel existingUser = getExistingUser(session, realm, authSession);

        logger.debugf("User '%s' is set to authentication context when link with identity provider '%s' . Identity provider username is '%s' ", existingUser.getUsername(),
                brokerContext.getIdpConfig().getAlias(), brokerContext.getUsername());

        context.setUser(existingUser);
        context.success();
    }

    @Override
    /** 动作处理与 {@link #authenticateImpl} 相同。 */
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        authenticateImpl(context, serializedCtx, brokerContext);
    }

    @Override
    /** @return 本步骤不要求上下文中已有用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** @return 始终未针对特定用户配置 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

}
