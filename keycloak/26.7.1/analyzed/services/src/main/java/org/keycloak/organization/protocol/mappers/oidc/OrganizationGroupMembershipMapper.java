/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.protocol.mappers.oidc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.TokenIntrospectionTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME;

/**
 * OIDC 组织组成员关系协议映射器：将用户在组织内的组路径（及可选角色）写入 organization 声明。
 * <p>依赖 {@link OrganizationMembershipMapper} 提供的声明名；在 {@link OrganizationMembershipMapper#PROVIDER_ID} 映射器存在且请求含 organization scope 时生效。</p>
 */
public class OrganizationGroupMembershipMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper, EnvironmentDependentProviderFactory {

    /** 映射器提供方 ID。 */
    public static final String PROVIDER_ID = "oidc-organization-group-membership-mapper";
    /** 配置键：是否同时映射组织组上的角色。 */
    public static final String ADD_GROUP_ROLE_MAPPINGS = "addGroupRoleMappings";

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> properties = new ArrayList<>();
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(properties, OrganizationGroupMembershipMapper.class);
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(ADD_GROUP_ROLE_MAPPINGS);
        property.setLabel(ADD_GROUP_ROLE_MAPPINGS + ".label");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue(Boolean.FALSE.toString());
        property.setHelpText(ADD_GROUP_ROLE_MAPPINGS + ".help");
        properties.add(property);
        return properties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Organization Group Membership";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Map user Organization group membership";
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel model, UserSessionModel userSession, KeycloakSession session, ClientSessionContext clientSessionCtx) {
        if (!Organizations.isEnabled(session)) {
            return;
        }
        // 从 client session note 或请求的 scope 解析组织 ID
        String orgId = clientSessionCtx.getClientSession().getNote(OrganizationModel.ORGANIZATION_ATTRIBUTE);
        Stream<OrganizationModel> organizations;

        if (orgId == null) {
            organizations = resolveFromRequestedScopes(session, userSession, clientSessionCtx);
        } else {
            OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
            OrganizationModel org = orgProvider.getById(orgId);
            organizations = org != null ? Stream.of(org) : Stream.empty();
        }

        UserModel user = userSession.getUser();
        OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
        ProtocolMapperModel organizationMapperModel = getOrganizationMapperModel(clientSessionCtx);

        if (organizationMapperModel == null) {
            // 本映射器要求请求包含 organization scope 及对应 membership mapper
            return;
        }

        String orgClaimName = organizationMapperModel.getConfig().get(TOKEN_CLAIM_NAME);

        model = getEffectiveModel(session, userSession.getRealm(), model);
        model.getConfig().put(TOKEN_CLAIM_NAME, orgClaimName);

        Map<String, Object> orgClaims = OIDCAttributeMapperHelper.getOrInitializeOrganizationClaimAsMap(token, model);

        boolean includeRoles = isAddGroupRoleMappings(model);

        // 为每个组织写入 groups 列表
        organizations.forEach(org -> {
            if (org == null || !org.isEnabled() || !org.isMember(user)) {
                return;
            }

            List<GroupModel> userOrgGroups = orgProvider.getOrganizationGroupsByMember(org, user)
                .collect(Collectors.toList());

            List<String> groupPaths = userOrgGroups.stream()
                .map(ModelToRepresentation::buildGroupPath)
                .collect(Collectors.toList());

            String orgAlias = org.getAlias();

            // 获取或创建该组织在声明中的数据 Map
            Map<String, Object> orgData = (Map<String, Object>) orgClaims.get(orgAlias);
            if (orgData == null) {
                orgData = new HashMap<>();
                orgClaims.put(orgAlias, orgData);
            }

            // 写入组路径
            orgData.put("groups", groupPaths);

            // 配置启用时追加组织组角色映射
            if (includeRoles) {
                Set<RoleModel> roleMappings = userOrgGroups.stream()
                    .flatMap(GroupModel::getRoleMappingsStream)
                    .collect(Collectors.toSet());
                roleMappings = RoleUtils.expandCompositeRoles(roleMappings);

                List<String> realmRoles = new ArrayList<>();
                Map<String, List<String>> clientRoles = new HashMap<>();

                for (RoleModel role : roleMappings) {
                    if (role.getContainer() instanceof RealmModel) {
                        realmRoles.add(role.getName());
                    } else if (role.getContainer() instanceof ClientModel clientModel) {
                        clientRoles.computeIfAbsent(clientModel.getClientId(), k -> new ArrayList<>())
                            .add(role.getName());
                    }
                }

                if (!realmRoles.isEmpty()) {
                    orgData.put("realm_access", Map.of("roles", realmRoles));
                }
                if (!clientRoles.isEmpty()) {
                    Map<String, Object> resourceAccess = new HashMap<>();
                    clientRoles.forEach((clientId, roles) -> resourceAccess.put(clientId, Map.of("roles", roles)));
                    orgData.put("resource_access", resourceAccess);
                }
            }
        });
    }

    private Stream<OrganizationModel> resolveFromRequestedScopes(KeycloakSession session, UserSessionModel userSession, ClientSessionContext context) {
        String rawScopes = context.getScopeString(true);
        OrganizationScope scope = OrganizationScope.valueOfScope(session, rawScopes);

        if (scope == null) {
            return Stream.empty();
        }

        return scope.resolveOrganizations(userSession.getUser(), rawScopes, session);
    }

    private boolean isAddGroupRoleMappings(ProtocolMapperModel model) {
        return Boolean.parseBoolean(model.getConfig().getOrDefault(ADD_GROUP_ROLE_MAPPINGS, Boolean.FALSE.toString()));
    }

    public static ProtocolMapperModel create(String name, boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);

        return mapper;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION);
    }

    @Override
    public int getPriority() {
        // 在 OrganizationMembershipMapper 之后执行（数值越大越靠后）
        return 10;
    }

    private ProtocolMapperModel getOrganizationMapperModel(ClientSessionContext clientSessionContext) {
        return clientSessionContext.getProtocolMappersStream()
                .filter(m -> OrganizationMembershipMapper.PROVIDER_ID.equals(m.getProtocolMapper()))
                .findAny()
                .orElse(null);
    }
}
