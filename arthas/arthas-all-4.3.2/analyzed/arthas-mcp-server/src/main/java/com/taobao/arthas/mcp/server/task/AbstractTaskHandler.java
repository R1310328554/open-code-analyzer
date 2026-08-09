/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Task 处理器抽象基类，统一创建 {@link TaskManager}、注册自定义方法并管理关闭流程。
 * <p>
 * 子类可覆写 {@link #findAndInvokeCustomHandler} 以拦截 tasks/get、tasks/result 等请求。
 *
 * @param <S> {@link TaskStore} 中存储的结果类型
 * @author Yeaury
 */
public abstract class AbstractTaskHandler<S extends McpSchema.Result> implements TaskManagerHost {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTaskHandler.class);

    /** 任务持久化存储；未配置时为 null。 */
    protected final TaskStore<S> taskStore;
    /** 任务编排器；未配置 Task 时使用 {@link NullTaskManager}。 */
    protected final TaskManager taskManager;
    /** 按 MCP 方法名注册的自定义 Task 请求处理器表。 */
    protected final TaskHandlerRegistry taskHandlerRegistry = new TaskHandlerRegistry();

    protected AbstractTaskHandler(TaskStore<S> taskStore, TaskManagerOptions taskOptions) {
        this.taskStore = taskStore;
        if (taskOptions != null && taskStore != null) {
            this.taskManager = taskOptions.createTaskManager();
            this.taskManager.bind(this);
            logger.info("TaskManager created: {}", this.taskManager.getClass().getSimpleName());
        } else {
            this.taskManager = NullTaskManager.getInstance();
            logger.info("Using NullTaskManager (tasks not configured)");
        }
    }

    @Override
    public void registerHandler(String method, TaskRequestHandler handler) {
        this.taskHandlerRegistry.registerHandler(method, handler);
        logger.debug("Registered task handler for method: {}", method);
    }

    @Override
    public <T extends McpSchema.Result> CompletableFuture<T> invokeCustomTaskHandler(
            String taskId, String method, McpSchema.Request request,
            TaskHandlerContext context, Class<T> resultType) {

        if (this.taskStore == null) {
            return CompletableFuture.completedFuture(null);
        }
        return this.taskStore.getTask(taskId, context.sessionId())
                .thenCompose(storeResult -> {
                    if (storeResult == null) {
                        logger.debug("invokeCustomTaskHandler: task not found for taskId={}", taskId);
                        return CompletableFuture.completedFuture(null);
                    }
                    return findAndInvokeCustomHandler(storeResult, method, request, context, resultType);
                })
                .exceptionally(ex -> {
                    logger.debug("invokeCustomTaskHandler: task lookup failed for taskId={}, returning null",
                            taskId, ex);
                    return null;
                });
    }

    /** 子类钩子：查找并调用工具专属的自定义 Task 处理器；默认返回 null 走内置逻辑。 */
    protected <T extends McpSchema.Result> CompletableFuture<T> findAndInvokeCustomHandler(
            GetTaskFromStoreResult storeResult, String method, McpSchema.Request request,
            TaskHandlerContext context, Class<T> resultType) {
        return CompletableFuture.completedFuture(null);
    }

    public TaskStore<S> getTaskStore() {
        return this.taskStore;
    }

    public TaskManager taskManager() {
        return this.taskManager;
    }

    /** 关闭 TaskManager 并等待 TaskStore 优雅停机。 */
    public void close() {
        if (this.taskManager != null) {
            try {
                this.taskManager.onClose();
                logger.info("TaskManager closed");
            } catch (Exception e) {
                logger.error("Error closing TaskManager", e);
            }
        }
        if (this.taskStore != null) {
            try {
                this.taskStore.shutdown().get(TaskDefaults.TASK_STORE_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                logger.info("TaskStore shutdown completed");
            } catch (Exception e) {
                logger.error("Error shutting down TaskStore", e);
            }
        }
    }

    /** 异步关闭：通知 TaskManager 并返回 TaskStore shutdown Future。 */
    public CompletableFuture<Void> closeGracefully() {
        if (this.taskManager != null) {
            this.taskManager.onClose();
        }
        return this.taskStore != null ? this.taskStore.shutdown() : CompletableFuture.completedFuture(null);
    }

    // ---------------------------------------
    // 处理器上下文工厂：为 Task 侧 RPC 封装 session 与发送函数
    // ---------------------------------------

    /** 构造匿名 {@link TaskManagerHost.TaskHandlerContext}，桥接请求/通知发送回调。 */
    protected static TaskManagerHost.TaskHandlerContext createTaskHandlerContext(
            String sessionId,
            TriFunction<String, Object, Class<? extends McpSchema.Result>, CompletableFuture<? extends McpSchema.Result>> requestSender,
            java.util.function.BiFunction<String, Object, CompletableFuture<Void>> notificationSender) {
        return new TaskManagerHost.TaskHandlerContext() {
            @Override
            public String sessionId() {
                return sessionId;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <R extends McpSchema.Result> CompletableFuture<R> sendRequest(
                    String reqMethod, Object reqParams, Class<R> resultType) {
                return (CompletableFuture<R>) requestSender.apply(reqMethod, reqParams, resultType);
            }

            @Override
            public CompletableFuture<Void> sendNotification(String notifMethod, Object notification) {
                return notificationSender.apply(notifMethod, notification);
            }
        };
    }
}
