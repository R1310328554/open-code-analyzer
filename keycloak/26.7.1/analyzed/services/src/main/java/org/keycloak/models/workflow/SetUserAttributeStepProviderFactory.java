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
 * 设置用户属性步骤的 {@link WorkflowStepProviderFactory}，工厂 ID 为 {@code set-user-attribute}。
 * <p>创建 {@link SetUserAttributeStepProvider}，将步骤组件配置中的键值对映射为用户属性；除调度与优先级键外的所有配置项均视为属性名。</p>
 */
public class SetUserAttributeStepProviderFactory implements WorkflowStepProviderFactory<SetUserAttributeStepProvider> {

    /** 工厂标识 {@code set-user-attribute}。 */
    public static final String ID = "set-user-attribute";

    /** 创建 {@link SetUserAttributeStepProvider} 实例。 */
    @Override
    public SetUserAttributeStepProvider create(KeycloakSession session, ComponentModel model) {
        return new SetUserAttributeStepProvider(session, model);
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
        return "Sets attributes on the user";
    }
}
