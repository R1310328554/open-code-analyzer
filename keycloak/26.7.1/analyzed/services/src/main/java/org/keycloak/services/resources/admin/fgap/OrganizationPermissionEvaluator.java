/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.OrganizationModel;

/**
 * 组织（Organization）细粒度管理权限评估接口。
 * <p>提供组织级与全局的查看、管理及查询权限判断，以及对应的 {@code require*} 强制校验方法。</p>
 */
public interface OrganizationPermissionEvaluator {

    /** 是否可管理任意组织。 */
    boolean canManage();

    /** 是否可管理指定组织。 */
    boolean canManage(OrganizationModel organization);

    /** 要求具备组织管理权限，否则抛出 403。 */
    void requireManage();

    /** 要求具备指定组织的管理权限，否则抛出 403。 */
    void requireManage(OrganizationModel organization);

    /** 是否可查看任意组织。 */
    boolean canView();

    /** 是否可查看指定组织。 */
    boolean canView(OrganizationModel organization);

    /** 要求具备组织查看权限，否则抛出 403。 */
    void requireView();

    /** 要求具备指定组织的查看权限，否则抛出 403。 */
    void requireView(OrganizationModel organization);

    /** 是否可查询组织列表（含 query-organizations 角色或查看权限）。 */
    boolean canQuery();

    /** 要求具备组织查询权限，否则抛出 403。 */
    void requireQuery();
}
