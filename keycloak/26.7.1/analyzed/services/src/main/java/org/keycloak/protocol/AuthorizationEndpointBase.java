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

import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.AuthenticationFlowResolver;
import org.keycloak.protocol.LoginProtocol.Error;
import org.keycloak.services.ErrorPageException;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import org.jboss.logging.Logger;

/**
 * 授权端点 REST 实现的公共基类：各登录协议（OIDC、SAML 等）的授权端点继承此类。
 * <p>统一浏览器认证请求处理、SSL/Realm 校验及认证会话创建逻辑。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public abstract class AuthorizationEndpointBase {

    private static final Logger logger = Logger.getLogger(AuthorizationEndpointBase.class);

    /** 认证会话 note：标记由应用发起的登录流。 */
    public static final String APP_INITIATED_FLOW = "APP_INITIATED_FLOW";

    protected final RealmModel realm;
    protected final EventBuilder event;
    protected AuthenticationManager authManager;

    protected final HttpHeaders headers;

    protected final HttpRequest httpRequest;

    protected final KeycloakSession session;

    protected final ClientConnection clientConnection;

    /**
     * @param session Keycloak 会话
     * @param event 事件构建器
     */
    public AuthorizationEndpointBase(KeycloakSession session, EventBuilder event) {
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.realm = session.getContext().getRealm();
        this.event = event;
        this.httpRequest = session.getContext().getHttpRequest();
        this.headers = session.getContext().getRequestHeaders();
    }

    /** 创建并配置浏览器认证处理器，写入当前流路径 note。 */
    protected AuthenticationProcessor createProcessor(AuthenticationSessionModel authSession, String flowId, String flowPath) {
        AuthenticationProcessor processor = new AuthenticationProcessor();
        processor.setAuthenticationSession(authSession)
                .setFlowPath(flowPath)
                .setFlowId(flowId)
                .setBrowserFlow(true)
                .setConnection(clientConnection)
                .setEventBuilder(event)
                .setRealm(realm)
                .setSession(session)
                .setUriInfo(session.getContext().getUri())
                .setRequest(httpRequest);

        authSession.setAuthNote(AuthenticationProcessor.CURRENT_FLOW_PATH, flowPath);

        return processor;
    }

    /**
     * 以统一方式处理各协议的浏览器认证请求。
     * @param authSession 当前请求的认证会话
     * @param protocol 发起登录的协议处理器
     * @param isPassive 为 true 时被动登录（OIDC prompt=none / SAML IsPassive）
     * @param redirectToAuthentication 为 true 时重定向到认证流 URL（POST 入口通常需开启以禁用浏览器后退）
     * @return 返回给浏览器的响应
     */
    protected Response handleBrowserAuthenticationRequest(AuthenticationSessionModel authSession, LoginProtocol protocol, boolean isPassive, boolean redirectToAuthentication) {
        AuthenticationFlowModel flow = getAuthenticationFlow(authSession);
        String flowId = flow.getId();
        AuthenticationProcessor processor = createProcessor(authSession, flowId, LoginActionsService.AUTHENTICATE_PATH);
        event.detail(Details.CODE_ID, authSession.getParentSession().getId());
        if (isPassive) {
            // OIDC prompt=none 或 SAML IsPassive：客户端仅探测是否已完全登录
            // 客户端仅探测是否已完全登录；若仍需认证动作或必需操作则取消被动登录
            try {
                Response challenge = processor.authenticateOnly();
                if (challenge != null) {
                    // KEYCLOAK-8043：prompt=none 时转发至默认身份提供方
                    if ("true".equals(authSession.getAuthNote(AuthenticationProcessor.FORWARDED_PASSIVE_LOGIN))) {
                        RestartLoginCookie.setRestartCookie(session, authSession);
                        if (redirectToAuthentication) {
                            return processor.redirectToFlow();
                        }
                        // 无需再次 authenticate，直接返回 authenticateOnly 的挑战
                        return challenge;
                    }
                    else {
                        return protocol.sendError(authSession, Error.PASSIVE_LOGIN_REQUIRED, null);
                    }
                }

                AuthenticationManager.setClientScopesInSession(session, authSession);

                if (processor.nextRequiredAction() != null) {
                    return protocol.sendError(authSession, Error.PASSIVE_INTERACTION_REQUIRED, null);
                }

            } catch (Exception e) {
                return processor.handleBrowserException(e);
            }
            return processor.finishAuthentication(protocol);
        } else {
            try {
                RestartLoginCookie.setRestartCookie(session, authSession);
                if (redirectToAuthentication) {
                    return processor.redirectToFlow();
                }
                return processor.authenticate();
            } catch (Exception e) {
                return processor.handleBrowserException(e);
            }
        }
    }

    /** 解析浏览器认证流模型。 */
    protected AuthenticationFlowModel getAuthenticationFlow(AuthenticationSessionModel authSession) {
        return AuthenticationFlowResolver.resolveBrowserFlow(authSession);
    }

    /** 按 Realm SSL 要求校验当前连接是否为 HTTPS。 */
    protected void checkSsl() {
        if (!session.getContext().getUri().getBaseUri().getScheme().equals("https") && realm.getSslRequired().isRequired(clientConnection)) {
            event.error(Errors.SSL_REQUIRED);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.HTTPS_REQUIRED);
        }
    }

    /** 校验 Realm 已启用，否则抛出错误页异常。 */
    protected void checkRealm() {
        if (!realm.isEnabled()) {
            event.error(Errors.REALM_DISABLED);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.REALM_NOT_ENABLED);
        }
    }

    /**
     * 创建或复用根认证会话下的客户端认证会话；必要时从身份 Cookie 恢复用户会话。
     * @param client 请求客户端
     * @param requestState 请求状态（由子类使用）
     * @return 新建的认证会话
     */
    protected AuthenticationSessionModel createAuthenticationSession(ClientModel client, String requestState) {
        AuthenticationSessionManager manager = new AuthenticationSessionManager(session);
        RootAuthenticationSessionModel rootAuthSession = manager.getCurrentRootAuthenticationSession(realm);

        AuthenticationSessionModel authSession;

        if (rootAuthSession != null) {
            authSession = rootAuthSession.createAuthenticationSession(client);

            logger.debugf("Sent request to authz endpoint. Root authentication session with ID '%s' exists. Client is '%s' . Created new authentication session with tab ID: %s",
                    rootAuthSession.getId(), client.getClientId(), authSession.getTabId());
        } else {
            UserSessionModel userSession = manager.getUserSessionFromAuthenticationCookie(realm);

            if (userSession != null) {
                UserModel user = userSession.getUser();
                if (user != null && !user.isEnabled()) {
                    authSession = createNewAuthenticationSession(manager, client);

                    AuthenticationManager.backchannelLogout(session, userSession, true);
                } else {
                    String userSessionId = userSession.getId();
                    rootAuthSession = session.authenticationSessions().getRootAuthenticationSession(realm, userSessionId);
                    if (rootAuthSession == null) {
                        // 视存储层实现，可能需按 userSessionId 重建根认证会话
                        rootAuthSession = session.authenticationSessions().createRootAuthenticationSession(realm, userSessionId);
                    }
                    authSession = rootAuthSession.createAuthenticationSession(client);
                    // 从身份 Cookie 恢复时可能缺少认证会话 Cookie，此处补写
                    manager.setAuthSessionCookie(rootAuthSession.getId());
                    manager.setAuthSessionIdHashCookie(rootAuthSession.getId());
                    logger.debugf("Sent request to authz endpoint. We don't have root authentication session with ID '%s' but we have userSession." +
                            "Re-created root authentication session with same ID. Client is: %s . New authentication session tab ID: %s", userSessionId, client.getClientId(), authSession.getTabId());
                }
            } else {
                authSession = createNewAuthenticationSession(manager, client);
            }
        }

        session.getContext().setAuthenticationSession(authSession);
        session.getProvider(LoginFormsProvider.class).setAuthenticationSession(authSession);

        return authSession;

    }

    /** 创建新的根认证会话及客户端认证子会话。 */
    private AuthenticationSessionModel createNewAuthenticationSession(AuthenticationSessionManager manager, ClientModel client) {
        RootAuthenticationSessionModel rootAuthSession = manager.createAuthenticationSession(realm, true);
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);
        logger.debugf("Sent request to authz endpoint. Created new root authentication session with ID '%s' . Client: %s . New authentication session tab ID: %s",
                rootAuthSession.getId(), client.getClientId(), authSession.getTabId());
        return authSession;
    }
}
