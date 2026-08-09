/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Task 执行期间 side-channel 消息的异步队列。
 * <p>
 * 支持三类 {@link QueuedMessage}：Request（服务端→客户端，需回复）、
 * Notification（异步，无需回复）、Response（客户端回复，仅经 {@link #waitForResponse} 获取）。
 *
 * @author Yeaury
 */
public interface TaskMessageQueue {

    /** 将 Request、Response 或 Notification 入队。 */
    CompletableFuture<Void> enqueue(String taskId, QueuedMessage message);

    /** 取出下一条可投递消息（Request 或 Notification）；队列为空时返回 null。 */
    CompletableFuture<QueuedMessage> dequeue(String taskId);

    /** 批量取出所有可投递消息（Request 与 Notification）。 */
    CompletableFuture<List<QueuedMessage>> dequeueAll(String taskId);

    /** 阻塞等待与 {@code requestId} 匹配的 Response 入队，或超时。 */
    CompletableFuture<QueuedMessage.Response> waitForResponse(String taskId, Object requestId, Duration timeout);

    /** 清除指定 Task 的全部队列消息（过期或清理时调用）。 */
    CompletableFuture<Void> clearTask(String taskId);

    default CompletableFuture<Integer> getQueueSize(String taskId) {
        return CompletableFuture.completedFuture(0);
    }

    CompletableFuture<Void> shutdown();
}
