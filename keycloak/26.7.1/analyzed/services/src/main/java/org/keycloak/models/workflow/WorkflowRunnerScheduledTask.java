package org.keycloak.models.workflow;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.timer.ScheduledTask;

import org.jboss.logging.Logger;

/**
 * 工作流定时任务：按 Realm 逐个执行已调度的资源工作流步骤。
 * <p>实现 {@link ScheduledTask}，遍历所有 Realm 并在独立事务中调用 {@link WorkflowProvider#runScheduledSteps()}。</p>
 */
final class WorkflowRunnerScheduledTask implements ScheduledTask {

    private final Logger logger = Logger.getLogger(WorkflowRunnerScheduledTask.class);

    private final KeycloakSessionFactory sessionFactory;

    /** @param sessionFactory Keycloak 会话工厂 */
    WorkflowRunnerScheduledTask(KeycloakSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /** 遍历所有 Realm 并委托 {@link #runScheduledTasksOnRealm(String)} 执行定时步骤。 */
    @Override
    public void run(KeycloakSession session) {
        // TODO：Realm 与步骤数量较多时可能消耗大量 CPU/内存/网络，后续需分批与窗口间隔优化
        session.realms().getRealmsStream().map(RealmModel::getId).forEach(this::runScheduledTasksOnRealm);
    }

    /** 在独立事务中为指定 Realm 执行定时工作流步骤并发布成功事件。 */
    private void runScheduledTasksOnRealm(String id) {
        KeycloakModelUtils.runJobInTransaction(sessionFactory, (KeycloakSession session) -> {
            try {
                KeycloakContext context = session.getContext();
                RealmModel realm = session.realms().getRealm(id);

                context.setRealm(realm);
                session.getProvider(WorkflowProvider.class).runScheduledSteps();

                sessionFactory.publish(new WorkflowStepRunnerSuccessEvent(session));
            } catch (Exception e) {
                logger.errorf(e, "Failed to run workflow steps on realm with id '%s'", id);
            }
        });
    }

    /** @return 定时任务名称 {@code workflow-runner-task} */
    @Override
    public String getTaskName() {
        return "workflow-runner-task";
    }
}
