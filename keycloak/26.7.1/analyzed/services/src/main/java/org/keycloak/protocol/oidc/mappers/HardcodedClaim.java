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

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

/**
 * 硬编码声明映射器。
 * <p>向访问令牌、ID Token、UserInfo、令牌响应或自省响应写入固定配置的声明值。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HardcodedClaim extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper,
        OIDCAccessTokenResponseMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    /** 配置键：硬编码声明值 */
    public static final String CLAIM_VALUE = "claim.value";

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(CLAIM_VALUE);
        property.setLabel("Claim value");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Value of the claim you want to hard code.  'true' and 'false can be used for boolean values.");
        configProperties.add(property);

        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, HardcodedClaim.class);
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-hardcoded-claim-mapper";


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
        return "Hardcoded claim";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Hardcode a claim into the token.";
    }

    /** 将硬编码值写入 ID Token 类声明 @param token 目标令牌 @param mappingModel 映射配置 */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {

        String attributeValue = mappingModel.getConfig().get(CLAIM_VALUE);
        if (attributeValue == null) return;
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, attributeValue);
    }

    /** 将硬编码值写入访问令牌响应 @param accessTokenResponse 令牌响应 @param mappingModel 映射配置 */
    @Override
    protected void setClaim(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                            KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {

        String attributeValue = mappingModel.getConfig().get(CLAIM_VALUE);
        if (attributeValue == null) return;
        OIDCAttributeMapperHelper.mapClaim(accessTokenResponse, mappingModel, attributeValue);
    }

    /**
     * 创建硬编码声明映射器。
     * @param name 映射器名称
     * @param hardcodedName 声明名
     * @param hardcodedValue 声明值
     * @param claimType JSON 类型
     * @param accessToken 是否写入访问令牌
     * @param idToken 是否写入 ID Token
     * @param introspectionEndpoint 是否写入自省端点
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel create(String name,
                                             String hardcodedName,
                                             String hardcodedValue, String claimType,
                                             boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, hardcodedName);
        config.put(CLAIM_VALUE, hardcodedValue);
        config.put(OIDCAttributeMapperHelper.JSON_TYPE, claimType);
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);
        return mapper;
    }


}
