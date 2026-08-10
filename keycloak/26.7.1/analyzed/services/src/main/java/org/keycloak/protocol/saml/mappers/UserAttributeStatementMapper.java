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
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 用户属性 AttributeStatement 映射器：将 {@link org.keycloak.models.UserModel} 自定义属性写入 SAML Assertion。
 * <p>映射的是 attributes 而非 getter 对应的内置属性；支持多值与聚合同名属性。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserAttributeStatementMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper {
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
        property.setType(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE);
        property.setRequired(Boolean.TRUE);
        configProperties.add(property);
        AttributeStatementHelper.setConfigProperties(configProperties);

        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.AGGREGATE_ATTRS);
        property.setLabel(ProtocolMapperUtils.AGGREGATE_ATTRS_LABEL);
        property.setHelpText(ProtocolMapperUtils.AGGREGATE_ATTRS_HELP_TEXT);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(property);
    }

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "saml-user-attribute-mapper";


    /** {@inheritDoc} 含用户属性、聚合选项及 SAML 属性配置 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：User Attribute */
    @Override
    public String getDisplayType() {
        return "User Attribute";
    }

    /** {@inheritDoc} 归类为 AttributeStatement 映射器 */
    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    /** {@inheritDoc} 将自定义用户属性映射为 SAML 属性 */
    @Override
    public String getHelpText() {
        return "Map a custom user attribute to a SAML attribute.";
    }

    /** 解析用户属性值（可选聚合）并写入 AttributeStatement */
    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        UserModel user = userSession.getUser();
        String attributeName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE);
        boolean aggregateAttrs = Boolean.valueOf(mappingModel.getConfig().get(ProtocolMapperUtils.AGGREGATE_ATTRS));
        Collection<String> attributeValues = KeycloakModelUtils.resolveAttribute(user, attributeName, aggregateAttrs);
        if (attributeValues.isEmpty()) return;
        AttributeStatementHelper.addAttributes(attributeStatement, mappingModel, attributeValues);
    }

    /**
     * 工厂方法：创建用户属性 SAML 映射器配置。
     * @param userAttribute 源用户属性名
     * @param samlAttributeName 目标 SAML 属性名
     */
        String mapperId = PROVIDER_ID;
        return AttributeStatementHelper.createAttributeMapper(name, userAttribute, samlAttributeName, nameFormat, friendlyName,
                mapperId);

    }

}
