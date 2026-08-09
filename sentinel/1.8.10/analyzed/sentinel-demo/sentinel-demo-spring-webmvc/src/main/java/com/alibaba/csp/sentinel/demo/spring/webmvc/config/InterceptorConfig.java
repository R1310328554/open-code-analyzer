/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelExceptionAware;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcTotalConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 Sentinel WebMvc 拦截器：URL 级限流、来源解析与 Web 上下文配置。
 *
 * @author kaizi2009
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sentinel 拦截器
        addSpringMvcInterceptor(registry);
    }

    @Bean
    public SentinelExceptionAware sentinelExceptionAware() {
        // 配置 ExceptionHandler 后，使异常对 Sentinel 可见
        return new SentinelExceptionAware();
    }

    private void addSpringMvcInterceptor(InterceptorRegistry registry) {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();

        // 可按需通过 BlockExceptionHandler 处理，或直接抛出由全局异常处理器捕获

        // config.setBlockExceptionHandler((request, response, e) -> { throw e; });

        // 使用默认 BlockExceptionHandler
        config.setBlockExceptionHandler(new DefaultBlockExceptionHandler());

        // 按需自定义配置
        config.setHttpMethodSpecify(true);
        // webContextUnify=true 时统一 Web 上下文（默认），可节省内存；
        // 设为 false 则按 URL 分离入口上下文，便于链路流控，可在 Dashboard「资源链路」查看效果
        config.setWebContextUnify(true);
        config.setOriginParser(request -> request.getHeader("S-user"));

        // 注册 SentinelWebInterceptor
        registry.addInterceptor(new SentinelWebInterceptor(config)).addPathPatterns("/**");
    }

    /** 注册全站 URL 总量统计拦截器（演示用，当前未调用）。 */
    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        // 全站统计配置
        SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();

        // 按需自定义属性名与总资源名
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container");
        config.setTotalResourceName("my-spring-mvc-total-url-request");

        // 注册 SentinelWebTotalInterceptor
        registry.addInterceptor(new SentinelWebTotalInterceptor(config)).addPathPatterns("/**");
    }
}
