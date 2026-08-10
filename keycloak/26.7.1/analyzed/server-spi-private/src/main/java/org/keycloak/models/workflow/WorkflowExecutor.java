package org.keycloak.models.workflow;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;

import org.jboss.logging.Logger;

/**
 * 工作流异步任务执行器：在线程池中提交 {@link WorkflowTask}，支持阻塞等待与超时取消。
 * <p>任务通过 {@link KeycloakTransaction} 在会话提交后执行。</p>
 */
final class WorkflowExecutor {

    private static final Logger log = Logger.getLogger(WorkflowExecutor.class);

    private final boolean blocking;
    private final ExecutorService taskExecutor;
    private final long taskTimeout;

    /**
     * @param taskExecutor 工作流任务线程池
     * @param blocking 是否在提交后阻塞等待完成
     * @param taskTimeout 单任务超时（毫秒）
     */
    WorkflowExecutor(ExecutorService taskExecutor, boolean blocking, long taskTimeout) {
        this.taskExecutor = taskExecutor;
        this.blocking = blocking;
        this.taskTimeout = taskTimeout;
    }

    /** 将工作流事务任务登记到会话的 after-completion 事务中。 */
    void runTask(KeycloakSession session, WorkflowTransactionalTask task) {
        enlistTransaction(session, new WorkflowTask(this, task));
    }

    /**
     * 异步提交任务；{@code blocking} 为 true 时等待完成。
     * @param task 待执行的工作流任务
     * @return 任务完成的 Future
     */
    CompletableFuture<Void> submit(WorkflowTask task) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(task, taskExecutor)
                .orTimeout(taskTimeout, TimeUnit.MILLISECONDS)
                .whenComplete((result, error) -> {
                    if (error instanceof TimeoutException) {
                        log.warnf("Timeout occurred while processing workflow task: %s", task);
                    } else if (error != null) {
                        log.warnf(error, "Error processing workflow task: %s", task);
                    }
                    task.cancel(error);
                });

        if (blocking) {
            future.join();
        }

        return future;
    }

    private void enlistTransaction(KeycloakSession session, KeycloakTransaction transaction) {
        session.getTransactionManager().enlistAfterCompletion(transaction);
    }
}
