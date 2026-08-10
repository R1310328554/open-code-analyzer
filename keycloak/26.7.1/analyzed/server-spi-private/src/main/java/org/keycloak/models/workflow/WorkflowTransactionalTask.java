package org.keycloak.models.workflow;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;

import static org.keycloak.models.utils.KeycloakModelUtils.runJobInTransaction;

/**
 * 在独立事务中运行的工作流任务抽象基类。
 * <p>保存创建时的 {@link KeycloakContext}，通过 {@link KeycloakModelUtils#runJobInTransaction} 执行。</p>
 */
public abstract class WorkflowTransactionalTask implements Runnable, KeycloakSessionTask {

    private final KeycloakSessionFactory sessionFactory;
    private final KeycloakContext context;
    protected AtomicReference<Throwable> futureCancelled = new AtomicReference<Throwable>(null);

    /** 从当前会话捕获 SessionFactory 与 Context 供异步事务使用。 */
    public WorkflowTransactionalTask(KeycloakSession session) {
        Objects.requireNonNull(session, "KeycloakSession must not be null");
        this.sessionFactory = session.getKeycloakSessionFactory();
        this.context = session.getContext();
    }

    /** 在新事务中执行 {@link KeycloakSessionTask#run(KeycloakSession)}。 */
    @Override
    public void run() {
        runJobInTransaction(sessionFactory, context, this);
    }

    /** 记录取消原因（仅首次设置生效）。 */
    public void cancel(Throwable error) {
        futureCancelled.compareAndSet(null, error);
    }
}
