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

package org.keycloak.services.clientpolicy.condition;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link AcrCondition} 的 SPI 工厂：配置期望的 ACR 属性值。
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class AcrConditionFactory extends AbstractClientPolicyConditionProviderFactory {

    /** 客户端策略条件提供方 ID */
    public static final String PROVIDER_ID = "acr-condition";

    /** 配置键：ACR 属性名 */
    public static final String ACR_PROPERTY = "acr-property";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property = new ProviderConfigProperty(ACR_PROPERTY, "ACR",
                "ACR to be requested to satisfy the condition",
                ProviderConfigProperty.STRING_TYPE, null);
        configProperties.add(property);
    }

    /** @param session Keycloak 会话 @return AcrCondition 实例 */
    @Override
    public ClientPolicyConditionProvider create(KeycloakSession session) {
        return new AcrCondition(session);
    }

    /** @return 提供方 ID */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 条件帮助说明 */
    @Override
    public String getHelpText() {
        return "The condition is satisfied when configured acr value is requested";
    }


    /** @return 静态注册的配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
