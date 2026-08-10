/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authorization.policy.provider.role;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.authorization.policy.provider.util.PolicyValidationException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.RolePolicyRepresentation;
import org.keycloak.representations.idm.authorization.RolePolicyRepresentation.RoleDefinition;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;

/**
 * 角色（role）策略类型的 {@link PolicyProviderFactory}，管理 realm/client 角色绑定及导入导出。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class RolePolicyProviderFactory implements PolicyProviderFactory<RolePolicyRepresentation> {

    public static final String ID = "role";
    private RolePolicyProvider provider = new RolePolicyProvider(this::toRepresentation);

    /** 管理控制台显示名称。 */
    @Override
    public String getName() {
        return "Role";
    }

    /** 策略分组：基于身份。 */
    @Override
    public String getGroup() {
        return "Identity Based";
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    /** 从策略配置 JSON 反序列化角色列表。 */
    @Override
    public RolePolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        RolePolicyRepresentation representation = new RolePolicyRepresentation();
        String roles = policy.getConfig().get("roles");

        representation.setRoles(getRoles(roles, authorization.getRealm()));

        String fetchRoles = policy.getConfig().get("fetchRoles");

        if (StringUtil.isNotBlank(fetchRoles)) {
            representation.setFetchRoles(Boolean.parseBoolean(fetchRoles));
        }

        return representation;
    }

    @Override
    public Class<RolePolicyRepresentation> getRepresentationType() {
        return RolePolicyRepresentation.class;
    }

    @Override
    public void onCreate(Policy policy, RolePolicyRepresentation representation, AuthorizationProvider authorization) {
        updateRoles(policy, representation, authorization);
    }

    @Override
    public void onUpdate(Policy policy, RolePolicyRepresentation representation, AuthorizationProvider authorization) {
        updateRoles(policy, representation, authorization);
    }

    @Override
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        updateRoles(policy, authorization, getRoles(representation.getConfig().get("roles"), authorization.getRealm()));
        String fetchRoles = representation.getConfig().get("fetchRoles");

        if (StringUtil.isNotBlank(fetchRoles)) {
            policy.putConfig("fetchRoles", fetchRoles);
        }
    }

    /** 导出时将角色 ID 转为可读名称（realm 角色名或 clientId/roleName）。 */
    @Override
    public void onExport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorizationProvider) {
        Map<String, String> config = new HashMap<>();
        Set<RolePolicyRepresentation.RoleDefinition> roles = toRepresentation(policy, authorizationProvider).getRoles();

        for (RolePolicyRepresentation.RoleDefinition roleDefinition : roles) {
            RoleModel role = authorizationProvider.getRealm().getRoleById(roleDefinition.getId());

            if (role.isClientRole()) {
                roleDefinition.setId(ClientModel.class.cast(role.getContainer()).getClientId() + "/" + role.getName());
            } else {
                roleDefinition.setId(role.getName());
            }
        }

        try {
            config.put("roles", JsonSerialization.writeValueAsString(roles));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to export role policy [" + policy.getName() + "]", cause);
        }

        String fetchRoles = policy.getConfig().get("fetchRoles");

        if (StringUtil.isNotBlank(fetchRoles)) {
            config.put("fetchRoles", fetchRoles);
        }

        representation.setConfig(config);
    }

    private void updateRoles(Policy policy, RolePolicyRepresentation representation, AuthorizationProvider authorization) {
        if (representation.isFetchRoles() != null) {
            policy.putConfig("fetchRoles", String.valueOf(representation.isFetchRoles()));
        }
        updateRoles(policy, authorization, representation.getRoles());
    }

    /** 解析并校验角色定义，禁止重复角色，序列化后写入策略配置。 */
    private void updateRoles(Policy policy, AuthorizationProvider authorization, Set<RolePolicyRepresentation.RoleDefinition> roles) {
        Set<RolePolicyRepresentation.RoleDefinition> updatedRoles = new HashSet<>();
        Set<String> processedRoles = new HashSet<>();
        if (roles != null) {
            RealmModel realm = authorization.getRealm();
            for (RolePolicyRepresentation.RoleDefinition definition : roles) {
                RoleModel role = getRole(definition, realm);
                if (role == null) {
                    continue;
                }

                if (!processedRoles.add(role.getId())) {
                    throw new PolicyValidationException("Role can't be specified multiple times - " + role.getName());
                }
                definition.setId(role.getId());
                updatedRoles.add(definition);
            }
        }

        try {
            policy.putConfig("roles", JsonSerialization.writeValueAsString(updatedRoles));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to serialize roles", cause);
        }
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    /** 从 JSON 配置解析角色定义，过滤 realm 中不存在的角色。 */
    private Set<RoleDefinition> getRoles(String rawRoles, RealmModel realm) {
        if (rawRoles != null) {
            try {
                return Arrays.stream(JsonSerialization.readValue(rawRoles, RoleDefinition[].class))
                        .filter(definition -> getRole(definition, realm) != null)
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            } catch (IOException e) {
                throw new RuntimeException("Could not parse roles from config: [" + rawRoles + "]", e);
            }
        }

        return Collections.emptySet();
    }

    /** UUID 格式正则，用于区分角色 ID 与角色名称。 */
    public static final Pattern UUID_PATTERN = Pattern.compile("[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}");

    /**
     * 按角色名、UUID 或 {@code clientId/roleName} 格式解析 {@link RoleModel}。
     */
    private RoleModel getRole(RolePolicyRepresentation.RoleDefinition definition, RealmModel realm) {
        String roleName = definition.getId();
        String clientId = null;
        int clientIdSeparator = roleName.indexOf("/");

        if (clientIdSeparator != -1) {
            clientId = roleName.substring(0, clientIdSeparator);
            roleName = roleName.substring(clientIdSeparator + 1);
        }

        RoleModel role;

        if (clientId == null) {
            // 若角色名形如 UUID，优先按 ID 查找以避免重复数据库访问
            // TODO: 未来版本可更严格区分 ID 与名称以避免双重查找
            boolean looksLikeAUuid = UUID_PATTERN.matcher(roleName).matches();
            role = looksLikeAUuid ? realm.getRoleById(roleName) : realm.getRole(roleName);

            if (role == null) {
                role = !looksLikeAUuid ? realm.getRoleById(roleName) : realm.getRole(roleName);;
            }
        } else {
            ClientModel client = realm.getClientByClientId(clientId);

            if (client == null) {
                throw new RuntimeException("Client with id [" + clientId + "] not found.");
            }

            role = client.getRole(roleName);
        }

        return role;
    }
}
