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
 * 令牌吊销请求上下文：在 {@link ClientPolicyEvent#TOKEN_REVOKE} 事件上携带客户端与 RFC 7009 吊销表单参数。
 * <p>处理令牌吊销端点请求前触发，供策略限制吊销范围或审计吊销操作。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class TokenRevokeContext implements ClientPolicyContext, ClientModelContext {

    /** 发起吊销请求的客户端。 */
    private final ClientModel client;
    /** 吊销端点表单参数（含 token、token_type_hint 等）。 */
    private final MultivaluedMap<String, String> params;

    /**
     * @param client 请求客户端
     * @param params 吊销表单参数
     */
        this.client = client;
        this.params = params;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#TOKEN_REVOKE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.TOKEN_REVOKE;
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
