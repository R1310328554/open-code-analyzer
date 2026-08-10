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

import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;

/**
 * 角色细粒度管理权限评估接口。
 * <p>定义角色列表、映射、查看、管理及复合/客户端 scope 映射等操作的权限判断。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RolePermissionEvaluator {

    /**
     * 若 {@link #canView(RoleContainerModel)} 返回 {@code true} 则返回 {@code true}。
     * <p/>
     * 对领域角色，若 {@link RealmPermissionEvaluator#canViewRealm()} 为真，
     * 或调用者拥有 {@link org.keycloak.models.AdminRoles#QUERY_USERS}、
     * {@link org.keycloak.models.AdminRoles#QUERY_CLIENTS}、
     * {@link org.keycloak.models.AdminRoles#QUERY_REALMS}、
     * {@link org.keycloak.models.AdminRoles#QUERY_GROUPS} 之一，也返回 {@code true}。
     * <p/>
     */
    boolean canList(RoleContainerModel container);

    /**
     * 若 {@link #canList(RoleContainerModel)} 返回 {@code false} 则抛出 {@link jakarta.ws.rs.ForbiddenException}。
     */
    void requireList(RoleContainerModel container);


    /**
     * 若调用者拥有 {@link org.keycloak.models.AdminRoles#MANAGE_USERS} 且
     * {@link RolePermissions#checkAdminRoles(RoleModel)} 返回 {@code true}，则返回 {@code true}。
     * <p/>
     * 或对客户端角色，若 {@link ClientPermissions#canMapRoles(ClientModel)} 返回 {@code true}。
     * <p/>
     * 或调用者对 {@link RolePermissionManagement#MAP_ROLE_SCOPE} 有权限且
     * {@link RolePermissions#checkAdminRoles(RoleModel)} 返回 {@code true}。
     * <p/>
     * V2 额外：若调用者对全部角色的 {@link RolePermissionManagement#MAP_ROLE_SCOPE} 有权限也返回 {@code true}。
     */
    boolean canMapRole(RoleModel role);

    /**
     * 若 {@link #canMapRole(RoleModel)} 返回 {@code false} 则抛出 ForbiddenException。
     */
    void requireMapRole(RoleModel role);

    /**
     * 领域角色：{@link RealmPermissions#canManageRealm()} 为真时返回 {@code true}。
     * <p/>
     * 客户端角色：{@link ClientPermissions#canConfigure(ClientModel)} 为真时返回 {@code true}。
     */
    boolean canManage(RoleModel role);

    /**
     * 若 {@link #canManage(RoleModel)} 返回 {@code false} 则抛出 ForbiddenException。
     */
    void requireManage(RoleModel role);

    /**
     * 领域角色：{@link RealmPermissions#canViewRealm()} 为真时返回 {@code true}。
     * <p/>
     * 客户端角色：{@link ClientPermissions#canView(ClientModel)} 为真时返回 {@code true}。
     */
    boolean canView(RoleModel role);

    /**
     * 若 {@link #canView(RoleModel)} 返回 {@code false} 则抛出 ForbiddenException。
     */
    void requireView(RoleModel role);

    /**
     * 若 {@link ClientPermissions#canManageClientsDefault()} 返回 {@code true} 则返回 {@code true}。
     * <p/>
     * 或对客户端角色，若 {@link ClientPermissions#canMapClientScopeRoles(ClientModel)} 返回 {@code true}。
     * <p/>
     * 或调用者对 {@link RolePermissionManagement#MAP_ROLE_CLIENT_SCOPE_SCOPE} 有权限。
     * <p/>
     * V2 额外：若调用者对全部角色的 {@link RolePermissionManagement#MAP_ROLE_CLIENT_SCOPE_SCOPE} 有权限也返回 {@code true}。
     */
    boolean canMapClientScope(RoleModel role);

    /**
     * 若 {@link #canMapClientScope(RoleModel)} 返回 {@code false} 则抛出 ForbiddenException。
     */
    void requireMapClientScope(RoleModel role);

    /**
     * 若 {@link RolePermissions#canManageDefault(RoleModel)} 与
     * {@link RolePermissions#checkAdminRoles(RoleModel)} 均为 {@code true} 则返回 {@code true}。
     * <p/>
     * 或对客户端角色，若 {@link ClientPermissions#canMapCompositeRoles(ClientModel)} 返回 {@code true}。
     * <p/>
     * 或调用者对 {@link RolePermissionManagement#MAP_ROLE_COMPOSITE_SCOPE} 有权限且
     * {@link RolePermissions#checkAdminRoles(RoleModel)} 返回 {@code true}。
     * <p/>
     * V2 额外：若调用者对全部角色的 {@link RolePermissionManagement#MAP_ROLE_COMPOSITE_SCOPE} 有权限也返回 {@code true}。
     */
    boolean canMapComposite(RoleModel role);

    /**
     * 若 {@link #canMapComposite(RoleModel)} 返回 {@code false} 则抛出 ForbiddenException。
     */
    void requireMapComposite(RoleModel role);

    /**
     * 领域角色容器：{@link RealmPermissions#canManageRealm()} 为真时返回 {@code true}。
     * <p/>
     * 客户端角色容器：{@link ClientPermissions#canConfigure(ClientModel)} 为真时返回 {@code true}。
     */
    boolean canManage(RoleContainerModel container);

    /**
     * 若 {@link #canManage(RoleContainerModel)} 返回 {@code false} 则抛出 ForbiddenException。
     */
    void requireManage(RoleContainerModel container);

    /**
     * 领域角色容器：{@link RealmPermissions#canViewRealm()} 为真时返回 {@code true}。
     * <p/>
     * 客户端角色容器：{@link ClientPermissions#canView(ClientModel)} 为真时返回 {@code true}。
     */
    boolean canView(RoleContainerModel container);

    /**
     * 若 {@link #canView(RoleContainerModel)} 返回 {@code false} 则抛出 ForbiddenException。
     */
    void requireView(RoleContainerModel container);

    /**
     * 返回当前用户对指定 {@code scope} 有权限的角色 ID 集合。
     *
     * @return 拥有 {@code scope} 权限的角色 ID 集合
     */
    Set<String> getRoleIdsByScope(String scope);
}
