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
import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.WebOriginsUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN;
import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION;

/**
 * 允许 Web 来源协议映射器。
 * <p>将客户端配置的允许 Web 来源写入访问令牌的 {@code allowed-origins} 声明。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AllowedWebOriginsProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();


    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-allowed-origins-mapper";

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AllowedWebOriginsProtocolMapper.class);
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
        return "Allowed Web Origins";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Adds all allowed web origins to the 'allowed-origins' claim in the token";
    }

    /** 转换访问令牌，按需写入允许 Web 来源 @return 转换后的访问令牌 */
    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        boolean shouldUseLightweightToken = getShouldUseLightweightToken(session);
        boolean includeInAccessToken = shouldUseLightweightToken ?  OIDCAttributeMapperHelper.includeInLightweightAccessToken(mappingModel) : includeInAccessToken(mappingModel);
        if (!includeInAccessToken){
            return token;
        }
        setWebOrigin(token, session, clientSessionCtx);
        return token;
    }

    /** 判断是否应写入访问令牌 @param mappingModel 映射配置 @return 是否包含 */
    private boolean includeInAccessToken(ProtocolMapperModel mappingModel) {
        String includeInAccessToken = mappingModel.getConfig().get(INCLUDE_IN_ACCESS_TOKEN);

        // 向后兼容：未配置时默认包含
        if (includeInAccessToken == null) {
            return true;
        }

        return "true".equals(includeInAccessToken);
    }

    /** 转换自省令牌响应 @return 转换后的令牌 */
    @Override
    public AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                                   UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        if (!includeInIntrospection(mappingModel)) {
            return token;
        }
        setWebOrigin(token, session, clientSessionCtx);
        return token;
    }

    /** 判断是否应写入自省响应 @param mappingModel 映射配置 @return 是否包含 */
    private boolean includeInIntrospection(ProtocolMapperModel mappingModel) {
        String includeInIntrospection = mappingModel.getConfig().get(INCLUDE_IN_INTROSPECTION);

        // Backwards compatibility
        if (includeInIntrospection == null) {
            return true;
        }

        return "true".equals(includeInIntrospection);
    }

    /** 计算有效映射配置（含向后兼容默认值） @return 克隆后的有效模型 */
    @Override
    public ProtocolMapperModel getEffectiveModel(KeycloakSession session, RealmModel realm, ProtocolMapperModel protocolMapperModel) {
        // 深拷贝映射模型
        ProtocolMapperModel copy = RepresentationToModel.toModel(ModelToRepresentation.toRepresentation(protocolMapperModel));

        copy.getConfig().put(INCLUDE_IN_ACCESS_TOKEN, String.valueOf(includeInAccessToken(copy)));
        copy.getConfig().put(INCLUDE_IN_INTROSPECTION, String.valueOf(includeInIntrospection(copy)));

        return copy;
    }

    /** 将客户端允许 Web 来源写入令牌 @param token 访问令牌 @param session Keycloak 会话 @param clientSessionCtx 客户端会话上下文 */
    private void setWebOrigin(AccessToken token, KeycloakSession session, ClientSessionContext clientSessionCtx) {
        ClientModel client = clientSessionCtx.getClientSession().getClient();

        Set<String> allowedOrigins = client.getWebOrigins();
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            token.setAllowedOrigins(WebOriginsUtils.resolveValidWebOrigins(session, client));
        }
    }

    /**
     * 创建允许 Web 来源映射器。
     * @param name 映射器名称
     * @param accessToken 是否写入访问令牌
     * @param introspectionEndpoint 是否写入自省端点
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel createClaimMapper(String name, boolean accessToken, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) {
            config.put(INCLUDE_IN_ACCESS_TOKEN, "true");
        } else {
            config.put(INCLUDE_IN_ACCESS_TOKEN, "false");
        }
        if (introspectionEndpoint) {
            config.put(INCLUDE_IN_INTROSPECTION, "true");
        } else {
            config.put(INCLUDE_IN_INTROSPECTION, "false");
        }
        mapper.setConfig(config);
        return mapper;
    }

}
