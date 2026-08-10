package org.keycloak.models.workflow;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.keycloak.Config.Scope;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.component.ComponentModel;
import org.keycloak.executors.ExecutorsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel.RealmRemovedEvent;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;

/**
 * 默认 {@link WorkflowProviderFactory}：装配 {@link WorkflowExecutor}、集群调度监听及迁移/realm 删除事件处理。
 */
public class DefaultWorkflowProviderFactory implements WorkflowProviderFactory<DefaultWorkflowProvider>, ProviderEventListener {

    static final String ID = "default";
    /** workflow 异步任务默认超时（毫秒）。 */
    private static final long DEFAULT_EXECUTOR_TASK_TIMEOUT = 5000L;

    private WorkflowExecutor executor;
    private WorkflowScheduleEventListener scheduleEventListener;
    private boolean blocking;
    private long taskTimeout;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public DefaultWorkflowProvider create(KeycloakSession session, ComponentModel model) {
        return new DefaultWorkflowProvider(session, executor);
    }

    @Override
    public DefaultWorkflowProvider create(KeycloakSession session) {
        return new DefaultWorkflowProvider(session, executor);
    }

    @Override
    public void init(Scope config) {
        blocking = config.getBoolean("executorBlocking", false);
        String executorTimeoutStr = config.get("executorTaskTimeout");
        taskTimeout = executorTimeoutStr == null ? DEFAULT_EXECUTOR_TASK_TIMEOUT : DurationConverter.parseDuration(executorTimeoutStr).toMillis();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        this.executor = new WorkflowExecutor(getTaskExecutor(factory), blocking, taskTimeout);
        this.scheduleEventListener = new WorkflowScheduleEventListener(factory);
        factory.register(this);
        factory.register(scheduleEventListener);
    }

    @Override
    public void onEvent(ProviderEvent event) {
        if (event instanceof PostMigrationEvent ev) {
            KeycloakModelUtils.runJobInTransaction(ev.getFactory(), session ->
                    session.realms().getRealmsStream().forEach(realm -> {
                        session.getContext().setRealm(realm);
                        DefaultWorkflowProvider provider = create(session);

                        try {
                            provider.getWorkflows().forEach(provider::rescheduleWorkflow);
                        } finally {
                            session.getContext().setRealm(null);
                            provider.close();
                        }
                    }));
        } else if (event instanceof RealmRemovedEvent ev) {
            KeycloakSession session = ev.getKeycloakSession();
            DefaultWorkflowProvider provider = create(session);

            try {
                provider.getWorkflows().forEach(provider::cancelScheduledWorkflow);
            } finally {
                provider.close();
            }
        }
    }


    /** 供 {@link DefaultWorkflowProvider} 通知集群调度变更。 */
    WorkflowScheduleEventListener getScheduleEventListener() {
        return scheduleEventListener;
    }

    @Override
    public void close() {

    }

    @Override
    public String getHelpText() {
        return null;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("executor-task-timeout")
                .type("long")
                .helpText("workflow 任务超时时间（毫秒），超时后标记为 timed out。")
                .defaultValue(DEFAULT_EXECUTOR_TASK_TIMEOUT)
                .add().build();
    }

    /** 获取 workflow 事件专用线程池。 */
    private ExecutorService getTaskExecutor(KeycloakSessionFactory factory) {
        return factory.getProviderFactory(ExecutorsProvider.class).create(null).getExecutor("workflow-event-executor");
    }
}
