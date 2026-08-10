/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.saml.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * SAML 硬编码角色映射器。
 * <p>向 SAML 断言预置配置的角色名，由 {@link RoleListMapper} 在角色列表映射阶段写入 AttributeStatement。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HardcodedRole extends AbstractSAMLProtocolMapper {
    /** 提供方标识 */
    public static final String PROVIDER_ID = "saml-hardcode-role-mapper";
    /** 配置键：属性值（未使用，保留兼容） */
    public static final String ATTRIBUTE_VALUE = "attribute.value";
    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    /** 配置键：硬编码角色名 */
    public static final String ROLE_ATTRIBUTE = "role";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ROLE_ATTRIBUTE);
        property.setLabel("Role");
        property.setHelpText("Arbitrary role name you want to hardcode.  This role does not have to exist in current realm and can be just any string you need");
        property.setType(ProviderConfigProperty.ROLE_TYPE);
        configProperties.add(property);
    }



    /** @return 配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    /** @return 映射器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Hardcoded role";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Hardcode role into SAML Assertion.";
    }

    /** 创建硬编码角色映射器 @param name 名称 @param role 角色名 @return 协议映射器模型 */
    public static ProtocolMapperModel create(String name,
                                             String role) {
        String mapperId = PROVIDER_ID;
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(SamlProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(ROLE_ATTRIBUTE, role);
        mapper.setConfig(config);
        return mapper;

    }

}
