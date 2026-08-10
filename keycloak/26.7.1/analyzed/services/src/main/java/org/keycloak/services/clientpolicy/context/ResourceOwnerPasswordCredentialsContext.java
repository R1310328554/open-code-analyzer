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

package org.keycloak.services.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.OAuth2Constants;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 资源所有者密码凭证（ROPC）请求客户端策略上下文。
 * <p>在密码凭证授权类型令牌请求处理时触发，暴露客户端、scope 与原始表单参数。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ResourceOwnerPasswordCredentialsContext implements ClientModelContext, ScopeParameterContext {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 令牌端点表单参数 */
    private final MultivaluedMap<String, String> params;

    /**
     * @param session Keycloak 会话
     * @param params 请求表单参数
     */
    public ResourceOwnerPasswordCredentialsContext(KeycloakSession session, MultivaluedMap<String, String> params) {
        this.session = session;
        this.params = params;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#RESOURCE_OWNER_PASSWORD_CREDENTIALS_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.RESOURCE_OWNER_PASSWORD_CREDENTIALS_REQUEST;
    }

    /** {@inheritDoc} @return 当前请求上下文中的客户端 */
    @Override
    public ClientModel getClient() {
        return session.getContext().getClient();
    }

    /** {@inheritDoc} @return scope 表单参数值 */
    @Override
    public String getScopeParameter() {
        return this.params.getFirst(OAuth2Constants.SCOPE);
    }

    /** @return 原始表单参数 */
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

}
