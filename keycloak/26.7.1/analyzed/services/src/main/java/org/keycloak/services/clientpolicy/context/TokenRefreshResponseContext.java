/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 令牌刷新响应上下文：在 {@link ClientPolicyEvent#TOKEN_REFRESH_RESPONSE} 事件上暴露新令牌响应构建器。
 * <p>刷新 grant 成功签发新 access/refresh token 后、HTTP 响应返回前触发。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class TokenRefreshResponseContext implements ClientPolicyContext, ClientPolicyClientSessionContext {

    /** 原始刷新请求表单参数。 */
    private final MultivaluedMap<String, String> params;
    /** 刷新后访问令牌响应构建器。 */
    private final TokenManager.AccessTokenResponseBuilder accessTokenResponseBuilder;

    /**
     * @param params 刷新请求表单参数
     * @param accessTokenResponseBuilder 访问令牌响应构建器
     */
        this.params = params;
        this.accessTokenResponseBuilder = accessTokenResponseBuilder;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#TOKEN_REFRESH_RESPONSE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.TOKEN_REFRESH_RESPONSE;
    }

    /** @return 刷新请求表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** @return 访问令牌响应构建器 */
    public TokenManager.AccessTokenResponseBuilder getAccessTokenResponseBuilder() {
        return accessTokenResponseBuilder;
    }

    /** {@inheritDoc} 从响应构建器解析客户端会话 */
    @Override
    public AuthenticatedClientSessionModel getClientSession() {
        return this.accessTokenResponseBuilder.getClientSessionCtx().getClientSession();
    }
}
