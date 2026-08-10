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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.client.utils.ParamUtil;
import com.alibaba.nacos.common.http.AbstractHttpClientFactory;
import com.alibaba.nacos.common.http.HttpClientBeanHolder;
import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.HttpClientFactory;
import com.alibaba.nacos.common.http.client.HttpClientRequestInterceptor;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.client.response.HttpClientResponse;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.model.RequestHttpEntity;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.MD5Utils;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 配置模块 HTTP 客户端管理器（单例）。
 *
 * <p>提供带连接/读超时与限流拦截器的 {@link NacosRestTemplate}，供配置拉取、发布等 HTTP 通路使用。</p>
 *
 * @author mai.jh
 */
public class ConfigHttpClientManager implements Closeable {
    
    private static final Logger LOGGER = LogUtils.logger(ConfigHttpClientManager.class);
    
    private static final HttpClientFactory HTTP_CLIENT_FACTORY = new ConfigHttpClientFactory();
    
    private static final int CON_TIME_OUT_MILLIS = ParamUtil.getConnectTimeout();
    
    private static final int READ_TIME_OUT_MILLIS = ParamUtil.getReadTimeout();
    
    private final LimiterHttpClientRequestInterceptor limiterHttpClientRequestInterceptor =
        new LimiterHttpClientRequestInterceptor();
    
    private static class ConfigHttpClientManagerInstance {
        
        private static final ConfigHttpClientManager INSTANCE = new ConfigHttpClientManager();
    }
    
    public static ConfigHttpClientManager getInstance() {
        return ConfigHttpClientManagerInstance.INSTANCE;
    }
    
    @Override
    public void shutdown() throws NacosException {
        NAMING_LOGGER.info("[ConfigHttpClientManager] Start destroying NacosRestTemplate");
        try {
            HttpClientBeanHolder.shutdownNacosSyncRest(HTTP_CLIENT_FACTORY.getClass().getName());
        } catch (Exception ex) {
            NAMING_LOGGER.error(
                "[ConfigHttpClientManager] An exception occurred when the HTTP client was closed : {}",
                ExceptionUtil.getStackTrace(ex));
        }
        NAMING_LOGGER.info("[ConfigHttpClientManager] Completed destruction of NacosRestTemplate");
    }
    
    /**
     * 获取连接超时（毫秒），取默认值与入参的较大值。
     *
     * @param connectTimeout 期望的连接超时
     * @return 实际使用的连接超时
     */
    public int getConnectTimeoutOrDefault(int connectTimeout) {
        return Math.max(CON_TIME_OUT_MILLIS, connectTimeout);
    }
    
    /**
     * 获取配置模块专用的 {@link NacosRestTemplate} 实例。
     *
     * @return 已挂载限流拦截器的 REST 模板
     */
    public NacosRestTemplate getNacosRestTemplate() {
        NacosRestTemplate nacosRestTemplate =
            HttpClientBeanHolder.getNacosRestTemplate(HTTP_CLIENT_FACTORY);
        List<HttpClientRequestInterceptor> interceptors = nacosRestTemplate.getInterceptors();
        if (!interceptors.contains(limiterHttpClientRequestInterceptor)) {
            interceptors.add(limiterHttpClientRequestInterceptor);
        }
        return nacosRestTemplate;
    }
    
    /** 配置 HTTP 客户端工厂，设置超时与日志。 */
    /** ConfigHttpClientFactory. */
    /** 配置 HTTP 客户端工厂。 */
    private static class ConfigHttpClientFactory extends AbstractHttpClientFactory {
        
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
    
    /** 基于 {@link Limiter} 的请求拦截器，超阈值时短路返回限流响应。 */
    /** config Limiter implement. */
    /** 配置限流拦截器。 */
    private static class LimiterHttpClientRequestInterceptor
        implements HttpClientRequestInterceptor {
        
        @Override
        public boolean isIntercept(URI uri, String httpMethod,
            RequestHttpEntity requestHttpEntity) {
            final String body = requestHttpEntity.isEmptyBody() ? ""
                : JacksonUtils.toJson(requestHttpEntity.getBody());
            return Limiter.isLimit(MD5Utils.md5Hex(uri + body, Constants.ENCODE));
        }
        
        @Override
        public HttpClientResponse intercept() {
            return new LimitResponse();
        }
    }
    
    /** 客户端限流触发时的占位 HTTP 响应。 */
    /** Limit Interrupt response. */
    /** 限流中断响应。 */
    private static class LimitResponse implements HttpClientResponse {
        
        @Override
        public Header getHeaders() {
            return Header.EMPTY;
        }
        
        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream("More than client-side current limit threshold"
                .getBytes(StandardCharsets.UTF_8));
        }
        
        @Override
        public int getStatusCode() {
            return NacosException.CLIENT_OVER_THRESHOLD;
        }
        
        @Override
        public String getStatusText() {
            return null;
        }
        
        @Override
        public void close() {
            
        }
    }
}
