/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 编排 Task 状态、side-channel 消息队列、轮询与处理器注册的核心接口。
 * <p>
 * 通过五个生命周期方法与协议层交互：
 * {@link #processInboundRequest}、{@link #processOutboundRequest}、
 * {@link #processInboundResponse}、{@link #processOutboundNotification}、{@link #onClose}。
 * 使用前须通过 {@link #bind} 绑定 {@link TaskManagerHost}。
 *
 * @author Yeaury
 * @see DefaultTaskManager
 * @see NullTaskManager
 */
public interface TaskManager {

    void bind(TaskManagerHost host);

    InboundRequestResult processInboundRequest(String requestMethod, Object requestParams,
                                                InboundRequestContext ctx);

    OutboundRequestResult processOutboundRequest(String requestMethod, Object requestParams,
                                                  RequestOptions options, Object messageId,
                                                  Consumer<Object> responseHandler,
                                                  Consumer<Throwable> errorHandler);

    InboundResponseResult processInboundResponse(Object responseResult, Object messageId);

    CompletableFuture<OutboundNotificationResult> processOutboundNotification(
            String notificationMethod, Object notification, NotificationOptions options);

    void onClose();

    Optional<TaskStore<?>> taskStore();

    Optional<TaskMessageQueue> messageQueue();

    Duration defaultPollInterval();

    // === 配套类型 ===

    /** 入站请求处理上下文：会话 ID 与通知/请求发送能力。 */
    class InboundRequestContext {
        private final String sessionId;
        private final NotificationSender sendNotification;
        private final RequestSender sendRequest;

        public InboundRequestContext(String sessionId,
                                     NotificationSender sendNotification,
                                     RequestSender sendRequest) {
            this.sessionId = sessionId;
            this.sendNotification = sendNotification;
            this.sendRequest = sendRequest;
        }

        public String sessionId() {
            return sessionId;
        }

        public NotificationSender sendNotification() {
            return sendNotification;
        }

        public RequestSender sendRequest() {
            return sendRequest;
        }
    }

    /** 发送出站通知的函数式接口。 */
    @FunctionalInterface
    interface NotificationSender {
        CompletableFuture<Void> send(Object notification, NotificationOptions options);
    }

    /** 发送出站请求并等待 typed 响应的函数式接口。 */
    @FunctionalInterface
    interface RequestSender {
        <T> CompletableFuture<T> send(Object request, Class<T> resultType, RequestOptions options);
    }

    /** {@link #processInboundRequest} 的返回值：封装 side-channel 回调与 Task 创建参数标志。 */
    class InboundRequestResult {
        private final Consumer<Object> sendNotification;
        private final RequestSender sendRequest;
        private final java.util.function.Function<Object, CompletableFuture<Boolean>> routeResponse;
        private final boolean hasTaskCreationParams;

        public InboundRequestResult(Consumer<Object> sendNotification,
                                     RequestSender sendRequest,
                                     java.util.function.Function<Object, CompletableFuture<Boolean>> routeResponse,
                                     boolean hasTaskCreationParams) {
            this.sendNotification = sendNotification;
            this.sendRequest = sendRequest;
            this.routeResponse = routeResponse;
            this.hasTaskCreationParams = hasTaskCreationParams;
        }

        public Consumer<Object> sendNotification() {
            return sendNotification;
        }

        public RequestSender sendRequest() {
            return sendRequest;
        }

        public java.util.function.Function<Object, CompletableFuture<Boolean>> routeResponse() {
            return routeResponse;
        }

        public boolean hasTaskCreationParams() {
            return hasTaskCreationParams;
        }
    }

    /** {@link #processOutboundRequest} 的返回值：指示请求是否已入队等待 Task 消费。 */
    class OutboundRequestResult {
        private final boolean queued;

        public OutboundRequestResult(boolean queued) {
            this.queued = queued;
        }

        public boolean queued() {
            return queued;
        }
    }

    /** {@link #processInboundResponse} 的返回值：指示响应是否已被 Task 侧消费。 */
    class InboundResponseResult {
        private final boolean consumed;

        public InboundResponseResult(boolean consumed) {
            this.consumed = consumed;
        }

        public boolean consumed() {
            return consumed;
        }
    }

    /** {@link #processOutboundNotification} 的返回值：指示通知是否入队及 JSON-RPC 封装体。 */
    class OutboundNotificationResult {
        private final boolean queued;
        private final Object jsonrpcNotification;

        public OutboundNotificationResult(boolean queued, Object jsonrpcNotification) {
            this.queued = queued;
            this.jsonrpcNotification = jsonrpcNotification;
        }

        public OutboundNotificationResult(boolean queued) {
            this(queued, null);
        }

        public boolean queued() {
            return queued;
        }

        public Object jsonrpcNotification() {
            return jsonrpcNotification;
        }
    }

    /** 出站请求的 Task 关联选项：TTL 与 relatedTask 元数据。 */
    class RequestOptions {
        private final TaskCreationParams task;
        private final RelatedTaskInfo relatedTask;

        public RequestOptions(TaskCreationParams task, RelatedTaskInfo relatedTask) {
            this.task = task;
            this.relatedTask = relatedTask;
        }

        public static RequestOptions empty() {
            return new RequestOptions(null, null);
        }

        public TaskCreationParams task() {
            return task;
        }

        public RelatedTaskInfo relatedTask() {
            return relatedTask;
        }
    }

    /** 出站通知的 relatedTask 元数据选项。 */
    class NotificationOptions {
        private final RelatedTaskInfo relatedTask;

        public NotificationOptions(RelatedTaskInfo relatedTask) {
            this.relatedTask = relatedTask;
        }

        public static NotificationOptions empty() {
            return new NotificationOptions(null);
        }
        public static NotificationOptions withRelatedTask(RelatedTaskInfo relatedTask) {
            return new NotificationOptions(relatedTask);
        }

        public RelatedTaskInfo relatedTask() {
            return relatedTask;
        }
    }

    /** Task 创建参数（当前仅 TTL）。 */
    class TaskCreationParams {
        private final Long ttl;

        public TaskCreationParams(Long ttl) {
            this.ttl = ttl;
        }

        public Long ttl() {
            return ttl;
        }
    }

    /** 关联 Task 标识，用于 _meta.relatedTask 元数据。 */
    class RelatedTaskInfo {
        private final String taskId;

        public RelatedTaskInfo(String taskId) {
            this.taskId = taskId;
        }

        public String taskId() {
            return taskId;
        }
    }
}
