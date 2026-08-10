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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.IdpConfirmOverrideLinkAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.PostBrokerLoginConstants;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ExchangeTokenToIdentityProviderToken;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.broker.provider.IdentityProviderMapperSyncModeDelegate;
import org.keycloak.broker.provider.IdpLinkAction;
import org.keycloak.broker.provider.UserAuthenticationIdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.common.util.Time;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.light.LightweightUserAdapter;
import org.keycloak.models.utils.AuthenticationFlowResolver;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.utils.AuthorizeClientUtil;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.protocol.saml.SamlSessionUtils;
import org.keycloak.protocol.saml.preprocessor.SamlAuthenticationPreprocessor;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.ErrorPageException;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.Urls;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.IdentityBrokeringAPIContext;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.BruteForceProtector;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.managers.GrantTypeEndpointRestrictionValidator;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.util.AuthenticationFlowURLHelper;
import org.keycloak.services.util.BrowserHistoryHelper;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.services.util.DPoPUtil;
import org.keycloak.services.util.DefaultClientSessionContext;
import org.keycloak.services.util.UserSessionUtil;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.Booleans;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.broker.provider.AbstractIdentityProvider.BROKER_REGISTERED_NEW_USER;

/**
 * 身份代理（Identity Brokering）REST 服务。
 * <p>处理外部 IdP 登录、账户关联、首次/后续 Broker 登录流程、令牌检索及 {@link UserAuthenticationIdentityProvider.AuthenticationCallback} 回调。</p>
 */
public class IdentityBrokerService implements UserAuthenticationIdentityProvider.AuthenticationCallback {

    // 认证会话 note：标识当前正在关联的身份提供方
    /** 认证会话 note 键：账户关联中的 IdP 标识 */
    public static final String LINKING_IDENTITY_PROVIDER = "LINKING_IDENTITY_PROVIDER";

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(IdentityBrokerService.class);

    /** 当前领域 */
    private final RealmModel realmModel;

    /** Keycloak 会话 */
    private final KeycloakSession session;

    /** 客户端连接 */
    private final ClientConnection clientConnection;

    /** HTTP 请求 */
    private final HttpRequest request;

    /** HTTP 头 */
    private final HttpHeaders headers;

    /** 身份代理登录事件构建器 */
    private EventBuilder event;


    /** 从会话上下文构造身份代理服务 */
    public IdentityBrokerService(KeycloakSession session) {
        this.session = session;
        this.clientConnection= session.getContext().getConnection();
        realmModel = session.getContext().getRealm();
        if (realmModel == null) {
            throw new IllegalArgumentException("Realm can not be null.");
        }
        this.request = session.getContext().getHttpRequest();
        this.headers = session.getContext().getRequestHeaders();
    }

    /** 初始化 IDENTITY_PROVIDER_LOGIN 类型事件 */
    public void init() {
        this.event = new EventBuilder(realmModel, session, clientConnection).event(EventType.IDENTITY_PROVIDER_LOGIN);
    }

    /** 校验领域已启用 */
    private void checkRealm() {
        if (!realmModel.isEnabled()) {
            event.error(Errors.REALM_DISABLED);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.REALM_NOT_ENABLED);
        }
    }

    /** 校验 client_id 参数并返回客户端模型 */
    private ClientModel checkClient(String clientId) {
        if (clientId == null) {
            event.error(Errors.INVALID_REQUEST);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.MISSING_PARAMETER, OIDCLoginProtocol.CLIENT_ID_PARAM);
        }

        event.client(clientId);

        ClientModel client = realmModel.getClientByClientId(clientId);
        if (client == null) {
            event.error(Errors.CLIENT_NOT_FOUND);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
        }

        if (!client.isEnabled()) {
            event.error(Errors.CLIENT_DISABLED);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
        }
        return client;

    }

    /**
     * 拒绝账户关联端点的 CORS 预检（返回 403）
     *
     * @param providerAlias
     * @return
     */
    @OPTIONS
    @Path("/{provider_alias}/link")
    /** {@inheritDoc} */
    public Response clientIntiatedAccountLinkingPreflight(@PathParam("provider_alias") String providerAlias) {
        return Response.status(403).build(); // don't allow preflight
    }


    @GET
    @NoCache
    @Path("/{provider_alias}/link")
    @Deprecated
    /**
     * 已弃用：客户端发起的账户关联（请改用 AIA idp_link）。
     * @param providerAlias IdP 别名
     * @param redirectUri 关联完成后的重定向 URI
     * @param clientId 发起客户端 ID
     * @param nonce 防重放随机数
     * @param hash SHA-256(nonce+sessionId+clientId+provider) 校验值
     */
    public Response clientInitiatedAccountLinking(@PathParam("provider_alias") String providerAlias,
                                                  @QueryParam("redirect_uri") String redirectUri,
                                                  @QueryParam("client_id") String clientId,
                                                  @QueryParam("nonce") String nonce,
                                                  @QueryParam("hash") String hash
    ) {
        logger.warnf("Calling deprecated endpoint for client-initiated account linking. This endpoint will be removed in the future. Please use application initiated action (AIA) idp_link instead");
        this.event.event(EventType.CLIENT_INITIATED_ACCOUNT_LINKING);
        checkRealm();
        ClientModel client = checkClient(clientId);
        redirectUri = RedirectUtils.verifyRedirectUri(session, redirectUri, client);
        if (redirectUri == null) {
            event.error(Errors.INVALID_REDIRECT_URI);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
        }

        event.detail(Details.REDIRECT_URI, redirectUri);

        if (nonce == null || hash == null) {
            event.error(Errors.INVALID_REDIRECT_URI);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);

        }

        AuthenticationManager.AuthResult cookieResult = AuthenticationManager.authenticateIdentityCookie(session, realmModel, true);
        String errorParam = "link_error";
        if (cookieResult == null) {
            event.error(Errors.NOT_LOGGED_IN);
            UriBuilder builder = UriBuilder.fromUri(redirectUri)
                    .queryParam(errorParam, Errors.NOT_LOGGED_IN)
                    .queryParam("nonce", nonce);

            return Response.status(302).location(builder.build()).build();
        }

        event.session(cookieResult.session());
        event.user(cookieResult.user());
        event.detail(Details.USERNAME, cookieResult.user().getUsername());

        AuthenticatedClientSessionModel clientSession = null;
        for (AuthenticatedClientSessionModel cs : cookieResult.session().getAuthenticatedClientSessions().values()) {
            if (cs.getClient().getClientId().equals(clientId)) {
                byte[] decoded = Base64Url.decode(hash);
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    throw new ErrorPageException(session, Response.Status.INTERNAL_SERVER_ERROR, Messages.UNEXPECTED_ERROR_HANDLING_REQUEST);
                }
                String input = nonce + cookieResult.session().getId() + clientId + providerAlias;
                byte[] check = md.digest(input.getBytes(StandardCharsets.UTF_8));
                if (MessageDigest.isEqual(decoded, check)) {
                    clientSession = cs;
                    break;
                }
            }
        }
        if (clientSession == null) {
            event.error(Errors.INVALID_TOKEN);
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
        }

        event.detail(Details.IDENTITY_PROVIDER, providerAlias);

        ClientModel accountService = this.realmModel.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
        if (!accountService.getId().equals(client.getId())) {
            RoleModel manageAccountRole = accountService.getRole(AccountRoles.MANAGE_ACCOUNT);

            // 校验用户具备 manage-account 或 manage-account-links 角色
            ClientSessionContext ctx = DefaultClientSessionContext.fromClientSessionScopeParameter(clientSession, session);
            Set<RoleModel> userAccountRoles = ctx.getRolesStream().collect(Collectors.toSet());

            if (!userAccountRoles.contains(manageAccountRole)) {
                RoleModel linkRole = accountService.getRole(AccountRoles.MANAGE_ACCOUNT_LINKS);
                if (!userAccountRoles.contains(linkRole)) {
                    event.error(Errors.NOT_ALLOWED);
                    UriBuilder builder = UriBuilder.fromUri(redirectUri)
                            .queryParam(errorParam, Errors.NOT_ALLOWED)
                            .queryParam("nonce", nonce);
                    return Response.status(302).location(builder.build()).build();
                }
            }
        }


        IdentityProviderModel identityProviderModel = getIdentityProviderModel(session, providerAlias);
        if (identityProviderModel == null) {
            event.error(Errors.UNKNOWN_IDENTITY_PROVIDER);
            UriBuilder builder = UriBuilder.fromUri(redirectUri)
                    .queryParam(errorParam, Errors.UNKNOWN_IDENTITY_PROVIDER)
                    .queryParam("nonce", nonce);
            return Response.status(302).location(builder.build()).build();

        }


        // 创建与用户会话同 ID 的认证会话并刷新 Cookie
        UserSessionModel userSession = cookieResult.session();

        // 罕见情况下用户会话 ID 对应的 root 认证会话已存在 in some rare cases (EG. if some client tried to login in another browser tab with "prompt=login")
        RootAuthenticationSessionModel rootAuthSession = session.authenticationSessions().getRootAuthenticationSession(realmModel, userSession.getId());
        if (rootAuthSession == null) {
            rootAuthSession = session.authenticationSessions().createRootAuthenticationSession(realmModel, userSession.getId());
        }

        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);
        authSession.setAuthenticatedUser(userSession.getUser());

        // 刷新认证会话 Cookie
        new AuthenticationSessionManager(session).setAuthSessionCookie(userSession.getId());

        ClientSessionCode<AuthenticationSessionModel> clientSessionCode = new ClientSessionCode<>(session, realmModel, authSession);
        clientSessionCode.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        clientSessionCode.getOrGenerateCode();
        authSession.setProtocol(client.getProtocol());
        authSession.setRedirectUri(redirectUri);
        authSession.setClientNote(OIDCLoginProtocol.STATE_PARAM, UUID.randomUUID().toString());
        authSession.setAuthNote(LINKING_IDENTITY_PROVIDER, cookieResult.session().getId() + clientId + providerAlias);

        event.detail(Details.CODE_ID, userSession.getId());
        event.success();

        return performClientInitiatedAccountLogin(providerAlias, clientSessionCode);
    }

    /** 按别名获取已启用的 IdP 配置 */
    private static IdentityProviderModel getIdentityProviderModel(KeycloakSession session, String providerAlias) {
        IdentityProviderModel model = session.identityProviders().getByAlias(providerAlias);

        if (model == null || !model.isEnabled()) {
            throw new IdentityBrokerException("Identity Provider [" + providerAlias + "] not found.");
        }

        return model;
    }

    /** 向 IdP 发起客户端发起的账户关联登录请求 */
    public Response performClientInitiatedAccountLogin(String providerAlias, ClientSessionCode<AuthenticationSessionModel> clientSessionCode) {
        try {
            UserAuthenticationIdentityProvider<?> identityProvider = getIdentityProvider(session, providerAlias);
            Response response = identityProvider.performLogin(createAuthenticationRequest(identityProvider, providerAlias, clientSessionCode));

            if (response != null) {
                if (isDebugEnabled()) {
                    logger.debugf("Identity provider [%s] is going to send a request [%s].", identityProvider, response);
                }

                return response;
            }
        } catch (IdentityBrokerException e) {
            return redirectToErrorPage(clientSessionCode.getClientSession(), Response.Status.INTERNAL_SERVER_ERROR, Messages.COULD_NOT_SEND_AUTHENTICATION_REQUEST, e, providerAlias);
        } catch (Exception e) {
            return redirectToErrorPage(clientSessionCode.getClientSession(), Response.Status.INTERNAL_SERVER_ERROR, Messages.UNEXPECTED_ERROR_HANDLING_REQUEST, e, providerAlias);
        }

        return redirectToErrorPage(clientSessionCode.getClientSession(), Response.Status.INTERNAL_SERVER_ERROR, Messages.COULD_NOT_PROCEED_WITH_AUTHENTICATION_REQUEST);
    }


    @POST
    @Path("/{provider_alias}/login")
    /** POST 方式发起 IdP 登录（委托给 {@link #performLogin}） */
    public Response performPostLogin(@PathParam("provider_alias") String providerAlias,
                                     @QueryParam(LoginActionsService.SESSION_CODE) String code,
                                     @QueryParam(Constants.CLIENT_ID) String clientId,
                                     @QueryParam(Constants.CLIENT_DATA) String clientData,
                                     @QueryParam(Constants.TAB_ID) String tabId,
                                     @QueryParam(OIDCLoginProtocol.LOGIN_HINT_PARAM) String loginHint) {
        return performLogin(providerAlias, code, clientId, tabId, clientData, loginHint);
    }

    @GET
    @NoCache
    @Path("/{provider_alias}/login")
    /**
     * 向指定 IdP 发起登录/联邦认证。
     * @param providerAlias IdP 别名
     * @param code 会话码
     * @param clientId 客户端 ID
     * @param tabId 标签页 ID
     * @param clientData 客户端数据
     * @param loginHint 登录提示（可选）
     */
    public Response performLogin(@PathParam("provider_alias") String providerAlias,
                                 @QueryParam(LoginActionsService.SESSION_CODE) String code,
                                 @QueryParam(Constants.CLIENT_ID) String clientId,
                                 @QueryParam(Constants.TAB_ID) String tabId,
                                 @QueryParam(Constants.CLIENT_DATA) String clientData,
                                 @QueryParam(OIDCLoginProtocol.LOGIN_HINT_PARAM) String loginHint) {
        this.event.detail(Details.IDENTITY_PROVIDER, providerAlias);

        if (isDebugEnabled()) {
            logger.debugf("Sending authentication request to identity provider [%s].", providerAlias);
        }

        try {
            AuthenticationSessionModel authSession = parseSessionCode(code, clientId, tabId, clientData);

            ClientSessionCode<AuthenticationSessionModel> clientSessionCode = new ClientSessionCode<>(session, realmModel, authSession);
            clientSessionCode.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
            IdentityProviderModel identityProviderModel = getIdentityProviderModel(session, providerAlias);
            if (identityProviderModel == null) {
                throw new IdentityBrokerException("Identity Provider [" + providerAlias + "] not found.");
            }
            if (Booleans.isTrue(identityProviderModel.isLinkOnly())) {
                throw new IdentityBrokerException("Identity Provider [" + providerAlias + "] is not allowed to perform a login.");
            }
            if (clientSessionCode.getClientSession() != null && loginHint != null) {
                clientSessionCode.getClientSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
            }

            UserAuthenticationIdentityProvider<?> identityProvider = getIdentityProvider(session, identityProviderModel.getAlias());
            Response response = identityProvider.performLogin(createAuthenticationRequest(identityProvider, providerAlias, clientSessionCode));

            if (response != null) {
                if (isDebugEnabled()) {
                    logger.debugf("Identity provider [%s] is going to send a request [%s].", identityProvider.getConfig().getAlias(), response);
                }
                return response;
            }
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (IdentityBrokerException e) {
            return redirectToErrorPage(Response.Status.BAD_GATEWAY, Messages.COULD_NOT_SEND_AUTHENTICATION_REQUEST, e, providerAlias);
        } catch (Exception e) {
            return redirectToErrorPage(Response.Status.INTERNAL_SERVER_ERROR, Messages.UNEXPECTED_ERROR_HANDLING_REQUEST, e, providerAlias);
        }

        return redirectToErrorPage(Response.Status.INTERNAL_SERVER_ERROR, Messages.COULD_NOT_PROCEED_WITH_AUTHENTICATION_REQUEST);
    }

    @Override
    /** {@inheritDoc} IdP 登录重试回调 */
    public Response retryLogin(UserAuthenticationIdentityProvider<?> identityProvider, AuthenticationSessionModel authSession) {
        ClientSessionCode<AuthenticationSessionModel> clientSessionCode = new ClientSessionCode<>(session, realmModel, authSession);
        clientSessionCode.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        Response response = identityProvider.performLogin(createAuthenticationRequest(identityProvider, identityProvider.getConfig().getAlias(), clientSessionCode));

        if (response != null) {
            event.detail(Details.IDENTITY_PROVIDER, identityProvider.getConfig().getAlias())
                    .detail(Details.LOGIN_RETRY, "true")
                    .success();

            if (isDebugEnabled()) {
                logger.debugf("Identity provider [%s] is going to retry a login request [%s].", identityProvider.getConfig().getAlias(), response);
            }
            return response;
        }
        return redirectToErrorPage(Response.Status.INTERNAL_SERVER_ERROR, Messages.COULD_NOT_PROCEED_WITH_AUTHENTICATION_REQUEST);
    }

    @Path("{provider_alias}/endpoint")
    /** 返回 IdP 回调端点（OAuth/SAML 等） */
    public Object getEndpoint(@PathParam("provider_alias") String providerAlias) {
        UserAuthenticationIdentityProvider<?> identityProvider;

        try {
            identityProvider = getIdentityProvider(session, providerAlias);
        } catch (IdentityBrokerException e) {
            throw new NotFoundException(e.getMessage());
        }

        return identityProvider.callback(realmModel, this, event);
    }

    @Path("{provider_alias}/token")
    @OPTIONS
    /** 令牌检索端点 CORS 预检 */
    public Response retrieveTokenPreflight() {
        return Cors.builder().auth().preflight().add(Response.ok());
    }

    @GET
    @NoCache
    @Path("{provider_alias}/token")
    /** IdP 存储令牌检索 API v1（Bearer + broker 角色） */
    public Response retrieveTokenV1(@PathParam("provider_alias") String providerAlias) {
        return getTokenV1(providerAlias);
    }

    @POST
    @NoCache
    @Path("{provider_alias}/token")
    /** IdP 令牌交换 API v2（客户端认证 + 用户令牌） */
    public Response retrieveTokenV2(@PathParam("provider_alias") String providerAlias) {
        return getTokenV2(providerAlias);
    }

    /** v2 令牌检索实现：客户端策略、受众与联邦身份校验 */
    private Response getTokenV2(String providerAlias) {
        this.event.event(EventType.IDENTITY_PROVIDER_RETRIEVE_TOKEN)
                .detail(Details.IDENTITY_PROVIDER, providerAlias);

        Cors cors = Cors.builder().auth().allowedMethods("POST").auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);

        // 校验 IDENTITY_BROKERING_API_V2 特性已启用
        if (!Profile.isFeatureEnabled(Profile.Feature.IDENTITY_BROKERING_API_V2)) {
            event.detail(Details.REASON, "Identity Brokering API feature not enabled");
            event.error(Errors.IDENTITY_PROVIDER_ERROR);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Identity Brokering API feature not enabled", Response.Status.BAD_REQUEST);
        }

        // 客户端认证（禁止 public client）
        AuthorizeClientUtil.ClientAuthResult clientAuth = AuthorizeClientUtil.authorizeClient(session, event, cors);
        ClientModel client = clientAuth.getClient();
        cors.checkAllowedOrigins(session, client);
        event.client(client);
        session.getContext().setClient(client);
        if (client.isPublicClient()) {
            event.detail(Details.REASON, "public clients not allowed");
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, "public clients not allowed", Response.Status.FORBIDDEN);
        }

        // 校验客户端 external-token 配置允许该 IdP
        OIDCAdvancedConfigWrapper oidcClient = OIDCAdvancedConfigWrapper.fromClientModel(client);
        if (!oidcClient.getExternalTokenEnabled() || !oidcClient.getExternalAllowedIdentityProviders().contains(providerAlias)) {
            event.detail(Details.REASON, "Client not allowed to retrieve token for the provider");
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, "Client not allowed to retrieve token for the provider", Response.Status.FORBIDDEN);
        }

        // 校验用户访问令牌（含 DPoP）
        String tokenString = session.getContext().getHttpRequest().getDecodedFormParameters().getFirst(OAuth2Constants.TOKEN);
        AuthenticationManager.AuthResult authResult = AuthenticationManager.verifyIdentityToken(
                session, realmModel, session.getContext().getUri(), clientConnection, true, true, null, false, tokenString, headers,
                verifier -> {
                    DPoPUtil.withDPoPVerifier(verifier, realmModel, new DPoPUtil.Validator(session).request(request).uriInfo(session.getContext().getUri()).accessToken(tokenString));
                    verifier.withChecks(GrantTypeEndpointRestrictionValidator.check(session));
                });
        if (authResult == null) {
            event.error(Errors.INVALID_TOKEN);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_TOKEN, "Invalid token", Response.Status.BAD_REQUEST);
        }
        AccessToken token = authResult.token();
        event.user(authResult.user());

        // 校验请求客户端在令牌受众中
        if (!client.getClientId().equals(token.getIssuedFor()) && !token.hasAudience(client.getClientId())) {
            event.detail(Details.REASON, "client is not within the token audience");
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.UNAUTHORIZED_CLIENT, "Client is not within the token audience", Response.Status.FORBIDDEN);
        }

        // 获取 IdP 配置
        IdentityProviderModel model = session.identityProviders().getByAlias(providerAlias);
        if (model == null || !model.isEnabled()) {
            event.detail(Details.REASON, "Invalid identity provider");
            event.error(Errors.IDENTITY_PROVIDER_ERROR);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Invalid identity provider", Response.Status.BAD_REQUEST);
        }

        // retrieve the provider
        UserAuthenticationIdentityProvider<?> identityProvider = getIdentityProvider(session, model, UserAuthenticationIdentityProvider.class);
        if (identityProvider == null) {
            event.detail(Details.REASON, "Invalid identity provider");
            event.error(Errors.IDENTITY_PROVIDER_ERROR);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Invalid identity provider", Response.Status.BAD_REQUEST);
        }

        // 校验用户已与该 IdP 关联
        FederatedIdentityModel identity = this.session.users().getFederatedIdentity(realmModel, authResult.user(), providerAlias);
        if (identity == null) {
            event.detail(Details.REASON, "User not associated to identity provider");
            event.error(Errors.IDENTITY_PROVIDER_ERROR);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "User not associated to identity provider", Response.Status.BAD_REQUEST);
        }

        // 从令牌解析有效用户会话
        UserSessionModel userSession = UserSessionUtil.findValidSessionForAccessToken(
                session, realmModel, token, authResult.client(), (invalidUserSession -> {}))
                .getUserSession();

        // 触发客户端策略 IdentityBrokeringAPI 事件
        try {
            session.clientPolicy().triggerOnEvent(new IdentityBrokeringAPIContext(session, authResult.token(), client, identityProvider.getConfig().getAlias()));
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw new CorsErrorResponseException(cors, cpe.getError(), cpe.getErrorDetail(), cpe.getErrorStatus());
        }

        // 从 IdP 或存储中检索令牌
        try {
            Response response = identityProvider.retrieveToken(session, identity, userSession, authResult.user());
            event.success();
            return cors.add(Response.fromResponse(response));
        } catch (Exception e) {
            logger.errorf(e, "Failed to retrieve token from identity provider");
            event.detail(Details.REASON, e.getMessage());
            event.error(Errors.INVALID_REQUEST);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Failed to retrieve token from identity provider", Response.Status.BAD_REQUEST);
        }
    }

    /** 检查令牌是否具备 broker read-token 角色 */
    private boolean canReadBrokerToken(AccessToken token) {
        Map<String, AccessToken.Access> resourceAccess = token.getResourceAccess();
        AccessToken.Access brokerRoles = resourceAccess == null ? null : resourceAccess.get(Constants.BROKER_SERVICE_CLIENT_ID);
        return brokerRoles != null && brokerRoles.isUserInRole(Constants.READ_TOKEN_ROLE);
    }

    /** v1 令牌检索：AppAuthManager Bearer 认证 + 存储令牌读取 */
    private Response getTokenV1(String providerAlias) {
        this.event.event(EventType.IDENTITY_PROVIDER_RETRIEVE_TOKEN)
                .detail(Details.IDENTITY_PROVIDER, providerAlias);

        if (!Profile.isFeatureEnabled(Profile.Feature.IDENTITY_BROKERING_API_V1)) {
            event.detail(Details.REASON, "Identity Brokering API feature not enabled");
            event.error(Errors.IDENTITY_PROVIDER_ERROR);
            return badRequest("Identity Brokering API feature not enabled");
        }

        try {
            AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                    .setRealm(realmModel)
                    .setConnection(clientConnection)
                    .setHeaders(request.getHttpHeaders())
                    .authenticate();

            if (authResult == null) {
                return badRequest("Invalid token.");
            }

            AccessToken token = authResult.token();
            ClientModel clientModel = authResult.client();
            UserModel user = authResult.user();

            this.event.client(clientModel);
            this.event.user(user);
            this.session.getContext().setClient(clientModel);

            ClientModel brokerClient = realmModel.getClientByClientId(Constants.BROKER_SERVICE_CLIENT_ID);
            if (brokerClient == null) {
                event.detail(Details.REASON, "Realm has not migrated to support the broker token exchange service");
                event.error(Errors.IDENTITY_PROVIDER_ERROR);
                return corsResponse(forbidden("Realm has not migrated to support the broker token exchange service"), clientModel);
            }

            if (!canReadBrokerToken(token)) {
                event.detail(Details.REASON, "Client not authorized to retrieve tokens for provider");
                event.error(Errors.UNAUTHORIZED_CLIENT);
                return corsResponse(forbidden("Client [" + clientModel.getClientId() + "] not authorized to retrieve tokens from identity provider [" + providerAlias + "]."), clientModel);
            }

            UserAuthenticationIdentityProvider<?> identityProvider = getIdentityProvider(session, providerAlias);
            IdentityProviderModel identityProviderConfig = getIdentityProviderConfig(providerAlias);
            if (Booleans.isFalse(identityProviderConfig.isStoreToken())) {
                event.detail(Details.REASON, "Identity Provider does not support this operation");
                event.error(Errors.IDENTITY_PROVIDER_ERROR);
                return corsResponse(badRequest("Identity Provider [" + providerAlias + "] does not support this operation."), clientModel);
            }

            FederatedIdentityModel identity = this.session.users().getFederatedIdentity(this.realmModel, user, providerAlias);
            if (identity == null) {
                this.event.detail(Details.REASON, "User not associated to identity provider");
                this.event.error(Errors.IDENTITY_PROVIDER_ERROR);
                return corsResponse(badRequest("User [" + user.getId() + "] is not associated with identity provider [" + providerAlias + "]."), clientModel);
            }
            if (identity.getToken() == null) {
                this.event.detail(Details.REASON, "No token stored for user in this provider");
                this.event.error(Errors.IDENTITY_PROVIDER_ERROR);
                return corsResponse(notFound("No token stored for user [" + authResult.user().getId() + "] with associated identity provider [" + providerAlias + "]."), clientModel);
            }

            String oldToken = identity.getToken();
            try {
                Response response = corsResponse(identityProvider.retrieveToken(session, identity), clientModel);
                this.event.success();
                return response;
            } catch (WebApplicationException e) {
                this.event.detail(Details.REASON, e.getMessage());
                this.event.error(Errors.IDENTITY_PROVIDER_ERROR);
                return corsResponse(e.getResponse(), clientModel);
            } finally {
                if (Booleans.isTrue(identityProviderConfig.isStoreToken()) && !Objects.equals(oldToken, identity.getToken())) {
                    session.users().updateFederatedIdentity(session.getContext().getRealm(), user, identity);
                }
            }

        } catch (WebApplicationException e) {
            this.event.detail(Details.REASON, e.getMessage());
            this.event.error(Errors.IDENTITY_PROVIDER_ERROR);
            return e.getResponse();
        } catch (IdentityBrokerException e) {
            return redirectToErrorPage(Response.Status.BAD_GATEWAY, Messages.COULD_NOT_OBTAIN_TOKEN, e, providerAlias);
        } catch (Exception e) {
            return redirectToErrorPage(Response.Status.BAD_GATEWAY, Messages.UNEXPECTED_ERROR_RETRIEVING_TOKEN, e, providerAlias);
        }
    }

    /**
     * IdP 认证成功回调：关联/创建用户、首次 Broker 登录或账户关联。
     * @param context Broker 身份上下文
     * @return 重定向或错误响应
     */
    public Response authenticated(BrokeredIdentityContext context) {
        IdentityProviderModel identityProviderConfig = context.getIdpConfig();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        String providerAlias = identityProviderConfig.getAlias();
        if (Booleans.isFalse(identityProviderConfig.isStoreToken())) {
            if (isDebugEnabled()) {
                logger.debugf("Token will not be stored for identity provider [%s].", providerAlias);
            }
            context.setToken(null);
        }

        ClientModel client = authenticationSession.getClient();

        if (!client.isEnabled()) {
            return redirectToErrorPage(Status.BAD_REQUEST, Messages.CLIENT_NOT_FOUND, null, providerAlias);
        }

        StatusResponseType loginResponse = (StatusResponseType) context.getContextData().get(SAMLEndpoint.SAML_LOGIN_RESPONSE);
        if (loginResponse != null) {
            for(Iterator<SamlAuthenticationPreprocessor> it = SamlSessionUtils.getSamlAuthenticationPreprocessorIterator(session); it.hasNext();) {
                loginResponse = it.next().beforeProcessingLoginResponse(loginResponse, authenticationSession);
            }
        }

        session.getContext().setClient(client);

        context.getIdp().preprocessFederatedIdentity(session, realmModel, context);
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        session.identityProviders().getMappersByAliasStream(context.getIdpConfig().getAlias()).forEach(mapper -> {
            IdentityProviderMapper target = (IdentityProviderMapper) sessionFactory
                    .getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
            target.preprocessFederatedIdentity(session, realmModel, mapper, context);
        });

        FederatedIdentityModel federatedIdentityModel = new FederatedIdentityModel(providerAlias, context.getId(),
                context.getUsername(), context.getToken());

        this.event.event(EventType.IDENTITY_PROVIDER_LOGIN)
                .detail(Details.REDIRECT_URI, authenticationSession.getRedirectUri())
                .detail(Details.IDENTITY_PROVIDER, providerAlias)
                .detail(Details.IDENTITY_PROVIDER_USERNAME, context.getUsername())
                .detail(Details.IDENTITY_PROVIDER_BROKER_SESSION_ID, context.getBrokerSessionId());

        UserModel federatedUser = this.session.users().getUserByFederatedIdentity(this.realmModel, federatedIdentityModel);
        boolean shouldMigrateId = false;
        // 尝试用 legacy federated ID 查找用户
        if (federatedUser == null && context.getLegacyId() != null) {
            federatedIdentityModel = new FederatedIdentityModel(federatedIdentityModel, context.getLegacyId());
            federatedUser = this.session.users().getUserByFederatedIdentity(this.realmModel, federatedIdentityModel);
            shouldMigrateId = true;
        }

        // 判断是账户关联还是正常联邦登录
        if (isDoingAccountLinking(authenticationSession, true, providerAlias)) {
            return performAccountLinking(authenticationSession, context, federatedIdentityModel, federatedUser);
        }

        if (Booleans.isTrue(identityProviderConfig.isLinkOnly())) {
            event.error(Errors.NOT_ALLOWED);
            return redirectToErrorPage(authenticationSession, Response.Status.BAD_REQUEST, Messages.COULD_NOT_SEND_AUTHENTICATION_REQUEST, providerAlias);
        }

        if (federatedUser == null) {

            logger.debugf("Federated user not found for provider '%s' and broker username '%s'", providerAlias, context.getUsername());
            authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, context.getUsername());

            String username = context.getModelUsername();
            if (username == null) {
                if (this.realmModel.isRegistrationEmailAsUsername() && !Validation.isBlank(context.getEmail())) {
                    username = context.getEmail();
                } else if (context.getUsername() == null) {
                    username = context.getIdpConfig().getAlias() + "." + context.getId();
                } else {
                    username = context.getUsername();
                }
            }
            username = username.trim();
            context.setModelUsername(username);

            SerializedBrokeredIdentityContext ctx0 = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authenticationSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
            if (ctx0 != null) {
                SerializedBrokeredIdentityContext ctx1 = SerializedBrokeredIdentityContext.serialize(context);
                ctx1.saveToAuthenticationSession(authenticationSession, AbstractIdpAuthenticator.NESTED_FIRST_BROKER_CONTEXT);
                logger.warnv("Nested first broker flow detected: {0} -> {1}", ctx0.getIdentityProviderId(), ctx1.getIdentityProviderId());
                logger.debug("Resuming last execution");
                URI redirect = new AuthenticationFlowURLHelper(session, realmModel, session.getContext().getUri())
                    .getLastExecutionUrl(authenticationSession);
                return Response.status(Status.FOUND).location(redirect).build();
            }

            logger.debug("Redirecting to flow for firstBrokerLogin");

            boolean forwardedPassiveLogin = "true".equals(authenticationSession.getAuthNote(AuthenticationProcessor.FORWARDED_PASSIVE_LOGIN));

            String userRequestedLocale = authenticationSession.getAuthNote(LocaleSelectorProvider.USER_REQUEST_LOCALE);
            // Redirect to firstBrokerLogin after successful login and ensure that previous authentication state removed
            AuthenticationProcessor.resetFlow(authenticationSession, LoginActionsService.FIRST_BROKER_LOGIN_PATH);
            if (userRequestedLocale != null) {
                authenticationSession.setAuthNote(LocaleSelectorProvider.USER_REQUEST_LOCALE, userRequestedLocale);
            }

            // Set the FORWARDED_PASSIVE_LOGIN note (if needed) after resetting the session so it is not lost.
            if (forwardedPassiveLogin) {
                authenticationSession.setAuthNote(AuthenticationProcessor.FORWARDED_PASSIVE_LOGIN, "true");
            }

            SerializedBrokeredIdentityContext ctx = SerializedBrokeredIdentityContext.serialize(context);
            ctx.saveToAuthenticationSession(authenticationSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);

            URI redirect = LoginActionsService.firstBrokerLoginProcessor(session.getContext().getUri())
                    .queryParam(Constants.CLIENT_ID, client.getClientId())
                    .queryParam(Constants.TAB_ID, authenticationSession.getTabId())
                    .queryParam(Constants.CLIENT_DATA, AuthenticationProcessor.getClientData(session, authenticationSession))
                    .build(realmModel.getName());
            return Response.status(302).location(redirect).build();

        } else {
            authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, federatedUser.getUsername());
            Response response = validateUser(authenticationSession, federatedUser, realmModel);
            if (response != null) {
                return response;
            }

            updateFederatedIdentity(context, federatedUser);
            if (shouldMigrateId) {
                migrateFederatedIdentityId(context, federatedUser);
            }
            authenticationSession.setAuthenticatedUser(federatedUser);

            return finishOrRedirectToPostBrokerLogin(authenticationSession, context, false);
        }
    }


    /** 校验联邦用户已启用且未被暴力破解临时锁定 */
    public Response validateUser(AuthenticationSessionModel authSession, UserModel user, RealmModel realm) {
        if (!user.isEnabled()) {
            event.error(Errors.USER_DISABLED);
            return ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.ACCOUNT_DISABLED);
        }
        if (realm.isBruteForceProtected()) {
            if (session.getProvider(BruteForceProtector.class).isTemporarilyDisabled(session, realm, user)) {
                event.error(Errors.USER_TEMPORARILY_DISABLED);
                return ErrorPage.error(session, authSession, Response.Status.BAD_REQUEST, Messages.ACCOUNT_DISABLED);
            }
        }
        return null;
    }

    // LoginActionsService 首次 Broker 登录完成后的回调 and Keycloak account is successfully linked/created
    @GET
    @NoCache
    @Path("/after-first-broker-login")
    /** 首次 Broker 登录流程完成后的 HTTP 入口 */
    public Response afterFirstBrokerLogin(@QueryParam(LoginActionsService.SESSION_CODE) String code,
                                          @QueryParam(Constants.CLIENT_ID) String clientId,
                                          @QueryParam(Constants.CLIENT_DATA) String clientData,
                                          @QueryParam(Constants.TAB_ID) String tabId) {
        AuthenticationSessionModel authSession = parseSessionCode(code, clientId, tabId, clientData);
        return afterFirstBrokerLogin(authSession);
    }

    /** 创建/关联联邦身份、导入新用户、授予 read-token 角色 */
    private Response afterFirstBrokerLogin(AuthenticationSessionModel authSession) {
        try {
            this.event.detail(Details.CODE_ID, authSession.getParentSession().getId())
                    .removeDetail("auth_method");

            SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
            if (serializedCtx == null) {
                throw new IdentityBrokerException("Not found serialized context in clientSession");
            }
            BrokeredIdentityContext context = serializedCtx.deserialize(session, authSession);
            String providerAlias = context.getIdpConfig().getAlias();

            event.detail(Details.IDENTITY_PROVIDER, providerAlias);
            event.detail(Details.IDENTITY_PROVIDER_USERNAME, context.getUsername());

            // 确认 first-broker-login 流程已成功完成
            String authProvider = authSession.getAuthNote(AbstractIdpAuthenticator.FIRST_BROKER_LOGIN_SUCCESS);
            if (authProvider == null || !authProvider.equals(providerAlias)) {
                throw new IdentityBrokerException("Invalid request. Not found the flag that first-broker-login flow was finished");
            }

            // 清除 BROKERED_CONTEXT note
            authSession.removeAuthNote(AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);

            UserModel federatedUser = authSession.getAuthenticatedUser();
            if (federatedUser == null) {
                throw new IdentityBrokerException("Couldn't found authenticated federatedUser in authentication session");
            }

            event.user(federatedUser);
            event.detail(Details.USERNAME, federatedUser.getUsername());

            if (Booleans.isTrue(context.getIdpConfig().isAddReadTokenRoleOnCreate())) {
                ClientModel brokerClient = realmModel.getClientByClientId(Constants.BROKER_SERVICE_CLIENT_ID);
                if (brokerClient == null) {
                    logger.warnf("Identity provider '%s' has 'Stored tokens readable' enabled, but the broker client does not exist. This option requires the broker client with read-token role, which is only created when identity-broker-api:v1 is enabled.", context.getIdpConfig().getAlias());
                } else {
                    RoleModel readTokenRole = brokerClient.getRole(Constants.READ_TOKEN_ROLE);
                    if (readTokenRole == null) {
                        logger.warnf("Identity provider '%s' has 'Stored tokens readable' enabled, but the read-token role does not exist in the broker client.", context.getIdpConfig().getAlias());
                    } else {
                        federatedUser.grantRole(readTokenRole);
                    }
                }
            }

            // 为非轻量用户添加联邦身份链接
            if (!(federatedUser instanceof LightweightUserAdapter)) {
                checkOverrideLink(authSession, federatedUser, providerAlias);

                FederatedIdentityModel federatedIdentityModel = new FederatedIdentityModel(context.getIdpConfig().getAlias(), context.getId(),
                        context.getUsername(), context.getToken());
                try {
                    session.users().addFederatedIdentity(realmModel, federatedUser, federatedIdentityModel);
                } catch (ModelDuplicateException de) {
                    String idpDisplayName = KeycloakModelUtils.getIdentityProviderDisplayName(session, context.getIdpConfig());
                    return redirectToErrorPage(authSession, Status.CONFLICT, Messages.IDENTITY_PROVIDER_ALREADY_LINKED_TO_CURRENT_USER, de, idpDisplayName);
                }
            }

            String isRegisteredNewUser = authSession.getAuthNote(BROKER_REGISTERED_NEW_USER);
            if (Boolean.parseBoolean(isRegisteredNewUser)) {

                logger.debugf("Registered new user '%s' after first login with identity provider '%s'. Identity provider username is '%s' . ", federatedUser.getUsername(), providerAlias, context.getUsername());

                UserAuthenticationIdentityProvider<?> idp = context.getIdp();
                idp.importNewUser(session, realmModel, federatedUser, context);
                KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
                session.identityProviders().getMappersByAliasStream(providerAlias).forEach(mapper -> {
                    IdentityProviderMapper target = (IdentityProviderMapper) sessionFactory
                            .getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
                    target.importNewUser(session, realmModel, federatedUser, mapper, context);
                });

                idp.updateBrokeredUser(session, realmModel, federatedUser, context);

                event.clone()
                        .event(EventType.REGISTER)
                        .detail(Details.REGISTER_METHOD, "broker")
                        .detail(Details.EMAIL, federatedUser.getEmail())
                        .success();

            } else {
                logger.debugf("Linked existing keycloak user '%s' with identity provider '%s' . Identity provider username is '%s' .", federatedUser.getUsername(), providerAlias, context.getUsername());

                event.event(EventType.FEDERATED_IDENTITY_LINK)
                        .success();

                updateFederatedIdentity(context, federatedUser);
            }

            return finishOrRedirectToPostBrokerLogin(authSession, context, true);
        }  catch (Exception e) {
            return redirectToErrorPage(authSession, Response.Status.INTERNAL_SERVER_ERROR, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR, e);
        }
    }

    /** 若用户确认覆盖现有关联则移除旧联邦身份 */
    private void checkOverrideLink(AuthenticationSessionModel authSession, UserModel federatedUser, String providerAlias) {
        String isOverride = authSession.getAuthNote(IdpConfirmOverrideLinkAuthenticator.OVERRIDE_LINK);
        if (!Boolean.parseBoolean(isOverride)) {
            return;
        }

        FederatedIdentityModel previous = session.users()
                .getFederatedIdentity(realmModel, federatedUser, providerAlias);
        if (previous == null) {
            return;
        }

        session.users().removeFederatedIdentity(realmModel, federatedUser, providerAlias);

        event.clone()
                .event(EventType.FEDERATED_IDENTITY_OVERRIDE_LINK)
                .detail(Details.PREF_PREVIOUS + Details.IDENTITY_PROVIDER_USERNAME, previous.getUserName())
                .success();
    }

    /** 完成认证或重定向到 post-broker-login 流程 */
    private Response finishOrRedirectToPostBrokerLogin(AuthenticationSessionModel authSession, BrokeredIdentityContext context, boolean wasFirstBrokerLogin) {
        String postBrokerLoginFlowId = context.getIdpConfig().getPostBrokerLoginFlowId();
        if (postBrokerLoginFlowId == null) {

            logger.debugf("Skip redirect to postBrokerLogin flow. PostBrokerLogin flow not set for identityProvider '%s'.", context.getIdpConfig().getAlias());
            return afterPostBrokerLoginFlowSuccess(authSession, context, wasFirstBrokerLogin);
        } else {

            logger.debugf("Redirect to postBrokerLogin flow after authentication with identityProvider '%s'.", context.getIdpConfig().getAlias());

            authSession.getParentSession().setTimestamp(Time.currentTime());

            SerializedBrokeredIdentityContext ctx = SerializedBrokeredIdentityContext.serialize(context);
            ctx.saveToAuthenticationSession(authSession, PostBrokerLoginConstants.PBL_BROKERED_IDENTITY_CONTEXT);

            authSession.setAuthNote(PostBrokerLoginConstants.PBL_AFTER_FIRST_BROKER_LOGIN, String.valueOf(wasFirstBrokerLogin));

            URI redirect = LoginActionsService.postBrokerLoginProcessor(session.getContext().getUri())
                    .queryParam(Constants.CLIENT_ID, authSession.getClient().getClientId())
                    .queryParam(Constants.TAB_ID, authSession.getTabId())
                    .queryParam(Constants.CLIENT_DATA, AuthenticationProcessor.getClientData(session, authSession))
                    .build(realmModel.getName());
            return Response.status(302).location(redirect).build();
        }
    }


    // post-broker-login 流程完成后的回调
    @GET
    @NoCache
    @Path("/after-post-broker-login")
    /** post-broker-login 流程完成 HTTP 入口 */
    public Response afterPostBrokerLoginFlow(@QueryParam(LoginActionsService.SESSION_CODE) String code,
                                             @QueryParam(Constants.CLIENT_ID) String clientId,
                                             @QueryParam(Constants.CLIENT_DATA) String clientData,
                                             @QueryParam(Constants.TAB_ID) String tabId) {
        AuthenticationSessionModel authenticationSession = parseSessionCode(code, clientId, tabId, clientData);

        try {
            SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authenticationSession, PostBrokerLoginConstants.PBL_BROKERED_IDENTITY_CONTEXT);
            if (serializedCtx == null) {
                throw new IdentityBrokerException("Not found serialized context in clientSession. Note " + PostBrokerLoginConstants.PBL_BROKERED_IDENTITY_CONTEXT + " was null");
            }
            BrokeredIdentityContext context = serializedCtx.deserialize(session, authenticationSession);

            String wasFirstBrokerLoginNote = authenticationSession.getAuthNote(PostBrokerLoginConstants.PBL_AFTER_FIRST_BROKER_LOGIN);
            boolean wasFirstBrokerLogin = Boolean.parseBoolean(wasFirstBrokerLoginNote);

            // 确认 post-broker-login 流程已成功完成
            String authStateNoteKey = PostBrokerLoginConstants.PBL_AUTH_STATE_PREFIX + context.getIdpConfig().getAlias();
            String authState = authenticationSession.getAuthNote(authStateNoteKey);
            if (!Boolean.parseBoolean(authState)) {
                throw new IdentityBrokerException("Invalid request. Not found the flag that post-broker-login flow was finished");
            }

            // 清除 PBL 相关 note
            authenticationSession.removeAuthNote(PostBrokerLoginConstants.PBL_BROKERED_IDENTITY_CONTEXT);
            authenticationSession.removeAuthNote(PostBrokerLoginConstants.PBL_AFTER_FIRST_BROKER_LOGIN);

            return afterPostBrokerLoginFlowSuccess(authenticationSession, context, wasFirstBrokerLogin);
        } catch (IdentityBrokerException e) {
            return redirectToErrorPage(authenticationSession, Response.Status.INTERNAL_SERVER_ERROR, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR, e);
        }
    }

    /** post-broker-login 成功后继续首次或常规 Broker 认证 */
    private Response afterPostBrokerLoginFlowSuccess(AuthenticationSessionModel authSession, BrokeredIdentityContext context, boolean wasFirstBrokerLogin) {
        String providerAlias = context.getIdpConfig().getAlias();
        UserModel federatedUser = authSession.getAuthenticatedUser();

        if (wasFirstBrokerLogin) {
            return finishBrokerAuthentication(context, federatedUser, authSession, providerAlias);
        } else {

            boolean firstBrokerLoginInProgress = (authSession.getAuthNote(AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE) != null);
            if (firstBrokerLoginInProgress) {
                logger.debugf("Reauthenticated with broker '%s' when linking user '%s' with other broker", context.getIdpConfig().getAlias(), federatedUser.getUsername());

                SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
                authSession.setAuthNote(AbstractIdpAuthenticator.FIRST_BROKER_LOGIN_SUCCESS, serializedCtx.getIdentityProviderId());

                return afterFirstBrokerLogin(authSession);
            } else {
                return finishBrokerAuthentication(context, federatedUser, authSession, providerAlias);
            }
        }
    }


    /** 完成 Broker 认证：设置会话 note、检查 required-action 并重定向 */
    private Response finishBrokerAuthentication(BrokeredIdentityContext context, UserModel federatedUser, AuthenticationSessionModel authSession, String providerAlias) {
        authSession.setAuthNote(AuthenticationProcessor.BROKER_SESSION_ID, context.getBrokerSessionId());
        authSession.setAuthNote(AuthenticationProcessor.BROKER_USER_ID, context.getBrokerUserId());

        this.event.user(federatedUser);

        context.getIdp().authenticationFinished(authSession, context);
        authSession.setUserSessionNote(Details.IDENTITY_PROVIDER, providerAlias);
        authSession.setUserSessionNote(Details.IDENTITY_PROVIDER_USERNAME, context.getUsername());

        event.detail(Details.IDENTITY_PROVIDER, providerAlias)
                .detail(Details.IDENTITY_PROVIDER_USERNAME, context.getUsername())
                .detail(Details.IDENTITY_PROVIDER_BROKER_SESSION_ID, context.getBrokerSessionId());

        if (isDebugEnabled()) {
            logger.debugf("Performing local authentication for user [%s].", federatedUser);
        }

        AuthenticationManager.setClientScopesInSession(session, authSession);

        String nextRequiredAction = AuthenticationManager.nextRequiredAction(session, authSession, request, event);
        if (nextRequiredAction != null) {
            if ("true".equals(authSession.getAuthNote(AuthenticationProcessor.FORWARDED_PASSIVE_LOGIN))) {
                logger.errorf("Required action %s found. Auth requests using prompt=none are incompatible with required actions", nextRequiredAction);
                return checkPassiveLoginError(authSession, OAuthErrorException.INTERACTION_REQUIRED);
            }
            return AuthenticationManager.redirectToRequiredActions(session, realmModel, authSession, session.getContext().getUri(), nextRequiredAction);
        } else {
            event.detail(Details.CODE_ID, authSession.getParentSession().getId());  // todo This should be set elsewhere.  find out why tests fail.  Don't know where this is supposed to be set
            return AuthenticationManager.finishedRequiredActions(session, authSession, null, clientConnection, request, session.getContext().getUri(), event);
        }
    }


    @Override
    /** {@inheritDoc} IdP 认证取消回调 */
    public Response cancelled(IdentityProviderModel idpConfig) {
        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
        event.detail(Details.IDENTITY_PROVIDER, idpConfig.getAlias());

        // Check if linking was requested (for example by kc_action) or if we're authenticating
        if (isDoingAccountLinking(authSession, true, idpConfig.getAlias())) {
            authSession.setAuthNote(IdpLinkAction.IDP_LINK_STATUS, RequiredActionContext.KcActionStatus.CANCELLED.name());
            return redirectAfterIDPLinking(authSession);
        }
        String idpDisplayName = KeycloakModelUtils.getIdentityProviderDisplayName(session, idpConfig);
        return browserAuthentication(authSession, Messages.ACCESS_DENIED_WHEN_IDP_AUTH, idpDisplayName);
    }

    @Override
    /** {@inheritDoc} IdP 认证错误回调 */
    public Response error(IdentityProviderModel idpConfig, String message) {
        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
        event.detail(Details.IDENTITY_PROVIDER, idpConfig.getAlias());

        // Check if linking was requested (for example by kc_action) or if we're authenticating
        UserSessionModel userSession = new AuthenticationSessionManager(session).getUserSession(authSession);
        if (isDoingAccountLinking(authSession, true, idpConfig.getAlias())) {
            return redirectToErrorWhenLinkingFailed(authSession, message);
        }

        Response passiveLoginErrorReturned = checkPassiveLoginError(authSession, message);
        if (passiveLoginErrorReturned != null) {
            return passiveLoginErrorReturned;
        }

        return browserAuthentication(authSession, message);
    }


    /** 判断是否处于账户关联流程并校验 LINKING note */
    private boolean isDoingAccountLinking(AuthenticationSessionModel authSession, boolean checkProviderAlias, String providerAlias) {
        String noteFromSession = authSession.getAuthNote(LINKING_IDENTITY_PROVIDER);
        if (noteFromSession == null) {
            return false;
        }

        boolean linkingValid;
        if (checkProviderAlias) {
            String expectedNote = authSession.getParentSession().getId() + authSession.getClient().getClientId() + providerAlias;
            linkingValid = expectedNote.equals(noteFromSession);
        } else {
            String expectedNotePrefix = authSession.getParentSession().getId() + authSession.getClient().getClientId();
            linkingValid = noteFromSession.startsWith(expectedNotePrefix);
        }

        if (linkingValid) {
            authSession.removeAuthNote(LINKING_IDENTITY_PROVIDER);
            return true;
        } else {
            throw new ErrorPageException(session, Response.Status.BAD_REQUEST, Messages.BROKER_LINKING_SESSION_EXPIRED);
        }
    }


    /** 将 IdP 身份关联到已登录用户 */
    private Response performAccountLinking(AuthenticationSessionModel authSession, BrokeredIdentityContext context, FederatedIdentityModel newModel, UserModel federatedUser) {
        this.event.event(EventType.FEDERATED_IDENTITY_LINK);

        UserModel authenticatedUser = authSession.getAuthenticatedUser();
        authSession.setAuthenticatedUser(authenticatedUser);

        logger.debugf("Will try to link identity provider [%s] to user [%s]", context.getIdpConfig().getAlias(), authenticatedUser.getUsername());

        if (federatedUser != null && !authenticatedUser.getId().equals(federatedUser.getId())) {
            logger.debugf("Cannot link user '%s' to identity provider '%s' . Other user '%s' already linked with the identity provider", authenticatedUser.getUsername(), context.getIdpConfig().getAlias(), federatedUser.getUsername());
            String idpDisplayName = KeycloakModelUtils.getIdentityProviderDisplayName(session, context.getIdpConfig());
            return redirectToErrorWhenLinkingFailed(authSession, Messages.IDENTITY_PROVIDER_ALREADY_LINKED, idpDisplayName);
        }

        RoleModel manageAccountRole = this.realmModel.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID).getRole(AccountRoles.MANAGE_ACCOUNT);
        RoleModel manageAccountLinkRole = this.realmModel.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID).getRole(AccountRoles.MANAGE_ACCOUNT_LINKS);
        if (!authenticatedUser.hasRole(manageAccountRole) && !authenticatedUser.hasRole(manageAccountLinkRole)) {
            return redirectToErrorPage(authSession, Response.Status.FORBIDDEN, Messages.INSUFFICIENT_PERMISSION);
        }

        if (!authenticatedUser.isEnabled()) {
            return redirectToErrorWhenLinkingFailed(authSession, Messages.ACCOUNT_DISABLED);
        }

        if (!Organizations.resolveHomeBroker(session, authenticatedUser).isEmpty()) {
            return redirectToErrorWhenLinkingFailed(authSession, Messages.FEDERATED_IDENTITY_BOUND_ORGANIZATION);
        }

        if (federatedUser != null) {
            if (Booleans.isTrue(context.getIdpConfig().isStoreToken())) {
                FederatedIdentityModel oldModel = this.session.users().getFederatedIdentity(this.realmModel, federatedUser, context.getIdpConfig().getAlias());
                if (!ObjectUtil.isEqualOrBothNull(context.getToken(), oldModel.getToken())) {
                    this.session.users().updateFederatedIdentity(this.realmModel, federatedUser, newModel);
                    if (isDebugEnabled()) {
                        logger.debugf("Identity [%s] update with response from identity provider [%s].", federatedUser, context.getIdpConfig().getAlias());
                    }
                }
            }
        } else {
            try {
                this.session.users().addFederatedIdentity(this.realmModel, authenticatedUser, newModel);
                federatedUser = authenticatedUser;
            } catch(ModelDuplicateException e) {
                logger.warnf(e,"Cannot link user '%s' to identity provider '%s' as the link already exists for this user and identity provider",
                        authenticatedUser.getUsername(), context.getIdpConfig().getAlias());
                String idpDisplayName = KeycloakModelUtils.getIdentityProviderDisplayName(session, context.getIdpConfig());
                return redirectToErrorWhenLinkingFailed(authSession, Messages.IDENTITY_PROVIDER_ALREADY_LINKED_TO_CURRENT_USER, idpDisplayName);
            }
        }

        updateFederatedIdentity(context, federatedUser);

        context.getIdp().authenticationFinished(authSession, context);

        if (isDebugEnabled()) {
            logger.debugf("Linking account [%s] from identity provider [%s] to user [%s].", newModel, context.getIdpConfig().getAlias(), authenticatedUser);
        }

        // 写入用户会话 note 以便登出时一并注销 IdP 会话 when this user session is complete.
        // But for the case when userSession was previously authenticated with broker1 and now is linked to another broker2, we shouldn't override broker1 notes with the broker2 for sure.
        // Maybe broker logout should be rather always skiped in case of broker-linking
        UserSessionModel userSession = new AuthenticationSessionManager(session).getUserSession(authSession);
        if (userSession != null && userSession.getNote(Details.IDENTITY_PROVIDER) == null) {
            userSession.setNote(Details.IDENTITY_PROVIDER, context.getIdpConfig().getAlias());
            userSession.setNote(Details.IDENTITY_PROVIDER_USERNAME, context.getUsername());
        }

        authSession.setAuthNote(IdpLinkAction.IDP_LINK_STATUS, RequiredActionContext.KcActionStatus.SUCCESS.name());

        if (!Boolean.parseBoolean(authSession.getAuthNote(IdpLinkAction.KC_ACTION_LINKING_IDENTITY_PROVIDER))) {
            // 传统客户端发起关联：重定向到 redirect_uri

            // In legacy client-initiated account linking, the userSession should exists before linking was started, however it might be expired during the time when user is authenticating to the IDP
            if (userSession == null) {
                return redirectToErrorWhenLinkingFailed(authSession, Messages.BROKER_LINKING_SESSION_EXPIRED);
            }

            AuthenticationManager.setClientScopesInSession(session, authSession);
            TokenManager.attachAuthenticationSession(session, userSession, authSession);

            this.event.user(authenticatedUser)
                    .detail(Details.USERNAME, authenticatedUser.getUsername())
                    .detail(Details.IDENTITY_PROVIDER, newModel.getIdentityProvider())
                    .detail(Details.IDENTITY_PROVIDER_USERNAME, newModel.getUserName())
                    .success();
        }
        return redirectAfterIDPLinking(authSession);
    }

    /** 账户关联完成后重定向（AIA 或 legacy redirect_uri） */
    private Response redirectAfterIDPLinking(AuthenticationSessionModel authSession) {
        URI redirect;
        if (Boolean.parseBoolean(authSession.getAuthNote(IdpLinkAction.KC_ACTION_LINKING_IDENTITY_PROVIDER))) {
            // AIA idp_link：重定向到 required-action 完成流程
            ClientSessionCode<AuthenticationSessionModel> clientSessionCode = new ClientSessionCode<>(session, realmModel, authSession);
            clientSessionCode.setAction(AuthenticationSessionModel.Action.REQUIRED_ACTIONS.name());
            String sessionCode = clientSessionCode.getOrGenerateCode();

            authSession.setAction(AuthenticationSessionModel.Action.REQUIRED_ACTIONS.name());
            return new LoginActionsService(session, event).requiredActionPOST(null,
                    sessionCode,
                    authSession.getClientNote(Constants.KC_ACTION),
                    authSession.getClient().getClientId(),
                    AuthenticationProcessor.getClientData(session, authSession),
                    authSession.getTabId()
                    );
        } else {
            // Legacy client-initiated account linking
            redirect = UriBuilder.fromUri(authSession.getRedirectUri()).build();
        }
        return Response.status(302).location(redirect).build();
    }


    /** 关联失败时将错误序列化到 note 并重定向 */
    private Response redirectToErrorWhenLinkingFailed(AuthenticationSessionModel authSession, String error, Object... parameters) {
        FormMessage errorMessage = new FormMessage(error, parameters);
        String serializedError;
        try {
            serializedError = JsonSerialization.writeValueAsString(errorMessage);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        authSession.setAuthNote(IdpLinkAction.IDP_LINK_STATUS, RequiredActionContext.KcActionStatus.ERROR.name());
        authSession.setAuthNote(IdpLinkAction.IDP_LINK_ERROR, serializedError);
        return redirectAfterIDPLinking(authSession);
    }


    /** 同步联邦身份属性、令牌及 IdP Mapper */
    private void updateFederatedIdentity(BrokeredIdentityContext context, UserModel federatedUser) {
        FederatedIdentityModel federatedIdentityModel = this.session.users().getFederatedIdentity(this.realmModel, federatedUser, context.getIdpConfig().getAlias());

        if (context.getIdpConfig().getSyncMode() == IdentityProviderSyncMode.FORCE) {
            setBasicUserAttributes(context, federatedUser);

            if (!Objects.equals(context.getUsername(), federatedIdentityModel.getUserName())) {
                federatedIdentityModel = new FederatedIdentityModel(federatedIdentityModel.getIdentityProvider(),
                        federatedIdentityModel.getUserId(), context.getUsername(),
                        federatedIdentityModel.getToken());

                this.session.users().updateFederatedIdentity(this.realmModel, federatedUser, federatedIdentityModel);
            }
        }

        // 令牌未变化时跳过数据库写入
        updateToken(context, federatedUser, federatedIdentityModel);
        context.getIdp().updateBrokeredUser(session, realmModel, federatedUser, context);
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        session.identityProviders().getMappersByAliasStream(context.getIdpConfig().getAlias()).forEach(mapper -> {
            IdentityProviderMapper target = (IdentityProviderMapper) sessionFactory
                    .getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
            IdentityProviderMapperSyncModeDelegate.delegateUpdateBrokeredUser(session, realmModel, federatedUser, mapper, context, target);
        });
    }

    /** FORCE 同步模式下更新姓名等基本属性 */
    private void setBasicUserAttributes(BrokeredIdentityContext context, UserModel federatedUser) {
        setDiffAttrToConsumer(federatedUser.getFirstName(), context.getFirstName(), federatedUser::setFirstName, false);
        setDiffAttrToConsumer(federatedUser.getLastName(), context.getLastName(), federatedUser::setLastName, false);
    }

    /** 属性值变化时调用 setter */
    private void setDiffAttrToConsumer(String actualValue, String newValue, Consumer<String> consumer, boolean ignoreCase) {
        String actualValueNotNull = Optional.ofNullable(actualValue).orElse("");
        if (newValue != null && !(ignoreCase? newValue.equalsIgnoreCase(actualValueNotNull) : newValue.equals(actualValueNotNull))) {
            consumer.accept(newValue);
        }
    }

    /** 将 legacy federated ID 迁移为新 ID（删旧建新） */
    private void migrateFederatedIdentityId(BrokeredIdentityContext context, UserModel federatedUser) {
        FederatedIdentityModel identityModel = this.session.users().getFederatedIdentity(this.realmModel, federatedUser, context.getIdpConfig().getAlias());
        FederatedIdentityModel migratedIdentityModel = new FederatedIdentityModel(identityModel, context.getId());

        // 联邦 ID 为复合主键的一部分，需删除后重建
        session.users().removeFederatedIdentity(realmModel, federatedUser, identityModel.getIdentityProvider());
        session.users().addFederatedIdentity(realmModel, federatedUser, migratedIdentityModel);
        logger.debugf("Federated user ID was migrated from %s to %s", identityModel.getUserId(), migratedIdentityModel.getUserId());
    }

    /** 更新存储的 IdP 令牌（保留 refresh token 等特殊逻辑） */
    private void updateToken(BrokeredIdentityContext context, UserModel federatedUser, FederatedIdentityModel federatedIdentityModel) {
        if (Booleans.isTrue(context.getIdpConfig().isStoreToken()) && !ObjectUtil.isEqualOrBothNull(context.getToken(), federatedIdentityModel.getToken())) {
            // like in OIDCIdentityProvider.exchangeStoredToken()
            // Google 等 IdP：上下文无 refresh token 时不覆盖 DB 中已有值
            // as for google IDP it will be lost forever
            if (federatedIdentityModel.getToken() != null && ExchangeTokenToIdentityProviderToken.class.isInstance(context.getIdp())) {
                try {
                    AccessTokenResponse previousResponse = JsonSerialization.readValue(federatedIdentityModel.getToken(), AccessTokenResponse.class);
                    AccessTokenResponse newResponse = JsonSerialization.readValue(context.getToken(), AccessTokenResponse.class);

                    if (newResponse.getRefreshToken() == null && previousResponse.getRefreshToken() != null) {
                        newResponse.setRefreshToken(previousResponse.getRefreshToken());
                        newResponse.setRefreshExpiresIn(previousResponse.getRefreshExpiresIn());
                    }

                    federatedIdentityModel.setToken(JsonSerialization.writeValueAsString(newResponse));
                } catch (IOException ioe) {
                    logger.debugf("Token deserialization failed for identity provider %s:  %s", context.getIdpConfig().getAlias(), ioe.getMessage());
                    federatedIdentityModel.setToken(context.getToken());
                }
            } else {
                federatedIdentityModel.setToken(context.getToken());
            }

            this.session.users().updateFederatedIdentity(this.realmModel, federatedUser, federatedIdentityModel);
            logger.debugf("Identity [%s] update with response from identity provider [%s].", federatedUser, context.getIdpConfig().getAlias());
        }
    }

    @Override
    /** {@inheritDoc} 解码 broker state 并解析认证会话 */
    public AuthenticationSessionModel getAndVerifyAuthenticationSession(String encodedCode) {
        IdentityBrokerState state = IdentityBrokerState.encoded(encodedCode, realmModel);
        String code = state.getDecodedState();
        String clientId = state.getClientId();
        String tabId = state.getTabId();
        String clientData = state.getClientData();
        return parseSessionCode(code, clientId, tabId, clientData);
    }

    /**
     * 解析会话码；失败时抛出 WebApplicationException，永不返回 null
     */
    /** 解析并校验 broker 回调会话码 */
    private AuthenticationSessionModel parseSessionCode(String code, String clientId, String tabId, String clientData) {
        if (code == null || clientId == null || tabId == null) {
            logger.debugf("Invalid request. Authorization code, clientId or tabId was null. Code=%s, clientId=%s, tabID=%s", code, clientId, tabId);
            Response staleCodeError = redirectToErrorPage(Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
            throw new WebApplicationException(staleCodeError);
        }

        SessionCodeChecks checks = new SessionCodeChecks(realmModel, session.getContext().getUri(), request, clientConnection, session, event, null, code, null, clientId, tabId, clientData, LoginActionsService.AUTHENTICATE_PATH);
        checks.initialVerify();
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(), ClientSessionCode.ActionType.LOGIN)) {

            AuthenticationSessionModel authSession = checks.getAuthenticationSession();
            if (authSession != null) {
                // 区分登录失败与账户控制台关联失败
                if (isDoingAccountLinking(authSession, false, null)) {
                    Response accountManagementFailedLinking = redirectToErrorWhenLinkingFailed(authSession, Messages.STALE_CODE_ACCOUNT);
                    throw new WebApplicationException(accountManagementFailedLinking);
                } else {
                    Response errorResponse = checks.getResponse();

                    // 从浏览器历史中移除 code 参数
                    errorResponse = BrowserHistoryHelper.getInstance().saveResponseAndRedirect(session, authSession, errorResponse, true, request);
                    throw new WebApplicationException(errorResponse);
                }
            } else {
                throw new WebApplicationException(checks.getResponse());
            }
        } else {
            if (isDebugEnabled()) {
                logger.debugf("Authorization code is valid.");
            }

            return checks.getClientCode().getClientSession();
        }
    }

    /**
     * 若错误消息为 passive login 相关 OAuth 错误，则通过协议重定向回客户端 and if it does builds a response that
     * redirects the error back to the client.
     *
     * @param authSession the authentication session.
     * @param message the error message.
     * @return a {@code {@link Response}} that redirects the error message back to the client if the {@code message} is one
     * of the passive login error messages, or {@code null} if it is not.
     */
    /** {@inheritDoc} */
    private Response checkPassiveLoginError(AuthenticationSessionModel authSession, String message) {
        LoginProtocol.Error error = OAuthErrorException.LOGIN_REQUIRED.equals(message) ? LoginProtocol.Error.PASSIVE_LOGIN_REQUIRED :
                (OAuthErrorException.INTERACTION_REQUIRED.equals(message) ? LoginProtocol.Error.PASSIVE_INTERACTION_REQUIRED : null);
        if (error != null) {
            LoginProtocol protocol = session.getProvider(LoginProtocol.class, authSession.getProtocol());
            protocol.setRealm(realmModel)
                    .setHttpHeaders(headers)
                    .setUriInfo(session.getContext().getUri())
                    .setEventBuilder(event);
            return protocol.sendError(authSession, error, null);
        }
        return null;
    }

    /** 构建发往 IdP 的 AuthenticationRequest（含 relay state） */
    private AuthenticationRequest createAuthenticationRequest(UserAuthenticationIdentityProvider<?> identityProvider, String providerAlias, ClientSessionCode<AuthenticationSessionModel> clientSessionCode) {
        AuthenticationSessionModel authSession = null;
        IdentityBrokerState encodedState = null;

        if (clientSessionCode != null) {
            authSession = clientSessionCode.getClientSession();
            String relayState = clientSessionCode.getOrGenerateCode();
            String clientData = identityProvider.supportsLongStateParameter() ? AuthenticationProcessor.getClientData(session, authSession) : null;
            encodedState = IdentityBrokerState.decoded(relayState, authSession.getClient().getId(), authSession.getClient().getClientId(), authSession.getTabId(), clientData);
        }

        return new AuthenticationRequest(this.session, this.realmModel, authSession, this.request, this.session.getContext().getUri(), encodedState, getRedirectUri(providerAlias));
    }

    /** 构建 IdP 认证响应回调 URI */
    private String getRedirectUri(String providerAlias) {
        return Urls.identityProviderAuthnResponse(this.session.getContext().getUri().getBaseUri(), providerAlias, this.realmModel.getName()).toString();
    }

    /** 重定向到错误页（无 throwable） */
    private Response redirectToErrorPage(AuthenticationSessionModel authSession, Response.Status status, String message, Object ... parameters) {
        return redirectToErrorPage(authSession, status, message, null, parameters);
    }

    /** 重定向到错误页（无 authSession） */
    private Response redirectToErrorPage(Response.Status status, String message, Object ... parameters) {
        return redirectToErrorPage(null, status, message, null, parameters);
    }

    /** 重定向到错误页（带 throwable，无 authSession） */
    private Response redirectToErrorPage(Response.Status status, String message, Throwable throwable, Object ... parameters) {
        return redirectToErrorPage(null, status, message, throwable, parameters);
    }

    /** 记录错误事件并抛出 {@link ErrorPageException} 或返回 WebApplicationException 响应 */
    private Response redirectToErrorPage(AuthenticationSessionModel authSession, Response.Status status, String message, Throwable throwable, Object ... parameters) {
        if (message == null) {
            message = Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR;
        }

        fireErrorEvent(message, throwable);

        if (throwable instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) throwable;
            return webEx.getResponse();
        }

        throw new ErrorPageException(this.session, authSession, status, message, parameters);
    }


    /** 在浏览器认证流程中展示 IdP 错误消息 */
    protected Response browserAuthentication(AuthenticationSessionModel authSession, String errorMessage, Object... parameters) {
        this.event.event(EventType.LOGIN);
        AuthenticationFlowModel flow = AuthenticationFlowResolver.resolveBrowserFlow(authSession);
        String flowId = flow.getId();
        AuthenticationProcessor processor = new AuthenticationProcessor();
        processor.setAuthenticationSession(authSession)
                .setFlowPath(LoginActionsService.AUTHENTICATE_PATH)
                .setFlowId(flowId)
                .setBrowserFlow(true)
                .setConnection(clientConnection)
                .setEventBuilder(event)
                .setRealm(realmModel)
                .setSession(session)
                .setUriInfo(session.getContext().getUri())
                .setRequest(request);
        if (errorMessage != null) processor.setForwardedErrorMessage(new FormMessage(null, errorMessage, parameters));

        try {
            CacheControlUtil.noBackButtonCacheControlHeader(session);
            return processor.authenticate();
        } catch (Exception e) {
            return processor.handleBrowserException(e);
        }
    }


    /** 抛出 400 错误响应 */
    private Response badRequest(String message) {
        fireErrorEvent(message);
        throw ErrorResponse.error(message, Response.Status.BAD_REQUEST);
    }

    /** 抛出 403 错误响应 */
    private Response forbidden(String message) {
        fireErrorEvent(message);
        throw ErrorResponse.error(message, Response.Status.FORBIDDEN);
    }

    /** 抛出 404 错误响应 */
    private Response notFound(String message) {
        fireErrorEvent(message);
        throw ErrorResponse.error(message, Response.Status.NOT_FOUND);
    }

    /** 按别名获取 UserAuthenticationIdentityProvider 实例 */
    public static UserAuthenticationIdentityProvider<?> getIdentityProvider(KeycloakSession session, String alias) {
        IdentityProviderModel identityProviderModel = getIdentityProviderModel(session, alias);
        UserAuthenticationIdentityProvider<?> identityProvider = getIdentityProvider(session, identityProviderModel, UserAuthenticationIdentityProvider.class);
        if (identityProvider == null) {
            throw new IdentityBrokerException("Identity Provider [" + alias + "] not found.");
        }
        return identityProvider;
    }

    /** 按配置与类型获取 IdP 实例 */
    public static <T extends IdentityProvider<?>> T getIdentityProvider(KeycloakSession session, IdentityProviderModel identityProviderModel, Class<T> type) {
        if (identityProviderModel != null) {
            IdentityProviderFactory<?> providerFactory = getIdentityProviderFactory(session, identityProviderModel);
            if (providerFactory != null) {
                IdentityProvider<?> idp = providerFactory.create(session, identityProviderModel);
                return type.isInstance(idp) ? type.cast(idp) : null;
            }
        }
        return null;
    }

    /** 解析 IdP 工厂（含 SocialIdentityProvider） */
    private static IdentityProviderFactory<?> getIdentityProviderFactory(KeycloakSession session, IdentityProviderModel model) {
        if (model == null) {
            return null;
        }

        return Stream.concat(session.getKeycloakSessionFactory().getProviderFactoriesStream(IdentityProvider.class),
                session.getKeycloakSessionFactory().getProviderFactoriesStream(SocialIdentityProvider.class))
                .filter(providerFactory -> Objects.equals(providerFactory.getId(), model.getProviderId()))
                .map(IdentityProviderFactory.class::cast)
                .findFirst()
                .orElse(null);
    }

    /** 获取 IdP 配置，不存在时抛异常 */
    private IdentityProviderModel getIdentityProviderConfig(String providerAlias) {
        IdentityProviderModel model = getIdentityProviderModel(session, providerAlias);
        if (model == null) {
            throw new IdentityBrokerException("Configuration for identity provider [" + providerAlias + "] not found.");
        }
        return model;
    }

    /** 为响应附加 CORS 头 */
    private Response corsResponse(Response response, ClientModel clientModel) {
        return Cors.builder().auth().checkAllowedOrigins(session, clientModel).add(Response.fromResponse(response));
    }

    /** 记录身份代理错误事件与日志 */
    private void fireErrorEvent(String message, Throwable throwable) {
        if (!this.event.getEvent().getType().toString().endsWith("_ERROR")) {
            boolean newTransaction = !this.session.getTransactionManager().isActive();

            try {
                if (newTransaction) {
                    this.session.getTransactionManager().begin();
                }

                this.event.error(message);

                if (newTransaction) {
                    this.session.getTransactionManager().commit();
                }
            } catch (Exception e) {
                ServicesLogger.LOGGER.couldNotFireEvent(e);
                rollback();
            }
        }

        if (throwable != null) {
            logger.error(message, throwable);
        } else {
            logger.error(message);
        }
    }

    /** 记录错误事件（无 throwable） */
    private void fireErrorEvent(String message) {
        fireErrorEvent(message, null);
    }

    /** @return 是否启用 DEBUG 日志 */
    private boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /** 回滚活动事务 */
    private void rollback() {
        if (this.session.getTransactionManager().isActive()) {
            this.session.getTransactionManager().rollback();
        }
    }

}
