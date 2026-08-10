/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.AudienceRestrictionType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

/**
 * SAML 受众（Audience）协议映射器。
 * <p>向断言 Conditions 中的 AudienceRestriction 添加指定受众 URI；优先使用配置的客户端 ID，否则使用自定义 URI（行为与 OIDC 受众映射器一致）。</p>
 *
 * @author rmartinc
 */
public class SAMLAudienceProtocolMapper extends AbstractSAMLProtocolMapper implements SAMLLoginResponseMapper {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(SAMLAudienceProtocolMapper.class);

    /** 提供方标识 */
    public static final String PROVIDER_ID = "saml-audience-mapper";

    /** 映射器分类标签 */
    public static final String AUDIENCE_CATEGORY = "Audience mapper";

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    /** 配置键：包含的客户端受众（clientId） */
    public static final String INCLUDED_CLIENT_AUDIENCE = "included.client.audience";
    private static final String INCLUDED_CLIENT_AUDIENCE_LABEL = "included.client.audience.label";
    private static final String INCLUDED_CLIENT_AUDIENCE_HELP_TEXT = "included.client.audience.tooltip";

    /** 配置键：自定义受众 URI */
    public static final String INCLUDED_CUSTOM_AUDIENCE = "included.custom.audience";
    private static final String INCLUDED_CUSTOM_AUDIENCE_LABEL = "included.custom.audience.label";
    private static final String INCLUDED_CUSTOM_AUDIENCE_HELP_TEXT = "included.custom.audience.tooltip";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(INCLUDED_CLIENT_AUDIENCE);
        property.setLabel(INCLUDED_CLIENT_AUDIENCE_LABEL);
        property.setHelpText(INCLUDED_CLIENT_AUDIENCE_HELP_TEXT);
        property.setType(ProviderConfigProperty.CLIENT_LIST_TYPE);
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(INCLUDED_CUSTOM_AUDIENCE);
        property.setLabel(INCLUDED_CUSTOM_AUDIENCE_LABEL);
        property.setHelpText(INCLUDED_CUSTOM_AUDIENCE_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
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

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Audience";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return AUDIENCE_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Add specified audience to the audience conditions in the assertion.";
    }

    /** 从 SAML 响应断言 Conditions 中定位首个 AudienceRestriction @param response SAML 响应 @return 受众限制或 null */
    protected static AudienceRestrictionType locateAudienceRestriction(ResponseType response) {
        try {
            return response.getAssertions().get(0).getAssertion().getConditions().getConditions()
                    .stream()
                    .filter(AudienceRestrictionType.class::isInstance)
                    .map(AudienceRestrictionType.class::cast)
                    .findFirst().orElse(null);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            logger.warn("Invalid SAML ResponseType to add the audience restriction", e);
            return null;
        }
    }

    /** 向断言添加配置的受众 URI @return 转换后的 SAML 响应 */
    @Override
    public ResponseType transformLoginResponse(ResponseType response,
            ProtocolMapperModel mappingModel, KeycloakSession session,
            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // 读取配置：优先 clientId，其次自定义 URI（与 OIDC 一致）
        String audience = mappingModel.getConfig().get(INCLUDED_CLIENT_AUDIENCE);
        if (audience == null || audience.isEmpty()) {
            audience = mappingModel.getConfig().get(INCLUDED_CUSTOM_AUDIENCE);
        }
        // 定位首个含 AudienceRestriction 的 Conditions
        if (audience != null && !audience.isEmpty()) {
            AudienceRestrictionType aud = locateAudienceRestriction(response);
            if (aud != null) {
                logger.debugf("adding audience: %s", audience);
                try {
                    aud.addAudience(URI.create(audience));
                } catch (IllegalArgumentException e) {
                    logger.warnf(e, "Invalid URI syntax for audience: %s", audience);
                }
            }
        }
        return response;
    }

}
