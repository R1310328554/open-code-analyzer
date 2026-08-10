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
 * 用户会话 Note AttributeStatement 映射器：将 {@link UserSessionModel} 中的 session note 写入 SAML 属性。
 * <p>适用于认证 flow 或上游步骤写入会话 note 后需传递给 SP 的场景。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserSessionNoteStatementMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper {
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName("note");
        property.setLabel("User Session Note Attribute");
        property.setHelpText("The user session note you want to grab the value from.");
        configProperties.add(property);
        AttributeStatementHelper.setConfigProperties(configProperties);

    }

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "saml-user-session-note-mapper";


    /** {@inheritDoc} 含 note 键名与 SAML 属性配置 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：User Session Note */
    @Override
    public String getDisplayType() {
        return "User Session Note";
    }

    /** {@inheritDoc} 归类为 AttributeStatement 映射器 */
    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    /** {@inheritDoc} 将用户会话 note 映射为 SAML 属性 */
    @Override
    public String getHelpText() {
        return "Map a user session note to a SAML attribute.";
    }

    /** 读取配置的 session note 并写入 AttributeStatement */
    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        String note = mappingModel.getConfig().get("note");
        String value = userSession.getNote(note);
        if (value == null) return;
        AttributeStatementHelper.addAttribute(attributeStatement, mappingModel, value);

    }
}
