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
 * 用户持久化服务接口。
 *
 * <p>定义用户创建、删除、改密、单查与分页/模糊查询； 实现类根据部署模式选择内嵌或外部 JDBC 访问。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public interface UserPersistService {
    
    /**
     * 创建新用户。
     *
     * @param username username
     * @param password password
     */
    void createUser(String username, String password);
    
    /**
     * 按用户名删除用户。
     *
     * @param username username
     */
    void deleteUser(String username);
    
    /**
     * 更新用户密码。
     *
     * @param username username
     * @param password password
     */
    void updateUserPassword(String username, String password);
    
    /**
     * 按用户名精确查询用户。
     *
     * @param username username
     * @return user
     */
    User findUserByUsername(String username);
    
    /**
     * 分页查询用户列表。
     *
     * @param pageNo pageNo
     * @param pageSize pageSize
     * @return user page info
     */
    Page<User> getUsers(int pageNo, int pageSize, String username);
    
    /**
     * 用户名模糊匹配，返回用户名集合。
     *
     * @param username username
     * @return usernames
     */
    List<String> findUserLikeUsername(String username);
    
    /** 用户名模糊查询并分页返回用户实体。 */
    Page<User> findUsersLike4Page(String username, int pageNo, int pageSize);
    
    String generateLikeArgument(String s);
    
    /**
     * create Pagination utils.
     *
     * @param <E> Generic object
     * @return {@link AuthPaginationHelper}
      * <p>Nacos 3.2.3：默认鉴权插件持久化层；涵盖用户/角色/权限 CRUD、内嵌与外部 JDBC 分页助手及 MySQL/Derby 方言适配器。</p>
     */
    <E> AuthPaginationHelper<E> createPaginationHelper();
}
