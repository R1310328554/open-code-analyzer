/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.grants;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.ClientConnection;
import org.keycloak.constants.AdapterConstants;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.encode.AccessTokenContext;
import org.keycloak.protocol.oidc.encode.TokenContextEncoderProvider;
import org.keycloak.protocol.oidc.rar.AuthorizationDetailsProcessorManager;
import org.keycloak.protocol.oidc.rar.InvalidAuthorizationDetailsException;
import org.keycloak.protocol.oidc.utils.AuthorizeClientUtil;
import org.keycloak.protocol.oidc.utils.ClientHostUtils;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.util.MtlsHoKTokenUtil;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

import static org.keycloak.OAuth2Constants.AUTHORIZATION_DETAILS;

/**
 * OAuth 2.0 授权类型抽象基类：封装令牌响应构建、客户端校验、authorization_details 处理等公共逻辑。
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a> (et al.)
 */
public abstract class OAuth2GrantTypeBase implements OAuth2GrantType {

    private static final Logger logger = Logger.getLogger(OAuth2GrantTypeBase.class);

    /** 当前授权类型执行上下文 */
    protected OAuth2GrantType.Context context;

    protected KeycloakSession session;
    protected RealmModel realm;
    protected ClientModel client;
    protected OIDCAdvancedConfigWrapper clientConfig;
    protected ClientConnection clientConnection;
    protected Map<String, String> clientAuthAttributes;
    protected MultivaluedMap<String, String> formParams;
    protected EventBuilder event;
    protected Cors cors;
    protected TokenManager tokenManager;
    protected HttpRequest request;
    protected HttpResponse response;
    protected HttpHeaders headers;

    /** 从上下文注入会话、客户端、表单参数等字段 @param context 授权类型上下文 */
    protected void setContext(Context context) {
        this.context = context;
        this.session = context.session;
        this.realm = context.realm;
        this.client = context.client;
        this.clientConfig = (OIDCAdvancedConfigWrapper) context.clientConfig;
        this.clientConnection = context.clientConnection;
        this.clientAuthAttributes = context.clientAuthAttributes;
        this.request = context.request;
        this.response = context.response;
        this.headers = context.headers;
        this.formParams = context.formParams;
        this.event = context.event;
        this.cors = context.cors;
        this.tokenManager = (TokenManager) context.tokenManager;
    }

    /**
     * 构建访问令牌响应（含可选刷新令牌、ID Token、MTLS HoK 绑定等）。
     * @param user 用户
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @param scopeParam scope 参数
     * @param clientPolicyContextGenerator 客户端策略上下文生成器
     * @return 令牌响应构建器
     */
    protected TokenManager.AccessTokenResponseBuilder createTokenResponseBuilder(UserModel user, UserSessionModel userSession, ClientSessionContext clientSessionCtx,  String scopeParam, Function<TokenManager.AccessTokenResponseBuilder, ClientPolicyContext> clientPolicyContextGenerator) {
        clientSessionCtx.setAttribute(Constants.GRANT_TYPE, context.getGrantType());
        clientSessionCtx.setAttribute(OAuth2Constants.RESOURCE, formParams.getFirst(OAuth2Constants.RESOURCE));
        AccessToken token = tokenManager.createClientAccessToken(session, realm, client, user, userSession, clientSessionCtx, clientSessionCtx.isOfflineTokenRequested());

        TokenManager.AccessTokenResponseBuilder responseBuilder = tokenManager
                .responseBuilder(realm, client, event, session, userSession, clientSessionCtx).accessToken(token);
        boolean useRefreshToken = useRefreshToken();
        if (useRefreshToken) {
            responseBuilder.generateRefreshToken();
            if (TokenUtil.TOKEN_TYPE_OFFLINE.equals(responseBuilder.getRefreshToken().getType())
                    && clientSessionCtx.getClientSession().getNote(AuthenticationProcessor.FIRST_OFFLINE_ACCESS) != null) {
                // the online session can be removed if first created for offline access
                session.sessions().removeUserSession(realm, userSession);
                // also remove the root authentication session to prevent AUTH_SESSION_ID cookie reuse by a different user
                // consistent with backchannel logout and logout endpoint which both clean up root auth sessions
                logger.tracef("Removing root authentication session '%s' after first offline access", userSession.getId());
                RootAuthenticationSessionModel rootAuthSession = session.authenticationSessions().getRootAuthenticationSession(realm, userSession.getId());
                if (rootAuthSession != null) {
                    session.authenticationSessions().removeRootAuthenticationSession(realm, rootAuthSession);
                }
            }
        } else {
            TokenContextEncoderProvider encoder = session.getProvider(TokenContextEncoderProvider.class);
            if (encoder.getTokenContextFromTokenId(responseBuilder.getAccessToken().getId()).getSessionType() == AccessTokenContext.SessionType.TRANSIENT) {
                // transient sessions do not add the session ID to the token
                responseBuilder.getAccessToken().setSessionId(null);
                event.session((String) null);
            }
        }

        checkAndBindMtlsHoKToken(responseBuilder, useRefreshToken);

        if (TokenUtil.isOIDCRequest(scopeParam)) {
            responseBuilder.generateIDToken().generateAccessTokenHash();
        }

        if (clientPolicyContextGenerator != null) {
            try {
                session.clientPolicy().triggerOnEvent(clientPolicyContextGenerator.apply(responseBuilder));
            } catch (ClientPolicyException cpe) {
                event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
                event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
                event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
                event.error(cpe.getError());
                throw new CorsErrorResponseException(cors, cpe.getError(), cpe.getErrorDetail(), cpe.getErrorStatus());
            }
        }

        return responseBuilder;
    }

    /** 从已构建的 builder 生成 HTTP 令牌响应 @param code 是否为授权码模式（影响加密 KEK 错误处理） */
    protected Response createTokenResponse(TokenManager.AccessTokenResponseBuilder responseBuilder, ClientSessionContext clientSessionCtx, boolean code) {
        AccessTokenResponse res;
        if (code) {
            try {
                res = responseBuilder.build();
            } catch (RuntimeException re) {
                if ("can not get encryption KEK".equals(re.getMessage())) {
                    throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST,
                            "can not get encryption KEK", Response.Status.BAD_REQUEST);
                } else {
                    throw re;
                }
            }
        } else {
            res = responseBuilder.build();
        }

        // Extension point for subclasses to add custom claims
        addCustomTokenResponseClaims(res, clientSessionCtx);

        // Sanitize authorization details before they are sent as part of the Token Response
        var authDetailsProcessor = new AuthorizationDetailsProcessorManager(session);
        authDetailsProcessor.sanitizeBeforeSendingTokenResponse(res);

        event.success();

        return cors.add(Response.ok(res).type(MediaType.APPLICATION_JSON_TYPE));
    }

    /** 一站式创建令牌 HTTP 响应 */
    protected Response createTokenResponse(UserModel user, UserSessionModel userSession, ClientSessionContext clientSessionCtx,
                                           String scopeParam, boolean code, Function<TokenManager.AccessTokenResponseBuilder, ClientPolicyContext> clientPolicyContextGenerator) {
        TokenManager.AccessTokenResponseBuilder responseBuilder = createTokenResponseBuilder(user, userSession,
                clientSessionCtx, scopeParam, clientPolicyContextGenerator);
        return createTokenResponse(responseBuilder, clientSessionCtx, code);
    }

    /** 若客户端启用 MTLS HoK，将客户端证书绑定写入 access/refresh token */
    protected void checkAndBindMtlsHoKToken(TokenManager.AccessTokenResponseBuilder responseBuilder, boolean useRefreshToken) {
        // KEYCLOAK-6771 Certificate Bound Token
        // https://tools.ietf.org/html/draft-ietf-oauth-mtls-08#section-3
        if (clientConfig.isUseMtlsHokToken()) {
            AccessToken.Confirmation confirmation = MtlsHoKTokenUtil.bindTokenWithClientCertificate(request, session);
            if (confirmation != null) {
                responseBuilder.getAccessToken().setConfirmation(confirmation);
                if (useRefreshToken) {
                    responseBuilder.getRefreshToken().setConfirmation(confirmation);
                }
            } else {
                String errorMessage = "Client Certification missing for MTLS HoK Token Binding";
                event.detail(Details.REASON, errorMessage);
                event.error(Errors.INVALID_REQUEST);
                throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST,
                        errorMessage, Response.Status.BAD_REQUEST);
            }
        }
    }

    /** 将适配器 session_state/host 写入客户端会话 note（host 需通过白名单校验） */
    public void updateClientSession(AuthenticatedClientSessionModel clientSession) {

        if(clientSession == null) {
            ServicesLogger.LOGGER.clientSessionNull();
            return;
        }

        String adapterSessionId = formParams.getFirst(AdapterConstants.CLIENT_SESSION_STATE);
        if (adapterSessionId != null) {
            String adapterSessionHost = formParams.getFirst(AdapterConstants.CLIENT_SESSION_HOST);
            logger.debugf("Adapter Session '%s' saved in ClientSession '%s' for client '%s'. Host is '%s'",
                    adapterSessionId, clientSession.getId(), client.getClientId(), adapterSessionHost);

            String oldClientSessionState = clientSession.getNote(AdapterConstants.CLIENT_SESSION_STATE);
            if (!adapterSessionId.equals(oldClientSessionState)) {
                clientSession.setNote(AdapterConstants.CLIENT_SESSION_STATE, adapterSessionId);
            }

            if ((adapterSessionHost != null) && (!adapterSessionHost.trim().isEmpty())) {
                // CVE-2026-4874 - client_session_host requires validation as an external field that is stored in client
                // session and can be used to generate logout callback URL.
                if (!ClientHostUtils.isHostAllowedForClient(adapterSessionHost, client, session)) {
                    logger.warnf("Adapter Session '%s' not valid in ClientSession for client '%s'. Host is '%s' and has been removed.",
                            adapterSessionId, client.getClientId(), adapterSessionHost);
                    return;
                }

                String oldClientSessionHost = clientSession.getNote(AdapterConstants.CLIENT_SESSION_HOST);
                if (!Objects.equals(adapterSessionHost, oldClientSessionHost)) {
                    clientSession.setNote(AdapterConstants.CLIENT_SESSION_HOST, adapterSessionHost);
                    logger.debugf("Adapter Session '%s' saved in ClientSession for client '%s'. Host is '%s'",
                            adapterSessionId, client.getClientId(), adapterSessionHost);
                }
            }
        }
    }

    /** 将客户端认证属性写入用户会话 note */
    public void updateUserSessionFromClientAuth(UserSessionModel userSession) {
        for (Map.Entry<String, String> attr : clientAuthAttributes.entrySet()) {
            userSession.setNote(attr.getKey(), attr.getValue());
        }
    }

    /** 解析并校验请求 scope 参数 @return 合法 scope 字符串 */
    protected String getRequestedScopes() {
        String scope = formParams.getFirst(OAuth2Constants.SCOPE);

        if (!TokenManager.isValidScope(session, scope, client)) {
            String errorMessage = "Invalid scopes: " + scope;
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_REQUEST);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_SCOPE, errorMessage, Response.Status.BAD_REQUEST);
        }

        return scope;
    }

    /** 执行客户端认证并校验 CORS 与 bearer-only 限制 */
    protected void checkClient() {
        AuthorizeClientUtil.ClientAuthResult clientAuth = AuthorizeClientUtil.authorizeClient(session, event, cors);
        client = clientAuth.getClient();
        clientAuthAttributes = clientAuth.getClientAuthAttributes();
        clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(client);

        cors.checkAllowedOrigins(session, client);

        if (client.isBearerOnly()) {
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, "Bearer-only not allowed", Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 子类扩展点：在返回前向 {@link AccessTokenResponse} 添加自定义 claim。
     * 默认空实现。
     */
    protected void addCustomTokenResponseClaims(AccessTokenResponse res, ClientSessionContext clientSessionCtx) {
        // Default: do nothing
    }

    /**
     * authorization_details 处理完成、令牌响应创建前的钩子。
     * 供处理器执行后续动作（如创建状态对象）。
     *
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @param authorizationDetailsResponse 已处理的 authorization_details 响应
     */
    protected void afterAuthorizationDetailsProcessed(UserSessionModel userSession, ClientSessionContext clientSessionCtx,
                                                      List<AuthorizationDetailsJSONRepresentation> authorizationDetailsResponse) {
        if (authorizationDetailsResponse != null && !authorizationDetailsResponse.isEmpty()) {
            new AuthorizationDetailsProcessorManager(session)
                    .afterAuthorizationDetailsProcessed(userSession, clientSessionCtx, authorizationDetailsResponse);
        }
    }

    /**
     * 通过 Provider 发现机制处理 authorization_details 参数；子类可覆盖。
     *
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 处理成功时返回 authorization_details 响应，否则 null
     */
    protected List<AuthorizationDetailsJSONRepresentation> processAuthorizationDetails(UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        String authorizationDetailsParam = formParams.getFirst(AUTHORIZATION_DETAILS);
        if (authorizationDetailsParam != null) {
            try {
                return new AuthorizationDetailsProcessorManager(session)
                        .processAuthorizationDetails(userSession, clientSessionCtx, authorizationDetailsParam);
            } catch (InvalidAuthorizationDetailsException e) {
                logger.warnf(e, "Error when processing authorization_details");
                event.detail(Details.REASON, e.getMessage());
                event.error(Errors.INVALID_AUTHORIZATION_DETAILS);
                throw new CorsErrorResponseException(cors, Errors.INVALID_AUTHORIZATION_DETAILS, "Error when processing authorization_details: " + e.getMessage(), Response.Status.BAD_REQUEST);
            }
        }
        return null;
    }

    /**
     * 请求中缺少 authorization_details 时，由处理器生成响应（如预授权或凭证 offer 流程）。
     *
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 生成成功时返回响应，否则 null
     */
    protected List<AuthorizationDetailsJSONRepresentation> handleMissingAuthorizationDetails(UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        try {
            return new AuthorizationDetailsProcessorManager(session)
                    .handleMissingAuthorizationDetails(userSession, clientSessionCtx);
        } catch (RuntimeException e) {
            logger.warnf(e, "Error when handling missing authorization_details");
            event.detail(Details.REASON, e.getMessage());
            event.error(Errors.INVALID_AUTHORIZATION_DETAILS);
            throw new CorsErrorResponseException(cors, Errors.INVALID_AUTHORIZATION_DETAILS, e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 处理授权阶段存储的 authorization_details（如 PAR 流程）。
     * 适用于授权请求含该参数但令牌请求缺失的场景。
     *
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 处理成功时返回响应，否则 null
     */
    protected List<AuthorizationDetailsJSONRepresentation> processStoredAuthorizationDetails(UserSessionModel userSession, ClientSessionContext clientSessionCtx) throws CorsErrorResponseException {
        // Check if authorization_details was stored during authorization request (e.g., from PAR)
        String storedAuthDetails = clientSessionCtx.getClientSession().getNote(AUTHORIZATION_DETAILS);
        if (storedAuthDetails != null) {
            logger.debugf("Found authorization_details in client session, processing it");
            try {
                return new AuthorizationDetailsProcessorManager(session)
                        .processStoredAuthorizationDetails(userSession, clientSessionCtx, storedAuthDetails);
            } catch (InvalidAuthorizationDetailsException e) {
                logger.warnf(e, "Error when processing stored authorization_details");
                event.detail(Details.REASON, e.getMessage());
                event.error(Errors.INVALID_AUTHORIZATION_DETAILS);
                throw new CorsErrorResponseException(cors, Errors.INVALID_AUTHORIZATION_DETAILS, e.getMessage(), Response.Status.BAD_REQUEST);
            }
        }
        return null;
    }

    /**
     * 本授权类型是否生成刷新令牌。
     * @return 生成刷新令牌时为 true
     */
    protected boolean useRefreshToken() {
        return clientConfig.isUseRefreshToken();
    }

    /** Provider 关闭钩子（默认空实现） */
    @Override
    public void close() {
    }

}
