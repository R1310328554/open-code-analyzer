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

package com.alibaba.nacos.console.handler.ai;

import com.alibaba.nacos.ai.pipeline.model.PipelineExecution;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;

/**
 * 控制台流水线（Pipeline）查询处理器接口：按 ID 查询执行详情及分页列举执行记录。
 * Handler interface for Pipeline query operations in Console layer.
 *
 * @author kiro
 * @since 3.2.0
 */
public interface PipelineHandler {
    
    /**
     * 按流水线执行 ID 查询单次执行详情。
     * Get pipeline execution detail by ID.
     *
     * @param pipelineId 流水线执行 ID
     * @return 流水线执行实体
     * @throws NacosException 查询失败时抛出
     */
    PipelineExecution getPipeline(String pipelineId) throws NacosException;
    
    /**
     * 按资源类型及可选过滤条件分页列举流水线执行记录。
     * List pipeline executions with pagination.
     *
     * @param resourceType 资源类型（必填）
     * @param resourceName 资源名称（可选）
     * @param namespaceId  命名空间 ID（可选）
     * @param version      版本号（可选）
     * @param pageNo       页码
     * @param pageSize     每页条数
     * @return 流水线执行分页结果
     * @throws NacosException 查询失败时抛出
     */
    Page<PipelineExecution> listPipelines(String resourceType, String resourceName,
        String namespaceId, String version, int pageNo, int pageSize) throws NacosException;
}
