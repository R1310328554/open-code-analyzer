/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.resources.admin.fgap;

import java.util.Map;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.services.resources.admin.fgap.ModelRecord.RoleModelRecord;

import static org.keycloak.authorization.fgap.AdminPermissionsSchema.ROLES_RESOURCE_TYPE;

/**
 * 角色细粒度管理权限 V2 实现。
 * <p>基于 {@link AdminPermissionsSchema} 与 {@link FineGrainedAdminPermissionEvaluator}，不再使用 V1  per-role 授权资源模型。</p>
 */
class RolePermissionsV2 extends RolePermissions {

    /** FGAP V2 通用权限评估器 */
    private final FineGrainedAdminPermissionEvaluator eval;

    /** 构造 V2 角色权限评估器。 */
    RolePermissionsV2(KeycloakSession session, RealmModel realm, AuthorizationProvider authz, MgmtPermissions root) {
        super(session, realm, authz, root);
        this.eval = new FineGrainedAdminPermissionEvaluator(session, root, resourceStore, policyStore);
    }

    @Override
    public boolean canMapRole(RoleModel role) {
        if (isRealmAdminRole(role)) {
            if (realm.isAdminPermissionsEnabled()) {
                // 启用 FGAP 时仅服务器或领域管理员可映射管理员角色
                return root.isRealmAdmin();
            }
            // 否则需 manage-users 且自身已拥有待映射的管理员角色
            return root.hasOneAdminRole(AdminRoles.MANAGE_USERS) && checkAdminRoles(role);
        }

        if (root.hasOneAdminRole(AdminRoles.MANAGE_USERS)) {
            // 拥有 manage-users 可映射任意非管理员角色
            return true;
        }

        if (role.getContainer() instanceof ClientModel clientModel) {
            if (root.clients().canMapRoles(clientModel)) {
                return true;
            }
        }

        return eval.hasPermission(new RoleModelRecord(role), null, AdminPermissionsSchema.MAP_ROLE);
    }

    @Override
    public boolean canMapComposite(RoleModel role) {
        if (isRealmAdminRole(role)) {
            if (realm.isAdminPermissionsEnabled()) {
                // only server or realm admins can map roles if FGAP is enabled
                return root.isRealmAdmin();
            }
            // 否则需 manage-realm/manage-clients 且已通过 checkAdminRoles
            return canManageDefault(role) && checkAdminRoles(role);
        }

        if (canManageDefault(role)) {
            // 拥有领域/客户端管理默认权限时可映射非管理员复合角色
            return checkAdminRoles(role);
        }

        if (role.getContainer() instanceof ClientModel clientModel) {
            if (root.hasOneAdminRole(AdminRoles.MANAGE_CLIENTS)) {
                return true;
            }
            if (root.clients().canMapCompositeRoles(clientModel)) {
                return true;
            }
        } else {
            if (root.hasOneAdminRole(AdminRoles.MANAGE_REALM)) {
                return true;
            }
        }

        return eval.hasPermission(new RoleModelRecord(role), null, AdminPermissionsSchema.MAP_ROLE_COMPOSITE);
    }

    @Override
    public boolean canMapClientScope(RoleModel role) {
        if (role.getContainer() instanceof ClientModel clientModel) {
            if (root.hasOneAdminRole(AdminRoles.MANAGE_CLIENTS)) {
                return true;
            }
            if (root.clients().canMapClientScopeRoles(clientModel)) {
                return true;
            }
        } else {
            if (root.hasOneAdminRole(AdminRoles.MANAGE_REALM)) {
                return true;
            }
        }

        return eval.hasPermission(new RoleModelRecord(role), null, AdminPermissionsSchema.MAP_ROLE_CLIENT_SCOPE);
    }

    @Override
    public Set<String> getRoleIdsByScope(String scope) {
        return eval.getIdsByScope(ROLES_RESOURCE_TYPE, scope);
    }

    @Override
    public boolean isPermissionsEnabled(RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public void setPermissionsEnabled(RoleModel role, boolean enable) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Map<String, String> getPermissions(RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy mapRolePermission(RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy mapCompositePermission(RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy mapClientScopePermission(RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Resource resource(RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public ResourceServer resourceServer(RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy manageUsersPolicy(ResourceServer server) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy viewUsersPolicy(ResourceServer server) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy rolePolicy(ResourceServer server, RoleModel role) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    /** 判断是否为 master/realm-management 等管理员角色。 */
    private boolean isRealmAdminRole(RoleModel role) {
        RoleContainerModel container = role.getContainer();
        boolean isMasterRealmRole = container.equals(root.getMasterRealm());
        boolean isMasterRealmManagementAdminRole = (container instanceof ClientModel c)
                && c.getRealm().getName().equals(Config.getAdminRealm())
                && c.getClientId().endsWith("-realm");
        boolean isRealmManagementAdminRole = container.equals(root.getRealmManagementClient());

        if (isMasterRealmRole|| isRealmManagementAdminRole || isMasterRealmManagementAdminRole) {
            return AdminRoles.ALL_ROLES.contains(role.getName());
        }

        return false;
    }
}
