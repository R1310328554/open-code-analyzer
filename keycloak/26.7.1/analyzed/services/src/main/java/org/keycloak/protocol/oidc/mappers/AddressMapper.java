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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AddressClaimSet;
import org.keycloak.representations.IDToken;

/**
 * OIDC 地址声明映射器。
 * <p>将用户地址属性（街道、地区、省/州、邮编、国家等）映射到 OpenID Connect {@code address} 声明。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AddressMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    /** 街道地址属性名 */
    public static final String STREET = "street";

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AddressMapper.class);

        configProperties.add(createConfigProperty(STREET));
        configProperties.add(createConfigProperty(AddressClaimSet.LOCALITY));
        configProperties.add(createConfigProperty(AddressClaimSet.REGION));
        configProperties.add(createConfigProperty(AddressClaimSet.POSTAL_CODE));
        configProperties.add(createConfigProperty(AddressClaimSet.COUNTRY));
        configProperties.add(createConfigProperty(AddressClaimSet.FORMATTED));
    }

    /** 为指定地址子声明创建配置属性 @param claimName 声明名 @return 配置属性 */
    protected static ProviderConfigProperty createConfigProperty(String claimName) {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(getModelPropertyName(claimName));
        property.setLabel("addressClaim." + claimName + ".label");
        property.setHelpText("addressClaim." + claimName + ".tooltip");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue(claimName);
        return property;
    }

    /** 将声明名转换为模型配置键 @param claimName 声明名 @return 配置键名 */
    public static String getModelPropertyName(String claimName) {
        return "user.attribute." + claimName;
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-address-mapper";

    /** 创建默认地址映射器（全部令牌类型均包含） @return 协议映射器模型 */
    public static ProtocolMapperModel createAddressMapper() {
        return createAddressMapper(true, true, true, true);
    }

    /**
     * 创建地址映射器。
     * @param idToken 是否写入 ID Token
     * @param accessToken 是否写入访问令牌
     * @param userInfo 是否写入 UserInfo
     * @param introspectionEndpoint 是否写入自省端点响应
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel createAddressMapper(boolean idToken, boolean accessToken, boolean userInfo, boolean introspectionEndpoint) {
        Map<String, String> config;
        ProtocolMapperModel address = new ProtocolMapperModel();
        address.setName("address");
        address.setProtocolMapper(PROVIDER_ID);
        address.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, Boolean.toString(accessToken));
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, Boolean.toString(idToken));
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, Boolean.toString(userInfo));
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, Boolean.toString(introspectionEndpoint));

        config.put(getModelPropertyName(STREET), STREET);
        config.put(getModelPropertyName(AddressClaimSet.LOCALITY), AddressClaimSet.LOCALITY);
        config.put(getModelPropertyName(AddressClaimSet.REGION), AddressClaimSet.REGION);
        config.put(getModelPropertyName(AddressClaimSet.POSTAL_CODE), AddressClaimSet.POSTAL_CODE);
        config.put(getModelPropertyName(AddressClaimSet.COUNTRY), AddressClaimSet.COUNTRY);
        config.put(getModelPropertyName(AddressClaimSet.FORMATTED), AddressClaimSet.FORMATTED);

        address.setConfig(config);
        return address;
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
        return "User Address";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Maps user address attributes (street, locality, region, postal_code, and country) to the OpenID Connect 'address' claim.";
    }

    /**
     * 从用户属性填充 {@code address} 声明。
     * @param token 目标令牌
     * @param mappingModel 映射器配置
     * @param userSession 用户会话
     */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        UserModel user = userSession.getUser();
        Map<String, Object> addressSet = Optional.ofNullable(token.getAddressClaimsMap()).orElseGet(() -> Optional.ofNullable(token.getOtherClaims().get(IDToken.ADDRESS))
                .filter(Map.class::isInstance)
                .map(o -> (HashMap<String, Object>) o)
                .orElseGet(HashMap::new));
        Optional.ofNullable(getUserModelAttributeValue(user, mappingModel, STREET))
                .ifPresent(street -> addressSet.put(AddressClaimSet.STREET_ADDRESS, street));
        Optional.ofNullable(getUserModelAttributeValue(user, mappingModel, AddressClaimSet.LOCALITY))
                .ifPresent(locality -> addressSet.put(AddressClaimSet.LOCALITY, locality));
        Optional.ofNullable(getUserModelAttributeValue(user, mappingModel, AddressClaimSet.REGION))
                .ifPresent(region -> addressSet.put(AddressClaimSet.REGION, region));
        Optional.ofNullable(getUserModelAttributeValue(user, mappingModel, AddressClaimSet.POSTAL_CODE))
                .ifPresent(postalCode -> addressSet.put(AddressClaimSet.POSTAL_CODE, postalCode));
        Optional.ofNullable(getUserModelAttributeValue(user, mappingModel, AddressClaimSet.COUNTRY))
                .ifPresent(country -> addressSet.put(AddressClaimSet.COUNTRY, country));
        Optional.ofNullable(getUserModelAttributeValue(user, mappingModel, AddressClaimSet.FORMATTED))
                .ifPresent(formatted -> addressSet.put(AddressClaimSet.FORMATTED, formatted));

        if (!addressSet.isEmpty()) {
            token.setAddress(addressSet);
        }
    }

    /** 读取用户模型上的地址属性值 @param user 用户 @param mappingModel 映射配置 @param claim 声明名 @return 属性值 */
    private String getUserModelAttributeValue(UserModel user, ProtocolMapperModel mappingModel, String claim) {
        String modelPropertyName = getModelPropertyName(claim);
        String userAttrName = mappingModel.getConfig().get(modelPropertyName);

        if (userAttrName == null) {
            userAttrName = claim;
        }

        return user.getFirstAttribute(userAttrName);
    }

}
