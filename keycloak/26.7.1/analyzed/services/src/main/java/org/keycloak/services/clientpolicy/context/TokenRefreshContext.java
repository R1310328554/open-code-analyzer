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

import org.keycloak.models.ClientModel;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 令牌刷新请求上下文：在 {@link ClientPolicyEvent#TOKEN_REFRESH} 事件上携带 refresh_token 请求参数、客户端与 scope。
 * <p>处理 OAuth refresh_token grant 前触发，供策略限制刷新行为或校验 scope 变更。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class TokenRefreshContext implements ClientPolicyContext, ClientModelContext, ScopeParameterContext {

    /** 刷新令牌请求表单参数。 */
    private final MultivaluedMap<String, String> params;
    /** 发起刷新的客户端。 */
    private final ClientModel client;
    /** 刷新请求中请求的 scope（可为空表示沿用原 scope）。 */
    private final String scope;

    /**
     * @param params 刷新请求表单参数
     * @param client 客户端模型
     * @param scope 请求的 scope 参数
     */
        this.params = params;
        this.client = client;
        this.scope = scope;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#TOKEN_REFRESH} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.TOKEN_REFRESH;
    }

    /** @return 刷新请求表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** {@inheritDoc} @return 发起刷新的客户端 */
    @Override
    public ClientModel getClient() {
        return client;
    }

    /** {@inheritDoc} @return 刷新请求的 scope */
    @Override
    public String getScopeParameter() {
        return scope;
    }
}
