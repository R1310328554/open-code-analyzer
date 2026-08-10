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

package com.alibaba.nacos.naming.web;

import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.core.web.NacosWebBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 命名模块 Web 层 Spring 配置。
 *
 * <p>注册 Distro、服务名校验、流量修订与客户端属性等 Servlet Filter，并预热 {@link ControllerMethodsCache}。</p>
 *
 * @author nkorange
 */
@Configuration
@NacosWebBean
public class NamingConfig {
    
    private static final String URL_PATTERNS = "/v1/ns/*";
    
    private static final String URL_PATTERNS_V2 = "/v2/ns/*";
    
    private static final String URL_PATTERNS_V3_CLIENT = "/v3/client/ns/*";
    private static final String URL_PATTERNS_V3_ADMIN = "/v3/admin/ns/*";
    private static final String DISTRO_FILTER = "distroFilter";
    
    private static final String SERVICE_NAME_FILTER = "serviceNameFilter";
    
    private static final String TRAFFIC_REVISE_FILTER = "trafficReviseFilter";
    
    private static final String CLIENT_ATTRIBUTES_FILTER = "clientAttributes_filter";
    
    private final ControllerMethodsCache methodsCache;
    
    public NamingConfig(ControllerMethodsCache methodsCache) {
        this.methodsCache = methodsCache;
    }
    
    /** 启动时扫描 naming controllers 包，缓存方法元数据供 DistroFilter 使用。 */
    @PostConstruct
    public void init() {
        methodsCache.initClassMethod("com.alibaba.nacos.naming.controllers");
    }
    
    /** 注册 Distro 过滤器，匹配 v1/v3 命名 API，顺序 7。 */
    @Bean
    public FilterRegistrationBean<DistroFilter> distroFilterRegistration() {
        FilterRegistrationBean<DistroFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(distroFilter());
        registration.addUrlPatterns(URL_PATTERNS, URL_PATTERNS_V3_CLIENT, URL_PATTERNS_V3_ADMIN);
        registration.setName(DISTRO_FILTER);
        registration.setOrder(7);
        return registration;
    }
    
    /** 注册服务名兼容过滤器，顺序 5。 */
    @Bean
    public FilterRegistrationBean<ServiceNameFilter> serviceNameFilterRegistration() {
        FilterRegistrationBean<ServiceNameFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(serviceNameFilter());
        registration.addUrlPatterns(URL_PATTERNS);
        registration.setName(SERVICE_NAME_FILTER);
        registration.setOrder(5);
        return registration;
    }
    
    /** 注册流量修订过滤器（限流与读写状态），顺序 1。 */
    @Bean
    public FilterRegistrationBean<TrafficReviseFilter> trafficReviseFilterRegistration() {
        FilterRegistrationBean<TrafficReviseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(trafficReviseFilter());
        registration.addUrlPatterns(URL_PATTERNS);
        registration.setName(TRAFFIC_REVISE_FILTER);
        registration.setOrder(1);
        return registration;
    }
    
    /** 注册客户端属性采集过滤器，匹配 v1/v2，顺序 8。 */
    @Bean
    public FilterRegistrationBean<ClientAttributesFilter> clientAttributesFilterRegistration() {
        FilterRegistrationBean<ClientAttributesFilter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter(clientAttributesFilter());
        registration.addUrlPatterns(URL_PATTERNS, URL_PATTERNS_V2);
        registration.setName(CLIENT_ATTRIBUTES_FILTER);
        registration.setOrder(8);
        return registration;
    }
    
    @Bean
    public DistroFilter distroFilter() {
        return new DistroFilter();
    }
    
    @Bean
    public TrafficReviseFilter trafficReviseFilter() {
        return new TrafficReviseFilter();
    }
    
    @Bean
    public ServiceNameFilter serviceNameFilter() {
        return new ServiceNameFilter();
    }
    
    @Bean
    public ClientAttributesFilter clientAttributesFilter() {
        return new ClientAttributesFilter();
    }
}
