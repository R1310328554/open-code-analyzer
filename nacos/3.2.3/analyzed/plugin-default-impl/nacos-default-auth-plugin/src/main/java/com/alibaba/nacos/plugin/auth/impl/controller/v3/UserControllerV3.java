/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.plugin.auth.impl.controller.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.context.RequestContextHolder;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.authenticate.IAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthSystemTypes;
import com.alibaba.nacos.plugin.auth.impl.persistence.RoleInfo;
import com.alibaba.nacos.plugin.auth.impl.persistence.User;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import com.alibaba.nacos.plugin.auth.impl.utils.PasswordGeneratorUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * V3 用户管理 REST 控制器。
 *
 * <p>提供用户 CRUD、管理员初始化、登录及模糊搜索，路径前缀为 {@link AuthConstants#USER_PATH}。</p>
 *
 * @author zhangyukun on:2024/8/16
 */
@RestController
@RequestMapping(AuthConstants.USER_PATH)
public class UserControllerV3 {
    
    private final NacosUserService userDetailsService;
    
    private final NacosRoleService roleService;
    
    private final AuthConfigs authConfigs;
    
    private final IAuthenticationManager iAuthenticationManager;
    
    private final TokenManagerDelegate jwtTokenManager;
    
    /** 模糊搜索模式标识。 */
    private static final String SEARCH_TYPE_BLUR = "blur";
    
    /** 注入用户服务、角色服务、鉴权配置、认证管理器与 JWT 令牌管理器。 */

    public UserControllerV3(NacosUserService userDetailsService, NacosRoleService roleService,
        AuthConfigs authConfigs,
        IAuthenticationManager iAuthenticationManager, TokenManagerDelegate jwtTokenManager) {
        this.userDetailsService = userDetailsService;
        this.roleService = roleService;
        this.authConfigs = authConfigs;
        this.iAuthenticationManager = iAuthenticationManager;
        this.jwtTokenManager = jwtTokenManager;
    }
    
    /**
     * 创建新用户。
     *
     * @param username username
     * @param password password
     * @return ok if create succeed
     * @throws IllegalArgumentException if user already exist
     * @since 1.2.0
     */
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "users",
        action = ActionTypes.WRITE)
    @Since("3.0.0")
    @PostMapping
    public Result<String> createUser(@RequestParam String username, @RequestParam String password) {
        User user = userDetailsService.getUser(username);
        if (user != null) {
            throw new IllegalArgumentException("user '" + username + "' already exist!");
        }
        userDetailsService.createUser(username, password);
        return Result.success("create user ok!");
    }
    
    /** 初始化全局管理员（仅当系统中尚不存在管理员时可用）。 */

    @Since("3.0.0")
    @PostMapping("/admin")
    public Result<User> createAdminUser(@RequestParam(required = false) String password) {
        
        if (StringUtils.isBlank(password)) {
            password = PasswordGeneratorUtil.generateRandomPassword();
        }
        
        if (AuthSystemTypes.NACOS.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
            if (iAuthenticationManager.hasGlobalAdminRole()) {
                return Result.failure(HttpStatus.CONFLICT.value(), "have admin user cannot use it.",
                    null);
            }
            String username = AuthConstants.DEFAULT_USER;
            userDetailsService.createUser(username, password);
            roleService.addAdminRole(username);
            User result = new User();
            result.setUsername(username);
            result.setPassword(password);
            return Result.success(result);
        } else {
            return Result.failure(HttpStatus.NOT_IMPLEMENTED.value(),
                "Current auth type not supported create admin user.", null);
        }
    }
    
    /**
     * 删除用户；禁止删除全局管理员账号。
     *
     * @param username username of user
     * @return ok if deleted succeed, keep silent if user not exist
     * @since 1.2.0
     */
    @Since("3.0.0")
    @DeleteMapping
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "users",
        action = ActionTypes.WRITE)
    public Result<String> deleteUser(@RequestParam String username) {
        List<RoleInfo> roleInfoList = roleService.getRoles(username);
        if (roleInfoList != null) {
            for (RoleInfo roleInfo : roleInfoList) {
                if (AuthConstants.GLOBAL_ADMIN_ROLE.equals(roleInfo.getRole())) {
                    throw new IllegalArgumentException("cannot delete admin: " + username);
                }
            }
        }
        userDetailsService.deleteUser(username);
        return Result.success("delete user ok!");
    }
    
    /**
     * 修改用户密码（需本人或全局管理员权限）。
     *
     * @param username    username of user
     * @param newPassword new password of user
     * @param response    http response
     * @param request     http request
     * @return ok if update succeed
     * @throws IllegalArgumentException if user not exist or oldPassword is incorrect
     * @since 1.2.0
     */
    @Since("3.0.0")
    @PutMapping
    @Secured(resource = AuthConstants.UPDATE_PASSWORD_ENTRY_POINT, action = ActionTypes.WRITE,
        tags = {
            com.alibaba.nacos.plugin.auth.constant.Constants.Tag.ONLY_IDENTITY,
            AuthConstants.UPDATE_PASSWORD_ENTRY_POINT})
    public Result<String> updateUser(@RequestParam String username,
        @RequestParam String newPassword,
        HttpServletResponse response, HttpServletRequest request) throws IOException {
        try {
            if (!hasPermission(username, request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "authorization failed!");
                return null;
            }
        } catch (HttpSessionRequiredException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "session expired!");
            return null;
        } catch (AccessException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "authorization failed!");
            return null;
        }
        
        User user = userDetailsService.getUser(username);
        if (user == null) {
            throw new IllegalArgumentException("user " + username + " not exist!");
        }
        
        userDetailsService.updateUserPassword(username, newPassword);
        return Result.success("update user ok!");
        
    }
    
    /** 校验当前请求是否有权修改指定用户密码。 */
    private boolean hasPermission(String username, HttpServletRequest request)
        throws HttpSessionRequiredException, AccessException {
        if (!NacosAuthConfigHolder.getInstance().isAnyAuthEnabled()) {
            return true;
        }
        // 修复 #13959：Server 身份请求跳过权限校验
        if (isFromServerIdentity(request)) {
            return true;
        }
        IdentityContext identityContext =
            RequestContextHolder.getContext().getAuthContext().getIdentityContext();
        if (identityContext == null) {
            throw new HttpSessionRequiredException("session expired!");
        }
        NacosUser user = (NacosUser) identityContext.getParameter(AuthConstants.NACOS_USER_KEY);
        if (user == null) {
            user = iAuthenticationManager.authenticate(request);
            if (user == null) {
                throw new HttpSessionRequiredException("session expired!");
            }
        }
        // 从 JWT 解析用户后需刷新全局管理员标记
        iAuthenticationManager.hasGlobalAdminRole(user);
        // 全局管理员可修改任意用户密码
        if (user.isGlobalAdmin()) {
            return true;
        }
        // 普通用户仅可修改自己的密码
        return user.getUserName().equals(username);
    }
    
    /**
     * 分页查询用户列表，支持精确或模糊搜索。
     *
     * @param pageNo   number index of page
     * @param pageSize size of page
     * @param username the username to search for, can be an empty string
     * @param search   the type of search: "accurate" for exact match, "blur" for fuzzy match
     * @return A collection of users, empty set if no user is found
     * @since 1.2.0
     */
    @Since("3.0.0")
    @GetMapping("/list")
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "users",
        action = ActionTypes.READ)
    public Result<Page<User>> getUserList(@RequestParam int pageNo, @RequestParam int pageSize,
        @RequestParam(name = "username", required = false, defaultValue = "") String username,
        @RequestParam(name = "search", required = false, defaultValue = "accurate") String search) {
        Page<User> userPage;
        if (SEARCH_TYPE_BLUR.equalsIgnoreCase(search)) {
            userPage = userDetailsService.findUsers(username, pageNo, pageSize);
        } else {
            userPage = userDetailsService.getUsers(pageNo, pageSize, username);
        }
        return Result.success(userPage);
    }
    
    /**
     * 按用户名模糊匹配，返回匹配的用户名列表。
     *
     * @param username username
     * @return Matched username
     */
    @Since("3.0.0")
    @GetMapping("/search")
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "users",
        action = ActionTypes.WRITE)
    public Result<List<String>> getUserListByUsername(@RequestParam String username) {
        List<String> userList = userDetailsService.findUserNames(username);
        return Result.success(userList);
    }
    
    /**
     * V3 登录接口：用户名密码换取 JWT。
     *
     * <p>仅支持 Nacos 内置或 LDAP 鉴权类型。</p>
     *
     * @param response http response
     * @param request  http request
     * @return new token of the user
     * @throws AccessException if user info is incorrect
     */
    @Since("3.0.0")
    @PostMapping("/login")
    public Object login(HttpServletResponse response, HttpServletRequest request)
        throws AccessException, IOException {
        if (AuthSystemTypes.NACOS.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())
            || AuthSystemTypes.LDAP.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
            
            NacosUser user = iAuthenticationManager.authenticate(request);
            
            response.addHeader(AuthConstants.AUTHORIZATION_HEADER,
                AuthConstants.TOKEN_PREFIX + user.getToken());
            
            ObjectNode result = JacksonUtils.createEmptyJsonNode();
            result.put(Constants.ACCESS_TOKEN, user.getToken());
            result.put(Constants.TOKEN_TTL, jwtTokenManager.getTokenTtlInSeconds(user.getToken()));
            result.put(Constants.GLOBAL_ADMIN, iAuthenticationManager.hasGlobalAdminRole(user));
            result.put(Constants.USERNAME, user.getUserName());
            return result;
        }
        return Result.failure(ErrorCode.ILLEGAL_STATE.getCode(),
            "Current Nacos auth plugin type is not `nacos` or `nacos-ldap`, don't support login API.",
            null);
    }
    
    /** 判断请求是否携带合法的 Server 集群身份头。 */
    private boolean isFromServerIdentity(HttpServletRequest request) {
        String serverIdentityKey = authConfigs.getServerIdentityKey();
        String serverIdentityValue = request.getHeader(serverIdentityKey);
        return authConfigs.getServerIdentityValue().equals(serverIdentityValue);
    }
}
