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

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.resources.admin.AdminAuth;

/**
 * Admin REST 客户端注册上下文：在 {@link ClientPolicyEvent#REGISTER} 事件上携带待创建的 {@link ClientRepresentation}。
 * <p>由 Admin API 创建客户端前触发，供客户端策略条件/Executor 评估提议配置。</p>
 */
public class AdminClientRegisterContext extends AbstractAdminClientCRUDContext {

    /** 管理员提交的待注册客户端表示。 */
    private final ClientRepresentation proposedClientRepresentation;

    /**
     * @param proposedClientRepresentation 待创建的客户端表示
     * @param adminAuth Admin REST 认证上下文
     */
        super(adminAuth);
        this.proposedClientRepresentation = proposedClientRepresentation;
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
