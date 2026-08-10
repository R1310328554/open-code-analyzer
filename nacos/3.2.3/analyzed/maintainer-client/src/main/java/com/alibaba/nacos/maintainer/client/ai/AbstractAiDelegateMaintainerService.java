/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.maintainer.client.ai;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.maintainer.client.model.HttpRequest;
import com.alibaba.nacos.plugin.auth.api.RequestResource;

import java.util.Map;

/**
 * AI 维护服务抽象基类：封装 HTTP 代理、命名空间解析与请求构建等公共能力。
 *
 * <p>各 {@code *MaintainerServiceImpl} 通过 {@link AiMaintainerHttpContext} 共享客户端配置。</p>
 */
abstract class AbstractAiDelegateMaintainerService {
    
    /** AI 维护 HTTP 上下文（代理、命名空间、鉴权资源）。 */
    protected final AiMaintainerHttpContext context;
    
    /** 保存 HTTP 上下文供子类委托调用。 */
    protected AbstractAiDelegateMaintainerService(AiMaintainerHttpContext context) {
        this.context = context;
    }
    
    /** 同步执行 HTTP 请求并返回字符串响应体。 */
    protected HttpRestResult<String> executeSyncHttpRequest(HttpRequest request)
        throws NacosException {
        return context.getClientHttpProxy().executeSyncHttpRequest(request);
    }
    
    /** 解析命名空间 ID（空则使用默认命名空间）。 */
    protected String resolveNamespace(String namespaceId) {
        return context.resolveNamespace(namespaceId);
    }
    
    /** 构建 AI 模块鉴权 {@link RequestResource}。 */
    protected RequestResource buildRequestResource(String namespaceId, String resourceName) {
        return context.buildRequestResource(namespaceId, resourceName);
    }
    
    /** 基于鉴权资源创建 {@link HttpRequest.Builder}。 */
    protected HttpRequest.Builder buildHttpRequestBuilder(RequestResource resource) {
        return context.buildHttpRequestBuilder(resource);
    }
    
    /** 非空时向参数 Map 写入键值。 */
    protected void putIfNotBlank(Map<String, String> params, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            params.put(key, value);
        }
    }
}
