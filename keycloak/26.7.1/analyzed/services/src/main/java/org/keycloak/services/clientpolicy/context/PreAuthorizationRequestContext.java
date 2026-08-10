/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 授权请求预处理客户端策略上下文。
 * <p>在完整解析授权端点请求之前触发，仅携带 client_id 与原始请求参数，供早期策略拦截。</p>
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public class PreAuthorizationRequestContext implements ClientPolicyContext {

    /** 请求中的 client_id */
    private final String clientId;
    /** 原始授权请求参数 */
    private final MultivaluedMap<String, String> requestParameters;

    /**
     * @param clientId 客户端标识
     * @param requestParameters 原始请求参数
     */
    public PreAuthorizationRequestContext(String clientId, MultivaluedMap<String, String> requestParameters) {
        this.clientId = clientId;
        this.requestParameters = requestParameters;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#PRE_AUTHORIZATION_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.PRE_AUTHORIZATION_REQUEST;
    }

    /** @return client_id 字符串 */
    public String getClientId() {
        return clientId;
    }

    /** @return 原始请求参数 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }

}
