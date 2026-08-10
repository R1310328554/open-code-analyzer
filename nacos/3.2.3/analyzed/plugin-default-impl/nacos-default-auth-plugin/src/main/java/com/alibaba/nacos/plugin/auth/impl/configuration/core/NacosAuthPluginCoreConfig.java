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

package com.alibaba.nacos.plugin.auth.impl.configuration.core;

import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnNacosAuth;
import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.core.auth.NacosServerAuthConfig;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.plugin.auth.impl.authenticate.DefaultAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.authenticate.IAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnInnerDatasource;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthSystemTypes;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManager;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.token.impl.CachedJwtTokenManager;
import com.alibaba.nacos.plugin.auth.impl.token.impl.JwtTokenManager;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

/**
 * 默认鉴权插件 Spring Security 核心配置。
 *
 * <p>注册密码编码器、JWT Token 管理器、 {@link DefaultAuthenticationManager} 及内嵌数据源下的全局认证配置器； 启动时扫描鉴权 Controller 方法缓存。</p>
 *
 * @author Nacos
 */
public class NacosAuthPluginCoreConfig {
    
    /** Nacos 用户详情服务，供 Spring Security 认证使用。 */
    private final NacosUserService userDetailsService;
    
    /** Controller 方法缓存，用于鉴权注解解析。 */
    private final ControllerMethodsCache methodsCache;
    
    /** 注入用户服务与 Controller 方法缓存。 */
    public NacosAuthPluginCoreConfig(NacosUserService userDetailsService,
        ControllerMethodsCache methodsCache) {
        this.userDetailsService = userDetailsService;
        this.methodsCache = methodsCache;
    }
    
    /** 初始化鉴权 Controller 包的方法缓存索引。 */
    @PostConstruct
    public void init() {
        methodsCache.initClassMethod("com.alibaba.nacos.plugin.auth.impl.controller");
    }
    
    /** 内嵌 Nacos 鉴权体系下配置 UserDetailsService 与 BCrypt 密码编码器。 */
    @Bean
    @ConditionalOnMissingBean
    @Conditional(value = {ConditionOnInnerDatasource.class, ConditionOnNacosAuth.class})
    public GlobalAuthenticationConfigurerAdapter authenticationConfigurer() {
        return new GlobalAuthenticationConfigurerAdapter() {
            
            @Override
            public void init(AuthenticationManagerBuilder auth) throws Exception {
                if (AuthSystemTypes.NACOS.name()
                    .equalsIgnoreCase(NacosAuthConfigHolder.getInstance()
                        .getNacosAuthConfigByScope(NacosServerAuthConfig.NACOS_SERVER_AUTH_SCOPE)
                        .getNacosAuthSystemType())) {
                    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
                }
            }
        };
    }
    
    /** 注册 BCrypt 密码编码器 Bean。 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /** 创建默认鉴权管理器，协调用户、Token 与角色服务。 */
    @Bean
    @ConditionalOnMissingBean
    @Conditional(value = ConditionOnNacosAuth.class)
    public IAuthenticationManager defaultAuthenticationManager(NacosUserService userDetailsService,
        TokenManagerDelegate jwtTokenManager, NacosRoleService roleService) {
        return new DefaultAuthenticationManager(userDetailsService, jwtTokenManager, roleService);
    }
    
    /** Token 缓存关闭时使用标准 JWT Token 管理器。 */
    @Bean
    @ConditionalOnProperty(value = TokenManagerDelegate.NACOS_AUTH_TOKEN_CACHING_ENABLED,
        havingValue = "false", matchIfMissing = true)
    public TokenManager tokenManager(AuthConfigs authConfigs) {
        return new JwtTokenManager(authConfigs);
    }
    
    /** Token 缓存开启时使用带本地缓存的 JWT 管理器。 */
    @Bean
    @ConditionalOnProperty(value = TokenManagerDelegate.NACOS_AUTH_TOKEN_CACHING_ENABLED,
        havingValue = "true")
    public TokenManager cachedTokenManager(AuthConfigs authConfigs) {
        return new CachedJwtTokenManager(new JwtTokenManager(authConfigs));
    }
    
    /** 将具体 TokenManager 包装为统一委托 Bean。 */
    @Bean
    public TokenManagerDelegate tokenManagerDelegate(TokenManager tokenManager) {
        return new TokenManagerDelegate(tokenManager);
    }
}
