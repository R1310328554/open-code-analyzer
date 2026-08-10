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

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * 动态客户端更新完成客户端策略上下文。
 * <p>在动态注册端点成功更新客户端后触发，携带已持久化的更新结果。</p>
 */
public class DynamicClientUpdatedContext extends AbstractDynamicClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 更新后的客户端 */
    private final ClientModel updatedClient;

    /**
     * @param session Keycloak 会话
     * @param updatedClient 已更新的客户端
     * @param token 动态注册访问令牌 JWT
     * @param realm 目标 Realm
     */
    public DynamicClientUpdatedContext(KeycloakSession session, ClientModel updatedClient, JsonWebToken token, RealmModel realm) {
        super(session, token, realm);
        this.updatedClient = updatedClient;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#UPDATED} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UPDATED;
    }

    /** {@inheritDoc} @return 更新后的客户端 */
    @Override
    public ClientModel getTargetClient() {
        return updatedClient;
    }
}
