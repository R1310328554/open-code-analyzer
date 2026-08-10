/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.protocol.oidc;

import java.util.Arrays;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.CryptoUtils;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.ImpersonationSessionNote;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import org.keycloak.services.util.DefaultClientSessionContext;
import org.keycloak.services.util.UserSessionUtil;
import org.keycloak.tracing.TracingAttributes;
import org.keycloak.tracing.TracingProvider;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.logging.Logger;

/**
 * 访问令牌（Access Token）自省（Introspection）提供者。
 * <p>验证令牌签名与生命周期，校验客户端、用户会话与受众，并返回 RFC 7662 风格的 active/claims JSON。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AccessTokenIntrospectionProvider<T extends AccessToken> implements TokenIntrospectionProvider {

    /** Keycloak 会话。 */
    protected final KeycloakSession session;
    /** 令牌管理器。 */
    protected final TokenManager tokenManager;
    /** 当前领域。 */
    protected final RealmModel realm;
    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(AccessTokenIntrospectionProvider.class);
    /** 当前自省事件构建器。 */
    protected EventBuilder eventBuilder;

    // 校验成功后填充的上下文
    /** 已验证的原始令牌。 */
    protected T token;

    /** 经协议映射器转换后的令牌视图。 */
    protected AccessToken transformedToken;
    /** 令牌所属客户端。 */
    protected ClientModel client;
    /** 关联用户会话。 */
    protected UserSessionModel userSession;
    /** 令牌主体用户。 */
    protected UserModel user;

    /** @param session Keycloak 会话 */
    public AccessTokenIntrospectionProvider(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.tokenManager = new TokenManager();
    }

    /**
     * 执行令牌自省并返回 JSON 响应。
     * @param tokenStr 待自省令牌字符串
     * @param eventBuilder 事件构建器
     * @return 含 active 及声明的 HTTP 响应
     */
    @Override
    public Response introspect(String tokenStr, EventBuilder eventBuilder) {
        this.eventBuilder = eventBuilder;

        try {
            ClientModel authenticatedClient = session.getContext().getClient();

            ObjectNode tokenMetadata;
            if (introspectionChecks(tokenStr)) {
                tokenMetadata = JsonSerialization.createObjectNode(transformedToken);
                tokenMetadata.put("client_id", transformedToken.getIssuedFor());

                String scope = transformedToken.getScope();
                if (scope != null && scope.trim().isEmpty()) {
                    tokenMetadata.remove("scope");
                }

                if (!tokenMetadata.has("username")) {
                    if (transformedToken.getPreferredUsername() != null) {
                        tokenMetadata.put("username", transformedToken.getPreferredUsername());
                    } else {
                        UserModel userModel = userSession.getUser();
                        if (userModel != null) {
                            tokenMetadata.put("username", userModel.getUsername());
                            eventBuilder.user(userModel);
                        }
                    }
                }

                String actor = userSession.getNote(ImpersonationSessionNote.IMPERSONATOR_USERNAME.toString());
                if (actor != null) {
                    // 令牌交换委托语义：记录实际行使权限的 actor（非 subject）
                    tokenMetadata.putObject("act").put("sub", actor);
                }

                tokenMetadata.put(OAuth2Constants.TOKEN_TYPE, transformedToken.getType());
                tokenMetadata.put("active", true);
                eventBuilder.success();
            } else {
                tokenMetadata = JsonSerialization.createObjectNode();
                logger.debug("Keycloak token introspection return false");
                tokenMetadata.put("active", false);
            }

            // 若 Accept 为 application/jwt 且客户端启用，则在响应中附加 jwt 字段
            if (transformedToken != null) {
                boolean isJwtRequest = org.keycloak.utils.MediaType.APPLICATION_JWT.equals(session.getContext().getRequestHeaders().getHeaderString(HttpHeaders.ACCEPT));
                if (isJwtRequest && Boolean.parseBoolean(authenticatedClient.getAttribute(Constants.SUPPORT_JWT_CLAIM_IN_INTROSPECTION_RESPONSE_ENABLED))) {
                    // 供调用方将不透明令牌转为 JWT 形式
                    tokenMetadata.put("jwt", session.tokens().encode(transformedToken));
                }
            }

            return Response.ok(JsonSerialization.writeValueAsBytes(tokenMetadata)).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            String clientId = transformedToken != null ? transformedToken.getIssuedFor() : "unknown";
            logger.debugf(e, "Exception during Keycloak introspection for %s client in realm %s", clientId, realm.getName());
            eventBuilder.detail(Details.REASON, e.getMessage());
            eventBuilder.error(Errors.TOKEN_INTROSPECTION_FAILED);
            throw new RuntimeException("Error creating token introspection response.", e);
        }
    }


    /**
     * 按客户端会话与 scope 对访问令牌做自省专用转换。
     * @param token 原始令牌
     * @param userSession 用户会话
     * @return 转换后的令牌
     */
    public AccessToken transformAccessToken(AccessToken token, UserSessionModel userSession) {
        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());
        if(clientSession == null) {
            return token;
        }

        ClientSessionContext clientSessionCtx = DefaultClientSessionContext.fromClientSessionAndScopeParameter(clientSession, token.getScope(), session);
        AccessToken smallToken = getAccessTokenFromStoredData(token);
        return tokenManager.transformIntrospectionAccessToken(session, token, smallToken, userSession, clientSessionCtx);
    }

    private AccessToken getAccessTokenFromStoredData(AccessToken token) {
        // 仅复制基础声明（同 TokenManager.initToken）；其余由协议映射器按需加入自省响应
        AccessToken newToken = new AccessToken();
        newToken.id(token.getId());
        newToken.type(token.getType());
        newToken.subject(token.getSubject());
        newToken.iat(token.getIat());
        newToken.exp(token.getExp());
        newToken.issuedFor(token.getIssuedFor());
        newToken.issuer(token.getIssuer());
        newToken.setNonce(token.getNonce());
        newToken.setScope(token.getScope());
        newToken.setSessionId(token.getSessionId());

        // 刷新令牌场景下 aud 亦为基础声明
        newToken.audience(token.getAudience());

        // cnf 不由协议映射器控制
        newToken.setConfirmation(token.getConfirmation());
        return newToken;
    }

    /**
     * 串联令牌、客户端、用户会话等自省校验；失败时已写入错误事件。
     * 全部通过时填充实例字段。
     * @return 全部校验通过为 true
     */
    protected boolean introspectionChecks(String tokenStr) {
        if (!verifyToken(tokenStr)) {
            return false;
        }
        if (!verifyClient()) {
            return false;
        }

        eventBuilder.session(this.token.getSessionId());
        UserSessionUtil.UserSessionValidationResult result = verifyUserSession();
        if (result.getError() != null) {
            logger.debugf( "Introspection access token for " + token.getIssuedFor() + " client: " + result.getError());
            eventBuilder.detail(Details.REASON,  "Introspection access token for " + token.getIssuedFor() + " client: " + result.getError());
            eventBuilder.error(result.getError());
            return false;
        } else {
            this.userSession = result.getUserSession();
        }

        this.user = userSession.getUser();
        eventBuilder.user(user);
        if (!TokenManager.isUserValid(session, realm, token, userSession.getUser())) {
            logger.debugf("Could not find valid user from user session " + userSession.getId());
            eventBuilder.detail(Details.REASON, "Could not find valid user from user session " + userSession.getId());
            eventBuilder.error(user == null ? Errors.USER_NOT_FOUND : Errors.USER_DISABLED);
            return false;
        }

        if (userSession.isOffline() && !UserSessionUtil.isOfflineAccessGranted(
                session, userSession.getAuthenticatedClientSessionByClient(client.getId()))) {
            logger.debugf("Offline session invalid because offline access not granted anymore");
            eventBuilder.detail(Details.REASON, "Offline session invalid because offline access not granted anymore");
            eventBuilder.error(Errors.SESSION_EXPIRED);
            return false;
        }


        if (!verifyTokenReuse()) {
            return false;
        }

        transformedToken = transformAccessToken(this.token, userSession);

        if (!verifyAudience()) {
            return false;
        }

        return true;
    }

    /** 验证 JWT 签名与基本有效性。 @param tokenStr 令牌字符串 @return 通过为 true */
    protected boolean verifyToken(String tokenStr) {
        try {
            TokenVerifier<T> verifier = TokenVerifier.create(tokenStr, getTokenClass())
                    .realmUrl(Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));

            SignatureVerifierContext verifierContext = CryptoUtils.getSignatureProvider(session, verifier.getHeader().getAlgorithm().name()).verifier(verifier.getHeader().getKeyId());
            verifier.verifierContext(verifierContext);

            this.token = verifier.verify().getToken();
            eventBuilder.detail(Details.TOKEN_ID, token.getId());
            eventBuilder.detail(Details.TOKEN_TYPE, token.getType());

            var tracing = session.getProvider(TracingProvider.class);
            var span = tracing.getCurrentSpan();
            if (span.isRecording()) {
                span.setAttribute(TracingAttributes.TOKEN_ISSUER, token.getIssuer());
                span.setAttribute(TracingAttributes.TOKEN_SID, token.getSessionId());
                span.setAttribute(TracingAttributes.TOKEN_ID, token.getId());
            }

            return true;
        } catch (VerificationException e) {
            logger.debugf("Introspection access token : JWT check failed: %s", e.getMessage());
            eventBuilder.detail(Details.REASON,"Access token JWT check failed");
            eventBuilder.error(Errors.INVALID_TOKEN);
            return false;
        }
    }


    /** @return 令牌类型 Class */
    protected Class<T> getTokenClass() {
        return (Class<T>) AccessToken.class;
    }

    /** 校验令牌 issued_for 对应客户端存在且启用。 @return 通过为 true */
    protected boolean verifyClient() {
        eventBuilder.detail(Details.TOKEN_ISSUED_FOR, token.getIssuedFor());
        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null) {
            logger.debugf("Introspection access token : client with clientId %s does not exist", token.getIssuedFor() );
            eventBuilder.detail(Details.REASON, String.format("Could not find client for %s", token.getIssuedFor()));
            eventBuilder.error(Errors.CLIENT_NOT_FOUND);
            return false;
        } else {
            if (!client.isEnabled()) {
                logger.debugf("Introspection access token : client with clientId %s is disabled", token.getIssuedFor() );
                eventBuilder.detail(Details.REASON, String.format("Client with clientId %s is disabled", token.getIssuedFor()));
                eventBuilder.error(Errors.CLIENT_DISABLED);
                return false;
            } else {

                try {
                    TokenVerifier.createWithoutSignature(token)
                            .withChecks(TokenManager.NotBeforeCheck.forModel(realm), TokenManager.NotBeforeCheck.forModel(client), TokenVerifier.IS_ACTIVE, new TokenManager.TokenRevocationCheck(session))
                            .verify();
                    this.client = client;
                    return true;
                } catch (VerificationException e) {
                    logger.debugf("Introspection access token for %s client: JWT check failed: %s", token.getIssuedFor(), e.getMessage());
                    eventBuilder.detail(Details.REASON, "Introspection access token for " + token.getIssuedFor() +" client: JWT check failed");
                    eventBuilder.error(Errors.INVALID_TOKEN);
                    return false;
                }
            }
        }
    }

    /** 校验自省客户端位于令牌 aud 中，或允许跳过受众检查。 @return 通过为 true */
    protected boolean verifyAudience() {
        ClientModel authenticatedClient = session.getContext().getClient();

        // 检查已认证客户端是否在令牌 aud（原始或转换后）中
        String[] audiences = token.getAudience() != null ? token.getAudience() : transformedToken.getAudience();
        if (audiences != null && Arrays.asList(audiences).contains(authenticatedClient.getClientId())) {
            return true;
        }

        // 读取 OIDC 服务端全局配置
        OIDCLoginProtocol loginProtocol = (OIDCLoginProtocol) session.getProvider(LoginProtocol.class, OIDCLoginProtocol.LOGIN_PROTOCOL);
        OIDCProviderConfig config = loginProtocol.getConfig();

        // 服务端是否允许跳过受众检查（兼容选项）
        if (config.isAllowTokenIntrospectionWithoutAudienceCheck()) {
            logger.warnf("Client '%s' introspecting token for '%s' without audience check (server-wide setting)",
                    authenticatedClient.getClientId(), token.getIssuedFor());
            return true;
        }

        // 自省客户端自身是否允许跳过受众检查
        OIDCAdvancedConfigWrapper clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(authenticatedClient);
        if (clientConfig.isAllowTokenIntrospectionWithoutAudienceCheck()) {
            logger.warnf("Client '%s' introspecting token for '%s' without audience check (per-client setting on '%s')",
                    authenticatedClient.getClientId(), token.getIssuedFor(), authenticatedClient.getClientId());
            return true;
        }

        logger.debugf("Introspection denied: client '%s' not in audience of token for '%s'",
                authenticatedClient.getClientId(), token.getIssuedFor());
        eventBuilder.detail(Details.REASON, String.format("Client '%s' is not in the token audience", authenticatedClient.getClientId()));
        eventBuilder.error(Errors.INVALID_TOKEN);
        return false;
    }

    /** @return 用户会话校验结果 */
    protected UserSessionUtil.UserSessionValidationResult verifyUserSession() {
        return UserSessionUtil.findValidSessionForAccessToken(session, realm, token, client, (invalidUserSession -> {}));
    }


    /** 子类可覆盖以检测令牌重用。 @return 默认 true */
    protected boolean verifyTokenReuse() {
        return true;
    }

    @Override
    public void close() {

    }
}
