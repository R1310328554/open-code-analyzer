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

import org.keycloak.Config;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN;
import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN;
import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION;
import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO;

/**
 * OIDC 协议映射器抽象基类：统一处理 ID Token、Access Token、UserInfo、Introspection 等声明注入。
 * <p>子类实现 {@link #setClaim} 向各类令牌写入自定义声明。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractOIDCProtocolMapper implements ProtocolMapper {

    /** 管理控制台中令牌映射器分类名 */
    public static final String TOKEN_MAPPER_CATEGORY = "Token mapper";

    @Override
    public String getProtocol() {
        return OIDCLoginProtocol.LOGIN_PROTOCOL;
    }

    @Override
    public void close() {

    }

    @Override
    public final ProtocolMapper create(KeycloakSession session) {
        throw new RuntimeException("UNSUPPORTED METHOD");
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** 向 UserInfo 令牌注入声明（若配置包含） */
    public AccessToken transformUserInfoToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                              UserSessionModel userSession, ClientSessionContext clientSessionCtx) {

        if (!OIDCAttributeMapperHelper.includeInUserInfo(mappingModel)) {
            return token;
        }

        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /** 判断是否应使用轻量级访问令牌 */
    public static boolean getShouldUseLightweightToken(KeycloakSession session) {
        Object attributeValue = session.getAttribute(Constants.USE_LIGHTWEIGHT_ACCESS_TOKEN_ENABLED);
        return Boolean.parseBoolean(session.getContext().getClient().getAttribute(Constants.USE_LIGHTWEIGHT_ACCESS_TOKEN_ENABLED)) || (attributeValue != null && (boolean) attributeValue);
    }

    /** 向 Access Token 注入声明（支持轻量级令牌配置） */
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        boolean shouldUseLightweightToken = getShouldUseLightweightToken(session);
        boolean includeInAccessToken = shouldUseLightweightToken ? OIDCAttributeMapperHelper.includeInLightweightAccessToken(mappingModel) : OIDCAttributeMapperHelper.includeInAccessToken(mappingModel);
        if (!includeInAccessToken) {
            return token;
        }

        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /** 向 ID Token 注入声明 */
    public IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                    UserSessionModel userSession, ClientSessionContext clientSessionCtx) {

        if (!OIDCAttributeMapperHelper.includeInIDToken(mappingModel)) {
            return token;
        }

        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /** 向令牌端点 JSON 响应注入声明 */
    public AccessTokenResponse transformAccessTokenResponse(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel,
                                                            KeycloakSession session, UserSessionModel userSession,
                                                            ClientSessionContext clientSessionCtx) {

        if (!OIDCAttributeMapperHelper.includeInAccessTokenResponse(mappingModel)) {
            return accessTokenResponse;
        }

        setClaim(accessTokenResponse, mappingModel, userSession, session, clientSessionCtx);
        return accessTokenResponse;
    }

    /** 向 Introspection 响应令牌注入声明 */
    public AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                                   UserSessionModel userSession, ClientSessionContext clientSessionCtx) {

        if (!OIDCAttributeMapperHelper.includeInIntrospection(mappingModel)) {
            return token;
        }

        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /**
     * 向令牌添加声明（旧版三参数重载，已弃用）。
     * @param token 目标令牌
     * @param mappingModel 映射器配置
     * @param userSession 用户会话
     * @deprecated 请改用带 KeycloakSession 与 ClientSessionContext 的重载
     */
    @Deprecated
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
    }

    /**
     * 向 ID/Access Token 添加声明，子类应覆盖此方法。
     * @param token 目标令牌
     * @param mappingModel 映射器配置
     * @param userSession 用户会话
     * @param keycloakSession Keycloak 会话
     * @param clientSessionCtx 客户端会话上下文
     */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {
        // 向后兼容：委托旧版 setClaim 重载
        setClaim(token, mappingModel, userSession);
    }

    /**
     * 向 AccessTokenResponse 添加声明，子类可覆盖。
     * @param accessTokenResponse 令牌响应
     * @param mappingModel 映射器配置
     * @param userSession 用户会话
     * @param keycloakSession Keycloak 会话
     * @param clientSessionCtx 客户端会话上下文
     */
    protected void setClaim(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {

    }

    /**
     * 计算有效映射器模型：补全 UserInfo/Introspection 包含标志的默认值。
     */
        // 克隆配置
        ProtocolMapperModel copy = RepresentationToModel.toModel(ModelToRepresentation.toRepresentation(protocolMapperModel));

        // UserInfo 默认与 ID Token 包含设置一致
        if (copy.getConfig().get(INCLUDE_IN_ID_TOKEN) != null) {
            copy.getConfig().put(INCLUDE_IN_USERINFO, String.valueOf(OIDCAttributeMapperHelper.includeInUserInfo(protocolMapperModel)));
        }

        // Introspection 默认与 Access Token 包含设置一致
        if (copy.getConfig().get(INCLUDE_IN_ACCESS_TOKEN) != null) {
            copy.getConfig().put(INCLUDE_IN_INTROSPECTION, String.valueOf(OIDCAttributeMapperHelper.includeInIntrospection(protocolMapperModel)));
        }

        return copy;
    }
}
