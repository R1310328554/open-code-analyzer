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

package com.alibaba.nacos.ai.pipeline.repository;

import com.alibaba.nacos.ai.pipeline.model.PipelineExecution;

import java.util.List;

/**
 * Repository interface for pipeline execution record persistence.
 * <p>流水线执行记录持久化仓储接口。</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public interface PipelineExecutionRepository {
    
    /**
     * Insert a new pipeline execution record.
     * <p>插入新的流水线执行记录。</p>
     *
     * @param execution the execution record to save
     */
    void save(PipelineExecution execution);
    
    /**
     * Update an existing pipeline execution record (status and pipeline JSON).
     * <p>更新已有执行记录的状态与 pipeline JSON。</p>
     *
     * @param execution the execution record to update
     */
    void update(PipelineExecution execution);
    
    /**
     * Find a pipeline execution record by execution ID.
     * <p>按 executionId 查询单条执行记录。</p>
     *
     * @param executionId the execution ID
     * @return the execution record, or null if not found
     */
    PipelineExecution findById(String executionId);
    
    /**
     * Find the most recent pipeline execution record by resource information.
     * <p>按资源四元组查询最近一条执行记录。</p>
     *
     * @param resourceType the resource type
     * @param resourceName the resource name
     * @param namespaceId  the namespace ID
     * @param version      the resource version
     * @return the most recent matching execution record, or null if not found
     */
    PipelineExecution findByResource(String resourceType, String resourceName, String namespaceId,
        String version);
    
    /**
     * Find pipeline execution records by resource information with pagination support.
     *
     * <p>Supports optional filtering by resourceName, namespaceId, and version.
     * Results are ordered by create_time DESC.</p>
     * <p>按资源信息分页查询，可选过滤 resourceName/namespaceId/version，按 create_time 降序。</p>
     *
     * @param resourceType the resource type (required)
     * @param resourceName the resource name (optional, ignored if blank)
     * @param namespaceId  the namespace ID (optional, ignored if blank)
     * @param version      the resource version (optional, ignored if blank)
     * @param offset       the offset for pagination
     * @param limit        the maximum number of records to return
     * @return list of matching execution records
     */
    List<PipelineExecution> findByResourceWithPage(String resourceType, String resourceName,
        String namespaceId,
        String version, int offset, int limit);
    
    /**
     * Count pipeline execution records by resource information.
     *
     * <p>Uses the same filtering logic as {@link #findByResourceWithPage}.</p>
     * <p>统计符合相同过滤条件的执行记录总数。</p>
     *
     * @param resourceType the resource type (required)
     * @param resourceName the resource name (optional, ignored if blank)
     * @param namespaceId  the namespace ID (optional, ignored if blank)
     * @param version      the resource version (optional, ignored if blank)
     * @return the count of matching execution records
     */
    int countByResource(String resourceType, String resourceName, String namespaceId,
        String version);
}
