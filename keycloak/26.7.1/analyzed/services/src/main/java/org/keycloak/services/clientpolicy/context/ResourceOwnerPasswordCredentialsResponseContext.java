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

package org.keycloak.services.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 资源所有者密码凭证（ROPC）令牌响应客户端策略上下文。
 * <p>在 ROPC 流成功生成令牌响应时触发，供策略修改或校验响应内容。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ResourceOwnerPasswordCredentialsResponseContext implements ClientPolicyContext {

    /** 原始请求表单参数 */
    private final MultivaluedMap<String, String> params;
    /** 客户端会话上下文 */
    private final ClientSessionContext clientSessionCtx;
    /** 正在构建的访问令牌响应 */
    private final TokenManager.AccessTokenResponseBuilder accessTokenResponseBuilder;

    /**
     * @param params 请求表单参数
     * @param clientSessionCtx 客户端会话上下文
     * @param accessTokenResponseBuilder 令牌响应构建器
     */
    public ResourceOwnerPasswordCredentialsResponseContext(MultivaluedMap<String, String> params,
                                                           ClientSessionContext clientSessionCtx,
                                                           TokenManager.AccessTokenResponseBuilder accessTokenResponseBuilder) {
        this.params = params;
        this.clientSessionCtx = clientSessionCtx;
        this.accessTokenResponseBuilder = accessTokenResponseBuilder;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#RESOURCE_OWNER_PASSWORD_CREDENTIALS_RESPONSE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.RESOURCE_OWNER_PASSWORD_CREDENTIALS_RESPONSE;
    }

    /** @return 原始表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
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
