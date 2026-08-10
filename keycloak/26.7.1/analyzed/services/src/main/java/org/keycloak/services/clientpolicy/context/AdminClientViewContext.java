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
 * Admin REST 客户端查看上下文：在 {@link ClientPolicyEvent#VIEW} 事件上携带被读取的 {@link ClientModel}。
 * <p>由 Admin API 读取客户端详情时触发，供策略条件/Executor 控制或审计访问。</p>
 */
public class AdminClientViewContext extends AbstractAdminClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 被查看的目标客户端。 */
    private final ClientModel targetClient;

    /**
     * @param targetClient 被读取的客户端
     * @param adminAuth Admin REST 认证上下文
     */
        super(adminAuth);
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
