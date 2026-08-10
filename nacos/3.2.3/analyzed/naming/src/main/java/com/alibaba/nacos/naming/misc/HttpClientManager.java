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

package com.alibaba.nacos.naming.misc;

import static com.alibaba.nacos.naming.misc.Loggers.SRV_LOG;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;

import com.alibaba.nacos.common.http.AbstractApacheHttpClientFactory;
import com.alibaba.nacos.common.http.AbstractHttpClientFactory;
import com.alibaba.nacos.common.http.HttpClientBeanHolder;
import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.HttpClientFactory;
import com.alibaba.nacos.common.http.client.NacosAsyncRestTemplate;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * Naming HTTP 客户端管理器。
 *
 * <p>维护同步/异步及健康检查专用等多套 {@link NacosRestTemplate} 与 {@link NacosAsyncRestTemplate} 实例，注册 JVM 关闭钩子统一销毁连接池。</p>
 *
 * @author mai.jh
 */
public class HttpClientManager {
    
    private static final int TIME_OUT_MILLIS = 10000;
    
    private static final int CON_TIME_OUT_MILLIS = 5000;
    
    private static final HttpClientFactory SYNC_HTTP_CLIENT_FACTORY = new SyncHttpClientFactory();
    
    private static final HttpClientFactory ASYNC_HTTP_CLIENT_FACTORY = new AsyncHttpClientFactory();
    
    private static final HttpClientFactory PROCESSOR_ASYNC_HTTP_CLIENT_FACTORY =
        new ProcessorHttpClientFactory();
    
    private static final HttpClientFactory APACHE_SYNC_HTTP_CLIENT_FACTORY =
        new ApacheSyncHttpClientFactory();
    
    private static final NacosRestTemplate NACOS_REST_TEMPLATE;
    
    private static final NacosRestTemplate APACHE_NACOS_REST_TEMPLATE;
    
    private static final NacosAsyncRestTemplate NACOS_ASYNC_REST_TEMPLATE;
    
    private static final NacosAsyncRestTemplate PROCESSOR_NACOS_ASYNC_REST_TEMPLATE;
    
    static {
        // 初始化各类型 Nacos HTTP 客户端模板
        NACOS_REST_TEMPLATE = HttpClientBeanHolder.getNacosRestTemplate(SYNC_HTTP_CLIENT_FACTORY);
        APACHE_NACOS_REST_TEMPLATE =
            HttpClientBeanHolder.getNacosRestTemplate(APACHE_SYNC_HTTP_CLIENT_FACTORY);
        NACOS_ASYNC_REST_TEMPLATE =
            HttpClientBeanHolder.getNacosAsyncRestTemplate(ASYNC_HTTP_CLIENT_FACTORY);
        PROCESSOR_NACOS_ASYNC_REST_TEMPLATE = HttpClientBeanHolder
            .getNacosAsyncRestTemplate(PROCESSOR_ASYNC_HTTP_CLIENT_FACTORY);
        
        ThreadUtils.addShutdownHook(HttpClientManager::shutdown);
    }
    
    /** 获取默认同步 REST 模板。 */
    public static NacosRestTemplate getNacosRestTemplate() {
        return NACOS_REST_TEMPLATE;
    }
    
    /**
     * 获取基于 Apache HttpClient 的同步模板。
     *
     * @return NacosRestTemplate
     */
    public static NacosRestTemplate getApacheRestTemplate() {
        return APACHE_NACOS_REST_TEMPLATE;
    }
    
    /** 获取通用异步 REST 模板。 */
    public static NacosAsyncRestTemplate getAsyncRestTemplate() {
        return NACOS_ASYNC_REST_TEMPLATE;
    }
    
    /**
     * 获取健康检查专用异步模板（兼容旧版 VIPServer 行为）。
     *
     * <p>仅供 {@link com.alibaba.nacos.naming.healthcheck.v2.processor.HttpHealthCheckProcessor} 使用。</p>
     *
     * @return NacosAsyncRestTemplate
     */
    public static NacosAsyncRestTemplate getProcessorNacosAsyncRestTemplate() {
        return PROCESSOR_NACOS_ASYNC_REST_TEMPLATE;
    }
    
    private static void shutdown() {
        SRV_LOG.info("[NamingServerHttpClientManager] Start destroying HTTP-Client");
        try {
            HttpClientBeanHolder
                .shutdownNacosSyncRest(SYNC_HTTP_CLIENT_FACTORY.getClass().getName());
            HttpClientBeanHolder
                .shutdownNacosSyncRest(APACHE_SYNC_HTTP_CLIENT_FACTORY.getClass().getName());
            HttpClientBeanHolder
                .shutdownNacosAsyncRest(ASYNC_HTTP_CLIENT_FACTORY.getClass().getName());
            HttpClientBeanHolder
                .shutdownNacosAsyncRest(PROCESSOR_ASYNC_HTTP_CLIENT_FACTORY.getClass().getName());
        } catch (Exception ex) {
            SRV_LOG.error(
                "[NamingServerHttpClientManager] An exception occurred when the HTTP client was closed : {}",
                ExceptionUtil.getStackTrace(ex));
        }
        SRV_LOG.info("[NamingServerHttpClientManager] Completed destruction of HTTP-Client");
    }
    
    /** 通用异步 HTTP 客户端工厂。 */
    private static class AsyncHttpClientFactory extends AbstractHttpClientFactory {
        
        @Override
        protected HttpClientConfig buildHttpClientConfig() {
            return HttpClientConfig.builder().setConTimeOutMillis(CON_TIME_OUT_MILLIS)
                .setReadTimeOutMillis(TIME_OUT_MILLIS).setUserAgent(UtilsAndCommons.SERVER_VERSION)
                .setMaxConnTotal(-1).setMaxConnPerRoute(128).setMaxRedirects(0).build();
        }
        
        @Override
        protected Logger assignLogger() {
            return SRV_LOG;
        }
    }
    
    /** 通用同步 HTTP 客户端工厂。 */
    private static class SyncHttpClientFactory extends AbstractHttpClientFactory {
        
        @Override
        protected HttpClientConfig buildHttpClientConfig() {
            return HttpClientConfig.builder().setConTimeOutMillis(CON_TIME_OUT_MILLIS)
                .setReadTimeOutMillis(TIME_OUT_MILLIS).setMaxRedirects(0).build();
        }
        
        @Override
        protected Logger assignLogger() {
            return SRV_LOG;
        }
    }
    
    /** Apache 同步 HTTP 客户端工厂。 */
    private static class ApacheSyncHttpClientFactory extends AbstractApacheHttpClientFactory {
        
        @Override
        protected HttpClientConfig buildHttpClientConfig() {
            return HttpClientConfig
                .builder()
                .setConnectionTimeToLive(500, TimeUnit.MILLISECONDS)
                .setMaxConnTotal(EnvUtil.getAvailableProcessors(2))
                .setMaxConnPerRoute(EnvUtil.getAvailableProcessors()).setMaxRedirects(0).build();
        }
        
        @Override
        protected Logger assignLogger() {
            return SRV_LOG;
        }
    }
    
    /** 健康检查处理器专用异步 HTTP 客户端工厂，短超时、高并发连接池。 */
    public static class ProcessorHttpClientFactory extends AbstractHttpClientFactory {
        
        @Override
        protected HttpClientConfig buildHttpClientConfig() {
            return HttpClientConfig
                .builder()
                .setConnectionRequestTimeout(500)
                .setReadTimeOutMillis(500)
                .setConTimeOutMillis(500)
                .setIoThreadCount(1)
                .setContentCompressionEnabled(false)
                .setMaxRedirects(0)
                .setMaxConnTotal(5000)
                .setMaxConnPerRoute(-1)
                .setUserAgent("VIPServer")
                .build();
        }
        
        @Override
        protected Logger assignLogger() {
            return SRV_LOG;
        }
        
        @Override
        protected void monitorAndExtension(AsyncClientConnectionManager connectionManager) {
            GlobalExecutor.scheduleMonitorHealthCheckPool(
                new MonitorHealthCheckPool(connectionManager), 60, 60, TimeUnit.SECONDS);
        }
    }
    
    /** 周期性监控并清理健康检查 HTTP 连接池。 */
    private static class MonitorHealthCheckPool implements Runnable {
        
        private AsyncClientConnectionManager connectionManager;
        
        public MonitorHealthCheckPool(AsyncClientConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
        }
        
        @Override
        public void run() {
            // 释放过期与空闲连接
            closeExpiredAndIdleConnections();
            monitor();
        }
        
        private void monitor() {
            try {
                PoolingAsyncClientConnectionManager manager =
                    (PoolingAsyncClientConnectionManager) connectionManager;
                // 输出各路由连接池统计
                Set<HttpRoute> routes = manager.getRoutes();
                if (routes != null && !routes.isEmpty()) {
                    for (HttpRoute httpRoute : routes) {
                        PoolStats stats = manager.getStats(httpRoute);
                        SRV_LOG.debug("connectionManager every route: {}", stats);
                    }
                }
                // 输出全局连接池统计
                PoolStats totalStats = manager.getTotalStats();
                SRV_LOG.debug("connectionManager total status: {}", totalStats);
            } catch (Exception e) {
                SRV_LOG.warn("MonitorHealthCheckPool monitor warn", e);
            }
        }
        
        private void closeExpiredAndIdleConnections() {
            try {
                PoolingAsyncClientConnectionManager manager =
                    (PoolingAsyncClientConnectionManager) connectionManager;
                manager.closeExpired();
                manager.closeIdle(Timeout.of(CON_TIME_OUT_MILLIS * 10, TimeUnit.SECONDS));
            } catch (Exception e) {
                SRV_LOG.warn("MonitorHealthCheckPool clean warn", e);
            }
        }
    }
}
