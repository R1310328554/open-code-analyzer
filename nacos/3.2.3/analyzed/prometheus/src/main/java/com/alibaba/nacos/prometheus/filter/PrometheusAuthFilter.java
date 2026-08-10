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

package com.alibaba.nacos.prometheus.filter;

import com.alibaba.nacos.core.web.NacosWebBean;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.prometheus.controller.PrometheusController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static com.alibaba.nacos.prometheus.api.ApiConstants.PROMETHEUS_CONTROLLER_PATH;

/**
 * Prometheus 端点 HTTP Basic 认证过滤器配置。
 *
 * <p>在 {@code nacos.core.auth.enabled=true} 且存在 {@link PrometheusController} 时注册 Basic、匿名、授权与异常转换过滤器链，仅作用于 {@code /prometheus} 路径。</p>
 *
 * @author vividfish
 */
@NacosWebBean
@Configuration
@ConditionalOnProperty(value = Constants.Auth.NACOS_CORE_AUTH_ENABLED, havingValue = "true")
@ConditionalOnBean(PrometheusController.class)
public class PrometheusAuthFilter {
    
    /** 构建基于 UserDetailsService 的 AuthenticationManager。 */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(
            AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.getOrBuild();
    }
    
    /** 注册 Basic 认证过滤器，顺序为 2。 */
    @Bean
    public FilterRegistrationBean<BasicAuthenticationFilter> basicAuthenticationFilter(
        AuthenticationManager authenticationManager) {
        FilterRegistrationBean<BasicAuthenticationFilter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter(new BasicAuthenticationFilter(authenticationManager));
        registration.addUrlPatterns(PROMETHEUS_CONTROLLER_PATH);
        registration.setName("prometheusBasicAuthenticationFilter");
        registration.setOrder(2);
        return registration;
    }
    
    /** 注册匿名认证过滤器，顺序为 3。 */
    @Bean
    public FilterRegistrationBean<AnonymousAuthenticationFilter> anonymousAuthenticationFilter() {
        FilterRegistrationBean<AnonymousAuthenticationFilter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter(new AnonymousAuthenticationFilter("annony"));
        registration.addUrlPatterns(PROMETHEUS_CONTROLLER_PATH);
        registration.setName("prometheusAnonymousAuthenticationFilter");
        registration.setOrder(3);
        return registration;
    }
    
    /** 注册已认证用户授权过滤器，顺序为 4。 */
    @Bean
    public FilterRegistrationBean<AuthorizationFilter> authorizationFilter() {
        FilterRegistrationBean<AuthorizationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthorizationFilter(new AuthenticatedAuthorizationManager<>()));
        registration.addUrlPatterns(PROMETHEUS_CONTROLLER_PATH);
        registration.setName("prometheusAuthorizationFilter");
        registration.setOrder(4);
        return registration;
    }
    
    /** 注册认证/授权异常转 403 的过滤器，顺序为 1。 */
    @Bean
    public FilterRegistrationBean<ExceptionTranslationFilter> exceptionTranslationFilter() {
        FilterRegistrationBean<ExceptionTranslationFilter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter(new ExceptionTranslationFilter(new Http403ForbiddenEntryPoint()));
        registration.addUrlPatterns(PROMETHEUS_CONTROLLER_PATH);
        registration.setName("prometheusExceptionTranslationFilter");
        registration.setOrder(1);
        return registration;
    }
}
