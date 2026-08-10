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
import org.keycloak.models.ClientModel;
import org.keycloak.models.UserModel;

/**
 * 用户细粒度管理权限配置接口。
 * <p>管理 Users 授权资源及其 manage/view/map-roles/impersonate 等权限策略的启用与查询。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserPermissionManagement {
    /** 用户细粒度权限是否已启用 */
    boolean isPermissionsEnabled();

    /** 启用或禁用用户细粒度权限 */
    void setPermissionsEnabled(boolean enable);

    /** 返回各作用域对应的权限策略 ID 映射 */
    Map<String, String> getPermissions();

    /** 返回 Users 授权资源 */
    Resource resource();

    /** 返回 manage 权限策略 */
    Policy managePermission();

    /** 返回 view 权限策略 */
    Policy viewPermission();

    /** 返回 manage-group-membership 权限策略 */
    Policy manageGroupMembershipPermission();

    /** 返回 map-roles 权限策略 */
    Policy mapRolesPermission();

    /** 返回管理员模拟登录权限策略 */
    Policy adminImpersonatingPermission();

    /** 返回用户被模拟登录权限策略 */
    Policy userImpersonatedPermission();

    /** 指定客户端是否可模拟登录该用户 */
    boolean canClientImpersonate(ClientModel client, UserModel user);

    /** 该用户是否允许被模拟登录 */
    boolean isImpersonatable(UserModel user);
}
