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

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 禁用用户工作流步骤工厂，ID 为 {@code disable-user}。
 * <p>创建 {@link DisableUserStepProvider}，用于在工作流中停用用户账户并发送通知。</p>
 */
public class DisableUserStepProviderFactory implements WorkflowStepProviderFactory<DisableUserStepProvider> {

    /** 步骤工厂标识 {@code disable-user}。 */
    public static final String ID = "disable-user";

    /** 创建绑定会话与组件模型的 {@link DisableUserStepProvider}。 */
    @Override
    public DisableUserStepProvider create(KeycloakSession session, ComponentModel model) {
        return new DisableUserStepProvider(session, model);
    }

    @Override
    public String getId() {
        return ID;
    }

    /** @return 仅支持用户资源类型 */
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return Set.of(ResourceType.USERS);
    }

    /** @return 管理控制台步骤说明文本 */
    @Override
    public String getHelpText() {
        return "Disables the user";
    }
}
