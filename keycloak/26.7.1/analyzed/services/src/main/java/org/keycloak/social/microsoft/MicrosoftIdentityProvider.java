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

package org.keycloak.social.microsoft;

import java.util.Optional;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.validation.Validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;

/**
 * Microsoft 账户 OAuth2 社交身份提供者。
 * <p>使用 Microsoft Graph OAuth 2 协议，文档参见
 * <a href="https://docs.microsoft.com/en-us/onedrive/developer/rest-api/getting-started/graph-oauth">Microsoft Graph OAuth</a>。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class MicrosoftIdentityProvider extends AbstractOAuth2IdentityProvider implements SocialIdentityProvider {

    private static final Logger log = Logger.getLogger(MicrosoftIdentityProvider.class);

    /** 授权码端点 URL 模板（占位符为租户 ID）。 */
    private static final String AUTH_URL_TEMPLATE = "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize";
    /** 令牌端点 URL 模板。 */
    private static final String TOKEN_URL_TEMPLATE = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
    /** Microsoft Graph 用户资料端点。 */
    private static final String PROFILE_URL = "https://graph.microsoft.com/v1.0/me/";
    /** 默认 scope，User.read 足以获取基本用户信息。 */
    private static final String DEFAULT_SCOPE = "User.read";

    /**
     * 构造 Microsoft IdP 并按租户配置 OAuth 端点。
     * <p>未指定 tenantId 时使用多租户 {@code common} 端点。</p>
     */
    public MicrosoftIdentityProvider(KeycloakSession session, MicrosoftIdentityProviderConfig config) {
        super(session, config);

        // 未配置租户时使用多租户 common 端点
        String tenant = Optional.ofNullable(config.getTenantId()).map(String::trim).orElse("common");

        config.setAuthorizationUrl(String.format(AUTH_URL_TEMPLATE, tenant));
        config.setTokenUrl(String.format(TOKEN_URL_TEMPLATE, tenant));
        config.setUserInfoUrl(PROFILE_URL);
    }

    /** 支持外部令牌交换。 */
    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    /** 返回用于令牌校验的 Graph 用户资料端点。 */
    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return PROFILE_URL;
    }

    /** 使用访问令牌从 Microsoft Graph 拉取用户资料。 */
    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            JsonNode profile = SimpleHttp.create(session).doGet(PROFILE_URL).auth(accessToken).asJson();
            if (profile.has("error") && !profile.get("error").isNull()) {
                throw new IdentityBrokerException("Error in Microsoft Graph API response. Payload: " + profile.toString());
            }
            return extractIdentityFromProfile(null, profile);
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from Microsoft Graph", e);
        }
    }

    /**
     * 从 Graph API JSON 资料提取联邦身份上下文。
     * <p>优先使用 mail 字段作为邮箱；若缺失且 userPrincipalName 为合法邮箱则回退使用。</p>
     */
    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        String id = getJsonProperty(profile, "id");
        BrokeredIdentityContext user = new BrokeredIdentityContext(id, getConfig());

        String email = getJsonProperty(profile, "mail");
        if (email == null && profile.has("userPrincipalName")) {
            String username = getJsonProperty(profile, "userPrincipalName");
            if (Validation.isEmailValid(username)) {
                email = username;
            }
        }
        user.setUsername(email != null ? email : id);
        user.setFirstName(getJsonProperty(profile, "givenName"));
        user.setLastName(getJsonProperty(profile, "surname"));
        if (email != null)
            user.setEmail(email);
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
        return user;
    }

    /** 返回默认 OAuth scope。 */
    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}
