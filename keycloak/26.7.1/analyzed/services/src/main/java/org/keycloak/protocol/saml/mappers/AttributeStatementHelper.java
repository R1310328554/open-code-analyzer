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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;

/**
 * SAML AttributeStatement 构建辅助类。
 * <p>封装属性名、NameFormat、FriendlyName 配置，以及向断言添加单值/多值属性的通用逻辑。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AttributeStatementHelper {
    /** 配置键：SAML 属性名 */
    public static final String SAML_ATTRIBUTE_NAME = "attribute.name";
    /** 管理控制台映射器分类标签 */
    public static final String ATTRIBUTE_STATEMENT_CATEGORY = "AttributeStatement Mapper";
    /** 配置键：SAML FriendlyName */
    public static final String FRIENDLY_NAME = "friendly.name";
    /** FriendlyName 配置项标签 */
    public static final String FRIENDLY_NAME_LABEL = "Friendly Name";
    public static final String FRIENDLY_NAME_HELP_TEXT = "Standard SAML attribute setting.  An optional, more human-readable form of the attribute's name that can be provided if the actual attribute name is cryptic.";
    /** 配置键：SAML Attribute NameFormat */
    public static final String SAML_ATTRIBUTE_NAMEFORMAT = "attribute.nameformat";
    /** NameFormat 选项：Basic */
    public static final String BASIC = "Basic";
    /** NameFormat 选项：URI Reference */
    public static final String URI_REFERENCE = "URI Reference";
    /** NameFormat 选项：Unspecified */
    public static final String UNSPECIFIED = "Unspecified";

    /** 向 AttributeStatement 添加单值属性 @param attributeStatement 目标语句 @param mappingModel 映射配置 @param attributeValue 属性值 */
    public static void addAttribute(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel,
                                    String attributeValue) {
        AttributeType attribute = createAttributeType(mappingModel);
        attribute.addAttributeValue(attributeValue);
        attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attribute));
    }

    /** 向 AttributeStatement 添加多值属性 @param attributeValues 属性值集合 */
    public static void addAttributes(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel,
                                    Collection<String> attributeValues) {

        AttributeType attribute = createAttributeType(mappingModel);
        attributeValues.forEach(attribute::addAttributeValue);

        attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attribute));
    }

    /** 根据映射配置创建 {@link AttributeType}（含 NameFormat 与 FriendlyName） @return 属性类型对象 */
    public static AttributeType createAttributeType(ProtocolMapperModel mappingModel) {
        String attributeName = mappingModel.getConfig().get(SAML_ATTRIBUTE_NAME);
        AttributeType attribute = new AttributeType(attributeName);
        String attributeType = mappingModel.getConfig().get(SAML_ATTRIBUTE_NAMEFORMAT);
        String attributeNameFormat = JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get();
        if (URI_REFERENCE.equals(attributeType)) attributeNameFormat = JBossSAMLURIConstants.ATTRIBUTE_FORMAT_URI.get();
        else if (UNSPECIFIED.equals(attributeType)) attributeNameFormat = JBossSAMLURIConstants.ATTRIBUTE_FORMAT_UNSPECIFIED.get();
        attribute.setNameFormat(attributeNameFormat);
        String friendlyName = mappingModel.getConfig().get(FRIENDLY_NAME);
        if (friendlyName != null && !friendlyName.trim().equals("")) attribute.setFriendlyName(friendlyName);
        return attribute;
    }

    /** 向配置列表追加 FriendlyName、属性名与 NameFormat 选项 @param configProperties 目标配置列表 */
    public static void setConfigProperties(List<ProviderConfigProperty> configProperties) {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(AttributeStatementHelper.FRIENDLY_NAME);
        property.setLabel(AttributeStatementHelper.FRIENDLY_NAME_LABEL);
        property.setHelpText(AttributeStatementHelper.FRIENDLY_NAME_HELP_TEXT);
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(AttributeStatementHelper.SAML_ATTRIBUTE_NAME);
        property.setLabel("SAML Attribute Name");
        property.setHelpText("SAML Attribute Name");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(AttributeStatementHelper.SAML_ATTRIBUTE_NAMEFORMAT);
        property.setLabel("SAML Attribute NameFormat");
        property.setHelpText("SAML Attribute NameFormat.  Can be basic, URI reference, or unspecified.");
        List<String> types = new ArrayList<>(3);
        types.add(AttributeStatementHelper.BASIC);
        types.add(AttributeStatementHelper.URI_REFERENCE);
        types.add(AttributeStatementHelper.UNSPECIFIED);
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(types);
        configProperties.add(property);

    }
    /**
     * 创建属性映射器模型。
     * @param name 映射器名称
     * @param userAttribute 用户属性名（可为 null）
     * @param samlAttributeName SAML 属性名
     * @param nameFormat NameFormat 选项
     * @param friendlyName FriendlyName
     * @param mapperId 映射器提供方 ID
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel createAttributeMapper(String name, String userAttribute, String samlAttributeName, String nameFormat,  String friendlyName, String mapperId) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(SamlProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (userAttribute != null) config.put(ProtocolMapperUtils.USER_ATTRIBUTE, userAttribute);
        config.put(SAML_ATTRIBUTE_NAME, samlAttributeName);
        if (friendlyName != null) {
            config.put(FRIENDLY_NAME, friendlyName);
        }
        if (nameFormat != null) {
            config.put(SAML_ATTRIBUTE_NAMEFORMAT, nameFormat);
        }
        mapper.setConfig(config);
        return mapper;
    }
}
