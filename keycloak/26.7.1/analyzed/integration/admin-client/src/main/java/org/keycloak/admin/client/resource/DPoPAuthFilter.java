/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.HttpHeaders;

import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.util.DPoPGenerator;

import static org.keycloak.OAuth2Constants.DPOP_HTTP_HEADER;

/**
 * DPoP（Demonstrating Proof-of-Possession）认证请求过滤器。
 * <p>
 * 在 {@link BearerAuthFilter} 基础上，为出站请求附加 RSA 签名的 DPoP 证明头；
 * 令牌端点请求与常规 Admin REST 请求采用不同的证明生成策略。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DPoPAuthFilter extends BearerAuthFilter {

    /** 是否为令牌端点请求模式。 */
    private final boolean tokenRequest;

    /**
     * 构造 DPoP 认证过滤器。
     *
     * @param tokenManager 令牌管理器，提供访问令牌与 DPoP 密钥对
     * @param tokenRequest {@code true} 表示仅对 {@code /token} 端点附加 DPoP 证明；
     *                     {@code false} 表示对 Admin API 请求附加 DPoP 证明及 DPoP 授权头
     */
    public DPoPAuthFilter(TokenManager tokenManager, boolean tokenRequest) {
        super(tokenManager);
        this.tokenRequest = tokenRequest;
    }

    /**
     * 为出站请求生成并附加 DPoP 证明头。
     * <p>
     * 令牌请求模式下仅对 {@code /token} URI 生成不含访问令牌的证明；
     * 常规模式下同时设置 {@code DPoP} 授权头与绑定访问令牌的证明。
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String requestUri = requestContext.getUri().toString();
        if (tokenRequest) {
            if (requestUri.endsWith("/token")) {
                // 获取新访问令牌或刷新令牌时的请求
                String dpop = DPoPGenerator.generateRsaSignedDPoPProof(tokenManager.getDpopKeyPair(), requestContext.getMethod(), requestUri, null);
                requestContext.getHeaders().add(DPOP_HTTP_HEADER, dpop);
            }
        } else {
            // 发往 Admin REST API 的常规请求
            String accessToken = tokenManager.getAccessTokenString();
            String dpop = DPoPGenerator.generateRsaSignedDPoPProof(tokenManager.getDpopKeyPair(), requestContext.getMethod(), requestUri, accessToken);
            requestContext.getHeaders().add(DPOP_HTTP_HEADER, dpop);

            String authHeader = DPOP_HTTP_HEADER + " " + accessToken;
            requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
        }
    }


    /** 返回 DPoP 授权头前缀（{@code DPoP}）。 */
    @Override
    protected String getAuthHeaderPrefix() {
        return DPOP_HTTP_HEADER;
    }
}
