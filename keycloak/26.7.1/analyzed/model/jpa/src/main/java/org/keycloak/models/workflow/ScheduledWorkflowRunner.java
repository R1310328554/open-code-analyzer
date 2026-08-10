package org.keycloak.models.workflow;

import jakarta.ws.rs.BadRequestException;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ExecutionResult;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.workflows.WorkflowConstants;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TimerProvider;

import org.jboss.logging.Logger;

/**
 * 定时工作流运行器：按配置间隔周期性激活所有符合条件的资源。
 * <p>
 * 通过集群锁避免多节点重复执行；配置变更（禁用、删除 schedule、间隔变化）时自动取消或重调度任务。
 */
public class ScheduledWorkflowRunner implements ScheduledTask {

    private static final Logger log = Logger.getLogger("org.keycloak.workflow.schedule");

    /** 集群锁最小超时（秒）。 */
    private static final int MIN_LOCK_TIMEOUT_SECS = 30;

    /** 工作流组件 ID。 */
    private final String workflowId;
    /** 所属 realm ID。 */
    private final String realmId;
    /** 调度间隔（秒）。 */
    private final int intervalSecs;

    public ScheduledWorkflowRunner(String workflowId, String realmId, int intervalSecs) {
        this.workflowId = workflowId;
        this.realmId = realmId;
        this.intervalSecs = intervalSecs;
    }

    @Override
    public void run(KeycloakSession session) {
        RealmModel realm = session.realms().getRealm(realmId);

        if (realm == null) {
            log.warnf("Realm %s for scheduled workflow %s not found, cancelling task", realmId, workflowId);
            cancelTask(session);
            return;
        }

        session.getContext().setRealm(realm);
        WorkflowProvider provider = session.getProvider(WorkflowProvider.class);
        Workflow workflow;

        try {
            workflow = provider.getWorkflow(workflowId);
        } catch (BadRequestException e) {
            log.warnf("Scheduled workflow %s in realm %s not found, cancelling task", workflowId, realmId);
            cancelTask(session);
            return;
        }

        if (!workflow.isEnabled()) {
            log.debugf("Workflow '%s' in realm %s is disabled, cancelling scheduled task", workflow.getName(), realm.getName());
            cancelTask(session);
            return;
        }

        String currentSchedule = workflow.getConfig().getFirst(WorkflowConstants.CONFIG_SCHEDULE_AFTER);
        if (currentSchedule == null) {
            log.debugf("Workflow '%s' in realm %s no longer has a schedule, cancelling task", workflow.getName(), realm.getName());
            cancelTask(session);
            return;
        }

        int currentIntervalSecs = (int) DurationConverter.parseDuration(currentSchedule).toSeconds();
        if (currentIntervalSecs != intervalSecs) {
            log.debugf("Schedule interval for workflow '%s' in realm %s changed from %d to %d s, rescheduling",
                    workflow.getName(), realm.getName(), intervalSecs, currentIntervalSecs);
            cancelTask(session);
            scheduleAligned(session, workflow, currentIntervalSecs);
            return;
        }

        if (!isSchedulePeriod(workflow)) {
            log.debugf("Skipping scheduled workflow '%s' in realm %s, too soon since last run", workflow.getName(), realm.getName());
            return;
        }

        ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);
        String taskKey = workflowId + "::schedule";
        int lockTimeout = Math.max(MIN_LOCK_TIMEOUT_SECS, intervalSecs);

        ExecutionResult<Void> result = clusterProvider.executeIfNotExecuted(taskKey, lockTimeout, () -> {
            KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), s -> {
                RealmModel r = s.realms().getRealm(realmId);
                s.getContext().setRealm(r);
                updateLastScheduleRun(s);
            });

            log.debugf("Executing scheduled workflow '%s' in realm %s", workflow.getName(), realm.getName());

            try {
                provider.activateForAllEligibleResources(workflow);
            } catch (Exception e) {
                log.errorf(e, "Error while executing scheduled workflow %s in realm %s", workflow.getName(), realm.getName());
            }

            log.debugf("Finished executing scheduled workflow '%s' in realm %s", workflow.getName(), realm.getName());
            return null;
        });

        if (!result.isExecuted()) {
            log.debugf("Skipping scheduled workflow '%s' in realm %s, already in progress on another node", workflow.getName(), realm.getName());
        }
    }

    /** 判断是否已到下一调度周期（距上次运行至少 intervalSecs 秒）。 */
    private boolean isSchedulePeriod(Workflow workflow) {
        int lastRun = getLastScheduleRun(workflow);

        if (lastRun <= 0) {
            return true;
        }

        int elapsed = Time.currentTime() - lastRun;
        return elapsed >= (intervalSecs - 1);
    }

    /** 将本次运行时间写入工作流组件配置。 */
    private void updateLastScheduleRun(KeycloakSession session) {
        ComponentModel component = session.getContext().getRealm().getComponent(workflowId);
        component.put(WorkflowConstants.CONFIG_LAST_SCHEDULE_RUN, String.valueOf(Time.currentTime()));
        session.getContext().getRealm().updateComponent(component);
    }

    /** 取消当前定时任务。 */
    private void cancelTask(KeycloakSession session) {
        session.getProvider(TimerProvider.class).cancelTask(getTaskName());
    }

    /** 按新间隔重新注册对齐后的定时任务。 */
    private void scheduleAligned(KeycloakSession session, Workflow workflow, int newIntervalSecs) {
        TimerProvider timer = session.getProvider(TimerProvider.class);
        ScheduledWorkflowRunner newRunner = new ScheduledWorkflowRunner(workflowId, realmId, newIntervalSecs);
        long initialDelayMillis = computeInitialDelay(workflow, newIntervalSecs) * 1000L;
        timer.scheduleTask(newRunner, initialDelayMillis, newIntervalSecs * 1000L);
    }

    @Override
    public String getTaskName() {
        return taskName(workflowId);
    }

    /** 生成工作流定时任务的唯一名称。 */
    static String taskName(String workflowId) {
        return "workflow-" + workflowId;
    }

    /** 从工作流配置读取上次调度运行时间（epoch 秒）。 */
    static int getLastScheduleRun(Workflow workflow) {
        String val = workflow.getConfig().getFirst(WorkflowConstants.CONFIG_LAST_SCHEDULE_RUN);
        return val == null ? 0 : Integer.parseInt(val);
    }

    /** 根据工作流配置计算初始延迟（秒）。 */
    static int computeInitialDelay(Workflow workflow, int intervalSecs) {
        return computeInitialDelay(getLastScheduleRun(workflow), intervalSecs);
    }

    /**
     * 根据上次运行时间与间隔计算初始延迟（秒）。
     *
     * @param lastRunSecs 上次运行 epoch 秒，{@code <=0} 表示从未运行
     * @param intervalSecs 调度间隔（秒）
     * @return 距下次触发的延迟秒数
     */
    static int computeInitialDelay(int lastRunSecs, int intervalSecs) {
        if (lastRunSecs <= 0) {
            return intervalSecs;
        }

        int nextFireTime = lastRunSecs + intervalSecs;
        int delay = nextFireTime - Time.currentTime();
        return Math.max(0, delay);
    }
}
