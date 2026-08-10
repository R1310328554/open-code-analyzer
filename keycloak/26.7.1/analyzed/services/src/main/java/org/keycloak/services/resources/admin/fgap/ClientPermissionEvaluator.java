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

import java.util.Map;
import java.util.Set;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;

/**
 * 客户端细粒度管理权限评估接口。
 * <p>定义客户端及客户端范围上的列表/查看/管理/配置/角色映射等权限判定与强制检查。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientPermissionEvaluator {
    /** 客户端是否已启用细粒度管理权限 */
    boolean isPermissionsEnabled(ClientModel client);

    /** 启用或禁用客户端细粒度管理权限 */
    void setPermissionsEnabled(ClientModel client, boolean enable);

    /**
     * Throws ForbiddenException if {@link #canListClientScopes()} returns {@code false}.
     */
    void requireListClientScopes();

    /**
     * 调用者是否可管理客户端（V1：{@link org.keycloak.models.AdminRoles#MANAGE_CLIENTS}；V2：另含 {@link AdminPermissionsSchema#MANAGE}）。
     */
    boolean canManage();

    /**
     * Throws ForbiddenException if {@link #canManage()} returns {@code false}.
     */
    void requireManage();

    /**
     * Returns {@code true} if the caller has {@link org.keycloak.models.AdminRoles#MANAGE_CLIENTS} role.
     * <p/>
     * For V2 only: Also if it has permission to {@link AdminPermissionsSchema#MANAGE}.
     */
    boolean canManageClientScopes();

    /**
     * Throws ForbiddenException if {@link #canManageClientScopes()} returns {@code false}.
     */
    void requireManageClientScopes();

    /**
     * 是否可查看客户端（管理/查看角色或 V2 {@link AdminPermissionsSchema#VIEW}）。
     */
    boolean canView();

    /**
     * 是否可列出客户端（可查看、QUERY_CLIENTS 或 V1 下 QUERY_USERS）。
     */
    boolean canList();

    /**
     * Returns {@code true} if {@link #canView()} returns {@code true}.
     */
    boolean canViewClientScopes();

    /**
     * Throws ForbiddenException if {@link #canList()} returns {@code false}.
     */
    void requireList();

    /**
     * Returns {@code true} if {@link #canView()} returns {@code true}.
     * <p/>
     * Or if the caller has {@link AdminRoles#QUERY_CLIENTS} role.
     */
    boolean canListClientScopes();

    /**
     * Returns {@code true} if {@link #canView()} returns {@code true}.
     */
    void requireView();

    /**
     * Returns {@code true} if {@link #canViewClientScopes()} returns {@code true}.
     */
    void requireViewClientScopes();

    /**
     * Returns {@code true} if the caller has {@link org.keycloak.models.AdminRoles#MANAGE_CLIENTS} role.
     * <p/>
     * Or if the caller has a permission to {@link AdminPermissionManagement#MANAGE_SCOPE} the client.
     * <p/>
     * For V2 only: Also if the caller has a permission to {@link AdminPermissionsSchema#MANAGE} all clients.
     */
    boolean canManage(ClientModel client);

    /**
     * 是否可配置指定客户端（可 manage 或持有 configure 作用域；V2 重定向至 {@code canManage(ClientModel)}）。
     */
    boolean canConfigure(ClientModel client);

    /**
     * Throws ForbiddenException if {@link #canConfigure(ClientModel)} returns {@code false}.
     * <p/>
     * For V2 only: the call is redirected to {@code requireManage(ClientModel)}.
     */
    void requireConfigure(ClientModel client);

    /**
     * Throws ForbiddenException if {@link #canManage(ClientModel)}  returns {@code false}.
     */
    void requireManage(ClientModel client);

    /**
     * 是否可查看指定客户端（全局可查看、可配置或持有 view 作用域/V2 类型级 VIEW）。
     */
    boolean canView(ClientModel client);

    /**
     * Throws ForbiddenException if {@link #canView(ClientModel)}  returns {@code false}.
     */
    void requireView(ClientModel client);

    /**
     * Returns {@code true} if the caller has {@link org.keycloak.models.AdminRoles#MANAGE_CLIENTS} role.
     * <p/>
     * For V2 only: Also if it has permission to {@link AdminPermissionsSchema#MANAGE}.
     */
    boolean canManage(ClientScopeModel clientScope);

    /**
     * Throws ForbiddenException if {@link #canManage(ClientScopeModel)} returns {@code false}.
     */
    void requireManage(ClientScopeModel clientScope);

    /**
     * Returns {@code true} if the caller has at least one of the {@link org.keycloak.models.AdminRoles#VIEW_CLIENTS} or {@link org.keycloak.models.AdminRoles#MANAGE_CLIENTS} roles.
     * <p/>
     * For V2 only: Also if it has permission to {@link AdminPermissionsSchema#VIEW}.
     */
    boolean canView(ClientScopeModel clientScope);

    /**
     * Throws ForbiddenException if {@link #canView(ClientScopeModel)} returns {@code false}.
     */
    void requireView(ClientScopeModel clientScope);

    /**
     * Returns {@code true} if the caller has a permission to {@link ClientPermissionManagement#MAP_ROLES_SCOPE} for the client.
     * <p/>
     * For V2 only: Also if the caller has a permission to {@link AdminPermissionsSchema#MAP_ROLES} for all clients.
     */
    boolean canMapRoles(ClientModel client);

    /**
     * Returns {@code true} if the caller has a permission to {@link ClientPermissionManagement#MAP_ROLES_COMPOSITE_SCOPE} for the client.
     * <p/>
     * For V2 only: Also if the caller has a permission to {@link AdminPermissionsSchema#MAP_ROLES_COMPOSITE} for all clients.
     */
    boolean canMapCompositeRoles(ClientModel client);

    /**
     * Returns {@code true} if the caller has a permission to {@link ClientPermissionManagement#MAP_ROLES_CLIENT_SCOPE} for the client.
     * <p/>
     * For V2 only: Also if the caller has a permission to {@link AdminPermissionsSchema#MAP_ROLES_CLIENT_SCOPE} for all clients.
     */
    boolean canMapClientScopeRoles(ClientModel client);

    /** 返回调用者对指定客户端的各项访问能力映射 */
    Map<String, Boolean> getAccess(ClientModel client);

    /**
     * 返回当前用户对 {@code scope} 拥有权限的客户端 ID 集合。
     * @return 具备该作用域权限的客户端 ID
     */
    Set<String> getClientIdsByScope(String scope);
}
