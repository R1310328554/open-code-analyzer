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

package com.alibaba.nacos.ai.pipeline.model;

import java.util.List;

/**
 * Pipeline execution record entity, persisted to the database.
 * <p>流水线执行记录实体，持久化至数据库。</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public class PipelineExecution {
    
    /** 执行 ID（UUID）。 */
    /** Execution ID (UUID).
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private String executionId;
    
    /** 资源类型。 */
    /** Resource type.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private String resourceType;
    
    /** 资源名称。 */
    /** Resource name.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private String resourceName;
    
    /** 命名空间 ID。 */
    /** Namespace ID.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private String namespaceId;
    
    /** 资源版本号。 */
    /** Resource version.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private String version;
    
    /** 执行状态：IN_PROGRESS、APPROVED、REJECTED。 */
    /** Execution status: IN_PROGRESS, APPROVED, REJECTED.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private PipelineExecutionStatus status;
    
    /** 各节点执行详情列表（pipeline 字段以 JSON 序列化存储）。 */
    /** Node execution details list (serialized as JSON in the pipeline field).
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private List<PipelineNodeResult> pipeline;
    
    /** 创建时间戳（毫秒）。 */
    /** Creation time.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private long createTime;
    
    /** 最后更新时间戳（毫秒）。 */
    /** Last update time.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private long updateTime;
    
    public PipelineExecution() {
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public PipelineExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(PipelineExecutionStatus status) {
        this.status = status;
    }
    
    public List<PipelineNodeResult> getPipeline() {
        return pipeline;
    }
    
    public void setPipeline(List<PipelineNodeResult> pipeline) {
        this.pipeline = pipeline;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public long getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
