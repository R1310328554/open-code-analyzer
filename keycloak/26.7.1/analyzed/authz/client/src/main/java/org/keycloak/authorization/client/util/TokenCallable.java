/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.client.util;

import java.util.concurrent.Callable;

import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.common.util.Time;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.RefreshToken;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * <p>访问令牌获取与刷新 callable，供授权客户端在请求间复用 token。
 *
 * <p>优先返回仍有效的 access token；过期时尝试 refresh token，失败则重新向 token 端点申请。
 */
public class TokenCallable implements Callable<String> {

    private static Logger log = Logger.getLogger(TokenCallable.class);
    private final String userName;
    private final String password;
    private final String scope;
    private final Http http;
    private final Configuration configuration;
    private final ServerConfiguration serverConfiguration;
    private AccessTokenResponse tokenResponse;

    /** 使用资源所有者密码 grant 创建 token 供应器。 */
    public TokenCallable(String userName, String password, String scope, Http http, Configuration configuration,
        ServerConfiguration serverConfiguration) {
        this.userName = userName;
        this.password = password;
        this.scope = scope;
        this.http = http;
        this.configuration = configuration;
        this.serverConfiguration = serverConfiguration;
    }

    /** 使用资源所有者密码 grant（无 scope）创建 token 供应器。 */
    public TokenCallable(String userName, String password, Http http, Configuration configuration,
        ServerConfiguration serverConfiguration) {
        this(userName, password, null, http, configuration, serverConfiguration);
    }

    /** 使用客户端凭证 grant 创建 token 供应器。 */
    public TokenCallable(Http http, Configuration configuration, ServerConfiguration serverConfiguration) {
        this(null, null, http, configuration, serverConfiguration);
    }

    /** 返回有效 access token，必要时刷新或重新获取。 */
    @Override
    public String call() {
        if (tokenResponse == null) {
            tokenResponse = obtainTokens();
        }

        try {
            String rawAccessToken = tokenResponse.getToken();
            AccessToken accessToken = JsonSerialization.readValue(new JWSInput(rawAccessToken).getContent(), AccessToken.class);

            if (accessToken.isActive() && this.isTokenTimeToLiveSufficient(accessToken)) {
                return rawAccessToken;
            } else {
                log.debug("Access token is expired.");
            }
        } catch (Exception cause) {
            clearTokens();
            throw new RuntimeException("Failed to parse access token", cause);
        }

        tokenResponse = tryRefreshToken();

        return tokenResponse.getToken();
    }

    /** 尝试用 refresh token 换取新 access token，不可用时重新申请。 */
    private AccessTokenResponse tryRefreshToken() {
        String rawRefreshToken = tokenResponse.getRefreshToken();

        if (rawRefreshToken == null) {
            log.debug("Refresh token not found, obtaining new tokens");
            return obtainTokens();
        }

        try {
            RefreshToken refreshToken = JsonSerialization.readValue(new JWSInput(rawRefreshToken).getContent(), RefreshToken.class);
            if (!refreshToken.isActive() || !isTokenTimeToLiveSufficient(refreshToken)) {
                log.debug("Refresh token is expired.");
                return obtainTokens();
            }
        } catch (Exception cause) {
            clearTokens();
            throw new RuntimeException("Failed to parse refresh token", cause);
        }

        return refreshToken(rawRefreshToken);
    }

    /** 判断 token 剩余有效期是否大于配置的最小存活时间。 */
    public boolean isTokenTimeToLiveSufficient(AccessToken token) {
        return token != null && (token.getExp() - getConfiguration().getTokenMinimumTimeToLive()) > Time.currentTime();
    }

    /**
     * 使用客户端凭证 grant 获取 access token。
     *
     * @return {@link AccessTokenResponse}
     */
    AccessTokenResponse clientCredentialsGrant() {
        return this.http.<AccessTokenResponse>post(this.serverConfiguration.getTokenEndpoint())
                .authentication()
                .client()
                .response()
                .json(AccessTokenResponse.class)
                .execute();
    }

    /**
     * 使用资源所有者密码 grant 获取 access token。
     *
     * @return {@link AccessTokenResponse}
     */
    AccessTokenResponse resourceOwnerPasswordGrant(String userName, String password) {
        return resourceOwnerPasswordGrant(userName, password, null);
    }

    /** 使用资源所有者密码 grant 获取 access token，可指定 scope。 */
    AccessTokenResponse resourceOwnerPasswordGrant(String userName, String password, String scope) {
        return this.http.<AccessTokenResponse>post(this.serverConfiguration.getTokenEndpoint()).authentication()
            .oauth2ResourceOwnerPassword(userName, password, scope).response().json(AccessTokenResponse.class).execute();
    }

    /** 向 token 端点提交 refresh_token grant 刷新令牌。 */
    private AccessTokenResponse refreshToken(String rawRefreshToken) {
        log.debug("Refreshing tokens");
        return http.<AccessTokenResponse>post(serverConfiguration.getTokenEndpoint())
                .authentication().client()
                .form()
                .param("grant_type", "refresh_token")
                .param("refresh_token", rawRefreshToken)
                .response()
                .json(AccessTokenResponse.class)
                .execute();
    }

    /** 根据构造参数选择客户端凭证或密码 grant 获取新 token。 */
    private AccessTokenResponse obtainTokens() {
        if (userName == null || password == null) {
            return clientCredentialsGrant();
        } else if (scope != null) {
            return resourceOwnerPasswordGrant(userName, password, scope);
        } else {
            return resourceOwnerPasswordGrant(userName, password);
        }
    }

    /** 返回关联的 {@link Http} 实例。 */
    Http getHttp() {
        return http;
    }

    /** 是否允许在 403 时触发 token 重试（子类可覆盖）。 */
    protected boolean isRetry() {
        return true;
    }

    /** 返回客户端配置。 */
    Configuration getConfiguration() {
        return configuration;
    }

    /** 返回从 UMA 发现端点拉取的服务器配置。 */
    ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    /** 清除缓存的 token 响应，强制下次调用重新获取。 */
    void clearTokens() {
        tokenResponse = null;
    }
}
