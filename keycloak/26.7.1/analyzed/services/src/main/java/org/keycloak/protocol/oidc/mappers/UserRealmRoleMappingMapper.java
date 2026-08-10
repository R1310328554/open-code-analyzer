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

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.utils.RoleResolveUtil;

/**
 * 用户领域角色映射器。
 * <p>将用户已解析的领域角色映射写入 ID Token 与访问令牌声明。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class UserRealmRoleMappingMapper extends AbstractUserRoleMappingMapper {

    /** 提供方标识 */
    public static final String PROVIDER_ID = "oidc-usermodel-realm-role-mapper";

    /** 映射器配置属性列表 */
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {

        ProviderConfigProperty realmRolePrefix = new ProviderConfigProperty();
        realmRolePrefix.setName(ProtocolMapperUtils.USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX);
        realmRolePrefix.setLabel(ProtocolMapperUtils.USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX_LABEL);
        realmRolePrefix.setHelpText(ProtocolMapperUtils.USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX_HELP_TEXT);
        realmRolePrefix.setType(ProviderConfigProperty.STRING_TYPE);
        CONFIG_PROPERTIES.add(realmRolePrefix);

        ProviderConfigProperty multiValued = new ProviderConfigProperty();
        multiValued.setName(ProtocolMapperUtils.MULTIVALUED);
        multiValued.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        multiValued.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        multiValued.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        multiValued.setDefaultValue("true");
        CONFIG_PROPERTIES.add(multiValued);

        OIDCAttributeMapperHelper.addAttributeConfig(CONFIG_PROPERTIES, UserRealmRoleMappingMapper.class);
    }

    /** @return 配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    /** @return 映射器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "User Realm Role";
    }

    /** @return 映射器分类 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** @return 映射器说明文本 */
    @Override
    public String getHelpText() {
        return "Map a user realm role to a token claim.";
    }

    /**
     * 将已解析的领域角色写入令牌声明。
     * @param token 目标令牌
     * @param mappingModel 映射器配置
     * @param userSession 用户会话
     * @param session Keycloak 会话
     * @param clientSessionCtx 客户端会话上下文
     */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession session, ClientSessionContext clientSessionCtx) {
        String rolePrefix = mappingModel.getConfig().get(ProtocolMapperUtils.USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX);

        AccessToken.Access access = RoleResolveUtil.getResolvedRealmRoles(session, clientSessionCtx, false);
        if (access == null) {
            return;
        }

        AbstractUserRoleMappingMapper.setClaim(token, mappingModel, access.getRoles(),null, rolePrefix);
    }

    /** 创建领域角色映射器（默认单值） @param realmRolePrefix 角色前缀 @return 协议映射器模型 */
    public static ProtocolMapperModel create(String realmRolePrefix,
                                             String name,
                                             String tokenClaimName, boolean accessToken, boolean idToken, boolean introspectionEndpoint) {

        return create(realmRolePrefix, name, tokenClaimName, accessToken, idToken, introspectionEndpoint, false);
    }

    /**
     * 创建领域角色映射器。
     * @param realmRolePrefix 角色名前缀
     * @param name 映射器名称
     * @param tokenClaimName 令牌声明名
     * @param accessToken 是否写入访问令牌
     * @param idToken 是否写入 ID Token
     * @param introspectionEndpoint 是否写入自省端点
     * @param multiValued 是否多值声明
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel create(String realmRolePrefix,
                                             String name,
                                             String tokenClaimName, boolean accessToken, boolean idToken, boolean introspectionEndpoint, boolean multiValued) {
        ProtocolMapperModel mapper = OIDCAttributeMapperHelper.createClaimMapper(name, "foo",
                tokenClaimName, "String",
                accessToken, idToken, false, introspectionEndpoint,
                PROVIDER_ID);

        mapper.getConfig().put(ProtocolMapperUtils.MULTIVALUED, String.valueOf(multiValued));
        mapper.getConfig().put(ProtocolMapperUtils.USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX, realmRolePrefix);
        return mapper;
    }
}
