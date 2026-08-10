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

package com.alibaba.nacos.core.control.http;

import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.core.web.NacosWebBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP TPS 限流切点 Filter 的 Spring 注册配置：将 {@link NacosHttpTpsFilter} 挂载到命名与配置服务的 v1/v2 路径，并注入 {@link ControllerMethodsCache} 供请求解析。
 * Nacos http tps control cut point filter registration.
 *
 * @author xiweng.yy
 */
@Configuration
@NacosWebBean
public class NacosHttpTpsControlRegistration {
    
    /**
     * 注册 TPS Filter 到 Servlet 容器，覆盖命名与配置 HTTP 路径。
     *
     * @param tpsFilter 已构造的 TPS Filter 实例
     * @return Filter 注册 Bean
     */
    @Bean
    public FilterRegistrationBean<NacosHttpTpsFilter> tpsFilterRegistration(
        NacosHttpTpsFilter tpsFilter) {
        FilterRegistrationBean<NacosHttpTpsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(tpsFilter);
        // 命名服务 HTTP 路径
        registration.addUrlPatterns("/v1/ns/*", "/v2/ns/*");
        // 配置服务 HTTP 路径
        registration.addUrlPatterns("/v1/cs/*", "/v2/cs/*");
        registration.setName("tpsFilter");
        registration.setOrder(6);
        return registration;
    }
    
    /**
     * 创建 TPS Filter Bean，依赖 Controller 方法缓存解析目标方法。
     *
     * @param methodsCache Controller 方法索引缓存
     * @return TPS Filter 实例
     */
    @Bean
    public NacosHttpTpsFilter tpsFilter(ControllerMethodsCache methodsCache) {
        return new NacosHttpTpsFilter(methodsCache);
    }
}
