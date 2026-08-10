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
 */

package org.keycloak.protocol.oidc.par.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.ClientModel;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.context.ClientModelContext;
import org.keycloak.services.clientpolicy.context.ScopeParameterContext;

/**
 * 推送授权请求（PAR）客户端策略上下文。
 * <p>在 PAR 端点处理期间传递给客户端策略，携带客户端、授权请求与原始表单参数。</p>
 */
public class PushedAuthorizationRequestContext implements ClientPolicyContext, ClientModelContext, ScopeParameterContext {

    /** 发起 PAR 的客户端 */
    private final ClientModel client;
    /** 原始表单参数（多值映射） */
    private final MultivaluedMap<String, String> requestParameters;
    /** 已解析的授权端点请求 */
    private AuthorizationEndpointRequest request;

    /**
     * @param client 客户端模型
     * @param request 授权端点请求
     * @param requestParameters 原始请求参数
     */
    public PushedAuthorizationRequestContext(ClientModel client, AuthorizationEndpointRequest request,
            MultivaluedMap<String, String> requestParameters) {
        this.client = client;
        this.request = request;
        this.requestParameters = requestParameters;
    }

    /** @return 客户端策略事件 {@link ClientPolicyEvent#PUSHED_AUTHORIZATION_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.PUSHED_AUTHORIZATION_REQUEST;
    }

    /** @return 授权端点请求 */
    public AuthorizationEndpointRequest getRequest() {
        return request;
    }

    /** @return 原始表单参数 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }

    /** @return 客户端模型 */
    @Override
    public ClientModel getClient() {
        return client;
    }

    /** @return 授权请求中的 scope 参数 */
    @Override
    public String getScopeParameter() {
        return request.getScope();
    }
}
