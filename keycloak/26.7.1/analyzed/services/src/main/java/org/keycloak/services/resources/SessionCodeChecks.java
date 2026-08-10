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

package org.keycloak.services.resources;

import java.io.IOException;
import java.net.URI;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.AuthorizationEndpointBase;
import org.keycloak.protocol.ClientData;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.RestartLoginCookie;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.util.AuthenticationFlowURLHelper;
import org.keycloak.services.util.BrowserHistoryHelper;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.CommonClientSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import org.jboss.logging.Logger;

import static org.keycloak.services.managers.AuthenticationManager.authenticateIdentityCookie;


/**
 * 登录/登出流程的认证会话码校验器。
 * <p>验证 SSL、领域/客户端状态、会话码有效性、动作类型及 required-action 一致性；失败时设置 {@link #response}。</p>
 */
public class SessionCodeChecks {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(SessionCodeChecks.class);

    /** 当前认证会话 */
    private AuthenticationSessionModel authSession;
    /** 客户端会话码包装 */
    private ClientSessionCode<AuthenticationSessionModel> clientCode;
    /** 校验失败时的 HTTP 响应 */
    private Response response;
    /** 是否为 POST 动作请求（非页面刷新） */
    private boolean actionRequest;

    /** 领域模型 */
    private final RealmModel realm;
    /** 请求 URI 信息 */
    private final UriInfo uriInfo;
    /** HTTP 请求 */
    private final HttpRequest request;
    /** 客户端连接信息 */
    private final ClientConnection clientConnection;
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 事件构建器 */
    private final EventBuilder event;

    /** URL 中的 session_code 参数 */
    private final String code;
    /** 认证执行步骤 ID */
    private final String execution;
    /** 客户端 ID */
    private final String clientId;
    /** 编码的 client_data 参数 */
    private final String clientDataString;
    /** 浏览器标签页 ID */
    private final String tabId;
    /** 认证流程路径（authenticate 等） */
    private final String flowPath;
    /** 编码的 auth_session_id（可选） */
    private final String authSessionId;


    /**
     * 构造会话码校验器并保存请求参数。
     * @param realm 领域
     * @param uriInfo URI 信息
     * @param request HTTP 请求
     * @param clientConnection 客户端连接
     * @param session Keycloak 会话
     * @param event 事件构建器
     * @param authSessionId 认证会话 ID（可选）
     * @param code 会话码
     * @param execution 执行步骤
     * @param clientId 客户端 ID
     * @param tabId 标签页 ID
     * @param clientData client_data
     * @param flowPath 流程路径
     */
    public SessionCodeChecks(RealmModel realm, UriInfo uriInfo, HttpRequest request, ClientConnection clientConnection, KeycloakSession session, EventBuilder event,
                             String authSessionId, String code, String execution, String clientId, String tabId, String clientData, String flowPath) {
        this.realm = realm;
        this.uriInfo = uriInfo;
        this.request = request;
        this.clientConnection = clientConnection;
        this.session = session;
        this.event = event;

        this.code = code;
        this.execution = execution;
        this.clientId = clientId;
        this.tabId = tabId;
        this.flowPath = flowPath;
        this.authSessionId = authSessionId;
        this.clientDataString = clientData;
    }


    /** @return 解析后的认证会话 */
    public AuthenticationSessionModel getAuthenticationSession() {
        return authSession;
    }


    /** @return 是否已设置失败响应 */
    private boolean failed() {
        return response != null;
    }


    /** @return 失败或缓存的 HTTP 响应 */
    public Response getResponse() {
        return response;
    }


    /** @return 客户端会话码 */
    public ClientSessionCode<AuthenticationSessionModel> getClientCode() {
        return clientCode;
    }

    /** @return 是否为动作 POST 请求 */
    public boolean isActionRequest() {
        return actionRequest;
    }


    /** 校验 HTTPS 是否满足领域 SSL 要求 */
    private boolean checkSsl() {
        if (uriInfo.getBaseUri().getScheme().equals("https")) {
            return true;
        } else {
            return !realm.getSslRequired().isRequired(clientConnection);
        }
    }


    /**
     * 初始认证会话解析：SSL/领域检查、Cookie 与 query 参数一致性、
     * 从 Cookie 重启或已登录用户处理。
     * @return 认证会话，失败时返回 null 并设置 response
     */
    public AuthenticationSessionModel initialVerifyAuthSession() {
        // 基础领域与 SSL 检查
        if (!checkSsl()) {
            event.error(Errors.SSL_REQUIRED);
            response = ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.HTTPS_REQUIRED);
            return null;
        }
        if (!realm.isEnabled()) {
            event.error(Errors.REALM_DISABLED);
            response = ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.REALM_NOT_ENABLED);
            return null;
        }

        // 根据 client_id 设置错误页「返回应用」链接所用客户端
        logger.debugf("Will use client '%s' in back-to-application link", clientId);
        ClientModel client = null;
        if (clientId != null) {
            client = realm.getClientByClientId(clientId);
        }
        if (client != null) {
            session.getContext().setClient(client);
            setClientToEvent(client);
        }


        // 从 query 参数或 Cookie 获取认证会话
        AuthenticationSessionManager authSessionManager = new AuthenticationSessionManager(session);
        AuthenticationSessionModel authSession = null;
        if (authSessionId != null)
            authSession = authSessionManager.getAuthenticationSessionByEncodedIdAndClient(realm, authSessionId, client, tabId);
        AuthenticationSessionModel authSessionCookie = authSessionManager.getCurrentAuthenticationSession(realm, client, tabId);

        if (authSession != null && (authSessionCookie == null || !authSession.getParentSession().getId().equals(authSessionCookie.getParentSession().getId()))) {
            event.detail(Details.REASON, "cookie does not match auth_session query parameter");
            event.error(Errors.INVALID_CODE);
            response = ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_CODE);
            return null;

        }

        ClientData clientData;
        try {
            clientData = ClientData.decodeClientDataFromParameter(clientDataString);
        } catch (RuntimeException | IOException e) {
            logger.debugf(e, "ClientData parameter in invalid format. ClientData parameter was %s", clientDataString);
            event.detail(Details.REASON, "Invalid client data: " + e.getMessage());
            event.error(Errors.INVALID_REQUEST);
            response = ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
            return null;
        }

        if (authSession != null) {
            session.getProvider(LoginFormsProvider.class).setAuthenticationSession(authSession);
            return authSession;
        }

        if (authSessionCookie != null) {
            session.getProvider(LoginFormsProvider.class).setAuthenticationSession(authSessionCookie);
            return authSessionCookie;

        }

        // 否则尝试从 KC_RESTART Cookie 重启流程
        RootAuthenticationSessionModel existingRootAuthSession = authSessionManager.getCurrentRootAuthenticationSession(realm);
        response = restartAuthenticationSessionFromCookie(existingRootAuthSession);

        // Cookie 重启失败时检查用户是否已通过身份 Cookie 登录
        if (response.getStatus() != Response.Status.FOUND.getStatusCode()) {
            AuthenticationManager.AuthResult authResult = authenticateIdentityCookie(session, realm, false);

            if (authResult != null && authResult.session() != null) {
                response = null;

                if (client != null && clientData != null) {
                    LoginProtocol protocol = session.getProvider(LoginProtocol.class, client.getProtocol());
                    protocol.setRealm(realm)
                            .setHttpHeaders(session.getContext().getRequestHeaders())
                            .setUriInfo(session.getContext().getUri())
                            .setEventBuilder(event);
                    response = protocol.sendError(client, clientData, LoginProtocol.Error.ALREADY_LOGGED_IN);
                    event.detail(Details.REDIRECTED_TO_CLIENT, "true");
                }

                if (response == null) {
                    LoginFormsProvider loginForm = session.getProvider(LoginFormsProvider.class).setAuthenticationSession(authSession)
                            .setSuccess(Messages.ALREADY_LOGGED_IN);

                    if (client == null) {
                        loginForm.setAttribute(Constants.SKIP_LINK, true);
                    }

                    response = loginForm.createInfoPage();
                    event.detail(Details.REDIRECTED_TO_CLIENT, "false");
                }
                event.error(Errors.ALREADY_LOGGED_IN);
            } else {
                event.error(Errors.COOKIE_NOT_FOUND);
            }
        }

        return null;
    }


    /** 完整初始校验：会话解析、浏览器历史缓存、客户端检查、动作码解析 */
    public boolean initialVerify() {
        // Basic realm checks and authenticationSession retrieve
        authSession = initialVerifyAuthSession();
        if (authSession == null) {
            return false;
        }
        session.getContext().setAuthenticationSession(authSession);

        // 检查浏览器历史中缓存的上一次动作响应
        response = BrowserHistoryHelper.getInstance().loadSavedResponse(session, authSession);
        if (response != null) {
            return false;
        }

        // 客户端存在性与启用状态检查
        event.detail(Details.CODE_ID, authSession.getParentSession().getId());
        ClientModel client = authSession.getClient();
        if (client == null) {
            event.error(Errors.CLIENT_NOT_FOUND);
            session.getProvider(LoginFormsProvider.class).setDetachedAuthSession();
            response = ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.UNKNOWN_LOGIN_REQUESTER);
            removeAuthenticationSession(authSession);
            return false;
        }

        setClientToEvent(client);
        session.getContext().setClient(client);

        if (checkClientDisabled(client)) {
            event.error(Errors.CLIENT_DISABLED);
            session.getProvider(LoginFormsProvider.class).setDetachedAuthSession();
            response = ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.LOGIN_REQUESTER_NOT_ENABLED);
            removeAuthenticationSession(authSession);
            return false;
        }


        // 区分页面刷新（无 code）与动作提交（有 code）
        if (code == null) {
            String lastExecFromSession = authSession.getAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
            String lastFlow = authSession.getAuthNote(AuthenticationProcessor.CURRENT_FLOW_PATH);

            // 检测流程切换（如登录页点击「注册」）
            if (execution == null && !flowPath.equals(lastFlow)) {
                logger.debugf("Transition between flows! Current flow: %s, Previous flow: %s", flowPath, lastFlow);

                // 已在 required-actions 时不允许切换流程
                if (AuthenticationSessionModel.Action.AUTHENTICATE.name().equals(authSession.getAction())) {
                    authSession.setAuthNote(AuthenticationProcessor.CURRENT_FLOW_PATH, flowPath);
                    authSession.removeAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
                    lastExecFromSession = null;
                }
            }

            if (execution == null || execution.equals(lastExecFromSession) || CommonClientSessionModel.ExecutionStatus.CHALLENGED.equals(authSession.getExecutionStatus().get(execution))) {
                // 允许刷新当前步骤页面
                clientCode = new ClientSessionCode<>(session, realm, authSession);
                actionRequest = false;

                // 允许刷新但更新浏览器历史
                if (execution == null && lastExecFromSession != null) {
                    logger.debugf("Parameter 'execution' is not in the request, but flow wasn't changed. Will update browser history");
                    session.setAttribute(BrowserHistoryHelper.SHOULD_UPDATE_BROWSER_HISTORY, true);
                }

                return true;
            } else {
                response = showPageExpired(authSession);
                return false;
            }
        } else {
            ClientSessionCode.ParseResult<AuthenticationSessionModel> result = ClientSessionCode.parseResult(code, tabId, session, realm, client, event, authSession);
            clientCode = result.getCode();
            if (clientCode == null) {

                // 动作码过期但 execution 匹配时重定向并转发错误消息, we just re-render the page
                if (ObjectUtil.isEqualOrBothNull(execution, authSession.getAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION))) {
                    String latestFlowPath = authSession.getAuthNote(AuthenticationProcessor.CURRENT_FLOW_PATH);
                    if (latestFlowPath != null) {
                        String clientData = AuthenticationProcessor.getClientData(session, authSession);
                        URI redirectUri = getLastExecutionUrl(latestFlowPath, execution, tabId, clientData);

                        logger.debugf("Invalid action code, but execution matches. So just redirecting to %s", redirectUri);
                        authSession.setAuthNote(LoginActionsService.FORWARDED_ERROR_MESSAGE_NOTE, Messages.EXPIRED_ACTION);
                        response = Response.status(Response.Status.FOUND).location(redirectUri).build();
                        return false;
                    }
                }
                response = showPageExpired(authSession);
                return false;

            }


            actionRequest = true;
            if (execution != null) {
                authSession.setAuthNote(AuthenticationProcessor.LAST_PROCESSED_EXECUTION, execution);
            }
            return true;
        }
    }

    // Client is not null
    /** 将客户端写入审计事件 */
    protected void setClientToEvent(ClientModel client) {
        event.client(client);
    }


    /**
     * 校验动作码活跃且 action 字段匹配。
     * @param expectedAction 预期 action 名称
     * @param actionType 动作类型（LOGIN/USER 等）
     */
    public boolean verifyActiveAndValidAction(String expectedAction, ClientSessionCode.ActionType actionType) {
        if (failed()) {
            return false;
        }

        if (!isActionActive(actionType)) {
            return false;
        }

        if (!clientCode.isValidAction(expectedAction)) {
            AuthenticationSessionModel authSession = getAuthenticationSession();
            if (AuthenticationSessionModel.Action.REQUIRED_ACTIONS.name().equals(authSession.getAction())) {
                logger.debugf("Incorrect action '%s' . User authenticated already.", authSession.getAction());
                response = showPageExpired(authSession);
                return false;
            } else {
                logger.errorf("Bad action. Expected action '%s', current action '%s'", expectedAction, authSession.getAction());
                response = ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.EXPIRED_CODE);
                return false;
            }
        }

        return true;
    }


    /** 校验动作码未过期，过期时重置流程并重定向 */
    protected boolean isActionActive(ClientSessionCode.ActionType actionType) {
        if (!clientCode.isActionActive(actionType)) {
            event.clone().error(Errors.EXPIRED_CODE);

            AuthenticationProcessor.resetFlow(authSession, LoginActionsService.AUTHENTICATE_PATH);

            authSession.setAuthNote(LoginActionsService.FORWARDED_ERROR_MESSAGE_NOTE, Messages.LOGIN_TIMEOUT);

            String clientData = AuthenticationProcessor.getClientData(session, authSession);
            URI redirectUri = getLastExecutionUrl(LoginActionsService.AUTHENTICATE_PATH, null, tabId, clientData);
            logger.debugf("Flow restart after timeout. Redirecting to %s", redirectUri);
            response = Response.status(Response.Status.FOUND).location(redirectUri).build();
            return false;
        }
        return true;
    }


    /** 校验 required-action 流程的动作码与当前执行步骤一致 */
    public boolean verifyRequiredAction(String executedAction) {
        if (failed()) {
            return false;
        }

        if (!clientCode.isValidAction(AuthenticationSessionModel.Action.REQUIRED_ACTIONS.name())) {
            logger.debugf("Expected required action, but session action is '%s' . Showing expired page now.", authSession.getAction());
            event.error(Errors.INVALID_CODE);

            response = showPageExpired(authSession);

            return false;
        }

        if (!isActionActive(ClientSessionCode.ActionType.USER)) {
            return false;
        }

        if (actionRequest) {
            String currentRequiredAction = authSession.getAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
            if (executedAction == null || !executedAction.equals(currentRequiredAction)) {
                logger.debug("required action doesn't match current required action");
                response = redirectToRequiredActions(currentRequiredAction);
                return false;
            }
        }
        return true;
    }


    /** 从 KC_RESTART Cookie 重启认证会话并重定向到流程入口 */
    protected Response restartAuthenticationSessionFromCookie(RootAuthenticationSessionModel existingRootSession) {
        logger.debug("Authentication session not found. Trying to restart from cookie.");
        AuthenticationSessionModel authSession = null;

        String cook = RestartLoginCookie.getRestartCookie(session);
        if (cook == null) {
            return ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.COOKIE_NOT_FOUND);
        }

        try {
            authSession = RestartLoginCookie.restartSession(session, realm, existingRootSession, clientId, cook);
        } catch (Exception e) {
            ServicesLogger.LOGGER.failedToParseRestartLoginCookie(e);
        }

        if (authSession != null) {

            event.clone();
            event.detail(Details.RESTART_AFTER_TIMEOUT, "true");
            event.error(Errors.EXPIRED_CODE);

            String warningMessage = Messages.LOGIN_TIMEOUT;
            authSession.setAuthNote(LoginActionsService.FORWARDED_ERROR_MESSAGE_NOTE, warningMessage);

            String flowPath = authSession.getClientNote(AuthorizationEndpointBase.APP_INITIATED_FLOW);
            if (flowPath == null) {
                flowPath = LoginActionsService.AUTHENTICATE_PATH;
            }

            // 若 client_data 中 redirect_uri 合法则写入认证会话
            try {
                ClientData clientData = ClientData.decodeClientDataFromParameter(clientDataString);
                if (RedirectUtils.verifyRedirectUri(session, clientData.getRedirectUri(), authSession.getClient()) != null) {
                    authSession.setRedirectUri(clientData.getRedirectUri());
                }
            } catch (Exception e) {
                logger.debugf(e, "ClientData parameter in invalid format. ClientData parameter was %s", clientDataString);
            }

            String clientData = AuthenticationProcessor.getClientData(session, authSession);
            URI redirectUri = getLastExecutionUrl(flowPath, null, authSession.getTabId(), clientData);
            logger.debugf("Authentication session restart from cookie succeeded. Redirecting to %s", redirectUri);
            return Response.status(Response.Status.FOUND).location(redirectUri).build();
        } else {
            // Finally need to show error as all the fallbacks failed
            event.error(Errors.INVALID_CODE);
            return ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.INVALID_CODE);
        }
    }


    /** 重定向到 required-action 端点 */
    private Response redirectToRequiredActions(String action) {
        UriBuilder uriBuilder = LoginActionsService.loginActionsBaseUrl(uriInfo)
                .path(LoginActionsService.REQUIRED_ACTION);

        if (action != null) {
            uriBuilder.queryParam(Constants.EXECUTION, action);
        }

        ClientModel client = authSession.getClient();
        uriBuilder.queryParam(Constants.CLIENT_ID, client.getClientId())
                .queryParam(Constants.TAB_ID, authSession.getTabId())
                .queryParam(Constants.CLIENT_DATA, AuthenticationProcessor.getClientData(session, authSession));

        URI redirect = uriBuilder.build(realm.getName());
        return Response.status(302).location(redirect).build();
    }


    /** 构建流程最后执行步骤 URL */
    private URI getLastExecutionUrl(String flowPath, String executionId, String tabId, String clientData) {
        return new AuthenticationFlowURLHelper(session, realm, uriInfo)
                .getLastExecutionUrl(flowPath, executionId, clientId, tabId, clientData);
    }


    /** 显示「页面已过期」提示 */
    private Response showPageExpired(AuthenticationSessionModel authSession) {
        return new AuthenticationFlowURLHelper(session, realm, uriInfo)
                .showPageExpired(authSession);
    }

    /** @return Keycloak 会话 */
    protected KeycloakSession getSession() {
        return session;
    }

    /** @return 事件构建器 */
    protected EventBuilder getEvent() {
        return event;
    }

    /** 移除过期的认证会话 */
    private void removeAuthenticationSession(AuthenticationSessionModel authSession) {
        ClientSessionCode<AuthenticationSessionModel> codeToRemove = clientCode != null
                ? clientCode
                : new ClientSessionCode<>(session, realm, authSession);
        codeToRemove.removeExpiredClientSession();
    }

    /** @return 客户端是否已禁用 */
    protected boolean checkClientDisabled(ClientModel client) {
        return !client.isEnabled();
    }
}
