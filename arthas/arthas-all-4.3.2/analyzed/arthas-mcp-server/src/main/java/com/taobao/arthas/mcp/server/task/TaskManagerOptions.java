/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import java.time.Duration;

/**
 * {@link TaskManager} 的配置选项：TaskStore、消息队列与轮询参数。
 *
 * @author Yeaury
 */
public class TaskManagerOptions {

    private final TaskStore<?> taskStore;
    private final TaskMessageQueue messageQueue;
    private final Duration defaultPollInterval;
    private final Duration pollTimeout;

    private TaskManagerOptions(Builder builder) {
        this.taskStore = builder.taskStore;
        this.messageQueue = builder.messageQueue;
        this.defaultPollInterval = builder.defaultPollInterval != null
                ? builder.defaultPollInterval
                : Duration.ofMillis(TaskDefaults.DEFAULT_POLL_INTERVAL_MS);
        this.pollTimeout = builder.pollTimeout;
    }

    public TaskStore<?> taskStore() {
        return this.taskStore;
    }

    public TaskMessageQueue messageQueue() {
        return this.messageQueue;
    }

    public Duration defaultPollInterval() {
        return this.defaultPollInterval;
    }

    public Duration pollTimeout() {
        return this.pollTimeout;
    }

    /**
     * 若配置了 store 或 queue 则创建 {@link DefaultTaskManager}，否则返回 {@link NullTaskManager} 单例。
     */
    public TaskManager createTaskManager() {
        if (this.taskStore == null && this.messageQueue == null) {
            return NullTaskManager.getInstance();
        }
        return new DefaultTaskManager(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** {@link TaskManagerOptions} 的流式构建器。 */
    public static class Builder {
        private TaskStore<?> taskStore;
        private TaskMessageQueue messageQueue;
        private Duration defaultPollInterval;
        private Duration pollTimeout;

        private Builder() {}

        public Builder store(TaskStore<?> taskStore) {
            this.taskStore = taskStore;
            return this;
        }

        public Builder messageQueue(TaskMessageQueue messageQueue) {
            this.messageQueue = messageQueue;
            return this;
        }

        public Builder defaultPollInterval(Duration interval) {
            this.defaultPollInterval = interval;
            return this;
        }

        public Builder pollTimeout(Duration timeout) {
            this.pollTimeout = timeout;
            return this;
        }

        public TaskManagerOptions build() {
            return new TaskManagerOptions(this);
        }
    }
}
