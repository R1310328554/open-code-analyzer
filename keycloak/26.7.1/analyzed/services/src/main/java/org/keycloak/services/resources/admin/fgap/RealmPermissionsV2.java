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

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.ClientModel;

/**
 * 领域权限评估 V2 实现。
 * <p>对 admin-permissions 客户端的 ResourceServer 使用 manage/view-realm 判断；
 * 其他客户端 ResourceServer 额外检查客户端级 FGAP 权限。</p>
 */
class RealmPermissionsV2 extends RealmPermissions {

    /** 构造 V2 领域权限评估器。 */
    public RealmPermissionsV2(MgmtPermissions root) {
        super(root);
    }

    @Override
    public boolean canManageAuthorizationDefault(ResourceServer resourceServer) {
        // ResourceServer 属于 admin-permissions 客户端时，检查 manage-realm
        if (resourceServer != null && AdminPermissionsSchema.SCHEMA.isAdminPermissionClient(root.realm, resourceServer.getId())) {
            return super.canManageRealm();
        }
        if (super.canManageAuthorizationDefault(resourceServer)) {
            return true;
        }

        return root.clients().canManage(getClient(resourceServer));
    }

    @Override
    public boolean canViewAuthorizationDefault(ResourceServer resourceServer) {
        // ResourceServer 属于 admin-permissions 客户端时，检查 manage-realm 或 view-realm
        if (resourceServer != null && AdminPermissionsSchema.SCHEMA.isAdminPermissionClient(root.realm, resourceServer.getId())) {
            return super.canViewRealm();
        }
        if (super.canViewAuthorizationDefault(resourceServer)) {
            return true;
        }

        return root.clients().canView(getClient(resourceServer));
    }

    /** 由 ResourceServer ID 解析所属客户端。 */
    private ClientModel getClient(ResourceServer resourceServer) {
        if (resourceServer == null) return null;
        return root.session.clients().getClientById(root.realm, resourceServer.getId());
    }
}
