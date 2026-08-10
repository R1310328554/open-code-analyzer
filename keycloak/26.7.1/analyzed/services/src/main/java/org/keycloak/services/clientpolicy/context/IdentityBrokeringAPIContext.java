/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 身份联邦 API 客户端策略上下文。
 * <p>在身份联邦相关 API 调用期间触发，暴露客户端、访问令牌 scope 与目标 IdP 别名。</p>
 */
public class IdentityBrokeringAPIContext implements ClientModelContext, ScopeParameterContext, IdentityProviderContext {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 调用方访问令牌 */
    private final AccessToken accessToken;
    /** 发起请求的客户端 */
    private final ClientModel client;
    /** 目标身份提供者别名 */
    private final String identityProviderAlias;

    /**
     * @param session Keycloak 会话
     * @param accessToken 访问令牌（含 scope）
     * @param client 客户端模型
     * @param identityProviderAlias IdP 别名
     */
    public IdentityBrokeringAPIContext(KeycloakSession session, AccessToken accessToken, ClientModel client, String identityProviderAlias) {
        this.session = session;
        this.accessToken = accessToken;
        this.client = client;
        this.identityProviderAlias = identityProviderAlias;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#IDENTITY_BROKERING_API} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.IDENTITY_BROKERING_API;
    }

    /** {@inheritDoc} @return 客户端模型 */
    @Override
    public ClientModel getClient() {
        return client;
    }

    /** {@inheritDoc} @return 访问令牌中的 scope 声明 */
    @Override
    public String getScopeParameter() {
        return accessToken.getScope();
    }

    /** {@inheritDoc} @return 身份提供者别名 */
    @Override
    public String getIdentityProviderAlias() {
        return identityProviderAlias;
    }
}
