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

package com.alibaba.nacos.console.config;

import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.core.exception.NacosApiExceptionHandler;
import com.alibaba.nacos.console.filter.NacosConsoleAuthFilter;
import com.alibaba.nacos.console.filter.XssFilter;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.core.paramcheck.ParamCheckerFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.PostConstruct;
import java.time.ZoneId;

/**
 * 控制台 Web 层 Spring 配置：CORS、XSS/鉴权/参数校验过滤器、Jackson 时区与 Spring Security 链。
 * Console config.
 *
 * @author yshen
 * @author nkorange
 * @since 1.2.0
 */
@Configuration
public class ConsoleWebConfig {
    
    /** Controller 方法元数据缓存，供过滤器与启动初始化共用 */
    private final ControllerMethodsCache methodsCache;
    
    /** 注入方法缓存 Bean */
    public ConsoleWebConfig(ControllerMethodsCache methodsCache) {
        this.methodsCache = methodsCache;
    }
    
    /** 启动后扫描 {@code com.alibaba.nacos.console.controller} 包并缓存接口元数据 */

    @PostConstruct
    public void init() {
        methodsCache.initClassMethod("com.alibaba.nacos.console.controller");
    }
    
    /** 根据 {@link ConsoleCorsConfig} 注册全局 CORS 过滤器 */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        ConsoleCorsConfig corsConfig = new ConsoleCorsConfig();
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        if (corsConfig.getAllowedHeaders().isEmpty()) {
            config.addAllowedHeader("*");
        } else {
            config.setAllowedHeaders(corsConfig.getAllowedHeaders());
        }
        config.setMaxAge(corsConfig.getMaxAge());
        if (corsConfig.getAllowedMethods().isEmpty()) {
            config.addAllowedMethod("*");
        } else {
            config.setAllowedMethods(corsConfig.getAllowedMethods());
        }
        if (corsConfig.getAllowedOrigins().isEmpty()) {
            config.addAllowedOriginPattern("*");
        } else {
            config.setAllowedOrigins(corsConfig.getAllowedOrigins());
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    
    /** 注册 XSS 请求过滤 Bean */
    @Bean
    public XssFilter xssFilter() {
        return new XssFilter();
    }
    
    /** 将控制台鉴权过滤器注册到 {@code /*}，顺序 6 */
    @Bean
    public FilterRegistrationBean<NacosConsoleAuthFilter> authFilterRegistration(
        NacosConsoleAuthFilter authFilter) {
        FilterRegistrationBean<NacosConsoleAuthFilter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/*");
        registration.setName("consoleAuthFilter");
        registration.setOrder(6);
        return registration;
    }
    
    /** 创建绑定控制台鉴权 scope 的 {@link NacosConsoleAuthFilter} */
    @Bean
    public NacosConsoleAuthFilter consoleAuthFilter(ControllerMethodsCache methodsCache) {
        return new NacosConsoleAuthFilter(NacosAuthConfigHolder.getInstance()
            .getNacosAuthConfigByScope(NacosConsoleAuthConfig.NACOS_CONSOLE_AUTH_SCOPE),
            methodsCache);
    }
    
    /** 注册参数校验过滤器，顺序 8 */
    @Bean
    public FilterRegistrationBean<ParamCheckerFilter> consoleParamCheckerFilterRegistration(
        ParamCheckerFilter consoleParamCheckerFilter) {
        FilterRegistrationBean<ParamCheckerFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(consoleParamCheckerFilter);
        registration.addUrlPatterns("/*");
        registration.setName("consoleParamCheckerFilter");
        registration.setOrder(8);
        return registration;
    }
    
    /** 创建基于方法缓存的参数校验过滤器 */
    @Bean
    public ParamCheckerFilter consoleParamCheckerFilter(ControllerMethodsCache methodsCache) {
        return new ParamCheckerFilter(methodsCache);
    }
    
    /** 将 Jackson 默认时区设为 JVM 系统默认时区 */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
            .timeZone(ZoneId.systemDefault().toString());
    }
    
    /** 配置 Spring Security：放行全部路径并禁用 CSRF（鉴权由 Nacos 过滤器负责） */
    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
            (authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/**").permitAll());
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
    
    /** 注册统一 API 异常处理器 Bean */
    @Bean
    public NacosApiExceptionHandler nacosApiExceptionHandler() {
        return new NacosApiExceptionHandler();
    }
}
