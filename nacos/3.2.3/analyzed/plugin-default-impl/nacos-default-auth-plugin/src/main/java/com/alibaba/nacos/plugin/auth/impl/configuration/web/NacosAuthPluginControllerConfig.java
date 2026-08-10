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

package com.alibaba.nacos.plugin.auth.impl.configuration.web;

import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.plugin.auth.impl.authenticate.IAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.controller.v3.PermissionControllerV3;
import com.alibaba.nacos.plugin.auth.impl.controller.v3.RoleControllerV3;
import com.alibaba.nacos.plugin.auth.impl.controller.v3.UserControllerV3;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import org.springframework.context.annotation.Bean;

/**
 * 鉴权插件 V3 REST Controller Bean 配置。
 *
 * <p>注册用户、角色、权限三类 {@code /v3/auth/*} 控制器， 供远程数据源部署模式下 Console 对外提供鉴权管理 API。</p>
 *
 * @author xiweng.yy
 */
public class NacosAuthPluginControllerConfig {
    
    /** 注册 V3 用户管理 Controller（登录、CRUD 等）。 */
    @Bean
    public UserControllerV3 userControllerV3(NacosUserService userDetailsService,
        NacosRoleService roleService,
        AuthConfigs authConfigs, IAuthenticationManager iAuthenticationManager,
        TokenManagerDelegate jwtTokenManager) {
        return new UserControllerV3(userDetailsService, roleService, authConfigs,
            iAuthenticationManager,
            jwtTokenManager);
    }
    
    /** 注册 V3 角色管理 Controller。 */
    @Bean
    public RoleControllerV3 roleControllerV3(NacosRoleService roleService) {
        return new RoleControllerV3(roleService);
    }
    
    /** 注册 V3 权限管理 Controller。 */
    @Bean
    public PermissionControllerV3 permissionControllerV3(NacosRoleService roleService) {
        return new PermissionControllerV3(roleService);
    }
}
