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
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.utils.RoleResolveUtil;

/**
 * 角色名称映射器：将已分配角色在令牌中重命名或移动到不同位置（领域/客户端角色树）。
 * <p>先移除原角色名，再按新名称写入目标 access 结构。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RoleNameMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /** 配置键：待重命名的源角色（clientname.role 或领域角色名） */
    public static final String ROLE_CONFIG = "role";
    /** 配置键：目标新角色名及位置 */
    public static String NEW_ROLE_NAME = "new.role.name";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ROLE_CONFIG);
        property.setLabel("Role");
        property.setHelpText("Role name you want changed.  Click 'Select Role' button to browse roles, or just type it in the textbox.  To reference a client role the syntax is clientname.clientrole, i.e. myclient.myrole");
        property.setType(ProviderConfigProperty.ROLE_TYPE);
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(NEW_ROLE_NAME);
        property.setLabel("New Role Name");
        property.setHelpText("The new role name.  The new name format corresponds to where in the access token the role will be mapped to.  So, a new name of 'myapp.newname' will map the role to that position in the access token.  A new name of 'newname' will map the role to the realm roles in the token.");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "oidc-role-name-mapper";


    /** {@inheritDoc} 返回源角色与新角色名配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：Role Name Mapper */
    @Override
    public String getDisplayType() {
        return "Role Name Mapper";
    }

    /** {@inheritDoc} 归类为令牌映射器 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** {@inheritDoc} 将已分配角色映射到新名称或令牌位置 */
    @Override
    public String getHelpText() {
        return "Map an assigned role to a new name or position in the token.";
    }

    /** {@inheritDoc} 角色名称映射器优先级 */
    @Override
    public int getPriority() {
        return ProtocolMapperUtils.PRIORITY_ROLE_NAMES_MAPPER;
    }

    @Override
    public AccessToken transformUserInfoToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                              UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // 映射器始终执行；最终是否写入令牌由其他角色映射器决定
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // 映射器始终执行；最终是否写入令牌由其他角色映射器决定
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    @Override
    public AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // 映射器始终执行；最终是否写入令牌由其他角色映射器决定
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    /** 若用户持有源角色，则移除并在目标位置以新名称添加 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession session,
                            ClientSessionContext clientSessionCtx) {
        String role = mappingModel.getConfig().get(ROLE_CONFIG);
        String newName = mappingModel.getConfig().get(NEW_ROLE_NAME);

        String[] scopedRole = KeycloakModelUtils.parseRole(role);
        String[] newScopedRole = KeycloakModelUtils.parseRole(newName);
        String appName = scopedRole[0];
        String roleName = scopedRole[1];
        if (appName != null) {
            AccessToken.Access access = RoleResolveUtil.getResolvedClientRoles(session, clientSessionCtx, appName, false);
            if (access == null) return;
            if (!access.getRoles().contains(roleName)) return;
            access.getRoles().remove(roleName);
        } else {
            AccessToken.Access access = RoleResolveUtil.getResolvedRealmRoles(session, clientSessionCtx, false);
            if (access == null || !access.getRoles().contains(roleName)) return;
            access.getRoles().remove(roleName);
        }

        String newAppName = newScopedRole[0];
        String newRoleName = newScopedRole[1];
        AccessToken.Access access = null;
        if (newAppName == null) {
            access = RoleResolveUtil.getResolvedRealmRoles(session, clientSessionCtx, true);
        } else {
            access = RoleResolveUtil.getResolvedClientRoles(session, clientSessionCtx, newAppName, true);
        }

        access.addRole(newRoleName);
    }

    /**
     * 工厂方法：创建角色名称映射器配置。
     * @param role 源角色
     * @param newName 新角色名及位置
     */
        String mapperId = PROVIDER_ID;
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(ROLE_CONFIG, role);
        config.put(NEW_ROLE_NAME, newName);
        mapper.setConfig(config);
        return mapper;

    }

}
