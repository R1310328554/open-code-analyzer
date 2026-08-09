package com.taobao.arthas.mcp.server.tool.definition;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema.TaskSupportMode;

/**
 * MCP 工具元数据：名称、描述、入参 JSON Schema，以及流式与任务能力标志。
 * <p>
 * 由 {@link ToolDefinitions} 从 {@code @Tool} 注解方法构建，或手动通过 {@link Builder} 组装。
 */
public class ToolDefinition {
    /** 工具唯一名称，对应 MCP tools/call 请求中的 name 字段。 */
    private String name;

    /** 工具描述，帮助客户端模型选择合适工具。 */
    private String description;

    /** 入参 JSON Schema，描述 arguments 对象的结构与约束。 */
    private McpSchema.JsonSchema inputSchema;

    /** 是否支持流式返回（SSE 或类似机制）。 */
    private boolean streamable;
    
    /** 任务支持模式：禁止、可选或必须异步任务调用。 */
    private TaskSupportMode taskSupport;

    /** 完整构造：显式指定任务支持模式。 */
    public ToolDefinition(String name, String description,
                          McpSchema.JsonSchema inputSchema, boolean streamable, TaskSupportMode taskSupport) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.streamable = streamable;
        this.taskSupport = taskSupport;
    }
    
    /** 便捷构造：任务支持默认为 {@link TaskSupportMode#FORBIDDEN}。 */
    public ToolDefinition(String name, String description,
                          McpSchema.JsonSchema inputSchema, boolean streamable) {
        this(name, description, inputSchema, streamable, TaskSupportMode.FORBIDDEN);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public McpSchema.JsonSchema getInputSchema() {
        return inputSchema;
    }

    public boolean isStreamable() {
        return streamable;
    }

    public TaskSupportMode taskSupport() {
        return taskSupport;
    }

    /** 创建流式构建器，逐步填充各字段后 {@link Builder#build()}。 */
    public static Builder builder() {
        return new Builder();
    }

    /** {@link ToolDefinition} 的建造者，支持链式设置各元数据字段。 */
    public static final class Builder {

        private String name;

        private String description;

        private McpSchema.JsonSchema inputSchema;

        private boolean streamable;
        
        private TaskSupportMode taskSupport = TaskSupportMode.FORBIDDEN;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputSchema(McpSchema.JsonSchema inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }

        public Builder streamable(boolean streamable) {
            this.streamable = streamable;
            return this;
        }
        
        public Builder taskSupport(TaskSupportMode taskSupport) {
            this.taskSupport = taskSupport;
            return this;
        }

        public ToolDefinition build() {
            return new ToolDefinition(name, description, inputSchema, streamable, taskSupport);
        }
    }
}
