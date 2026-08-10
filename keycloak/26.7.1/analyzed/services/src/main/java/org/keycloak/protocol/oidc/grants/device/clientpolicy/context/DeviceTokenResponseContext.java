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

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.OAuth2DeviceCodeModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 设备换令牌响应客户端策略上下文：封装令牌响应构建器与客户端会话。
 * <p>触发事件 {@link ClientPolicyEvent#DEVICE_TOKEN_RESPONSE}，可在签发前修改令牌响应。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class DeviceTokenResponseContext implements ClientPolicyContext {

    /** 设备码模型 */
    private final OAuth2DeviceCodeModel deviceCodeModel;
    /** 令牌请求表单参数 */
    private final MultivaluedMap<String, String> requestParameters;
    /** 已认证客户端会话 */
    private final AuthenticatedClientSessionModel clientSession;
    /** 访问令牌响应构建器 */
    private final TokenManager.AccessTokenResponseBuilder accessTokenResponseBuilder;

    /**
     * @param deviceCodeModel 设备码模型
     * @param requestParameters 令牌请求参数
     * @param clientSession 客户端会话
     * @param accessTokenResponseBuilder 令牌响应构建器
     */
        this.deviceCodeModel = deviceCodeModel;
        this.requestParameters = requestParameters;
        this.clientSession = clientSession;
        this.accessTokenResponseBuilder = accessTokenResponseBuilder;
    }

    /** {@inheritDoc} 返回 DEVICE_TOKEN_RESPONSE */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.DEVICE_TOKEN_RESPONSE;
    }

    /** @return 设备码模型 */
    public OAuth2DeviceCodeModel getDeviceCodeModel() {
        return deviceCodeModel;
    }

    /** @return 令牌请求表单参数 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }

    /** @return 访问令牌响应构建器 */
    public TokenManager.AccessTokenResponseBuilder getAccessTokenResponseBuilder() {
        return accessTokenResponseBuilder;
    }

    /** @return 已认证客户端会话 */
    public AuthenticatedClientSessionModel getClientSession() {
        return clientSession;
    }

}