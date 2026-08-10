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

import com.alibaba.nacos.common.utils.ThreadUtils;

import java.util.concurrent.TimeUnit;

/**
 * http client config build.
 * <p>HTTP 客户端不可变配置：封装连接/读超时、连接存活时间、连接池大小、压缩开关、IO 线程数与 User-Agent 等，通过 {@link HttpClientConfigBuilder} 构建。</p>
 *
 * @author mai.jh
 */
public class HttpClientConfig {
    
    /**
     * connect time out.
     * <p>建立 TCP 连接的超时时间（毫秒）。</p>
     */
    private final int conTimeOutMillis;
    
    /**
     * read time out.
     * <p>等待响应数据的读超时时间（毫秒）。</p>
     */
    private final int readTimeOutMillis;
    
    /**
     * connTimeToLive.
     * <p>连接在连接池中的最大存活时间。</p>
     */
    private final long connTimeToLive;
    
    /**
     * connTimeToLiveTimeUnit.
      * <p>HTTP 客户端配置与 Builder；详见类级说明。</p>
     */
    private final TimeUnit connTimeToLiveTimeUnit;
    
    /**
     * connectionRequestTimeout.
      * <p>HTTP 客户端配置与 Builder；详见类级说明。</p>
     */
    private final int connectionRequestTimeout;
    
    /**
     * max redirect.
      * <p>HTTP 客户端配置与 Builder；详见类级说明。</p>
     */
    private final int maxRedirects;
    
    /**
     * max connect total.
     * <p>连接池允许的最大连接总数（0 表示使用客户端默认值）。</p>
     */
    private final int maxConnTotal;
    
    /**
     * Assigns maximum connection per route value.
      * <p>HTTP 客户端配置与 Builder；详见类级说明。</p>
     */
    private final int maxConnPerRoute;
    
    /**
     * is HTTP compression enabled.
     * <p>是否启用 HTTP 内容压缩（如 gzip）。</p>
     */
    private final boolean contentCompressionEnabled;
    
    /**
     * io thread count.
      * <p>HTTP 客户端配置与 Builder；详见类级说明。</p>
     */
    private final int ioThreadCount;
    
    /**
     * user agent.
      * <p>HTTP 客户端配置与 Builder；详见类级说明。</p>
     */
    private final String userAgent;
    
    public HttpClientConfig(int conTimeOutMillis, int readTimeOutMillis, long connTimeToLive,
        TimeUnit timeUnit,
        int connectionRequestTimeout, int maxRedirects, int maxConnTotal, int maxConnPerRoute,
        boolean contentCompressionEnabled, int ioThreadCount, String userAgent) {
        this.conTimeOutMillis = conTimeOutMillis;
        this.readTimeOutMillis = readTimeOutMillis;
        this.connTimeToLive = connTimeToLive;
        this.connTimeToLiveTimeUnit = timeUnit;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.maxRedirects = maxRedirects;
        this.maxConnTotal = maxConnTotal;
        this.maxConnPerRoute = maxConnPerRoute;
        this.contentCompressionEnabled = contentCompressionEnabled;
        this.ioThreadCount = ioThreadCount;
        this.userAgent = userAgent;
    }
    
    public int getConTimeOutMillis() {
        return conTimeOutMillis;
    }
    
    public int getReadTimeOutMillis() {
        return readTimeOutMillis;
    }
    
    public long getConnTimeToLive() {
        return connTimeToLive;
    }
    
    public TimeUnit getConnTimeToLiveTimeUnit() {
        return connTimeToLiveTimeUnit;
    }
    
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }
    
    public int getMaxRedirects() {
        return maxRedirects;
    }
    
    public int getMaxConnTotal() {
        return maxConnTotal;
    }
    
    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }
    
    public boolean getContentCompressionEnabled() {
        return contentCompressionEnabled;
    }
    
    public int getIoThreadCount() {
        return ioThreadCount;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public static HttpClientConfigBuilder builder() {
        return new HttpClientConfigBuilder();
    }
    
    public static final class HttpClientConfigBuilder {
        
        /** 连接超时默认值（HttpClient5 不允许负数） */
        // not allow negative number in httpclient5
        private int conTimeOutMillis = 180_000;
        
        // not allow negative number in httpclient5
        private int readTimeOutMillis = 180_000;
        
        // not allow negative number in httpclient5
        private long connTimeToLive = 180_000;
        
        private TimeUnit connTimeToLiveTimeUnit = TimeUnit.MILLISECONDS;
        
        private int connectionRequestTimeout = 5000;
        
        private int maxRedirects = 50;
        
        private int maxConnTotal = 0;
        
        private int maxConnPerRoute = 0;
        
        private boolean contentCompressionEnabled = true;
        
        private int ioThreadCount = ThreadUtils.getSuitableThreadCount(1);
        
        private String userAgent;
        
        public HttpClientConfigBuilder setConTimeOutMillis(int conTimeOutMillis) {
            this.conTimeOutMillis = conTimeOutMillis;
            return this;
        }
        
        public HttpClientConfigBuilder setReadTimeOutMillis(int readTimeOutMillis) {
            this.readTimeOutMillis = readTimeOutMillis;
            return this;
        }
        
        public HttpClientConfigBuilder setConnectionTimeToLive(long connTimeToLive,
            TimeUnit connTimeToLiveTimeUnit) {
            this.connTimeToLive = connTimeToLive;
            this.connTimeToLiveTimeUnit = connTimeToLiveTimeUnit;
            return this;
        }
        
        public HttpClientConfigBuilder setConnectionRequestTimeout(int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }
        
        public HttpClientConfigBuilder setMaxRedirects(int maxRedirects) {
            this.maxRedirects = maxRedirects;
            return this;
        }
        
        public HttpClientConfigBuilder setMaxConnTotal(int maxConnTotal) {
            this.maxConnTotal = maxConnTotal;
            return this;
        }
        
        public HttpClientConfigBuilder setMaxConnPerRoute(int maxConnPerRoute) {
            this.maxConnPerRoute = maxConnPerRoute;
            return this;
        }
        
        public HttpClientConfigBuilder setContentCompressionEnabled(
            boolean contentCompressionEnabled) {
            this.contentCompressionEnabled = contentCompressionEnabled;
            return this;
        }
        
        public HttpClientConfigBuilder setIoThreadCount(int ioThreadCount) {
            this.ioThreadCount = ioThreadCount;
            return this;
        }
        
        public HttpClientConfigBuilder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        /**
         * build http client config.
         * <p>根据 Builder 当前字段值构造不可变 {@link HttpClientConfig}。</p>
         *
         * @return HttpClientConfig
         */
        public HttpClientConfig build() {
            return new HttpClientConfig(conTimeOutMillis, readTimeOutMillis, connTimeToLive,
                connTimeToLiveTimeUnit,
                connectionRequestTimeout, maxRedirects, maxConnTotal, maxConnPerRoute,
                contentCompressionEnabled,
                ioThreadCount, userAgent);
        }
    }
}
