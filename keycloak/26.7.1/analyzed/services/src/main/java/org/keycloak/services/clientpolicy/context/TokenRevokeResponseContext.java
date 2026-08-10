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
 * 令牌吊销响应上下文：在 {@link ClientPolicyEvent#TOKEN_REVOKE_RESPONSE} 事件上携带客户端与原始吊销参数。
 * <p>令牌吊销处理完成后触发，供策略记录审计信息或执行后续清理。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class TokenRevokeResponseContext implements ClientPolicyContext, ClientModelContext {

    /** 发起吊销的客户端。 */
    private final ClientModel client;
    /** 吊销请求表单参数。 */
    private final MultivaluedMap<String, String> params;

    /**
     * @param client 请求客户端
     * @param params 吊销表单参数
     */
        this.client = client;
        this.params = params;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#TOKEN_REVOKE_RESPONSE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.TOKEN_REVOKE_RESPONSE;
    }

    /** @return 吊销表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** {@inheritDoc} @return 发起吊销的客户端 */
    @Override
    public ClientModel getClient() {
        return client;
    }
}
