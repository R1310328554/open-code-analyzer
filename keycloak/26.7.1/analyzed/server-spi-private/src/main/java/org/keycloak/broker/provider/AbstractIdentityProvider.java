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
package org.keycloak.broker.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.Booleans;

import org.jboss.logging.Logger;

/**
 * 身份联邦提供者抽象基类，实现 {@link UserAuthenticationIdentityProvider} 的通用逻辑。
 * <p>涵盖登录/登出、令牌交换错误响应、账户链接 URL 生成、联邦用户资料同步（邮箱等）及默认 {@link IdentityProviderDataMarshaller}。</p>
 *
 * @author Pedro Igor
 */
public abstract class AbstractIdentityProvider<C extends IdentityProviderModel> implements UserAuthenticationIdentityProvider<C> {

    protected static final Logger logger = Logger.getLogger(AbstractIdentityProvider.class);

    // 客户端会话标记：用户在更新资料页修改了 IdP 提供的邮箱
    public static final String UPDATE_PROFILE_EMAIL_CHANGED = "UPDATE_PROFILE_EMAIL_CHANGED";
    public static final String UPDATE_PROFILE_USERNAME_CHANGED = "UPDATE_PROFILE_USERNAME_CHANGED";

    // 客户端会话标记：true 表示新导入用户，false 表示仅关联已有 Keycloak 用户
    public static final String BROKER_REGISTERED_NEW_USER = "BROKER_REGISTERED_NEW_USER";

    /** 令牌交换错误响应中账户链接 URL 的 JSON 字段名。 */
    public static final String ACCOUNT_LINK_URL = "account-link-url";
    protected final KeycloakSession session;
    private final C config;

    /** 绑定 Keycloak 会话与身份提供者配置。 */
    public AbstractIdentityProvider(KeycloakSession session, C config) {
        this.session = session;
        this.config = config;
    }

    /** 返回当前身份提供者配置模型。 */
    public C getConfig() {
        return this.config;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return null;
    }

    @Override
    public Response performLogin(AuthenticationRequest request) {
        return null;
    }

    @Override
    public Response keycloakInitiatedBrowserLogout(KeycloakSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {
        return null;
    }

    @Override
    public void backchannelLogout(KeycloakSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {

    }

    /** 返回 {@code invalid_target} / {@code target_exchange_unsupported} 错误响应。 */
    public Response exchangeNotSupported() {
        Map<String, String> error = new HashMap<>();
        error.put("error", "invalid_target");
        error.put("error_description", "target_exchange_unsupported");
        return  Response.status(400).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /** 身份提供者未与当前用户关联时的交换错误响应。 */
    public Response exchangeNotLinked(UriInfo uriInfo, ClientModel authorizedClient, UserSessionModel tokenUserSession, UserModel tokenSubject) {
        return exchangeErrorResponse(uriInfo, authorizedClient, tokenUserSession, "not_linked", "identity provider is not linked");
    }

    /** 未关联且仅允许链接到当前用户会话时的交换错误响应。 */
    public Response exchangeNotLinkedNoStore(UriInfo uriInfo, ClientModel authorizedClient, UserSessionModel tokenUserSession, UserModel tokenSubject) {
        return exchangeErrorResponse(uriInfo, authorizedClient, tokenUserSession, "not_linked", "identity provider is not linked, can only link to current user session");
    }

    /** 构建带可选 {@link #ACCOUNT_LINK_URL} 的 JSON 格式交换错误响应。 */
    protected Response exchangeErrorResponse(UriInfo uriInfo, ClientModel authorizedClient, UserSessionModel tokenUserSession, String errorCode, String reason) {
        Map<String, String> error = new HashMap<>();
        error.put("error", errorCode);
        error.put("error_description", reason);
        if (authorizedClient != null) {
            String accountLinkUrl = getLinkingUrl(uriInfo, authorizedClient, tokenUserSession);
            if (accountLinkUrl != null) {
                error.put(ACCOUNT_LINK_URL, accountLinkUrl);
            }
        }
        return Response.status(400).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /** 生成带 nonce/hash 校验的账户链接 URL（{@code /realms/{realm}/broker/{provider}/link}）。 */
    protected String getLinkingUrl(UriInfo uriInfo, ClientModel authorizedClient, UserSessionModel tokenUserSession) {
        String provider = getConfig().getAlias();
        String clientId = authorizedClient.getClientId();
        String nonce = UUID.randomUUID().toString();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        String input = nonce + tokenUserSession.getId() + clientId + provider;
        byte[] check = md.digest(input.getBytes(StandardCharsets.UTF_8));
        String hash = Base64Url.encode(check);
        return KeycloakUriBuilder.fromUri(uriInfo.getBaseUri())
                .path("/realms/{realm}/broker/{provider}/link")
                .queryParam("nonce", nonce)
                .queryParam("hash", hash)
                .queryParam("client_id", clientId)
                .build(authorizedClient.getRealm().getName(), provider)
                .toString();
    }

    /** 关联令牌已过期时的交换错误响应。 */
    public Response exchangeTokenExpired(UriInfo uriInfo, ClientModel authorizedClient, UserSessionModel tokenUserSession, UserModel tokenSubject) {
        return exchangeErrorResponse(uriInfo, authorizedClient, tokenUserSession, "token_expired", "linked token is expired");
    }

    /** 请求的响应令牌类型不受支持时的错误响应。 */
    public Response exchangeUnsupportedRequiredType() {
        Map<String, String> error = new HashMap<>();
        error.put("error", "invalid_target");
        error.put("error_description", "response_token_type_unsupported");
        return Response.status(400).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public void authenticationFinished(AuthenticationSessionModel authSession, BrokeredIdentityContext context) {

    }

    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, BrokeredIdentityContext context) {

    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, BrokeredIdentityContext context) {

    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, BrokeredIdentityContext context) {
        updateEmail(user, context);
    }

    /** 按同步模式与更新资料标记，将联邦邮箱写入本地用户。 */
    protected void updateEmail(UserModel user, BrokeredIdentityContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        // Could be the case during external-internal token exchange
        if (authSession == null) {
            return;
        }

        String email = context.getEmail();

        if (email == null) {
            // do not set email if not provided by the IdP
            return;
        }

        boolean isNewUser = Boolean.parseBoolean(authSession.getAuthNote(BROKER_REGISTERED_NEW_USER));

        if (isNewUser || IdentityProviderSyncMode.FORCE.equals(getConfig().getSyncMode())) {
            if (Boolean.parseBoolean(authSession.getAuthNote(UPDATE_PROFILE_EMAIL_CHANGED))) {
                // user updated the email and needs verification
                user.setEmailVerified(false);
            } else {
                setEmailVerified(user, context);
            }

            user.setEmail(email);
        }
    }

    /** 根据 IdP {@code trustEmail} 配置决定是否自动验证邮箱。 */
    protected void setEmailVerified(UserModel user, BrokeredIdentityContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        boolean isNewUser = Boolean.parseBoolean(authSession.getAuthNote(BROKER_REGISTERED_NEW_USER));
        String federatedEmail = context.getEmail();
        String localEmail = user.getEmail();

        if (isNewUser || federatedEmail != null && !federatedEmail.equalsIgnoreCase(localEmail)) {
            IdentityProviderModel config = context.getIdpConfig();
            boolean trustEmail = Booleans.isTrue(config.isTrustEmail());

            if (logger.isTraceEnabled()) {
                logger.tracef("Email %s verified automatically after updating user '%s' through Identity provider '%s' ", trustEmail ? "" : "not", user.getUsername(), config.getAlias());
            }

            user.setEmailVerified(trustEmail);
        }
    }

    /** 默认使用 {@link DefaultDataMarshaller} 序列化联邦上下文数据。 */
    @Override
    public IdentityProviderDataMarshaller getMarshaller() {
        return new DefaultDataMarshaller();
    }

    /** 若当前会话由本 IdP 登录，返回 {@code FEDERATED_ACCESS_TOKEN} 会话备注。 */
    protected String getFederatedAccessToken(UserSessionModel userSession) {
        // return the FEDERATED_ACCESS_TOKEN but just if logged in using this identity provider
        if (getConfig().getAlias().equals(userSession.getNote(Details.IDENTITY_PROVIDER))) {
             return userSession.getNote(FEDERATED_ACCESS_TOKEN);
        }
        return null;
    }

    /** 构建令牌交换成功响应，附带 {@code issued_token_type} 与可选账户链接 URL。 */
    protected Response buildTokenResponse(UriInfo uriInfo, EventBuilder event, ClientModel authorizedClient,
            UserSessionModel tokenUserSession, AccessTokenResponse tokenResponse, String issuedTokenType) {
        tokenResponse.setIdToken(null);
        tokenResponse.setRefreshToken(null);
        tokenResponse.setRefreshExpiresIn(0);
        tokenResponse.getOtherClaims().clear();

        tokenResponse.getOtherClaims().put(OAuth2Constants.ISSUED_TOKEN_TYPE, issuedTokenType);

        if (authorizedClient != null) {
            tokenResponse.getOtherClaims().put(ACCOUNT_LINK_URL, getLinkingUrl(uriInfo, authorizedClient, tokenUserSession));
        }
        if (event != null) {
            event.success();
        }
        return Response.ok(tokenResponse).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
