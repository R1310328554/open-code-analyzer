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

package org.keycloak.protocol.oidc.endpoints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.TokenVerifier;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.VerificationException;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.headers.SecurityHeadersProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.SystemClientUtil;
import org.keycloak.protocol.oidc.BackchannelLogoutResponse;
import org.keycloak.protocol.oidc.LogoutTokenValidationCode;
import org.keycloak.protocol.oidc.LogoutTokenValidationContext;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.utils.AuthorizeClientUtil;
import org.keycloak.protocol.oidc.utils.LogoutUtil;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.LogoutToken;
import org.keycloak.representations.RefreshToken;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.LogoutRequestContext;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.LogoutSessionCodeChecks;
import org.keycloak.services.resources.SessionCodeChecks;
import org.keycloak.services.util.LocaleUtil;
import org.keycloak.services.util.MtlsHoKTokenUtil;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.models.UserSessionModel.State.LOGGED_OUT;
import static org.keycloak.models.UserSessionModel.State.LOGGING_OUT;
import static org.keycloak.services.resources.LoginActionsService.SESSION_CODE;

/**
 * OIDC 登出端点。
 * <p>支持 RP 发起的浏览器登出（GET/POST）、刷新令牌登出、登出确认、以及 OpenID Connect 后台通道登出（Back-Channel Logout）。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LogoutEndpoint {
    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(LogoutEndpoint.class);

    private final KeycloakSession session;

    private final ClientConnection clientConnection;

    private final HttpRequest request;

    private final HttpHeaders headers;

    private final TokenManager tokenManager;
    private final RealmModel realm;
    private final EventBuilder event;

    /** CORS 构建器（令牌登出等 API 场景）。 */
    private Cors cors;

    /**
     * @param session Keycloak 会话
     * @param tokenManager 令牌管理器
     * @param event 事件构建器
     */
    public LogoutEndpoint(KeycloakSession session, TokenManager tokenManager, EventBuilder event) {
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.tokenManager = tokenManager;
        this.realm = session.getContext().getRealm();
        this.event = event;
        this.request = session.getContext().getHttpRequest();
        this.headers = session.getContext().getRequestHeaders();
    }

    @Path("/")
    @OPTIONS
    /** CORS 预检请求。 @return 204/200 */
    public Response issueUserInfoPreflight() {
        return Cors.builder().auth().preflight().add(Response.ok());
    }

    /**
     * RP 发起的浏览器登出（GET）。用户通常需持有会话 Cookie。
     * <p>远程 IdP 发起时可传 {@code initiating_idp} 以跳过上联登出。符合 <a href="https://openid.net/specs/openid-connect-rpinitiated-1_0.html#RPLogout">OIDC RP-Initiated Logout</a>。参数均可选，部分组合无效。</p>
     * @param encodedIdToken {@code id_token_hint}
     * @param clientId {@code client_id}
     * @param postLogoutRedirectUri 登出后重定向 URI
     * @param state 登出完成后回传应用的状态值
     * @param uiLocales 登出页面 UI 语言
     * @param initiatingIdp 发起登出的 IdP 别名
     * @return 确认页、执行登出或错误页
     */
    @GET
    @NoCache
    public Response logout(@QueryParam(OIDCLoginProtocol.ID_TOKEN_HINT) String encodedIdToken,
                           @QueryParam(OIDCLoginProtocol.CLIENT_ID_PARAM) String clientId,
                           @QueryParam(OIDCLoginProtocol.POST_LOGOUT_REDIRECT_URI_PARAM) String postLogoutRedirectUri,
                           @QueryParam(OIDCLoginProtocol.STATE_PARAM) String state,
                           @QueryParam(OIDCLoginProtocol.UI_LOCALES_PARAM) String uiLocales,
                           @QueryParam(AuthenticationManager.INITIATING_IDP_PARAM) String initiatingIdp) {

        if (postLogoutRedirectUri != null && encodedIdToken == null && clientId == null) {
            event.event(EventType.LOGOUT);
            String errorMessage = "Either the parameter 'client_id' or the parameter 'id_token_hint' is required when 'post_logout_redirect_uri' is used.";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_REQUEST);
            logger.warnf(errorMessage);
            return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.MISSING_PARAMETER,
                    OIDCLoginProtocol.ID_TOKEN_HINT);
        }

        boolean confirmationNeeded = true;
        boolean forcedConfirmation = false;
        ClientModel client = clientId == null ? null : realm.getClientByClientId(clientId);
        if (clientId != null && client == null) {
            logger.warnf("Client '%s' not found.", clientId);
            forcedConfirmation = true;
        }

        IDToken idToken = null;
        if (encodedIdToken != null) {
            try {
                idToken = tokenManager.verifyIDTokenSignature(session, encodedIdToken);
                TokenVerifier.createWithoutSignature(idToken).tokenType(Arrays.asList(TokenUtil.TOKEN_TYPE_ID)).verify();
            } catch (OAuthErrorException | VerificationException e) {
                event.event(EventType.LOGOUT);
                event.detail(Details.REASON, e.getMessage());
                event.error(Errors.INVALID_TOKEN);
                return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_PARAMETER, OIDCLoginProtocol.ID_TOKEN_HINT);
            }
        }

        if (clientId == null) {
            // 从 id_token_hint 解析客户端
            client = (idToken == null || idToken.getIssuedFor() == null) ? null : realm.getClientByClientId(idToken.getIssuedFor());
            if (client != null) {
                confirmationNeeded = false;
            }
        } else {
            // 校验 client_id 与 id_token_hint 指向同一客户端
            if (idToken != null && idToken.getIssuedFor() != null) {
                if (!idToken.getIssuedFor().equals(clientId)) {
                    event.event(EventType.LOGOUT);
                    event.client(clientId);
                    String errorMessage = "Parameter client_id is different than the client for which ID Token was issued.";
                    event.detail(Details.REASON, errorMessage);
                    event.error(Errors.INVALID_TOKEN);
                    logger.warnf("%s Parameter client_id: '%s', ID Token issued for: '%s'.", errorMessage, clientId, idToken.getIssuedFor());
                    return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_PARAMETER, OIDCLoginProtocol.ID_TOKEN_HINT);
                } else {
                    confirmationNeeded = false;
                }
            }
        }
        if (client != null) {
            session.getContext().setClient(client);
            event.client(client);
        }

        String validatedRedirectUri = null;
        if (postLogoutRedirectUri != null) {
            if (client != null) {
                OIDCAdvancedConfigWrapper wrapper = OIDCAdvancedConfigWrapper.fromClientModel(client);
                Set<String> postLogoutRedirectUris = wrapper.getPostLogoutRedirectUris() != null ? new HashSet(wrapper.getPostLogoutRedirectUris()) : new HashSet<>();
                validatedRedirectUri = RedirectUtils.verifyRedirectUri(session, client.getRootUrl(), postLogoutRedirectUri, postLogoutRedirectUris, true);
            }

            if (validatedRedirectUri == null) {
                event.event(EventType.LOGOUT);
                event.detail(Details.REDIRECT_URI, postLogoutRedirectUri);
                event.error(Errors.INVALID_REDIRECT_URI);
                return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_REDIRECT_URI);
            }
        }

        UserSessionModel userSession = null;

        // 浏览器会话与 id_token_hint 不一致时需显示确认页
        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(session, realm, false);
        if (authResult != null) {
            userSession = authResult.session();
            if (idToken != null && idToken.getSessionState() != null && !idToken.getSessionState().equals(authResult.session().getId())) {
                forcedConfirmation = true;
            }
        } else {
            // 无浏览器会话且无 id_token_hint 但有合法 post_logout_redirect_uri 时可跳过确认
            // We can do automatic redirect as there is no logout needed at all for this scenario (Session was probably already logged-out before)
            if (encodedIdToken == null && client != null && validatedRedirectUri != null) {
                confirmationNeeded = false;
            }
        }

        if (userSession == null && idToken != null && idToken.getSessionState() != null) {
            userSession = session.sessions().getUserSession(realm, idToken.getSessionState());
        }

        AuthenticationSessionModel logoutSession = AuthenticationManager.createOrJoinLogoutSession(session, realm,
                new AuthenticationSessionManager(session), userSession, true, true);
        session.getContext().setAuthenticationSession(logoutSession);
        if (uiLocales != null) {
            logoutSession.setClientNote(LocaleSelectorProvider.CLIENT_REQUEST_LOCALE, uiLocales);
        }
        if (validatedRedirectUri != null) {
            logoutSession.setAuthNote(OIDCLoginProtocol.LOGOUT_REDIRECT_URI, validatedRedirectUri);
        }
        if (state != null) {
            logoutSession.setAuthNote(OIDCLoginProtocol.LOGOUT_STATE_PARAM, state);
        }
        if (initiatingIdp != null) {
            logoutSession.setAuthNote(AuthenticationManager.LOGOUT_INITIATING_IDP, initiatingIdp);
        }
        if (idToken != null) {
            logoutSession.setAuthNote(OIDCLoginProtocol.LOGOUT_VALIDATED_ID_TOKEN_SESSION_STATE, idToken.getSessionState());
            logoutSession.setAuthNote(OIDCLoginProtocol.LOGOUT_VALIDATED_ID_TOKEN_ISSUED_AT, String.valueOf(idToken.getIat()));
        }

        LoginFormsProvider loginForm = session.getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(logoutSession);

        // 为本地化设置当前用户
        if (userSession != null) {
            UserModel user = userSession.getUser();
            logoutSession.setAuthenticatedUser(user);
            loginForm.setUser(user);
        }

        // 需要或强制显示登出确认页
        if (confirmationNeeded || forcedConfirmation) {
            return displayLogoutConfirmationScreen(loginForm, logoutSession);
        } else {
            return doBrowserLogout(logoutSession);
        }
    }

    private Response displayLogoutConfirmationScreen(LoginFormsProvider loginForm, AuthenticationSessionModel authSession) {
        ClientSessionCode<AuthenticationSessionModel> accessCode = new ClientSessionCode<>(session, realm, authSession);
        accessCode.setAction(AuthenticatedClientSessionModel.Action.LOGGING_OUT.name());

        return loginForm
                .setClientSessionCode(accessCode.getOrGenerateCode())
                .createLogoutConfirmPage();
    }

    /**
     * POST 登出：RP-Initiated Logout 表单参数，或带 {@code refresh_token} 的传统 API 登出（见 {@link #logoutToken}）。
     * @return 登出结果响应
     */
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response logout() {
        MultivaluedMap<String, String> form = request.getDecodedFormParameters();
        if (form.containsKey(OAuth2Constants.REFRESH_TOKEN)) {
            return logoutToken();
        } else {
            return logout(
                    form.getFirst(OIDCLoginProtocol.ID_TOKEN_HINT),
                    form.getFirst(OIDCLoginProtocol.CLIENT_ID_PARAM),
                    form.getFirst(OIDCLoginProtocol.POST_LOGOUT_REDIRECT_URI_PARAM),
                    form.getFirst(OIDCLoginProtocol.STATE_PARAM),
                    form.getFirst(OIDCLoginProtocol.UI_LOCALES_PARAM),
                    form.getFirst(AuthenticationManager.INITIATING_IDP_PARAM)
            );
        }
    }

    @Path("/logout-confirm")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response logoutConfirmAction() {
        MultivaluedMap<String, String> formData = request.getDecodedFormParameters();
        event.event(EventType.LOGOUT);
        String code = formData.getFirst(SESSION_CODE);
        String clientId = session.getContext().getUri().getQueryParameters().getFirst(Constants.CLIENT_ID);
        String tabId = session.getContext().getUri().getQueryParameters().getFirst(Constants.TAB_ID);

        logger.tracef("Logout confirmed. sessionCode=%s, clientId=%s, tabId=%s", code, clientId, tabId);

        SessionCodeChecks checks = new LogoutSessionCodeChecks(realm, session.getContext().getUri(), request, clientConnection, session, event, code, clientId, tabId);
        checks.initialVerify();
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.LOGGING_OUT.name(), ClientSessionCode.ActionType.USER) || !checks.isActionRequest()) {
            AuthenticationSessionModel logoutSession = checks.getAuthenticationSession();
            String errorMessage = "Failed verification during logout.";
            logger.debugf( "%s logoutSessionId=%s, clientId=%s, tabId=%s",
                    errorMessage, logoutSession != null ? logoutSession.getParentSession().getId() : "unknown", clientId, tabId);

            SystemClientUtil.checkSkipLink(session, logoutSession);

            event.detail(Details.REASON, errorMessage);
            event.error(Errors.SESSION_EXPIRED);

            return ErrorPage.error(session, logoutSession, Response.Status.BAD_REQUEST, Messages.FAILED_LOGOUT);
        }

        AuthenticationSessionModel logoutSession = checks.getAuthenticationSession();
        logger.tracef("Logout code successfully verified. Logout Session is '%s'. Client ID is '%s'.", logoutSession.getParentSession().getId(),
                logoutSession.getClient().getClientId());
        return doBrowserLogout(logoutSession);
    }


    // 用户在登出确认页切换语言时触发
    @Path("/logout-confirm")
    @NoCache
    @GET
    public Response logoutConfirmGet() {
        event.event(EventType.LOGOUT);

        String clientId = session.getContext().getUri().getQueryParameters().getFirst(Constants.CLIENT_ID);
        String tabId = session.getContext().getUri().getQueryParameters().getFirst(Constants.TAB_ID);

        logger.tracef("Changing localization by user during logout. clientId=%s, tabId=%s, kc_locale: %s", clientId, tabId, session.getContext().getUri().getQueryParameters().getFirst(LocaleSelectorProvider.KC_LOCALE_PARAM));

        SessionCodeChecks checks = new LogoutSessionCodeChecks(realm, session.getContext().getUri(), request, clientConnection, session, event, null, clientId, tabId);
        AuthenticationSessionModel logoutSession = checks.initialVerifyAuthSession();
        if (logoutSession == null) {
            String errorMessage = "Failed verification when changing locale during logout.";
            logger.debugf("%s clientId=%s, tabId=%s", errorMessage, clientId, tabId);

            SystemClientUtil.checkSkipLink(session, logoutSession);

            AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(session, realm, false);
            if (authResult != null) {
                event.detail(Details.REASON, errorMessage);
                event.error(Errors.LOGOUT_FAILED);
                return ErrorPage.error(session, logoutSession, Response.Status.BAD_REQUEST, Messages.FAILED_LOGOUT);
            } else {
                // Probably changing locale on logout screen after logout was already performed. If there is no session in the browser, we can just display that logout was already finished
                return session.getProvider(LoginFormsProvider.class).setSuccess(Messages.SUCCESS_LOGOUT).createInfoPage();
            }
        }

        LocaleUtil.processLocaleParam(session, realm, logoutSession);

        LoginFormsProvider loginForm = session.getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(logoutSession)
                .setUser(logoutSession.getAuthenticatedUser());

        return displayLogoutConfirmationScreen(loginForm, logoutSession);
    }


    // 用户确认登出且校验通过后执行浏览器登出
    private Response doBrowserLogout(AuthenticationSessionModel logoutSession) {
        UserSessionModel userSession = null;
        String userSessionIdFromIdToken = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_VALIDATED_ID_TOKEN_SESSION_STATE);
        String idTokenIssuedAtStr = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_VALIDATED_ID_TOKEN_ISSUED_AT);
        if (userSessionIdFromIdToken != null && idTokenIssuedAtStr != null) {
            try {
                userSession = session.sessions().getUserSession(realm, userSessionIdFromIdToken);

                if (userSession == null) {
                    event.event(EventType.LOGOUT);
                    event.error(Errors.SESSION_EXPIRED);
                    KeycloakContext context = session.getContext();
                    AuthenticationSessionModel authSession = context.getAuthenticationSession();

                    if (authSession != null) {
                        // 无有效会话时清理根认证会话并重置 Cookie
                        new AuthenticationSessionManager(session).removeAuthenticationSession(authSession.getRealm(), authSession, true);
                    }
                } else {
                    Integer idTokenIssuedAt = Integer.parseInt(idTokenIssuedAtStr);
                    checkTokenIssuedAt(idTokenIssuedAt, userSession);
                }
            } catch (OAuthErrorException e) {
                event.event(EventType.LOGOUT);
                event.detail(Details.REASON, e.getDescription());
                event.error(Errors.INVALID_TOKEN);
                return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.SESSION_NOT_ACTIVE);
            }
        }

        // 校验身份 Cookie（登出场景忽略访问令牌超时）
        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(session, realm, false);
        if (authResult != null) {
            userSession = userSession != null ? userSession : authResult.session();
            return initiateBrowserLogout(userSession);
        } else if (userSession != null) {
            // 无身份 Cookie 但 id_token_hint 与会话 Cookie 匹配时继续浏览器登出
            if (AuthenticationManager.compareSessionIdWithSessionCookie(session, userSessionIdFromIdToken)) {
                return initiateBrowserLogout(userSession);
            }
            // 会话未处于登出中/已登出时执行后台登出
            // this might happen when a backChannelLogout is already initiated from AuthenticationManager.authenticateIdentityCookie
            if (userSession.getState() != LOGGING_OUT && userSession.getState() != LOGGED_OUT) {
                // non browser logout
                event.event(EventType.LOGOUT);
                AuthenticationManager.backchannelLogout(session, realm, userSession, session.getContext().getUri(), clientConnection, headers, true);

                String redirectUri = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_REDIRECT_URI);
                if (redirectUri != null) {
                    event.detail(Details.REDIRECT_URI, redirectUri);
                }
                event.user(userSession.getUser()).session(userSession).success();
            }
        }

        logger.tracef("Removing logout session '%s' used during logout.", logoutSession.getParentSession().getId());
        RootAuthenticationSessionModel rootAuthSession = logoutSession.getParentSession();
        rootAuthSession.removeAuthenticationSessionByTabId(logoutSession.getTabId());
        return LogoutUtil.sendResponseAfterLogoutFinished(session, logoutSession);
    }


    /**
     * 非浏览器登出：提交 {@code refresh_token} 并校验客户端身份。
     * <p>机密客户端用 Basic Auth；公开客户端须传 {@code client_id}。成功返回 204。</p>
     * @return 204 或错误 JSON
     */
    private Response logoutToken() {
        cors = Cors.builder().auth().allowedMethods("POST").auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);

        MultivaluedMap<String, String> form = request.getDecodedFormParameters();
        checkSsl();

        event.event(EventType.LOGOUT);

        ClientModel client = authorizeClient();
        String refreshToken = form.getFirst(OAuth2Constants.REFRESH_TOKEN);
        if (refreshToken == null) {
            event.error(Errors.INVALID_TOKEN);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "No refresh token", Response.Status.BAD_REQUEST);
        }

        try {
            session.clientPolicy().triggerOnEvent(new LogoutRequestContext(client, form));
            refreshToken = form.getFirst(OAuth2Constants.REFRESH_TOKEN);
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw new CorsErrorResponseException(cors, cpe.getError(), cpe.getErrorDetail(), cpe.getErrorStatus());
        }

        RefreshToken token = null;
        try {
            // KEYCLOAK-6771 证书绑定令牌校验
            token = tokenManager.verifyRefreshToken(session, realm, client, request, refreshToken, false);

            boolean offline = TokenUtil.TOKEN_TYPE_OFFLINE.equals(token.getType());

            UserSessionModel userSessionModel;
            if (offline) {
                UserSessionManager sessionManager = new UserSessionManager(session);
                userSessionModel = sessionManager.findOfflineUserSession(realm, token.getSessionState());
            } else {
                String sessionState = token.getSessionState();
                userSessionModel = session.sessions().getUserSession(realm, sessionState);
            }

            if (userSessionModel != null) {
                checkTokenIssuedAt(token.getIat(), userSessionModel);
                logout(userSessionModel, offline);
            }
        } catch (OAuthErrorException e) {
            // KEYCLOAK-6771 Certificate Bound Token
            if (MtlsHoKTokenUtil.CERT_VERIFY_ERROR_DESC.equals(e.getDescription())) {
                event.detail(Details.REASON, e.getDescription());
                event.error(Errors.NOT_ALLOWED);
                throw new CorsErrorResponseException(cors, e.getError(), e.getDescription(), Response.Status.UNAUTHORIZED);
            } else {
                event.detail(Details.REASON, e.getDescription());
                event.error(Errors.INVALID_TOKEN);
                throw new CorsErrorResponseException(cors, e.getError(), e.getDescription(), Response.Status.BAD_REQUEST);
            }
        }

        return cors.add(Response.noContent());
    }

    /**
     * OpenID Connect 后台通道登出：接收有效 {@code logout_token}，注销关联用户会话。
     * @see <a href="https://openid.net/specs/openid-connect-backchannel-1_0.html">Back-Channel Logout</a>
     * @return 200/504 等 JSON 响应
     */
    @Path("/backchannel-logout")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response backchannelLogout() {
        MultivaluedMap<String, String> form = request.getDecodedFormParameters();
        event.event(EventType.LOGOUT);

        String encodedLogoutToken = form.getFirst(OAuth2Constants.LOGOUT_TOKEN);
        if (encodedLogoutToken == null) {
            String errorMessage = "No logout token";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_TOKEN);
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, errorMessage,
                    Response.Status.BAD_REQUEST);
        }

        LogoutTokenValidationContext validationCtx = tokenManager.verifyLogoutToken(session, encodedLogoutToken);
        if (!validationCtx.getStatus().equals(LogoutTokenValidationCode.VALIDATION_SUCCESS)) {
            String errorMessage = validationCtx.getStatus().getErrorMessage();
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_TOKEN);
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, errorMessage,
                    Response.Status.BAD_REQUEST);
        }

        LogoutToken logoutToken = validationCtx.getLogoutToken();

        Stream<String> identityProviderAliases = validationCtx.getValidIdentityProviders().stream()
                .map(idp -> idp.getConfig().getAlias());

        boolean logoutOfflineSessions = Boolean.parseBoolean(logoutToken.getEvents()
                .getOrDefault(TokenUtil.TOKEN_BACKCHANNEL_LOGOUT_EVENT_REVOKE_OFFLINE_TOKENS, false).toString());

        BackchannelLogoutResponse backchannelLogoutResponse;

        if (logoutToken.getSid() != null) {
            backchannelLogoutResponse = backchannelLogoutWithSessionId(logoutToken.getSid(), identityProviderAliases,
                    logoutOfflineSessions, logoutToken.getSubject());
        } else {
            backchannelLogoutResponse = backchannelLogoutFederatedUserId(logoutToken.getSubject(),
                    identityProviderAliases, logoutOfflineSessions);
        }

        if (!backchannelLogoutResponse.getLocalLogoutSucceeded()) {
            String errorMessage = "There was an error during the local logout";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.LOGOUT_FAILED);
            throw new ErrorResponseException(OAuthErrorException.SERVER_ERROR, errorMessage,
                    Response.Status.NOT_IMPLEMENTED);
        }

        session.getProvider(SecurityHeadersProvider.class).options().allowEmptyContentType();

        if (oneOrMoreDownstreamLogoutsFailed(backchannelLogoutResponse)) {
            return Cors.builder()
                    .auth()
                    .add(Response.status(Response.Status.GATEWAY_TIMEOUT)
                            .type(MediaType.APPLICATION_JSON_TYPE));
        }

        return Cors.builder()
                .auth()
                .add(Response.ok()
                        .type(MediaType.APPLICATION_JSON_TYPE));
    }

    private BackchannelLogoutResponse backchannelLogoutWithSessionId(String sessionId,
            Stream<String> identityProviderAliases, boolean logoutOfflineSessions, String federatedUserId) {
        AtomicReference<BackchannelLogoutResponse> backchannelLogoutResponse = new AtomicReference<>(new BackchannelLogoutResponse());
        backchannelLogoutResponse.get().setLocalLogoutSucceeded(true);
        identityProviderAliases.forEach(identityProviderAlias -> {
            UserSessionModel userSession = session.sessions().getUserSessionByBrokerSessionId(realm, identityProviderAlias + "." + sessionId);

            if (logoutOfflineSessions) {
                logoutOfflineUserSessionByBrokerUserId(identityProviderAlias + "." + federatedUserId, identityProviderAlias + "." + sessionId);
            }

            if (userSession != null) {
                backchannelLogoutResponse.set(logoutUserSession(userSession));
            }
        });

        return backchannelLogoutResponse.get();
    }

    private BackchannelLogoutResponse backchannelLogoutFederatedUserId(String federatedUserId,
                                                                       Stream<String> identityProviderAliases,
                                                                       boolean logoutOfflineSessions) {
        BackchannelLogoutResponse backchannelLogoutResponse = new BackchannelLogoutResponse();
        backchannelLogoutResponse.setLocalLogoutSucceeded(true);
        identityProviderAliases.forEach(identityProviderAlias -> {

            if (logoutOfflineSessions) {
                logoutOfflineUserSessions(identityProviderAlias + "." + federatedUserId);
            }

            session.sessions().getUserSessionByBrokerUserIdStream(realm, identityProviderAlias + "." + federatedUserId)
                    .collect(Collectors.toList()) // 收集列表避免 backchannelLogout 并发修改
                    .forEach(userSession -> {
                        BackchannelLogoutResponse userBackchannelLogoutResponse = this.logoutUserSession(userSession);
                        backchannelLogoutResponse.setLocalLogoutSucceeded(backchannelLogoutResponse.getLocalLogoutSucceeded()
                                && userBackchannelLogoutResponse.getLocalLogoutSucceeded());
                        userBackchannelLogoutResponse.getClientResponses()
                                .forEach(backchannelLogoutResponse::addClientResponses);
                    });
        });

        return backchannelLogoutResponse;
    }

    private void logoutOfflineUserSessions(String brokerUserId) {
        UserSessionManager userSessionManager = new UserSessionManager(session);
        session.sessions().getOfflineUserSessionByBrokerUserIdStream(realm, brokerUserId).collect(Collectors.toList())
                .forEach(userSessionManager::revokeOfflineUserSession);
    }

    private void logoutOfflineUserSessionByBrokerUserId(String brokerUserId, String brokerSessionId) {
        UserSessionManager userSessionManager = new UserSessionManager(session);
        if (brokerUserId != null && brokerSessionId != null) {
            session.sessions().getOfflineUserSessionByBrokerUserIdStream(realm, brokerUserId)
                    .filter(userSession -> brokerSessionId.equals(userSession.getBrokerSessionId()))
                    .forEach(userSessionManager::revokeOfflineUserSession);
        }
    }

    private BackchannelLogoutResponse logoutUserSession(UserSessionModel userSession) {
        BackchannelLogoutResponse backchannelLogoutResponse = AuthenticationManager.backchannelLogout(session, realm,
                userSession, session.getContext().getUri(), clientConnection, headers, false);

        if (backchannelLogoutResponse.getLocalLogoutSucceeded()) {
            event.user(userSession.getUser())
                    .session(userSession)
                    .success();
        }

        return backchannelLogoutResponse;
    }

    private boolean oneOrMoreDownstreamLogoutsFailed(BackchannelLogoutResponse backchannelLogoutResponse) {
        BackchannelLogoutResponse filteredBackchannelLogoutResponse = new BackchannelLogoutResponse();
        for (BackchannelLogoutResponse.DownStreamBackchannelLogoutResponse response : backchannelLogoutResponse
                .getClientResponses()) {
            if (response.isWithBackchannelLogoutUrl()) {
                filteredBackchannelLogoutResponse.addClientResponses(response);
            }
        }

        return backchannelLogoutResponse.getClientResponses().stream()
                .filter(BackchannelLogoutResponse.DownStreamBackchannelLogoutResponse::isWithBackchannelLogoutUrl)
                .anyMatch(clientResponse -> !(clientResponse.getResponseCode().isPresent() &&
                        (clientResponse.getResponseCode().get() == Response.Status.OK.getStatusCode() ||
                                clientResponse.getResponseCode().get() == Response.Status.NO_CONTENT.getStatusCode())));
    }

    private void logout(UserSessionModel userSession, boolean offline) {
        AuthenticationManager.backchannelLogout(session, realm, userSession, session.getContext().getUri(), clientConnection, headers, true, offline);
        event.user(userSession.getUser()).session(userSession).success();
    }

    private ClientModel authorizeClient() {
        ClientModel client = AuthorizeClientUtil.authorizeClient(session, event, cors).getClient();
        cors.checkAllowedOrigins(session, client);

        if (client.isBearerOnly()) {
            throw new CorsErrorResponseException(cors, Errors.INVALID_CLIENT, "Bearer-only not allowed", Response.Status.BAD_REQUEST);
        }

        return client;
    }

    private void checkSsl() {
        if (!session.getContext().getUri().getBaseUri().getScheme().equals("https") && realm.getSslRequired().isRequired(clientConnection)) {
            throw new CorsErrorResponseException(cors.allowAllOrigins(), "invalid_request", "HTTPS required", Response.Status.FORBIDDEN);
        }
    }

    private void checkTokenIssuedAt(long idTokenIssuedAt, UserSessionModel userSession) throws OAuthErrorException {
        if (idTokenIssuedAt + 1 < userSession.getStarted()) {
            throw new OAuthErrorException(OAuthErrorException.INVALID_GRANT, "Toked issued before the user session started");
        }
    }

    private Response initiateBrowserLogout(UserSessionModel userSession) {
        userSession.setNote(AuthenticationManager.KEYCLOAK_LOGOUT_PROTOCOL, OIDCLoginProtocol.LOGIN_PROTOCOL);
        logger.tracef("Calling initiateBrowserLogout for user session '%s'", userSession.getId());
        Response response =  AuthenticationManager.browserLogout(session, realm, userSession, session.getContext().getUri(), clientConnection, headers);
        logger.tracef("Finished call of initiateBrowserLogout for user session '%s'", userSession.getId());
        return response;
    }
}
