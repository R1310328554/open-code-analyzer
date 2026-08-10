/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint;

import org.jboss.logging.Logger;

/**
 * 期望参数认证器：校验 OIDC 授权端点请求中 {@code foo} 查询参数是否与配置值匹配。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ExpectedParamAuthenticator implements Authenticator {

    /** 配置项键名：期望的查询参数值。 */
    public static final String EXPECTED_VALUE = "expected_value";

    /** 配置项键名：匹配成功后自动登录的用户名。 */
    public static final String LOGGED_USER = "logged_user";


    private static final Logger logger = Logger.getLogger(ExpectedParamAuthenticator.class);

    /**
     * 读取客户端附带的 {@code foo} 参数并与期望值比较；匹配则可选设置用户并标记成功。
     *
     * @param context 认证流程上下文
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String paramValue = context.getAuthenticationSession().getClientNote(AuthorizationEndpoint.LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX + "foo");
        String expectedValue = context.getAuthenticatorConfig().getConfig().get(EXPECTED_VALUE);
        logger.info("Value: " + paramValue + ", expectedValue: " + expectedValue);

        if (paramValue != null && paramValue.equals(expectedValue)) {

            String loggedUser = context.getAuthenticatorConfig().getConfig().get(LOGGED_USER);
            if (loggedUser == null) {
                logger.info("Successfully authenticated, but don't set any authenticated user");
            } else {
                UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), loggedUser);
                logger.info("Successfully authenticated as user " + user.getUsername());
                context.setUser(user);
            }

            context.success();
        } else {
            context.attempted();
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    /** {@inheritDoc} 不依赖已登录用户。 */
    @Override
    public boolean requiresUser() {
        return false;
    }

    /** {@inheritDoc} 对所有用户均视为已配置。 */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }


    @Override
    public void close() {

    }
}
