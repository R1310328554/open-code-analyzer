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
 * 动态客户端注销客户端策略上下文。
 * <p>在动态客户端注册端点删除客户端时触发，携带待注销目标客户端与注册 JWT 身份。</p>
 */
public class DynamicClientUnregisterContext extends AbstractDynamicClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 待注销的目标客户端 */
    private final ClientModel targetClient;

    /**
     * @param session Keycloak 会话
     * @param targetClient 待注销客户端
     * @param token 动态注册访问令牌 JWT
     * @param realm 目标 Realm
     */
    public DynamicClientUnregisterContext(KeycloakSession session, ClientModel targetClient, JsonWebToken token, RealmModel realm) {
        super(session, token, realm);
        this.targetClient = targetClient;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#UNREGISTER} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UNREGISTER;
    }

    /** {@inheritDoc} @return 待注销客户端 */
    @Override
    public ClientModel getTargetClient() {
        return this.targetClient;
    }
}
