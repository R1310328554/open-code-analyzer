package org.keycloak.models.workflow;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.keycloak.common.util.Time;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 工作流异步任务包装器，在父事务提交后提交至 {@link WorkflowExecutor} 执行。
 * <p>实现 {@link Runnable} 与 {@link AbstractKeycloakTransaction}，支持取消与状态查询。</p>
 */
public final class WorkflowTask extends AbstractKeycloakTransaction implements Runnable {

    private final WorkflowTransactionalTask task;
    private final WorkflowExecutor executor;
    private final String id;
    private CompletableFuture<Void> future;
    private long startTime;
    private AtomicReference<Thread> thread;

    /** 构造异步任务，生成唯一 ID 并绑定执行器与事务性任务。 */
    WorkflowTask(WorkflowExecutor executor, WorkflowTransactionalTask task) {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(task, "task");
        this.executor = executor;
        this.task = task;
        id = KeycloakModelUtils.generateId();
    }

    /** 在持有线程引用后执行底层任务（防止重复运行）。 */
    @Override
    public void run() {
        if (thread.compareAndSet(null, Thread.currentThread())) {
            task.run();
        }
    }

    /** 父事务提交时将任务提交至执行器。 */
    @Override
    protected void commitImpl() {
        startTime = Time.currentTimeMillis();
        thread = new AtomicReference<>();
        future = executor.submit(this);
    }

    /** 父事务回滚时标记 future 为失败。 */
    @Override
    protected void rollbackImpl() {
        future = CompletableFuture.failedFuture(new RuntimeException("Parent transaction rolled back"));
    }

    @Override
    public boolean isActive() {
        return future != null && !future.isDone();
    }

    /** 任务是否尚未提交至执行器。 */
    public boolean isScheduled() {
        return future == null;
    }

    public boolean isDone() {
        return future != null && future.isDone();
    }

    public boolean isCompletedExceptionally() {
        return future != null && future.isCompletedExceptionally();
    }

    public Throwable getThrowable() {
        if (future != null && future.isCompletedExceptionally()) {
            try {
                future.join();
            } catch (Throwable t) {
                return t;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String status = isScheduled() ? "SCHEDULED" : isActive() ? "ACTIVE" : isCompletedExceptionally() ? "FAILED" : "SUCCESS";
        return "id: " + id + ", executionTime: " + (System.currentTimeMillis() - startTime) + "ms , status: " + status + ", task: [" + task.toString() + "]";
    }

    /** 取消任务并中断执行线程。 */
    public void cancel(Throwable error) {
        this.task.cancel(error);
        if (future != null) {
            future.cancel(true);
            if (thread.get() != null) {
                thread.get().interrupt();
            }
        }
    }
}
