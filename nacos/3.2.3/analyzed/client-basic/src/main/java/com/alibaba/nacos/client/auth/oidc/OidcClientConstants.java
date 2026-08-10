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

package com.alibaba.nacos.client.auth.oidc;

/**
 * Client-specific constants for OIDC client authentication.
 * <p>OIDC 客户端鉴权专用配置键与默认值；协议层常量见 {@link com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants}。</p>
 *
 * <p>Protocol-level constants (Discovery fields, OAuth2 parameters, HTTP headers, etc.)
 * are defined in {@link com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants}.
 *
 * @author wangzji
 */
public final class OidcClientConstants {
    
    private OidcClientConstants() {
    }
    
    // ----- 客户端 Properties 配置键 -----
    
    /**
     * OIDC Issuer URI，用于拼接 {@code .well-known/openid-configuration} 做 Discovery。
     */
    public static final String PROP_ISSUER_URI = "nacos.client.auth.oidc.issuer-uri";
    
    /**
     * OAuth2 Client Credentials 模式的 client-id。
     */
    public static final String PROP_CLIENT_ID = "nacos.client.auth.oidc.client-id";
    
    /**
     * OAuth2 Client Credentials 模式的 client-secret。
     */
    public static final String PROP_CLIENT_SECRET = "nacos.client.auth.oidc.client-secret";
    
    /**
     * OAuth2 授权范围，默认 {@link #DEFAULT_SCOPE}（openid）。
     */
    public static final String PROP_SCOPE = "nacos.client.auth.oidc.scope";
    
    /**
     * 令牌端点直配项；非空时跳过 Discovery 直接使用该 URL。
     */
    public static final String PROP_TOKEN_ENDPOINT = "nacos.client.auth.oidc.token-endpoint";
    
    // ----- 默认值 -----
    
    /** 未显式配置 scope 时的默认值 */
    public static final String DEFAULT_SCOPE = "openid";
}
