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

import java.util.List;
import java.util.Map.Entry;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_AFTER;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_PRIORITY;

/**
 * 设置用户属性工作流步骤：将步骤配置中的键值对写入用户自定义属性。
 * <p>遍历组件配置条目，跳过 {@link org.keycloak.representations.workflows.WorkflowConstants} 中的调度/优先级键，对其余项调用 {@link UserModel#setAttribute}。</p>
 */
public class SetUserAttributeStepProvider implements WorkflowStepProvider {

    private final KeycloakSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(SetUserAttributeStepProvider.class);

    /** @param session Keycloak 会话 @param model 步骤组件配置（键为属性名，值为属性值列表） */
    public SetUserAttributeStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    /** 无资源需释放。 */
    @Override
    public void close() {
    }

    /** 将非调度/优先级配置项作为用户属性批量写入。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user != null) {
            for (Entry<String, List<String>> entry : stepModel.getConfig().entrySet()) {
                String key = entry.getKey();

                if (!key.startsWith(CONFIG_AFTER) && !key.startsWith(CONFIG_PRIORITY)) {
                    log.debugv("Setting attribute {0} to user {1}", key, user.getId());
                    user.setAttribute(key, entry.getValue());
                }
            }
        }
    }
}
