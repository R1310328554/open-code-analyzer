/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.auth.impl.oidc.token;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants;
import com.alibaba.nacos.plugin.auth.impl.oidc.config.OidcAuthConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.jwk.JWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 从 OIDC 身份提供方拉取并缓存 JWKS（JSON Web Key Set）的提供者。
 *
 * <p>支持通过配置直接指定 JWKS 端点，或从 Issuer 的 well-known 发现文档自动解析。
 * 拉取到的公钥集合供 {@link JwtTokenValidator} 校验 JWT 签名时使用。</p>
 *
 * @author WangzJi
 */
public class JwksProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JwksProvider.class);
    
    /** JWKS 缓存在 Caffeine 中使用的固定键。 */
    private static final String CACHE_KEY = "jwks";
    
    /** 单例实例，采用双重检查锁定懒加载。 */
    private static volatile JwksProvider instance;
    
    /** OIDC 认证相关配置。 */
    private final OidcAuthConfig config;
    
    /** 用于拉取 JWKS 与 OIDC 发现文档的 HTTP 客户端。 */
    private final HttpClient httpClient;
    
    /** JWKS 本地缓存，过期时间由配置项控制。 */
    private final Cache<String, JWKSet> jwksCache;
    
    /** 已解析或已配置的 JWKS 端点 URI，发现成功后会被缓存。 */
    private volatile String jwksUri;
    
    private JwksProvider() {
        this.config = OidcAuthConfig.getInstance();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.jwksCache = Caffeine.newBuilder()
            .expireAfterWrite(config.getJwksCacheTtlSeconds(), TimeUnit.SECONDS)
            .maximumSize(1)
            .build();
    }
    
    /**
     * 获取单例实例。
     *
     * @return JwksProvider 实例
     */
    public static JwksProvider getInstance() {
        if (instance == null) {
            synchronized (JwksProvider.class) {
                if (instance == null) {
                    instance = new JwksProvider();
                }
            }
        }
        return instance;
    }
    
    /**
     * 从缓存读取 JWKS，缓存未命中时从提供方拉取并写入缓存。
     *
     * @return JWKSet 公钥集合
     * @throws IOException 拉取或解析失败时抛出
     */
    public JWKSet getJwkSet() throws IOException {
        JWKSet cached = jwksCache.getIfPresent(CACHE_KEY);
        if (cached != null) {
            return cached;
        }
        
        synchronized (this) {
            cached = jwksCache.getIfPresent(CACHE_KEY);
            if (cached != null) {
                return cached;
            }
            
            JWKSet jwkSet = fetchJwkSet();
            jwksCache.put(CACHE_KEY, jwkSet);
            return jwkSet;
        }
    }
    
    /**
     * 强制刷新 JWKS 缓存并重新拉取。
     *
     * <p>适用于 IdP 密钥轮换导致签名验证失败的场景。</p>
     *
     * @return 刷新后的 JWKSet
     * @throws IOException 拉取或解析失败时抛出
     */
    public JWKSet refreshJwkSet() throws IOException {
        jwksCache.invalidateAll();
        return getJwkSet();
    }
    
    /**
     * 从 JWKS 端点 HTTP 拉取公钥集合。
     *
     * @return JWKSet 公钥集合
     * @throws IOException 端点未配置、HTTP 失败或解析异常时抛出
     */
    private JWKSet fetchJwkSet() throws IOException {
        String uri = getJwksUri();
        if (StringUtils.isBlank(uri)) {
            throw new IOException("JWKS URI is not configured or discovered");
        }
        
        LOGGER.info("Fetching JWKS from: {}", uri);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();
            
            HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != OidcProtocolConstants.HTTP_STATUS_OK) {
                throw new IOException("Failed to fetch JWKS, status: " + response.statusCode());
            }
            
            JWKSet jwkSet = JWKSet.parse(response.body());
            LOGGER.info("Successfully fetched JWKS with {} keys", jwkSet.getKeys().size());
            return jwkSet;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("JWKS fetch interrupted", e);
        } catch (ParseException e) {
            throw new IOException("Failed to parse JWKS", e);
        }
    }
    
    /**
     * 获取 JWKS 端点 URI，必要时通过 OIDC 发现流程解析。
     *
     * @return JWKS 端点 URI
     * @throws IOException 发现或配置缺失时抛出
     */
    private String getJwksUri() throws IOException {
        // 优先使用已发现或已缓存的 URI
        if (StringUtils.isNotBlank(jwksUri)) {
            return jwksUri;
        }
        
        // 其次读取静态配置
        String configuredJwksUri = config.getJwksUri();
        if (StringUtils.isNotBlank(configuredJwksUri)) {
            this.jwksUri = configuredJwksUri;
            return jwksUri;
        }
        
        // 最后通过 Issuer well-known 端点自动发现
        String issuerUri = config.getIssuerUri();
        if (StringUtils.isBlank(issuerUri)) {
            throw new IOException("Issuer URI is not configured");
        }
        
        discoverOidcConfiguration(issuerUri);
        return jwksUri;
    }
    
    /**
     * 从 OIDC well-known 发现端点拉取并解析配置。
     *
     * <p>解析成功后会把 jwks_uri 及各 OAuth 端点写回 {@link OidcAuthConfig}。</p>
     *
     * @param issuerUri OIDC Issuer URI
     * @throws IOException 发现请求失败或响应解析异常时抛出
     */
    private void discoverOidcConfiguration(String issuerUri) throws IOException {
        String discoveryUrl = issuerUri.endsWith("/")
            ? issuerUri + ".well-known/openid-configuration"
            : issuerUri + OidcProtocolConstants.WELL_KNOWN_PATH;
        
        LOGGER.info("Discovering OIDC configuration from: {}", discoveryUrl);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discoveryUrl))
                .header("Accept", "application/json")
                .GET()
                .build();
            
            HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != OidcProtocolConstants.HTTP_STATUS_OK) {
                throw new IOException(
                    "Failed to discover OIDC configuration, status: " + response.statusCode());
            }
            
            JsonNode root = JacksonUtils.toObj(response.body());
            if (root != null) {
                if (root.has(OidcProtocolConstants.DISCOVERY_JWKS_URI)) {
                    this.jwksUri = root.get(OidcProtocolConstants.DISCOVERY_JWKS_URI).asText();
                    config.setJwksUri(jwksUri);
                }
                if (root.has(OidcProtocolConstants.DISCOVERY_AUTHORIZATION_ENDPOINT)) {
                    config.setAuthorizationEndpoint(
                        root.get(OidcProtocolConstants.DISCOVERY_AUTHORIZATION_ENDPOINT).asText());
                }
                if (root.has(OidcProtocolConstants.DISCOVERY_TOKEN_ENDPOINT)) {
                    config.setTokenEndpoint(
                        root.get(OidcProtocolConstants.DISCOVERY_TOKEN_ENDPOINT).asText());
                }
                if (root.has(OidcProtocolConstants.DISCOVERY_USERINFO_ENDPOINT)) {
                    config.setUserinfoEndpoint(
                        root.get(OidcProtocolConstants.DISCOVERY_USERINFO_ENDPOINT).asText());
                }
                if (root.has(OidcProtocolConstants.DISCOVERY_END_SESSION_ENDPOINT)) {
                    config.setEndSessionEndpoint(
                        root.get(OidcProtocolConstants.DISCOVERY_END_SESSION_ENDPOINT).asText());
                }
            }
            
            LOGGER.info("OIDC configuration discovered: jwksUri={}", jwksUri);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("OIDC discovery interrupted", e);
        } catch (Exception e) {
            LOGGER.error("Failed to parse OIDC configuration", e);
            throw new IOException("Failed to parse OIDC configuration", e);
        }
    }
    
    /**
     * 清空 JWKS 缓存及已解析的端点 URI。
     */
    public void clearCache() {
        jwksCache.invalidateAll();
        jwksUri = null;
    }
}
