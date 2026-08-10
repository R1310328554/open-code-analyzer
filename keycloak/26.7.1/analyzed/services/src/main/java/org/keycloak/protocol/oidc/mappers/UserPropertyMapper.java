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
import java.util.List;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

/**
 * 用户属性协议映射器。
 * <p>将 {@link UserModel} 属性（getter 方法对应的属性名）映射到 ID Token 声明；声明名可为嵌套路径（如 {@code address.country}），会在令牌中生成嵌套 JSON 对象。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserPropertyMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {
    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_PROPERTY_LABEL);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_PROPERTY_HELP_TEXT);
        configProperties.add(property);
        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, UserPropertyMapper.class);
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-usermodel-property-mapper";


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
        return "User Property";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Map a built in user property (email, firstName, lastName) to a token claim.";
    }

    /**
     * 读取用户模型属性并写入令牌声明。
     * @param token 目标令牌
     * @param mappingModel 映射器配置
     * @param userSession 用户会话
     */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        UserModel user = userSession.getUser();
        String propertyName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE);

        if (propertyName == null || propertyName.trim().isEmpty()) return;

        String propertyValue = ProtocolMapperUtils.getUserModelValue(user, propertyName);
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, propertyValue);
    }

    /**
     * 创建用户属性映射器。
     * @param name 映射器名称
     * @param userAttribute 用户属性名
     * @param tokenClaimName 令牌声明名
     * @param claimType JSON 类型
     * @param accessToken 是否写入访问令牌
     * @param idToken 是否写入 ID Token
     * @param introspectionEndpoint 是否写入自省端点
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userAttribute,
                                                        String tokenClaimName, String claimType,
                                                        boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        return OIDCAttributeMapperHelper.createClaimMapper(name, userAttribute,
                tokenClaimName, claimType,
                accessToken, idToken, introspectionEndpoint,
                PROVIDER_ID);
    }
}
