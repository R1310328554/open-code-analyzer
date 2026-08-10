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

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.resources.admin.fgap.ModelRecord.ClientModelRecord;

import static org.keycloak.authorization.fgap.AdminPermissionsSchema.CLIENTS_RESOURCE_TYPE;

/**
 * 客户端细粒度管理权限 V2 实现。
 * <p>基于 {@link AdminPermissionsSchema} 与 {@link FineGrainedAdminPermissionEvaluator} 评估类型级与实例级权限；不支持 V1  per-client 策略 CRUD。</p>
 */
class ClientPermissionsV2 extends ClientPermissions {

    /** FGAP v2 权限评估器 */
    private final FineGrainedAdminPermissionEvaluator eval;

    /** 构造 V2 客户端权限实现 */
    ClientPermissionsV2(KeycloakSession session, RealmModel realm, AuthorizationProvider authz, MgmtPermissionsV2 root) {
        super(session, realm, authz, root);
        this.eval = new FineGrainedAdminPermissionEvaluator(session, root, resourceStore, policyStore);
    }

    @Override
    public boolean canList() {
        return root.hasOneAdminRole(AdminRoles.QUERY_CLIENTS) || canView();
    }

    @Override
    public void requireConfigure(ClientModel client) {
        // V2 将 configure 重定向为 manage
        super.requireManage(client);
    }

    @Override
    public boolean canConfigure(ClientModel client) {
        //redirecting call to manage for V2
        return canManage(client);
    }

    @Override
    public boolean canManage(ClientModel client) {
        if (isInternal(client)) {
            return false;
        }
        if (root.hasOneAdminRole(AdminRoles.MANAGE_CLIENTS)) {
            return true;
        }

        return eval.hasPermission(new ClientModelRecord(client), null, AdminPermissionsSchema.MANAGE);
    }

    @Override
    public boolean canManage() {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_CLIENTS)) {
            return true;
        }

        return eval.hasPermission(new ClientModelRecord(null), null, AdminPermissionsSchema.MANAGE);
    }

    @Override
    public boolean canView(ClientModel client) {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_CLIENTS, AdminRoles.VIEW_CLIENTS)) {
            return true;
        }

        return eval.hasPermission(new ClientModelRecord(client), null, AdminPermissionsSchema.VIEW);
    }

    @Override
    public boolean canView() {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_CLIENTS, AdminRoles.VIEW_CLIENTS)) {
            return true;
        }

        return eval.hasPermission(new ClientModelRecord(null), null, AdminPermissionsSchema.VIEW);
    }

    @Override
    public boolean canMapRoles(ClientModel client) {
        return eval.hasPermission(new ClientModelRecord(client), null, AdminPermissionsSchema.MAP_ROLES);
    }

    @Override
    public boolean canMapCompositeRoles(ClientModel client) {
        return eval.hasPermission(new ClientModelRecord(client), null, AdminPermissionsSchema.MAP_ROLES_COMPOSITE);
    }

    @Override
    public boolean canMapClientScopeRoles(ClientModel client) {
        return eval.hasPermission(new ClientModelRecord(client), null, AdminPermissionsSchema.MAP_ROLES_CLIENT_SCOPE);
    }

    @Override
    public boolean canManageClientScopes() {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_CLIENTS)) {
            return true;
        }

        return eval.hasPermission(new ClientModelRecord(null), null, AdminPermissionsSchema.MANAGE);
    }

    @Override
    public boolean canManage(ClientScopeModel clientScope) {
        return canManageClientScopes();
    }

    @Override
    public boolean canView(ClientScopeModel clientScope) {
        if (root.hasOneAdminRole(AdminRoles.VIEW_CLIENTS, AdminRoles.MANAGE_CLIENTS)) {
            return true;
        }

        return eval.hasPermission(new ClientModelRecord(null), null, AdminPermissionsSchema.VIEW);
    }

    @Override
    public Set<String> getClientIdsByScope(String scope) {
        return eval.getIdsByScope(CLIENTS_RESOURCE_TYPE, scope);
    }

    @Override
    public boolean canExchangeTo(ClientModel authorizedClient, ClientModel to, AccessToken token) {
        // V2 不支持配置 token-exchange 权限，静默拒绝而非抛未捕获异常
        return false;
    }

    @Override
    public Policy exchangeToPermission(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy mapRolesPermission(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy mapRolesClientScopePermission(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy mapRolesCompositePermission(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy managePermission(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy configurePermission(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Policy viewPermission(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public boolean isPermissionsEnabled(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public void setPermissionsEnabled(ClientModel client, boolean enable) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Resource resource(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }

    @Override
    public Map<String, String> getPermissions(ClientModel client) {
        throw new UnsupportedOperationException("Not supported in V2");
    }
}
