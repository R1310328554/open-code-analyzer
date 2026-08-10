package org.keycloak.models.workflow;


import java.util.concurrent.TimeoutException;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ExecutionResult;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

import static org.keycloak.models.workflow.Workflows.getStepProvider;

/**
 * 工作流执行事务任务：在集群锁保护下逐步运行工作流步骤。
 * <p>
 * 通过 {@link ClusterProvider#executeIfNotExecuted} 保证同一 execution 在集群中仅一个节点执行；
 * 支持延迟步骤调度、激活/完成事件发布，以及执行取消与超时检测。
 */
class RunWorkflowTask extends WorkflowTransactionalTask {

    private static final Logger log = Logger.getLogger(RunWorkflowTask.class);

    /** 工作流执行上下文（含 workflow、step、resource 等信息）。 */
    protected final DefaultWorkflowExecutionContext context;

    RunWorkflowTask(DefaultWorkflowExecutionContext context) {
        super(context.getSession());
        this.context = context;
    }

    /** 集群执行锁超时（秒）。 */
    private static final int EXECUTION_LOCK_TIMEOUT_SECS = 300;

    @Override
    public void run(KeycloakSession session) {

        DefaultWorkflowExecutionContext context = new DefaultWorkflowExecutionContext(session, this.context);
        String executionId = context.getExecutionId();

        ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);
        ExecutionResult<Void> result = clusterProvider.executeIfNotExecuted("wf-exec::" + executionId, EXECUTION_LOCK_TIMEOUT_SECS, () -> {
            if (context.getStep() != null) {
                WorkflowStateProvider stateProvider = session.getProvider(WorkflowStateProvider.class);
                WorkflowStateProvider.ScheduledStep scheduledStep = stateProvider.getScheduledStep(
                        context.getWorkflow().getId(), context.getResourceId());
                if (scheduledStep == null || !scheduledStep.stepId().equals(context.getStep().getId())) {
                    log.debugf("Execution %s for resource %s: DB state has changed (expected step %s), skipping",
                            executionId, context.getResourceId(), context.getStep().getProviderId());
                    return null;
                }
            }

            WorkflowStep nextStep = runCurrentStep(context);

            while (nextStep != null) {
                WorkflowStateProvider.ScheduleResult scheduleResult = scheduleStep(session, context, nextStep);
                if (scheduleResult == WorkflowStateProvider.ScheduleResult.CREATED && context.getEvent() != null) {
                    fireWorkflowActivated(session, context);
                }
                boolean isNextStepScheduled = DurationConverter.isPositiveDuration(nextStep.getAfter());
                if (isNextStepScheduled) {
                    fireWorkflowStepScheduled(session, context, nextStep);
                    return null;
                }
                nextStep = runWorkflowStep(context, nextStep);
            }

            completeWorkflowExecution(session, context);
            return null;
        });

        if (!result.isExecuted()) {
            log.debugf("Execution %s for resource %s already in progress on another node, skipping",
                    executionId, context.getResourceId());
        }
    }

    /**
     * 运行当前步骤；若上下文未指定步骤则取工作流第一步。
     *
     * @param context 执行上下文
     * @return 下一步骤，或 {@code null}
     */
    protected WorkflowStep runCurrentStep(DefaultWorkflowExecutionContext context) {
        if (context.getStep() != null) {
            return runWorkflowStep(context, context.getStep());
        }
        return context.getWorkflow().getSteps().findFirst().orElse(null);
    }

    /**
     * 在独立事务中执行单个工作流步骤，并发布成功/失败事件。
     *
     * @param context 执行上下文
     * @param step 待执行步骤
     * @return 步骤执行后确定的下一步，或 {@code null}
     */
    protected WorkflowStep runWorkflowStep(DefaultWorkflowExecutionContext context, WorkflowStep step) {
        String executionId = context.getExecutionId();
        String resourceId = context.getResourceId();
        Workflow workflow = context.getWorkflow();
        KeycloakSession s = context.getSession();

        log.debugf("Running step %s on resource %s (execution id: %s)", step.getProviderId(), resourceId, executionId);
        try {
            String nextStepId = KeycloakModelUtils.runJobInTransactionWithResult(s.getKeycloakSessionFactory(), s.getContext(), session -> {
                // 需用新 session 复制上下文以运行步骤 provider
                DefaultWorkflowExecutionContext stepContext = new DefaultWorkflowExecutionContext(session, context, step);
                // 运行步骤前检查工作流执行是否已被取消
                checkExecutionCancelled(step);
                getStepProvider(session, step).run(stepContext);
                // 步骤 provider 可能耗时较长，运行后再次检查是否已取消
                checkExecutionCancelled(step);
                WorkflowStep nextStep = stepContext.getNextStep();
                return nextStep != null ? nextStep.getId() : null;
            }, "Workflow step execution task");
            log.debugf("Step %s completed successfully (execution id: %s)", step.getProviderId(), executionId);
            // 发布工作流步骤已执行事件
            WorkflowProviderEvents.fireWorkflowStepExecutedEvent(s, workflow, step, resourceId, executionId);
            return nextStepId != null ? context.getWorkflow().getStepById(nextStepId) : null;
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Step %s failed (execution id: %s)");
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                sb.append(" - error message: %s");
                log.debugf(sb.toString(), step.getProviderId(), executionId, errorMessage);
            }
            else {
                log.debugf(sb.toString(), step.getProviderId(), executionId);
            }

            // 发布工作流步骤失败事件
            WorkflowProviderEvents.fireWorkflowStepFailedEvent(s, workflow, step, resourceId, executionId, errorMessage);

            throw e;
        }
    }

    /** 在事务中将下一步写入工作流状态表。 */
    private WorkflowStateProvider.ScheduleResult scheduleStep(KeycloakSession session, DefaultWorkflowExecutionContext context, WorkflowStep nextStep) {

        Workflow workflow = context.getWorkflow();
        String resourceId = context.getResourceId();
        String executionId = context.getExecutionId();

        return KeycloakModelUtils.runJobInTransactionWithResult(session.getKeycloakSessionFactory(), session.getContext(), s -> {
            WorkflowStateProvider stateProvider = s.getProvider(WorkflowStateProvider.class);
            return stateProvider.scheduleStep(workflow, nextStep, resourceId, executionId);
        }, "Workflow step scheduling task");
    }

    /** 发布工作流激活事件。 */
    private void fireWorkflowActivated(KeycloakSession session, DefaultWorkflowExecutionContext context) {
        log.debugf("Workflow '%s' activated for resource %s (execution id: %s)", context.getWorkflow().getName(),
                context.getResourceId(), context.getExecutionId());
        WorkflowProviderEvents.fireWorkflowActivatedEvent(session, context.getWorkflow(), context.getEvent().getResourceId(),
                context.getExecutionId(), context.getEvent().getEventProviderId());
    }

    /** 发布工作流步骤已调度事件。 */
    private void fireWorkflowStepScheduled(KeycloakSession session, DefaultWorkflowExecutionContext context, WorkflowStep nextStep) {
        log.debugf("Scheduled step %s to run in %s for resource %s (execution id: %s)",
                nextStep.getProviderId(), nextStep.getAfter(), context.getResourceId(), context.getExecutionId());
        long scheduledTime = System.currentTimeMillis() + DurationConverter.parseDuration(nextStep.getAfter()).toMillis();
        WorkflowProviderEvents.fireWorkflowStepScheduledEvent(session, context.getWorkflow(), nextStep, context.getResourceId(), context.getExecutionId(),
                scheduledTime, nextStep.getAfter());
    }

    /** 移除状态表记录并发布工作流完成事件。 */
    private void completeWorkflowExecution(KeycloakSession session, DefaultWorkflowExecutionContext context) {
        // 工作流执行完成：先删除状态表条目，再记录日志并发布事件
        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), session.getContext(), s -> {;
            WorkflowStateProvider stateProvider = s.getProvider(WorkflowStateProvider.class);
            stateProvider.remove(context.getExecutionId());
        });
        log.debugf("Workflow '%s' completed for resource %s (execution id: %s)", context.getWorkflow().getName(),
                context.getResourceId(), context.getExecutionId());
        WorkflowProviderEvents.fireWorkflowCompletedEvent(session, context.getWorkflow(), context.getResourceId(), context.getExecutionId());
    }

    /** 若 Future 已取消或超时，抛出运行时异常。 */
    private void checkExecutionCancelled(WorkflowStep step) {
        Throwable throwable = super.futureCancelled.get();
        if (super.futureCancelled.get() != null) {
            if (throwable instanceof TimeoutException || throwable.getCause() instanceof TimeoutException) {
                throw new RuntimeException("Workflow executor timed out during execution of step " + step.getProviderId(), throwable);
            } else {
                throw new RuntimeException("Workflow executor was cancelled during execution of step " + step.getProviderId(), throwable);
            }
        }
    }
}
