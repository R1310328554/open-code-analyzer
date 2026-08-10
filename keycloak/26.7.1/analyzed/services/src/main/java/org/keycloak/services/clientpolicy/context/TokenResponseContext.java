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
import org.keycloak.models.ClientSessionContext;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.utils.OAuth2CodeParser;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 令牌响应上下文：在 {@link ClientPolicyEvent#TOKEN_RESPONSE} 事件上暴露令牌响应构建器与会话信息。
 * <p>授权码等 grant 成功签发令牌后、HTTP 响应返回客户端前触发，Executor 可修改 token 内容。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class TokenResponseContext implements ClientPolicyClientSessionContext {

    /** 原始令牌请求表单参数。 */
    private final MultivaluedMap<String, String> params;
    /** 授权码解析结果。 */
    private final OAuth2CodeParser.ParseResult parseResult;
    /** 客户端会话上下文，含 scope 与角色映射。 */
    private final ClientSessionContext clientSessionCtx;
    /** 访问令牌响应构建器。 */
    private final TokenManager.AccessTokenResponseBuilder accessTokenResponseBuilder;

    /**
     * @param params 令牌请求表单参数
     * @param parseResult 授权码解析结果
     * @param clientSessionCtx 客户端会话上下文
     * @param accessTokenResponseBuilder 访问令牌响应构建器
     */
        this.params = params;
        this.parseResult = parseResult;
        this.clientSessionCtx = clientSessionCtx;
        this.accessTokenResponseBuilder = accessTokenResponseBuilder;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#TOKEN_RESPONSE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.TOKEN_RESPONSE;
    }

    /** @return 令牌请求表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** @return 授权码解析结果 */
    public OAuth2CodeParser.ParseResult getParseResult() {
        return parseResult;
    }

    /** @return 访问令牌响应构建器 */
    public TokenManager.AccessTokenResponseBuilder getAccessTokenResponseBuilder() {
        return accessTokenResponseBuilder;
    }

    /** {@inheritDoc} 从解析结果获取客户端会话 */
    @Override
    public AuthenticatedClientSessionModel getClientSession() {
        return getParseResult().getClientSession();
    }

    /** @return 客户端会话上下文 */
    public ClientSessionContext getClientSessionContext() {
        return clientSessionCtx;
    }

}
