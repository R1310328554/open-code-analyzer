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

package org.keycloak.social.paypal;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * PayPal OpenID Connect 社交身份提供者。
 * <p>支持生产与沙箱环境，通过 UserInfo 端点获取用户资料并映射联邦身份。</p>
 *
 * @author Petter Lysne (petterlysne at hotmail dot com)
 */
public class PayPalIdentityProvider extends AbstractOAuth2IdentityProvider<PayPalIdentityProviderConfig> implements SocialIdentityProvider<PayPalIdentityProviderConfig>{

  /** PayPal 生产环境 API 根 URL。 */
  public static final String BASE_URL = "https://api.paypal.com/v1";
  /** PayPal 生产环境 OAuth2 授权端点。 */
  public static final String AUTH_URL = "https://www.paypal.com/signin/authorize";
	/** OpenID Connect 令牌服务路径。 */
	public static final String TOKEN_RESOURCE = "/identity/openidconnect/tokenservice";
	/** UserInfo 端点路径（OpenID schema）。 */
	public static final String PROFILE_RESOURCE = "/oauth2/token/userinfo?schema=openid";
	/** 默认 OAuth scope：openid、profile、email。 */
	public static final String DEFAULT_SCOPE = "openid profile email";

	/** 构造 PayPal IdP，按沙箱配置切换授权/令牌/UserInfo URL。 */
	public PayPalIdentityProvider(KeycloakSession session, PayPalIdentityProviderConfig config) {
		super(session, config);
		config.setAuthorizationUrl(config.targetSandbox() ? "https://www.sandbox.paypal.com/signin/authorize" : AUTH_URL);
		config.setTokenUrl((config.targetSandbox() ? "https://api.sandbox.paypal.com/v1" : BASE_URL) + TOKEN_RESOURCE);
		config.setUserInfoUrl((config.targetSandbox() ? "https://api.sandbox.paypal.com/v1" : BASE_URL) + PROFILE_RESOURCE);
	}

	/** 支持通过外部令牌交换进行身份联邦。 */
	@Override
	protected boolean supportsExternalExchange() {
		return true;
	}

	/** 外部交换校验时使用的 UserInfo 端点。 */
	@Override
	protected String getProfileEndpointForValidation(EventBuilder event) {
		return getConfig().getUserInfoUrl();
	}

	/** 从 PayPal UserInfo JSON 提取联邦身份并存储原始 profile 供映射器使用。 */
	@Override
	protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
		BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "user_id"), getConfig());

		user.setUsername(getJsonProperty(profile, "email"));
		user.setName(getJsonProperty(profile, "name"));
		user.setEmail(getJsonProperty(profile, "email"));
		user.setIdp(this);

		AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
		return user;
	}


	/** 使用 access token 调用 PayPal UserInfo 获取联邦身份。 */
	@Override
	protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
		try {
			JsonNode profile = SimpleHttp.create(session).doGet(getConfig().getUserInfoUrl()).header("Authorization", "Bearer " + accessToken).asJson();

			return extractIdentityFromProfile(null, profile);
		} catch (Exception e) {
			throw new IdentityBrokerException("Could not obtain user profile from paypal.", e);
		}
	}

	/** 返回默认 OAuth scope。 */
	@Override
	protected String getDefaultScopes() {
		return DEFAULT_SCOPE;
	}
}
