/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.OIDCWellKnownProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.TokenUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Claims 参数令牌映射器。
 * <p>将 OIDC 授权请求中 {@code claims} 参数标记为 essential 的声明写入 ID Token 或 UserInfo 响应。</p>
 */
public class ClaimsParameterTokenMapper extends AbstractOIDCProtocolMapper implements OIDCIDTokenMapper, UserInfoTokenMapper {

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-claims-param-token-mapper";

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, ClaimsParameterTokenMapper.class);
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Claims parameter Token";
    }

    /** @return 映射器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Claims specified by Claims parameter are put into tokens.";
    }

    /** @return 配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** 根据 claims 参数向 ID Token 或 UserInfo 写入 essential 声明 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        String claims = clientSessionCtx.getClientSession().getNote(OIDCLoginProtocol.CLAIMS_PARAM);
        if (claims == null) return;

        if (TokenUtil.TOKEN_TYPE_ID.equals(token.getType())) {
            // 处理 ID Token
            putClaims("id_token", claims, token, mappingModel, userSession);
        } else {
            // 处理 UserInfo 响应
            putClaims("userinfo", claims, token, mappingModel, userSession);
        }
    }

    /** 解析 claims 参数并按令牌类型写入 essential 声明 @param tokenType 令牌类型键（id_token/userinfo） */
    private void putClaims(String tokenType, String claims, IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        JsonNode requestParams = null;

        try {
            requestParams = JsonSerialization.readValue(claims, JsonNode.class);
        } catch (IOException e) {
            return;
        }
        if (!requestParams.has(tokenType)) return;

        JsonNode tokenNode = requestParams.findValue(tokenType);

        OIDCWellKnownProvider.DEFAULT_CLAIMS_SUPPORTED.stream()
            .filter(i->tokenNode.has(i))
            .filter(i->tokenNode.findValue(i).has("essential"))
            .filter(i->tokenNode.findValue(i).findValue("essential").isBoolean())
            .filter(i->tokenNode.findValue(i).findValue("essential").asBoolean())
            .forEach(i -> {
                    // 将声明写入令牌
                    // aud/sub/iss/auth_time/acr 默认已设置；name/given_name 等需借助现有映射器显式填充
                    if (i.equals(IDToken.NAME)) {
                        FullNameMapper fullNameMapper = new FullNameMapper();
                        fullNameMapper.setClaim(token, mappingModel, userSession);
                    } else if (i.equals(IDToken.GIVEN_NAME)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested firstName", "firstName", IDToken.GIVEN_NAME, "String", false, true, false), userSession);
                    } else if (i.equals(IDToken.FAMILY_NAME)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested lastName", "lastName", IDToken.FAMILY_NAME, "String", false, true, false), userSession);
                    } else if (i.equals(IDToken.PREFERRED_USERNAME)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested username", "username", IDToken.PREFERRED_USERNAME, "String", false, true, false), userSession);
                    } else if (i.equals(IDToken.EMAIL)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested email", "email", IDToken.EMAIL, "String", false, true, false), userSession);
                    }
            });
    }

    /** 创建 Claims 参数映射器 @param name 名称 @param idToken 是否写入 ID Token @param userInfo 是否写入 UserInfo @return 协议映射器模型 */
    public static ProtocolMapperModel createMapper(String name, boolean idToken, boolean userInfo) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<String, String>();
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (userInfo) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, "true");
        mapper.setConfig(config);
        return mapper;
    }

}
