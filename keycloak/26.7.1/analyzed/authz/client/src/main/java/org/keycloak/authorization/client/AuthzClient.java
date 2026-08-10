/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.client;

import java.io.IOException;
import java.io.InputStream;

import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.authorization.client.resource.AuthorizationResource;
import org.keycloak.authorization.client.resource.ProtectionResource;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.TokenCallable;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.representations.AccessTokenResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.keycloak.constants.ServiceUrlConstants.AUTHZ_DISCOVERY_URL;

/**
 * <p>访问 Keycloak 授权服务（Authorization Services）的客户端入口类。
 *
 * <p>创建实例前请确保配置中指定的 Keycloak 服务器已运行。客户端会通过 UMA 发现端点拉取服务器元数据，
 * 通常位于 <i>http(s)://{server}:{port}/auth/realms/{realm}/.well-known/uma-configuration</i>。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthzClient {

    private final Http http;
    private TokenCallable patSupplier;

    /**
     * <p>创建新实例。
     *
     * <p>要求 classpath 中存在 <code>keycloak.json</code>，否则抛出异常。
     *
     * @return 新客户端实例
     * @throws RuntimeException 当 classpath 中无 <code>keycloak.json</code> 或解析失败时
     */
    public static AuthzClient create() throws RuntimeException {
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");

        return create(configStream);
    }

    /**
     * <p>从配置输入流创建新实例。
     *
     * @param configStream 配置 JSON 输入流
     * @return 新客户端实例
     */
    public static AuthzClient create(InputStream configStream) throws RuntimeException {
        if (configStream == null) {
            throw new IllegalArgumentException("Config input stream can not be null");
        }

        try {
            ObjectMapper mapper = new ObjectMapper(new SystemPropertiesJsonParserFactory());

            mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

            return create(mapper.readValue(configStream, Configuration.class));
        } catch (IOException e) {
            throw new RuntimeException("Could not parse configuration.", e);
        }
    }

    /**
     * <p>从 {@link Configuration} 对象创建新实例。
     *
     * @param configuration 客户端配置
     * @return 新客户端实例
     */
    public static AuthzClient create(Configuration configuration) {
        CryptoIntegration.init(AuthzClient.class.getClassLoader());
        return new AuthzClient(configuration);
    }

    private final ServerConfiguration serverConfiguration;
    private final Configuration configuration;

    /**
     * <p>创建 {@link ProtectionResource}，用于访问 Protection API。
     *
     * <p>此方法会为客户端自身获取 PAT（带 {@code uma_protection} scope 的访问令牌），
     * 支持 client/secret、JWT 等凭证类型。
     *
     * @return {@link ProtectionResource} 实例
     */
    public ProtectionResource protection() {
        return new ProtectionResource(this.http, this.serverConfiguration, configuration, createPatSupplier());
    }

    /**
     * <p>使用给定访问令牌创建 {@link ProtectionResource}。
     *
     * @param accessToken PAT（带 {@code uma_protection} scope 的访问令牌）
     * @return {@link ProtectionResource} 实例
     */
    public ProtectionResource protection(final String accessToken) {
        return new ProtectionResource(this.http, this.serverConfiguration, configuration, new TokenCallable(http, configuration, serverConfiguration) {
            @Override
            public String call() {
                return accessToken;
            }

            @Override
            protected boolean isRetry() {
                return false;
            }
        });
    }

    /**
     * <p>使用资源所有者用户名/密码创建 {@link ProtectionResource}，并据此获取 PAT。
     *
     * @return {@link ProtectionResource} 实例
     */
    public ProtectionResource protection(String userName, String password) {
        patSupplier = null;
        return new ProtectionResource(this.http, this.serverConfiguration, configuration, createPatSupplier(userName, password));
    }

    /**
     * <p>创建 {@link AuthorizationResource}，用于向服务器申请权限（RPT）。
     *
     * @return {@link AuthorizationResource} 实例
     */
    public AuthorizationResource authorization() {
        return new AuthorizationResource(configuration, serverConfiguration, this.http, null);
    }

    /**
     * <p>使用指定 Bearer 访问令牌创建 {@link AuthorizationResource}。
     *
     * @param accessToken 作为 Bearer 访问令牌端点的访问令牌
     * @return {@link AuthorizationResource} 实例
     */
    public AuthorizationResource authorization(final String accessToken) {
        return new AuthorizationResource(configuration, serverConfiguration, this.http, new TokenCallable(http, configuration, serverConfiguration) {
            @Override
            public String call() {
                return accessToken;
            }

            @Override
            protected boolean isRetry() {
                return false;
            }
        });
    }

    /**
     * <p>使用资源所有者凭证创建 {@link AuthorizationResource}。
     *
     * @param userName 代表身份/访问上下文的 ID Token 或 Access Token，或用户名
     * @param password 密码
     * @return {@link AuthorizationResource} 实例
     */
    public AuthorizationResource authorization(final String userName, final String password) {
        return authorization(userName, password, null);
    }

    public AuthorizationResource authorization(final String userName, final String password, final String scope) {
        return new AuthorizationResource(configuration, serverConfiguration, this.http,
            createRefreshableAccessTokenSupplier(userName, password, scope));
    }

    /**
     * 使用客户端凭证（client credentials）获取访问令牌。
     *
     * @return {@link AccessTokenResponse}
     */
    public AccessTokenResponse obtainAccessToken() {
        return this.http.<AccessTokenResponse>post(this.serverConfiguration.getTokenEndpoint())
                .authentication()
                    .client()
                .response()
                    .json(AccessTokenResponse.class)
                .execute();
    }

    /**
     * 使用资源所有者密码凭证（ROPC）获取访问令牌。
     *
     * @return {@link AccessTokenResponse}
     */
    public AccessTokenResponse obtainAccessToken(String userName, String password) {
        return this.http.<AccessTokenResponse>post(this.serverConfiguration.getTokenEndpoint())
                .authentication()
                    .oauth2ResourceOwnerPassword(userName, password)
                .response()
                    .json(AccessTokenResponse.class)
                .execute();
    }

    /**
     * 返回自 UMA 发现端点获取的服务器 {@link ServerConfiguration}。
     *
     * @return {@link ServerConfiguration}
     */
    public ServerConfiguration getServerConfiguration() {
        return this.serverConfiguration;
    }

    /**
     * 返回本地客户端 {@link Configuration}。
     *
     * @return {@link Configuration}
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /** 私有构造：拉取 UMA 发现文档并初始化 HTTP 客户端。 */
    private AuthzClient(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Client configuration can not be null.");
        }

        String configurationUrl = configuration.getAuthServerUrl();

        if (configurationUrl == null) {
            throw new IllegalArgumentException("Configuration URL can not be null.");
        }

        configurationUrl = KeycloakUriBuilder.fromUri(configurationUrl).clone().path(AUTHZ_DISCOVERY_URL).build(configuration.getRealm()).toString();
        this.configuration = configuration;

        this.http = new Http(configuration, configuration.getClientCredentialsProvider());

        try {
            this.serverConfiguration = this.http.<ServerConfiguration>get(configurationUrl)
                    .response().json(ServerConfiguration.class)
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException("Could not obtain configuration from server [" + configurationUrl + "].", e);
        }
    }

    /** 为指定用户创建 PAT 供应器（可缓存）。 */
    public TokenCallable createPatSupplier(String userName, String password) {
        if (patSupplier == null) {
            patSupplier = createRefreshableAccessTokenSupplier(userName, password);
        }
        return patSupplier;
    }

    /** 为客户端自身创建 PAT 供应器。 */
    public TokenCallable createPatSupplier() {
        return createPatSupplier(null, null);
    }

    private TokenCallable createRefreshableAccessTokenSupplier(final String userName, final String password) {
        return createRefreshableAccessTokenSupplier(userName, password, null);
    }

    private TokenCallable createRefreshableAccessTokenSupplier(final String userName, final String password,
        final String scope) {
        return new TokenCallable(userName, password, scope, http, configuration, serverConfiguration);
    }
}
