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

package com.alibaba.nacos.plugin.auth.constant;

/**
 * OIDC/OAuth2 协议层共享常量。
 *
 * <p>包含服务端与客户端 OIDC 实现共用的常量，涵盖 OIDC Discovery 字段、
 * HTTP 请求头、OAuth2 令牌请求/响应参数等。</p>
 *
 * @author wangzji
 */
public final class OidcProtocolConstants {
    
    private OidcProtocolConstants() {
    }
    
    // ===== OIDC Discovery =====
    
    /** OIDC Discovery 端点路径。 */
    public static final String WELL_KNOWN_PATH = "/.well-known/openid-configuration";
    
    /** Discovery 响应中的 token 端点字段名。 */
    public static final String DISCOVERY_TOKEN_ENDPOINT = "token_endpoint";
    
    /** Discovery 响应中的 JWKS URI 字段名。 */
    public static final String DISCOVERY_JWKS_URI = "jwks_uri";
    
    /** Discovery 响应中的授权端点字段名。 */
    public static final String DISCOVERY_AUTHORIZATION_ENDPOINT = "authorization_endpoint";
    
    /** Discovery 响应中的用户信息端点字段名。 */
    public static final String DISCOVERY_USERINFO_ENDPOINT = "userinfo_endpoint";
    
    /** Discovery 响应中的登出端点字段名。 */
    public static final String DISCOVERY_END_SESSION_ENDPOINT = "end_session_endpoint";
    
    // ===== HTTP Headers & Auth =====
    
    /** Authorization 请求头名称。 */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    /** Bearer 令牌前缀。 */
    public static final String BEARER_PREFIX = "Bearer ";
    
    /** 访问令牌参数名。 */
    public static final String ACCESS_TOKEN_PARAM = "accessToken";
    
    /** OIDC 认证插件类型标识。 */
    public static final String AUTH_PLUGIN_TYPE = "oidc";
    
    // ===== HTTP =====
    
    /** HTTP 200 成功状态码。 */
    public static final int HTTP_STATUS_OK = 200;
    
    /** JSON 内容类型。 */
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    /** 表单 URL 编码内容类型。 */
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    
    /** 默认连接超时（毫秒）。 */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    
    /** 默认读取超时（毫秒）。 */
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;
    
    // ===== OAuth2 Token Request =====
    
    /** OAuth2 grant_type 参数名。 */
    public static final String GRANT_TYPE = "grant_type";
    
    /** client_credentials 授权类型值。 */
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    
    /** client_id 参数名。 */
    public static final String PARAM_CLIENT_ID = "client_id";
    
    /** client_secret 参数名。 */
    public static final String PARAM_CLIENT_SECRET = "client_secret";
    
    /** scope 参数名。 */
    public static final String PARAM_SCOPE = "scope";
    
    // ===== Token Response =====
    
    /** 令牌响应中的 access_token 字段名。 */
    public static final String TOKEN_RESPONSE_ACCESS_TOKEN = "access_token";
    
    /** 令牌响应中的 expires_in 字段名。 */
    public static final String TOKEN_RESPONSE_EXPIRES_IN = "expires_in";
    
    /** 令牌响应中的 token_type 字段名。 */
    public static final String TOKEN_RESPONSE_TOKEN_TYPE = "token_type";
}
