/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
import org.keycloak.protocol.oidc.utils.OAuth2CodeParser;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 令牌请求上下文：在 {@link ClientPolicyEvent#TOKEN_REQUEST} 事件上携带授权码/token 交换等 grant 的表单参数与解析结果。
 * <p>OAuth 令牌端点处理 authorization_code、implicit 等 grant 前触发。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class TokenRequestContext implements ClientPolicyClientSessionContext {

    /** 令牌端点表单参数。 */
    private final MultivaluedMap<String, String> params;
    /** 授权码解析结果，含客户端会话与 code 有效性信息。 */
    private final OAuth2CodeParser.ParseResult parseResult;

    /**
     * @param params 令牌请求表单参数
     * @param parseResult 授权码解析结果
     */
        this.params = params;
        this.parseResult = parseResult;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#TOKEN_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.TOKEN_REQUEST;
    }

    /** @return 令牌请求表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** @return 授权码解析结果 */
    public OAuth2CodeParser.ParseResult getParseResult() {
        return parseResult;
    }

    /** {@inheritDoc} 从解析结果获取客户端会话 */
    @Override
    public AuthenticatedClientSessionModel getClientSession() {
        return getParseResult().getClientSession();
    }

}
