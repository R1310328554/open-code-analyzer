package org.keycloak.models.workflow;

import java.util.List;

/**
 * 重启 workflow 的步骤 provider：将执行指针设到配置的步骤索引。
 *
 * @param position 目标步骤在 workflow 中的零基索引
 */
public record RestartWorkflowStepProvider(int position) implements WorkflowStepProvider {

    @Override
    public void run(WorkflowExecutionContext context) {
        if (context instanceof DefaultWorkflowExecutionContext) {
            Workflow workflow = ((DefaultWorkflowExecutionContext) context).getWorkflow();
            List<WorkflowStep> steps = workflow.getSteps().toList();

            if (position < 0 || position >= steps.size()) {
                throw new IllegalStateException("Invalid position to restart workflow: " + position);
            }

            ((DefaultWorkflowExecutionContext) context).restart(position);
        } else {
            throw new IllegalArgumentException("Context must be DefaultWorkflowExecutionContext");
        }
    }

    @Override
    public void close() {
        // 无需释放资源
    }
}
