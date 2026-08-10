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

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.authentication.authenticators.util.AcrStore;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.protocol.mappers.oidc.OrganizationScope;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * SSO Cookie 认证器：校验身份 Cookie 实现静默登录；处理强制重新认证、步骤升级认证及组织上下文等场景。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CookieAuthenticator implements Authenticator {

    @Override
    /** @return 不依赖上下文中已有用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** 校验 SSO Cookie：成功则附加用户会话，否则标记 attempted。 */
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(context.getSession(),
                context.getRealm(), true);
        if (authResult == null) {
            context.attempted();
        } else {
            AuthenticationSessionModel authSession = context.getAuthenticationSession();
            LoginProtocol protocol = context.getSession().getProvider(LoginProtocol.class, authSession.getProtocol());
            authSession.setAuthNote(Constants.LOA_MAP, authResult.session().getNote(Constants.LOA_MAP));
            context.setUser(authResult.user());
            AcrStore acrStore = new AcrStore(context.getSession(), authSession);

            // 协议要求重新认证时跳过 Cookie 静默登录
            if (protocol.requireReauthentication(authResult.session(), authSession)) {
                // 完整重新认证，从 NO_LOA 开始
                acrStore.setLevelAuthenticatedToCurrentRequest(Constants.NO_LOA);
                authSession.setAuthNote(AuthenticationManager.FORCED_REAUTHENTICATION, "true");
                context.setForwardedInfoMessage(Messages.REAUTHENTICATE);
                context.attempted();
            } else if(AuthenticatorUtil.isForkedFlow(authSession)){
                context.attempted();
            } else {
                String topLevelFlowId = context.getTopLevelFlow().getId();
                int previouslyAuthenticatedLevel = acrStore.getHighestAuthenticatedLevelFromPreviousAuthentication(topLevelFlowId);
                AuthenticatorUtils.updateCompletedExecutions(context.getAuthenticationSession(), authResult.session(), context.getExecution().getId());

                if (acrStore.getRequestedLevelOfAuthentication(context.getTopLevelFlow()) > previouslyAuthenticatedLevel) {
                    // 步骤升级认证：保留已有 LOA，Cookie 不足需后续认证
                    acrStore.setLevelAuthenticatedToCurrentRequest(previouslyAuthenticatedLevel);

                    if (authSession.getClientNote(Constants.KC_ACTION) != null) {
                        context.setForwardedInfoMessage(Messages.AUTHENTICATE_STRONG);
                    }

                    context.attempted();
                } else {
                    // 仅 Cookie 认证成功
                    acrStore.setLevelAuthenticatedToCurrentRequest(previouslyAuthenticatedLevel);
                    authSession.setAuthNote(AuthenticationManager.SSO_AUTH, "true");
                    context.attachUserSession(authResult.session());

                    if (isOrganizationContext(context)) {
                        // 组织上下文中需先解析组织再完成认证
                        context.attempted();
                    } else {
                        context.success();
                    }
                }
            }
        }

    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

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

    /** 判断当前登录是否在组织 scope 内。 */
    private boolean isOrganizationContext(AuthenticationFlowContext context) {
        KeycloakSession session = context.getSession();

        if (Organizations.isEnabledAndOrganizationsPresent(session)) {
            return OrganizationScope.valueOfScope(session) != null;
        }

        return false;
    }
}
