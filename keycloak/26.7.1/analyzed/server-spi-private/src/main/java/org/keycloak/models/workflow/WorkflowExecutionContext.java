package org.keycloak.models.workflow;

/**
 * 工作流执行上下文：暴露当前资源、触发事件与待执行步骤等信息。
 */
public interface WorkflowExecutionContext {

    /**
     * 返回当前工作流执行所绑定的资源 ID。
     *
     * @return the id of the resource
     */
    String getResourceId();

    /**
     * 返回激活当前执行的工作流事件；从计划步骤恢复时可为 {@code null}。
     *
     * @return the event bound to the current execution.
     */
    WorkflowEvent getEvent();

    /**
     * 返回工作流中下一个待执行的步骤。
     *
     * @return the next workflow step
     */
    WorkflowStep getNextStep();
}
