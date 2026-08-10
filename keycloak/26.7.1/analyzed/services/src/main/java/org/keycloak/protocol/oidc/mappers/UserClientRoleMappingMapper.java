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
import java.util.Map;
import java.util.Objects;

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
 * 用户客户端角色映射器：将用户在指定客户端（或全部客户端）的角色映射为令牌声明。
 * <p>支持角色前缀与多值数组输出。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class UserClientRoleMappingMapper extends AbstractUserRoleMappingMapper {

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "oidc-usermodel-client-role-mapper";

    private static final String TOKEN_CLAIM_NAME_TOOLTIP = "usermodel.clientRoleMapping.tokenClaimName.tooltip";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {

        ProviderConfigProperty clientId = new ProviderConfigProperty();
        clientId.setName(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID);
        clientId.setLabel(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID_LABEL);
        clientId.setHelpText(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID_HELP_TEXT);
        clientId.setType(ProviderConfigProperty.CLIENT_LIST_TYPE);
        CONFIG_PROPERTIES.add(clientId);

        ProviderConfigProperty clientRolePrefix = new ProviderConfigProperty();
        clientRolePrefix.setName(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX);
        clientRolePrefix.setLabel(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX_LABEL);
        clientRolePrefix.setHelpText(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX_HELP_TEXT);
        clientRolePrefix.setType(ProviderConfigProperty.STRING_TYPE);
        CONFIG_PROPERTIES.add(clientRolePrefix);

        ProviderConfigProperty multiValued = new ProviderConfigProperty();
        multiValued.setName(ProtocolMapperUtils.MULTIVALUED);
        multiValued.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        multiValued.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        multiValued.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        multiValued.setDefaultValue("true");
        CONFIG_PROPERTIES.add(multiValued);

        OIDCAttributeMapperHelper.addAttributeConfig(CONFIG_PROPERTIES, UserClientRoleMappingMapper.class);

        // 为「令牌声明名」使用客户端角色专用提示文本
        for (ProviderConfigProperty prop : CONFIG_PROPERTIES) {
            if (OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME.equals(prop.getName())) {
                prop.setHelpText(TOKEN_CLAIM_NAME_TOOLTIP);
            }
        }
    }

    /** {@inheritDoc} 含客户端 ID、角色前缀与多值等配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：User Client Role */
    @Override
    public String getDisplayType() {
        return "User Client Role";
    }

    /** {@inheritDoc} 归类为令牌映射器 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** {@inheritDoc} 将用户客户端角色映射到令牌声明 */
    @Override
    public String getHelpText() {
        return "Map a user client role to a token claim.";
    }

    /** 解析客户端角色并写入声明；未指定 clientId 时遍历全部客户端 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession session, ClientSessionContext clientSessionCtx) {
        String clientId = mappingModel.getConfig().get(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID);
        String rolePrefix = mappingModel.getConfig().get(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX);

        if (clientId != null && !clientId.isEmpty()) {
            AccessToken.Access access = RoleResolveUtil.getResolvedClientRoles(session, clientSessionCtx, clientId, false);
            if (access == null) {
                return;
            }

            AbstractUserRoleMappingMapper.setClaim(token, mappingModel, access.getRoles(), clientId, rolePrefix);
        } else {
            // 未指定 clientId 时处理所有客户端的角色
            Map<String, AccessToken.Access> allAccess = RoleResolveUtil.getAllResolvedClientRoles(session, clientSessionCtx);

            for (Map.Entry<String, AccessToken.Access> entry : allAccess.entrySet()) {
                String currClientId = entry.getKey();
                AccessToken.Access access = entry.getValue();
                if (access == null) {
                    continue;
                }
                String augmentedRolePrefix = Objects.nonNull(rolePrefix)
                        && !rolePrefix.trim().isEmpty()
                        ? rolePrefix.replaceAll(CLIENT_ID_PATTERN.toString(), currClientId)
                        : rolePrefix;

                AbstractUserRoleMappingMapper.setClaim(token, mappingModel, access.getRoles(), currClientId, augmentedRolePrefix);
            }
        }
    }


    /** 工厂方法：创建客户端角色映射器（默认多值） */
    public static ProtocolMapperModel create(String clientId, String clientRolePrefix,
                                             String name,
                                             String tokenClaimName,
                                             boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        return create(clientId, clientRolePrefix, name, tokenClaimName, accessToken, idToken, introspectionEndpoint, false);

    }

    /**
     * 工厂方法：创建客户端角色映射器。
     * @param clientId 目标客户端 ID（空表示全部）
     * @param clientRolePrefix 角色名前缀（可含 ${clientId} 占位）
     * @param multiValued 是否以数组写入多角色
     */
        ProtocolMapperModel mapper = OIDCAttributeMapperHelper.createClaimMapper(name, "foo",
                tokenClaimName, "String",
                accessToken, idToken, false, introspectionEndpoint,
                PROVIDER_ID);

        mapper.getConfig().put(ProtocolMapperUtils.MULTIVALUED, String.valueOf(multiValued));
        mapper.getConfig().put(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID, clientId);
        mapper.getConfig().put(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX, clientRolePrefix);
        return mapper;
    }

}
