/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.roles;

import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionInfo;
import com.alibaba.nacos.plugin.auth.impl.persistence.RoleInfo;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;

import java.util.List;

/**
 * Nacos 默认鉴权插件角色服务接口。
 *
 * <p>涵盖角色 CRUD、权限绑定、分页/模糊查询及全局管理员判定。</p>
 *
 * @author xiweng.yy
 */
public interface NacosRoleService {
    
    /**
     * 判断用户是否拥有指定资源权限。
     *
     * <p>用户多角色时任一角色匹配即返回 true。</p>
     *
     * @param nacosUser  user info
     * @param permission permission to auth
     * @return true if granted, false otherwise
     */
    boolean hasPermission(NacosUser nacosUser, Permission permission);
    
    /**
     * 为角色新增权限。
     *
     * @param role     role name
     * @param resource resource
     * @param action   action
     */
    void addPermission(String role, String resource, String action);
    
    /**
     * 删除角色的指定权限。
     *
     * @param role     role name
     * @param resource resource
     * @param action   action
     */
    void deletePermission(String role, String resource, String action);
    
    /**
     * 获取角色的全部权限。
     *
     * @param role role name
     * @return List of {@link PermissionInfo} for the role
     */
    List<PermissionInfo> getPermissions(String role);
    
    /**
     * 按角色名精确分页查询权限。
     *
     * @param role      role name pattern
     * @param pageNo    page number
     * @param pageSize  page size
     * @return List of {@link RoleInfo} match role name pattern
     */
    Page<PermissionInfo> getPermissions(String role, int pageNo, int pageSize);
    
    /**
     * 按角色名模糊分页查询权限。
     *
     * @param role      role name pattern
     * @param pageNo    page number
     * @param pageSize  page size
     * @return List of {@link RoleInfo} match role name pattern
     */
    Page<PermissionInfo> findPermissions(String role, int pageNo, int pageSize);
    
    /**
     * 判断权限是否重复（含 rw 通配动作）。
     *
     * @param role role name
     * @param resource resource
     * @param action action
     * @return true if duplicate, false otherwise
     */
    Result<Boolean> isDuplicatePermission(String role, String resource, String action);
    
    /**
     * 获取目标用户的全部角色绑定。
     *
     * @param username username of target user
     * @return List of {@link RoleInfo} for target user
     */
    List<RoleInfo> getRoles(String username);
    
    /**
     * Accurate search roles by role name pattern.
     *
     * @param username  username of target user
     * @param role      role name
     * @param pageNo    page number
     * @param pageSize  page size
     * @return List of {@link RoleInfo} match role name pattern
      * <p>Nacos 角色服务接口。</p>
     */
    Page<RoleInfo> getRoles(String username, String role, int pageNo, int pageSize);
    
    /**
     * Blur search roles by role name pattern.
     *
     * @param username  username of target user
     * @param role      role name pattern
     * @param pageNo    page number
     * @param pageSize  page size
     * @return List of {@link RoleInfo} match role name pattern
      * <p>Nacos 角色服务接口。</p>
     */
    Page<RoleInfo> findRoles(String username, String role, int pageNo, int pageSize);
    
    /**
     * Blur search role names by role name pattern.
     *
     * @param role role name pattern
     * @return List of {@link RoleInfo} match role name pattern
      * <p>Nacos 角色服务接口。</p>
     */
    List<String> findRoleNames(String role);
    
    /**
     * Get All roles in Nacos.
     *
     * @return List of {@link RoleInfo} in Nacos
      * <p>Nacos 角色服务接口。</p>
     */
    List<RoleInfo> getAllRoles();
    
    /**
     * 为用户绑定角色。
     *
     * @param role     role name
     * @param username user name
     */
    void addRole(String role, String username);
    
    /**
     * Delete Role from user.
     *
     * @param role     role
     * @param userName userName
      * <p>Nacos 角色服务接口。</p>
     */
    void deleteRole(String role, String userName);
    
    /**
     * Delete Role from Nacos.
     *
     * @param role role
      * <p>Nacos 角色服务接口。</p>
     */
    void deleteRole(String role);
    
    /**
     * Add role.
     *
     * @param username user name
      * <p>Nacos 角色服务接口。</p>
     */
    void addAdminRole(String username);
    
    /**
     * 判断用户是否拥有 GLOBAL_ADMIN 角色。
     *
     * @param userName user name
     * @return true if user has admin role.
     */
    boolean hasGlobalAdminRole(String userName);
    
    /**
     * 判断系统中是否已存在全局管理员角色。
     *
     * @return true if all user has at least one admin role.
     */
    boolean hasGlobalAdminRole();
}
