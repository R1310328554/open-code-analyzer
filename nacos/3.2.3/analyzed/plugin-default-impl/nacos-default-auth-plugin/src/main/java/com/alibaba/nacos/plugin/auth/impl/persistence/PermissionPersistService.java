/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.auth.impl.persistence;

import com.alibaba.nacos.api.model.Page;

/**
 * 权限持久化服务接口。
 *
 * <p>定义角色权限的分页查询、授予、撤销及模糊搜索； 实现类区分内嵌 Derby 与外部 MySQL 等数据源。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public interface PermissionPersistService {
    
    /**
     * 分页查询指定角色的权限列表。
     *
     * @param role role
     * @param pageNo pageNo
     * @param pageSize pageSize
     * @return permissions page info
     */
    Page<PermissionInfo> getPermissions(String role, int pageNo, int pageSize);
    
    /**
     * 为角色授予对资源的操作权限。
     *
     * @param role role
     * @param resource resource
     * @param action action
     */
    void addPermission(String role, String resource, String action);
    
    /**
     * 撤销角色的指定资源操作权限。
     *
     * @param role role
     * @param resource resource
     * @param action action
     */
    void deletePermission(String role, String resource, String action);
    
    /** 按角色名模糊匹配并分页返回权限。 */
    Page<PermissionInfo> findPermissionsLike4Page(String role, int pageNo, int pageSize);
    
    /** 生成 SQL LIKE 模糊查询参数。 */
    String generateLikeArgument(String s);
    
    /**
     * 创建鉴权模块专用分页助手。
     *
     * @param <E> Generic object
     * @return {@link AuthPaginationHelper}
     */
    <E> AuthPaginationHelper<E> createPaginationHelper();
}
