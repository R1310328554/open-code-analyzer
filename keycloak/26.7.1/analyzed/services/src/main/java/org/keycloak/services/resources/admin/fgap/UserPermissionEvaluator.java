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

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ImpersonationConstants;
import org.keycloak.models.UserModel;

/**
 * 用户细粒度管理权限评估接口。
 * <p>定义管理端对用户资源的查看、管理、模拟登录、角色映射、组成员关系及密码重置等权限判定与强制检查。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserPermissionEvaluator {

    /**
     * 若 {@link #canManage()} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireManage();

    /**
     * 若 {@link #canManage(UserModel)} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireManage(UserModel user);

    /**
     * 调用者是否可管理全部用户：持有 {@link AdminRoles#MANAGE_USERS} 角色，
     * 或拥有 {@link AdminPermissionsSchema#MANAGE} 用户权限。
     */
    boolean canManage();

    /**
     * 调用者是否可管理指定用户：持有 {@link AdminRoles#MANAGE_USERS} 角色，
     * 或拥有对该用户的 {@link AdminPermissionsSchema#MANAGE} 权限，
     * 或拥有用户所属组链上的 {@link AdminPermissionsSchema#MANAGE_MEMBERS} 权限。
     */
    boolean canManage(UserModel user);

    /**
     * 若 {@link #canResetPassword(UserModel)} 返回 {@code false}，则抛出 ForbiddenException。
     */
    default void requireResetPassword(UserModel user) {
        if (!canResetPassword(user)) {
            throw new jakarta.ws.rs.ForbiddenException();
        }
    }

    /**
     * 调用者是否拥有对指定用户的 {@link org.keycloak.authorization.fgap.AdminPermissionsSchema#RESET_PASSWORD} 权限。
     * <p>默认实现回退至 {@link #canManage(UserModel)} 以保持向后兼容。</p>
     */
    default boolean canResetPassword(UserModel user) {
        return canManage(user);
    }

    /**
     * 若 {@link #canQuery()} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireQuery();

    /**
     * 调用者是否可查询用户：持有 QUERY/MANAGE/VIEW_USERS 角色之一，
     * 或拥有 {@link AdminPermissionsSchema#VIEW} 用户权限。
     */
    boolean canQuery();

    /**
     * 若 {@link #canView()} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireView();

    /**
     * 若 {@link #canView(UserModel)} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireView(UserModel user);

    /**
     * 调用者是否可查看全部用户：持有 MANAGE/VIEW_USERS 角色之一，
     * 或拥有 {@link AdminPermissionsSchema#VIEW} 用户权限。
     */
    boolean canView();

    /**
     * 调用者是否可查看指定用户：持有 MANAGE/VIEW_USERS 角色之一，
     * 或拥有对该用户的 {@link AdminPermissionsSchema#VIEW} 权限，
     * 或拥有用户所属组链上的 {@link AdminPermissionsSchema#VIEW_MEMBERS} 权限。
     */
    boolean canView(UserModel user);

    /**
     * 若 {@link #canImpersonate(UserModel, ClientModel)} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireImpersonate(UserModel user);

    /**
     * 调用者是否可模拟登录用户：持有 {@link ImpersonationConstants#IMPERSONATION_ROLE} 角色，
     * 或拥有 {@link AdminPermissionsSchema#IMPERSONATE} 用户权限。
     */
    boolean canImpersonate();

    /**
     * 调用者是否可模拟登录指定用户：持有模拟登录角色或拥有 IMPERSONATE 权限。
     * <p>若提供 requester，其 clientId 会加入评估上下文。</p>
     */
    boolean canImpersonate(UserModel user, ClientModel requester);

    /**
     * 返回调用者对指定用户拥有的各项访问权限映射（view/manage/mapRoles 等）。
     */
    Map<String, Boolean> getAccess(UserModel user);

    /**
     * 返回用户列表场景下调用者对指定用户的访问权限映射。
     */
    Map<String, Boolean> getAccessForListing(UserModel user);

    /**
     * 若 {@link #canMapRoles(UserModel)} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireMapRoles(UserModel user);

    /**
     * 调用者是否可为指定用户映射角色：持有 MANAGE_USERS 角色，
     * 或拥有 {@link AdminPermissionsSchema#MAP_ROLES} 权限。
     */
    boolean canMapRoles(UserModel user);

    /**
     * 若 {@link #canManageGroupMembership(UserModel)} 返回 {@code false}，则抛出 ForbiddenException。
     */
    void requireManageGroupMembership(UserModel user);

    /**
     * 调用者是否可管理指定用户的组成员关系：持有 MANAGE_USERS 角色，
     * 或拥有 {@link AdminPermissionsSchema#MANAGE_GROUP_MEMBERSHIP} 权限。
     */
    boolean canManageGroupMembership(UserModel user);

    /** @deprecated 请使用 {@link #canImpersonate(UserModel, ClientModel)} */
    @Deprecated
    boolean isImpersonatable(UserModel user, ClientModel requester);
    /** @deprecated 无权限时是否自动授予（V1 兼容） */
    @Deprecated
    void grantIfNoPermission(boolean grantIfNoPermission);
}