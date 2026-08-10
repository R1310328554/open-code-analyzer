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

import java.util.Comparator;
import java.util.stream.Stream;

import org.keycloak.provider.Provider;
import org.keycloak.representations.workflows.WorkflowRepresentation;
import org.keycloak.utils.StringUtil;

/**
 * 工作流核心提供者接口，负责工作流 CRUD、激活/停用、事件提交与定时步骤执行。
 * <p>实现类管理领域内工作流定义及其与资源的绑定关系。</p>
 */
public interface WorkflowProvider extends Provider {

    /**
     * 返回指定资源类型的 {@link ResourceTypeSelector}。
     *
     * @param type     the resource type.
     * @return the corresponding {@link ResourceTypeSelector}.
     */
    ResourceTypeSelector getResourceTypeSelector(ResourceType type);

    /** 将 REST 表示转换为领域 {@link Workflow} 模型。 */
    Workflow toModel(WorkflowRepresentation representation);

    /** 按 ID 获取工作流。 */
    Workflow getWorkflow(String id);

    /** 删除工作流及其关联状态。 */
    void removeWorkflow(Workflow workflow);

    /** 返回当前领域内所有工作流。 */
    Stream<Workflow> getWorkflows();

    /** 按名称搜索并分页返回工作流（支持精确/模糊匹配）。 */
    default Stream<Workflow> getWorkflows(String search, Boolean exact, Integer first, Integer max) {
        return getWorkflows().sorted(Comparator.comparing(Workflow::getName))
                .filter(workflow -> {
                    if (StringUtil.isBlank(search)) {
                        return true;
                    }
                    return Boolean.TRUE.equals(exact) ? workflow.getName().equals(search) : workflow.getName().toLowerCase().contains(search.toLowerCase());
                })
                .skip(first).limit(max);
    }

    /** 获取资源上已调度的工作流表示。 */
    Stream<WorkflowRepresentation> getScheduledWorkflowsByResource(String resourceId);

    /** 将领域模型转换为 REST 表示。 */
    WorkflowRepresentation toRepresentation(Workflow workflow);

    /** 用 REST 表示更新工作流定义。 */
    void updateWorkflow(Workflow workflow, WorkflowRepresentation rep);

    /** 为指定资源激活工作流。 */
    void activate(Workflow workflow, ResourceType type, String resourceId);

    /** 停用资源上的工作流。 */
    void deactivate(Workflow workflow, String resourceId);

    /** 提交工作流触发事件以驱动执行。 */
    void submit(WorkflowEvent event);

    /** 执行所有已到期的定时步骤。 */
    void runScheduledSteps();

    /** 为所有满足激活条件的资源激活工作流。 */
    void activateForAllEligibleResources(Workflow workflow);

    /**
     * 将已调度资源从源步骤迁移至目标步骤（可在同一或不同工作流内）。
     * <br/>
     * 跨工作流迁移时须满足：源/目标工作流支持相同资源类型，且资源满足目标工作流激活条件。
     * 行为等同首次在目标工作流激活，但从指定目标步骤开始处理。
     * <br/>
     * If the resources are being migrated to a different workflow, the following conditions must be met:
     * <ul>
     *     <li>the source and destination workflows must support the same resource type;</li>
     *     <li>all resources must satisfy the activation conditions of the destination workflow.</li>
     * </ul>
     * The process behaves exactly as if the resources were being activated for the first time in the destination workflow,
     * except that the first step to be processed is the specified destination step. So, if the step is a scheduled step,
     * the resources will be scheduled accordingly. If the step is not a scheduled step, it will run immediately.
     *
     * @param stepIdFrom the id of the step to migrate from.
     * @param stepIdTo the id of the step to migrate to.
     */
    void migrateScheduledResources(String stepIdFrom, String stepIdTo);
}
