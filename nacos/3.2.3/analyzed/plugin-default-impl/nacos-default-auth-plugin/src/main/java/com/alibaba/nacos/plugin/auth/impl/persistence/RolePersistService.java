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

import java.util.List;

/**
 * 角色持久化服务接口。
 *
 * <p>涵盖角色分页查询、用户-角色绑定增删、模糊搜索及分页助手工厂方法； 由 Embedded/External 实现类分别对接内嵌与外部数据库。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public interface RolePersistService {
    
    /**
     * 分页查询全部角色信息。
     *
     * @param pageNo pageNo
     * @param pageSize pageSize
     * @return roles page info
     */
    Page<RoleInfo> getRoles(int pageNo, int pageSize);
    
    /**
     * 按用户名与角色名过滤后分页查询。
     *
     * @param username username
     * @param pageNo pageNo
     * @param pageSize pageSize
     * @return roles page info
     */
    Page<RoleInfo> getRolesByUserNameAndRoleName(String username, String role, int pageNo,
        int pageSize);
    
    /**
     * 为用户分配角色。
     *
     * @param role role
     * @param userName username
     */
    void addRole(String role, String userName);
    
    /**
     * 删除角色及其全部用户绑定。
     *
     * @param role role
     */
    void deleteRole(String role);
    
    /**
     * 解除指定用户的角色绑定。
     *
     * @param role role
     * @param username username
     */
    void deleteRole(String role, String username);
    
    /**
     * 按角色名模糊查询角色列表。
     *
     * @param role role
     * @return roles
     */
    List<String> findRolesLikeRoleName(String role);
    
    /**
     * 将用户输入转为 SQL LIKE 参数。
     *
     * @param s origin string
     * @return fuzzy search Sql
     */
    String generateLikeArgument(String s);
    
    /**
     * 用户名与角色名模糊查询并分页。
     *
     * @param username username of user
     * @param pageNo page number
     * @param pageSize page size
     * @return {@link Page} with {@link RoleInfo} generation
     */
    Page<RoleInfo> findRolesLike4Page(String username, String role, int pageNo, int pageSize);
    
    /**
     * create Pagination utils.
     *
     * @param <E> Generic object
     * @return {@link AuthPaginationHelper}
      * <p>Nacos 3.2.3：默认鉴权插件持久化层；涵盖用户/角色/权限 CRUD、内嵌与外部 JDBC 分页助手及 MySQL/Derby 方言适配器。</p>
     */
    <E> AuthPaginationHelper<E> createPaginationHelper();
}
