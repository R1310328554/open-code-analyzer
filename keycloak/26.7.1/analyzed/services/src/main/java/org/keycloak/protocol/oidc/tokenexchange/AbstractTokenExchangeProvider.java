/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.protocol.oidc.tokenexchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ExchangeExternalToken;
import org.keycloak.broker.provider.ExchangeTokenToIdentityProviderToken;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.broker.provider.IdentityProviderMapperSyncModeDelegate;
import org.keycloak.broker.provider.UserAuthenticationIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.light.LightweightUserAdapter;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenExchangeContext;
import org.keycloak.protocol.oidc.TokenExchangeProvider;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.Urls;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.BruteForceProtector;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.services.resources.IdentityBrokerService;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.Booleans;

import org.jboss.logging.Logger;

import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;
import static org.keycloak.models.IdentityProviderType.EXCHANGE_EXTERNAL_TOKEN;

/**
 * 令牌交换抽象基类。
 * <p>为 V1 与 V2（标准 RFC 8693）令牌交换提供公共逻辑：外部-内部交换、客户端间交换、受众校验及外部身份导入等。</p>
 *
 * @author <a href="mailto:dmitryt@backbase.com">Dmitry Telegin</a>
 */
public abstract class AbstractTokenExchangeProvider implements TokenExchangeProvider {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(AbstractTokenExchangeProvider.class);

    /** 令牌交换请求参数 */
    protected TokenExchangeContext.Params params;
    /** 表单参数 */
    protected MultivaluedMap<String, String> formParams;
    /** Keycloak 会话 */
    protected KeycloakSession session;
    /** CORS 处理器 */
    protected Cors cors;
    /** 当前领域 */
    protected RealmModel realm;
    /** 请求客户端 */
    protected ClientModel client;
    /** 事件构建器 */
    protected EventBuilder event;
    /** 客户端连接信息 */
    protected ClientConnection clientConnection;
    /** HTTP 请求头 */
    protected HttpHeaders headers;
    /** 令牌管理器 */
    protected TokenManager tokenManager;
    /** 客户端认证属性（写入用户会话备注） */
    protected Map<String, String> clientAuthAttributes;
    /** 令牌交换上下文 */
    protected TokenExchangeContext context;

    /** 入口：从上下文填充字段并执行 {@link #tokenExchange()} @param context 令牌交换上下文 @return HTTP 响应 */
    @Override
    public Response exchange(TokenExchangeContext context) {
        this.params = context.getParams();
        this.formParams = context.getFormParams();
        this.session = context.getSession();
        this.cors = context.getCors();
        this.realm = context.getRealm();
        this.client = context.getClient();
        this.event = context.getEvent();
        this.clientConnection = context.getClientConnection();
        this.headers = context.getHeaders();
        this.tokenManager = (TokenManager)context.getTokenManager();
        this.clientAuthAttributes = context.getClientAuthAttributes();
        this.context = context;
        return tokenExchange();
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {
    }

    /** 子类实现的令牌交换核心逻辑 @return HTTP 响应 */
    protected abstract Response tokenExchange();

    /**
     * 是否为外部-内部令牌交换请求（subject_token 发行者与当前领域不同）。
     * @param context 令牌交换上下文
     * @return 是外部-内部交换时返回 true
     */
    protected boolean isExternalInternalTokenExchangeRequest(TokenExchangeContext context) {
        String subjectToken = context.getParams().getSubjectToken();
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        EventBuilder event = context.getEvent();

        if (subjectToken != null) {
            String subjectTokenType = context.getParams().getSubjectTokenType();
            String realmIssuerUrl = Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName());
            String subjectIssuer = getSubjectIssuer(context, subjectToken, subjectTokenType);

            if (subjectIssuer != null && !realmIssuerUrl.equals(subjectIssuer)) {
                event.detail(OAuth2Constants.SUBJECT_ISSUER, subjectIssuer);
                return true;
            }
        }
        return false;
    }

    /** 解析 subject_token 发行者（表单参数或 JWT iss） @return 发行者 URL，无法解析时 null */
    protected String getSubjectIssuer(TokenExchangeContext context, String subjectToken, String subjectTokenType) {
        String subjectIssuer = context.getFormParams().getFirst(OAuth2Constants.SUBJECT_ISSUER);
        if (subjectIssuer != null) return subjectIssuer;

        if (OAuth2Constants.JWT_TOKEN_TYPE.equals(subjectTokenType)) {
            try {
                JWSInput jws = new JWSInput(subjectToken);
                JsonWebToken jwt = jws.readJsonContent(JsonWebToken.class);
                return jwt.getIssuer();
            } catch (JWSInputException e) {
                context.getEvent().detail(Details.REASON, "unable to parse jwt subject_token");
                context.getEvent().error(Errors.INVALID_TOKEN);
                throw new CorsErrorResponseException(context.getCors(), OAuthErrorException.INVALID_REQUEST, "Invalid token type, must be access token", Response.Status.BAD_REQUEST);
            }
        } else {
            return null;
        }
    }

    /** 向身份提供方交换令牌 @param requestedIssuer 目标 IdP 别名 @return IdP 返回的令牌响应 */
    protected Response exchangeToIdentityProvider(UserModel targetUser, UserSessionModel targetUserSession, String requestedIssuer) {
        event.detail(Details.REQUESTED_ISSUER, requestedIssuer);
        IdentityProviderModel providerModel = session.identityProviders().getByAlias(requestedIssuer);
        if (providerModel == null) {
            event.detail(Details.REASON, "unknown requested_issuer");
            event.error(Errors.UNKNOWN_IDENTITY_PROVIDER);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Invalid issuer", Response.Status.BAD_REQUEST);
        }

        IdentityProvider<?> provider = IdentityBrokerService.getIdentityProvider(session, requestedIssuer);
        if (!(provider instanceof ExchangeTokenToIdentityProviderToken)) {
            event.detail(Details.REASON, "exchange unsupported by requested_issuer");
            event.error(Errors.UNKNOWN_IDENTITY_PROVIDER);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Issuer does not support token exchange", Response.Status.BAD_REQUEST);
        }
        if (!AdminPermissions.management(session, realm).idps().canExchangeTo(client, providerModel)) {
            event.detail(Details.REASON, "client not allowed to exchange for requested_issuer");
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client not allowed to exchange", Response.Status.FORBIDDEN);
        }
        Response response = ((ExchangeTokenToIdentityProviderToken)provider).exchangeFromToken(session.getContext().getUri(), event, client, targetUserSession, targetUser, formParams);
        return cors.add(Response.fromResponse(response));

    }

    /** @return 请求的响应令牌类型 */
    protected abstract String getRequestedTokenType();

    /** 解析 audience 参数；未指定时默认为请求客户端自身 @return 目标受众客户端列表 */
    protected List<ClientModel> getTargetAudienceClients() {
        List<String> audienceParams = params.getAudience();
        List<ClientModel> targetAudienceClients = new ArrayList<>();
        if (audienceParams != null) {
            for (String audience : audienceParams) {
                ClientModel targetClient = realm.getClientByClientId(audience);
                if (targetClient == null) {
                    event.detail(Details.REASON, "audience not found");
                    event.detail(Details.AUDIENCE, audience);
                    event.error(Errors.CLIENT_NOT_FOUND);
                    throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, "Audience not found", Response.Status.BAD_REQUEST);
                } else {
                    targetAudienceClients.add(targetClient);
                }
            }
        }
        // 未提供 audience 时默认请求客户端自身为受众
        if (targetAudienceClients.isEmpty()) {
            targetAudienceClients.add(client);
        }
        return targetAudienceClients;
    }

    /** 校验受众与客户端权限 @param disallowOnHolderOfTokenMismatch 是否禁止非令牌持有者交换 */
    protected abstract void validateAudience(AccessToken token, boolean disallowOnHolderOfTokenMismatch, List<ClientModel> targetAudienceClients);

    /**
     * 客户端间令牌交换主流程。
     * @param targetUser 目标用户
     * @param targetUserSession 目标用户会话
     * @param token subject 访问令牌（可为 null）
     * @param disallowOnHolderOfTokenMismatch 是否禁止非持有者交换
     * @return OIDC 或 SAML2 令牌响应
     */
    protected Response exchangeClientToClient(UserModel targetUser, UserSessionModel targetUserSession,
            AccessToken token, boolean disallowOnHolderOfTokenMismatch) {

        String requestedTokenType = getRequestedTokenType();
        event.detail(Details.REQUESTED_TOKEN_TYPE, requestedTokenType);
        List<ClientModel> targetAudienceClients = getTargetAudienceClients();
        validateAudience(token, disallowOnHolderOfTokenMismatch, targetAudienceClients);
        String scope = getRequestedScope(token, targetAudienceClients);

        try {
            setClientToContext(targetAudienceClients);
            if (getSupportedOAuthResponseTokenTypes().contains(requestedTokenType))
                return exchangeClientToOIDCClient(targetUser, targetUserSession, requestedTokenType, targetAudienceClients, scope, token);
            else if (OAuth2Constants.SAML2_TOKEN_TYPE.equals(requestedTokenType)) {
                return exchangeClientToSAML2Client(targetUser, targetUserSession, requestedTokenType, targetAudienceClients);
            }
        } finally {
            session.getContext().setClient(client);
        }

        throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "requested_token_type unsupported", Response.Status.BAD_REQUEST);
    }

    /** 若请求客户端不在令牌受众内则抛出 403 @param token subject 令牌 */
    protected void forbiddenIfClientIsNotWithinTokenAudience(AccessToken token) {
        if (token != null && !token.hasAudience(client.getClientId())) {
            event.detail(Details.REASON, "client is not within the token audience");
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client is not within the token audience", Response.Status.FORBIDDEN);
        }
    }

    /** 若请求客户端非令牌持有者且策略禁止则抛出 403 */
    protected void forbiddenIfClientIsNotTokenHolder(boolean disallowOnHolderOfTokenMismatch, ClientModel tokenHolder) {
        if (disallowOnHolderOfTokenMismatch && !client.equals(tokenHolder)) {
            event.detail(Details.REASON, "client is not the token holder");
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client is not the holder of the token", Response.Status.FORBIDDEN);
        }
    }

    /** @return 支持的 OAuth 响应令牌类型列表 */
    protected abstract List<String> getSupportedOAuthResponseTokenTypes();

    /** 创建认证会话并绑定用户、协议与 scope @return 认证会话模型 */
    protected AuthenticationSessionModel createSessionModel(UserSessionModel targetUserSession, RootAuthenticationSessionModel rootAuthSession, UserModel targetUser, ClientModel client, String scope) {
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);
        authSession.setAuthenticatedUser(targetUser);
        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
        authSession.setClientNote(OIDCLoginProtocol.SCOPE_PARAM, scope);
        return authSession;
    }

    /** @return 交换后请求的 scope 字符串 */
    protected abstract String getRequestedScope(AccessToken token, List<ClientModel> targetAudienceClients);

    /** 将请求客户端设置到会话上下文 @param targetAudienceClients 目标受众（V1 可能取首个） */
    protected void setClientToContext(List<ClientModel> targetAudienceClients) {
        // 将发起交换的请求客户端写入上下文
        session.getContext().setClient(client);
    }

    /** 交换为 OIDC 访问/刷新/ID 令牌 @return JSON 令牌响应 */
    protected abstract Response exchangeClientToOIDCClient(UserModel targetUser, UserSessionModel targetUserSession, String requestedTokenType,
                                                  List<ClientModel> targetAudienceClients, String scope, AccessToken subjectToken);

    /** 交换为 SAML 2.0 断言 @return Base64 编码的 SAML 响应 */
    protected abstract Response exchangeClientToSAML2Client(UserModel targetUser, UserSessionModel targetUserSession, String requestedTokenType, List<ClientModel> targetAudienceClients);

    /** 外部-内部令牌交换：通过 IdP 验证外部令牌并导入用户 @return 交换后的 OIDC 令牌 */
    protected Response exchangeExternalToken(String subjectIssuer, String subjectToken) {
        // 按别名或 subject_issuer 查找支持外部交换的 IdP
        ExternalExchangeContext externalExchangeContext = this.locateExchangeExternalTokenByAlias(subjectIssuer);

        if (externalExchangeContext == null) {
            event.error(Errors.INVALID_ISSUER);
            throw new CorsErrorResponseException(cors, Errors.INVALID_ISSUER, "Invalid " + OAuth2Constants.SUBJECT_ISSUER + " parameter", Response.Status.BAD_REQUEST);
        }
        if (!AdminPermissions.management(session, realm).idps().canExchangeTo(client, externalExchangeContext.idpModel())) {
            event.detail(Details.REASON, "client not allowed to exchange subject_issuer");
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client not allowed to exchange", Response.Status.FORBIDDEN);
        }
        BrokeredIdentityContext context = externalExchangeContext.provider().exchangeExternal(this, this.context);
        if (context == null) {
            event.error(Errors.INVALID_ISSUER);
            throw new CorsErrorResponseException(cors, Errors.INVALID_ISSUER, "Invalid " + OAuth2Constants.SUBJECT_ISSUER + " parameter", Response.Status.BAD_REQUEST);
        }

        UserModel user = importUserFromExternalIdentity(context);

        UserSessionModel userSession = new UserSessionManager(session).createUserSession(realm, user, user.getUsername(), clientConnection.getRemoteHost(), "external-exchange", false, null, null);
        externalExchangeContext.provider().exchangeExternalComplete(userSession, context, formParams);

        // 写入外部 IdP 备注，以便 IdP 未存储令牌时仍可从会话获取访问令牌
        userSession.setNote(UserAuthenticationIdentityProvider.EXTERNAL_IDENTITY_PROVIDER, externalExchangeContext.idpModel().getAlias());
        userSession.setNote(UserAuthenticationIdentityProvider.FEDERATED_ACCESS_TOKEN, subjectToken);

        context.addSessionNotesToUserSession(userSession);

        return exchangeClientToClient(user, userSession, null, false);
    }

    /** 从外部身份上下文导入或更新联邦用户 @return 领域用户模型 */
    protected UserModel importUserFromExternalIdentity(BrokeredIdentityContext context) {
        IdentityProviderModel identityProviderConfig = context.getIdpConfig();

        String providerId = identityProviderConfig.getAlias();

        context.getIdp().preprocessFederatedIdentity(session, realm, context);
        Set<IdentityProviderMapperModel> mappers = session.identityProviders().getMappersByAliasStream(context.getIdpConfig().getAlias())
                .collect(Collectors.toSet());
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        for (IdentityProviderMapperModel mapper : mappers) {
            IdentityProviderMapper target = (IdentityProviderMapper)sessionFactory.getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
            target.preprocessFederatedIdentity(session, realm, mapper, context);
        }

        UserModel user = null;
        if (!context.getIdpConfig().isTransientUsers()) {
            FederatedIdentityModel federatedIdentityModel = new FederatedIdentityModel(providerId, context.getId(),
                    context.getUsername(), context.getToken());

            user = this.session.users().getUserByFederatedIdentity(realm, federatedIdentityModel);
        }

        if (user == null || context.getIdpConfig().isTransientUsers()) {

            logger.debugf("Federated user not found for provider '%s' and broker username '%s'.", providerId, context.getUsername());

            String username = context.getModelUsername();
            if (username == null) {
                if (this.realm.isRegistrationEmailAsUsername() && !Validation.isBlank(context.getEmail())) {
                    username = context.getEmail();
                } else if (context.getUsername() == null) {
                    username = context.getIdpConfig().getAlias() + "." + context.getId();
                } else {
                    username = context.getUsername();
                }
            }
            username = username.trim();
            context.setModelUsername(username);
            if (context.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
                UserModel existingUser = session.users().getUserByEmail(realm, context.getEmail());
                if (existingUser != null) {
                    event.error(Errors.FEDERATED_IDENTITY_EXISTS);
                    throw new CorsErrorResponseException(cors, Errors.INVALID_TOKEN, "User already exists", Response.Status.BAD_REQUEST);
                }
            }

            UserModel existingUser = session.users().getUserByUsername(realm, username);
            if (existingUser != null) {
                event.error(Errors.FEDERATED_IDENTITY_EXISTS);
                throw new CorsErrorResponseException(cors, Errors.INVALID_TOKEN, "User already exists", Response.Status.BAD_REQUEST);
            }

            if (context.getIdpConfig().isTransientUsers()) {
                String authSessionId = context.getAuthenticationSession() != null && context.getAuthenticationSession().getParentSession() != null
                                       ? context.getAuthenticationSession().getParentSession().getId() : null;
                user = new LightweightUserAdapter(session, realm, authSessionId);
            } else {
                user = session.users().addUser(realm, username);
            }
            user.setEnabled(true);
            user.setEmail(context.getEmail());
            user.setFirstName(context.getFirstName());
            user.setLastName(context.getLastName());


            if (! context.getIdpConfig().isTransientUsers()) {
                FederatedIdentityModel federatedIdentityModel = new FederatedIdentityModel(context.getIdpConfig().getAlias(), context.getId(),
                        context.getModelUsername(), context.getToken());
                session.users().addFederatedIdentity(realm, user, federatedIdentityModel);
            }

            context.getIdp().importNewUser(session, realm, user, context);

            for (IdentityProviderMapperModel mapper : mappers) {
                IdentityProviderMapper target = (IdentityProviderMapper)sessionFactory.getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
                target.importNewUser(session, realm, user, mapper, context);
            }

            if (Booleans.isTrue(context.getIdpConfig().isTrustEmail()) && !Validation.isBlank(user.getEmail())) {
                logger.debugf("Email verified automatically after registration of user '%s' through Identity provider '%s' ", user.getUsername(), context.getIdpConfig().getAlias());
                user.setEmailVerified(true);
            }

            event.clone()
                    .event(EventType.REGISTER)
                    .user(user.getId())
                    .detail(Details.REGISTER_METHOD, "token-exchange")
                    .detail(Details.EMAIL, user.getEmail())
                    .detail(Details.IDENTITY_PROVIDER, providerId)
                    .success();
        } else {
            if (!user.isEnabled()) {
                event.error(Errors.USER_DISABLED);
                throw new CorsErrorResponseException(cors, Errors.INVALID_TOKEN, "Invalid Token", Response.Status.BAD_REQUEST);
            }

            String bruteForceError = getDisabledByBruteForceEventError(session.getProvider(BruteForceProtector.class), session, realm, user);
            if (bruteForceError != null) {
                event.error(bruteForceError);
                throw new CorsErrorResponseException(cors, Errors.INVALID_TOKEN, "Invalid Token", Response.Status.BAD_REQUEST);
            }

            context.getIdp().updateBrokeredUser(session, realm, user, context);

            for (IdentityProviderMapperModel mapper : mappers) {
                IdentityProviderMapper target = (IdentityProviderMapper)sessionFactory.getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
                IdentityProviderMapperSyncModeDelegate.delegateUpdateBrokeredUser(session, realm, user, mapper, context, target);
            }
        }

        // 按上下文属性更新用户模型属性
        for (Map.Entry<String, List<String>> attr : context.getAttributes().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (!UserModel.USERNAME.equalsIgnoreCase(attr.getKey())) {
                user.setAttribute(attr.getKey(), attr.getValue());
            }
        }

        return user;
    }

    // TODO：可移至工具类
    /** 将客户端认证属性写入用户会话备注 @param userSession 用户会话 */
    protected void updateUserSessionFromClientAuth(UserSessionModel userSession) {
        for (Map.Entry<String, String> attr : clientAuthAttributes.entrySet()) {
            userSession.setNote(attr.getKey(), attr.getValue());
        }
    }

    /** 外部交换上下文：IdP 提供方与模型 */
    protected record ExternalExchangeContext (ExchangeExternalToken provider, IdentityProviderModel idpModel) {};

    /** 按别名或发行者匹配查找支持 {@link ExchangeExternalToken} 的 IdP @return 匹配上下文或 null */
    protected ExternalExchangeContext locateExchangeExternalTokenByAlias(String alias) {
        try {
            IdentityProvider<?> idp = IdentityBrokerService.getIdentityProvider(session, alias);

            if (idp instanceof ExchangeExternalToken external) {
                IdentityProviderModel model = session.identityProviders().getByAlias(alias);
                return new ExternalExchangeContext(external, model);
            }
        } catch (IdentityBrokerException ignore) {
        }

        return session.identityProviders().getAllStream(IdentityProviderQuery.type(EXCHANGE_EXTERNAL_TOKEN)).map(idpModel -> {
            IdentityProvider<?> idp = IdentityBrokerService.getIdentityProvider(session, idpModel.getAlias());

            if (idp instanceof ExchangeExternalToken external && external.isIssuer(alias, formParams)) {
                return new ExternalExchangeContext(external, idpModel);
            }

            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }

}
