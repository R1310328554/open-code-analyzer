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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserDetails;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import com.alibaba.nacos.plugin.auth.impl.utils.PasswordEncoderUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证管理器抽象实现：封装用户名密码、JWT 与 HTTP 请求三种认证入口及 RBAC 授权。
 *
 * @author Weizhan▪Yun
 * @date 2023/1/13 12:48
 */
public class AbstractAuthenticationManager implements IAuthenticationManager {
    
    private static final String USER_NOT_FOUND_MESSAGE =
        "User not found! Please check user exist or password is right!";
    
    protected NacosUserService userDetailsService;
    
    protected TokenManagerDelegate jwtTokenManager;
    
    protected NacosRoleService roleService;
    
    public AbstractAuthenticationManager(NacosUserService userDetailsService,
        TokenManagerDelegate jwtTokenManager,
        NacosRoleService roleService) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenManager = jwtTokenManager;
        this.roleService = roleService;
    }
    
    /** 用户名密码认证，成功后签发 JWT 并返回 {@link NacosUser}。 */
    @Override
    public NacosUser authenticate(String username, String rawPassword) throws AccessException {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(rawPassword)) {
            throw new AccessException(USER_NOT_FOUND_MESSAGE);
        }
        NacosUserDetails nacosUserDetails =
            (NacosUserDetails) userDetailsService.loadUserByUsername(username);
        if (nacosUserDetails == null
            || !PasswordEncoderUtil.matches(rawPassword, nacosUserDetails.getPassword())) {
            throw new AccessException(USER_NOT_FOUND_MESSAGE);
        }
        return new NacosUser(nacosUserDetails.getUsername(), jwtTokenManager.createToken(username));
    }
    
    /** 解析并校验 JWT，返回对应 {@link NacosUser}。 */
    @Override
    public NacosUser authenticate(String token) throws AccessException {
        if (StringUtils.isBlank(token)) {
            throw new AccessException(USER_NOT_FOUND_MESSAGE);
        }
        return jwtTokenManager.parseToken(token);
    }
    
    /** 从 HTTP 请求提取 token 或表单凭证并完成认证。 */
    @Override
    public NacosUser authenticate(HttpServletRequest httpServletRequest) throws AccessException {
        String token = resolveToken(httpServletRequest);
        
        NacosUser user;
        if (StringUtils.isNotBlank(token)) {
            user = authenticate(token);
        } else {
            String userName = httpServletRequest.getParameter(AuthConstants.PARAM_USERNAME);
            String password = httpServletRequest.getParameter(AuthConstants.PARAM_PASSWORD);
            user = authenticate(userName, password);
        }
        
        return user;
    }
    
    /** 全局管理员或具备 RBAC 权限时放行，否则抛出 {@link AccessException}。 */
    @Override
    public void authorize(Permission permission, NacosUser nacosUser) throws AccessException {
        if (Loggers.AUTH.isDebugEnabled()) {
            Loggers.AUTH.debug("auth permission: {}, nacosUser: {}", permission, nacosUser);
        }
        if (nacosUser.isGlobalAdmin()) {
            return;
        }
        if (hasGlobalAdminRole(nacosUser)) {
            return;
        }
        
        if (!roleService.hasPermission(nacosUser, permission)) {
            throw new AccessException("authorization failed!");
        }
    }
    
    /** 从 Authorization 头或 accessToken 参数解析 Bearer JWT。 */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(bearerToken)
            && bearerToken.startsWith(AuthConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(AuthConstants.TOKEN_PREFIX.length());
        }
        bearerToken = request.getParameter(Constants.ACCESS_TOKEN);
        
        return bearerToken;
    }
    
    /** 指定用户名是否拥有全局管理员角色。 */
    @Override
    public boolean hasGlobalAdminRole(String username) {
        return roleService.hasGlobalAdminRole(username);
    }
    
    /** 系统中是否存在全局管理员角色。 */
    @Override
    public boolean hasGlobalAdminRole() {
        return roleService.hasGlobalAdminRole();
    }
    
    /** 判断用户是否为全局管理员并回写 {@link NacosUser#setGlobalAdmin}。 */
    @Override
    public boolean hasGlobalAdminRole(NacosUser nacosUser) {
        if (nacosUser.isGlobalAdmin()) {
            return true;
        }
        nacosUser.setGlobalAdmin(hasGlobalAdminRole(nacosUser.getUserName()));
        return nacosUser.isGlobalAdmin();
    }
}
