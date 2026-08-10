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

package com.alibaba.nacos.client.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.http.AbstractHttpClientFactory;
import com.alibaba.nacos.common.http.HttpClientBeanHolder;
import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http Manager.
 * <p>Nacos 客户端同步 HTTP 客户端单例管理器：基于 {@link AbstractHttpClientFactory} 创建 {@link NacosRestTemplate}，统一连接/读超时并在关闭时释放 BeanHolder 资源。</p>
 *
 * @author Nacos
 */
public class HttpClientManager implements Closeable {
    
    /** HTTP 客户端生命周期日志 */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientManager.class);
    
    /** 内置工厂，固定 1s 连接超时与 3s 读超时 */
    private static final HttpClientFactory HTTP_CLIENT_FACTORY = new HttpClientFactory();
    
    /** 默认连接超时（毫秒） */
    private static final int CON_TIME_OUT_MILLIS = 1000;
    
    /** 默认读超时（毫秒） */
    private static final int READ_TIME_OUT_MILLIS = 3000;
    
    /** 静态内部类持有单例，延迟加载且线程安全 */
    private static class HttpClientManagerInstance {
        
        private static final HttpClientManager INSTANCE = new HttpClientManager();
    }
    
    /** 获取全局 {@link HttpClientManager} 单例。 */
    public static HttpClientManager getInstance() {
        return HttpClientManagerInstance.INSTANCE;
    }
    
    /** {@inheritDoc} 关闭同步 REST 客户端并记录销毁日志。 */
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
    
    /**
     * 返回有效连接超时：取调用方值与默认 1s 的较大者。
     *
     * @param connectTimeout 调用方期望超时
     * @return 实际使用的连接超时（毫秒）
     */
    public int getConnectTimeoutOrDefault(int connectTimeout) {
        return Math.max(CON_TIME_OUT_MILLIS, connectTimeout);
    }
    
    /**
     * 获取（或创建）与内置工厂绑定的 {@link NacosRestTemplate}。
     *
     * @return 同步 REST 模板实例
     */
    public NacosRestTemplate getNacosRestTemplate() {
        return HttpClientBeanHolder.getNacosRestTemplate(HTTP_CLIENT_FACTORY);
    }
    
    /** 内置 {@link AbstractHttpClientFactory}：注入超时与 Logger。 */
    private static class HttpClientFactory extends AbstractHttpClientFactory {
        
        @Override
        protected HttpClientConfig buildHttpClientConfig() {
            return HttpClientConfig.builder().setConTimeOutMillis(CON_TIME_OUT_MILLIS)
                .setReadTimeOutMillis(READ_TIME_OUT_MILLIS).build();
        }
        
        @Override
        protected Logger assignLogger() {
            return LOGGER;
        }
    }
}
