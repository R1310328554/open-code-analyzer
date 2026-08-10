/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.auth;

import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.core.web.NacosWebBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 鉴权过滤器 Spring 配置：注册开放 API 与管理端 API 两套 Servlet Filter 及对应 Bean。
 * auth filter config.
 *
 * @author mai.jh
 */
@Configuration
@NacosWebBean
public class AuthConfig {
    
    /** 注册开放 API 鉴权 Filter，拦截全部 URL，执行顺序为 6。 */
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration(AuthFilter authFilter) {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/*");
        registration.setName("authFilter");
        registration.setOrder(6);
        return registration;
    }
    
    /** 注册管理端 API 鉴权 Filter，与开放 API Filter 并列、顺序同为 6。 */
    @Bean
    public FilterRegistrationBean<AuthAdminFilter> authAdminFilterRegistration(
        AuthAdminFilter authAdminFilter) {
        FilterRegistrationBean<AuthAdminFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authAdminFilter);
        registration.addUrlPatterns("/*");
        registration.setName("authAdminFilter");
        registration.setOrder(6);
        return registration;
    }
    
    /** 构造开放 API {@link AuthFilter}，绑定 OPEN_API 作用域配置与内部 API 升级探测。 */
    @Bean
    public AuthFilter authFilter(ControllerMethodsCache methodsCache,
        InnerApiAuthEnabled innerApiAuthEnabled) {
        return new AuthFilter(NacosAuthConfigHolder.getInstance()
            .getNacosAuthConfigByScope(NacosServerAuthConfig.NACOS_SERVER_AUTH_SCOPE), methodsCache,
            innerApiAuthEnabled);
    }
    
    /** 构造管理端 {@link AuthAdminFilter}，绑定 ADMIN_API 作用域鉴权配置。 */
    @Bean
    public AuthAdminFilter authAdminFilter(ControllerMethodsCache methodsCache) {
        return new AuthAdminFilter(NacosAuthConfigHolder.getInstance()
            .getNacosAuthConfigByScope(NacosServerAdminAuthConfig.NACOS_SERVER_ADMIN_AUTH_SCOPE),
            methodsCache);
    }
}
