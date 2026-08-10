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

package org.keycloak.social.github;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.BasicAuthHelper;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * GitHub OAuth2 社交身份提供者。
 * <p>支持自定义 base/api URL、JSON 令牌响应格式，以及主邮箱回退查询。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class GitHubIdentityProvider extends AbstractOAuth2IdentityProvider implements SocialIdentityProvider {

    /** 默认 GitHub Web 站点根 URL。 */
    public static final String DEFAULT_BASE_URL = "https://github.com";
    /** OAuth 授权路径片段。 */
    public static final String AUTH_FRAGMENT = "/login/oauth/authorize";
    /** OAuth 令牌路径片段。 */
    public static final String TOKEN_FRAGMENT = "/login/oauth/access_token";
    public static final String DEFAULT_AUTH_URL = DEFAULT_BASE_URL + AUTH_FRAGMENT;
    public static final String DEFAULT_TOKEN_URL = DEFAULT_BASE_URL + TOKEN_FRAGMENT;
    /** @deprecated Use {@link #DEFAULT_AUTH_URL} instead. */
    @Deprecated
    public static final String AUTH_URL = DEFAULT_AUTH_URL;
    /** @deprecated Use {@link #DEFAULT_TOKEN_URL} instead. */
    @Deprecated
    public static final String TOKEN_URL = DEFAULT_TOKEN_URL;

    public static final String DEFAULT_API_URL = "https://api.github.com";
    public static final String APPLICATIONS_FRAGMENT = "/applications";
    public static final String PROFILE_FRAGMENT = "/user";
    public static final String EMAIL_FRAGMENT = "/user/emails";
    public static final String DEFAULT_APPLICATIONS_URL = DEFAULT_API_URL + APPLICATIONS_FRAGMENT;
    public static final String DEFAULT_PROFILE_URL = DEFAULT_API_URL + PROFILE_FRAGMENT;
    public static final String DEFAULT_EMAIL_URL = DEFAULT_API_URL + EMAIL_FRAGMENT;
    /** @deprecated Use {@link #DEFAULT_PROFILE_URL} instead. */
    @Deprecated
    public static final String PROFILE_URL = DEFAULT_PROFILE_URL;
    /** @deprecated Use {@link #DEFAULT_EMAIL_URL} instead. */
    @Deprecated
    public static final String EMAIL_URL = DEFAULT_EMAIL_URL;

    /** 默认 scope，含读取用户邮箱权限。 */
    public static final String DEFAULT_SCOPE = "user:email";

    /** 配置 map 中自定义 GitHub Web 根 URL 的键。 */
    protected static final String BASE_URL_KEY = "baseUrl";
    /** 配置 map 中自定义 GitHub API 根 URL 的键。 */
    protected static final String API_URL_KEY = "apiUrl";
    /** 配置 map 中是否启用 GitHub JSON 令牌响应格式的键。 */
    protected static final String GITHUB_JSON_FORMAT_KEY = "githubJsonFormat";
    /** 配置 map 中邮箱 API URL 的键（运行时写入）。 */
    protected static final String EMAIL_URL_KEY = "emailUrl";

    private final String authUrl;
    private final String tokenUrl;
    private final String profileUrl;
    private final String emailUrl;
    private final boolean githubJsonFormat;

    /** 根据 base/api URL 配置组装授权、令牌、profile 与邮箱端点。 */
    public GitHubIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
        super(session, config);

        String baseUrl = getUrlFromConfig(config, BASE_URL_KEY, DEFAULT_BASE_URL);
        String apiUrl = getUrlFromConfig(config, API_URL_KEY, DEFAULT_API_URL);

        authUrl = baseUrl + AUTH_FRAGMENT;
        tokenUrl = baseUrl + TOKEN_FRAGMENT;
        profileUrl = apiUrl + PROFILE_FRAGMENT;
        emailUrl = apiUrl + EMAIL_FRAGMENT;

        config.setAuthorizationUrl(authUrl);
        config.setTokenUrl(tokenUrl);
        config.setUserInfoUrl(profileUrl);
        config.getConfig().put(EMAIL_URL_KEY, emailUrl);
        githubJsonFormat = Boolean.parseBoolean(config.getConfig().getOrDefault(GITHUB_JSON_FORMAT_KEY, "false"));
    }

    /**
     * 从配置读取 URL，空值回退默认值并去除末尾斜杠。
     *
     * @param config 身份提供者配置
     * @param key 配置 map 中的键
     * @param defaultValue 键值为空时的默认 URL
     * @return 规范化后的 URL
     */
    protected static String getUrlFromConfig(OAuth2IdentityProviderConfig config, String key, String defaultValue) {
        String url = config.getConfig().get(key);
        if (url == null || url.trim().isEmpty()) {
            url = defaultValue;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

	/** 支持外部令牌交换。 */
	@Override
	protected boolean supportsExternalExchange() {
		return true;
	}

	@Override
	protected String getProfileEndpointForValidation(EventBuilder event) {
		return profileUrl;
	}

    /** 若启用 JSON 格式，在令牌请求上设置 Accept: application/json。 */
    @Override
    public SimpleHttpRequest authenticateTokenRequest(SimpleHttpRequest tokenRequest) {
        SimpleHttpRequest simpleHttp = super.authenticateTokenRequest(tokenRequest);
        if (githubJsonFormat) {
            simpleHttp.acceptJson();
        }
        return simpleHttp;
    }

    /** 从 GitHub /user JSON 提取 login、name、email 等字段。 */
    @Override
	protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
		BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "id"), getConfig());

		String username = getJsonProperty(profile, "login");
		user.setUsername(username);
		user.setName(getJsonProperty(profile, "name"));
		user.setEmail(getJsonProperty(profile, "email"));
		user.setIdp(this);

		AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

		return user;
	}

	/** 获取 GitHub 用户资料；profile 无 email 时查询主邮箱列表。 */
	@Override
	protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
		try (SimpleHttpResponse response = SimpleHttp.create(session).doGet(profileUrl)
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Accept", "application/json")
                        .asResponse()) {

                    if (Response.Status.fromStatusCode(response.getStatus()).getFamily() != Response.Status.Family.SUCCESSFUL) {
                        logger.warnf("Profile endpoint returned an error (%d): %s", response.getStatus(), response.asString());
                        throw new IdentityBrokerException("Profile could not be retrieved from the github endpoint");
                    }

                    JsonNode profile = response.asJson();
                    logger.tracef("profile retrieved from github: %s", profile);
                    BrokeredIdentityContext user = extractIdentityFromProfile(null, profile);

                    if (user.getEmail() == null) {
                        user.setEmail(searchEmail(accessToken));
                    }
                    return user;
		} catch (Exception e) {
			throw new IdentityBrokerException("Profile could not be retrieved from the github endpoint", e);
		}
	}

	/** 遍历 /user/emails 响应，返回标记为 primary 的邮箱地址。 */
	private String searchEmail(String accessToken) {
		try (SimpleHttpResponse response = SimpleHttp.create(session).doGet(emailUrl)
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Accept", "application/json")
                        .asResponse()) {

                    if (Response.Status.fromStatusCode(response.getStatus()).getFamily() != Response.Status.Family.SUCCESSFUL) {
                        logger.warnf("Primary email endpoint returned an error (%d): %s", response.getStatus(), response.asString());
                        throw new IdentityBrokerException("Primary email could not be retrieved from the github endpoint");
                    }

                    JsonNode emails = response.asJson();
                    logger.tracef("emails retrieved from github: %s", emails);
                    if (emails.isArray()) {
                        Iterator<JsonNode> loop = emails.elements();
                        while (loop.hasNext()) {
                            JsonNode mail = loop.next();
                            JsonNode primary = mail.get("primary");
                            if (primary != null && primary.asBoolean()) {
                                return getJsonProperty(mail, "email");
                            }
                        }
                    }

                    throw new IdentityBrokerException("Primary email from github is not found in the user's email list.");
		} catch (Exception e) {
			throw new IdentityBrokerException("Primary email could not be retrieved from the github endpoint", e);
		}
	}

    /** 通过 GitHub Applications API 校验 token 是否属于当前 client id。 */
    private void verifyToken(String accessToken) throws IOException {
        String tokenUrl = DEFAULT_APPLICATIONS_URL + "/" + getConfig().getClientId() + "/token";
        SimpleHttpResponse response = SimpleHttp.create(session).doPost(tokenUrl)
                .header("Authorization",  BasicAuthHelper.createHeader(getConfig().getClientId(), getConfig().getClientSecret()))
                .json(Map.of("access_token", accessToken)).asResponse();

        JsonNode jsonNodeResponse = response.asJson();
        if (response.getStatus() != 200) {
            String errorMessage = getJsonProperty(jsonNodeResponse, "message");
            throw new RuntimeException("Error message: " + errorMessage);
        }

        JsonNode appNode = jsonNodeResponse.get("app");
        if (appNode == null || appNode.isNull()) {
            throw new RuntimeException("Invalid token check response: 'app' field is missing.");
        }

        String clientId = getJsonProperty(appNode, "client_id");
        if (!getConfig().getClientId().equals(clientId)) {
            throw new RuntimeException("Client ID does not match the client_id in the access token check response.");
        }
    }


	/** 返回默认 OAuth scope。 */
    @Override
	protected String getDefaultScopes() {
		return DEFAULT_SCOPE;
	}
}
