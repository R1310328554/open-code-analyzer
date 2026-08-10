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

package com.alibaba.nacos.plugin.auth.impl.oidc.constant;

/**
 * OIDC 认证插件服务端常量。
 *
 * <p>协议层常量（Discovery 字段、OAuth2 参数、HTTP 头等）定义在
 * {@link com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants}。</p>
 *
 * @author WangzJi
 */
@SuppressWarnings("PMD")
public final class OidcConstants {
    
    private OidcConstants() {
    }
    
    /**
     * 身份上下文中存储 OIDC 用户的键名。
     */
    public static final String OAUTH2_USER_KEY = "oidc_user";
    
    // ==================== 配置项键名 ====================
    
    /**
     * OIDC 插件配置前缀。
     */
    public static final String CONFIG_PREFIX = "nacos.core.auth.plugin.oidc.";
    
    /**
     * OIDC Issuer URI（用于自动 Discovery）。
     */
    public static final String CONFIG_ISSUER_URI = CONFIG_PREFIX + "issuer-uri";
    
    /**
     * OIDC 客户端 ID。
     */
    public static final String CONFIG_CLIENT_ID = CONFIG_PREFIX + "client-id";
    
    /**
     * OIDC 客户端密钥。
     */
    public static final String CONFIG_CLIENT_SECRET = CONFIG_PREFIX + "client-secret";
    
    /**
     * OIDC 授权范围（scope）。
     */
    public static final String CONFIG_SCOPE = CONFIG_PREFIX + "scope";
    
    /**
     * 令牌校验方式：jwt 或 introspection。
     */
    public static final String CONFIG_TOKEN_VALIDATION_METHOD =
        CONFIG_PREFIX + "token-validation-method";
    
    /**
     * JWKS 缓存 TTL（秒）。
     */
    public static final String CONFIG_JWKS_CACHE_TTL = CONFIG_PREFIX + "jwks-cache-ttl-seconds";
    
    /**
     * ID Token 中用作用户名的 claim 名称。
     */
    public static final String CONFIG_USERNAME_CLAIM = CONFIG_PREFIX + "username-claim";
    
    /**
     * ID Token 中用作角色的 claim 名称。
     */
    public static final String CONFIG_ROLES_CLAIM = CONFIG_PREFIX + "roles-claim";
    
    /**
     * 全局管理员角色名称（在 OIDC claim 中匹配）。
     */
    public static final String CONFIG_ADMIN_ROLE = CONFIG_PREFIX + "admin-role";
    
    /**
     * 首次登录时是否自动创建用户。
     */
    public static final String CONFIG_AUTO_CREATE_USER = CONFIG_PREFIX + "auto-create-user";
    
    /**
     * 外部授权评估端点（权限决策由 IdP 完成）。
     */
    public static final String CONFIG_AUTHORIZATION_ENDPOINT =
        CONFIG_PREFIX + "authorization-endpoint";
    
    /**
     * 授权请求超时时间（毫秒）。
     */
    public static final String CONFIG_AUTHORIZATION_TIMEOUT_MS =
        CONFIG_PREFIX + "authorization-timeout-ms";
    
    /**
     * 是否启用严格 nonce 校验。
     */
    public static final String CONFIG_STRICT_NONCE_VALIDATION =
        CONFIG_PREFIX + "strict-nonce-validation";
    
    /**
     * 是否启用严格 audience 校验。
     */
    public static final String CONFIG_STRICT_AUDIENCE_VALIDATION =
        CONFIG_PREFIX + "strict-audience-validation";
    
    // ==================== 默认值 ====================
    
    /**
     * 默认令牌校验方式（jwt）。
     */
    public static final String DEFAULT_TOKEN_VALIDATION_METHOD = "jwt";
    
    /**
     * 默认 JWKS 缓存 TTL：1 小时。
     */
    public static final long DEFAULT_JWKS_CACHE_TTL_SECONDS = 3600L;
    
    /**
     * 默认用户名 claim（preferred_username）。
     */
    public static final String DEFAULT_USERNAME_CLAIM = "preferred_username";
    
    /**
     * 默认角色 claim（roles）。
     */
    public static final String DEFAULT_ROLES_CLAIM = "roles";
    
    /**
     * 默认全局管理员角色名（nacos-admin）。
     */
    public static final String DEFAULT_ADMIN_ROLE = "nacos-admin";
    
    /**
     * 默认 OIDC scope（openid profile email）。
     */
    public static final String DEFAULT_SCOPE = "openid profile email";
    
    /**
     * 默认授权请求超时：5 秒。
     */
    public static final long DEFAULT_AUTHORIZATION_TIMEOUT_MS = 5000L;
    
    // ==================== HTTP 状态码（服务端专用） ====================
    
    /**
     * HTTP 401 未授权状态码。
     */
    public static final int HTTP_STATUS_UNAUTHORIZED = 401;
    
    /**
     * HTTP 403 禁止访问状态码。
     */
    public static final int HTTP_STATUS_FORBIDDEN = 403;
    
    // ==================== HTTP 常量（服务端专用） ====================
    
    /**
     * HTTP 协议前缀。
     */
    public static final String HTTP_PROTOCOL = "http";
    
    /**
     * HTTPS 协议前缀。
     */
    public static final String HTTPS_PROTOCOL = "https";
    
    /**
     * 默认 HTTP 端口（80）。
     */
    public static final int DEFAULT_HTTP_PORT = 80;
    
    /**
     * 默认 HTTPS 端口（443）。
     */
    public static final int DEFAULT_HTTPS_PORT = 443;
    
    /**
     * URL 查询字符串分隔符（?）。
     */
    public static final String QUERY_STRING_SEPARATOR = "?";
    
    // ==================== JSON 字段名 ====================
    
    /**
     * JSON 字段名：allowed（是否允许）。
     */
    public static final String JSON_FIELD_ALLOWED = "\"allowed\"";
    
    /**
     * JSON 字段名：result（Keycloak 授权结果）。
     */
    public static final String JSON_FIELD_RESULT = "\"result\"";
    
    /**
     * JSON 字段名：decision（授权决策）。
     */
    public static final String JSON_FIELD_DECISION = "\"decision\"";
}
