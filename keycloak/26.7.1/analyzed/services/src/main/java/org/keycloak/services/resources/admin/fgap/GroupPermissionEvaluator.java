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
import org.keycloak.models.GroupModel;

/**
 * 组细粒度管理权限评估接口。
 * <p>定义组列表、查看、管理、成员与成员关系等权限判定。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface GroupPermissionEvaluator {

    /**
     * 是否可列出组（QUERY_GROUPS/MANAGE_USERS/VIEW_USERS 或 V2 组 VIEW/MANAGE）。
     */
    boolean canList();

    /**
     * Throws ForbiddenException if {@link #canList()} returns {@code false}.
     */
    void requireList();

    /**
     * Returns {@code true} if the caller has {@link AdminRoles#MANAGE_USERS} role.
     * <p/>
     * Or if it has a permission to {@link AdminPermissionsSchema#MANAGE} the group.
     */
    boolean canManage(GroupModel group);

    /**
     * Throws ForbiddenException if {@link #canManage(GroupModel)} returns {@code false}.
     */
    void requireManage(GroupModel group);

    /**
     * Returns {@code true} if the caller has one of {@link AdminRoles#MANAGE_USERS} or
     * {@link AdminRoles#VIEW_USERS} roles.
     * <p/>
     * Or if it has a permission to {@link AdminPermissionsSchema#VIEW} the group.
     */
    boolean canView(GroupModel group);

    /**
     * Throws ForbiddenException if {@link #canView(GroupModel)} returns {@code false}.
     */
    void requireView(GroupModel group);

    /**
     * Returns {@code true} if the caller has {@link AdminRoles#MANAGE_USERS} role.
     * <p/>
     * For V2 only: Also if it has permission to {@link AdminPermissionsSchema#MANAGE} groups.
     */
    boolean canManage();

    /**
     * Throws ForbiddenException if {@link #canManage()} returns {@code false}.
     */
    void requireManage();

    /**
     * Returns {@code true} if the caller has one of {@link AdminRoles#MANAGE_USERS} or 
     * {@link AdminRoles#VIEW_USERS} roles.
     * <p/>
     * Or if it has a permission to {@link AdminPermissionsSchema#VIEW} groups.
     */
    boolean canView();

    /**
     * Throws ForbiddenException if {@link #canView()} returns {@code false}.
     */
    void requireView();

    /**
     * Throws ForbiddenException if {@link #canViewMembers(GroupModel)} returns {@code false}.
     */
    void requireViewMembers(GroupModel group);

    /**
     * Returns {@code true} if the caller has {@link AdminRoles#MANAGE_USERS} role.
     * <p/>
     * Or if it has a permission to {@link AdminPermissionsSchema#MANAGE_MEMBERS} of the group.
     */
    boolean canManageMembers(GroupModel group);

    /**
     * Returns {@code true} if the caller has {@link AdminRoles#MANAGE_USERS} role.
     * <p/>
     * Or if it has a permission to {@link AdminPermissionsSchema#MANAGE_MEMBERSHIP} of the group.
     */
    boolean canManageMembership(GroupModel group);

    /**
     * Returns {@code true} if the caller has one of {@link AdminRoles#MANAGE_USERS} or 
     * {@link AdminRoles#VIEW_USERS} roles.
     * <p/>
     * Or if it has a permission to {@link AdminPermissionsSchema#VIEW_MEMBERS} of the group.
     */
    boolean canViewMembers(GroupModel group);

    /**
     * Throws ForbiddenException if {@link #canManageMembership(GroupModel)} returns {@code false}.
     */
    void requireManageMembership(GroupModel group);

    /**
     * Throws ForbiddenException if {@link #canManageMembership(GroupModel)} returns {@code false}.
     */
    void requireManageMembers(GroupModel group);

    /**
     * 返回调用者对指定组的 view/manage/manageMembership/viewMembers/manageMembers 访问能力。
     */
    Map<String, Boolean> getAccess(GroupModel group);

    /**
     * 若 {@link UserPermissionEvaluator#canView()} 为 true 则返回空集；否则返回有 view 权限的组 ID。
     * @return 可查看成员的组 ID 集合
     */
    Set<String> getGroupIdsWithViewPermission();
}
