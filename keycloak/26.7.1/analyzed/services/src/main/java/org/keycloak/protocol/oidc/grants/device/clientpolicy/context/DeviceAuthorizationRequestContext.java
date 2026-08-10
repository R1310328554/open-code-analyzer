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

package org.keycloak.protocol.oidc.grants.device.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 设备授权请求客户端策略上下文：封装设备授权端点解析后的请求与原始表单参数。
 * <p>触发事件 {@link ClientPolicyEvent#DEVICE_AUTHORIZATION_REQUEST}。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class DeviceAuthorizationRequestContext implements ClientPolicyContext {

    /** 解析后的授权端点请求 */
    private final AuthorizationEndpointRequest request;
    /** 原始请求表单参数 */
    private final MultivaluedMap<String, String> requestParameters;

    /**
     * @param request 授权端点请求对象
     * @param requestParameters 原始表单参数
     */
        this.request = request;
        this.requestParameters = requestParameters;
    }

    /** {@inheritDoc} 返回 DEVICE_AUTHORIZATION_REQUEST */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.DEVICE_AUTHORIZATION_REQUEST;
    }

    /** @return 解析后的授权端点请求 */
    public AuthorizationEndpointRequest getRequest() {
        return request;
    }

    /** @return 原始请求表单参数 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }

}
