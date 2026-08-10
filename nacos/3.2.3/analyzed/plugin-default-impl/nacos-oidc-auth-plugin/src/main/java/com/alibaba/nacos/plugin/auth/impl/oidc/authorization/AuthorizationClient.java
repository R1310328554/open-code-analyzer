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

package com.alibaba.nacos.plugin.auth.impl.oidc.authorization;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants;
import com.alibaba.nacos.plugin.auth.impl.oidc.config.OidcAuthConfig;
import com.alibaba.nacos.plugin.auth.impl.oidc.constant.OidcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 外部 IdP 授权端点 HTTP 客户端。
 *
 * <p>所有授权决策均委托 IdP 完成，Nacos 仅转发请求并解析响应。</p>
 *
 * @author WangzJi
 */
public class AuthorizationClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationClient.class);
    
    private static volatile AuthorizationClient instance;
    
    private final OidcAuthConfig config;
    
    private final HttpClient httpClient;
    
    private AuthorizationClient() {
        this.config = OidcAuthConfig.getInstance();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(config.getAuthorizationTimeoutMs()))
            .build();
    }
    
    /**
     * 获取单例实例。
     *
     * @return AuthorizationClient 实例
     */
    public static AuthorizationClient getInstance() {
        if (instance == null) {
            synchronized (AuthorizationClient.class) {
                if (instance == null) {
                    instance = new AuthorizationClient();
                }
            }
        }
        return instance;
    }
    
    /**
     * 向 IdP 授权端点发起权限校验请求。
     *
     * <p>未配置授权端点时默认放行全部访问（并输出警告日志）。</p>
     *
     * @param request 授权请求体
     * @return IdP 返回的 {@link AuthorizationResponse}
     */
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        String authzEndpoint = config.getAuthorizationEvaluateEndpoint();
        
        if (StringUtils.isBlank(authzEndpoint)) {
            LOGGER.warn("Authorization endpoint not configured. DEFAULTING TO ALLOW ALL ACCESS. "
                + "Configure 'nacos.auth.oidc.authorization.endpoint' for external authorization.");
            return AuthorizationResponse.allowed();
        }
        
        try {
            LOGGER.debug("Calling IdP authorization endpoint: {}", authzEndpoint);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(authzEndpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(request.toJson()))
                .timeout(Duration.ofMillis(config.getAuthorizationTimeoutMs()))
                .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == OidcProtocolConstants.HTTP_STATUS_OK) {
                AuthorizationResponse authzResponse =
                    AuthorizationResponse.fromJson(response.body());
                LOGGER.debug("IdP authorization response: {}", authzResponse);
                return authzResponse;
            } else if (response.statusCode() == OidcConstants.HTTP_STATUS_UNAUTHORIZED
                || response.statusCode() == OidcConstants.HTTP_STATUS_FORBIDDEN) {
                LOGGER.debug("IdP denied authorization: status={}", response.statusCode());
                return AuthorizationResponse.denied("Access denied by IdP");
            } else {
                LOGGER.warn("IdP authorization endpoint returned unexpected status: {}",
                    response.statusCode());
                return AuthorizationResponse.denied("Authorization service error");
            }
            
        } catch (IOException e) {
            LOGGER.error("Failed to call IdP authorization endpoint: {}", e.getMessage());
            return AuthorizationResponse.denied("Authorization service unavailable");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Authorization request interrupted");
            return AuthorizationResponse.denied("Authorization request interrupted");
        } catch (Exception e) {
            LOGGER.error("Authorization error", e);
            return AuthorizationResponse.denied("Authorization error: " + e.getMessage());
        }
    }
    
    /**
     * 便捷方法：携带令牌、资源与操作发起授权校验。
     *
     * @param token    用户访问令牌
     * @param resource 资源标识
     * @param action   操作类型
     * @return IdP 允许时返回 true
     */
    public boolean isAuthorized(String token, String resource, String action) {
        AuthorizationRequest request = AuthorizationRequest.builder()
            .token(token)
            .resource(resource)
            .action(action)
            .build();
        
        AuthorizationResponse response = authorize(request);
        return response.isAllowed();
    }
}
