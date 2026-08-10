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

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientregistration.ClientRegistrationContext;

/**
 * 动态客户端更新请求客户端策略上下文。
 * <p>在动态注册端点处理客户端更新前触发，暴露待更新客户端与拟提交的 {@link ClientRepresentation}。</p>
 */
public class DynamicClientUpdateContext extends AbstractDynamicClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 将被更新的现有客户端 */
    private final ClientModel clientToBeUpdated;
    /** 客户端提交的拟更新表示 */
    private final ClientRepresentation proposedClientRepresentation;

    /**
     * @param context 客户端注册上下文（会话与请求体）
     * @param proposedClientRepresentation 待更新的目标客户端
     * @param token 动态注册访问令牌 JWT
     * @param realm 目标 Realm
     */
    public DynamicClientUpdateContext(ClientRegistrationContext context, ClientModel proposedClientRepresentation, JsonWebToken token, RealmModel realm) {
        super(context.getSession(), token, realm);
        this.clientToBeUpdated = proposedClientRepresentation;
        this.proposedClientRepresentation = context.getClient();
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#UPDATE} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UPDATE;
    }

    /** {@inheritDoc} @return 拟提交的客户端表示 */
    @Override
    public ClientRepresentation getProposedClientRepresentation() {
        return proposedClientRepresentation;
    }

    /** {@inheritDoc} @return 待更新客户端 */
    @Override
    public ClientModel getTargetClient() {
        return clientToBeUpdated;
    }
}
