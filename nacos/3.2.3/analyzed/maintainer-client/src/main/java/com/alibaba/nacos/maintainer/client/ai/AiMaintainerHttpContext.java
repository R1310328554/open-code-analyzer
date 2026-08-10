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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.maintainer.client.model.HttpRequest;
import com.alibaba.nacos.maintainer.client.remote.ClientHttpProxy;
import com.alibaba.nacos.maintainer.client.utils.ParamUtil;
import com.alibaba.nacos.plugin.auth.api.RequestResource;

import java.util.Properties;

/**
 * AI 维护 HTTP 上下文：持有 {@link ClientHttpProxy} 并提供命名空间、鉴权资源与请求构建。
 *
 * <p>由 {@link NacosAiMaintainerServiceImpl} 及各子服务实现共享。</p>
 */
final class AiMaintainerHttpContext {
    
    /** 维护客户端 HTTP 代理（服务端列表、鉴权、同步请求）。 */
    private final ClientHttpProxy clientHttpProxy;
    
    /** 根据属性创建 HTTP 代理并初始化序列化。 */
    AiMaintainerHttpContext(Properties properties) throws NacosException {
        this(new ClientHttpProxy(properties));
    }
    
    /** 注入已有 HTTP 代理（测试或组合场景）。 */
    AiMaintainerHttpContext(ClientHttpProxy clientHttpProxy) {
        this.clientHttpProxy = clientHttpProxy;
        ParamUtil.initSerialization();
    }
    
    /** 返回底层 HTTP 代理。 */
    ClientHttpProxy getClientHttpProxy() {
        return clientHttpProxy;
    }
    
    /** 空命名空间时回退为 {@link Constants#DEFAULT_NAMESPACE_ID}。 */
    String resolveNamespace(String namespaceId) {
        return StringUtils.isBlank(namespaceId) ? Constants.DEFAULT_NAMESPACE_ID : namespaceId;
    }
    
    /** 构建 AI 类型 {@link RequestResource}（默认 group、可空资源名）。 */
    RequestResource buildRequestResource(String namespaceId, String resourceName) {
        RequestResource.Builder builder = RequestResource.aiBuilder();
        builder.setNamespace(namespaceId);
        builder.setGroup(Constants.DEFAULT_GROUP);
        builder.setResource(null == resourceName ? StringUtils.EMPTY : resourceName);
        return builder.build();
    }
    
    /** 创建绑定鉴权资源的 HTTP 请求构建器。 */
    HttpRequest.Builder buildHttpRequestBuilder(RequestResource resource) {
        return new HttpRequest.Builder().setResource(resource);
    }
}
