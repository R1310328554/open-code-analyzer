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

import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.RoleModel;

/**
 * 角色细粒度管理权限的配置与管理接口（V1 模型）。
 * <p>为每个 {@link RoleModel} 创建授权资源与 map-role / map-role-client-scope / map-role-composite 权限策略。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RolePermissionManagement {
    /** 将角色映射到用户的 scope 名称。 */
    public static final String MAP_ROLE_SCOPE = "map-role";
    /** 将角色映射到客户端 scope 的 scope 名称。 */
    public static final String MAP_ROLE_CLIENT_SCOPE_SCOPE = "map-role-client-scope";
    /** 将角色作为复合角色关联的 scope 名称。 */
    public static final String MAP_ROLE_COMPOSITE_SCOPE = "map-role-composite";

    /** 指定角色是否已启用细粒度权限。 */
    boolean isPermissionsEnabled(RoleModel role);
    /** 启用或禁用指定角色的细粒度权限。 */
    void setPermissionsEnabled(RoleModel role, boolean enable);

    /** 返回角色各 scope 对应的权限策略 ID 映射。 */
    Map<String, String> getPermissions(RoleModel role);

    /** 返回 map-role scope 对应的权限策略。 */
    Policy mapRolePermission(RoleModel role);

    /** 返回 map-role-composite scope 对应的权限策略。 */
    Policy mapCompositePermission(RoleModel role);

    /** 返回 map-role-client-scope scope 对应的权限策略。 */
    Policy mapClientScopePermission(RoleModel role);

    /** 返回角色对应的授权资源。 */
    Resource resource(RoleModel role);

    /** 返回角色所属容器的 {@link ResourceServer}。 */
    ResourceServer resourceServer(RoleModel role);

    /** 返回 manage-users 角色对应的策略（用于默认权限绑定）。 */
    Policy manageUsersPolicy(ResourceServer server);

    /** 返回 view-users 角色对应的策略。 */
    Policy viewUsersPolicy(ResourceServer server);

    /** 查找或创建指定角色的角色策略。 */
    Policy rolePolicy(ResourceServer server, RoleModel role);
}
