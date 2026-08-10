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

package org.keycloak.services.resources.admin.fgap;

import jakarta.ws.rs.ForbiddenException;

import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.services.resources.admin.AdminAuth;


/**
 * 领域级管理 REST 资源的粗粒度角色权限辅助类。
 * <p>基于 {@link AdminAuth} 与 realm-management 客户端角色，按 {@link AdminAuth.Resource}
 * 类型判断查看/管理权限，不满足时抛出 {@link ForbiddenException}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
class RealmAuth {

    /** 当前校验的管理资源类型 */
    private AdminAuth.Resource resource;

    /** 管理员认证上下文 */
    private AdminAuth auth;
    /** realm-management 客户端 */
    private ClientModel realmAdminApp;

    /** 绑定认证上下文与 realm 管理客户端。 */
    public RealmAuth(AdminAuth auth, ClientModel realmAdminApp) {
        this.auth = auth;
        this.realmAdminApp = realmAdminApp;
    }

    /** 设置待校验的资源类型并返回自身以支持链式调用。 */
    public RealmAuth init(AdminAuth.Resource resource) {
        this.resource = resource;
        return this;
    }

    /** 返回底层 {@link AdminAuth}。 */
    public AdminAuth getAuth() {
        return auth;
    }

    /** 要求调用者至少拥有任一 realm 管理角色，否则抛出 403。 */
    public void requireAny() {
        if (!hasAny()) {
            throw new ForbiddenException();
        }
    }

    /** 是否拥有任一 realm 管理角色。 */
    public boolean hasAny() {
        return auth.hasOneOfAppRole(realmAdminApp, AdminRoles.ALL_REALM_ROLES);
    }

    /** 是否拥有当前资源的查看或管理角色。 */
    public boolean hasView() {
        return auth.hasOneOfAppRole(realmAdminApp, getViewRole(resource), getManageRole(resource));
    }

    /** 是否拥有当前资源的管理角色。 */
    public boolean hasManage() {
        return auth.hasOneOfAppRole(realmAdminApp, getManageRole(resource));
    }

    /** 要求拥有查看权限，否则抛出 403。 */
    public void requireView() {
        if (!hasView()) {
            throw new ForbiddenException();
        }
    }

    /** 要求拥有管理权限，否则抛出 403。 */
    public void requireManage() {
        if (!hasManage()) {
            throw new ForbiddenException();
        }
    }

    /** 按资源类型返回对应的查看角色名。 */
    private String getViewRole(AdminAuth.Resource resource) {
        switch (resource) {
            case CLIENT:
                return AdminRoles.VIEW_CLIENTS;
            case USER:
                return AdminRoles.VIEW_USERS;
            case REALM:
                return AdminRoles.VIEW_REALM;
            case EVENTS:
                return AdminRoles.VIEW_EVENTS;
            case IDENTITY_PROVIDER:
                return AdminRoles.VIEW_IDENTITY_PROVIDERS;
            case AUTHORIZATION:
                return AdminRoles.VIEW_AUTHORIZATION;
            default:
                throw new IllegalStateException();
        }
    }

    /** 按资源类型返回对应的管理角色名。 */
    private String getManageRole(AdminAuth.Resource resource) {
        switch (resource) {
            case CLIENT:
                return AdminRoles.MANAGE_CLIENTS;
            case USER:
                return AdminRoles.MANAGE_USERS;
            case REALM:
                return AdminRoles.MANAGE_REALM;
            case EVENTS:
                return AdminRoles.MANAGE_EVENTS;
            case IDENTITY_PROVIDER:
                return AdminRoles.MANAGE_IDENTITY_PROVIDERS;
            case IMPERSONATION:
                return AdminRoles.IMPERSONATION;
            case AUTHORIZATION:
                return AdminRoles.MANAGE_AUTHORIZATION;
            default:
                throw new IllegalStateException();
        }
    }

}
