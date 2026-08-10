package org.keycloak.models.workflow;

import org.keycloak.representations.workflows.WorkflowConstants;

/**
 * 临时（Ad-hoc）工作流事件：无绑定原始事件，使用 {@link org.keycloak.representations.workflows.WorkflowConstants#AD_HOC} 标识。
 * <p>用于手动触发或计划步骤恢复等不依赖具体 Keycloak 事件的场景。</p>
 */
final class AdhocWorkflowEvent extends WorkflowEvent {

    AdhocWorkflowEvent(ResourceType type, String resourceId) {
        super(type, resourceId, null, WorkflowConstants.AD_HOC);
    }
}
