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

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.OAuth2Constants;
import org.keycloak.TokenVerifier;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.ExplainedVerificationException;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionContextResult;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.authentication.actiontoken.ActionTokenHandler;
import org.keycloak.authentication.actiontoken.ExplainedTokenVerificationException;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionTokenHandler;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.PostBrokerLoginConstants;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Time;
import org.keycloak.common.util.TriFunction;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.exceptions.TokenNotActiveException;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.MessageType;
import org.keycloak.forms.login.freemarker.DetachedInfoStateChecker;
import org.keycloak.forms.login.freemarker.DetachedInfoStateCookie;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.DefaultActionTokenKey;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectKeyModel;
import org.keycloak.models.UserConsentModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.AuthenticationFlowResolver;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.SystemClientUtil;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.AuthorizationEndpointBase;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocol.Error;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.grants.device.DeviceGrantType;
import org.keycloak.protocol.oidc.utils.OIDCResponseMode;
import org.keycloak.protocol.oidc.utils.OIDCResponseType;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.rar.AuthorizationDetails;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.ErrorPageException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.managers.UserConsentManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.util.AuthenticationFlowURLHelper;
import org.keycloak.services.util.BrowserHistoryHelper;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.services.util.LocaleUtil;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import org.jboss.logging.Logger;

import static org.keycloak.authentication.actiontoken.DefaultActionToken.ACTION_TOKEN_BASIC_CHECKS;
import static org.keycloak.models.utils.DefaultRequiredActions.getDefaultRequiredActionCaseInsensitively;

/**
 * 登录操作 REST 服务（{@code /realms/{realm}/login-actions}）。
 * <p>处理浏览器认证、注册、重置凭证、required-action、Action Token、Broker 登录及 OAuth 同意等流程。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoginActionsService {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(LoginActionsService.class);

    /** 浏览器认证流程路径 */
    public static final String AUTHENTICATE_PATH = "authenticate";
    /** 用户注册流程路径 */
    public static final String REGISTRATION_PATH = "registration";
    /** 重置凭证流程路径 */
    public static final String RESET_CREDENTIALS_PATH = "reset-credentials";
    /** 必需操作（required-action）路径 */
    public static final String REQUIRED_ACTION = "required-action";
    /** 首次 Broker 登录流程路径 */
    public static final String FIRST_BROKER_LOGIN_PATH = "first-broker-login";
    /** Broker 登录后附加流程路径 */
    public static final String POST_BROKER_LOGIN_PATH = "post-broker-login";

    /** 重启认证会话路径 */
    public static final String RESTART_PATH = "restart";

    /**  detached 信息页路径（认证会话已删除后切换语言） */
    public static final String DETACHED_INFO_PATH = "detached-info";

    /** 认证会话 note：跨请求转发的错误消息键 */
    public static final String FORWARDED_ERROR_MESSAGE_NOTE = "forwardedErrorMessage";

    /** URL 参数：会话动作码 */
    public static final String SESSION_CODE = "session_code";
    /** URL 参数：认证会话 ID */
    public static final String AUTH_SESSION_ID = "auth_session_id";

    /** 表单字段：取消应用发起的 required-action */
    public static final String CANCEL_AIA = "cancel-aia";

    /** 当前领域 */
    private final RealmModel realm;

    /** HTTP 请求 */
    private final HttpRequest request;

    /** HTTP 头 */
    protected final HttpHeaders headers;

    /** 客户端连接 */
    private final ClientConnection clientConnection;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 登录相关事件构建器 */
    private EventBuilder event;

    /** 构建 login-actions 基础 URL（UriInfo） */
    public static UriBuilder loginActionsBaseUrl(UriInfo uriInfo) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return loginActionsBaseUrl(baseUriBuilder);
    }

    /** 构建 authenticate 表单 POST URL */
    public static UriBuilder authenticationFormProcessor(UriInfo uriInfo) {
        return loginActionsBaseUrl(uriInfo).path(LoginActionsService.class, "authenticateForm");
    }

    /** 构建 required-action POST URL */
    public static UriBuilder requiredActionProcessor(UriInfo uriInfo) {
        return loginActionsBaseUrl(uriInfo).path(LoginActionsService.class, "requiredActionPOST");
    }

    /** 构建 action-token GET URL */
    public static UriBuilder actionTokenProcessor(UriInfo uriInfo) {
        return loginActionsBaseUrl(uriInfo).path(LoginActionsService.class, "executeActionToken");
    }

    /** 构建 registration POST URL */
    public static UriBuilder registrationFormProcessor(UriInfo uriInfo) {
        return loginActionsBaseUrl(uriInfo).path(LoginActionsService.class, "processRegister");
    }

    /** 构建 first-broker-login GET URL */
    public static UriBuilder firstBrokerLoginProcessor(UriInfo uriInfo) {
        return loginActionsBaseUrl(uriInfo).path(LoginActionsService.class, "firstBrokerLoginGet");
    }

    /** 构建 post-broker-login GET URL */
    public static UriBuilder postBrokerLoginProcessor(UriInfo uriInfo) {
        return loginActionsBaseUrl(uriInfo).path(LoginActionsService.class, "postBrokerLoginGet");
    }

    /** 构建 login-actions 基础 URL（UriBuilder） */
    public static UriBuilder loginActionsBaseUrl(UriBuilder baseUriBuilder) {
        return baseUriBuilder.path(RealmsResource.class).path(RealmsResource.class, "getLoginActionsService");
    }

    /** 构造登录操作服务并设置禁止后退缓存头 */
    public LoginActionsService(KeycloakSession session, EventBuilder event) {
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.realm = session.getContext().getRealm();
        this.event = event;
        CacheControlUtil.noBackButtonCacheControlHeader(session);
        this.request = session.getContext().getHttpRequest();
        this.headers = session.getContext().getRequestHeaders();
    }

    /** 校验 HTTPS 是否满足领域要求 */
    private boolean checkSsl() {
        if (session.getContext().getUri().getBaseUri().getScheme().equals("https")) {
            return true;
        } else {
            return !realm.getSslRequired().isRequired(clientConnection);
        }
    }


    /** 创建 {@link SessionCodeChecks} 并执行 initialVerify */
    private SessionCodeChecks checksForCode(String authSessionId, String code, String execution, String clientId, String tabId, String clientData, String flowPath) {
        SessionCodeChecks res = new SessionCodeChecks(realm, session.getContext().getUri(), request, clientConnection, session, event, authSessionId, code, execution, clientId, tabId, clientData, flowPath);
        res.initialVerify();
        return res;
    }


    /** 构建流程最后执行步骤 URL */
    protected URI getLastExecutionUrl(String flowPath, String executionId, String clientId, String tabId, String clientData) {
        return new AuthenticationFlowURLHelper(session, realm, session.getContext().getUri())
                .getLastExecutionUrl(flowPath, executionId, clientId, tabId, clientData);
    }


    /**
     * 协议无关的认证流程重启入口
     *
     * @return
     */
    @Path(RESTART_PATH)
    @GET
    /** {@inheritDoc} 重置流程并可选择登出重认证中的用户会话 */
    public Response restartSession(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                   @QueryParam(Constants.CLIENT_ID) String clientId,
                                   @QueryParam(Constants.TAB_ID) String tabId,
                                   @QueryParam(Constants.CLIENT_DATA) String clientData,
                                   @QueryParam(Constants.SKIP_LOGOUT) String skipLogout) {
        event.event(EventType.RESTART_AUTHENTICATION);
        SessionCodeChecks checks = new SessionCodeChecks(realm, session.getContext().getUri(), request, clientConnection, session, event, authSessionId, null, null, clientId,  tabId, clientData, null);

        AuthenticationSessionModel authSession = checks.initialVerifyAuthSession();
        if (authSession == null) {
            return checks.getResponse();
        }

        event.user(authSession.getAuthenticatedUser());
        event.detail(Details.USERNAME, authSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME));
        event.detail(Details.AUTH_METHOD, authSession.getProtocol());

        String flowPath = authSession.getClientNote(AuthorizationEndpointBase.APP_INITIATED_FLOW);
        if (flowPath == null) {
            flowPath = AUTHENTICATE_PATH;
        }

        if (!Boolean.parseBoolean(skipLogout)) {
            // 重认证场景下若已有 userSession 则先 backchannel 登出 This means restart of authentication session during re-authentication
            // We logout userSession in this case
            UserSessionModel userSession = new AuthenticationSessionManager(session).getUserSession(authSession);
            if (userSession != null) {
                logger.debugf("Logout of user session %s when restarting flow during re-authentication", userSession.getId());
                AuthenticationManager.backchannelLogout(session, userSession, false);
                authSession = AuthenticationProcessor.recreate(session, authSession);
            }
        }

        AuthenticationProcessor.resetFlow(authSession, flowPath);

        URI redirectUri = getLastExecutionUrl(flowPath, null, authSession.getClient().getClientId(), authSession.getTabId(), AuthenticationProcessor.getClientData(session, authSession));
        logger.debugf("Flow restart requested. Redirecting to %s", redirectUri);
        event.success();
        return Response.status(Response.Status.FOUND).location(redirectUri).build();
    }

    /**
     * 协议无关的 detached 信息页（认证会话已移除后切换 locale） Shown when locale is changed by user on info/error page
     * after authenticationSession was already removed.
     *
     * @return
     */
    @Path(DETACHED_INFO_PATH)
    @GET
    /** {@inheritDoc} 基于 state cookie 渲染 info/error 页 */
    public Response detachedInfo(@QueryParam(DetachedInfoStateChecker.STATE_CHECKER_PARAM) String stateCheckerParam) {
        DetachedInfoStateCookie cookie;
        try {
            cookie = new DetachedInfoStateChecker(session, realm).verifyStateCheckerParameter(stateCheckerParam);
            logger.tracef("Detached info endpoint invoked and cookie successfully verified. StateCheckerParam=%s, StateCookie=%s", stateCheckerParam, cookie);
        } catch (VerificationException ve) {
            logger.warn(ve.getMessage());
            return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.EXPIRED_ACTION_TOKEN_NO_SESSION);
        }

        processLocaleParam(null);

        boolean skipLink = true;
        if (cookie.getClientUuid() != null) {
            ClientModel client = session.clients().getClientById(realm, cookie.getClientUuid());
            if (client != null) {
                session.getContext().setClient(client);
                skipLink = client.equals(SystemClientUtil.getSystemClient(realm));
            }
        }

        MessageType type = Enum.valueOf(MessageType.class, cookie.getMessageType());
        Response.Status statusObj = cookie.getStatus() == null ? Response.Status.BAD_REQUEST : Response.Status.fromStatusCode(cookie.getStatus());
        Object[] paramsAsObject = cookie.getMessageParameters() == null ? null : cookie.getMessageParameters().toArray();

        LoginFormsProvider loginForm = session.getProvider(LoginFormsProvider.class)
                .setDetachedAuthSession()
                .setMessage(type, cookie.getMessageKey(), paramsAsObject);

        if (skipLink) {
            loginForm.setAttribute(Constants.SKIP_LINK, true);
        }

        return type == MessageType.ERROR ? loginForm.createErrorPage(statusObj) : loginForm.createInfoPage();
    }


    /**
     * 协议无关的浏览器登录入口（GET authenticate）
     *
     * @param code
     * @return
     */
    @Path(AUTHENTICATE_PATH)
    @GET
    /** {@inheritDoc} 执行或展示浏览器认证流程 */
    public Response authenticate(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                 @QueryParam(SESSION_CODE) String code,
                                 @QueryParam(Constants.EXECUTION) String execution,
                                 @QueryParam(Constants.CLIENT_ID) String clientId,
                                 @QueryParam(Constants.TAB_ID) String tabId,
                                 @QueryParam(Constants.CLIENT_DATA) String clientData) {

        event.event(EventType.LOGIN);

        SessionCodeChecks checks = checksForCode(authSessionId, code, execution, clientId, tabId, clientData, AUTHENTICATE_PATH);
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(), ClientSessionCode.ActionType.LOGIN)) {
            return checks.getResponse();
        }

        AuthenticationSessionModel authSession = checks.getAuthenticationSession();
        boolean actionRequest = checks.isActionRequest();

        processLocaleParam(authSession);

        return processAuthentication(actionRequest, execution, authSession, null);
    }

    /** 处理 locale 查询参数并写入认证会话 */
    protected void processLocaleParam(AuthenticationSessionModel authSession) {
        LocaleUtil.processLocaleParam(session, realm, authSession);
    }

    /** 执行浏览器认证流程（authenticate 路径） */
    protected Response processAuthentication(boolean action, String execution, AuthenticationSessionModel authSession, String errorMessage) {
        return processFlow(action, execution, authSession, AUTHENTICATE_PATH, AuthenticationFlowResolver.resolveBrowserFlow(authSession), errorMessage, new AuthenticationProcessor());
    }

    /**
     * 通用认证流程处理器：配置 AuthenticationProcessor 并保存浏览器历史。
     * @param action 是否为表单 POST 动作
     * @param execution 执行步骤 ID
     * @param authSession 认证会话
     * @param flowPath 流程路径
     * @param flow 认证流程模型
     * @param errorMessage 转发错误消息
     * @param processor 认证处理器
     */
    protected Response processFlow(boolean action, String execution, AuthenticationSessionModel authSession, String flowPath, AuthenticationFlowModel flow, String errorMessage, AuthenticationProcessor processor) {
        processor.setAuthenticationSession(authSession)
                .setFlowPath(flowPath)
                .setBrowserFlow(true)
                .setFlowId(flow.getId())
                .setConnection(clientConnection)
                .setEventBuilder(event)
                .setRealm(realm)
                .setSession(session)
                .setUriInfo(session.getContext().getUri())
                .setRequest(request);
        if (errorMessage != null) {
            processor.setForwardedErrorMessage(new FormMessage(null, errorMessage));
        }

        // 读取上一次请求转发的错误消息 note
        String forwardedErrorMessage = authSession.getAuthNote(FORWARDED_ERROR_MESSAGE_NOTE);
        if (forwardedErrorMessage != null) {
            authSession.removeAuthNote(FORWARDED_ERROR_MESSAGE_NOTE);
            processor.setForwardedErrorMessage(new FormMessage(null, forwardedErrorMessage));
        }


        Response response;
        try {
            if (action) {
                response = processor.authenticationAction(execution);
            } else {
                response = processor.authenticate();
            }
        } catch (WebApplicationException e) {
            response = e.getResponse();
            authSession = processor.getAuthenticationSession();
        } catch (Exception e) {
            response = processor.handleBrowserException(e);
            authSession = processor.getAuthenticationSession(); // Could be changed (eg. Forked flow)
        }

        return BrowserHistoryHelper.getInstance().saveResponseAndRedirect(session, authSession, response, action, request);
    }

    /**
     * 登录表单 POST 端点（勿直接调用）
     *
     * @param code
     * @return
     */
    @Path(AUTHENTICATE_PATH)
    @POST
    /** {@inheritDoc} 委托给 {@link #authenticate} */
    public Response authenticateForm(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                     @QueryParam(SESSION_CODE) String code,
                                     @QueryParam(Constants.EXECUTION) String execution,
                                     @QueryParam(Constants.CLIENT_ID) String clientId,
                                     @QueryParam(Constants.TAB_ID) String tabId,
                                     @QueryParam(Constants.CLIENT_DATA) String clientData) {
        return authenticate(authSessionId, code, execution, clientId, tabId, clientData);
    }

    @Path(RESET_CREDENTIALS_PATH)
    @POST
    /** 重置凭证 POST：支持 action token key 或表单流程 */
    public Response resetCredentialsPOST(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                         @QueryParam(SESSION_CODE) String code,
                                         @QueryParam(Constants.EXECUTION) String execution,
                                         @QueryParam(Constants.CLIENT_ID) String clientId,
                                         @QueryParam(Constants.TAB_ID) String tabId,
                                         @QueryParam(Constants.CLIENT_DATA) String clientData,
                                         @QueryParam(Constants.KEY) String key) {
        if (key != null) {
            return handleActionToken(key, execution, clientId, tabId, clientData, null);
        }

        event.event(EventType.RESET_PASSWORD);

        return resetCredentials(authSessionId, code, execution, clientId, tabId, clientData);
    }

    /**
     * 重置凭证流程 GET 入口；无会话时可 deep-link 创建认证会话  If token is null, a authentication session is created with the account
     * service as the client.  Successful reset sends you to the account page.  Note, account service must be enabled.
     *
     * @param code
     * @param execution
     * @return
     */
    @Path(RESET_CREDENTIALS_PATH)
    @GET
    /** {@inheritDoc} */
    public Response resetCredentialsGET(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                        @QueryParam(SESSION_CODE) String code,
                                        @QueryParam(Constants.EXECUTION) String execution,
                                        @QueryParam(Constants.CLIENT_ID) String clientId,
                                        @QueryParam(OIDCLoginProtocol.REDIRECT_URI_PARAM) String redirectUri,
                                        @QueryParam(Constants.TAB_ID) String tabId,
                                        @QueryParam(Constants.CLIENT_DATA) String clientData) {
        ClientModel client = realm.getClientByClientId(clientId);
        AuthenticationSessionModel authSession = new AuthenticationSessionManager(session).getCurrentAuthenticationSession(realm, client, tabId);
        processLocaleParam(authSession);

        event.event(EventType.RESET_PASSWORD);
        // 允许应用不经 OAuth/SAML 握手直接 deep-link 到重置凭证
        if (authSession == null && code == null && clientData == null) {
            if (!realm.isResetPasswordAllowed()) {
                event.error(Errors.NOT_ALLOWED);
                return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.RESET_CREDENTIAL_NOT_ALLOWED);

            }
            authSession = createAuthenticationSessionForClient(clientId, redirectUri);
            processLocaleParam(authSession);
            return processResetCredentials(false, null, authSession, null);
        }

        return resetCredentials(authSessionId, code, execution, clientId, tabId, clientData);
    }

    /**
     * 为重置凭证等场景创建独立认证会话。
     * @param clientID 客户端 ID（null 则用 system client）
     * @param redirectUriParam 重定向 URI（可选）
     */
    AuthenticationSessionModel createAuthenticationSessionForClient(String clientID, String redirectUriParam)
            throws UriBuilderException, IllegalArgumentException {
        AuthenticationSessionModel authSession;

        ClientModel client;
        String redirectUri = null;

        if (clientID == null) {
            if (redirectUriParam != null) {
                logger.warn("Unsupported to send 'redirect_uri' parameter without providing 'client_id' parameter.");
                throw new ErrorPageException(session, null, Response.Status.BAD_REQUEST, Messages.MISSING_PARAMETER, OIDCLoginProtocol.CLIENT_ID_PARAM);
            }
            client = SystemClientUtil.getSystemClient(realm);
            redirectUri = Urls.accountBase(session.getContext().getUri().getBaseUri()).path("/").build(realm.getName()).toString();
        } else {
            client = session.clients().getClientByClientId(realm, clientID);
            if (client == null) {
                throw new ErrorPageException(session, null, Response.Status.BAD_REQUEST, Messages.CLIENT_NOT_FOUND);
            }
            if (!client.isEnabled()) {
                throw new ErrorPageException(session, null, Response.Status.BAD_REQUEST, Messages.CLIENT_DISABLED);
            }
            if (redirectUriParam != null) {
                redirectUri = RedirectUtils.verifyRedirectUri(session, redirectUriParam, client);
                if (redirectUri == null) {
                    throw new ErrorPageException(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_PARAMETER, OIDCLoginProtocol.REDIRECT_URI_PARAM);
                }
            }
        }

        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, true);
        authSession = rootAuthSession.createAuthenticationSession(client);

        authSession.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OAuth2Constants.CODE);
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));

        if (redirectUri != null) {
            authSession.setRedirectUri(redirectUri);
            authSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        } else {
            authSession.setAuthNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS, "true");
        }
        return authSession;
    }

    /**
     * @param code
     * @param execution
     * @return
     */
    /** 重置凭证流程共用逻辑 */
    protected Response resetCredentials(String authSessionId, String code, String execution, String clientId, String tabId, String clientData) {
        SessionCodeChecks checks = checksForCode(authSessionId, code, execution, clientId, tabId, clientData, RESET_CREDENTIALS_PATH);
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(), ClientSessionCode.ActionType.USER)) {
            return checks.getResponse();
        }
        final AuthenticationSessionModel authSession = checks.getAuthenticationSession();

        if (!realm.isResetPasswordAllowed()) {
            event.error(Errors.NOT_ALLOWED);
            return ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.RESET_CREDENTIAL_NOT_ALLOWED);

        }

        return processResetCredentials(checks.isActionRequest(), execution, authSession, null);
    }

    /**
     * 执行 Action Token（邮件链接等）并统一处理 VerificationException If there is any {@link VerificationException} thrown
     * in the handler, it is handled automatically here to reduce boilerplate code.
     *
     * @param key
     * @param execution
     * @return
     */
    @Path("action-token")
    @GET
    /** Action Token GET 入口 */
    public Response executeActionToken(@QueryParam(AUTH_SESSION_ID) String authSessionId,
                                       @QueryParam(Constants.KEY) String key,
                                       @QueryParam(Constants.EXECUTION) String execution,
                                       @QueryParam(Constants.CLIENT_ID) String clientId,
                                       @QueryParam(Constants.CLIENT_DATA) String clientData,
                                       @QueryParam(Constants.TAB_ID) String tabId) {
        return handleActionToken(key, execution, clientId, tabId, clientData, null);
    }

    /**
     * 忽略邮件服务器对 action-token 的 HEAD 探测请求
     * as they are usually used by mail servers to validate links. The actual request will eventually be
     * processed by the {@link #executeActionToken} method.
     *
     * @return a {@link Response.Status#OK} response with no message body
     */
    @Path("action-token")
    @HEAD
    /** {@inheritDoc} 返回 200 空响应 */
    public Response executeActionTokenHead() {
        return Response.ok().build();
    }

    /**
     * Action Token 核心处理：验签、用户/客户端校验、处理器分发。
     * @param preHandleToken 可选预处理器（组织邀请等）
     */
    protected <T extends JsonWebToken & SingleUseObjectKeyModel> Response handleActionToken(String tokenString, String execution, String clientId, String tabId, String clientData, 
            TriFunction<ActionTokenHandler<T>, T, ActionTokenContext<T>, Response> preHandleToken) {
        T token;
        ActionTokenHandler<T> handler;
        ActionTokenContext<T> tokenContext;
        String eventError = null;
        String defaultErrorMessage = null;

        AuthenticationSessionModel authSession = null;

        // 预设 client 以便错误页显示「返回应用」
        ClientModel client = null;
        if (clientId != null) {
            client = realm.getClientByClientId(clientId);
        }
        AuthenticationSessionManager authenticationSessionManager = new AuthenticationSessionManager(session);
        KeycloakContext sessionContext = session.getContext();

        if (client != null) {
            sessionContext.setClient(client);
            authSession = authenticationSessionManager.getCurrentAuthenticationSession(realm, client, tabId);
        }

        event.event(EventType.EXECUTE_ACTION_TOKEN);

        // 解析 Action Token 处理器并验签
        try {
            if (tokenString == null) {
                throw new ExplainedTokenVerificationException(null, Errors.NOT_ALLOWED, Messages.INVALID_REQUEST);
            }

            TokenVerifier<DefaultActionTokenKey> tokenVerifier = TokenVerifier.create(tokenString, DefaultActionTokenKey.class);
            DefaultActionTokenKey aToken = tokenVerifier.getToken();

            event
              .detail(Details.TOKEN_ID, aToken.getId())
              .detail(Details.ACTION, aToken.getActionId())
              .user(aToken.getUserId());

            handler = resolveActionTokenHandler(aToken.getActionId());
            eventError = handler.getDefaultEventError();
            defaultErrorMessage = handler.getDefaultErrorMessage();

            if (! realm.isEnabled()) {
                throw new ExplainedTokenVerificationException(aToken, Errors.REALM_DISABLED, Messages.REALM_NOT_ENABLED);
            }
            if (! checkSsl()) {
                throw new ExplainedTokenVerificationException(aToken, Errors.SSL_REQUIRED, Messages.HTTPS_REQUIRED);
            }

            TokenVerifier<DefaultActionTokenKey> verifier = tokenVerifier
                    .withChecks(
                            // 令牌自省检查（活跃、issuer 等）
                            TokenVerifier.IS_ACTIVE,
                            new TokenVerifier.RealmUrlCheck(Urls.realmIssuer(sessionContext.getUri().getBaseUri(), realm.getName())),
                            ACTION_TOKEN_BASIC_CHECKS
                    );

            String kid = verifier.getHeader().getKeyId();
            String algorithm = verifier.getHeader().getAlgorithm().name();

            SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, algorithm);
            if (signatureProvider == null) {
                throw new ExplainedTokenVerificationException(aToken, Errors.INVALID_SIGNATURE, Messages.INVALID_REQUEST);
            }
            SignatureVerifierContext signatureVerifier = signatureProvider.verifier(kid);
            verifier.verifierContext(signatureVerifier);

            verifier.verify();

            token = TokenVerifier.create(tokenString, handler.getTokenClass()).getToken();
        } catch (TokenNotActiveException ex) {
            if (authSession != null) {
                event.clone().error(Errors.EXPIRED_CODE);
                String flowPath = authSession.getClientNote(AuthorizationEndpointBase.APP_INITIATED_FLOW);
                if (flowPath == null) {
                    flowPath = AUTHENTICATE_PATH;
                }
                AuthenticationProcessor.resetFlow(authSession, flowPath);

                // 令牌过期但存在会话时重置流程并继续
                return processFlowFromPath(flowPath, authSession, Messages.EXPIRED_ACTION_TOKEN_SESSION_EXISTS);
            }

            return handleActionTokenVerificationException(null, ex, Errors.EXPIRED_CODE, Messages.EXPIRED_ACTION_TOKEN_NO_SESSION);
        } catch (ExplainedTokenVerificationException ex) {
            return handleActionTokenVerificationException(null, ex, ex.getErrorEvent(), ex.getMessage());
        } catch (ExplainedVerificationException ex) {
            return handleActionTokenVerificationException(null, ex, ex.getErrorEvent(), ex.getMessage());
        } catch (VerificationException ex) {
            return handleActionTokenVerificationException(null, ex, eventError, defaultErrorMessage);
        }

        // 完成校验并调用 handler.handleToken
        tokenContext = new ActionTokenContext<>(session, realm, sessionContext.getUri(), clientConnection, request, event, handler, execution, clientData, this::processFlow, this::brokerLoginFlow);

        if (preHandleToken != null) {
            KeycloakContext context = session.getContext();
            authSession = context.getAuthenticationSession();
            if (authSession != null) {
                tokenContext.setAuthenticationSession(authSession, false);
            }
            return preHandleToken.apply(handler, token, tokenContext);
        }

        try {
            String tokenAuthSessionCompoundId = handler.getAuthenticationSessionIdFromToken(token, tokenContext, authSession);

            if (authSession == null) {
                authSession = handler.startFreshAuthenticationSession(token, tokenContext);
                tokenContext.setAuthenticationSession(authSession, true);
            } else if (!LoginActionsServiceChecks.doesAuthenticationSessionFromCookieMatchOneFromToken(tokenContext, authSession, tokenAuthSessionCompoundId)) {
                // There exists an authentication session but no auth session ID was received in the action token
                logger.debugf("Authentication session in progress but no authentication session ID was found in action token %s, restarting.", token.getId());
                authenticationSessionManager.removeAuthenticationSession(realm, authSession, false);

                authSession = handler.startFreshAuthenticationSession(token, tokenContext);
                tokenContext.setAuthenticationSession(authSession, true);

                processLocaleParam(authSession);
            }

            sessionContext.setAuthenticationSession(authSession);
            initLoginEvent(authSession);
            event.event(handler.eventType());

            LoginActionsServiceChecks.checkIsUserValid(token, tokenContext, event);
            LoginActionsServiceChecks.checkIsClientValid(token, tokenContext);

            sessionContext.setClient(authSession.getClient());

            TokenVerifier.createWithoutSignature(token)
              .withChecks(handler.getVerifiers(tokenContext))
              .verify();

            authSession = tokenContext.getAuthenticationSession();
            event = tokenContext.getEvent();
            event.event(handler.eventType());

            if (! handler.canUseTokenRepeatedly(token, tokenContext)) {
                LoginActionsServiceChecks.checkTokenWasNotUsedYet(token, tokenContext);
                authSession.setAuthNote(AuthenticationManager.INVALIDATE_ACTION_TOKEN, token.serializeKey());
            }

            authSession.setAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID, token.getUserId());

            authSession.setAuthNote(Constants.KEY, tokenString);

            return handler.handleToken(token, tokenContext);
        } catch (ExplainedTokenVerificationException ex) {
            return handleActionTokenVerificationException(tokenContext, ex, ex.getErrorEvent(), ex.getMessage());
        } catch (LoginActionsServiceException ex) {
            Response response = ex.getResponse();
            return response == null
              ? handleActionTokenVerificationException(tokenContext, ex, eventError, defaultErrorMessage)
              : response;
        } catch (VerificationException ex) {
            return handleActionTokenVerificationException(tokenContext, ex, eventError, defaultErrorMessage);
        }
    }

    /** 根据 flowPath 分发到 authenticate/registration/reset-credentials */
    private Response processFlowFromPath(String flowPath, AuthenticationSessionModel authSession, String errorMessage) {
        if (AUTHENTICATE_PATH.equals(flowPath)) {
            return processAuthentication(false, null, authSession, errorMessage);
        } else if (REGISTRATION_PATH.equals(flowPath)) {
            return processRegistration(false, null, authSession, errorMessage);
        } else if (RESET_CREDENTIALS_PATH.equals(flowPath)) {
            return processResetCredentials(false, null, authSession, errorMessage);
        } else {
            return ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, errorMessage == null ? Messages.INVALID_REQUEST : errorMessage);
        }
    }

    /** 按 actionId 解析 ActionTokenHandler SPI */
    private <T extends JsonWebToken> ActionTokenHandler<T> resolveActionTokenHandler(String actionId) throws VerificationException {
        if (actionId == null) {
            throw new VerificationException("Action token operation not set");
        }
        ActionTokenHandler<T> handler = session.getProvider(ActionTokenHandler.class, actionId);

        if (handler == null) {
            throw new VerificationException("Invalid action token operation");
        }
        return handler;
    }

    /** 统一处理 Action Token 验证失败 */
    private Response handleActionTokenVerificationException(ActionTokenContext<?> tokenContext, VerificationException ex, String eventError, String errorMessage) {
        if (tokenContext != null && tokenContext.getAuthenticationSession() != null) {
            new AuthenticationSessionManager(session).removeAuthenticationSession(realm, tokenContext.getAuthenticationSession(), true);
        }

        event
          .detail(Details.REASON, ex == null ? "<unknown>" : ex.getMessage())
          .error(eventError == null ? Errors.INVALID_CODE : eventError);
        return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, errorMessage == null ? Messages.INVALID_CODE : errorMessage);
    }

    /** 执行重置凭证认证流程 */
    protected Response processResetCredentials(boolean actionRequest, String execution, AuthenticationSessionModel authSession, String errorMessage) {
        AuthenticationProcessor authProcessor = new ResetCredentialsActionTokenHandler.ResetCredsAuthenticationProcessor();

        return processFlow(actionRequest, execution, authSession, RESET_CREDENTIALS_PATH, realm.getResetCredentialsFlow(), errorMessage, authProcessor);
    }


    /** 执行用户注册认证流程 */
    protected Response processRegistration(boolean action, String execution, AuthenticationSessionModel authSession, String errorMessage) {
        return processFlow(action, execution, authSession, REGISTRATION_PATH, realm.getRegistrationFlow(), errorMessage, new AuthenticationProcessor());
    }


    /**
     * 协议无关的注册页 GET 入口
     *
     * @param code
     * @return
     */
    @Path(REGISTRATION_PATH)
    @GET
    /** {@inheritDoc} */
    public Response registerPage(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                 @QueryParam(SESSION_CODE) String code,
                                 @QueryParam(Constants.EXECUTION) String execution,
                                 @QueryParam(Constants.CLIENT_ID) String clientId,
                                 @QueryParam(Constants.CLIENT_DATA) String clientData,
                                 @QueryParam(Constants.TAB_ID) String tabId,
                                 @QueryParam(Constants.TOKEN) String tokenString) {
        return registerRequest(authSessionId, code, execution, clientId,  tabId,clientData, tokenString);
    }


    /**
     * 注册表单 POST 入口
     *
     * @param code
     * @return
     */
    @Path(REGISTRATION_PATH)
    @POST
    /** {@inheritDoc} */
    public Response processRegister(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                    @QueryParam(SESSION_CODE) String code,
                                    @QueryParam(Constants.EXECUTION) String execution,
                                    @QueryParam(Constants.CLIENT_ID) String clientId,
                                    @QueryParam(Constants.CLIENT_DATA) String clientData,
                                    @QueryParam(Constants.TAB_ID) String tabId,
                                    @QueryParam(Constants.TOKEN) String tokenString) {
        return registerRequest(authSessionId, code, execution, clientId, tabId, clientData, tokenString);
    }


    /** 注册请求共用逻辑（含组织邀请 token 预处理） */
    private Response registerRequest(String authSessionId, String code, String execution, String clientId, String tabId, String clientData, String tokenString) {
        event.event(EventType.REGISTER);

        SessionCodeChecks checks = checksForCode(authSessionId, code, execution, clientId, tabId, clientData, REGISTRATION_PATH);
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(), ClientSessionCode.ActionType.LOGIN)) {
            return checks.getResponse();
        }

        AuthenticationSessionModel authSession = checks.getAuthenticationSession();

        session.getContext().setAuthenticationSession(authSession);

        processLocaleParam(authSession);

        AuthenticationManager.expireIdentityCookie(session);

        if (Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION) && tokenString != null) {
            // 组织特性：从邀请 token 提取 orgId 写入上下文
            Response response = preHandleActionToken(tokenString);
            // restore event type because handleActionToken() overwrites it to EXECUTE_ACTION_TOKEN
            event.event(EventType.REGISTER);

            if (response != null) {
                return response;
            }
        }

        if (!Organizations.isRegistrationAllowed(session, realm)) {
            event.error(Errors.REGISTRATION_DISABLED);
            return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.REGISTRATION_NOT_ALLOWED);
        }

        return processRegistration(checks.isActionRequest(), execution, authSession, null);
    }


    @Path(FIRST_BROKER_LOGIN_PATH)
    @GET
    /** 首次 Broker 登录 GET */
    public Response firstBrokerLoginGet(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                        @QueryParam(SESSION_CODE) String code,
                                        @QueryParam(Constants.EXECUTION) String execution,
                                        @QueryParam(Constants.CLIENT_ID) String clientId,
                                        @QueryParam(Constants.CLIENT_DATA) String clientData,
                                        @QueryParam(Constants.TAB_ID) String tabId) {
        return brokerLoginFlow(authSessionId, code, execution, clientId, tabId, clientData, FIRST_BROKER_LOGIN_PATH);
    }

    @Path(FIRST_BROKER_LOGIN_PATH)
    @POST
    /** 首次 Broker 登录 POST */
    public Response firstBrokerLoginPost(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                         @QueryParam(SESSION_CODE) String code,
                                         @QueryParam(Constants.EXECUTION) String execution,
                                         @QueryParam(Constants.CLIENT_ID) String clientId,
                                         @QueryParam(Constants.CLIENT_DATA) String clientData,
                                         @QueryParam(Constants.TAB_ID) String tabId) {
        return brokerLoginFlow(authSessionId, code, execution, clientId, tabId, clientData, FIRST_BROKER_LOGIN_PATH);
    }

    @Path(POST_BROKER_LOGIN_PATH)
    @GET
    /** post-broker-login GET */
    public Response postBrokerLoginGet(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                       @QueryParam(SESSION_CODE) String code,
                                       @QueryParam(Constants.EXECUTION) String execution,
                                       @QueryParam(Constants.CLIENT_ID) String clientId,
                                       @QueryParam(Constants.CLIENT_DATA) String clientData,
                                       @QueryParam(Constants.TAB_ID) String tabId) {
        return brokerLoginFlow(authSessionId, code, execution, clientId, tabId, clientData, POST_BROKER_LOGIN_PATH);
    }

    @Path(POST_BROKER_LOGIN_PATH)
    @POST
    /** post-broker-login POST */
    public Response postBrokerLoginPost(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                        @QueryParam(SESSION_CODE) String code,
                                        @QueryParam(Constants.EXECUTION) String execution,
                                        @QueryParam(Constants.CLIENT_ID) String clientId,
                                        @QueryParam(Constants.CLIENT_DATA) String clientData,
                                        @QueryParam(Constants.TAB_ID) String tabId) {
        return brokerLoginFlow(authSessionId, code, execution, clientId, tabId, clientData, POST_BROKER_LOGIN_PATH);
    }


    /** 首次/post Broker 登录流程共用实现 */
    protected Response brokerLoginFlow(String authSessionId, String code, String execution, String clientId, String tabId, String clientData, String flowPath) {
        boolean firstBrokerLogin = flowPath.equals(FIRST_BROKER_LOGIN_PATH);

        EventType eventType = firstBrokerLogin ? EventType.IDENTITY_PROVIDER_FIRST_LOGIN : EventType.IDENTITY_PROVIDER_POST_LOGIN;
        event.event(eventType);

        SessionCodeChecks checks = checksForCode(authSessionId, code, execution, clientId, tabId, clientData, flowPath);
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(), ClientSessionCode.ActionType.LOGIN)) {
            event.error("Failed to verify login action");
            return checks.getResponse();
        }
        event.detail(Details.CODE_ID, code);
        final AuthenticationSessionModel authSession = checks.getAuthenticationSession();

        processLocaleParam(authSession);

        String noteKey = firstBrokerLogin ? AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE : PostBrokerLoginConstants.PBL_BROKERED_IDENTITY_CONTEXT;
        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authSession, noteKey);
        if (serializedCtx == null) {
            ServicesLogger.LOGGER.notFoundSerializedCtxInClientSession(noteKey);
            String message = "Not found serialized context in authenticationSession.";
            event.error(message);
            throw new WebApplicationException(ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, message));
        }
        BrokeredIdentityContext brokerContext = serializedCtx.deserialize(session, authSession);
        final String identityProviderAlias = brokerContext.getIdpConfig().getAlias();

        String flowId;
        if (firstBrokerLogin) {
            flowId = brokerContext.getIdpConfig().getFirstBrokerLoginFlowId();
            if (flowId == null) {
                flowId = realm.getFirstBrokerLoginFlow().getId();
            }
        } else {
            flowId = brokerContext.getIdpConfig().getPostBrokerLoginFlowId();
        }
        if (flowId == null) {
            ServicesLogger.LOGGER.flowNotConfigForIDP(identityProviderAlias);
            String message = "Flow not configured for identity provider";
            event.error(message);
            throw new WebApplicationException(ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, message));
        }
        AuthenticationFlowModel brokerLoginFlow = realm.getAuthenticationFlowById(flowId);
        if (brokerLoginFlow == null) {
            ServicesLogger.LOGGER.flowNotFoundForIDP(flowId, identityProviderAlias);
            String message = "Flow not found for identity provider";
            event.error(message);
            throw new WebApplicationException(ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, message));
        }

        event.detail(Details.IDENTITY_PROVIDER, identityProviderAlias)
                .detail(Details.IDENTITY_PROVIDER_USERNAME, brokerContext.getUsername())
                .detail(Details.IDENTITY_PROVIDER_BROKER_SESSION_ID, brokerContext.getBrokerSessionId());

        AuthenticationProcessor processor = new AuthenticationProcessor() {

            @Override
            public Response authenticateOnly() throws AuthenticationFlowException {
                Response challenge = super.authenticateOnly();
                if (challenge != null) {
                    if ("true".equals(authenticationSession.getAuthNote(FORWARDED_PASSIVE_LOGIN))) {
                        // prompt=none 与 broker 流程 challenge 不兼容 created by the broker flows.
                        logger.errorf("Challenge encountered when executing %s flow. Auth requests with prompt=none are incompatible with challenges", flowPath);
                        LoginProtocol protocol = session.getProvider(LoginProtocol.class, authSession.getProtocol());
                        protocol.setRealm(realm)
                                .setHttpHeaders(headers)
                                .setUriInfo(session.getContext().getUri())
                                .setEventBuilder(event);
                        return protocol.sendError(authSession, Error.PASSIVE_INTERACTION_REQUIRED, null);
                    }
                }
                return challenge;
            }

            @Override
            protected Response authenticationComplete() {
                if (firstBrokerLogin) {
                    authSession.setAuthNote(AbstractIdpAuthenticator.FIRST_BROKER_LOGIN_SUCCESS, identityProviderAlias);
                } else {
                    String authStateNoteKey = PostBrokerLoginConstants.PBL_AUTH_STATE_PREFIX + identityProviderAlias;
                    authSession.setAuthNote(authStateNoteKey, "true");
                }

                return redirectToAfterBrokerLoginEndpoint(authSession, firstBrokerLogin);
            }

        };

        configureOrganization(brokerContext);

        Response response = processFlow(checks.isActionRequest(), execution, authSession, flowPath, brokerLoginFlow, null, processor);
        event.success();

        return response;
    }

    /** 若 IdP 绑定组织则写入会话组织上下文 */
    private void configureOrganization(BrokeredIdentityContext brokerContext) {
        if (Organizations.isEnabled(session)) {
            String organizationId = brokerContext.getIdpConfig().getOrganizationId();

            if (organizationId != null) {
                OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
                session.getContext().setOrganization(provider.getById(organizationId));
                session.setAttribute(BrokeredIdentityContext.class.getName(), brokerContext);
            }
        }
    }

    /** 重定向到 IdentityBrokerService after-*-broker-login 端点 */
    private Response redirectToAfterBrokerLoginEndpoint(AuthenticationSessionModel authSession, boolean firstBrokerLogin) {
        return redirectToAfterBrokerLoginEndpoint(session, realm, session.getContext().getUri(), authSession, firstBrokerLogin);
    }

    /** 静态方法：构建 after broker login 重定向 URL */
    public static Response redirectToAfterBrokerLoginEndpoint(KeycloakSession session, RealmModel realm, UriInfo uriInfo, AuthenticationSessionModel authSession, boolean firstBrokerLogin) {
        ClientSessionCode<AuthenticationSessionModel> accessCode = new ClientSessionCode<>(session, realm, authSession);
        authSession.getParentSession().setTimestamp(Time.currentTime());

        String clientId = authSession.getClient().getClientId();
        String tabId = authSession.getTabId();
        String clientData = AuthenticationProcessor.getClientData(session, authSession);
        URI redirect = firstBrokerLogin ? Urls.identityProviderAfterFirstBrokerLogin(uriInfo.getBaseUri(), realm.getName(), accessCode.getOrGenerateCode(), clientId, tabId, clientData) :
                Urls.identityProviderAfterPostBrokerLogin(uriInfo.getBaseUri(), realm.getName(), accessCode.getOrGenerateCode(), clientId, tabId, clientData) ;
        logger.debugf("Redirecting to '%s' ", redirect);

        return Response.status(302).location(redirect).build();
    }

    /** OAuth 同意页：检查 scope 是否需写入 consent */
    private boolean checkGranted(AuthorizationDetails details, UserConsentModel grantedConsent, List<String> alwaysConsent) {
        ClientScopeModel clientScope = details.getClientScope();
        String parameter = details.getParameterizedScopeParam();
        if (clientScope.isDisplayOnConsentScreen() && !clientScope.isAlwaysConsent()
                && !grantedConsent.isClientScopeGranted(clientScope, parameter)) {
            grantedConsent.addGrantedClientScope(clientScope, parameter);
            return true;
        } else if (clientScope.isAlwaysConsent()) {
            String scope = parameter != null
                    ? clientScope.getName() + ClientScopeModel.VALUE_SEPARATOR + parameter
                    : clientScope.getName();
            alwaysConsent.add(scope);
        }
        return false;
    }

    /**
     * OAuth 同意页 POST（勿直接调用）
     *
     * @return
     */
    @Path("consent")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    /** 处理用户 OAuth scope 同意或拒绝 */
    public Response processConsent() {
        MultivaluedMap<String, String> formData = request.getDecodedFormParameters();
        event.event(EventType.LOGIN);
        String code = formData.getFirst(SESSION_CODE);
        String clientId = session.getContext().getUri().getQueryParameters().getFirst(Constants.CLIENT_ID);
        String tabId = session.getContext().getUri().getQueryParameters().getFirst(Constants.TAB_ID);
        String clientData = session.getContext().getUri().getQueryParameters().getFirst(Constants.CLIENT_DATA);
        SessionCodeChecks checks = checksForCode(null, code, null, clientId, tabId, clientData, REQUIRED_ACTION);
        if (!checks.verifyRequiredAction(AuthenticationSessionModel.Action.OAUTH_GRANT.name())) {
            return checks.getResponse();
        }

        AuthenticationSessionModel authSession = checks.getAuthenticationSession();

        initLoginEvent(authSession);

        UserModel user = authSession.getAuthenticatedUser();
        ClientModel client = authSession.getClient();


        if (formData.containsKey("cancel")) {
            LoginProtocol protocol = session.getProvider(LoginProtocol.class, authSession.getProtocol());
            protocol.setRealm(realm)
                    .setHttpHeaders(headers)
                    .setUriInfo(session.getContext().getUri())
                    .setEventBuilder(event);
            Response response = protocol.sendError(authSession, Error.CONSENT_DENIED, null);
            event.error(Errors.REJECTED_BY_USER);
            return response;
        }

        if (DeviceGrantType.isDeviceCodeDeniedForDeviceVerificationFlow(session, realm, authSession)) {
            event.error(Errors.REJECTED_BY_USER);
            return DeviceGrantType.denyOAuth2DeviceAuthorization(authSession, Error.CONSENT_DENIED, session);
        }

        UserConsentModel existingConsent = UserConsentManager.getConsentByClient(session, realm, user, client.getId());
        UserConsentModel grantedConsent;
        if (existingConsent == null) {
            grantedConsent = new UserConsentModel(client);
            UserConsentManager.addConsent(session, realm, user, grantedConsent);
        } else {
            grantedConsent = existingConsent;
        }

        // prompt=consent 时可能所有 scope 已授权，无需更新 (May happen for example with prompt=consent)
        List<String> alwaysConsent = new LinkedList<>();
        Boolean updateConsentRequired = AuthenticationManager.getClientScopeModelStream(session, client)
                .map(d -> checkGranted(d, grantedConsent, alwaysConsent))
                .reduce(Boolean::logicalOr).orElse(Boolean.FALSE);

        if (updateConsentRequired) {
            UserConsentManager.updateConsent(session, realm, user, grantedConsent);
        }

        if (!alwaysConsent.isEmpty()) {
            authSession.setClientNote(OIDCLoginProtocol.CONSENT_NOTE, String.join(" ", alwaysConsent));
        }

        event.detail(Details.CONSENT, Details.CONSENT_VALUE_CONSENT_GRANTED);

        ClientSessionContext clientSessionCtx = AuthenticationProcessor.attachSession(authSession, null, session, realm, clientConnection, event);
        event.success();
        return AuthenticationManager.redirectAfterSuccessfulFlow(session, realm, clientSessionCtx.getClientSession().getUserSession(), clientSessionCtx, request, session.getContext().getUri(), clientConnection, event, authSession);
    }

    /** 用认证会话信息填充 LOGIN 事件详情 */
    private void initLoginEvent(AuthenticationSessionModel authSession) {
        String responseType = authSession.getClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM);
        if (responseType == null) {
            responseType = "code";
        }
        String respMode = authSession.getClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM);
        OIDCResponseMode responseMode = OIDCResponseMode.parse(respMode, OIDCResponseType.parse(responseType));

        event.event(EventType.LOGIN).client(authSession.getClient())
                .detail(Details.CODE_ID, authSession.getParentSession().getId())
                .detail(Details.REDIRECT_URI, authSession.getRedirectUri())
                .detail(Details.AUTH_METHOD, authSession.getProtocol())
                .detail(Details.RESPONSE_TYPE, responseType)
                .detail(Details.RESPONSE_MODE, responseMode.toString().toLowerCase());

        UserModel authenticatedUser = authSession.getAuthenticatedUser();
        if (authenticatedUser != null) {
            event.user(authenticatedUser)
                    .detail(Details.USERNAME, authenticatedUser.getUsername());
        }

        String attemptedUsername = authSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);
        if (attemptedUsername != null) {
            event.detail(Details.USERNAME, attemptedUsername);
        }

        String rememberMe = authSession.getAuthNote(Details.REMEMBER_ME);
        if (rememberMe==null || !rememberMe.equalsIgnoreCase("true")) {
            rememberMe = "false";
        }
        event.detail(Details.REMEMBER_ME, rememberMe);

        Map<String, String> userSessionNotes = authSession.getUserSessionNotes();
        String identityProvider = userSessionNotes.get(Details.IDENTITY_PROVIDER);
        if (identityProvider != null) {
            event.detail(Details.IDENTITY_PROVIDER, identityProvider)
                    .detail(Details.IDENTITY_PROVIDER_USERNAME, userSessionNotes.get(Details.IDENTITY_PROVIDER_USERNAME));
        }
    }

    @Path(REQUIRED_ACTION)
    @POST
    /** required-action 表单 POST */
    public Response requiredActionPOST(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                       @QueryParam(SESSION_CODE) final String code,
                                       @QueryParam(Constants.EXECUTION) String action,
                                       @QueryParam(Constants.CLIENT_ID) String clientId,
                                       @QueryParam(Constants.CLIENT_DATA) String clientData,
                                       @QueryParam(Constants.TAB_ID) String tabId) {
        return processRequireAction(authSessionId, code, action, clientId, tabId, clientData);
    }

    @Path(REQUIRED_ACTION)
    @GET
    /** required-action 页面 GET */
    public Response requiredActionGET(@QueryParam(AUTH_SESSION_ID) String authSessionId, // optional, can get from cookie instead
                                      @QueryParam(SESSION_CODE) final String code,
                                      @QueryParam(Constants.EXECUTION) String action,
                                      @QueryParam(Constants.CLIENT_ID) String clientId,
                                      @QueryParam(Constants.CLIENT_DATA) String clientData,
                                      @QueryParam(Constants.TAB_ID) String tabId) {
        return processRequireAction(authSessionId, code, action, clientId, tabId, clientData);
    }

    /** required-action 流程：展示或处理用户/custom required action */
    private Response processRequireAction(final String authSessionId, final String code, String action, String clientId, String tabId, String clientData) {
        event.event(EventType.CUSTOM_REQUIRED_ACTION);

        SessionCodeChecks checks = checksForCode(authSessionId, code, action, clientId, tabId, clientData, REQUIRED_ACTION);
        if (!checks.verifyRequiredAction(action)) {
            return checks.getResponse();
        }

        AuthenticationSessionModel authSession = checks.getAuthenticationSession();

        processLocaleParam(authSession);

        if (!checks.isActionRequest()) {
            initLoginEvent(authSession);
            event.event(EventType.CUSTOM_REQUIRED_ACTION);
            return AuthenticationManager.nextActionAfterAuthentication(session, authSession, clientConnection, request, session.getContext().getUri(), event);
        }

        initLoginEvent(authSession);
        event.event(EventType.CUSTOM_REQUIRED_ACTION);
        event.detail(Details.CUSTOM_REQUIRED_ACTION, action);

        RequiredActionFactory factory = (RequiredActionFactory)session.getKeycloakSessionFactory().getProviderFactory(RequiredActionProvider.class, getDefaultRequiredActionCaseInsensitively(action));
        if (factory == null) {
            ServicesLogger.LOGGER.actionProviderNull();
            event.error(Errors.INVALID_CODE);
            throw new WebApplicationException(ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.INVALID_CODE));
        }
        RequiredActionContextResult context = new RequiredActionContextResult(authSession, realm, event, session, request, authSession.getAuthenticatedUser(), factory) {
            @Override
            public void ignore() {
                throw new RuntimeException("Cannot call ignore within processAction()");
            }
        };
        RequiredActionProvider provider = null;
        try {
            provider = AuthenticationManager.createRequiredAction(context);
        }  catch (AuthenticationFlowException e) {
            if (e.getResponse() != null) {
                return e.getResponse();
            }
            throw new WebApplicationException(ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.DISPLAY_UNSUPPORTED));
        }


        Response response;

        if (isCancelAppInitiatedAction(factory.getId(), authSession, context)) {
            provider.initiatedActionCanceled(session, authSession);
            AuthenticationManager.setKcActionStatus(factory.getId(), RequiredActionContext.KcActionStatus.CANCELLED, authSession);
            context.cancel();
        } else {
            provider.processAction(context);
        }

        if (action != null) {
            authSession.setAuthNote(AuthenticationProcessor.LAST_PROCESSED_EXECUTION, action);
        }

        if (context.getStatus() == RequiredActionContext.Status.CANCELLED) {
            event.clone().error(Errors.REJECTED_BY_USER);
            initLoginEvent(authSession);
            event.event(EventType.LOGIN);
            authSession.removeAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
            AuthenticationManager.setKcActionStatus(factory.getId(), RequiredActionContext.KcActionStatus.CANCELLED, authSession);
            response = AuthenticationManager.nextActionAfterAuthentication(session, authSession, clientConnection, request, session.getContext().getUri(), event);
        } else if (context.getStatus() == RequiredActionContext.Status.SUCCESS) {
            event.clone().success();
            initLoginEvent(authSession);
            event.event(EventType.LOGIN);
            authSession.removeRequiredAction(factory.getId());
            authSession.getAuthenticatedUser().removeRequiredAction(factory.getId());
            authSession.removeAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
            AuthenticationManager.setKcActionStatus(factory.getId(), RequiredActionContext.KcActionStatus.SUCCESS, authSession);

            response = AuthenticationManager.nextActionAfterAuthentication(session, authSession, clientConnection, request, session.getContext().getUri(), event);
        } else if (context.getStatus() == RequiredActionContext.Status.CHALLENGE) {
            response = context.getChallenge();
        } else if (context.getStatus() == RequiredActionContext.Status.FAILURE) {
            response = interruptionResponse(context, authSession, action, Error.CONSENT_DENIED);
        } else {
            throw new RuntimeException("Unreachable");
        }

        return BrowserHistoryHelper.getInstance().saveResponseAndRedirect(session, authSession, response, true, request);
    }

    /** required-action 被用户拒绝时的协议错误响应 */
    private Response interruptionResponse(RequiredActionContextResult context, AuthenticationSessionModel authSession, String action, Error error) {
        LoginProtocol protocol = context.getSession().getProvider(LoginProtocol.class, authSession.getProtocol());
        protocol.setRealm(context.getRealm())
                .setHttpHeaders(context.getHttpRequest().getHttpHeaders())
                .setUriInfo(context.getUriInfo())
                .setEventBuilder(event);

        event.detail(Details.CUSTOM_REQUIRED_ACTION, action);

        event.error(Errors.REJECTED_BY_USER);
        return protocol.sendError(authSession, error, context.getErrorMessage());
    }

    /** 判断用户是否取消应用发起的 required-action（AIA） */
    private boolean isCancelAppInitiatedAction(String providerId, AuthenticationSessionModel authSession, RequiredActionContextResult context) {
        if (providerId.equals(authSession.getClientNote(Constants.KC_ACTION_EXECUTING))
                && !Boolean.TRUE.toString().equals(authSession.getClientNote(Constants.KC_ACTION_ENFORCED))) {
            MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
            boolean userRequestedCancelAIA = formData.getFirst(CANCEL_AIA) != null;
            return userRequestedCancelAIA;
        }
        return false;
    }

    /** 注册等场景预处理的 Action Token 入口 */
    public Response preHandleActionToken(String tokenString) {
        return handleActionToken(tokenString, null, null, null, null, ActionTokenHandler::preHandleToken);
    }
}
