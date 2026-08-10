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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.managers.BruteForceProtector;

/**
 * 认证流程执行的抽象上下文：提供领域、会话、执行状态及成功/失败/挑战等控制方法。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AbstractAuthenticationFlowContext {

    /**
     * 当前使用的事件构建器。
     *
     * Current event builder being used
     *
     * @return
     */
    /** 当前事件构建器。 */
    EventBuilder getEvent();

    /**
     * 创建新的 EventBuilder 供本上下文使用。
     *
     * Create a refresh new EventBuilder to use within this context
     *
     * @return
     */
    EventBuilder newEvent();

    /**
     * 流程中的当前认证执行步骤。
     *
     * The current execution in the flow
     *
     * @return
     */
    AuthenticationExecutionModel getExecution();

    /**
     * 返回本次认证的顶层（根）流程。
     *
     * @return the top level flow (root flow) of this authentication
     */
    AuthenticationFlowModel getTopLevelFlow();

    /**
     * 当前领域。
     *
     * Current realm
     *
     * @return
     */
    RealmModel getRealm();

    /**
     * 连接 HTTP 客户端的 IP 等信息。
     *
     * Information about the IP address from the connecting HTTP client.
     *
     * @return
     */
    ClientConnection getConnection();

    /**
     * 当前请求的 UriInfo。
     *
     * UriInfo of the current request
     *
     * @return
     */
    UriInfo getUriInfo();

    /**
     * 当前 Keycloak 会话。
     *
     * Current session
     *
     * @return
     */
    KeycloakSession getSession();

    /** 当前 HTTP 请求。 */
    HttpRequest getHttpRequest();
    /** 暴力破解防护器。 */
    BruteForceProtector getProtector();


    /**
     * 获取当前执行步骤关联的认证器配置。
     *
     * Get any configuration associated with the current execution
     *
     * @return
     */
    AuthenticatorConfigModel getAuthenticatorConfig();

    /**
     * 由其他认证器在重启或继续流程时转发的错误消息（如 IdP 失败后本地继续登录）。
     *
     * This could be an error message forwarded from another authenticator that is restarting or continuing the flo.  For example
     * the brokering API sends this when the broker failed authentication
     * and we want to continue authentication locally.  forwardedErrorMessage can then be displayed by
     * whatever form is challenging.
     */
    FormMessage getForwardedErrorMessage();

    /**
     * 由其他认证器转发的成功消息（如重置密码邮件发送后重启流程）。
     *
     * This could be an success message forwarded from another authenticator that is restarting or continuing the flow.  For example
     * a reset password sends an email, then resets the flow with a success message.  forwardedSuccessMessage can then be displayed by
     * whatever form is challenging.
     */
    FormMessage getForwardedSuccessMessage();

    /**
     * 由其他认证器转发的提示消息，通常仅在认证首个展示页显示一次。
     *
     * This could be an info message forwarded from another authenticator. This info message will be usually displayed only once on the
     * first screen shown to the user during authentication. The authenticator forwarding the info message does not know which the screen would be.
     * For example during user re-authentication, the user should see info message like "Please re-authenticate", but at the beginning of the
     * authentication, it is not 100% clear which screen will be the first shown screen where this message should be displayed
     */
    FormMessage getForwardedInfoMessage();

    /**
     * @see #getForwardedInfoMessage()
     * @param message to be forwarded
     * @param parameters parameters of the message if any
     */
    void setForwardedInfoMessage(String message, Object... parameters);

    /**
     * 生成访问码并更新客户端会话时间戳；表单 action 须携带此 code。
     *
     * Generates access code and updates clientsession timestamp
     * Access codes must be included in form action callbacks as a query parameter.
     *
     * @return
     */
    String generateAccessCode();


    /** 从当前流程获取指定认证器类别的执行要求（REQUIRED/ALTERNATIVE/OPTIONAL）。 */
    AuthenticationExecutionModel.Requirement getCategoryRequirementFromCurrentFlow(String authenticatorCategory);

    /**
     * 标记当前执行成功，流程将继续。
     *
     * Mark the current execution as successful.  The flow will then continue
     *
     */
    void success();

    /**
     * 标记成功并记录本次使用的凭证类型到认证会话。
     *
     * Mark the current execution as successful and the auth session sets the
     * credential type in the authentication session as the last credential used
     * to authenticate the user.
     *
     * @param credentialType The credential used to authenticate the user
     */
    void success(String credentialType);

    /**
     * 中止当前流程。
     *
     * Aborts the current flow
     *
     * @param error
     */
    void failure(AuthenticationFlowError error);

    /**
     * 中止流程并返回指定 HTTP 响应。
     *
     * Aborts the current flow.
     *
     * @param error
     * @param response Response that will be sent back to HTTP client
     */
    void failure(AuthenticationFlowError error, Response response);
    
    /**
     * Aborts the current flow.
     *
     * @param error
     * @param response Response that will be sent back to HTTP client
     * @param eventDetails 错误事件详情
     * @param userErrorMessage 展示给用户的错误消息
     * @param userErrorMessage A message describing the error to the user
     */
    void failure(AuthenticationFlowError error, Response response, String eventDetails, String userErrorMessage);

    /**
     * 向客户端发送挑战响应；optional 时不发送，alternative 时仅在其他分支未成功时发送。
     *
     * Sends a challenge response back to the HTTP client.  If the current execution requirement is optional, this response will not be
     * sent.  If the current execution requirement is alternative, then this challenge will be sent if no other alternative
     * execution was successful.
     *
     * @param challenge
     */
    void challenge(Response challenge);

    /**
     * 无视执行要求强制发送挑战响应。
     *
     * Sends the challenge back to the HTTP client regardless of the current execution requirement
     *
     * @param challenge
     */
    void forceChallenge(Response challenge);

    /**
     * 同 forceChallenge，但会递增暴力破解检测的失败计数。
     *
     * Same behavior as forceChallenge(), but the error count in brute force attack detection will be incremented.
     * For example, if a user enters in a bad password, the user is directed to try again, but Keycloak will keep track
     * of how many failures have happened.
     *
     * @param error
     * @param challenge
     */
    void failureChallenge(AuthenticationFlowError error, Response challenge);

    /**
     * 认证器已尝试但未完成；alternative/optional 时该状态可被忽略。
     *
     * There was no failure or challenge.  The authenticator was attempted, but not fulfilled.  If the current execution
     * requirement is alternative or optional, then this status is ignored by the flow.
     *
     */
    void attempted();

    /**
     * 获取当前执行状态，未设置时可能为 null。
     *
     * Get the current status of the current execution.
     *
     * @return may return null if not set yet.
     */
    FlowStatus getStatus();

    /**
     * 获取失败执行的错误码，无错误时为 null。
     *
     * Get the error condition of a failed execution.
     *
     * @return may return null if there was no error
     */
    AuthenticationFlowError getError();
    
    
    /**
     * 获取导致错误的事件详情。
     *
     * Get details of the event that caused an error
     * @return may return null if not set
     */
    String getEventDetails();
    
    /**
     * 可展示给用户的自定义错误消息。
     *
     * A custom error message that can be displayed to the user
     * @return Optional error message
     */
    String getUserErrorMessage();
}
