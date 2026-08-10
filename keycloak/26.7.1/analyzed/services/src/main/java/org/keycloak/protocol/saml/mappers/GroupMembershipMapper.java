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
import java.util.concurrent.atomic.AtomicReference;

import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * SAML 用户组成员身份映射器。
 * <p>将用户所属组写入 SAML AttributeStatement，可选择完整组路径或仅组名，以及单属性多值或每组一属性两种模式。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class GroupMembershipMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper {
    /** 提供方标识 */
    public static final String PROVIDER_ID = "saml-group-membership-mapper";
    /** 配置键：是否使用单属性多值模式 */
    public static final String SINGLE_GROUP_ATTRIBUTE = "single";

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(AttributeStatementHelper.SAML_ATTRIBUTE_NAME);
        property.setLabel("Group attribute name");
        property.setDefaultValue("member");
        property.setHelpText("Name of the SAML attribute you want to put your groups into.  i.e. 'member', 'memberOf'.");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(AttributeStatementHelper.FRIENDLY_NAME);
        property.setLabel(AttributeStatementHelper.FRIENDLY_NAME_LABEL);
        property.setHelpText(AttributeStatementHelper.FRIENDLY_NAME_HELP_TEXT);
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(AttributeStatementHelper.SAML_ATTRIBUTE_NAMEFORMAT);
        property.setLabel("SAML Attribute NameFormat");
        property.setHelpText("SAML Attribute NameFormat.  Can be basic, URI reference, or unspecified.");
        List<String> types = new ArrayList(3);
        types.add(AttributeStatementHelper.BASIC);
        types.add(AttributeStatementHelper.URI_REFERENCE);
        types.add(AttributeStatementHelper.UNSPECIFIED);
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(types);
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(SINGLE_GROUP_ATTRIBUTE);
        property.setLabel("Single Group Attribute");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue("true");
        property.setHelpText("If true, all groups will be stored under one attribute with multiple attribute values.");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName("full.path");
        property.setLabel("Full group path");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue("true");
        property.setHelpText("Include full path to group i.e. /top/level1/level2, false will just specify the group name");
        configProperties.add(property);


    }


    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return "Group Mapper";
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Group list";
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Group names are stored in an attribute value.  There is either one attribute with multiple attribute values, or an attribute per group name depending on how you configure it.  You can also specify the attribute name i.e. 'member' or 'memberOf' being examples.";
    }

    /** @return 配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** @return 映射器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 是否使用完整组路径 @param mappingModel 映射配置 @return 为 true 时使用完整路径 */
    public static boolean useFullPath(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get("full.path"));
    }


    /**
     * 将用户组列表写入 SAML AttributeStatement。
     * @param attributeStatement 目标属性语句
     * @param mappingModel 映射配置
     * @param userSession 用户会话
     */
    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        String single = mappingModel.getConfig().get(SINGLE_GROUP_ATTRIBUTE);
        boolean singleAttribute = Boolean.parseBoolean(single);

        boolean fullPath = useFullPath(mappingModel);
        final AtomicReference<AttributeType> singleAttributeType = new AtomicReference<>(null);
        userSession.getUser().getGroupsStream().forEach(group -> {
            String groupName;
            if (fullPath) {
                groupName = ModelToRepresentation.buildGroupPath(group);
            } else {
                groupName = group.getName();
            }
            AttributeType attributeType;
            if (singleAttribute) {
                if (singleAttributeType.get() == null) {
                    singleAttributeType.set(AttributeStatementHelper.createAttributeType(mappingModel));
                    attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(singleAttributeType.get()));
                }
                attributeType = singleAttributeType.get();
            } else {
                attributeType = AttributeStatementHelper.createAttributeType(mappingModel);
                attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attributeType));
            }
            attributeType.addAttributeValue(groupName);
        });
    }

    /**
     * 创建组成员映射器。
     * @param name 映射器名称
     * @param samlAttributeName SAML 属性名
     * @param nameFormat NameFormat
     * @param friendlyName FriendlyName
     * @param singleAttribute 是否单属性多值
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel create(String name, String samlAttributeName, String nameFormat, String friendlyName, boolean singleAttribute) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(SamlProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<String, String>();
        config.put(AttributeStatementHelper.SAML_ATTRIBUTE_NAME, samlAttributeName);
        if (friendlyName != null) {
            config.put(AttributeStatementHelper.FRIENDLY_NAME, friendlyName);
        }
        if (nameFormat != null) {
            config.put(AttributeStatementHelper.SAML_ATTRIBUTE_NAMEFORMAT, nameFormat);
        }
        config.put(SINGLE_GROUP_ATTRIBUTE, Boolean.toString(singleAttribute));
        mapper.setConfig(config);

        return mapper;
    }

}
