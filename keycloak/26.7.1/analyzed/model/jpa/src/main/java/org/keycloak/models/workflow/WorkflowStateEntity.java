/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.workflow;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 基于时间的工作流状态 JPA 实体，映射 WORKFLOW_STATE 表。
 * <p>
 * 记录某资源在某工作流下的执行 ID、待运行步骤及计划触发时间戳。
 */
@Entity
@Table(name = "WORKFLOW_STATE")
public class WorkflowStateEntity {

    /** 工作流执行唯一 ID（主键）。 */
    @Id
    @Column(name = "EXECUTION_ID")
    private String executionId;

    /** 目标资源 ID（如用户 ID）。 */
    @Column(name = "RESOURCE_ID")
    private String resourceId;

    /** 工作流组件 ID。 */
    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    /** 资源类型标识。 */
    @Column(name = "RESOURCE_TYPE")
    private String resourceType; // 是否需要持久化此字段仍待评估

    /** 下一待执行步骤 ID。 */
    @Column(name = "SCHEDULED_STEP_ID")
    private String scheduledStepId;

    /** 计划执行该步骤的时间戳（毫秒 epoch）。 */
    @Column(name = "SCHEDULED_STEP_TIMESTAMP")
    private long scheduledStepTimestamp;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getScheduledStepId() {
        return scheduledStepId;
    }

    public void setScheduledStepId(String scheduledStepId) {
        this.scheduledStepId = scheduledStepId;
    }

    public long getScheduledStepTimestamp() {
        return scheduledStepTimestamp;
    }

    public void setScheduledStepTimestamp(long scheduledStepTimestamp) {
        this.scheduledStepTimestamp = scheduledStepTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowStateEntity that = (WorkflowStateEntity) o;
        return Objects.equals(resourceId, that.resourceId) && Objects.equals(workflowId, that.workflowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, workflowId);
    }
}
