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
package org.keycloak.services.resources.admin.fgap;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.admin.AdminAuth;

/**
 * 细粒度管理权限 V2 入口。
 * <p>使用 {@link AdminPermissionsSchema} 与 admin-permissions 客户端，并返回 V2 版本的 users/groups/clients/roles/realm 权限评估器。</p>
 */
class MgmtPermissionsV2 extends MgmtPermissions {

    /** V2 客户端权限评估器 */
    private ClientPermissionsV2 clientPermissions;

    /** V2 组权限评估器 */
    private GroupPermissionsV2 groupPermissions;

    /** V2 角色权限评估器 */
    private RolePermissionsV2 rolePermissions;

    /** V2 用户权限评估器 */
    private UserPermissionsV2 userPermissions;

    /** V2 领域权限评估器 */
    private RealmPermissionsV2 realmPermissions;

    /** 构造 V2 权限管理器（无认证上下文）。 */
    public MgmtPermissionsV2(KeycloakSession session, RealmModel realm) {
        super(session, realm);
    }

    /** 构造 V2 权限管理器（带 AdminAuth）。 */
    public MgmtPermissionsV2(KeycloakSession session, RealmModel realm, AdminAuth auth) {
        super(session, realm, auth);
    }

    /** 构造 V2 权限管理器（仅 AdminAuth，领域由 token 解析）。 */
    public MgmtPermissionsV2(KeycloakSession session, AdminAuth auth) {
        super(session, auth);
    }

    /** 构造 V2 权限管理器（指定管理员用户）。 */
    public MgmtPermissionsV2(KeycloakSession session, RealmModel adminsRealm, UserModel admin) {
        super(session, adminsRealm, admin);
    }

    /** 构造 V2 权限管理器（指定目标领域与管理员）。 */
    public MgmtPermissionsV2(KeycloakSession session, RealmModel realm, RealmModel adminsRealm, UserModel admin) {
        super(session, realm, adminsRealm, admin);
    }

    @Override
    /** V2 使用领域的 admin-permissions 客户端。 */
    @Override
    public ClientModel getRealmPermissionsClient() {
        return realm.getAdminPermissionsClient();
    }

    @Override
    public RealmPermissions realm() {
        if (realmPermissions != null) return realmPermissions;
        realmPermissions = new RealmPermissionsV2(this);
        return realmPermissions;
    }

    @Override
    public GroupPermissions groups() {
        if (groupPermissions != null) return groupPermissions;
        groupPermissions = new GroupPermissionsV2(session, authz, this);
        return groupPermissions;
    }

    @Override
    public RolePermissions roles() {
        if (rolePermissions != null) return rolePermissions;
        rolePermissions = new RolePermissionsV2(session, realm, authz, this);
        return rolePermissions;
    }

    @Override
    public UserPermissions users() {
        if (userPermissions != null) return userPermissions;
        userPermissions = new UserPermissionsV2(session, authz, this);
        return userPermissions;
    }

    @Override
    public ClientPermissions clients() {
        if (clientPermissions != null) return clientPermissions;
        clientPermissions = new ClientPermissionsV2(session, realm, authz, this);
        return clientPermissions;
    }
}
