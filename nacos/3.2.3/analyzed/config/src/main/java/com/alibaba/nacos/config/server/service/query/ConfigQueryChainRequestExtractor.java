/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.query;

import com.alibaba.nacos.api.config.remote.request.ConfigQueryRequest;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 配置查询链请求提取接口：将 HTTP Servlet 或 gRPC {@link ConfigQueryRequest}
 * 统一转换为 {@link ConfigQueryChainRequest}，供责任链处理。
 * Interface for extracting configuration query chain requests from different sources.
 *
 * @author Nacos
 */
public interface ConfigQueryChainRequestExtractor {
    
    /**
     * 返回当前 SPI 实现名称，与配置项 nacos.config.query.chain.request.extractor 对应。
     *
     * @return the name of the current implementation
     */
    String getName();
    
    /**
     * 从 HTTP 请求解析 dataId、group、tenant、标签与客户端 IP 等查询参数。
     *
     * @param request the HTTP request object
     * @return the extracted configuration query chain request
     */
    ConfigQueryChainRequest extract(HttpServletRequest request);
    
    /**
     * 从 gRPC ConfigQueryRequest 与 RequestMeta 构建链式查询请求。
     *
     * @param request      the configuration query request object
     * @param requestMeta  the request metadata
     * @return the extracted configuration query chain request
     */
    ConfigQueryChainRequest extract(ConfigQueryRequest request, RequestMeta requestMeta);
}
