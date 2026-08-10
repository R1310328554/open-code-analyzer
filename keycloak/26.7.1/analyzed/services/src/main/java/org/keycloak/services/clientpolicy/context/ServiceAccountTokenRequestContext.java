/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 服务账户令牌请求上下文：在 {@link ClientPolicyEvent#SERVICE_ACCOUNT_TOKEN_REQUEST} 事件上携带 OAuth 表单参数与客户端会话。
 * <p>客户端凭据（client_credentials）grant 处理令牌请求前触发，供策略评估 scope、客户端类型等。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ServiceAccountTokenRequestContext implements ClientPolicyClientSessionContext {

    /** OAuth 令牌端点表单参数。 */
    private final MultivaluedMap<String, String> params;
    /** 已认证的服务账户客户端会话。 */
    private final AuthenticatedClientSessionModel clientSession;

    /**
     * @param params 令牌请求表单参数
     * @param clientSession 服务账户客户端会话
     */
        this.params = params;
        this.clientSession = clientSession;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#SERVICE_ACCOUNT_TOKEN_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.SERVICE_ACCOUNT_TOKEN_REQUEST;
    }

    /** @return 令牌请求表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    /** {@inheritDoc} @return 服务账户客户端会话 */
    @Override
    public AuthenticatedClientSessionModel getClientSession() {
        return clientSession;
    }
}
