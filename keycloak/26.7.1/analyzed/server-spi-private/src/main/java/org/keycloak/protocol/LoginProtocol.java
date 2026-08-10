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

package org.keycloak.protocol;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.Provider;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 登录协议 SPI：定义认证成功/失败、登出及客户端错误回传等行为。
 * <p>OIDC、SAML 等协议各自实现本接口。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface LoginProtocol extends Provider {

    enum Error {

        /**
         * 用户取消登录。
         * Login cancelled by the user
         */
        CANCELLED_BY_USER,
        /**
         * 用户取消应用发起的操作（AIA）。
         * Applications-initiated action was canceled by the user
         */
        CANCELLED_AIA,
        /**
         * 用户取消 AIA，且不向客户端发送错误。
         * Applications-initiated action was canceled by the user. Do not send error.
         */
        CANCELLED_AIA_SILENT,
        /**
         * 用户已登录但 authenticationSession 无效，需重定向回客户端重试（通常 SSO 可自动完成）。
         * User is already logged-in and he has userSession in this browser. But authenticationSession is not valid anymore and hence could not continue authentication
         * in proper way. Will need to redirect back to client, so client can retry authentication. Once client retries authentication, it will usually success automatically
         * due SSO reauthentication.
         */
        ALREADY_LOGGED_IN,
        /**
         * 用户拒绝授权同意。
         * Consent denied by the user
         */
        CONSENT_DENIED,
        /**
         * 被动认证模式下无已登录用户。
         * Passive authentication mode requested but nobody is logged in
         */
        PASSIVE_LOGIN_REQUIRED,
        /**
         * 被动认证模式下用户已登录但仍需交互（如需完成登录动作或同意授权）。
         * Passive authentication mode requested, user is logged in, but some other user interaction is necessary (eg. some required login actions exist or Consent approval is necessary for logged in
         * user)
         */
        PASSIVE_INTERACTION_REQUIRED,
        /**
         * 认证级别无效或未达最低要求（LoA）。
         * Level of Authentication invalid or minimum not reached.
         */
        LOA_INVALID;
    }

    /** 绑定 Keycloak 会话。 */
    LoginProtocol setSession(KeycloakSession session);

    /** 绑定当前 realm。 */
    LoginProtocol setRealm(RealmModel realm);

    /** 绑定请求 URI 信息。 */
    LoginProtocol setUriInfo(UriInfo uriInfo);

    /** 绑定 HTTP 请求头。 */
    LoginProtocol setHttpHeaders(HttpHeaders headers);

    /** 绑定事件构建器。 */
    LoginProtocol setEventBuilder(EventBuilder event);

    /** 认证成功后向客户端返回协议响应。 */
    Response authenticated(AuthenticationSessionModel authSession, UserSessionModel userSession, ClientSessionContext clientSessionCtx);

    /** 在认证会话仍有效时发送协议错误响应。 */
    Response sendError(AuthenticationSessionModel authSession, Error error, String errorMessage);

    /**
     * 从认证会话提取 {@link ClientData}，用于在认证流请求中携带 {@code clientData} 参数。
     * <p>当 authenticationSession 过期或移除后，仍可通过 clientData 向客户端回传错误（如 redirect-uri、state、RelayState 等）。</p>
     * <p>Returns client data, which will be wrapped in the "clientData" parameter sent within "authentication flow" requests.</p>
     *
     * @param authSession session from which particular clientData can be retrieved
     * @return client data, which will be wrapped in the "clientData" parameter sent within "authentication flow" requests
     */
    ClientData getClientData(AuthenticationSessionModel authSession);

    /**
     * 在无 authenticationSession 时，利用 {@link ClientData} 向客户端发送协议错误。
     * <p>应校验 clientData 与客户端配置一致（如 redirect-uri），因 clientData 来自请求参数可被篡改。</p>
     * <p>Send the specified error to the specified client with the use of this protocol.</p>
     *
     * @param client client where to send error
     * @param clientData clientData with additional protocol specific metadata needed for being able to properly send error with the use of this protocol
     * @param error error to be used
     * @return response if error was sent. Null if error was not sent.
     */
    Response sendError(ClientModel client, ClientData clientData, Error error);

    /** 执行后端通道登出。 */
    Response backchannelLogout(UserSessionModel userSession, AuthenticatedClientSessionModel clientSession);
    /** 执行前端通道登出。 */
    Response frontchannelLogout(UserSessionModel userSession, AuthenticatedClientSessionModel clientSession);

    /**
     * 浏览器登出流程结束时调用（后端通道登出不触发）。
     * <p>This method is called when browser logout is going to be finished. It is not triggered during backchannel logout</p>
     *
     * @param userSession user session, which was logged out
     * @param logoutSession authentication session, which was used during logout to track the logout state
     * @return response to be sent to the client
     */
    Response finishBrowserLogout(UserSessionModel userSession, AuthenticationSessionModel logoutSession);

    /**
     * 判断是否必须主动重新认证（不可依赖 SSO Cookie）。
     * @param userSession
     * @param authSession
     * @return true if SSO cookie authentication can't be used. User will need to "actively" reauthenticate
     */
    boolean requireReauthentication(UserSessionModel userSession, AuthenticationSessionModel authSession);

    /**
     * 向客户端推送 not-before 撤销策略。
     * @param realm
     * @param resource
     * @param notBefore
     * @param managementUrl
     * @return {@code true} if revocation policy was successfully updated at the client, {@code false} otherwise.
     */
    default boolean sendPushRevocationPolicyRequest(RealmModel realm, ClientModel resource, int notBefore, String managementUrl) {
        return false;
    }

    /**
     * 协议特定的认证完成处理钩子。
     * Protocol specific handling of authentication completeness
     */
    default void authenticationComplete(AuthenticationSessionModel authSession) {
        // 默认无额外处理
    }
}
