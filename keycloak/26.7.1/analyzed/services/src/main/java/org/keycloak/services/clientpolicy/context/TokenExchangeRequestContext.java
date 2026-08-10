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

package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.protocol.oidc.TokenExchangeContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 令牌交换请求上下文：在 {@link ClientPolicyEvent#TOKEN_EXCHANGE_REQUEST} 事件上封装 {@link TokenExchangeContext}。
 * <p>实现 {@link ClientModelContext} 与 {@link ScopeParameterContext}，供策略评估交换主体、目标客户端与 scope。</p>
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class TokenExchangeRequestContext implements ClientModelContext, ScopeParameterContext {

    /** RFC 8693 令牌交换处理上下文。 */
    private final TokenExchangeContext tokenExchangeContext;

    /** @param tokenExchangeContext 令牌交换上下文 */
    public TokenExchangeRequestContext(TokenExchangeContext tokenExchangeContext) {
        this.tokenExchangeContext = tokenExchangeContext;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#TOKEN_EXCHANGE_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.TOKEN_EXCHANGE_REQUEST;
    }


    /** @return 令牌交换上下文 */
    public TokenExchangeContext getTokenExchangeContext() {
        return tokenExchangeContext;
    }

    /** {@inheritDoc} 从交换上下文解析请求客户端 */
    @Override
    public ClientModel getClient() {
        return tokenExchangeContext.getClient();
    }

    /** {@inheritDoc} 从交换请求参数读取 scope */
    @Override
    public String getScopeParameter() {
        return tokenExchangeContext.getParams().getScope();
    }
}
