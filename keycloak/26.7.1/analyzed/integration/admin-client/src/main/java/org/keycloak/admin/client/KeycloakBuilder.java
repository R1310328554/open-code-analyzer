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

import jakarta.ws.rs.client.Client;

import static org.keycloak.OAuth2Constants.PASSWORD;

/**
 * {@link Keycloak} 管理客户端的流式构建器，支持自定义底层
 * {@link jakarta.ws.rs.client.Client RestEasy 客户端}。
 * <p>
 * <p>连接池大小为 20 的示例：</p>
 * <pre>
 *   Keycloak keycloak = KeycloakBuilder.builder()
 *     .serverUrl("https://sso.example.com/auth")
 *     .realm("realm")
 *     .username("user")
 *     .password("pass")
 *     .clientId("client")
 *     .clientSecret("secret")
 *     .resteasyClient(ResteasyClientClassicProvider.createClientBuilder()
 *                 .connectionPoolSize(20)
 *                 .build()
 *                 .register(org.keycloak.admin.client.JacksonProvider.class, 100))
 *     .build();
 * </pre>
 * <p>使用 grant_type=client_credentials 的示例：</p>
 * <pre>
 *   Keycloak keycloak = KeycloakBuilder.builder()
 *     .serverUrl("https://sso.example.com/auth")
 *     .realm("example")
 *     .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
 *     .clientId("client")
 *     .clientSecret("secret")
 *     .build();
 * </pre>
 *
 * @author Scott Rossillo
 * @see jakarta.ws.rs.client.Client
 */
public class KeycloakBuilder {
    private String serverUrl;
    private String realm;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private Client resteasyClient;
    private String authorization;
    private String scope;
    private boolean useDPoP = false;

    /** 设置 Keycloak 服务器 URL。 */
    public KeycloakBuilder serverUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    /** 设置目标领域名称。 */
    public KeycloakBuilder realm(String realm) {
        this.realm = realm;
        return this;
    }

    /** 设置 OAuth 授权类型（须为 PASSWORD 或 CLIENT_CREDENTIALS）。 */
    public KeycloakBuilder grantType(String grantType) {
        Config.checkGrantType(grantType);
        this.grantType = grantType;
        return this;
    }

    /** 设置管理员用户名（密码授权时使用）。 */
    public KeycloakBuilder username(String username) {
        this.username = username;
        return this;
    }

    /** 设置管理员密码（密码授权时使用）。 */
    public KeycloakBuilder password(String password) {
        this.password = password;
        return this;
    }

    /** 设置 OAuth 客户端 ID。 */
    public KeycloakBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /** 设置 OAuth scope 参数。 */
    public KeycloakBuilder scope(String scope) {
        this.scope = scope;
        return this;
    }

    /** 设置 OAuth 客户端密钥。 */
    public KeycloakBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * 指定自定义 RestEasy 客户端实例，详见
     * <a href="https://www.keycloak.org/securing-apps/admin-client#_admin_client_compatibility">兼容性文档</a>。
     *
     * @param resteasyClient 自定义 RestEasy 客户端
     * @return 当前构建器实例
     */
    public KeycloakBuilder resteasyClient(Client resteasyClient) {
        this.resteasyClient = resteasyClient;
        return this;
    }

    /** 设置预置 Bearer 访问令牌，跳过自动登录。 */
    public KeycloakBuilder authorization(String auth) {
        this.authorization = auth;
        return this;
    }

    /**
     * 是否启用 DPoP 证明：为 {@code true} 时，令牌请求与管理 REST API 请求均附加 DPoP 头。
     * 服务端须启用 DPoP 功能；默认 {@code false}，适用于 Keycloak 26.4.0 及更高版本。
     *
     * @param useDPoP 是否启用 DPoP
     * @return 当前构建器实例
     */
    public KeycloakBuilder useDPoP(boolean useDPoP) {
        this.useDPoP = useDPoP;
        return this;
    }

    /**
     * 根据当前配置构建 {@link Keycloak} 实例。
     *
     * @return 配置完成的管理客户端
     * @throws IllegalStateException 缺少必填参数时抛出
     */
    public Keycloak build() {
        if (serverUrl == null) {
            throw new IllegalStateException("serverUrl required");
        }

        if (realm == null) {
            throw new IllegalStateException("realm required");
        }

        if (authorization == null && grantType == null) {
            grantType = PASSWORD;
        }

        if (PASSWORD.equals(grantType)) {
            if (username == null) {
                throw new IllegalStateException("username required");
            }

            if (password == null) {
                throw new IllegalStateException("password required");
            }
        }

        if (authorization == null && clientId == null) {
            throw new IllegalStateException("clientId required");
        }

        return new Keycloak(serverUrl, realm, username, password, clientId, clientSecret, grantType, resteasyClient, authorization, scope, useDPoP);
    }

    private KeycloakBuilder() {
    }

    /** @return 新的 {@link KeycloakBuilder} 实例 */
    public static KeycloakBuilder builder() {
        return new KeycloakBuilder();
    }
}
