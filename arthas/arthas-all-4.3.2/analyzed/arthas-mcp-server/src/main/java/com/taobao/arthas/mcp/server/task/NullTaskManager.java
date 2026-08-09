/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 未配置 Task 支持时使用的空实现 {@link TaskManager}。
 * <p>
 * 所有入站/出站 Task 相关钩子均透传或返回“未处理”，不注册 tasks/* 处理器。
 *
 * @author Yeaury
 */
final class NullTaskManager implements TaskManager {

    private static final NullTaskManager INSTANCE = new NullTaskManager();

    private NullTaskManager() {}

    /** 返回单例 NullTaskManager。 */
    static TaskManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void bind(TaskManagerHost host) {
    }

    @Override
    public InboundRequestResult processInboundRequest(String requestMethod, Object requestParams,
                                                       InboundRequestContext ctx) {
        return new InboundRequestResult(
                notification -> {},
                ctx.sendRequest(),
                response -> CompletableFuture.completedFuture(false),
                false
        );
    }

    @Override
    public OutboundRequestResult processOutboundRequest(String requestMethod, Object requestParams,
                                                         RequestOptions options, Object messageId,
                                                         Consumer<Object> responseHandler,
                                                         Consumer<Throwable> errorHandler) {
        return new OutboundRequestResult(false);
    }

    @Override
    public InboundResponseResult processInboundResponse(Object responseResult, Object messageId) {
        return new InboundResponseResult(false);
    }

    @Override
    public CompletableFuture<OutboundNotificationResult> processOutboundNotification(
            String notificationMethod, Object notification, NotificationOptions options) {
        return CompletableFuture.completedFuture(new OutboundNotificationResult(false));
    }

    @Override
    public void onClose() {
    }

    @Override
    public Optional<TaskStore<?>> taskStore() {
        return Optional.empty();
    }

    @Override
    public Optional<TaskMessageQueue> messageQueue() {
        return Optional.empty();
    }

    @Override
    public Duration defaultPollInterval() {
        return Duration.ofMillis(TaskDefaults.DEFAULT_POLL_INTERVAL_MS);
    }
}
