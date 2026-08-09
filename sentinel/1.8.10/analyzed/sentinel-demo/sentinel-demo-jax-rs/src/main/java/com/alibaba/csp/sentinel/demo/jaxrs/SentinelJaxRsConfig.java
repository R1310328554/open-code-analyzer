/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.jaxrs;

import com.alibaba.csp.sentinel.adapter.jaxrs.SentinelJaxRsProviderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 {@link SentinelJaxRsProviderFilter}，对入站 JAX-RS 请求自动创建 Sentinel 资源。
 *
 * @author sea
 */
@Configuration(proxyBeanMethods = false)
public class SentinelJaxRsConfig {

    /** 向 Spring 容器注册 Provider 侧 Sentinel 过滤器。 */
    @Bean
    public SentinelJaxRsProviderFilter sentinelJaxRsProviderFilter() {
        return new SentinelJaxRsProviderFilter();
    }
}
