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
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.resources.admin.AdminAuth;

/**
 * Admin REST 客户端已更新上下文：在 {@link ClientPolicyEvent#UPDATED} 事件上携带更新后的 {@link ClientModel} 与原始提议表示。
 * <p>客户端持久化变更完成后触发，供策略 Executor 执行后续动作。</p>
 */
public class AdminClientUpdatedContext extends AbstractAdminClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 本次更新提交的客户端表示。 */
    private final ClientRepresentation proposedClientRepresentation;
    /** 已成功持久化更新的客户端。 */
    private final ClientModel updatedClient;

    /**
     * @param proposedClientRepresentation 更新提议表示
     * @param updatedClient 已更新的客户端模型
     * @param adminAuth Admin REST 认证上下文
     */
        super(adminAuth);
        this.proposedClientRepresentation = proposedClientRepresentation;
        this.updatedClient = updatedClient;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#UPDATED} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UPDATED;
    }

    /** {@inheritDoc} @return 更新提议表示 */
    @Override
    public ClientRepresentation getProposedClientRepresentation() {
        return proposedClientRepresentation;
    }

    /** {@inheritDoc} @return 已更新客户端 */
    @Override
    public ClientModel getTargetClient() {
        return updatedClient;
    }
}
