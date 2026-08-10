/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.web;

import com.alibaba.nacos.core.code.ControllerMethodsCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.annotation.PostConstruct;

/**
 * Core Web 模块 Spring 配置：注册表单大小过滤器并预热 Controller 方法缓存。
 * Nacos core web configuration.
 *
 * @author xiweng.yy
 * @author Huang Xiao
 */
@Configuration
@NacosWebBean
public class NacosCoreWebConfiguration {
    
    private final ControllerMethodsCache methodsCache;
    
    public NacosCoreWebConfiguration(ControllerMethodsCache methodsCache) {
        this.methodsCache = methodsCache;
    }
    
    /** 启动时扫描 core.controller 包填充 {@link ControllerMethodsCache}。 */
    @PostConstruct
    public void init() {
        methodsCache.initClassMethod("com.alibaba.nacos.core.controller");
    }
    
    /**
     * 注册 FormSizeFilter，order=5 需高于 AuthFilter 以确保校验生效。
     * auth admin filter registration.
     *
     * @param formSizeFilter form size filter
     * @return filter registration
     * @see com.alibaba.nacos.core.auth.AbstractWebAuthFilter
     */
    @Bean
    public FilterRegistrationBean<FormSizeFilter> formSizeFilterRegistration(
        FormSizeFilter formSizeFilter) {
        FilterRegistrationBean<FormSizeFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(formSizeFilter);
        registration.addUrlPatterns("/*");
        registration.setName("formSizeFilter");
        // 优先级须高于 AuthFilter，否则表单大小限制可能在鉴权前未生效
        registration.setOrder(5);
        return registration;
    }
    
    /**
     * 创建 FormSizeFilter Bean，默认上限与 Tomcat max-http-form-post-size 一致（2MB）。
     * form size filter.
     *
     * @param maxFormSize max form size (default 2MB, same as Tomcat's default)
     * @return filter
     */
    @Bean
    public FormSizeFilter formSizeFilter(
        @Value("${server.tomcat.max-http-form-post-size:2MB}") DataSize maxFormSize) {
        return new FormSizeFilter(maxFormSize.toBytes());
    }
}
