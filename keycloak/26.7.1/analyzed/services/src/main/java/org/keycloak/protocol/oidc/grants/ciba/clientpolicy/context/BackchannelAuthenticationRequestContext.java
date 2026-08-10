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

package org.keycloak.protocol.oidc.grants.ciba.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.protocol.oidc.grants.ciba.channel.CIBAAuthenticationRequest;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.request.BackchannelAuthenticationEndpointRequest;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 客户端策略上下文：CIBA 后台认证端点收到认证请求时触发。
 * <p>携带原始端点请求、解析后的 {@link CIBAAuthenticationRequest} 及表单参数。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class BackchannelAuthenticationRequestContext implements CIBAContext {

    /** 后台认证端点原始请求对象 */
    private final BackchannelAuthenticationEndpointRequest request;
    /** 解析后的 CIBA 认证请求（auth_req_id 载荷） */
    private final CIBAAuthenticationRequest parsedRequest;
    /** 请求表单/查询参数 */
    private final MultivaluedMap<String, String> requestParameters;

    /**
     * @param request 端点请求
     * @param parsedRequest 解析后的认证请求
     * @param requestParameters 原始请求参数
     */
        this.request = request;
        this.parsedRequest = parsedRequest;
        this.requestParameters = requestParameters;
    }

    /** @return {@link ClientPolicyEvent#BACKCHANNEL_AUTHENTICATION_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.BACKCHANNEL_AUTHENTICATION_REQUEST;
    }

    /** @return 后台认证端点请求 */
    public BackchannelAuthenticationEndpointRequest getRequest() {
        return request;
    }

    /** @return 解析后的 CIBA 认证请求 */
    @Override
    public CIBAAuthenticationRequest getParsedRequest() {
        return parsedRequest;
    }

    /** @return 请求参数映射 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }
}
