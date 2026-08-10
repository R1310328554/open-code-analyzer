/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.authenticate;

import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Nacos 鉴权认证管理器接口：统一用户名密码、JWT 与 HTTP 请求认证及 RBAC 授权。
 *
 * @author Weizhan▪Yun
 * @date 2023/1/12 23:31
 */
public interface IAuthenticationManager {
    
    /**
     * 用户名密码认证。
     *
     * @param username    username
     * @param rawPassword raw password
     * @return 认证成功后的 {@link NacosUser}
     * @throws AccessException if authentication is failed
     */
    NacosUser authenticate(String username, String rawPassword) throws AccessException;
    
    /**
     * JWT Token 认证。
     *
     * @param jwtToken json web token
     * @return nacos user
     * @throws AccessException if authentication is failed
     */
    NacosUser authenticate(String jwtToken) throws AccessException;
    
    /**
     * 从 HTTP 请求识别并认证访问用户。
     *
     * @param httpServletRequest http servlet request
     * @return nacos user
     * @throws AccessException if authentication is failed
     */
    NacosUser authenticate(HttpServletRequest httpServletRequest) throws AccessException;
    
    /**
     * 校验 {@link NacosUser} 是否具备指定 {@link Permission}。
     *
     * @param permission permission to auth
     * @param nacosUser  nacosUser who wants to access the resource.
     * @throws AccessException if authorization is failed
     */
    void authorize(Permission permission, NacosUser nacosUser) throws AccessException;
    
    /**
     * 指定用户是否拥有全局管理员角色。
     *
     * @param username nacos user name
     * @return if the user has the administrator role.
     */
    boolean hasGlobalAdminRole(String username);
    
    /**
     * 系统中是否已存在全局管理员角色。
     *
     * @return if the user exist the administrator role.
     */
    boolean hasGlobalAdminRole();
    
    /**
     * 给定 {@link NacosUser} 是否为全局管理员。
     *
     * @param nacosUser nacos user name
     * @return if the user has the administrator role.
     */
    boolean hasGlobalAdminRole(NacosUser nacosUser);
}
