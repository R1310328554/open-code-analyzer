/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.workflow.client;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.workflow.WorkflowExecutionContext;
import org.keycloak.models.workflow.WorkflowStepProvider;

import org.jboss.logging.Logger;

/**
 * 工作流步骤：禁用工作流上下文中的目标客户端。
 * <p>客户端已禁用时跳过；否则将 {@link ClientModel#setEnabled(boolean)} 设为 false。</p>
 */
public class DisableClientStepProvider implements WorkflowStepProvider {

    private final KeycloakSession session;
    private final Logger log = Logger.getLogger(DisableClientStepProvider.class);

    /** @param session Keycloak 会话 @param model 工作流步骤组件配置（本步骤未使用） */
    public DisableClientStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
    }

    @Override
    public void close() {
        // 无需要释放的资源
    }

    /** 查找目标客户端并在启用状态下将其禁用。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = session.clients().getClientById(realm, context.getResourceId());

        if (client != null && client.isEnabled()) {
            log.debugv("Disabling client {0} ({1})", client.getName(), client.getId());
            client.setEnabled(false);
        }
    }
}
