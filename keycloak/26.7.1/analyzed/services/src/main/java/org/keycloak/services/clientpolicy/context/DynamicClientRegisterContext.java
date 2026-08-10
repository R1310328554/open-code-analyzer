/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.RealmModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientregistration.ClientRegistrationContext;

/**
 * 动态客户端注册上下文：在 {@link ClientPolicyEvent#REGISTER} 事件上携带 DCR 提交的 {@link ClientRepresentation}。
 * <p>由动态客户端注册端点在创建客户端前触发，JWT 令牌解析见 {@link AbstractDynamicClientCRUDContext}。</p>
 */
public class DynamicClientRegisterContext extends AbstractDynamicClientCRUDContext {

    /** 动态注册请求提交的客户端表示。 */
    private final ClientRepresentation proposedClientRepresentation;

    /**
     * @param context 客户端注册上下文（含会话与提议表示）
     * @param token 注册 JWT（初始/注册访问令牌等）
     * @param realm 目标 Realm
     */
        super(context.getSession(), token, realm);
        this.proposedClientRepresentation = context.getClient();
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#REGISTER} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.REGISTER;
    }

    /** {@inheritDoc} @return 待注册客户端表示 */
    @Override
    public ClientRepresentation getProposedClientRepresentation() {
        return proposedClientRepresentation;
    }
}
