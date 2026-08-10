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
import org.keycloak.models.UserSessionNoteDescriptor;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

/**
 * 用户会话备注映射器。
 * <p>将 {@link UserSessionModel} 会话备注（note）映射到 OIDC 令牌或令牌响应声明。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserSessionNoteMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, OIDCAccessTokenResponseMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_SESSION_NOTE);
        property.setLabel(ProtocolMapperUtils.USER_SESSION_MODEL_NOTE_LABEL);
        property.setHelpText(ProtocolMapperUtils.USER_SESSION_MODEL_NOTE_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setRequired(true);
        configProperties.add(property);
        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, UserSessionNoteMapper.class);
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-usersessionmodel-note-mapper";


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
        return "User Session Note";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Map a custom user session note to a token claim.";
    }

    /** 将会话备注写入 ID Token 类声明 @param token 目标令牌 @param userSession 用户会话 */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {

        String noteName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_SESSION_NOTE);
        String noteValue = userSession.getNote(noteName);
        if (noteValue == null) return;
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, noteValue);
    }

    /** 将会话备注写入访问令牌响应 @param accessTokenResponse 令牌响应 */
    @Override
    protected void setClaim(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                            KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {

        String noteName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_SESSION_NOTE);
        String noteValue = userSession.getNote(noteName);
        if (noteValue == null) return;
        OIDCAttributeMapperHelper.mapClaim(accessTokenResponse, mappingModel, noteValue);
    }

    /** 创建会话备注映射器（不含 UserInfo） @return 协议映射器模型 */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userSessionNote,
                                                        String tokenClaimName, String jsonType,
                                                        boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        return createClaimMapper(name, userSessionNote, tokenClaimName, jsonType, accessToken, idToken, false, introspectionEndpoint);
    }

    /**
     * 创建会话备注映射器。
     * @param name 映射器名称
     * @param userSessionNote 会话备注键
     * @param tokenClaimName 令牌声明名
     * @param jsonType JSON 类型
     * @param accessToken 是否写入访问令牌
     * @param idToken 是否写入 ID Token
     * @param userInfo 是否写入 UserInfo
     * @param introspectionEndpoint 是否写入自省端点
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userSessionNote,
                                                        String tokenClaimName, String jsonType,
                                                        boolean accessToken, boolean idToken, boolean userInfo, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(ProtocolMapperUtils.USER_SESSION_NOTE, userSessionNote);
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, tokenClaimName);
        config.put(OIDCAttributeMapperHelper.JSON_TYPE, jsonType);
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (userInfo) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);
        return mapper;
    }

    /**
     * 基于 {@link UserSessionNoteDescriptor} 枚举创建预定义会话备注映射器。
     *
     * @param userSessionNoteDescriptor 会话备注描述符
     */
    public static ProtocolMapperModel createUserSessionNoteMapper(UserSessionNoteDescriptor userSessionNoteDescriptor) {
        return UserSessionNoteMapper.createClaimMapper(
                userSessionNoteDescriptor.getDisplayName(),
                userSessionNoteDescriptor.toString(),
                userSessionNoteDescriptor.getTokenClaim(),
                "String",
                true, true, true
        );
    }

}
