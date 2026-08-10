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

package org.keycloak.admin.client.resource;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;

import org.keycloak.admin.client.token.TokenManager;

/**
 * Bearer 令牌认证请求/响应过滤器。
 * <p>
 * 出站请求附加 {@code Authorization: Bearer ...} 头；收到 401 响应时，
 * 若使用 {@link TokenManager}，则使对应令牌失效以便下次请求刷新。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public class BearerAuthFilter implements ClientRequestFilter, ClientResponseFilter {

    /** Bearer 认证头前缀。 */
    public static final String AUTH_HEADER_PREFIX = "Bearer ";
    private final String tokenString;
    protected final TokenManager tokenManager;

    /**
     * 使用固定令牌字符串构造过滤器（不自动刷新）。
     *
     * @param tokenString 访问令牌
     */
    public BearerAuthFilter(String tokenString) {
        this.tokenString = tokenString;
        this.tokenManager = null;
    }

    /**
     * 使用 {@link TokenManager} 构造过滤器，支持自动获取与刷新令牌。
     *
     * @param tokenManager 令牌管理器
     */
    public BearerAuthFilter(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.tokenString = null;
    }


    /**
     * 为出站请求添加 Bearer 认证头。
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String authHeader = (tokenManager != null ? tokenManager.getAccessTokenString() : tokenString);
        if (!authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            authHeader = AUTH_HEADER_PREFIX + authHeader;
        }
        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
    }

    /**
     * 收到 401 响应时，使请求中使用的令牌在 {@link TokenManager} 中失效。
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (responseContext.getStatus() == 401 && tokenManager != null) {
            List<Object> authHeaders = requestContext.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null) {
                return;
            }
            for (Object authHeader : authHeaders) {
                if (authHeader instanceof String) {
                    String headerValue = (String) authHeader;
                    String authHeaderPrefix = getAuthHeaderPrefix();
                    if (headerValue.startsWith(authHeaderPrefix)) {
                        String token = headerValue.substring( authHeaderPrefix.length() );
                        tokenManager.invalidate( token );
                    }
                }
            }
        }
    }

    /** @return 认证头前缀，子类可覆盖以支持 DPoP 等变体 */
    protected String getAuthHeaderPrefix() {
        return AUTH_HEADER_PREFIX;
    }
}
