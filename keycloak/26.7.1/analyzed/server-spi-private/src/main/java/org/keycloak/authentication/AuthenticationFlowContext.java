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

import java.net.URI;
import java.util.List;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 认证流程执行上下文：封装当前用户、认证会话、表单 URL 及流程控制（重置/分叉/取消）。
 *
 * This interface encapsulates information about an execution in an AuthenticationFlow.  It is also used to set
 * the status of the execution being performed.
 *
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AuthenticationFlowContext extends AbstractAuthenticationFlowContext {

    /**
     * 当前流程关联的用户，尚未识别时为 null。
     *
     * Current user attached to this flow.  It can return null if no user has been identified yet
     *
     * @return
     */
    UserModel getUser();

    /**
     * 将指定用户绑定到本流程。
     *
     * Attach a specific user to this flow.
     *
     * @param user
     */
    void setUser(UserModel user);

    /** 获取可选认证方式列表（如 WebAuthn、OTP）。 */
    List<AuthenticationSelectionOption> getAuthenticationSelections();

    /** 设置可选认证执行映射。 */
    void setAuthenticationSelections(List<AuthenticationSelectionOption>  credentialAuthExecMap);

    /**
     * 清除流程中的用户绑定。
     *
     * Clear the user from the flow.
     */
    void clearUser();

    /** 关联已建立的用户会话。 */
    void attachUserSession(UserSessionModel userSession);


    /**
     * 本流程关联的认证会话。
     *
     * AuthenticationSessionModel attached to this flow
     *
     * @return
     */
    AuthenticationSessionModel getAuthenticationSession();

    /**
     * 当前流程路径（如 authenticate、reset-credentials）。
     *
     * @return current flow path (EG. authenticate, reset-credentials)
     */
    String getFlowPath();

    /**
     * 创建预置用户、action URI 与 access code 的登录表单构建器。
     *
     * Create a Freemarker form builder that presets the user, action URI, and a generated access code
     *
     * @return
     */
    LoginFormsProvider form();

    /**
     * 获取必需动作的 action URL。
     *
     * Get the action URL for the required action.
     *
     * @param code authentication session access code
     * @return
     */
    URI getActionUrl(String code);

    /**
     * 获取 action token 执行器的 action URL。
     *
     * Get the action URL for the action token executor.
     *
     * @param tokenString String representation (JWT) of action token
     * @return
     */
    URI getActionTokenUrl(String tokenString);

    /**
     * 获取必需动作的刷新 URL。
     *
     * Get the refresh URL for the required action.
     *
     * @return
     */
    URI getRefreshExecutionUrl();

    /**
     * 获取流程刷新 URL。
     *
     * Get the refresh URL for the flow.
     *
     * @param authSessionIdParam will include auth_session query param for clients that don't process cookies
     * @return
     */
    URI getRefreshUrl(boolean authSessionIdParam);

    /**
     * 结束流程并按协议重定向浏览器（仅浏览器流程）。
     *
     * End the flow and redirect browser based on protocol specific response.  This should only be executed
     * in browser-based flows.
     *
     */
    void cancelLogin();

    /**
     * 重置流程到开头并重新开始。
     *
     * Reset the current flow to the beginning and restarts it.
     *
     */
    void resetFlow();

    /**
     * 重置流程并在重启后执行额外监听器。
     *
     * Reset the current flow to the beginning and restarts it. Allows to add additional listener, which is triggered after flow restarted
     *
     */
    void resetFlow(Runnable afterResetListener);

    /**
     * 分叉当前流程：克隆认证会话并指向浏览器登录流程，原执行点保持不变（如重置密码发邮件场景）。
     *
     * Fork the current flow.  The authentication session will be cloned and set to point at the realm's browser login flow.  The Response will be the result
     * of this fork.  The previous flow will still be set at the current execution.  This is used by reset password when it sends an email.
     * It sends an email linking to the current flow and redirects the browser to a new browser login flow.
     *
     *
     *
     * @return
     */
    void fork();

    /**
     * Fork the current flow.  The authentication session will be cloned and set to point at the realm's browser login flow.  The Response will be the result
     * of this fork.  The previous flow will still be set at the current execution.  This is used by reset password when it sends an email.
     * It sends an email linking to the current flow and redirects the browser to a new browser login flow.
     *
     * 分叉并在新流程首页展示成功消息。
     *
     * This method will set up a success message that will be displayed in the first page of the new flow
     *
     * @param message Corresponds to raw text or a message property defined in a message bundle
     */
    void forkWithSuccessMessage(FormMessage message);
    /**
     * Fork the current flow.  The authentication session will be cloned and set to point at the realm's browser login flow.  The Response will be the result
     * of this fork.  The previous flow will still be set at the current execution.  This is used by reset password when it sends an email.
     * It sends an email linking to the current flow and redirects the browser to a new browser login flow.
     *
     * 分叉并在新流程首页展示错误消息。
     *
     * This method will set up an error message that will be displayed in the first page of the new flow
     *
     * @param message Corresponds to raw text or a message property defined in a message bundle
     */
    void forkWithErrorMessage(FormMessage message);
}
