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

import org.keycloak.models.OAuth2DeviceCodeModel;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 设备换令牌请求客户端策略上下文：封装 device_code 模型与令牌端点表单参数。
 * <p>触发事件 {@link ClientPolicyEvent#DEVICE_TOKEN_REQUEST}。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class DeviceTokenRequestContext implements ClientPolicyContext {

    /** 设备码模型（含 scope、nonce、PKCE 等） */
    private final OAuth2DeviceCodeModel deviceCodeModel;
    /** 令牌端点原始表单参数 */
    private final MultivaluedMap<String, String> requestParameters;

    /**
     * @param deviceCodeModel 设备码模型
     * @param requestParameters 令牌请求表单参数
     */
        this.deviceCodeModel = deviceCodeModel;
        this.requestParameters = requestParameters;
    }

    /** {@inheritDoc} 返回 DEVICE_TOKEN_REQUEST */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.DEVICE_TOKEN_REQUEST;
    }

    /** @return 设备码模型 */
    public OAuth2DeviceCodeModel getDeviceCodeModel() {
        return deviceCodeModel;
    }

    /** @return 令牌请求表单参数 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }
}
