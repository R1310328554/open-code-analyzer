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
package org.keycloak.social.google;

import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.JsonWebToken;



/**
 * Google OIDC 社交身份提供者。
 * <p>对接 Google OpenID Connect，支持托管域限制、离线 refresh token 与 userIp 参数。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class GoogleIdentityProvider extends OIDCIdentityProvider implements SocialIdentityProvider<OIDCIdentityProviderConfig> {

    /** Google OIDC issuer URL。 */
    public static final String ISSUER_URL = "https://accounts.google.com";
    /** Google OAuth2 授权端点。 */
    public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    /** Google OAuth2 令牌端点。 */
    public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    /** Google OpenID Connect UserInfo 端点。 */
    public static final String PROFILE_URL = "https://openidconnect.googleapis.com/v1/userinfo";
    public static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    public static final String JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    /** 默认 OIDC scope 集合。 */
    public static final String DEFAULT_SCOPE = "openid profile email";

    /** Google 托管域（hosted domain）查询参数名。 */
    private static final String OIDC_PARAMETER_HOSTED_DOMAINS = "hd";
    private static final String OIDC_PARAMETER_ACCESS_TYPE = "access_type";
    /** 请求 refresh token 时的 access_type 值。 */
    private static final String ACCESS_TYPE_OFFLINE = "offline";

    /** 构造 Google IdP，启用 JWKS 校验并设置 issuer 与 audience 策略。 */
    public GoogleIdentityProvider(KeycloakSession session, GoogleIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
        getConfig().setUseJwksUrl(true);
        getConfig().setJwksUrl(JWKS_URL);
        getConfig().setIssuer(ISSUER_URL);
        getConfig().setAllowClientIdAsAudience(true);
    }

    /** 返回默认 OIDC scope。 */
    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    /** 若启用 userIp，在 UserInfo URL 附加客户端 IP 以规避 Google 限流。 */
    @Override
    protected String getUserInfoUrl() {
        String uri = super.getUserInfoUrl();
        if (((GoogleIdentityProviderConfig)getConfig()).isUserIp()) {
            ClientConnection connection = session.getContext().getConnection();
            if (connection != null && connection.getRemoteAddr() != null) {
                uri = KeycloakUriBuilder.fromUri(super.getUserInfoUrl()).queryParam("userIp", connection.getRemoteAddr()).build().toString();
            }

        }
        logger.debugv("GOOGLE userInfoUrl: {0}", uri);
        return uri;
    }

    /** 支持外部令牌交换。 */
    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    /** 校验 subject_issuer 或 issuer 是否与 IdP alias 一致。 */
    @Override
    public boolean isIssuer(String issuer, MultivaluedMap<String, String> params) {
        String requestedIssuer = params.getFirst(OAuth2Constants.SUBJECT_ISSUER);
        if (requestedIssuer == null) requestedIssuer = issuer;
        return requestedIssuer.equals(getConfig().getAlias());
    }


    /** 外部交换仅通过 UserInfo 校验实现。 */
    @Override
    protected BrokeredIdentityContext exchangeExternalImpl(EventBuilder event, MultivaluedMap<String, String> params) {
        return exchangeExternalUserInfoValidationOnly(event, params);
    }

    /** 构建授权 URL，按需附加 hd（托管域）与 access_type=offline 参数。 */
    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        UriBuilder uriBuilder = super.createAuthorizationUrl(request);
        final GoogleIdentityProviderConfig googleConfig = (GoogleIdentityProviderConfig) getConfig();
        String hostedDomain = googleConfig.getHostedDomain();

        if (hostedDomain != null) {
            uriBuilder.queryParam(OIDC_PARAMETER_HOSTED_DOMAINS, hostedDomain);
        }

        if (googleConfig.isOfflineAccess()) {
            uriBuilder.queryParam(OIDC_PARAMETER_ACCESS_TYPE, ACCESS_TYPE_OFFLINE);
        }

        return uriBuilder;
    }

    /** 校验 ID Token，并在配置托管域时验证 hd claim 匹配。 */
    @Override
    protected JsonWebToken validateToken(final String encodedToken, final boolean ignoreAudience) {
        JsonWebToken token = super.validateToken(encodedToken, ignoreAudience);
        String hostedDomain = ((GoogleIdentityProviderConfig) getConfig()).getHostedDomain();
        boolean anyHostedDomain = hostedDomain == null || "*".equals(hostedDomain);

        // 未限制托管域或为通配符 * 时跳过 hd 校验
        if (anyHostedDomain) {
            return token;
        }

        Object receivedHdParam = token.getOtherClaims().get(OIDC_PARAMETER_HOSTED_DOMAINS);

        if (receivedHdParam == null) {
            throw new IdentityBrokerException("Identity token does not contain hosted domain parameter.");
        }

        if (List.of(hostedDomain.split(",")).contains(receivedHdParam))  {
            return token;
        }

        throw new IdentityBrokerException("Hosted domain does not match.");
    }

    /** 允许 JWT Authorization Grant 断言复用。 */
    @Override
    public boolean isAssertionReuseAllowed() {
        return true;
    }
}
