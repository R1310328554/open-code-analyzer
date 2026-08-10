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
 * OIDC 登出请求客户端策略上下文。
 * <p>在 RP 发起登出端点请求时触发，携带客户端与可选请求参数。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class LogoutRequestContext implements ClientPolicyContext, ClientModelContext {

    /** 发起登出的客户端 */
    private final ClientModel client;
    /** 登出请求参数（可为 null） */
    private final MultivaluedMap<String, String> params;

    /**
     * @param client 客户端模型
     * @param params 登出请求参数
     */
    public LogoutRequestContext(ClientModel client, MultivaluedMap<String, String> params) {
        this.client = client;
        this.params = params;
    }

    /** @param client 客户端模型（无额外参数） */
    public LogoutRequestContext(ClientModel client) {
        this(client, null);
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#LOGOUT_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.LOGOUT_REQUEST;
    }

    /** @return 登出请求参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** {@inheritDoc} @return 客户端模型 */
    @Override
    public ClientModel getClient() {
        return client;
    }
}
