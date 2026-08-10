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

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Nacos AI 模块流水线（Pipeline）维护服务接口。
 *
 * <p>继承 {@link PipelineAdminClient}，新版 API 返回 {@link com.alibaba.nacos.api.model.v2.Result}；
 * 为兼容旧调用方仍保留仅返回 {@link JsonNode} 的废弃方法。</p>
 *
 * <p>废弃方法返回 {@link JsonNode} 是因为 {@code PipelineExecution} 位于 ai 模块，
 * maintainer-client 无编译期依赖；调用方需自行反序列化为具体类型。</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public interface PipelineMaintainerService extends PipelineAdminClient {
    
    /**
     * 按流水线执行 ID 获取详情。
     *
     * @param pipelineId the pipeline execution ID
     * @return JSON representation of the pipeline execution data field on success
     * @throws NacosException if the request fails or the server returns a non-success Result
     * @deprecated since 3.2.1 use {@link #getPipelineDetail(String)} to handle {@code Result} explicitly
     */
    @Since("3.2.0")
    @Deprecated
    JsonNode getPipeline(String pipelineId) throws NacosException;
    
    /**
     * 分页列出流水线执行记录。
     *
     * @param resourceType the resource type (required)
     * @param resourceName the resource name (optional)
     * @param namespaceId  the namespace ID (optional)
     * @param version      the version (optional)
     * @param pageNo       the page number
     * @param pageSize     the page size
     * @return JSON representation of the page data field on success
     * @throws NacosException if the request fails or the server returns a non-success Result
     * @deprecated since 3.2.1 use {@link #listPipelineExecutions(String, String, String, String, int, int)}
     */
    @Since("3.2.0")
    @Deprecated
    JsonNode listPipelines(String resourceType, String resourceName, String namespaceId,
        String version, int pageNo, int pageSize) throws NacosException;
}
