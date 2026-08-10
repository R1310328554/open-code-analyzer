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

package org.keycloak.authentication;

import java.util.Collections;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 自定义认证器 SPI：在认证流程中校验请求、发起挑战或处理表单 action。
 * <p>实现类须同时提供 {@link AuthenticatorFactory}。</p>
 *
 * This interface is for users that want to add custom authenticators to an authentication flow.
 * You must implement this interface as well as an AuthenticatorFactory.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface Authenticator extends Provider {

    /**
     * 认证器入口：检查 HTTP 请求是否满足要求，否则通过 context.challenge 返回挑战。
     * action URL 须指向 login-actions/authenticate 或 registration 并携带 code 与 execution。
     *
     * Initial call for the authenticator.  This method should check the current HTTP request to determine if the request
     * satisfies the Authenticator's requirements.  If it doesn't, it should send back a challenge response by calling
     * the AuthenticationFlowContext.challenge(Response).  If this challenge is a authentication, the action URL
     * of the form must point to
     *
     * /realms/{realm}/login-actions/authenticate?code={session-code}&execution={executionId}
     *
     * or
     *
     * /realms/{realm}/login-actions/registration?code={session-code}&execution={executionId}
     *
     * {session-code} pertains to the code generated from AuthenticationFlowContext.generateAccessCode().  The {executionId}
     * pertains to the AuthenticationExecutionModel.getId() value obtained from AuthenticationFlowContext.getExecution().
     *
     * The action URL will invoke the action() method described below.
     *
     * @param context
     */
    void authenticate(AuthenticationFlowContext context);

    /**
     * 表单 action 回调时调用。
     *
     * Called from a form action invocation.
     *
     * @param context
     */
    void action(AuthenticationFlowContext context);


    /**
     * 是否要求用户已识别（getUser 非 null）。
     *
     * Does this authenticator require that the user has already been identified?  That AuthenticatorContext.getUser() is not null?
     *
     * @return
     */
    boolean requiresUser();

    /**
     * 该用户是否已配置此认证器所需凭证。
     *
     * Is this authenticator configured for this user.
     *
     * @param session
     * @param realm
     * @param user
     * @return
     */
    boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user);

    /**
     * 为用户注册配置此认证器所需的 Required Action。
     *
     * Set actions to configure authenticator
     *
     */
    void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user);

    /**
     * 若认证器依赖 Required Action，可覆盖返回对应工厂列表。
     *
     * Overwrite this if the authenticator is associated with
     * @return
     */
    default List<RequiredActionFactory> getRequiredActions(KeycloakSession session) {
        return Collections.emptyList();
    }

    /**
     * 检查领域是否已启用本认证器所需的全部 Required Action。
     *
     * Checks if all required actions are configured in the realm and are enabled
     * @return
     */
    default boolean areRequiredActionsEnabled(KeycloakSession session, RealmModel realm) {
        for (RequiredActionFactory raf : getRequiredActions(session)) {
            RequiredActionProviderModel rafpm = realm.getRequiredActionProviderByAlias(raf.getId());
            if (rafpm == null) {
                return false;
            }
            if (!rafpm.isEnabled()) {
                return false;
            }
        }
        return true;
    }
}
