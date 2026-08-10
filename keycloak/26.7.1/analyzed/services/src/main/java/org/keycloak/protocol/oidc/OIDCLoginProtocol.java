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
package org.keycloak.protocol.oidc;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.TokenIdGenerator;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.constants.AdapterConstants;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ClientData;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointChecker;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequestParserProcessor;
import org.keycloak.protocol.oidc.endpoints.request.RequestUriType;
import org.keycloak.protocol.oidc.par.endpoints.request.AuthzEndpointParParser;
import org.keycloak.protocol.oidc.utils.LogoutUtil;
import org.keycloak.protocol.oidc.utils.OAuth2Code;
import org.keycloak.protocol.oidc.utils.OAuth2CodeParser;
import org.keycloak.protocol.oidc.utils.OIDCRedirectUriBuilder;
import org.keycloak.protocol.oidc.utils.OIDCResponseMode;
import org.keycloak.protocol.oidc.utils.OIDCResponseType;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.adapters.action.PushNotBeforeAction;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.Urls;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.ImplicitHybridTokenResponse;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ResourceAdminManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

import static org.keycloak.protocol.oidc.grants.device.DeviceGrantType.approveOAuth2DeviceAuthorization;
import static org.keycloak.protocol.oidc.grants.device.DeviceGrantType.denyOAuth2DeviceAuthorization;
import static org.keycloak.protocol.oidc.grants.device.DeviceGrantType.isOAuth2DeviceVerificationFlow;

/**
 * OpenID Connect 登录协议实现（{@link LoginProtocol}）。
 * <p>处理授权码/隐式/混合流认证成功响应、错误重定向、前后端通道登出、浏览器登出完成及重新认证判定等 OIDC 核心流程。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class OIDCLoginProtocol implements LoginProtocol {

    /** 协议 ID：openid-connect。 */
    public static final String LOGIN_PROTOCOL = Constants.OIDC_PROTOCOL;
    /** OAuth2/OIDC state 参数名。 */
    public static final String STATE_PARAM = "state";
    /** scope 参数名。 */
    public static final String SCOPE_PARAM = "scope";
    public static final String RESOURCE_PARAM = OAuth2Constants.RESOURCE;
    /** 授权码参数名。 */
    public static final String CODE_PARAM = "code";
    /** response_type 参数名。 */
    public static final String RESPONSE_TYPE_PARAM = "response_type";
    public static final String GRANT_TYPE_PARAM = "grant_type";
    public static final String REDIRECT_URI_PARAM = "redirect_uri";
    public static final String POST_LOGOUT_REDIRECT_URI_PARAM = "post_logout_redirect_uri";
    public static final String CLIENT_ID_PARAM = "client_id";
    /** nonce 参数名。 */
    public static final String NONCE_PARAM = "nonce";
    public static final String MAX_AGE_PARAM = OAuth2Constants.MAX_AGE;
    public static final String PROMPT_PARAM = OAuth2Constants.PROMPT;
    public static final String LOGIN_HINT_PARAM = "login_hint";
    public static final String REQUEST_PARAM = "request";
    public static final String REQUEST_URI_PARAM = "request_uri";
    public static final String UI_LOCALES_PARAM = OAuth2Constants.UI_LOCALES_PARAM;
    public static final String CLAIMS_PARAM = "claims";
    public static final String ACR_PARAM = "acr_values";
    public static final String ID_TOKEN_HINT = "id_token_hint";
    /** 会话同意注释键。 */
    public static final String CONSENT_NOTE = "session_consent";

    public static final String LOGOUT_STATE_PARAM = "OIDC_LOGOUT_STATE_PARAM";
    public static final String LOGOUT_REDIRECT_URI = "OIDC_LOGOUT_REDIRECT_URI";
    public static final String LOGOUT_VALIDATED_ID_TOKEN_SESSION_STATE = "OIDC_LOGOUT_VALIDATED_ID_TOKEN_SESSION_STATE";
    public static final String LOGOUT_VALIDATED_ID_TOKEN_ISSUED_AT = "OIDC_LOGOUT_VALIDATED_ID_TOKEN_ISSUED_AT";

    /** issuer（iss）会话注释键。 */
    public static final String ISSUER = "iss";

    public static final String RESPONSE_MODE_PARAM = "response_mode";

    public static final String PROMPT_VALUE_NONE = "none";
    public static final String PROMPT_VALUE_LOGIN = "login";
    public static final String PROMPT_VALUE_CONSENT = "consent";
    public static final String PROMPT_VALUE_CREATE = "create";
    public static final String PROMPT_VALUE_SELECT_ACCOUNT = "select_account";

    // 客户端认证方法常量
    public static final String CLIENT_SECRET_BASIC = "client_secret_basic";
    public static final String CLIENT_SECRET_POST = "client_secret_post";
    public static final String CLIENT_SECRET_JWT = "client_secret_jwt";
    public static final String PRIVATE_KEY_JWT = "private_key_jwt";
    public static final String TLS_CLIENT_AUTH = "tls_client_auth";
    public static final String ATTEST_JWT_CLIENT_AUTH = "attest_jwt_client_auth";

    /**
     * 遗留 IdP 集成：发送未按 RFC6749 编码的 client_secret（已弃用）。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    public static final String CLIENT_SECRET_BASIC_UNENCODED = "client_secret_basic_unencoded";

    // PKCE code_challenge（RFC 7636 §4.3）
    public static final String CODE_CHALLENGE_PARAM = "code_challenge";
    public static final String CODE_CHALLENGE_METHOD_PARAM = "code_challenge_method";

    // PKCE code_challenge 长度限制（RFC 7636 §4.2）
    public static final int PKCE_CODE_CHALLENGE_MIN_LENGTH = 43;
    public static final int PKCE_CODE_CHALLENGE_MAX_LENGTH = 128;

    // PKCE code_verifier 长度限制（RFC 7636 §4.1）
    public static final int PKCE_CODE_VERIFIER_MIN_LENGTH = 43;
    public static final int PKCE_CODE_VERIFIER_MAX_LENGTH = 128;

    // PKCE 方法 plain / S256（RFC 7636 §6.2.2）
    public static final String PKCE_METHOD_PLAIN = "plain";
    public static final String PKCE_METHOD_S256 = "S256";

    // DPoP 公钥指纹参数 dpop_jkt（RFC 9449）
    public static final String DPOP_JKT = "dpop_jkt";

    private static final Logger logger = Logger.getLogger(OIDCLoginProtocol.class);

    /** Keycloak 会话。 */
    protected KeycloakSession session;

    /** 当前领域。 */
    protected RealmModel realm;

    /** JAX-RS URI 信息。 */
    protected UriInfo uriInfo;

    /** HTTP 请求头。 */
    protected HttpHeaders headers;

    /** 事件构建器。 */
    protected EventBuilder event;

    /** 解析后的 response_type。 */
    protected OIDCResponseType responseType;
    /** 解析后的 response_mode。 */
    protected OIDCResponseMode responseMode;

    /** OIDC 提供者级配置。 */
    protected OIDCProviderConfig providerConfig;

    /**
     * 构造完整上下文下的 OIDC 协议实例。
     * @param session 会话 @param realm 领域 @param uriInfo URI
     * @param headers 请求头 @param event 事件
     */
    public OIDCLoginProtocol(KeycloakSession session, RealmModel realm, UriInfo uriInfo, HttpHeaders headers, EventBuilder event) {
        this.session = session;
        this.realm = realm;
        this.uriInfo = uriInfo;
        this.headers = headers;
        this.event = event;
    }

    /** @param providerConfig 仅配置注入的轻量构造 */
    public OIDCLoginProtocol(OIDCProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    private void setupResponseTypeAndMode(String responseType, String responseMode) {
        this.responseType = OIDCResponseType.parse(responseType);
        this.responseMode = OIDCResponseMode.parse(responseMode, this.responseType);
        this.event.detail(Details.RESPONSE_TYPE, responseType);
        this.event.detail(Details.RESPONSE_MODE, this.responseMode.toString().toLowerCase());
    }

    @Override
    /** 设置Session 配置。 */
    public OIDCLoginProtocol setSession(KeycloakSession session) {
        this.session = session;
        return this;
    }

    @Override
    /** 设置Realm 配置。 */
    public OIDCLoginProtocol setRealm(RealmModel realm) {
        this.realm = realm;
        return this;
    }

    @Override
    /** 设置UriInfo 配置。 */
    public OIDCLoginProtocol setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        return this;
    }

    @Override
    /** 设置HttpHeaders 配置。 */
    public OIDCLoginProtocol setHttpHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    @Override
    /** 设置EventBuilder 配置。 */
    public OIDCLoginProtocol setEventBuilder(EventBuilder event) {
        this.event = event;
        return this;
    }

    /** @return OIDC 提供者配置 */
    public OIDCProviderConfig getConfig() {
        return this.providerConfig;
    }

    /**
     * 认证成功后构建授权响应（code/token/id_token 等）并重定向。
     * @param authSession 认证会话
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 重定向响应
     */
    @Override
    public Response authenticated(AuthenticationSessionModel authSession, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        AuthenticatedClientSessionModel clientSession = clientSessionCtx.getClientSession();

        if (isOAuth2DeviceVerificationFlow(authSession)) {
            return approveOAuth2DeviceAuthorization(authSession, clientSession, session);
        }

        String responseTypeParam = authSession.getClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM);
        String responseModeParam = authSession.getClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM);
        setupResponseTypeAndMode(responseTypeParam, responseModeParam);

        String redirect = authSession.getRedirectUri();
        OIDCRedirectUriBuilder redirectUri = OIDCRedirectUriBuilder.fromUri(redirect, responseMode, session, clientSession);
        String state = authSession.getClientNote(OIDCLoginProtocol.STATE_PARAM);
        logger.debugv("redirectAccessCode: state: {0}", state);
        if (state != null)
            redirectUri.addParam(OAuth2Constants.STATE, state);

        OIDCAdvancedConfigWrapper clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(clientSession.getClient());
        if (!clientConfig.isExcludeSessionStateFromAuthResponse()) {
            redirectUri.addParam(OAuth2Constants.SESSION_STATE, userSession.getId());
        }
        if (!clientConfig.isExcludeIssuerFromAuthResponse()) {
            redirectUri.addParam(OAuth2Constants.ISSUER, clientSession.getNote(OIDCLoginProtocol.ISSUER));
        }

        String nonce = authSession.getClientNote(OIDCLoginProtocol.NONCE_PARAM);
        clientSessionCtx.setAttribute(OIDCLoginProtocol.NONCE_PARAM, nonce);

        String kcActionStatus = authSession.getClientNote(Constants.KC_ACTION_STATUS);
        if (kcActionStatus != null) {
            String requiredActionAlias = authSession.getAuthNote(AuthenticationProcessor.LAST_PROCESSED_EXECUTION);
            if (requiredActionAlias != null) {
                redirectUri.addParam(Constants.KC_ACTION, requiredActionAlias);
            }
            redirectUri.addParam(Constants.KC_ACTION_STATUS, kcActionStatus);
        }

        // 标准或混合流：签发授权码
        String code = null;
        if (responseType.hasResponseType(OIDCResponseType.CODE)) {
            OAuth2Code codeData = new OAuth2Code(SecretGenerator.getInstance().generateSecureID(),
                Time.currentTime() + userSession.getRealm().getAccessCodeLifespan(),
                nonce,
                authSession.getClientNote(OAuth2Constants.SCOPE),
                authSession.getClientNote(OAuth2Constants.RESOURCE),
                authSession.getClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM),
                authSession.getClientNote(OIDCLoginProtocol.CODE_CHALLENGE_PARAM),
                authSession.getClientNote(OIDCLoginProtocol.CODE_CHALLENGE_METHOD_PARAM),
                authSession.getClientNote(OIDCLoginProtocol.DPOP_JKT),
                userSession.getId());

            code = OAuth2CodeParser.persistCode(session, clientSession, codeData);
            redirectUri.addParam(OAuth2Constants.CODE, code);
        }

        // 隐式或混合流：签发 access/id token
        if (responseType.isImplicitOrHybridFlow()) {
            org.keycloak.protocol.oidc.TokenManager tokenManager = new org.keycloak.protocol.oidc.TokenManager();
            org.keycloak.protocol.oidc.TokenManager.AccessTokenResponseBuilder responseBuilder = tokenManager.responseBuilder(realm, clientSession.getClient(), event, session, userSession, clientSessionCtx)
                .generateAccessToken();

            if (responseType.hasResponseType(OIDCResponseType.ID_TOKEN)) {

                responseBuilder.generateIDToken(isIdTokenAsDetachedSignature(clientSession.getClient()));

                if (responseType.hasResponseType(OIDCResponseType.TOKEN)) {
                    responseBuilder.generateAccessTokenHash();
                }

                if (responseType.hasResponseType(OIDCResponseType.CODE)) {
                    responseBuilder.generateCodeHash(code);
                }

                // FAPI Part 2：生成 state/at_hash/c_hash 等哈希
                // http://openid.net/specs/openid-financial-api-part-2.html#authorization-server
                if (state != null && !state.isEmpty())
                    responseBuilder.generateStateHash(state);
            }

            try {
                session.clientPolicy().triggerOnEvent(new ImplicitHybridTokenResponse(authSession, clientSessionCtx, responseBuilder));
            } catch (ClientPolicyException cpe) {
                event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
                event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
                event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
                event.error(cpe.getError());
                new AuthenticationSessionManager(session).removeTabIdInAuthenticationSession(realm, authSession);
                redirectUri.addParam(OAuth2Constants.ERROR_DESCRIPTION, cpe.getError());
                if (!clientConfig.isExcludeIssuerFromAuthResponse()) {
                    redirectUri.addParam(OAuth2Constants.ISSUER, clientSession.getNote(OIDCLoginProtocol.ISSUER));
                }
                return buildRedirectUri(redirectUri, authSession, userSession, clientSessionCtx, cpe, null);
            }

            AccessTokenResponse res = responseBuilder.build();

            if (responseType.hasResponseType(OIDCResponseType.ID_TOKEN)) {
                redirectUri.addParam(OAuth2Constants.ID_TOKEN, res.getIdToken());
            }

            if (responseType.hasResponseType(OIDCResponseType.TOKEN)) {
                redirectUri.addParam(OAuth2Constants.ACCESS_TOKEN, res.getToken());
                redirectUri.addParam(OAuth2Constants.TOKEN_TYPE, res.getTokenType());
                redirectUri.addParam(OAuth2Constants.EXPIRES_IN, String.valueOf(res.getExpiresIn()));
            }

            boolean offlineTokenRequested = clientSessionCtx.isOfflineTokenRequested();
            if (!responseType.isImplicitFlow() && offlineTokenRequested) {
                // 混合流+offline_access：提前创建 offline 会话供授权端点令牌使用
                responseBuilder.createOrUpdateOfflineSession();
            }
        }

        return buildRedirectUri(redirectUri, authSession, userSession, clientSessionCtx);
    }

    /**
     * 扩展点：认证成功后向重定向 URI 附加参数（可写入 clientSession）。
     * @see <a href="https://github.com/keycloak/keycloak/issues/31086">Keycloak #31086</a>
     */
    public Response buildRedirectUri(OIDCRedirectUriBuilder redirectUriBuilder,
                                     AuthenticationSessionModel authSession,
                                     UserSessionModel userSession,
                                     ClientSessionContext clientSessionCtx) {
        return redirectUriBuilder.build();
    }

    /**
     * 扩展点：认证失败/error 响应时向重定向 URI 附加参数。
     * @see <a href="https://github.com/keycloak/keycloak/issues/31086">Keycloak #31086</a>
     */
    public Response buildRedirectUri(OIDCRedirectUriBuilder redirectUriBuilder,
                                     AuthenticationSessionModel authSession,
                                     UserSessionModel userSession,
                                     ClientSessionContext clientSessionCtx,
                                     Exception ex,
                                     Error oidcError) {
        return redirectUriBuilder.build();
    }

    // FAPI 1.0 Advanced：ID Token  detached signature
    private boolean isIdTokenAsDetachedSignature(ClientModel client) {
        if (client == null) return false;
        return Boolean.valueOf(Optional.ofNullable(client.getAttribute(OIDCConfigAttributes.ID_TOKEN_AS_DETACHED_SIGNATURE)).orElse(Boolean.FALSE.toString())).booleanValue();
    }

    @Override
    /** sendError 相关操作。 */
    public Response sendError(AuthenticationSessionModel authSession, Error error, String errorMessage) {
        if (isOAuth2DeviceVerificationFlow(authSession)) {
            return denyOAuth2DeviceAuthorization(authSession, error, session);
        }
        String responseTypeParam = authSession.getClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM);
        String responseModeParam = authSession.getClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM);
        setupResponseTypeAndMode(responseTypeParam, responseModeParam);

        String redirect = authSession.getRedirectUri();
        String state = authSession.getClientNote(OIDCLoginProtocol.STATE_PARAM);

        OIDCRedirectUriBuilder redirectUri = buildErrorRedirectUri(redirect, state, error, errorMessage);

        // 从当前浏览器标签移除认证会话
        new AuthenticationSessionManager(session).removeTabIdInAuthenticationSession(realm, authSession);

        return buildRedirectUri(redirectUri, authSession, null, null, null, error);
    }

    private OIDCRedirectUriBuilder buildErrorRedirectUri(String redirect, String state, Error error, String errorMessage) {
        OIDCRedirectUriBuilder redirectUri = OIDCRedirectUriBuilder.fromUri(redirect, responseMode, session, null);

        OAuth2ErrorRepresentation oauthError = translateError(error, errorMessage);
        if (oauthError.getError() != null) {
            redirectUri.addParam(OAuth2Constants.ERROR, oauthError.getError());
        }
        if (oauthError.getErrorDescription() != null) {
            redirectUri.addParam(OAuth2Constants.ERROR_DESCRIPTION, oauthError.getErrorDescription());
        }
        if (state != null) {
            redirectUri.addParam(OAuth2Constants.STATE, state);
        }

        // RFC 9207 iss 参数及客户端兼容开关
        OIDCAdvancedConfigWrapper clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(session.getContext().getClient());
        if (!clientConfig.isExcludeIssuerFromAuthResponse()) {
            redirectUri.addParam(OAuth2Constants.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
        }

        return redirectUri;
    }

    @Override
    /** 获取ClientData 配置值。 */
    public ClientData getClientData(AuthenticationSessionModel authSession) {
        return new ClientData(authSession.getRedirectUri(),
            authSession.getClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM),
            authSession.getClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM),
            authSession.getClientNote(OIDCLoginProtocol.STATE_PARAM));
    }

    @Override
    /** sendError 相关操作。 */
    public Response sendError(ClientModel client, ClientData clientData, Error error) {
        logger.tracef("Calling sendError with clientData when authenticating with client '%s' in realm '%s'. Error: %s", client.getClientId(), realm.getName(), error);

        // 校验 clientData 对当前客户端有效
        AuthorizationEndpointRequest req = AuthorizationEndpointRequest.fromClientData(clientData);
        AuthorizationEndpointChecker checker = new AuthorizationEndpointChecker()
            .event(event)
            .client(client)
            .realm(realm)
            .request(req)
            .session(session);
        try {
            checker.checkResponseType();
            checker.checkRedirectUri();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            checker.throwAsErrorPageException(null, ex);
        }

        setupResponseTypeAndMode(clientData.getResponseType(), clientData.getResponseMode());
        OIDCRedirectUriBuilder redirectUri = buildErrorRedirectUri(clientData.getRedirectUri(), clientData.getState(), error, null);
        return buildRedirectUri(redirectUri, null, null, null, null, error);
    }

    private OAuth2ErrorRepresentation translateError(Error error, String errorMessage) {
        switch (error) {
            case CANCELLED_AIA_SILENT:
                return new OAuth2ErrorRepresentation(null, null);
            case CANCELLED_AIA:
                return new OAuth2ErrorRepresentation(OAuthErrorException.ACCESS_DENIED, "User cancelled application-initiated action.");
            case CANCELLED_BY_USER:
            case CONSENT_DENIED:
            case LOA_INVALID:
                return new OAuth2ErrorRepresentation(OAuthErrorException.ACCESS_DENIED, errorMessage);
            case PASSIVE_INTERACTION_REQUIRED:
                return new OAuth2ErrorRepresentation(OAuthErrorException.INTERACTION_REQUIRED, null);
            case PASSIVE_LOGIN_REQUIRED:
                return new OAuth2ErrorRepresentation(OAuthErrorException.LOGIN_REQUIRED, null);
            case ALREADY_LOGGED_IN:
                return new OAuth2ErrorRepresentation(OAuthErrorException.TEMPORARILY_UNAVAILABLE, Constants.AUTHENTICATION_EXPIRED_MESSAGE);
            default:
                ServicesLogger.LOGGER.untranslatedProtocol(error.name());
                return new OAuth2ErrorRepresentation(OAuthErrorException.SERVER_ERROR, null);
        }
    }

    /** 后端通道登出：调用客户端 backchannel_logout_uri 或本地会话注销。 */
    @Override
    public Response backchannelLogout(UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        ClientModel client = clientSession.getClient();
        if (OIDCAdvancedConfigWrapper.fromClientModel(clientSession.getClient()).getBackchannelLogoutUrl() != null) {
            return new ResourceAdminManager(session).logoutClientSessionWithBackchannelLogoutUrl(client, clientSession);
        } else {
            return new ResourceAdminManager(session).logoutClientSession(realm, client, clientSession);
        }
    }

    /** 注册需 iframe 前端登出的客户端。 */
    @Override
    public Response frontchannelLogout(UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        if (clientSession != null) {
            ClientModel client = clientSession.getClient();
            if (OIDCAdvancedConfigWrapper.fromClientModel(client).isFrontChannelLogoutEnabled()) {
                FrontChannelLogoutHandler logoutInfo = FrontChannelLogoutHandler.currentOrCreate(session, clientSession);
                logoutInfo.addClient(client);
            }
            clientSession.setAction(AuthenticationSessionModel.Action.LOGGED_OUT.name());
        }
        return null;
    }

    /** 浏览器登出收尾：渲染前端登出页或发送最终重定向。 */
    @Override
    public Response finishBrowserLogout(UserSessionModel userSession, AuthenticationSessionModel logoutSession) {
        event.event(EventType.LOGOUT);
        event.client(logoutSession.getClient());

        String redirectUri = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_REDIRECT_URI);
        if (redirectUri != null) {
            event.detail(Details.REDIRECT_URI, redirectUri);
        }
        event.user(userSession.getUser()).session(userSession).success();
        FrontChannelLogoutHandler frontChannelLogoutHandler = FrontChannelLogoutHandler.current(session);
        if (frontChannelLogoutHandler != null) {
            String finalRedirectUri = redirectUri == null ? null : LogoutUtil.getRedirectUriWithAttachedState(redirectUri, logoutSession).toString();
            return frontChannelLogoutHandler.renderLogoutPage(finalRedirectUri);
        }

        return LogoutUtil.sendResponseAfterLogoutFinished(session, logoutSession);
    }

    /** 是否因 prompt=login、max_age 或 KC 动作需强制重新认证。 */
    @Override
    public boolean requireReauthentication(UserSessionModel userSession, AuthenticationSessionModel authSession) {
        return isPromptLogin(authSession) || isAuthTimeExpired(userSession, authSession) || isReAuthRequiredForKcAction(userSession, authSession);
    }

    /** @param authSession 认证会话 @return 是否 prompt=login */
    protected boolean isPromptLogin(AuthenticationSessionModel authSession) {
        String prompt = authSession.getClientNote(OIDCLoginProtocol.PROMPT_PARAM);
        return TokenUtil.hasPrompt(prompt, OIDCLoginProtocol.PROMPT_VALUE_LOGIN);
    }

    /** 根据 max_age 判断 auth_time 是否过期。 */
    protected boolean isAuthTimeExpired(UserSessionModel userSession, AuthenticationSessionModel authSession) {
        if (userSession == null) {
            return false;
        }
        String authTime = userSession.getNote(AuthenticationManager.AUTH_TIME);
        String maxAge = authSession.getClientNote(OIDCLoginProtocol.MAX_AGE_PARAM);
        if (maxAge == null) {
            return false;
        }

        int authTimeInt = authTime == null ? 0 : Integer.parseInt(authTime);
        int maxAgeInt = Integer.parseInt(maxAge);

        if (authTimeInt + maxAgeInt < Time.currentTime()) {
            logger.debugf("Authentication time is expired, needs to reauthenticate. userSession=%s, clientId=%s, maxAge=%d, authTime=%d", userSession.getId(),
                authSession.getClient().getId(), maxAgeInt, authTimeInt);
            return true;
        }

        return false;
    }

    /** KC 必需动作是否超过 Provider 定义的 max auth age。 */
    protected boolean isReAuthRequiredForKcAction(UserSessionModel userSession, AuthenticationSessionModel authSession) {
        if (userSession != null && authSession.getClientNote(Constants.KC_ACTION) != null) {
            String providerId = authSession.getClientNote(Constants.KC_ACTION);
            RequiredActionProvider requiredActionProvider = this.session.getProvider(RequiredActionProvider.class, providerId);
            if (requiredActionProvider == null) {
                return false;
            }
            String authTime = userSession.getNote(AuthenticationManager.AUTH_TIME);
            int authTimeInt = authTime == null ? 0 : Integer.parseInt(authTime);
            int maxAgeInt = requiredActionProvider.getMaxAuthAge(session);
            return authTimeInt + maxAgeInt < Time.currentTime();
        } else {
            return false;
        }
    }

    /** 向适配器 management URL 推送 not-before 撤销策略。 */
    @Override
    public boolean sendPushRevocationPolicyRequest(RealmModel realm, ClientModel resource, int notBefore, String managementUrl) {
        PushNotBeforeAction adminAction = new PushNotBeforeAction(TokenIdGenerator.generateId(), Time.currentTime() + 30, resource.getClientId(), notBefore);
        String token = session.tokens().encode(adminAction);
        logger.debugv("pushRevocation resource: {0} url: {1}", resource.getClientId(), managementUrl);
        URI target = UriBuilder.fromUri(managementUrl).path(AdapterConstants.K_PUSH_NOT_BEFORE).build();
        try {
            int status = session.getProvider(HttpClientProvider.class).postText(target.toString(), token);
            boolean success = status == 204 || status == 200;
            logger.debugf("pushRevocation success for %s: %s", managementUrl, success);
            return success;
        } catch (IOException e) {
            ServicesLogger.LOGGER.failedToSendRevocation(e);
            return false;
        }
    }

    /** 认证完成回调：PAR request_uri 一次性消费等。 */
    @Override
    public void authenticationComplete(AuthenticationSessionModel authSession) {
        // PAR request_uri 在授权完成点一次性失效
        // not at the point of visiting the authorization endpoint
        String requestUri = authSession.getAuthNote(Constants.AUTHORIZATION_REQUEST_URI);
        RequestUriType requestUriType = Optional.ofNullable(requestUri)
                .map(AuthorizationEndpointRequestParserProcessor::getRequestUriType)
                .orElse(null);
        if (requestUriType == RequestUriType.PAR && AuthzEndpointParParser.removeRequestObject(session, requestUri) == null) {
            throw new AuthenticationFlowException("PAR not found, not issued or used multiple times.", AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    @Override
    /** close 相关操作。 */
    public void close() {
    }
}
