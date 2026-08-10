/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.broker.oauth;

import java.io.IOException;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 通用 OAuth 2.0 身份代理：通过 UserInfo 端点获取用户资料并映射联邦身份。
 * <p>适用于非 OIDC 的 OAuth 2.0 提供者；claim 名称可在配置中自定义。</p>
 */
public class OAuth2IdentityProvider extends AbstractOAuth2IdentityProvider<OAuth2IdentityProviderConfig> {

    /** @param session Keycloak 会话 @param config OAuth2 身份代理配置 */
    public OAuth2IdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
        super(session, config);
    }

    /** @return 默认 OAuth scope（OAuth2 提供者通常为空） */
    @Override
    protected String getDefaultScopes() {
        return "";
    }

    /** 调用 UserInfo 端点，按配置 claim 映射用户 ID、姓名、邮箱与用户名。 */
    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        BrokeredIdentityContext identity = new BrokeredIdentityContext(getConfig());

        if (accessToken != null) {
            JsonNode userInfo = fetchUserProfile(accessToken);

            AbstractJsonUserAttributeMapper.storeUserProfileForMapper(identity, userInfo, getConfig().getAlias());

            String id = getJsonProperty(userInfo, getConfig().getUserIDClaim());
            identity.setId(id);

            String givenName = getJsonProperty(userInfo, getConfig().getGivenNameClaim());

            if (givenName != null) {
                identity.setFirstName(givenName);
            }

            String familyName = getJsonProperty(userInfo, getConfig().getFamilyNameClaim());

            if (familyName != null) {
                identity.setLastName(familyName);
            }

            if (givenName == null && familyName == null) {
                String name = getJsonProperty(userInfo, getConfig().getFullNameClaim());
                identity.setName(name);
            }

            String email = getJsonProperty(userInfo, getConfig().getEmailClaim());
            identity.setEmail(email);

            identity.setBrokerUserId(getConfig().getAlias() + "." + id);

            String preferredUsername = getJsonProperty(userInfo, getConfig().getUserNameClaim());

            if (preferredUsername == null) {
                preferredUsername = email;
            }

            if (preferredUsername == null) {
                preferredUsername = id;
            }

            identity.setUsername(preferredUsername);
        }

        return identity;
    }


    /** 携带 Bearer access token 请求 UserInfo 并解析 JSON 响应。 */
    private JsonNode fetchUserProfile(String accessToken) {
        String userInfoUrl = getConfig().getUserInfoUrl();

        try (SimpleHttpResponse response = executeRequest(userInfoUrl, SimpleHttp.create(session).doGet(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))) {
            String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
            MediaType contentMediaType;

            try {
                contentMediaType = MediaType.valueOf(contentType);
            } catch (IllegalArgumentException ex) {
                contentMediaType = null;
            }

            if (contentMediaType == null || contentMediaType.isWildcardSubtype() || contentMediaType.isWildcardType()) {
                throw new RuntimeException("Unsupported content-type [" + contentType + "] in response from [" + userInfoUrl + "].");
            }

            if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(contentMediaType)) {
                throw new RuntimeException("Unsupported content-type [" + contentType + "] in response from [" + userInfoUrl + "].");
            }

            return response.asJson();
        } catch (Exception e) {
            throw new IdentityBrokerException("Error while fetching user profile", e);
        }
    }

    /** 执行 HTTP 请求并校验 2xx 成功状态。 */
    private SimpleHttpResponse executeRequest(String url, SimpleHttpRequest request) throws IOException {
        SimpleHttpResponse response = request.asResponse();
        int status = response.getStatus();

        if (Response.Status.fromStatusCode(status).getFamily() != Response.Status.Family.SUCCESSFUL) {
            logger.warnf("User profile endpoint (%s) returned an error (%d): %s", url, status, response.asString());
            throw new RuntimeException("Unexpected response from user profile endpoint");
        }

        return  response;
    }
}
