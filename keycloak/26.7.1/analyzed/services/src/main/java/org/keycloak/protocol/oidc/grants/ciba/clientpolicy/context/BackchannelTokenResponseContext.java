/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.ClientSessionContext;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.grants.ciba.channel.CIBAAuthenticationRequest;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 客户端策略上下文：CIBA 后台令牌响应即将返回给客户端时触发。
 * <p>可访问令牌构建器与会话上下文以调整响应内容。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class BackchannelTokenResponseContext implements CIBAContext {

    /** 关联的 CIBA 认证请求 */
    private final CIBAAuthenticationRequest parsedRequest;
    /** 原始令牌请求参数 */
    private final MultivaluedMap<String, String> requestParameters;
    /** 客户端会话上下文 */
    private final ClientSessionContext clientSessionCtx;
    /** 访问令牌响应构建器 */
    private final TokenManager.AccessTokenResponseBuilder accessTokenResponseBuilder;

    /**
     * @param parsedRequest 解析后的认证请求
     * @param requestParameters 请求参数
     * @param clientSessionCtx 客户端会话上下文
     * @param accessTokenResponseBuilder 令牌响应构建器
     */
        this.parsedRequest = parsedRequest;
        this.requestParameters = requestParameters;
        this.clientSessionCtx = clientSessionCtx;
        this.accessTokenResponseBuilder = accessTokenResponseBuilder;
    }

    /** @return {@link ClientPolicyEvent#BACKCHANNEL_TOKEN_RESPONSE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.BACKCHANNEL_TOKEN_RESPONSE;
    }

    /** @return 解析后的认证请求 */
    @Override
    public CIBAAuthenticationRequest getParsedRequest() {
        return parsedRequest;
    }

    /** @return 请求参数 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }

    /** @return 访问令牌响应构建器 */
    public TokenManager.AccessTokenResponseBuilder getAccessTokenResponseBuilder() {
        return accessTokenResponseBuilder;
    }

    /** @return 客户端会话上下文 */
    public ClientSessionContext getClientSessionContext() {
        return clientSessionCtx;
    }

}
