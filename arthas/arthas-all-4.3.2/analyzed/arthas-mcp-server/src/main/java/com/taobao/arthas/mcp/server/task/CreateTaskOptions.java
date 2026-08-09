/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

/**
 * 创建 Task 时的不可变选项快照，由 {@link Builder} 组装后传入 {@link TaskStore#createTask}。
 *
 * @author Yeaury
 */
public class CreateTaskOptions {

    private final String sessionId;
    private final String taskId;
    private final Long requestedTtl;
    private final Long pollInterval;
    private final McpSchema.Request originatingRequest;
    private final Object context;

    private CreateTaskOptions(Builder builder) {
        this.sessionId = builder.sessionId;
        this.taskId = builder.taskId;
        this.requestedTtl = builder.requestedTtl;
        this.pollInterval = builder.pollInterval;
        this.originatingRequest = builder.originatingRequest;
        this.context = builder.context;
    }

    public String sessionId() {
        return sessionId;
    }

    public String taskId() {
        return taskId;
    }

    public Long requestedTtl() {
        return requestedTtl;
    }

    public Long pollInterval() {
        return pollInterval;
    }

    public McpSchema.Request originatingRequest() {
        return originatingRequest;
    }

    public Object context() {
        return context;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** 流式构建 {@link CreateTaskOptions}。 */
    public static class Builder {
        private String sessionId;
        private String taskId;
        private Long requestedTtl;
        private Long pollInterval;
        private McpSchema.Request originatingRequest;
        private Object context;

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        /** 设置 Task 生存时间（毫秒）。 */
        public Builder ttl(Long ttl) {
            this.requestedTtl = ttl;
            return this;
        }

        /** 设置客户端轮询 tasks/result 的建议间隔（毫秒）。 */
        public Builder pollInterval(Long pollInterval) {
            this.pollInterval = pollInterval;
            return this;
        }

        /** 关联触发创建的原始 MCP 请求，便于审计与自定义处理。 */
        public Builder originatingRequest(McpSchema.Request request) {
            this.originatingRequest = request;
            return this;
        }

        /** 附加不透明上下文对象，供 TaskStore 或自定义处理器读取。 */
        public Builder context(Object context) {
            this.context = context;
            return this;
        }

        /** 构建不可变选项实例。 */
        public CreateTaskOptions build() {
            return new CreateTaskOptions(this);
        }
    }
}
