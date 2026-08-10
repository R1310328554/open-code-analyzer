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

package com.alibaba.nacos.common.http;

import com.alibaba.nacos.common.http.client.NacosAsyncRestTemplate;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;

/**
 * http Client Factory.
 * <p>HTTP 客户端工厂 SPI：由模块实现以定制 {@link HttpClientConfig}，并创建同步 {@link NacosRestTemplate} 与异步 {@link NacosAsyncRestTemplate}。</p>
 *
 * @author mai.jh
 */
public interface HttpClientFactory {
    
    /**
     * create new nacost rest.
     * <p>创建新的同步 {@link NacosRestTemplate} 实例。</p>
     *
     * @return NacosRestTemplate
     */
    NacosRestTemplate createNacosRestTemplate();
    
    /**
     * create new nacos async rest.
     * <p>创建新的异步 {@link NacosAsyncRestTemplate} 实例。</p>
     *
     * @return NacosAsyncRestTemplate
     */
    NacosAsyncRestTemplate createNacosAsyncRestTemplate();
    
}
