/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.condition;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link ClientAttributesCondition} 的 SPI 工厂：以 MAP 类型配置需匹配的客户端属性。
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public class ClientAttributesConditionFactory extends AbstractClientPolicyConditionProviderFactory {

    /** 客户端策略条件提供方 ID */
    public static final String PROVIDER_ID = "client-attributes";

    /** 配置键：客户端属性映射 */
    public static final String ATTRIBUTES = "attributes";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        addCommonConfigProperties(configProperties);

        ProviderConfigProperty property;
        property = new ProviderConfigProperty(ATTRIBUTES, PROVIDER_ID + ".label", PROVIDER_ID + "-condition.tooltip", ProviderConfigProperty.MAP_TYPE, null);
        configProperties.add(property);
    }

    /** @param session Keycloak 会话 @return ClientAttributesCondition 实例 */
    @Override
    public ClientPolicyConditionProvider create(KeycloakSession session) {
        return new ClientAttributesCondition(session);
    }

    /** @return 提供方 ID */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 条件帮助说明 */
    @Override
    public String getHelpText() {
        return "The condition checks whether all of the specified client attributes exists on the client to determine whether the policy is applied. This effectively allows client administrator to create client attribute of specified name on the client to make sure that particular client policy will be applied on requests of this client. Condition is checked during most of OpenID Connect requests (Authorization request, token requests, introspection endpoint request etc.).";
    }

    /** @return 静态注册的配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
