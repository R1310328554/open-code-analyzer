/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Base64Url;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.TokenExchangeContext;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.endpoints.TokenEndpoint;
import org.keycloak.protocol.saml.SamlClient;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.protocol.saml.SamlService;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_CLIENT;
import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_ID;
import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_USERNAME;

/**
 * V1 令牌交换提供者。
 * <p>支持全部交换类型：域内标准交换、联邦（外部-内部/内部-外部）交换及 subject impersonation；默认 requested_token_type 为 refresh_token。</p>
 *
 * @author <a href="mailto:dmitryt@backbase.com">Dmitry Telegin</a>
 */
public class V1TokenExchangeProvider extends AbstractTokenExchangeProvider {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(V1TokenExchangeProvider.class);


    /** V1 作为兜底提供者，接受所有交换请求 @return 始终 true */
    @Override
    public boolean supports(TokenExchangeContext context) {
        return true;
    }

    /** V1 交换主流程：subject 校验、impersonation、IdP 交换或客户端间交换 @return HTTP 响应 */
    protected Response tokenExchange() {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        ClientConnection clientConnection = context.getClientConnection();
        Cors cors = context.getCors();
        ClientModel client = context.getClient();
        EventBuilder event = context.getEvent();

        UserModel tokenUser = null;
        UserSessionModel tokenSession = null;
        AccessToken token = null;

        String subjectToken = context.getParams().getSubjectToken();
        if (subjectToken != null) {
            String subjectTokenType = context.getParams().getSubjectTokenType();
            if (isExternalInternalTokenExchangeRequest(this.context)) {
                String subjectIssuer = getSubjectIssuer(this.context, subjectToken, subjectTokenType);
                return exchangeExternalToken(subjectIssuer, subjectToken);
            }

            if (subjectTokenType != null && !subjectTokenType.equals(OAuth2Constants.ACCESS_TOKEN_TYPE)) {
                event.detail(Details.REASON, "subject_token supports access tokens only");
                event.error(Errors.INVALID_TOKEN);
                throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Invalid token type, must be access token", Response.Status.BAD_REQUEST);

            }

            AuthenticationManager.AuthResult authResult = AuthenticationManager.verifyIdentityToken(session, realm, session.getContext().getUri(), clientConnection, true, true, null,
                    false, subjectToken, context.getHeaders(), verifier -> {});
            if (authResult == null) {
                event.detail(Details.REASON, "subject_token validation failure");
                event.error(Errors.INVALID_TOKEN);
                throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Invalid token", Response.Status.BAD_REQUEST);
            }

            tokenUser = authResult.user();
            tokenSession = authResult.session();
            token = authResult.token();
        }

        String requestedSubject = context.getFormParams().getFirst(OAuth2Constants.REQUESTED_SUBJECT);
        boolean disallowOnHolderOfTokenMismatch = true;

        if (requestedSubject != null) {
            event.detail(Details.REQUESTED_SUBJECT, requestedSubject);
            UserModel requestedUser = session.users().getUserByUsername(realm, requestedSubject);
            if (requestedUser == null) {
                requestedUser = session.users().getUserById(realm, requestedSubject);
            }

            if (requestedUser == null || !requestedUser.isEnabled()) {
                // 统一返回 access_denied，避免用户名枚举
                event.detail(Details.REASON, "requested_subject not found");
                event.error(Errors.NOT_ALLOWED);
                throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client not allowed to exchange", Response.Status.FORBIDDEN);

            }

            if (token != null) {
                event.detail(Details.IMPERSONATOR, tokenUser.getUsername());
                // subject 令牌代表的用户须具备模拟 requested_subject 的权限
                AdminAuth auth = new AdminAuth(realm, token, tokenUser, client);
                if (!AdminPermissions.evaluator(session, realm, auth).users().canImpersonate(requestedUser, client)) {
                    event.detail(Details.REASON, "subject not allowed to impersonate");
                    event.error(Errors.NOT_ALLOWED);
                    throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client not allowed to exchange", Response.Status.FORBIDDEN);
                }
            } else {
                // 无 subject 令牌时为直接交换：客户端须已认证、非公开且具备模拟权限
                if (client.isPublicClient()) {
                    event.detail(Details.REASON, "public clients not allowed");
                    event.error(Errors.NOT_ALLOWED);
                    throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client not allowed to exchange", Response.Status.FORBIDDEN);

                }
                if (!AdminPermissions.management(session, realm).users().canClientImpersonate(client, requestedUser)) {
                    event.detail(Details.REASON, "client not allowed to impersonate");
                    event.error(Errors.NOT_ALLOWED);
                    throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client not allowed to exchange", Response.Status.FORBIDDEN);
                }

                // 见 KEYCLOAK-5492：直接交换时不强制持有者匹配
                disallowOnHolderOfTokenMismatch = false;
            }

            tokenSession = new UserSessionManager(session).createUserSession(realm, requestedUser, requestedUser.getUsername(), clientConnection.getRemoteHost(), "impersonate", false, null, null);
            if (tokenUser != null) {
                tokenSession.setNote(IMPERSONATOR_ID.toString(), tokenUser.getId());
                tokenSession.setNote(IMPERSONATOR_USERNAME.toString(), tokenUser.getUsername());
            }

            tokenUser = requestedUser;
        }

        String requestedIssuer = context.getFormParams().getFirst(OAuth2Constants.REQUESTED_ISSUER);
        if (requestedIssuer == null) {
            return exchangeClientToClient(tokenUser, tokenSession, token, disallowOnHolderOfTokenMismatch);
        } else {
            try {
                return exchangeToIdentityProvider(tokenUser, tokenSession, requestedIssuer);
            } finally {
                if (subjectToken == null) { // 无 subject 令牌时需清理临时用户会话
                    try {
                        session.sessions().removeUserSession(realm, tokenSession);
                    } catch (Exception ignore) {

                    }
                }
            }
        }
    }

    /** V1 默认 refresh_token；支持 access_token、refresh_token、saml2 @return 令牌类型 */
    @Override
    protected String getRequestedTokenType() {
        String requestedTokenType = context.getParams().getRequestedTokenType();
        if (requestedTokenType == null) {
            requestedTokenType = OAuth2Constants.REFRESH_TOKEN_TYPE;
            return requestedTokenType;
        }
        if (requestedTokenType.equals(OAuth2Constants.ACCESS_TOKEN_TYPE)
                || requestedTokenType.equals(OAuth2Constants.REFRESH_TOKEN_TYPE)
                || requestedTokenType.equals(OAuth2Constants.SAML2_TOKEN_TYPE)) {
            return requestedTokenType;
        }

        event.detail(Details.REASON, "requested_token_type unsupported");
        event.error(Errors.INVALID_REQUEST);
        throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "requested_token_type unsupported", Response.Status.BAD_REQUEST);
    }

    /** V1 受众校验：公开/机密客户端规则、同意要求及 exchange 权限 @param disallowOnHolderOfTokenMismatch 是否禁止非持有者交换 */
    @Override
    protected void validateAudience(AccessToken token, boolean disallowOnHolderOfTokenMismatch, List<ClientModel> targetAudienceClients) {
        ClientModel tokenHolder = token == null ? null : realm.getClientByClientId(token.getIssuedFor());
        for (ClientModel targetClient : targetAudienceClients) {
            if (targetClient.isConsentRequired()) {
                event.detail(Details.REASON, "audience requires consent");
                event.detail(Details.AUDIENCE, targetClient.getClientId());
                event.error(Errors.CONSENT_DENIED);
                throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, "Client requires user consent", Response.Status.BAD_REQUEST);
            }
            if (!targetClient.isEnabled()) {
                event.detail(Details.REASON, "audience client disabled");
                event.detail(Details.AUDIENCE, targetClient.getClientId());
                event.error(Errors.CLIENT_DISABLED);
                throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, "Client disabled", Response.Status.BAD_REQUEST);
            }
            boolean isClientTheAudience = targetClient.equals(client);
            if (isClientTheAudience) {
                if (client.isPublicClient()) {
                    // 公开客户端仅在为令牌持有者时可交换给自身
                    forbiddenIfClientIsNotTokenHolder(disallowOnHolderOfTokenMismatch, tokenHolder);
                } else if (!client.equals(tokenHolder)) {
                    // 机密客户端交换给自身时须在令牌受众内
                    forbiddenIfClientIsNotWithinTokenAudience(token);
                }
            } else {
                if (client.isPublicClient()) {
                    // 公开客户端不能交换其他客户端的令牌
                    forbiddenIfClientIsNotTokenHolder(disallowOnHolderOfTokenMismatch, tokenHolder);
                }
                if (!AdminPermissions.management(session, realm).clients().canExchangeTo(client, targetClient, token)) {
                    event.detail(Details.REASON, "client not allowed to exchange to audience");
                    event.detail(Details.AUDIENCE, targetClient.getClientId());
                    event.error(Errors.NOT_ALLOWED);
                    throw new CorsErrorResponseException(cors, OAuthErrorException.ACCESS_DENIED, "Client not allowed to exchange", Response.Status.FORBIDDEN);
                }
            }
        }
    }

    /** @return 支持 access_token 与 refresh_token */
    @Override
    protected List<String> getSupportedOAuthResponseTokenTypes() {
        return Arrays.asList(OAuth2Constants.ACCESS_TOKEN_TYPE, OAuth2Constants.REFRESH_TOKEN_TYPE);
    }

    /** V1 scope 解析：继承 subject 范围并与目标客户端范围求交 @return scope 字符串 */
    @Override
    protected String getRequestedScope(AccessToken token, List<ClientModel> targetAudienceClients) {
        ClientModel targetClient = targetAudienceClients.get(0);
        // TODO：多 audience 完整支持后移除此限制
        if (targetAudienceClients.size() > 1) {
            logger.warnf("Only one value of audience parameter currently supported for token exchange. Using audience '%s' and ignoring the other audiences provided", targetClient.getClientId());
        }

        String scope = formParams.getFirst(OAuth2Constants.SCOPE);
        if (token != null && token.getScope() != null && scope == null) {
            scope = token.getScope();

            Set<String> targetClientScopes = new HashSet<String>();
            targetClientScopes.addAll(targetClient.getClientScopes(true).keySet());
            targetClientScopes.addAll(targetClient.getClientScopes(false).keySet());
            // 从返回 scope 中移除非目标客户端默认/可选范围的项
            scope = Arrays.stream(scope.split(" ")).filter(s -> "openid".equals(s) || (targetClientScopes.contains(Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES) ? s.split(":")[0] : s))).collect(Collectors.joining(" "));
        } else if (token != null && token.getScope() != null) {
            String subjectTokenScopes = token.getScope();
            if (Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES)) {
                Set<String> subjectTokenScopesSet = Arrays.stream(subjectTokenScopes.split(" ")).map(s -> s.split(":")[0]).collect(Collectors.toSet());
                scope = Arrays.stream(scope.split(" ")).filter(sc -> subjectTokenScopesSet.contains(sc.split(":")[0])).collect(Collectors.joining(" "));
            } else {
                Set<String> subjectTokenScopesSet = Arrays.stream(subjectTokenScopes.split(" ")).collect(Collectors.toSet());
                scope = Arrays.stream(scope.split(" ")).filter(sc -> subjectTokenScopesSet.contains(sc)).collect(Collectors.joining(" "));
            }

            Set<String> targetClientScopes = new HashSet<String>();
            targetClientScopes.addAll(targetClient.getClientScopes(true).keySet());
            targetClientScopes.addAll(targetClient.getClientScopes(false).keySet());
            //from return scope remove scopes that are not default or optional scopes for targetClient
            scope = Arrays.stream(scope.split(" ")).filter(s -> "openid".equals(s) || (targetClientScopes.contains(Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES) ? s.split(":")[0] : s))).collect(Collectors.joining(" "));
        }
        return scope;
    }

    /** V1 将首个目标受众客户端写入上下文 */
    @Override
    protected void setClientToContext(List<ClientModel> targetAudienceClients) {
        ClientModel targetClient = getTargetClient(targetAudienceClients);
        session.getContext().setClient(targetClient);
    }

    /** V1 仅考虑首个 audience 客户端 @return 目标客户端 */
    protected ClientModel getTargetClient(List<ClientModel> targetAudienceClients) {
        // V1 仅使用 audience 列表中的第一个客户端
        return targetAudienceClients.get(0);
    }

    /** V1 OIDC 交换：为 target 与 requester 创建客户端会话并签发令牌 @return JSON 响应 */
    @Override
    protected Response exchangeClientToOIDCClient(UserModel targetUser, UserSessionModel targetUserSession, String requestedTokenType,
                                                  List<ClientModel> targetAudienceClients, String scope, AccessToken subjectToken) {
        ClientModel targetClient = getTargetClient(targetAudienceClients);
        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, false);
        AuthenticationSessionModel authSession = createSessionModel(targetUserSession, rootAuthSession, targetUser, targetClient, scope);

        AuthenticationManager.setClientScopesInSession(session, authSession);
        ClientSessionContext clientSessionCtx = TokenManager.attachAuthenticationSession(this.session, targetUserSession, authSession);

        if (!AuthenticationManager.isClientSessionValid(realm, client, targetUserSession, targetUserSession.getAuthenticatedClientSessionByClient(client.getId()))) {
            // 必要时为请求客户端创建客户端会话
            AuthenticationSessionModel clientAuthSession = createSessionModel(targetUserSession, rootAuthSession, targetUser, client, scope);
            TokenManager.attachAuthenticationSession(this.session, targetUserSession, clientAuthSession);
        }

        updateUserSessionFromClientAuth(targetUserSession);

        TokenManager.AccessTokenResponseBuilder responseBuilder = tokenManager.responseBuilder(realm, targetClient, event, this.session, targetUserSession, clientSessionCtx)
                .generateAccessToken();
        responseBuilder.getAccessToken().issuedFor(client.getClientId());

        if (targetClient != null && !targetClient.equals(client)) {
            responseBuilder.getAccessToken().addAudience(targetClient.getClientId());
        }

        if (formParams.containsKey(OAuth2Constants.REQUESTED_SUBJECT)) {
            // impersonation 时记录发起模拟的客户端 ID
            targetUserSession.setNote(IMPERSONATOR_CLIENT.toString(), client.getId());
        }

        if (targetUserSession.getPersistenceState() == UserSessionModel.SessionPersistenceState.TRANSIENT) {
            responseBuilder.getAccessToken().setSessionId(null);
        }

        String issuedTokenType;
        if (requestedTokenType.equals(OAuth2Constants.REFRESH_TOKEN_TYPE)
                && OIDCAdvancedConfigWrapper.fromClientModel(client).isUseRefreshToken()
                && targetUserSession.getPersistenceState() != UserSessionModel.SessionPersistenceState.TRANSIENT) {
            responseBuilder.generateRefreshToken();
            responseBuilder.getRefreshToken().issuedFor(client.getClientId());
            issuedTokenType = OAuth2Constants.REFRESH_TOKEN_TYPE;
        } else {
            issuedTokenType = OAuth2Constants.ACCESS_TOKEN_TYPE;
        }

        String scopeParam = clientSessionCtx.getClientSession().getNote(OAuth2Constants.SCOPE);
        if (TokenUtil.isOIDCRequest(scopeParam)) {
            responseBuilder.generateIDToken().generateAccessTokenHash();
        }

        AccessTokenResponse res = responseBuilder.build();
        res.setOtherClaims(OAuth2Constants.ISSUED_TOKEN_TYPE, issuedTokenType);

        event.detail(Details.AUDIENCE, targetClient.getClientId())
                .user(targetUser);

        event.success();

        return cors.add(Response.ok(res, MediaType.APPLICATION_JSON_TYPE));
    }

    /** V1 SAML2 交换：创建 SAML 认证会话并返回 Base64 断言 @return JSON 响应（token 为 SAML 断言） */
    @Override
    protected Response exchangeClientToSAML2Client(UserModel targetUser, UserSessionModel targetUserSession, String requestedTokenType, List<ClientModel> targetAudienceClients) {
        ClientModel targetClient = getTargetClient(targetAudienceClients);

        // 为目标 SAML 2.0 客户端创建已认证用户的认证会话
        LoginProtocolFactory factory = (LoginProtocolFactory) session.getKeycloakSessionFactory()
                .getProviderFactory(LoginProtocol.class, SamlProtocol.LOGIN_PROTOCOL);
        SamlService samlService = (SamlService) factory.createProtocolEndpoint(session, event);
        AuthenticationSessionModel authSession = samlService.getOrCreateLoginSessionForIdpInitiatedSso(session, realm,
                targetClient, null);
        if (authSession == null) {
            logger.error("SAML assertion consumer url not set up");
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, "Client requires assertion consumer url set up", Response.Status.BAD_REQUEST);
        }

        authSession.setAuthenticatedUser(targetUser);

        event.session(targetUserSession);

        AuthenticationManager.setClientScopesInSession(session, authSession);
        ClientSessionContext clientSessionCtx = TokenManager.attachAuthenticationSession(this.session, targetUserSession,
                authSession);

        updateUserSessionFromClientAuth(targetUserSession);

        // 生成 SAML 2.0 断言响应
        SamlClient samlClient = new SamlClient(targetClient);
        SamlProtocol samlProtocol = new TokenEndpoint.TokenExchangeSamlProtocol(samlClient).setEventBuilder(event).setHttpHeaders(headers).setRealm(realm)
                .setSession(session).setUriInfo(session.getContext().getUri());

        Response samlAssertion = samlProtocol.authenticated(authSession, targetUserSession, clientSessionCtx);
        if (samlAssertion.getStatus() != 200) {
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "Can not get SAML 2.0 token", Response.Status.BAD_REQUEST);
        }
        String xmlString = (String) samlAssertion.getEntity();
        String encodedXML = Base64Url.encode(xmlString.getBytes(GeneralConstants.SAML_CHARSET));

        int assertionLifespan = samlClient.getAssertionLifespan();

        AccessTokenResponse res = new AccessTokenResponse();
        res.setToken(encodedXML);
        res.setTokenType("Bearer");
        res.setExpiresIn(assertionLifespan <= 0 ? realm.getAccessCodeLifespan() : assertionLifespan);
        res.setOtherClaims(OAuth2Constants.ISSUED_TOKEN_TYPE, requestedTokenType);

        event.detail(Details.AUDIENCE, targetClient.getClientId())
                .user(targetUser);

        event.success();

        return cors.add(Response.ok(res, MediaType.APPLICATION_JSON_TYPE));
    }
}
