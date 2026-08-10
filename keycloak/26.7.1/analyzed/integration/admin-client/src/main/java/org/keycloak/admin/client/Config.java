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

package org.keycloak.admin.client;

import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;
import static org.keycloak.OAuth2Constants.PASSWORD;

/**
 * Keycloak 管理客户端的连接与认证配置。
 * <p>
 * 封装服务器地址、领域、凭据、授权类型及 DPoP 等参数，
 * 供 {@link Keycloak} 与 {@link KeycloakBuilder} 使用。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public class Config {

    private String serverUrl;
    private String realm;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String scope;
    private boolean useDPoP = false;

    /**
     * 使用密码授权类型（{@link org.keycloak.OAuth2Constants#PASSWORD}）构造配置。
     */
    public Config(String serverUrl, String realm, String username, String password, String clientId, String clientSecret) {
        this(serverUrl, realm, username, password, clientId, clientSecret, PASSWORD, null);
    }

    /**
     * 构造完整配置，指定授权类型与 scope。
     *
     * @param grantType 授权类型，仅支持 {@link org.keycloak.OAuth2Constants#PASSWORD} 与
     *                  {@link org.keycloak.OAuth2Constants#CLIENT_CREDENTIALS}
     */
    public Config(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, String grantType, String scope) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        checkGrantType(grantType);
        this.scope = scope;
    }

    /** @return Keycloak 服务器 URL */
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /** @return 目标领域名称 */
    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    /** @return 管理员用户名（密码授权时使用） */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /** @return 管理员密码（密码授权时使用） */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /** @return OAuth 客户端 ID */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return OAuth 客户端密钥；公共客户端时为 {@code null} */
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /** @return 是否为公共客户端（无 clientSecret） */
    public boolean isPublicClient() {
        return clientSecret == null;
    }

    /** @return OAuth scope 参数 */
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /** @return 当前授权类型 */
    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
        checkGrantType(grantType);
    }

    /**
     * 校验授权类型是否受支持。
     *
     * @param grantType 待校验的授权类型
     * @throws IllegalArgumentException 若类型不为 {@code null} 且非 PASSWORD 或 CLIENT_CREDENTIALS
     */
    public static void checkGrantType(String grantType) {
        if (grantType != null && !PASSWORD.equals(grantType) && !CLIENT_CREDENTIALS.equals(grantType)) {
            throw new IllegalArgumentException("Unsupported grantType: " + grantType +
                    " (only " + PASSWORD + " and " + CLIENT_CREDENTIALS + " are supported)");
        }
    }

    /** @return 是否启用 DPoP 证明 */
    public boolean isUseDPoP() {
        return useDPoP;
    }

    public void setUseDPoP(boolean useDPoP) {
        this.useDPoP = useDPoP;
    }
}
