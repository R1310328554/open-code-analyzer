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

package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;


/**
 * 工作流步骤：禁用工作流上下文中的目标用户账户。
 * <p>用户已禁用时跳过；完成后可触发账户禁用通知模板。</p>
 */
public class DisableUserStepProvider implements WorkflowStepProvider {

    private final KeycloakSession session;
    private final Logger log = Logger.getLogger(DisableUserStepProvider.class);

    /** @param session Keycloak 会话 @param model 工作流步骤组件配置（本步骤未使用） */
    public DisableUserStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
    }

    @Override
    public void close() {
    }

    /** 将目标用户 {@link UserModel#setEnabled(boolean)} 设为 false。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user != null && user.isEnabled()) {
            log.debugv("Disabling user {0} ({1})", user.getUsername(), user.getId());
            user.setEnabled(false);
        }
    }

    /** @return 账户禁用通知消息模板键 */
    @Override
    public String getNotificationMessage() {
        return "accountDisableNotificationBody";
    }

    /** @return 账户禁用通知主题模板键 */
    @Override
    public String getNotificationSubject() {
        return "accountDisableNotificationSubject";
    }
}
