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

package com.alibaba.nacos.core.context.remote;

import com.alibaba.nacos.core.web.NacosWebBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP 请求上下文的 Spring 配置：注册 {@link HttpRequestContextFilter} 并设为最高优先级，确保后续过滤器可读取已填充的 {@link RequestContext}。
 * Spring Configuration for request context of HTTP.
 *
 * @author xiweng.yy
 */
@Configuration
@NacosWebBean
public class HttpRequestContextConfig {
    
    /** 注册全局 HTTP 请求上下文过滤器，顺序为 {@link Integer#MIN_VALUE}。 */
    @Bean
    public FilterRegistrationBean<HttpRequestContextFilter> requestContextFilterRegistration(
        HttpRequestContextFilter requestContextFilter) {
        FilterRegistrationBean<HttpRequestContextFilter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter(requestContextFilter);
        registration.addUrlPatterns("/*");
        registration.setName("nacosRequestContextFilter");
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }
    
    /** 提供 {@link HttpRequestContextFilter} Bean 实例。 */
    @Bean
    public HttpRequestContextFilter nacosRequestContextFilter() {
        return new HttpRequestContextFilter();
    }
}
