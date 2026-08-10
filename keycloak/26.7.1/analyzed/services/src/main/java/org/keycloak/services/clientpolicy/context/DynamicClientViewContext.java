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
 * 动态客户端查看客户端策略上下文。
 * <p>在动态注册端点读取客户端元数据时触发，用于策略评估只读访问。</p>
 */
public class DynamicClientViewContext extends AbstractDynamicClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 被查看的目标客户端 */
    private final ClientModel targetClient;

    /**
     * @param session Keycloak 会话
     * @param targetClient 被查看客户端
     * @param token 动态注册访问令牌 JWT
     * @param realm 目标 Realm
     */
    public DynamicClientViewContext(KeycloakSession session, ClientModel targetClient, JsonWebToken token, RealmModel realm) {
        super(session, token, realm);
        this.targetClient = targetClient;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#VIEW} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.VIEW;
    }

    /** {@inheritDoc} @return 被查看客户端 */
    @Override
    public ClientModel getTargetClient() {
        return targetClient;
    }
}
