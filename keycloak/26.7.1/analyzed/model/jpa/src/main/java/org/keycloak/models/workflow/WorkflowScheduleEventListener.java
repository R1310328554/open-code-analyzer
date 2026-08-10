package org.keycloak.models.workflow;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterListener;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;
import org.keycloak.timer.TimerProvider;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.KeycloakModelUtils.runJobInTransaction;

/**
 * 工作流定时调度集群监听器：接收 {@link WorkflowScheduleClusterEvent} 并在各节点重调度本地 Timer。
 * <p>
 * 迁移完成后注册集群监听；通知在事务提交后发送，避免可串行化隔离级别下的死锁。
 */
public final class WorkflowScheduleEventListener implements ClusterListener, ProviderEventListener {

    private static final Logger logger = Logger.getLogger("org.keycloak.workflow.schedule");
    /** 集群通知通道键。 */
    static final String WORKFLOW_SCHEDULE_TASK_KEY = "workflow-schedule";

    private final KeycloakSessionFactory sessionFactory;

    public WorkflowScheduleEventListener(KeycloakSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void eventReceived(ClusterEvent event) {
        WorkflowScheduleClusterEvent workflowEvent = (WorkflowScheduleClusterEvent) event;
        logger.debugf("Received workflow schedule cluster event for workflow %s (removed=%s, interval=%d s)",
                workflowEvent.getWorkflowId(), workflowEvent.isRemoved(), workflowEvent.getIntervalSecs());

        runJobInTransaction(sessionFactory, session -> {
            RealmModel realm = session.realms().getRealm(workflowEvent.getRealmId());

            if (realm == null) {
                logger.debugf("Realm %s not found, ignoring workflow schedule cluster event", workflowEvent.getRealmId());
                return;
            }

            session.getContext().setRealm(realm);
            rescheduleWorkflow(session, workflowEvent);
        });
    }

    @Override
    public void onEvent(ProviderEvent event) {
        if (event instanceof PostMigrationEvent ev) {
            runJobInTransaction(ev.getFactory(), session -> {
                ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);

                if (clusterProvider != null) {
                    clusterProvider.registerListener(WORKFLOW_SCHEDULE_TASK_KEY, this);
                }
            });
        }
    }

    /** 根据集群事件取消旧任务并按新间隔注册 {@link ScheduledWorkflowRunner}。 */
    private void rescheduleWorkflow(KeycloakSession session, WorkflowScheduleClusterEvent workflowEvent) {
        TimerProvider timer = session.getProvider(TimerProvider.class);
        String workflowId = workflowEvent.getWorkflowId();
        String taskName = ScheduledWorkflowRunner.taskName(workflowId);

        timer.cancelTask(taskName);

        if (workflowEvent.isRemoved() || workflowEvent.getIntervalSecs() <= 0) {
            logger.debugf("Cancelled scheduled workflow task %s", workflowId);
            return;
        }

        int intervalSecs = workflowEvent.getIntervalSecs();
        int lastScheduleRun = workflowEvent.getLastScheduleRun();
        int initialDelaySecs = ScheduledWorkflowRunner.computeInitialDelay(lastScheduleRun, intervalSecs);
        String realmId = session.getContext().getRealm().getId();
        ScheduledWorkflowRunner runner = new ScheduledWorkflowRunner(workflowId, realmId, intervalSecs);
        timer.scheduleTask(runner, initialDelaySecs * 1000L, intervalSecs * 1000L);
        logger.debugf("Rescheduled workflow %s with interval %d s, initial delay %d s (cluster event)", workflowId, intervalSecs, initialDelaySecs);
    }

    /**
     * 向集群广播工作流 schedule 变更；在事务提交后执行 notify。
     *
     * @param session 当前会话
     * @param realmId realm ID
     * @param workflowId 工作流 ID
     * @param removed 是否取消调度
     * @param intervalSecs 新间隔（秒）
     * @param lastScheduleRun 上次运行 epoch 秒
     */
    void notifyCluster(KeycloakSession session, String realmId, String workflowId, boolean removed,
            int intervalSecs, int lastScheduleRun) {
        ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);

        if (clusterProvider != null) {
            WorkflowScheduleClusterEvent event = WorkflowScheduleClusterEvent.create(
                    realmId, workflowId, removed, intervalSecs, lastScheduleRun);
            // 仅在事务完成后发送通知，确保各节点加载最新数据，
            // 且在数据库使用可串行化隔离级别时避免死锁
            session.getTransactionManager().enlistAfterCompletion(new AbstractKeycloakTransaction() {
                @Override
                protected void commitImpl() {
                    clusterProvider.notify(WORKFLOW_SCHEDULE_TASK_KEY, event, true);
                }

                @Override
                protected void rollbackImpl() {
                    // NOOP
                }
            });
        }
    }
}
