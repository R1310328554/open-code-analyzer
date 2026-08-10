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
 *
 */

package org.keycloak.services.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 服务账户令牌响应上下文：在 {@link ClientPolicyEvent#SERVICE_ACCOUNT_TOKEN_RESPONSE} 事件上暴露响应构建器。
 * <p>服务账户令牌成功签发后、响应返回客户端前触发，Executor 可修改 access token 或附加字段。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ServiceAccountTokenResponseContext implements ClientPolicyClientSessionContext {

    /** 原始令牌请求表单参数。 */
    private final MultivaluedMap<String, String> params;
    /** 签发令牌对应的客户端会话。 */
    private final AuthenticatedClientSessionModel clientSession;
    /** 访问令牌响应构建器，可调整 token 内容与 HTTP 响应。 */
    private final TokenManager.AccessTokenResponseBuilder accessTokenResponseBuilder;

    /**
     * @param params 令牌请求表单参数
     * @param clientSession 客户端会话
     * @param accessTokenResponseBuilder 访问令牌响应构建器
     */
        this.params = params;
        this.clientSession = clientSession;
        this.accessTokenResponseBuilder = accessTokenResponseBuilder;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#SERVICE_ACCOUNT_TOKEN_RESPONSE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.SERVICE_ACCOUNT_TOKEN_RESPONSE;
    }

    /** @return 令牌请求表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** {@inheritDoc} @return 客户端会话 */
    @Override
    public AuthenticatedClientSessionModel getClientSession() {
        return clientSession;
    }

    /** @return 访问令牌响应构建器 */
    public TokenManager.AccessTokenResponseBuilder getAccessTokenResponseBuilder() {
        return accessTokenResponseBuilder;
    }
}
