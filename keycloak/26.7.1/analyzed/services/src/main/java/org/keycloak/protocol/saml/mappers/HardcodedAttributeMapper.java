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
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * SAML 硬编码属性映射器。
 * <p>向 SAML AttributeStatement 写入固定配置的属性值，不依赖用户模型属性。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HardcodedAttributeMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper {
    /** 提供方标识 */
    public static final String PROVIDER_ID = "saml-hardcode-attribute-mapper";
    /** 配置键：硬编码属性值 */
    public static final String ATTRIBUTE_VALUE = "attribute.value";
    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        AttributeStatementHelper.setConfigProperties(configProperties);
        property = new ProviderConfigProperty();
        property.setName(ATTRIBUTE_VALUE);
        property.setLabel("Attribute value");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Value of the attribute you want to hard code.");
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
        return "Hardcoded attribute";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Hardcode an attribute into the SAML Assertion.";
    }

    /** 将硬编码属性值写入 AttributeStatement @param attributeStatement 目标语句 @param mappingModel 映射配置 */
    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        String attributeValue = mappingModel.getConfig().get(ATTRIBUTE_VALUE);
        AttributeStatementHelper.addAttribute(attributeStatement, mappingModel, attributeValue);

    }

    /**
     * 创建硬编码属性映射器。
     * @param name 映射器名称
     * @param samlAttributeName SAML 属性名
     * @param nameFormat NameFormat
     * @param friendlyName FriendlyName
     * @param value 硬编码属性值
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel create(String name,
                                             String samlAttributeName, String nameFormat, String friendlyName, String value) {
        String mapperId = PROVIDER_ID;
        ProtocolMapperModel model = AttributeStatementHelper.createAttributeMapper(name, null, samlAttributeName, nameFormat, friendlyName,
                mapperId);
        model.getConfig().put(ATTRIBUTE_VALUE, value);
        return model;

    }

}
