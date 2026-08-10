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
 *
 */

package org.keycloak.services.clientpolicy.condition;

import java.util.Optional;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.util.JsonSerialization;

/**
 * 客户端策略条件提供者抽象基类：管理会话、配置与负向逻辑。
 * <p>子类实现 {@link ClientPolicyConditionProvider} 的具体条件评估逻辑。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractClientPolicyConditionProvider<CONFIG extends ClientPolicyConditionConfigurationRepresentation> implements ClientPolicyConditionProvider<CONFIG> {

    /** Keycloak 会话上下文。 */
    protected final KeycloakSession session;
    /** 当前条件配置。 */
    protected CONFIG configuration;

    /** @param session Keycloak 会话 */
    public AbstractClientPolicyConditionProvider(KeycloakSession session) {
        this.session = session;
    }

    /** 设置条件配置；{@code null} 时使用默认空配置。 */
    @Override
    public void setupConfiguration(CONFIG config) {
        if (config == null) {
            // 传入 null 配置时的回退处理
            // Fallback for the case that null configuration is passed as an argument
            this.configuration = JsonSerialization.mapper.convertValue(new ClientPolicyConditionConfigurationRepresentation(), getConditionConfigurationClass());
        } else {
            this.configuration = config;
        }
    }

    /** @return 是否启用负向逻辑（反转 {@link ClientPolicyConditionProvider#applyPolicy} 结果） */
    public boolean isNegativeLogic() throws ClientPolicyException {
        if (configuration == null) {
            throw new ClientPolicyException("Not allowed to call this when configuration is not set");
        }
        return Optional.ofNullable(this.configuration.isNegativeLogic()).orElse(Boolean.FALSE).booleanValue();
    }
}
