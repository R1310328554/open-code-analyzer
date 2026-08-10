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

package org.keycloak.social.facebook;

import java.io.IOException;

import jakarta.ws.rs.core.HttpHeaders;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.keycloak.saml.common.util.StringUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Facebook OAuth2 社交身份提供者。
 * <p>通过 Graph API 获取用户资料，支持可配置额外字段与外部令牌交换。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FacebookIdentityProvider extends AbstractOAuth2IdentityProvider<FacebookIdentityProviderConfig> implements SocialIdentityProvider<FacebookIdentityProviderConfig> {

	/** Facebook OAuth 授权端点。 */
	public static final String AUTH_URL = "https://graph.facebook.com/oauth/authorize";
	/** Facebook OAuth 令牌端点。 */
	public static final String TOKEN_URL = "https://graph.facebook.com/oauth/access_token";
	/** Graph API 用户资料端点（默认字段集）。 */
	public static final String PROFILE_URL = "https://graph.facebook.com/me?fields=id,name,email,first_name,last_name";
    /** Facebook debug_token 端点，用于校验 access token。 */
    public static final String DEBUG_TOKEN_URL = "https://graph.facebook.com/debug_token";
	/** 默认 OAuth scope，请求用户邮箱。 */
	public static final String DEFAULT_SCOPE = "email";
	/** 追加自定义 profile 字段时的 URL 分隔符。 */
	protected static final String PROFILE_URL_FIELDS_SEPARATOR = ",";

	/** 构造 Facebook IdP 并设置授权/令牌/UserInfo URL。 */
	public FacebookIdentityProvider(KeycloakSession session, FacebookIdentityProviderConfig config) {
		super(session, config);
		config.setAuthorizationUrl(AUTH_URL);
		config.setTokenUrl(TOKEN_URL);
		config.setUserInfoUrl(PROFILE_URL);
	}

	/** 拉取 Facebook 用户资料并映射为联邦身份。 */
	protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
		try {
			final String fetchedFields = getConfig().getFetchedFields();
			final String url = StringUtil.isNotNull(fetchedFields)
					? String.join(PROFILE_URL_FIELDS_SEPARATOR, PROFILE_URL, fetchedFields)
					: PROFILE_URL;
			JsonNode profile = SimpleHttp.create(session).doGet(url).header("Authorization", "Bearer " + accessToken).asJson();
			return extractIdentityFromProfile(null, profile);
		} catch (Exception e) {
			throw new IdentityBrokerException("Could not obtain user profile from facebook.", e);
		}
	}

    /** 调用 debug_token 校验 token 归属的应用 client id。 */
    private void verifyToken(String accessToken) throws IOException {
        JsonNode response = SimpleHttp.create(session).doGet(DEBUG_TOKEN_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getConfig().getClientId() + "|" + getConfig().getClientSecret())
                .param("input_token", accessToken)
                .asJson();

        JsonNode errorNode = response.get("error");
        if (errorNode != null) {
            String errorMessage = getJsonProperty(errorNode, "message");
            throw new RuntimeException("Error message:  " + errorMessage);
        }

        JsonNode dataNode = response.get("data");
        if (dataNode == null || dataNode.isNull()) {
            throw new RuntimeException("Invalid token debug response: 'data' field is missing.");
        }

        String appId = getJsonProperty(dataNode, "app_id");
        if (!getConfig().getClientId().equals(appId)) {
            throw new RuntimeException("Client ID does not match the app_id in the access token debug response.");
        }
    }

	/** 支持外部令牌交换。 */
	@Override
	protected boolean supportsExternalExchange() {
		return true;
	}

	/** 外部交换校验使用的 profile 端点。 */
	@Override
	protected String getProfileEndpointForValidation(EventBuilder event) {
		return PROFILE_URL;
	}

	/** 从 Graph API JSON 提取 id、邮箱、姓名等并构建联邦身份。 */
	@Override
	protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
		String id = getJsonProperty(profile, "id");

		BrokeredIdentityContext user = new BrokeredIdentityContext(id, getConfig());

		String email = getJsonProperty(profile, "email");

		user.setEmail(email);

		String username = getJsonProperty(profile, "username");

		// Facebook 可能无 username，回退至 email 或 id
		if (username == null) {
            if (email != null) {
                username = email;
            } else {
                username = id;
            }
        }

		user.setUsername(username);

		String firstName = getJsonProperty(profile, "first_name");
		String lastName = getJsonProperty(profile, "last_name");

		if (lastName == null) {
		    lastName = "";
		}

		user.setFirstName(firstName);
		user.setLastName(lastName);
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
