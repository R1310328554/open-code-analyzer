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
import java.util.Arrays;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link ClientAccessTypeCondition} 的 SPI 工厂：配置允许的客户端访问类型多选列表。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientAccessTypeConditionFactory extends AbstractClientPolicyConditionProviderFactory {

    /** 客户端策略条件提供方 ID */
    public static final String PROVIDER_ID = "client-access-type";

    /** 配置键：访问类型多选 */
    public static final String TYPE = "type";

    /** 访问类型：机密客户端（启用客户端认证） */
    public static final String TYPE_CONFIDENTIAL = "confidential";
    /** 访问类型：公开客户端（无客户端认证） */
    public static final String TYPE_PUBLIC = "public";
    /** 访问类型：仅 Bearer（已弃用） */
    public static final String TYPE_BEARERONLY = "bearer-only";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        addCommonConfigProperties(configProperties);

        ProviderConfigProperty property;
        property = new ProviderConfigProperty(TYPE, "client-accesstype.label", "client-accesstype.tooltip", ProviderConfigProperty.MULTIVALUED_LIST_TYPE, TYPE_CONFIDENTIAL);
        List<String> updateProfileValues = Arrays.asList(TYPE_CONFIDENTIAL, TYPE_PUBLIC, TYPE_BEARERONLY);
        property.setOptions(updateProfileValues);
        configProperties.add(property);
    }

    /** @param session Keycloak 会话 @return ClientAccessTypeCondition 实例 */
    @Override
    public ClientPolicyConditionProvider create(KeycloakSession session) {
        return new ClientAccessTypeCondition(session);
    }

    /** @return 提供方 ID */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 条件帮助说明 */
    @Override
    public String getHelpText() {
        return "It uses the client's access type (confidential, public, bearer-only) to determine whether the policy is applied. Condition is checked during most of OpenID Connect requests (Authorization request, token requests, introspection endpoint request etc). "
                + "Confidential client has enabled client authentication when public client has disabled client authentication. Bearer-only is deprecated client type.";
    }

    /** @return 静态注册的配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
