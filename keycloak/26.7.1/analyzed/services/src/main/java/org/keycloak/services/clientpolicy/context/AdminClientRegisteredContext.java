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
 * Admin REST 客户端已注册上下文：在 {@link ClientPolicyEvent#REGISTERED} 事件上携带新创建的 {@link ClientModel}。
 * <p>客户端持久化完成后触发，供策略 Executor 执行后续动作（如强制配置、审计等）。</p>
 */
public class AdminClientRegisteredContext extends AbstractAdminClientCRUDContext implements ClientCRUDClientAvailableContext {

    /** 已成功注册并持久化的客户端。 */
    private final ClientModel registeredClient;

    /**
     * @param registeredClient 已注册的客户端模型
     * @param adminAuth Admin REST 认证上下文
     */
        super(adminAuth);
        this.registeredClient = registeredClient;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#REGISTERED} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.REGISTERED;
    }

    /** {@inheritDoc} @return 已注册的客户端 */
    @Override
    public ClientModel getTargetClient() {
        return registeredClient;
    }
}
