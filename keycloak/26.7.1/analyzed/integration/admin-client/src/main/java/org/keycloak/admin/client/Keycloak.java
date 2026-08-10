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

import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;
import javax.net.ssl.SSLContext;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;

import org.keycloak.admin.client.resource.BearerAuthFilter;
import org.keycloak.admin.client.resource.DPoPAuthFilter;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.ServerInfoResource;
import org.keycloak.admin.client.spi.ResteasyClientProvider;
import org.keycloak.admin.client.token.TokenManager;

import static org.keycloak.OAuth2Constants.PASSWORD;

/**
 * Keycloak 管理 REST API 客户端入口。
 * <p>
 * 默认使用 RestEasy 客户端构建器的默认设置；如需自定义底层 HTTP 客户端，
 * 请通过 {@link KeycloakBuilder} 创建实例。
 * <p>
 * 解析创建类响应时，可使用 {@link CreatedResponseUtil} 提取新资源 ID。
 *
 * @author rodrigo.sasaki@icarros.com.br
 * @see KeycloakBuilder
 */
public class Keycloak implements AutoCloseable {

    private static volatile ResteasyClientProvider CLIENT_PROVIDER = resolveResteasyClientProvider();

    /** 通过 SPI 解析 {@link ResteasyClientProvider} 实现，若无则回退到经典实现。 */
    private static ResteasyClientProvider resolveResteasyClientProvider() {
        Iterator<ResteasyClientProvider> providers = ServiceLoader.load(ResteasyClientProvider.class).iterator();

        if (providers.hasNext()) {
            ResteasyClientProvider provider = providers.next();

            if (providers.hasNext()) {
                throw new IllegalArgumentException("Multiple " + ResteasyClientProvider.class + " implementations found");
            }

            return provider;
        }

        return createDefaultResteasyClientProvider();
    }

    /** 加载默认的经典 RestEasy 客户端提供程序。 */
    private static ResteasyClientProvider createDefaultResteasyClientProvider() {
        try {
            return (ResteasyClientProvider) Keycloak.class.getClassLoader().loadClass("org.keycloak.admin.client.spi.ResteasyClientClassicProvider").getDeclaredConstructor().newInstance();
        } catch (Exception cause) {
            throw new RuntimeException("Could not instantiate default client provider", cause);
        }
    }

    /** 设置全局 RestEasy 客户端提供程序（通常用于测试或自定义实现）。 */
    public static void setClientProvider(ResteasyClientProvider provider) {
        CLIENT_PROVIDER = provider;
    }

    /** @return 当前使用的 RestEasy 客户端提供程序 */
    public static ResteasyClientProvider getClientProvider() {
        return CLIENT_PROVIDER;
    }

    private final Config config;
    private final TokenManager tokenManager;
    private final String authToken;
    private final WebTarget target;
    private final Client client;
    private boolean closed = false;

    Keycloak(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, String grantType, Client resteasyClient, String authtoken, String scope, boolean useDPoP) {
        config = new Config(serverUrl, realm, username, password, clientId, clientSecret, grantType, scope);
        config.setUseDPoP(useDPoP);
        client = resteasyClient != null ? resteasyClient : newRestEasyClient(null, null, false);
        authToken = authtoken;
        tokenManager = authtoken == null ? new TokenManager(config, client) : null;

        target = client.target(config.getServerUrl());
        target.register(newAuthFilter());
    }

    private static Client newRestEasyClient(Object customJacksonProvider, SSLContext sslContext, boolean disableTrustManager) {
        return CLIENT_PROVIDER.newRestEasyClient(customJacksonProvider, sslContext, disableTrustManager);
    }

    /** 根据配置创建 Bearer 或 DPoP 认证过滤器。 */
    private ClientRequestFilter newAuthFilter() {
        if (config.isUseDPoP()) {
            if (authToken != null) throw new IllegalArgumentException("Not supported to require DPoP when token is provisioned");
            return new DPoPAuthFilter(tokenManager, false);
        }
        return authToken != null ? new BearerAuthFilter(authToken) : new BearerAuthFilter(tokenManager);
    }

    /**
     * 创建用于调用 Keycloak 管理 REST API 的 Java 客户端实例。
     *
     * @param serverUrl Keycloak 服务器 URL
     * @param realm 领域名称
     * @param username 管理员用户名
     * @param password 管理员密码
     * @param clientId 客户端 ID
     * @param clientSecret 客户端密钥；公共客户端可传 {@code null}
     * @param sslContext SSL 上下文；{@code null} 时使用默认上下文
     * @param customJacksonProvider 自定义 Jackson 提供程序；{@code null} 时由客户端自动提供，详见
     *        <a href="https://www.keycloak.org/securing-apps/admin-client#_admin_client_compatibility">兼容性文档</a>
     * @param disableTrustManager 是否禁用 SSL 信任管理器；默认 {@code false}，仅开发环境使用
     * @param authToken 预置访问令牌；{@code null} 时由客户端自行登录管理会话
     * @param scope 自定义 OAuth scope；{@code null} 时使用默认值
     * @return 配置完成的 {@link Keycloak} 实例
     */
    public static Keycloak getInstance(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, SSLContext sslContext, Object customJacksonProvider, boolean disableTrustManager, String authToken, String scope) {
        return new Keycloak(serverUrl, realm, username, password, clientId, clientSecret, PASSWORD, newRestEasyClient(customJacksonProvider, sslContext, disableTrustManager), authToken, scope, false);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, SSLContext sslContext, Object customJacksonProvider, boolean disableTrustManager, String authToken) {
        return new Keycloak(serverUrl, realm, username, password, clientId, clientSecret, PASSWORD, newRestEasyClient(customJacksonProvider, sslContext, disableTrustManager), authToken, null, false);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String username, String password, String clientId, String clientSecret) {
        return getInstance(serverUrl, realm, username, password, clientId, clientSecret, null, null, false, null);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, SSLContext sslContext) {
        return getInstance(serverUrl, realm, username, password, clientId, clientSecret, sslContext, null, false, null);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, SSLContext sslContext, Object customJacksonProvider) {
        return getInstance(serverUrl, realm, username, password, clientId, clientSecret, sslContext, customJacksonProvider, false, null);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String username, String password, String clientId) {
        return getInstance(serverUrl, realm, username, password, clientId, null, null, null, false, null);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String username, String password, String clientId, SSLContext sslContext) {
        return getInstance(serverUrl, realm, username, password, clientId, null, sslContext, null, false, null);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String clientId, String authToken) {
        return getInstance(serverUrl, realm, null, null, clientId, null, null, null, false, authToken);
    }

    /**
     * 参见 {@link #getInstance(String, String, String, String, String, String, SSLContext, Object, boolean, String, String)} 了解参数及默认值。
     */
    public static Keycloak getInstance(String serverUrl, String realm, String clientId, String authToken, SSLContext sllSslContext) {
        return getInstance(serverUrl, realm, null, null, clientId, null, sllSslContext, null, false, authToken);
    }

    /** @return 所有领域的管理资源代理 */
    public RealmsResource realms() {
        return CLIENT_PROVIDER.targetProxy(target, RealmsResource.class);
    }

    /** @return 指定领域的管理资源代理 */
    public RealmResource realm(String realmName) {
        return realms().realm(realmName);
    }

    /** @return 服务器信息资源代理 */
    public ServerInfoResource serverInfo() {
        return CLIENT_PROVIDER.targetProxy(target, ServerInfoResource.class);
    }

    /** @return 令牌管理器，用于获取、刷新与注销访问令牌 */
    public TokenManager tokenManager() {
        return tokenManager;
    }

    /**
     * 基于绝对 URI 创建带认证头的安全代理。
     *
     * @param proxyClass 代理接口类型
     * @param absoluteURI 目标资源的绝对 URI
     * @param <T> 代理类型
     * @return 已注册认证过滤器的 JAX-RS 代理
     */
    public <T> T proxy(Class<T> proxyClass, URI absoluteURI) {
        WebTarget register = client.target(absoluteURI).register(newAuthFilter());
        return CLIENT_PROVIDER.targetProxy(register, proxyClass);
    }
    
    /**
     * 创建指向 Keycloak 服务器根地址的安全代理。
     *
     * @param proxyClass 代理接口类型
     * @param <T> 代理类型
     * @return 已注册认证过滤器的 JAX-RS 代理
     */
    public <T> T proxy(Class<T> proxyClass) {
        return CLIENT_PROVIDER.targetProxy(target, proxyClass);
    }
    
    /**
     * 关闭底层 HTTP 客户端。调用后此实例不可复用。
     */
    @Override
    public void close() {
        closed = true;
        if (tokenManager != null) {
            try {
                tokenManager.logout();
            } catch (RuntimeException e) {
                // 尽力关闭会话；注销可能因共享客户端已关闭、领域/客户端被禁用等原因失败
            }
        }
        client.close();
    }

    /** @return 底层客户端是否已关闭 */
    public boolean isClosed() {
        return closed;
    }

}
