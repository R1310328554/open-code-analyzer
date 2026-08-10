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

package org.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

/**
 * 用户属性映射器：将 {@link org.keycloak.models.UserModel} 自定义属性映射为令牌声明。
 * <p>声明名支持点分嵌套路径（如 {@code address.country}），会在令牌中生成嵌套 JSON 对象。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserAttributeMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
        property.setType(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE);
        configProperties.add(property);
        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, UserAttributeMapper.class);

        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.MULTIVALUED);
        property.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        property.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.AGGREGATE_ATTRS);
        property.setLabel(ProtocolMapperUtils.AGGREGATE_ATTRS_LABEL);
        property.setHelpText(ProtocolMapperUtils.AGGREGATE_ATTRS_HELP_TEXT);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(property);
    }

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "oidc-usermodel-attribute-mapper";


    /** {@inheritDoc} 含用户属性、多值与聚合属性等配置项 */
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

    /** {@inheritDoc} 归类为令牌映射器 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** {@inheritDoc} 将自定义用户属性映射到令牌声明 */
    @Override
    public String getHelpText() {
        return "Map a custom user attribute to a token claim.";
    }

    /** 解析用户属性值（可选聚合）并写入令牌声明 */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {

        UserModel user = userSession.getUser();
        String attributeName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE);
        boolean aggregateAttrs = Boolean.valueOf(mappingModel.getConfig().get(ProtocolMapperUtils.AGGREGATE_ATTRS));
        Collection<String> attributeValue = KeycloakModelUtils.resolveAttribute(user, attributeName, aggregateAttrs);
        if (attributeValue == null) return;
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, attributeValue);
    }

    /** 工厂方法：创建用户属性映射器（指定是否多值） */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userAttribute,
                                                        String tokenClaimName, String claimType,
                                                        boolean accessToken, boolean idToken, boolean introspectionEndpoint, boolean multivalued) {
        return createClaimMapper(name, userAttribute, tokenClaimName, claimType,
                accessToken, idToken, introspectionEndpoint, multivalued, false);
    }

    /**
     * 工厂方法：创建用户属性映射器（多值与聚合选项）。
     * @param aggregateAttrs 是否聚合同名属性
     */
        ProtocolMapperModel mapper = OIDCAttributeMapperHelper.createClaimMapper(name, userAttribute,
                tokenClaimName, claimType,
                accessToken, idToken, introspectionEndpoint,
                PROVIDER_ID);

        if (multivalued) {
            mapper.getConfig().put(ProtocolMapperUtils.MULTIVALUED, "true");
        }
        if (aggregateAttrs) {
            mapper.getConfig().put(ProtocolMapperUtils.AGGREGATE_ATTRS, "true");
        }

        return mapper;
    }

    /** 工厂方法：创建默认单值、非聚合的用户属性映射器 */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userAttribute,
                                                        String tokenClaimName, String claimType,
                                                        boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        return createClaimMapper(name, userAttribute, tokenClaimName, claimType,
                accessToken, idToken, introspectionEndpoint, false, false);
    }
}
