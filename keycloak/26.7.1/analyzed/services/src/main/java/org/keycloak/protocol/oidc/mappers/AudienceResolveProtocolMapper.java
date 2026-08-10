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
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.utils.RoleResolveUtil;

import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN;
import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION;

/**
 * 受众解析协议映射器。
 * <p>将用户拥有至少一个客户端角色的所有「允许」客户端的 {@code client_id} 添加到令牌受众字段。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AudienceResolveProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, TokenIntrospectionTokenMapper {

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AudienceResolveProtocolMapper.class);
    }

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-audience-resolve-mapper";


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
        return "Audience Resolve";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Adds all client_ids of \"allowed\" clients to the audience field of the token. Allowed client means the client\n" +
                " for which user has at least one client role";
    }

    /** @return 映射器执行优先级 */
    @Override
    public int getPriority() {
        return ProtocolMapperUtils.PRIORITY_AUDIENCE_RESOLVE_MAPPER;
    }

    /** 解析并写入访问令牌受众 @return 转换后的访问令牌 */
    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        boolean shouldUseLightweightToken = getShouldUseLightweightToken(session);
        boolean includeInAccessToken = shouldUseLightweightToken ?  OIDCAttributeMapperHelper.includeInLightweightAccessToken(mappingModel) : includeInAccessToken(mappingModel);
        if (!includeInAccessToken){
            return token;
        }
        setAudience(token, clientSessionCtx, session);
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

    /** 解析并写入自省令牌受众 @return 转换后的令牌 */
    @Override
    public AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                                   UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        if (!includeInIntrospection(mappingModel)) {
            return token;
        }
        setAudience(token, clientSessionCtx, session);
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

    /** 计算有效映射配置 @return 克隆后的有效模型 */
    @Override
    public ProtocolMapperModel getEffectiveModel(KeycloakSession session, RealmModel realm, ProtocolMapperModel protocolMapperModel) {
        // Effectively clone
        ProtocolMapperModel copy = RepresentationToModel.toModel(ModelToRepresentation.toRepresentation(protocolMapperModel));

        copy.getConfig().put(INCLUDE_IN_ACCESS_TOKEN, String.valueOf(includeInAccessToken(copy)));
        copy.getConfig().put(INCLUDE_IN_INTROSPECTION, String.valueOf(includeInIntrospection(copy)));

        return copy;
    }

    /** 根据已解析客户端角色向令牌添加受众 @param token 访问令牌 @param clientSessionCtx 客户端会话上下文 @param session Keycloak 会话 */
    private void setAudience(AccessToken token, ClientSessionContext clientSessionCtx, KeycloakSession session) {
        String clientId = clientSessionCtx.getClientSession().getClient().getClientId();

        for (Map.Entry<String, AccessToken.Access> entry : RoleResolveUtil.getAllResolvedClientRoles(session, clientSessionCtx).entrySet()) {
            // 不将当前客户端自身加入受众
            if (entry.getKey().equals(clientId)) {
                continue;
            }

            AccessToken.Access access = entry.getValue();
            if (access != null && access.getRoles() != null && !access.getRoles().isEmpty()) {
                token.addAudience(entry.getKey());
            }
        }
    }

    /** 创建受众解析映射器 @param name 名称 @param accessToken 是否写入访问令牌 @param introspectionEndpoint 是否写入自省端点 @return 协议映射器模型 */
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
