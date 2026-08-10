package org.keycloak.models.workflow;

import java.util.UUID;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowStateProvider.ScheduledStep;

/**
 * {@link WorkflowExecutionContext} 默认实现：绑定 workflow、事件、当前步骤与 executionId。
 */
final class DefaultWorkflowExecutionContext implements WorkflowExecutionContext {

    /** 目标资源 ID。 */
    private final String resourceId;
    /** 本次 workflow 执行唯一标识。 */
    private final String executionId;
    private final Workflow workflow;
    private final WorkflowEvent event;
    private final KeycloakSession session;
    /** 当前或待恢复的步骤。 */
    private final WorkflowStep step;
    /** 非 null 时表示从该步骤索引重启 workflow。 */
    private Integer restartPosition;

    /**
     * 为 workflow 事件创建执行上下文，executionId 随机生成。
     *
     * @param workflow the workflow
     * @param event the event
     */
    DefaultWorkflowExecutionContext(KeycloakSession session, Workflow workflow, WorkflowEvent event) {
        this(session, workflow, event, null, UUID.randomUUID().toString(), event.getResourceId());
    }

    /**
     * 为 workflow 事件创建执行上下文，使用指定的 executionId。
     *
     * @param workflow the workflow
     * @param event the event
     * @param executionId the execution ID
     */
    DefaultWorkflowExecutionContext(KeycloakSession session, Workflow workflow, WorkflowEvent event, String executionId) {
        this(session, workflow, event, null, executionId, event.getResourceId());
    }

    /**
     * 为已调度步骤创建上下文，从该步骤恢复 workflow；executionId 与 resourceId 取自 {@link ScheduledStep}。
     *
     * @param session the session
     * @param workflow the workflow
     * @param step the scheduled step
     */
    DefaultWorkflowExecutionContext(KeycloakSession session, Workflow workflow, ScheduledStep step) {
        this(session, workflow, null, step.stepId(), step.executionId(), step.resourceId());
    }

    /**
     * 复制构造函数：绑定新的 {@link KeycloakSession}，其余状态来自已有上下文。
     *
     * @param session the session
     * @param context the existing context
     */
    DefaultWorkflowExecutionContext(KeycloakSession session, DefaultWorkflowExecutionContext context) {
        this(session, new Workflow(session, context.getWorkflow()), context.getEvent(), context.getCurrentStepId(), context.getExecutionId(), context.getResourceId());
    }

    /**
     * 复制构造函数：绑定新 session 并指定当前 {@link WorkflowStep}。
     *
     * @param session the session
     * @param context the existing context
     * @param step the current step
     */
    DefaultWorkflowExecutionContext(KeycloakSession session, DefaultWorkflowExecutionContext context, WorkflowStep step) {
        this(session, new Workflow(session, context.getWorkflow()), context.getEvent(), step.getId(), context.getExecutionId(), context.getResourceId());
    }

    private DefaultWorkflowExecutionContext(KeycloakSession session, Workflow workflow, WorkflowEvent event, String stepId, String executionId, String resourceId) {
        this.session = session;
        this.workflow = workflow;
        this.event = event;

        if (stepId != null) {
            this.step = workflow.getStepById(stepId);
        } else {
            this.step = null;
        }

        this.executionId = executionId;
        this.resourceId = resourceId;
    }

    @Override
    public String getResourceId() {
        return resourceId;
    }

    @Override
    public WorkflowEvent getEvent() {
        return event;
    }

    @Override
    public WorkflowStep getNextStep() {
        if (restartPosition != null) {
            // 重启模式：忽略当前步骤，返回 restartPosition 处的步骤
            return workflow.getSteps().skip(restartPosition).findFirst().orElse(null);
        }
        return workflow.getSteps(step.getId()).skip(1).findFirst().orElse(null);
    }

    String getExecutionId() {
        return this.executionId;
    }

    Workflow getWorkflow() {
        return workflow;
    }

    WorkflowStep getStep() {
        return step;
    }

    /** 标记从指定步骤索引重启 workflow。 */
    void restart(int position) {
        this.restartPosition = position;
    }

    KeycloakSession getSession() {
        return session;
    }

    private String getCurrentStepId() {
        return step != null ? step.getId() : null;
    }
}
