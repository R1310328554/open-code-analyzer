/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
import org.keycloak.representations.IDToken;

/**
 * 受众（Audience）协议映射器。
 * <p>向令牌的 {@code aud} 字段添加指定的客户端或自定义受众值。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AudienceProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /** 配置键：包含的客户端受众 */
    public static final String INCLUDED_CLIENT_AUDIENCE = "included.client.audience";
    /** 客户端受众配置项标签键 */
    private static final String INCLUDED_CLIENT_AUDIENCE_LABEL = "included.client.audience.label";
    /** 客户端受众配置项帮助文本键 */
    private static final String INCLUDED_CLIENT_AUDIENCE_HELP_TEXT = "included.client.audience.tooltip";

    /** 配置键：自定义受众字符串 */
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


        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AudienceProtocolMapper.class);

        // 默认不在 ID Token 中包含受众
        for (ProviderConfigProperty prop : configProperties) {
            if (OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN.equals(prop.getName())) {
                prop.setDefaultValue("false");
            }
        }
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-audience-mapper";


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
        return "Audience";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Add specified audience to the audience (aud) field of token";
    }

    /** 将配置的受众值添加到令牌 @param token 目标令牌 @param mappingModel 映射配置 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        String audienceValue = mappingModel.getConfig().get(INCLUDED_CLIENT_AUDIENCE);

        if (audienceValue == null) {
            // 回退到自定义受众
            audienceValue = mappingModel.getConfig().get(INCLUDED_CUSTOM_AUDIENCE);
        }

        if (audienceValue == null) return;
        token.addAudience(audienceValue);
    }

    /** 创建受众映射器（不含轻量访问令牌选项） @return 协议映射器模型 */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String includedClientAudience,
                                                        String includedCustomAudience,
                                                        boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        return createClaimMapper(name, includedClientAudience, includedCustomAudience, accessToken, idToken, introspectionEndpoint, false);
    }

    /**
     * 创建受众映射器。
     * @param name 映射器名称
     * @param includedClientAudience 客户端受众
     * @param includedCustomAudience 自定义受众
     * @param accessToken 是否写入访问令牌
     * @param idToken 是否写入 ID Token
     * @param introspectionEndpoint 是否写入自省端点
     * @param lightweightAccessToken 是否写入轻量访问令牌
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String includedClientAudience,
                                                        String includedCustomAudience,
                                                        boolean accessToken, boolean idToken, boolean introspectionEndpoint, boolean lightweightAccessToken) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);

        Map<String, String> config = new HashMap<>();
        if (includedClientAudience != null) {
            config.put(INCLUDED_CLIENT_AUDIENCE, includedClientAudience);
        }
        if (includedCustomAudience != null) {
            config.put(INCLUDED_CUSTOM_AUDIENCE, includedCustomAudience);
        }

        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        if (lightweightAccessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN, "true");
        mapper.setConfig(config);
        return mapper;
    }
}
