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

package com.alibaba.nacos.maintainer.client.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.http.DefaultHttpClientFactory;
import com.alibaba.nacos.common.http.HttpClientBeanHolder;
import com.alibaba.nacos.common.http.HttpClientFactory;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 维护客户端 HTTP 连接管理器（单例）：提供共享 {@link NacosRestTemplate}。
 *
 * <p>使用 {@link DefaultHttpClientFactory} 创建同步 REST 客户端，在 {@link #shutdown()} 时销毁 BeanHolder 中的实例。</p>
 *
 * @author Nacos
 */
public class HttpClientManager implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientManager.class);
    
    /** 单例实例（双重检查锁）。 */
    private static volatile HttpClientManager httpClientManager;
    
    private HttpClientManager() {
    }
    
    /** 获取全局 {@link HttpClientManager} 单例。 */
    public static HttpClientManager getInstance() {
        if (httpClientManager == null) {
            synchronized (HttpClientManager.class) {
                if (httpClientManager == null) {
                    httpClientManager = new HttpClientManager();
                }
            }
        }
        return httpClientManager;
    }
    
    private static final HttpClientFactory HTTP_CLIENT_FACTORY =
        new DefaultHttpClientFactory(LOGGER);
    
    /**
     * get NacosRestTemplate Instance.
     *
     * @return NacosRestTemplate
      * <p>Nacos 模块组件；详见上方说明。</p>
     */
    public NacosRestTemplate getNacosRestTemplate() {
        return HttpClientBeanHolder.getNacosRestTemplate(HTTP_CLIENT_FACTORY);
    }
    
    /** 销毁 NacosRestTemplate 并释放 HTTP 连接池。 */
    @Override
    public void shutdown() throws NacosException {
        LOGGER.info("[HttpClientManager] Start destroying NacosRestTemplate");
        try {
            HttpClientBeanHolder.shutdownNacosSyncRest(HTTP_CLIENT_FACTORY.getClass().getName());
        } catch (Exception ex) {
            LOGGER.error(
                "[HttpClientManager] An exception occurred when the HTTP client was closed : {}",
                ExceptionUtil.getStackTrace(ex));
        }
        LOGGER.info("[HttpClientManager] Completed destruction of NacosRestTemplate");
    }
}
