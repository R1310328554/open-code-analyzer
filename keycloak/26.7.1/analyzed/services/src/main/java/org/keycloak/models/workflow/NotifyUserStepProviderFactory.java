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
 * 通知用户步骤的 {@link WorkflowStepProviderFactory}，工厂 ID 为 {@code notify-user}。
 * <p>创建 {@link NotifyUserStepProvider}，基于可配置模板向用户发送工作流邮件通知；支持自定义收件人、主题与消息内容。</p>
 */
public class NotifyUserStepProviderFactory implements WorkflowStepProviderFactory<NotifyUserStepProvider> {

    /** 工厂标识 {@code notify-user}。 */
    public static final String ID = "notify-user";

    /** 创建 {@link NotifyUserStepProvider} 实例。 */
    @Override
    public NotifyUserStepProvider create(KeycloakSession session, ComponentModel model) {
        return new NotifyUserStepProvider(session, model);
    }

    /** @return 工厂 ID {@link #ID} */
    @Override
    public String getId() {
        return ID;
    }

    /** @return 支持的用户资源类型集合 */
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return Set.of(ResourceType.USERS);
    }

    /** @return 管理控制台显示的步骤说明文本 */
    @Override
    public String getHelpText() {
        return "Sends email notifications to users based on configurable templates";
    }
}
