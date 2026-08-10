/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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
import com.alibaba.nacos.api.model.v2.Result;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * AI 流水线执行 Admin API 客户端（{@code /v3/admin/ai/pipelines/...}）。
 *
 * <p>方法返回服务端 {@link Result} 包装，便于调用方检查 {@code code}/{@code message}，业务失败不一定抛异常。</p>
 *
 * @author nacos
 * @since 3.2.1
 */
public interface PipelineAdminClient {
    
    /**
     * GET {@code /v3/admin/ai/pipelines/detail?pipelineId=} 查询流水线详情。
     *
     * @param pipelineId pipeline execution id
     * @return parsed {@link Result}; may carry non-success {@code code} when HTTP status is 200
     * @throws NacosException transport / HTTP failure (e.g. connection error, non-2xx without body)
     */
    Result<JsonNode> getPipelineDetail(String pipelineId) throws NacosException;
    
    /**
     * GET {@code /v3/admin/ai/pipelines/list} 分页列出流水线执行记录。
     *
     * @param resourceType resource type (required)
     * @param resourceName resource name (optional)
     * @param namespaceId  namespace id (optional)
     * @param version      version (optional)
     * @param pageNo       page number
     * @param pageSize     page size
     * @return parsed {@link Result}; may carry non-success {@code code} when HTTP status is 200
     * @throws NacosException transport / HTTP failure
     */
    Result<JsonNode> listPipelineExecutions(String resourceType, String resourceName,
        String namespaceId,
        String version, int pageNo, int pageSize) throws NacosException;
}
