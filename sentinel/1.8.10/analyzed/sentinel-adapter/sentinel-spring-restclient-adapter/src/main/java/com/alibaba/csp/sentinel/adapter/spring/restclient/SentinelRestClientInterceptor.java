/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.restclient;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.spring.restclient.extractor.RestClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.fallback.RestClientFallback;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * 将 Sentinel 与 Spring {@link org.springframework.web.client.RestClient} 集成的 {@link ClientHttpRequestInterceptor}。
 *
 * <p>本拦截器为每个请求创建两级 Sentinel 资源：
 * <ul>
 * <li><b>主机级资源</b>：{@code METHOD:scheme://host[:port]}，
 * 例如 {@code GET:https://httpbin.org}</li>
 * <li><b>路径级资源</b>：由 {@link RestClientResourceExtractor} 提取，
 * 默认格式 {@code METHOD:scheme://host[:port]/path}，
 * 例如 {@code GET:https://httpbin.org/get}</li>
 * </ul>
 *
 * <p>双层设计支持：
 * <ul>
 * <li>主机级流控：限制对某服务的整体流量</li>
 * <li>路径级流控：限制特定端点的流量</li>
 * <li>任一层级均可配置熔断降级</li>
 * </ul>
 *
 * <p>支持能力：
 * <ul>
 * <li>流量控制（QPS 限流）</li>
 * <li>熔断降级（degrade）</li>
 * <li>通过 {@link RestClientResourceExtractor} 自定义资源名提取</li>
 * <li>通过 {@link RestClientFallback} 自定义降级响应</li>
 * </ul>
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestClientConfig
 * @see RestClientResourceExtractor
 * @see RestClientFallback
 */
public class SentinelRestClientInterceptor implements ClientHttpRequestInterceptor {

    private final SentinelRestClientConfig config;

    public SentinelRestClientInterceptor() {
        this.config = new SentinelRestClientConfig();
    }

    public SentinelRestClientInterceptor(SentinelRestClientConfig config) {
        AssertUtil.notNull(config, "config cannot be null");
        this.config = config;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        URI uri = request.getURI();
        
        String hostResource = buildHostResourceName(request, uri);
        String pathResource = buildPathResourceName(request);
        
        boolean entryWithPath = !hostResource.equals(pathResource);

        Entry hostEntry = null;
        Entry pathEntry = null;
        
        try {
            hostEntry = SphU.entry(hostResource, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            
            if (entryWithPath) {
                pathEntry = SphU.entry(pathResource, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            }

            ClientHttpResponse response = execution.execute(request, body);

            if (response.getStatusCode().is5xxServerError()) {
                RuntimeException ex = new RuntimeException("Server error: " + response.getStatusCode().value());
                Tracer.traceEntry(ex, hostEntry);
                if (pathEntry != null) {
                    Tracer.traceEntry(ex, pathEntry);
                }
            }

            return response;
        } catch (BlockException ex) {
            return handleBlockException(request, body, execution, ex);
        } catch (IOException ex) {
            // 发生 IO 异常时无需对路径级 Entry 进行异常追踪。
            Tracer.traceEntry(ex, hostEntry);
            throw ex;
        } finally {
            if (pathEntry != null) {
                pathEntry.exit();
            }
            if (hostEntry != null) {
                hostEntry.exit();
            }
        }
    }

    private String buildHostResourceName(HttpRequest request, URI uri) {
        String hostResource = request.getMethod().toString() + ":" + 
                              uri.getScheme() + "://" + 
                              uri.getHost() + 
                              (uri.getPort() == -1 ? "" : ":" + uri.getPort());
        
        if (StringUtil.isNotBlank(config.getResourcePrefix())) {
            hostResource = config.getResourcePrefix() + hostResource;
        }
        
        return hostResource;
    }

    private String buildPathResourceName(HttpRequest request) {
        String pathResource = config.getResourceExtractor().extract(request);
        
        if (StringUtil.isNotBlank(config.getResourcePrefix())) {
            pathResource = config.getResourcePrefix() + pathResource;
        }
        
        return pathResource;
    }

    private ClientHttpResponse handleBlockException(HttpRequest request, byte[] body,
                                                    ClientHttpRequestExecution execution, 
                                                    BlockException ex) {
		return config.getFallback().handle(request, body, execution, ex);
	}
}