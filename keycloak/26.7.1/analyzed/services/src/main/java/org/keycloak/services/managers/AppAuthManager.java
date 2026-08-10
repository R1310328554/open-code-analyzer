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
package org.keycloak.services.managers;

import java.util.List;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.util.DPoPUtil;

import org.jboss.logging.Logger;

import static org.keycloak.util.TokenUtil.TOKEN_TYPE_BEARER;
import static org.keycloak.util.TokenUtil.TOKEN_TYPE_DPOP;

/**
 * 应用层认证管理器。
 * <p>扩展 {@link AuthenticationManager}，处理 Identity Cookie 刷新及 Bearer/DPoP 令牌解析与校验。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AppAuthManager extends AuthenticationManager {

    @Override
    public AuthResult authenticateIdentityCookie(KeycloakSession session, RealmModel realm) {
        AuthResult authResult = super.authenticateIdentityCookie(session, realm);
        if (authResult == null) return null;
        // 刷新登录 Cookie
        createLoginCookie(session, realm, authResult.user(), authResult.session(), session.getContext().getUri(), session.getContext().getConnection());
        if (authResult.session().isRememberMe()) createRememberMeCookie(authResult.user().getUsername(), session.getContext().getUri(), session);
        return authResult;
    }

    /**
     * 从 Authorization 头解析令牌字符串。
     * @param authHeader Authorization 头原始值
     * @return 含 scheme 与 token 的 {@link AuthHeader}，无法解析时返回 {@literal null}
     */
    private static AuthHeader extractTokenStringFromAuthHeader(String authHeader) {

        if (authHeader == null) {
            return null;
        }

        int indexOfSpace = authHeader.indexOf(' ');

        if (indexOfSpace <= 0) {
            return null;
        }

        String typeString = authHeader.substring(0, indexOfSpace);
        String tokenString = authHeader.substring(indexOfSpace + 1);

        // 认证 scheme 按 RFC9110 大小写不敏感
        // Checked by fapi2-security-profile-final-access-token-type-header-case-sensitivity
        boolean isBearerHeader = typeString.equalsIgnoreCase(TOKEN_TYPE_BEARER);
        if (!Profile.isFeatureEnabled(Profile.Feature.DPOP)) {
            if (!isBearerHeader) {
                return null;
            }
        } else {
            if (!isBearerHeader && !typeString.equalsIgnoreCase(TOKEN_TYPE_DPOP)) {
                return null;
            }
        }

        if (ObjectUtil.isBlank(tokenString) || tokenString.contains(" ")) {
            return null;
        }

        return new AuthHeader(typeString, tokenString);
    }

    /**
     * 从 HTTP 头提取 Authorization Bearer/DPoP 令牌。
     * @param headers HTTP 请求头
     * @return 解析结果；不支持类型或缺少 token 时返回 {@literal null}
     * @throws NotAuthorizedException 存在多个 Authorization 头时
     */
    public static AuthHeader extractAuthorizationHeaderTokenOrReturnNull(HttpHeaders headers) {
        // 多个 Authorization 头视为错误
        List<String> authHeaders = headers.getRequestHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.isEmpty()) {
            return null;
        }
        if (authHeaders.size() != 1) {
            throw new NotAuthorizedException(TOKEN_TYPE_BEARER);
        }
        String authHeader = headers.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return extractTokenStringFromAuthHeader(authHeader);
    }

    /**
     * 从 Authorization 头提取纯 token 字符串。
     * @param headers HTTP 请求头
     * @return token 字符串；头缺失时返回 {@literal null}
     * @throws NotAuthorizedException 非 Bearer 类型或 token 缺失时
     */
    public static String extractAuthorizationHeaderToken(HttpHeaders headers) {
        String authHeader = headers.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return null;
        }
        AuthHeader parsedHeader = extractTokenStringFromAuthHeader(authHeader);
        if (parsedHeader == null ){
            throw new NotAuthorizedException(TOKEN_TYPE_BEARER);
        }
        return parsedHeader.getToken();
    }

    /** Bearer/DPoP 访问令牌认证器（流式配置）。 */
    public static class BearerTokenAuthenticator {
        private static final Logger logger = Logger.getLogger(BearerTokenAuthenticator.class);
        
        private KeycloakSession session;
        private RealmModel realm;
        private UriInfo uriInfo;
        private ClientConnection connection;
        private HttpHeaders headers;
        private HttpRequest request;
        private String tokenString;
        private String audience;

        public BearerTokenAuthenticator(KeycloakSession session) {
            this.session = session;
        }

        public BearerTokenAuthenticator setSession(KeycloakSession session) {
            this.session = session;
            return this;
        }

        public BearerTokenAuthenticator setRealm(RealmModel realm) {
            this.realm = realm;
            return this;
        }

        public BearerTokenAuthenticator setUriInfo(UriInfo uriInfo) {
            this.uriInfo = uriInfo;
            return this;
        }

        public BearerTokenAuthenticator setConnection(ClientConnection connection) {
            this.connection = connection;
            return this;
        }

        public BearerTokenAuthenticator setHeaders(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public BearerTokenAuthenticator setRequest(HttpRequest request) {
            this.request = request;
            return this;
        }

        public BearerTokenAuthenticator setTokenString(String tokenString) {
            this.tokenString = tokenString;
            return this;
        }

        public BearerTokenAuthenticator setAudience(String audience) {
            this.audience = audience;
            return this;
        }

        public AuthResult authenticate() {
            KeycloakContext ctx = session.getContext();
            if (realm == null) realm = ctx.getRealm();
            if (uriInfo == null) uriInfo = ctx.getUri();
            if (connection == null) connection = ctx.getConnection();
            if (headers == null) headers = ctx.getRequestHeaders();
            if (request == null) request = ctx.getHttpRequest();
            if (tokenString == null) tokenString = extractAuthorizationHeaderToken(headers);
            // audience 可为 null

            return verifyIdentityToken(session, realm, uriInfo, connection, true, true, audience, false, tokenString, headers,
                    verifier -> {
                        DPoPUtil.withDPoPVerifier(verifier, realm, new DPoPUtil.Validator(session).request(request).uriInfo(session.getContext().getUri()).accessToken(tokenString));
                        verifier.withChecks(GrantTypeEndpointRestrictionValidator.check(session));
                    });
        }
    }

    /** Authorization 头解析结果（scheme + token）。 */
    public static class AuthHeader {

        /** 认证 scheme（如 Bearer） */
        private final String scheme;
        /** 令牌字符串 */
        private final String token;

        public AuthHeader(String scheme, String token) {
            this.scheme = scheme;
            this.token = token;
        }

        public String getScheme() {
            return scheme;
        }

        public String getToken() {
            return token;
        }
    }

}
