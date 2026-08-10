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

package com.alibaba.nacos.client.naming.remote.http;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.http.AbstractHttpClientFactory;
import com.alibaba.nacos.common.http.HttpClientBeanHolder;
import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.HttpClientFactory;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.tls.TlsSystemConfig;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import org.slf4j.Logger;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;
import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTPS_PREFIX;
import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTP_PREFIX;

/**
 * 命名 HTTP 客户端管理器（单例）。
 *
 * <p>提供命名模块专用的 {@link NacosRestTemplate}，配置连接/读超时与 TLS 前缀；关闭时销毁同步 HTTP 客户端。</p>
 *
 * @author mai.jh
 */
public class NamingHttpClientManager implements Closeable {
    
    /** HTTP 读超时毫秒数，可通过系统属性覆盖。 */
    private static final int READ_TIME_OUT_MILLIS = Integer
        .getInteger("com.alibaba.nacos.client.naming.rtimeout", 50000);
    
    /** HTTP 连接超时毫秒数。 */
    private static final int CON_TIME_OUT_MILLIS =
        Integer.getInteger("com.alibaba.nacos.client.naming.ctimeout", 3000);
    
    /** 是否启用 HTTPS 前缀。 */
    private static final boolean ENABLE_HTTPS = Boolean.getBoolean(TlsSystemConfig.TLS_ENABLE);
    
    /** 最大 HTTP 重定向次数。 */
    private static final int MAX_REDIRECTS = 5;
    
    /** 命名模块专用 HTTP 客户端工厂。 */
    private static final HttpClientFactory HTTP_CLIENT_FACTORY = new NamingHttpClientFactory();
    
    /** 静态内部类持有单例。 */
    private static class NamingHttpClientManagerInstance {
        
        private static final NamingHttpClientManager INSTANCE = new NamingHttpClientManager();
    }
    
    /** 获取命名 HTTP 客户端管理器单例。 */
    public static NamingHttpClientManager getInstance() {
        return NamingHttpClientManagerInstance.INSTANCE;
    }
    
    /** 返回 HTTP 或 HTTPS URL 前缀。 */
    public String getPrefix() {
        return ENABLE_HTTPS ? HTTPS_PREFIX : HTTP_PREFIX;
    }
    
    /** 获取命名模块共享的 NacosRestTemplate。 */
    public NacosRestTemplate getNacosRestTemplate() {
        return HttpClientBeanHolder.getNacosRestTemplate(HTTP_CLIENT_FACTORY);
    }
    
    @Override
    public void shutdown() throws NacosException {
        NAMING_LOGGER.info("[NamingHttpClientManager] Start destroying NacosRestTemplate");
        try {
            HttpClientBeanHolder.shutdownNacosSyncRest(HTTP_CLIENT_FACTORY.getClass().getName());
        } catch (Exception ex) {
            NAMING_LOGGER.error(
                "[NamingHttpClientManager] An exception occurred when the HTTP client was closed : {}",
                ExceptionUtil.getStackTrace(ex));
        }
        NAMING_LOGGER.info("[NamingHttpClientManager] Completed destruction of NacosRestTemplate");
    }
    
    /** 命名 HTTP 客户端工厂，配置超时与日志。 */
    private static class NamingHttpClientFactory extends AbstractHttpClientFactory {
        
        @Override
        protected HttpClientConfig buildHttpClientConfig() {
            return HttpClientConfig.builder().setConTimeOutMillis(CON_TIME_OUT_MILLIS)
                .setReadTimeOutMillis(READ_TIME_OUT_MILLIS).setMaxRedirects(MAX_REDIRECTS)
                .build();
        }
        
        @Override
        protected Logger assignLogger() {
            return NAMING_LOGGER;
        }
    }
}
