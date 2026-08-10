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

package org.keycloak.social.instagram;

import java.io.IOException;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Instagram OAuth2 社交身份提供者。
 * <p>通过 Instagram Graph API 获取用户资料，并兼容旧版 {@code ig_id} 以支持存量用户登录。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class InstagramIdentityProvider extends AbstractOAuth2IdentityProvider implements SocialIdentityProvider {

	/** Instagram OAuth 授权端点。 */
	public static final String AUTH_URL = "https://api.instagram.com/oauth/authorize";
	/** Instagram OAuth 令牌端点。 */
	public static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
	/** Instagram Graph API 用户资料端点。 */
	public static final String PROFILE_URL = "https://graph.instagram.com/me";
	/** 请求用户资料时默认请求的字段列表。 */
	public static final String PROFILE_FIELDS = "id,username";
	/** 默认 OAuth scope，仅请求基本用户资料。 */
	public static final String DEFAULT_SCOPE = "user_profile";
	/** 旧版 Instagram 用户 ID 字段名（Graph API 中即将弃用）。 */
	public static final String LEGACY_ID_FIELD = "ig_id";

	/** 构造 Instagram IdP 并配置授权、令牌与 UserInfo URL。 */
	public InstagramIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
		super(session, config);
		config.setAuthorizationUrl(AUTH_URL);
		config.setTokenUrl(TOKEN_URL);
		config.setUserInfoUrl(PROFILE_URL);
	}

	/** 使用访问令牌从 Instagram 拉取用户资料并构建联邦身份上下文。 */
	protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
		try {
			// 优先请求含旧版 ig_id 的资料，以便存量用户仍可登录
			JsonNode profile = fetchUserProfile(accessToken, true);
			// ig_id 字段未来可能弃用并返回错误，失败时回退到不含该字段的请求
			if (!profile.has("id")) {
				logger.debugf("Could not fetch user profile from instagram. Trying without %s.", LEGACY_ID_FIELD);
				profile = fetchUserProfile(accessToken, false);
			}

			logger.debug(profile.toString());

			// 新旧 ID 体系是否冲突尚无文档说明，为保险起见对新 ID 加 graph_ 前缀
			String id = "graph_" + getJsonProperty(profile, "id");
	  		String username = getJsonProperty(profile, "username");
			String legacyId = getJsonProperty(profile, LEGACY_ID_FIELD);

			BrokeredIdentityContext user = new BrokeredIdentityContext(id, getConfig());
			user.setUsername(username);
			user.setIdp(this);
			if (legacyId != null && !legacyId.isEmpty()) {
				user.setLegacyId(legacyId);
			}

			AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

			return user;
		} catch (Exception e) {
			throw new IdentityBrokerException("Could not obtain user profile from instagram.", e);
		}
	}

	/**
	 * 调用 Instagram Graph API 获取用户 JSON 资料。
	 *
	 * @param accessToken OAuth 访问令牌
	 * @param includeIgId 是否在 fields 参数中包含旧版 {@link #LEGACY_ID_FIELD}
	 */
	protected JsonNode fetchUserProfile(String accessToken, boolean includeIgId) throws IOException {
		String fields = PROFILE_FIELDS;
		if (includeIgId) {
			fields += "," + LEGACY_ID_FIELD;
		}

		return SimpleHttp.create(session).doGet(PROFILE_URL)
				.param("access_token", accessToken)
				.param("fields", fields)
				.asJson();
	}

	/** 返回默认 OAuth scope。 */
	@Override
	protected String getDefaultScopes() {
		return DEFAULT_SCOPE;
	}
}
