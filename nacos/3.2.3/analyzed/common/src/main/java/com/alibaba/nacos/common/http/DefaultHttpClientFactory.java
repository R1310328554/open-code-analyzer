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

import org.slf4j.Logger;

/**
 * default http client factory.
 * <p>默认 HTTP 客户端工厂：从系统属性 {@code nacos.http.timeout} 读取超时（默认 5000ms），连接超时为全值、读超时为一半，供 {@link HttpClientBeanHolder} 创建共享 RestTemplate。</p>
 *
 * @author mai.jh
 */
public class DefaultHttpClientFactory extends AbstractHttpClientFactory {
    
    /** HTTP 超时毫秒数，可通过 JVM 属性 {@code nacos.http.timeout} 覆盖 */
    private static final int TIMEOUT = Integer.getInteger("nacos.http.timeout", 5000);
    
    /** 创建 RestTemplate 时使用的日志记录器 */
    private final Logger logger;
    
    public DefaultHttpClientFactory(Logger logger) {
        this.logger = logger;
    }
    
    @Override
    protected HttpClientConfig buildHttpClientConfig() {
        return HttpClientConfig.builder().setConTimeOutMillis(TIMEOUT)
            .setReadTimeOutMillis(TIMEOUT >> 1).build();
    }
    
    @Override
    protected Logger assignLogger() {
        return logger;
    }
}
