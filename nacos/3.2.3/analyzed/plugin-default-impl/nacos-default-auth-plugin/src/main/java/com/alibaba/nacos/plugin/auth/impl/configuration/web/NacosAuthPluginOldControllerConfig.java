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
import com.alibaba.nacos.plugin.auth.impl.controller.UserController;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;

/**
 * 鉴权插件 V1 登录 Controller 配置（兼容遗留 API）。
 *
 * <p>仅注册 V1 登录接口；其余 V1 用户/角色/权限 API 由  {@code nacos-api-legacy-adapter} 模块提供。</p>
 *
 * @author xiweng.yy
 * @deprecated after v1 api not supported
 */
@Deprecated
public class NacosAuthPluginOldControllerConfig {
    
    /** 注册 V1 用户登录 Controller Bean。 */
    @Bean
    public UserController userController(AuthConfigs authConfigs,
        IAuthenticationManager iAuthenticationManager,
        TokenManagerDelegate jwtTokenManager, AuthenticationManager authenticationManager) {
        return new UserController(jwtTokenManager, authConfigs, iAuthenticationManager,
            authenticationManager);
    }
}
