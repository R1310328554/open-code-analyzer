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

package com.alibaba.nacos.config.server.service.notify;

import com.alibaba.nacos.common.http.AbstractHttpClientFactory;
import com.alibaba.nacos.common.http.HttpClientBeanHolder;
import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.client.NacosAsyncRestTemplate;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.config.server.utils.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置服务端 HTTP 客户端管理器：维护同步与异步 {@link NacosRestTemplate} 单例，
 * 供集群内通知、监听状态查询等模块复用，并在 JVM 关闭时优雅释放连接池。
 * http client manager.
 *
 * @author mai.jh
 */
public final class HttpClientManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientManager.class);
    
    /**
     * 与其他 Nacos 节点通信的默认连接与读超时（毫秒）。
     */
    private static final int TIMEOUT = 500;
    
    private static final NacosRestTemplate NACOS_REST_TEMPLATE;
    
    private static final NacosAsyncRestTemplate NACOS_ASYNC_REST_TEMPLATE;
    
    static {
        // 初始化同步/异步 RestTemplate，通知模块使用可配置更长超时
        NACOS_REST_TEMPLATE = HttpClientBeanHolder
            .getNacosRestTemplate(new ConfigHttpClientFactory(TIMEOUT, TIMEOUT));
        NACOS_ASYNC_REST_TEMPLATE = HttpClientBeanHolder.getNacosAsyncRestTemplate(
            new ConfigHttpClientFactory(PropertyUtil.getNotifyConnectTimeout(),
                PropertyUtil.getNotifySocketTimeout()));
        
        ThreadUtils.addShutdownHook(HttpClientManager::shutdown);
    }
    
    /** 返回配置模块共享的同步 HTTP 客户端（500ms 超时）。 */
    public static NacosRestTemplate getNacosRestTemplate() {
        return NACOS_REST_TEMPLATE;
    }
    
    /** 返回异步 HTTP 客户端，超时取自 {@link PropertyUtil} 通知配置。 */
    public static NacosAsyncRestTemplate getNacosAsyncRestTemplate() {
        return NACOS_ASYNC_REST_TEMPLATE;
    }
    
    private static void shutdown() {
        LOGGER.info("[ConfigServer-HttpClientManager] Start destroying NacosRestTemplate");
        try {
            final String httpClientFactoryBeanName = ConfigHttpClientFactory.class.getName();
            HttpClientBeanHolder.shutdownNacosSyncRest(httpClientFactoryBeanName);
            HttpClientBeanHolder.shutdownNacosAsyncRest(httpClientFactoryBeanName);
        } catch (Exception ex) {
            LOGGER.error(
                "[ConfigServer-HttpClientManager] An exception occurred when the HTTP client was closed : {}",
                ExceptionUtil.getStackTrace(ex));
        }
        LOGGER.info("[ConfigServer-HttpClientManager] Completed destruction of NacosRestTemplate");
    }
    
    /**
     * 内部 HTTP 客户端工厂，按构造参数定制连接与读超时。
     */
    private static class ConfigHttpClientFactory extends AbstractHttpClientFactory {
        
        private final int conTimeOutMillis;
        
        private final int readTimeOutMillis;
        
        public ConfigHttpClientFactory(int conTimeOutMillis, int readTimeOutMillis) {
            this.conTimeOutMillis = conTimeOutMillis;
            this.readTimeOutMillis = readTimeOutMillis;
        }
        
        /** 构建带连接/读超时的 {@link HttpClientConfig}。 */
        @Override
        protected HttpClientConfig buildHttpClientConfig() {
            return HttpClientConfig.builder().setConTimeOutMillis(conTimeOutMillis)
                .setReadTimeOutMillis(readTimeOutMillis).build();
        }
        
        @Override
        protected Logger assignLogger() {
            return LOGGER;
        }
    }
}
