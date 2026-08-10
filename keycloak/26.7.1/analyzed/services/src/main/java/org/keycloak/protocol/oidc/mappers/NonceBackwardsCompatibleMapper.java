/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.AccessToken;

/**
 * Nonce 向后兼容映射器。
 * <p>将 {@code nonce} 声明写入访问令牌，恢复旧版行为；添加此映射器后，访问、刷新与 ID 令牌均会包含 nonce。</p>
 *
 * @author rmartinc
 */
public class NonceBackwardsCompatibleMapper implements OIDCAccessTokenMapper, ProtocolMapper {

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-nonce-backwards-compatible-mapper";

    /** @return OIDC 登录协议标识 */
    @Override
    public String getProtocol() {
        return OIDCLoginProtocol.LOGIN_PROTOCOL;
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {
        // 无操作
    }

    /** @param session Keycloak 会话 @return 新的映射器实例 */
    @Override
    public final ProtocolMapper create(KeycloakSession session) {
        return new NonceBackwardsCompatibleMapper();
    }

    /** 初始化（无操作） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    /** 工厂初始化后回调 @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** @return 映射器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Nonce backwards compatible";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return AbstractOIDCProtocolMapper.TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Adds the nonce claim to Access, Refresh and ID token";
    }

    /** @return 配置属性列表（空） */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create().build();
    }

    /** 将授权请求中的 nonce 写入访问令牌 @return 转换后的访问令牌 */
    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        token.setNonce(clientSessionCtx.getAttribute(OIDCLoginProtocol.NONCE_PARAM, String.class));
        return token;
    }

    /** 创建 nonce 向后兼容映射器 @param name 映射器名称 @return 协议映射器模型 */
    public static ProtocolMapperModel create(String name) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        mapper.setConfig(config);
        return mapper;
    }
}
