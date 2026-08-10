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

package org.keycloak.admin.client.token;

import java.security.KeyPair;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;

import org.keycloak.admin.client.Config;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.BasicAuthFilter;
import org.keycloak.admin.client.resource.DPoPAuthFilter;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.Time;
import org.keycloak.representations.AccessTokenResponse;

import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.GRANT_TYPE;
import static org.keycloak.OAuth2Constants.PASSWORD;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;
import static org.keycloak.OAuth2Constants.SCOPE;
import static org.keycloak.OAuth2Constants.USERNAME;

/**
 * admin-client 的访问令牌生命周期管理器。
 * <p>
 * 负责获取、刷新、注销 OAuth2 访问令牌，并在令牌即将过期时自动刷新。
 * 支持密码授权、刷新令牌及 DPoP 绑定。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public class TokenManager {
    /** 默认最小有效余量（秒），在此时间内视为令牌即将过期。 */
    private static final long DEFAULT_MIN_VALIDITY = 30;

    private AccessTokenResponse currentToken;
    private long expirationTime;
    private long refreshExpirationTime;
    private long minTokenValidity = DEFAULT_MIN_VALIDITY;
    private final Config config;
    private final TokenService tokenService;
    private final String accessTokenGrantType;
    private final KeyPair dpopKeyPair;

    /**
     * 构造令牌管理器并初始化令牌服务端点。
     *
     * @param config 客户端配置
     * @param client JAX-RS 客户端实例
     */
    public TokenManager(Config config, Client client) {
        this.config = config;
        WebTarget target = client.target(config.getServerUrl());
        if (!config.isPublicClient()) {
            target.register(new BasicAuthFilter(config.getClientId(), config.getClientSecret()));
        }

        if (this.config.isUseDPoP()) {
            this.dpopKeyPair = KeyUtils.generateRsaKeyPair(2048);
            target.register(new DPoPAuthFilter(this, true));
        } else {
            this.dpopKeyPair = null;
        }

        this.tokenService = Keycloak.getClientProvider().targetProxy(target, TokenService.class);
        this.accessTokenGrantType = config.getGrantType();
    }

    /** 获取当前访问令牌的字符串形式。 */
    public String getAccessTokenString() {
        return getAccessToken().getToken();
    }

    /**
     * 获取当前有效的访问令牌，必要时自动申请或刷新。
     *
     * @return 访问令牌响应
     */
    public synchronized AccessTokenResponse getAccessToken() {
        if (currentToken == null) {
            grantToken();
        } else if (tokenExpired()) {
            refreshToken();
        }
        return currentToken;
    }

    /**
     * 使用配置的授权类型申请新令牌。
     *
     * @return 新签发的访问令牌响应
     */
    public AccessTokenResponse grantToken() {
        Form form = new Form().param(GRANT_TYPE, accessTokenGrantType);
        if (PASSWORD.equals(accessTokenGrantType)) {
            form.param(USERNAME, config.getUsername())
                .param(PASSWORD, config.getPassword());
        }

        if (config.getScope() != null) {
            form.param(SCOPE, config.getScope());
        }

        if (config.isPublicClient()) {
            form.param(CLIENT_ID, config.getClientId());
        }

        int requestTime = Time.currentTime();
        synchronized (this) {
            currentToken = tokenService.grantToken(config.getRealm(), form.asMap());
            expirationTime = requestTime + currentToken.getExpiresIn();
            refreshExpirationTime = requestTime + currentToken.getRefreshExpiresIn();
        }
        return currentToken;
    }

    /**
     * 使用刷新令牌续期访问令牌；若刷新失败则重新申请。
     *
     * @return 刷新后的访问令牌响应
     */
    public synchronized AccessTokenResponse refreshToken() {
        if (currentToken.getRefreshToken() == null || refreshTokenExpired()) {
            return grantToken();
        }

        Form form = new Form().param(GRANT_TYPE, REFRESH_TOKEN)
                              .param(REFRESH_TOKEN, currentToken.getRefreshToken());

        if (config.isPublicClient()) {
            form.param(CLIENT_ID, config.getClientId());
        }

        try {
            int requestTime = Time.currentTime();

            currentToken = tokenService.refreshToken(config.getRealm(), form.asMap());
            expirationTime = requestTime + currentToken.getExpiresIn();
            return currentToken;
        } catch (BadRequestException e) {
            return grantToken();
        }
    }

    /** 注销当前会话并使刷新令牌失效。 */
    public synchronized void logout() {
        if (currentToken == null || currentToken.getRefreshToken() == null || refreshTokenExpired()) {
            return;
        }

        Form form = new Form().param(REFRESH_TOKEN, currentToken.getRefreshToken());

        if (config.isPublicClient()) {
            form.param(CLIENT_ID, config.getClientId());
        }

        tokenService.logout(config.getRealm(), form.asMap());
        currentToken = null;
    }

    /**
     * 设置令牌被视为即将过期的最小有效余量（秒）。
     *
     * @param minTokenValidity 最小有效余量
     */
    public synchronized void setMinTokenValidity(long minTokenValidity) {
        this.minTokenValidity = minTokenValidity;
    }

    /** 判断访问令牌是否已过期或即将过期。 */
    private synchronized boolean tokenExpired() {
        return (Time.currentTime() + minTokenValidity) >= expirationTime;
    }

    /** 判断刷新令牌是否已过期或即将过期。 */
    private synchronized boolean refreshTokenExpired() { return (Time.currentTime() + minTokenValidity) >= refreshExpirationTime; }

    /**
     * 使当前令牌失效，但仅当传入令牌与当前令牌相同时才执行。
     *
     * @param token 要失效的令牌（不可为 null）
     */
    public synchronized void invalidate(String token) {
        if (currentToken == null) {
            return; // 无令牌可失效
        }
        if (token.equals(currentToken.getToken())) {
            // 下次使用时将触发刷新；若刷新失败则重新申请
            expirationTime = -1;
        }
    }

    /**
     * 获取 DPoP 密钥对；若配置未启用 DPoP 则返回 null。
     *
     * @return 已生成的 dpopKeyPair，或未启用 DPoP 时为 null
     */
    public KeyPair getDpopKeyPair() {
        return dpopKeyPair;
    }
}
