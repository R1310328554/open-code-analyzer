/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.server.McpNettyServerExchange;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.session.ArthasCommandContext;

import java.util.concurrent.CompletableFuture;

/**
 * Task 感知工具规格：将 {@link McpSchema.Tool} 定义与 Task 生命周期处理器绑定。
 * <p>
 * Task 支持模式（{@link McpSchema.TaskSupportMode}）：OPTIONAL（默认，向后兼容）、
 * REQUIRED（必须带 Task 元数据）、FORBIDDEN（禁用 Task）。
 *
 * @author Yeaury
 */
public final class TaskAwareToolSpecification {

    private final McpSchema.Tool tool;
    private final TriFunction<McpNettyServerExchange, ArthasCommandContext, McpSchema.CallToolRequest, CompletableFuture<McpSchema.CallToolResult>> callHandler;
    private final CreateTaskHandler createTaskHandler;
    private final GetTaskHandler getTaskHandler;
    private final GetTaskResultHandler getTaskResultHandler;

    private TaskAwareToolSpecification(
            McpSchema.Tool tool,
            TriFunction<McpNettyServerExchange, ArthasCommandContext, McpSchema.CallToolRequest, CompletableFuture<McpSchema.CallToolResult>> callHandler,
            CreateTaskHandler createTaskHandler,
            GetTaskHandler getTaskHandler,
            GetTaskResultHandler getTaskResultHandler) {
        this.tool = tool;
        this.callHandler = callHandler;
        this.createTaskHandler = createTaskHandler;
        this.getTaskHandler = getTaskHandler;
        this.getTaskResultHandler = getTaskResultHandler;
    }

    public McpSchema.Tool tool() {
        return tool;
    }

    public TriFunction<McpNettyServerExchange, ArthasCommandContext, McpSchema.CallToolRequest, CompletableFuture<McpSchema.CallToolResult>> callHandler() {
        return callHandler;
    }

    public CreateTaskHandler createTaskHandler() {
        return createTaskHandler;
    }

    public GetTaskHandler getTaskHandler() {
        return getTaskHandler;
    }

    public GetTaskResultHandler getTaskResultHandler() {
        return getTaskResultHandler;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** 构建 {@link TaskAwareToolSpecification} 的流式 Builder。 */
    public static class Builder extends AbstractTaskAwareToolSpecificationBuilder<Builder> {

        private TriFunction<McpNettyServerExchange, ArthasCommandContext, McpSchema.CallToolRequest, CompletableFuture<McpSchema.CallToolResult>> callHandler;
        private CreateTaskHandler createTaskHandler;
        private GetTaskHandler getTaskHandler;
        private GetTaskResultHandler getTaskResultHandler;

        public Builder callHandler(TriFunction<McpNettyServerExchange, ArthasCommandContext, McpSchema.CallToolRequest, CompletableFuture<McpSchema.CallToolResult>> callHandler) {
            this.callHandler = callHandler;
            return this;
        }

        public Builder createTaskHandler(CreateTaskHandler createTaskHandler) {
            this.createTaskHandler = createTaskHandler;
            return this;
        }

        public Builder getTaskHandler(GetTaskHandler getTaskHandler) {
            this.getTaskHandler = getTaskHandler;
            return this;
        }

        public Builder getTaskResultHandler(GetTaskResultHandler getTaskResultHandler) {
            this.getTaskResultHandler = getTaskResultHandler;
            return this;
        }

        public TaskAwareToolSpecification build() {
            validateCommonFields();
            if (createTaskHandler == null) {
                throw new IllegalArgumentException("createTaskHandler must not be null");
            }
            McpSchema.Tool tool = buildTool();
            return new TaskAwareToolSpecification(tool, callHandler, createTaskHandler, getTaskHandler, getTaskResultHandler);
        }
    }
}
