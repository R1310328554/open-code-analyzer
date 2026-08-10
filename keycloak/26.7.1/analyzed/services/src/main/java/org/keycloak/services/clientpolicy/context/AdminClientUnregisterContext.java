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
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.resources.admin.AdminAuth;

/**
 * Admin REST 客户端注销上下文：在 {@link ClientPolicyEvent#UNREGISTER} 事件上携带待删除的 {@link ClientModel}。
 * <p>由 Admin API 删除客户端前触发，供策略条件/Executor 阻止或审计注销操作。</p>
 */
public class AdminClientUnregisterContext extends AbstractAdminClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 待注销的目标客户端。 */
    private final ClientModel targetClient;

    /**
     * @param targetClient 待删除的客户端
     * @param adminAuth Admin REST 认证上下文
     */
        super(adminAuth);
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
