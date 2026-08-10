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

package com.alibaba.nacos.plugin.auth.impl.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.model.RestResultUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.core.controller.compatibility.Compatibility;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.authenticate.IAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthSystemTypes;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * V1 鉴权用户 API：仅保留登录接口。
 *
 * <p>其余 v1 用户/角色/权限接口已迁移至 nacos-api-legacy-adapter， 在新版默认鉴权插件存在时由适配层加载。</p>
 *
 * @author wfnuser
 * @author nkorange
 */
@RestController
@RequestMapping({"/v1/auth", "/v1/auth/users"})
public class UserController {
    
    private final TokenManagerDelegate jwtTokenManager;
    
    private final AuthConfigs authConfigs;
    
    private final IAuthenticationManager iAuthenticationManager;
    
    /** 已废弃的 Spring Security 认证管理器（非 Nacos/LDAP 类型时使用）。 */
    @Deprecated
    private final AuthenticationManager authenticationManager;
    
    /** 注入 JWT 令牌管理、鉴权配置与认证管理器。 */
    public UserController(TokenManagerDelegate jwtTokenManager, AuthConfigs authConfigs,
        IAuthenticationManager iAuthenticationManager,
        AuthenticationManager authenticationManager) {
        this.jwtTokenManager = jwtTokenManager;
        this.authConfigs = authConfigs;
        this.iAuthenticationManager = iAuthenticationManager;
        this.authenticationManager = authenticationManager;
    }
    
    /**
     * Nacos 登录（v1 API，兼容旧客户端）。
     *
     * @param username username of user
     * @param password password
     * @param response http response
     * @param request  http request
     * @return 用户 JWT 及 TTL、是否全局管理员等信息
     * @throws AccessException if user info is incorrect
     */
    @Since("2.3.0")
    @PostMapping("/login")
    @Compatibility(apiType = ApiType.OPEN_API,
        alternatives = "POST ${contextPath:nacos}/v3/auth/user/login")
    public Object login(@RequestParam String username, @RequestParam String password,
        HttpServletResponse response,
        HttpServletRequest request) throws AccessException, IOException {
        
        // Nacos 内置或 LDAP 鉴权：走 IAuthenticationManager 统一认证
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
        
        // 其他鉴权类型：回退 Spring Security 用户名密码认证
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(username,
                password);
        
        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenManager.createToken(authentication);
            response.addHeader(AuthConstants.AUTHORIZATION_HEADER, "Bearer " + token);
            return RestResultUtils.success("Bearer " + token);
        } catch (BadCredentialsException authentication) {
            return RestResultUtils.failed(HttpStatus.UNAUTHORIZED.value(), null, "Login failed");
        }
    }
}
