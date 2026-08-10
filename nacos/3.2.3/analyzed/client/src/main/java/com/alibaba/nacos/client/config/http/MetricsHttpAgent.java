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

package com.alibaba.nacos.client.config.http;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.monitor.MetricsMonitor;
import com.alibaba.nacos.common.http.HttpRestResult;
import io.prometheus.client.Histogram;

import java.util.Date;
import java.util.Map;

/**
 * 带 Prometheus 指标采集的 HTTP 代理装饰器。
 *
 * <p>包装底层 {@link HttpAgent}，记录各 HTTP 方法的耗时与响应码。</p>
 *
 * @author Nacos
 */
public class MetricsHttpAgent implements HttpAgent {
    
    /** HTTP GET 方法标识。 */
    private static final String GET = "GET";
    
    /** HTTP POST 方法标识。 */
    private static final String POST = "POST";
    
    /** HTTP DELETE 方法标识。 */
    private static final String DELETE = "DELETE";
    
    /** 请求未完成时的默认响应码占位。 */
    private static final String DEFAULT_CODE = "NA";
    
    /** 被装饰的底层 HTTP 代理。 */
    private final HttpAgent httpAgent;
    
    /** 构造指标装饰器。 */
    public MetricsHttpAgent(HttpAgent httpAgent) {
        this.httpAgent = httpAgent;
    }
    
    @Override
    /** 委托底层代理启动。 */
    public void start() throws NacosException {
        httpAgent.start();
    }
    
    @Override
    /** GET 请求并记录 Prometheus 直方图指标。 */
    public HttpRestResult<String> httpGet(String path, Map<String, String> headers,
        Map<String, String> paramValues,
        String encode, long readTimeoutMs) throws Exception {
        Date start = new Date();
        Histogram.Child histogram = MetricsMonitor.getConfigRequestMonitor(GET, path, DEFAULT_CODE);
        HttpRestResult<String> result;
        try {
            result = httpAgent.httpGet(path, headers, paramValues, encode, readTimeoutMs);
            histogram = MetricsMonitor.getConfigRequestMonitor(GET, path,
                String.valueOf(result.getCode()));
        } finally {
            histogram.observe(System.currentTimeMillis() - start.getTime());
        }
        
        return result;
    }
    
    @Override
    /** POST 请求并记录 Prometheus 直方图指标。 */
    public HttpRestResult<String> httpPost(String path, Map<String, String> headers,
        Map<String, String> paramValues,
        String encode, long readTimeoutMs) throws Exception {
        Date start = new Date();
        Histogram.Child histogram = MetricsMonitor.getConfigRequestMonitor(GET, path, DEFAULT_CODE);
        HttpRestResult<String> result;
        try {
            result = httpAgent.httpPost(path, headers, paramValues, encode, readTimeoutMs);
            histogram = MetricsMonitor.getConfigRequestMonitor(GET, path,
                String.valueOf(result.getCode()));
        } finally {
            histogram.observe(System.currentTimeMillis() - start.getTime());
        }
        
        return result;
    }
    
    @Override
    /** DELETE 请求并记录 Prometheus 直方图指标。 */
    public HttpRestResult<String> httpDelete(String path, Map<String, String> headers,
        Map<String, String> paramValues,
        String encode, long readTimeoutMs) throws Exception {
        Date start = new Date();
        Histogram.Child histogram = MetricsMonitor.getConfigRequestMonitor(GET, path, DEFAULT_CODE);
        HttpRestResult<String> result;
        try {
            result = httpAgent.httpDelete(path, headers, paramValues, encode, readTimeoutMs);
            histogram = MetricsMonitor.getConfigRequestMonitor(GET, path,
                String.valueOf(result.getCode()));
        } finally {
            histogram.observe(System.currentTimeMillis() - start.getTime());
        }
        
        return result;
    }
    
    @Override
    public String getName() {
        return httpAgent.getName();
    }
    
    @Override
    public String getNamespace() {
        return httpAgent.getNamespace();
    }
    
    @Override
    public String getTenant() {
        return httpAgent.getTenant();
    }
    
    @Override
    public String getEncode() {
        return httpAgent.getEncode();
    }
    
    @Override
    public void shutdown() throws NacosException {
        httpAgent.shutdown();
    }
}
