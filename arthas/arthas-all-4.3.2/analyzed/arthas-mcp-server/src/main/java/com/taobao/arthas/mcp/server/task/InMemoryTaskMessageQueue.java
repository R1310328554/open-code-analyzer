/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * {@link TaskMessageQueue} 的内存实现，按 Task ID 维护双队列。
 * <p>
 * 可执行队列存放 Request/Notification（dequeue 消费）；响应队列仅由 {@link #waitForResponse} 按 requestId 匹配取出。
 *
 * @author Yeaury
 */
public class InMemoryTaskMessageQueue implements TaskMessageQueue {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryTaskMessageQueue.class);

    /** taskId -> 待处理的 Request/Notification 队列。 */
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<QueuedMessage>> actionableQueues
        = new ConcurrentHashMap<>();

    /** taskId -> 客户端响应队列（按 requestId 匹配）。 */
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<QueuedMessage.Response>> responseQueues
        = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> enqueue(String taskId, QueuedMessage message) {
        return CompletableFuture.runAsync(() -> {
            if (message instanceof QueuedMessage.Response) {
                QueuedMessage.Response response = (QueuedMessage.Response) message;
                responseQueues.computeIfAbsent(taskId, k -> new ConcurrentLinkedQueue<>())
                        .offer(response);
                logger.debug("Enqueued response for task {} (requestId: {})", taskId, response.requestId());
            } else {
                actionableQueues.computeIfAbsent(taskId, k -> new ConcurrentLinkedQueue<>())
                        .offer(message);
                logger.debug("Enqueued {} for task {}", message.getClass().getSimpleName(), taskId);
            }
        });
    }

    @Override
    public CompletableFuture<QueuedMessage> dequeue(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentLinkedQueue<QueuedMessage> queue = actionableQueues.get(taskId);
            if (queue == null || queue.isEmpty()) {
                return null;
            }
            QueuedMessage msg = queue.poll();
            if (msg != null) {
                logger.debug("Dequeued {} for task {}", msg.getClass().getSimpleName(), taskId);
            }
            return msg;
        });
    }

    @Override
    public CompletableFuture<List<QueuedMessage>> dequeueAll(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentLinkedQueue<QueuedMessage> queue = actionableQueues.get(taskId);
            if (queue == null || queue.isEmpty()) {
                return Collections.emptyList();
            }
            List<QueuedMessage> messages = new ArrayList<>();
            QueuedMessage msg;
            while ((msg = queue.poll()) != null) {
                messages.add(msg);
            }
            if (!messages.isEmpty()) {
                logger.debug("Dequeued {} messages for task {}", messages.size(), taskId);
            }
            return messages;
        });
    }

    @Override
    public CompletableFuture<QueuedMessage.Response> waitForResponse(String taskId, Object requestId, Duration timeout) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            long timeoutMs = timeout.toMillis();
            long pollInterval = TaskDefaults.RESPONSE_POLL_INTERVAL_MS;

            logger.debug("waitForResponse: Waiting for response to request {} for task {} (timeout: {}ms)",
                    requestId, taskId, timeoutMs);

            while (System.currentTimeMillis() - startTime < timeoutMs) {
                ConcurrentLinkedQueue<QueuedMessage.Response> queue = responseQueues.get(taskId);
                if (queue != null) {
                    for (QueuedMessage.Response response : queue) {
                        if (requestId.equals(response.requestId())) {
                            queue.remove(response);
                            logger.debug("waitForResponse: Found response for request {} in task {}",
                                    requestId, taskId);
                            return response;
                        }
                    }
                }
                try {
                    Thread.sleep(pollInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException("Interrupted while waiting for response", e);
                }
            }

            logger.warn("waitForResponse: Timeout waiting for response to request {} for task {}",
                    requestId, taskId);
            throw new CompletionException(new TimeoutException(
                    "Timeout waiting for response to request " + requestId + " for task " + taskId));
        });
    }

    @Override
    public CompletableFuture<Void> clearTask(String taskId) {
        return CompletableFuture.runAsync(() -> {
            ConcurrentLinkedQueue<QueuedMessage> actionableQueue = actionableQueues.remove(taskId);
            ConcurrentLinkedQueue<QueuedMessage.Response> responseQueue = responseQueues.remove(taskId);
            int totalCleared = 0;
            if (actionableQueue != null) totalCleared += actionableQueue.size();
            if (responseQueue != null) totalCleared += responseQueue.size();
            if (totalCleared > 0) {
                logger.debug("Cleared {} messages for task {}", totalCleared, taskId);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getQueueSize(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            int size = 0;
            ConcurrentLinkedQueue<QueuedMessage> actionableQueue = actionableQueues.get(taskId);
            if (actionableQueue != null) size += actionableQueue.size();
            ConcurrentLinkedQueue<QueuedMessage.Response> responseQueue = responseQueues.get(taskId);
            if (responseQueue != null) size += responseQueue.size();
            return size;
        });
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            int totalMessages = actionableQueues.values().stream().mapToInt(ConcurrentLinkedQueue::size).sum();
            totalMessages += responseQueues.values().stream().mapToInt(ConcurrentLinkedQueue::size).sum();
            actionableQueues.clear();
            responseQueues.clear();
            logger.info("TaskMessageQueue shut down (cleared {} messages)", totalMessages);
        });
    }

    /** 返回指定 Task 可执行队列长度（测试/监控用）。 */
    public int getActionableMessageCount(String taskId) {
        ConcurrentLinkedQueue<QueuedMessage> queue = actionableQueues.get(taskId);
        return queue != null ? queue.size() : 0;
    }

    /** 返回指定 Task 响应队列长度（测试/监控用）。 */
    public int getResponseMessageCount(String taskId) {
        ConcurrentLinkedQueue<QueuedMessage.Response> queue = responseQueues.get(taskId);
        return queue != null ? queue.size() : 0;
    }

    /** 返回所有 Task 的消息总数（测试/监控用）。 */
    public int getTotalMessageCount() {
        int total = actionableQueues.values().stream().mapToInt(ConcurrentLinkedQueue::size).sum();
        total += responseQueues.values().stream().mapToInt(ConcurrentLinkedQueue::size).sum();
        return total;
    }
}
