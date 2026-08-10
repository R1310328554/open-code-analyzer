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

import org.keycloak.services.resources.admin.AdminAuth;

/**
 * 管理端细粒度权限评估器门面。
 * <p>聚合领域、角色、用户、客户端、组与组织等子评估器，供 Admin REST 资源校验操作权限。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AdminPermissionEvaluator {
    /** 领域级权限评估 */
    RealmPermissionEvaluator realm();

    /** 要求调用者至少持有一个管理角色，否则抛出异常 */
    void requireAnyAdminRole();
    /** 是否持有给定管理角色之一 */
    boolean hasOneAdminRole(String... adminRoles);
    /** 要求 realm 管理权限 */
    void requireRealmAdmin();

    /** 当前管理认证上下文 */
    AdminAuth adminAuth();

    /** 角色权限评估器 */
    RolePermissionEvaluator roles();
    /** 用户权限评估器 */
    UserPermissionEvaluator users();
    /** 客户端权限评估器 */
    ClientPermissionEvaluator clients();
    /** 组权限评估器 */
    GroupPermissionEvaluator groups();
    /** 组织权限评估器 */
    OrganizationPermissionEvaluator orgs();

    /** 是否为领域管理员 */
    boolean isRealmAdmin();

    /**
     * 权限检查函数式接口（如 RoleMapperResource 在用户/组间复用时无法预知资源类型）。
     * <p>由调用方注入具体评估逻辑。</p>
     */
    interface PermissionCheck {
        boolean evaluate();
    }
    /**
     * 强制权限检查函数式接口；拒绝时抛出相应异常。
     * <p>用于 RoleMapperResource 等复用场景。</p>
     */
    interface RequirePermissionCheck {
        void require();
    }
}
