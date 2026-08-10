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

package org.keycloak.organization.protocol.mappers.saml;

import java.util.List;
import java.util.stream.Stream;

import org.keycloak.Config.Scope;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.protocol.saml.mappers.AbstractSAMLProtocolMapper;
import org.keycloak.protocol.saml.mappers.AttributeStatementHelper;
import org.keycloak.protocol.saml.mappers.SAMLAttributeStatementMapper;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;

import static org.keycloak.organization.utils.Organizations.isEnabledAndOrganizationsPresent;

/**
 * SAML 组织成员协议映射器：在 SAML 断言的属性声明中加入用户所属组织的别名。
 * <p>仅当组织特性启用且 Realm 中存在组织时生效；实现 {@link SAMLAttributeStatementMapper} 与 {@link EnvironmentDependentProviderFactory}。</p>
 */
public class OrganizationMembershipMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper, EnvironmentDependentProviderFactory {

    /** 协议映射器工厂 ID。 */
    public static final String ID = "saml-organization-membership-mapper";
    /** SAML 断言中组织成员属性的名称。 */
    public static final String ORGANIZATION_ATTRIBUTE_NAME = "organization";

    /** 创建默认的“organization” SAML 组织成员映射器配置。 */
    public static ProtocolMapperModel create() {
        ProtocolMapperModel mapper = new ProtocolMapperModel();

        mapper.setName("organization");
        mapper.setProtocolMapper(ID);
        mapper.setProtocol(SamlProtocol.LOGIN_PROTOCOL);

        return mapper;

    }

    /**
     * 将用户所属已启用组织的别名写入 SAML 属性声明。
     * @param attributeStatement 待填充的属性声明
     * @param mappingModel 协议映射器模型
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSession 客户端会话
     */
    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        OrganizationProvider provider = session.getProvider(OrganizationProvider.class);

        if (!isEnabledAndOrganizationsPresent(provider)) {
            return;
        }

        UserModel user = userSession.getUser();
        Stream<OrganizationModel> organizations = provider.getByMember(user).filter(OrganizationModel::isEnabled);
        AttributeType attribute = new AttributeType(ORGANIZATION_ATTRIBUTE_NAME);

        attribute.setFriendlyName(ORGANIZATION_ATTRIBUTE_NAME);
        attribute.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get());

        organizations.forEach(organization -> {
            attribute.addAttributeValue(organization.getAlias());
        });

        if (attribute.getAttributeValue().isEmpty()) {
            return;
        }

        attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attribute));
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public String getId() {
        return ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Organization Membership";
    }

    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    /** @return 映射器帮助说明文本 */
    @Override
    public String getHelpText() {
        return "Add an attribute to the assertion with information about the organization membership.";
    }

    /** 仅当 {@link Feature#ORGANIZATION} 特性启用时支持此映射器。 */
    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.ORGANIZATION);
    }
}
