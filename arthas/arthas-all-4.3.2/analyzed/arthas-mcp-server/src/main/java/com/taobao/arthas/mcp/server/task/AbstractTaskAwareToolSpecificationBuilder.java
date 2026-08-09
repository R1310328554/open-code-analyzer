/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

/**
 * 支持 MCP Task 模式的工具规格构建器抽象基类，采用自引用泛型实现链式 API。
 * <p>
 * 子类继承后可设置工具名、描述、输入 JSON Schema 及 {@link McpSchema.TaskSupportMode}。
 *
 * @param <T> 具体构建器类型
 * @author Yeaury
 */
public abstract class AbstractTaskAwareToolSpecificationBuilder<T extends AbstractTaskAwareToolSpecificationBuilder<T>> {

    /** MCP 工具名称。 */
    protected String name;
    /** 工具的人类可读描述。 */
    protected String description;
    /** 工具入参 JSON Schema。 */
    protected McpSchema.JsonSchema inputSchema;
    /** 工具是否支持/必须/禁止 Task 执行模式，默认可选。 */
    protected McpSchema.TaskSupportMode taskSupport = McpSchema.TaskSupportMode.OPTIONAL;

    @SuppressWarnings("unchecked")
    /** 安全的自引用转型，供链式方法返回具体构建器类型。 */
    protected T self() {
        return (T) this;
    }

    public T name(String name) {
        this.name = name;
        return self();
    }

    public T description(String description) {
        this.description = description;
        return self();
    }

    public T inputSchema(McpSchema.JsonSchema schema) {
        this.inputSchema = schema;
        return self();
    }

    public T taskSupport(McpSchema.TaskSupportMode mode) {
        this.taskSupport = mode;
        return self();
    }

    /** 以字符串形式设置 taskSupport：optional / required / forbidden。 */
    public T taskSupport(String mode) {
        if ("optional".equalsIgnoreCase(mode)) {
            this.taskSupport = McpSchema.TaskSupportMode.OPTIONAL;
        } else if ("required".equalsIgnoreCase(mode)) {
            this.taskSupport = McpSchema.TaskSupportMode.REQUIRED;
        } else if ("forbidden".equalsIgnoreCase(mode)) {
            this.taskSupport = McpSchema.TaskSupportMode.FORBIDDEN;
        } else {
            throw new IllegalArgumentException("Invalid taskSupport mode: " + mode);
        }
        return self();
    }

    /** 校验 name 非空且 inputSchema 已配置。 */
    protected void validateCommonFields() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name must not be null or empty");
        }
        if (inputSchema == null) {
            throw new IllegalArgumentException("Input schema must not be null");
        }
    }

    /** 根据已设字段组装 {@link McpSchema.Tool} 元数据。 */
    protected McpSchema.Tool buildTool() {
        return McpSchema.Tool.builder()
            .name(name)
            .description(description)
            .inputSchema(inputSchema)
            .execution(new McpSchema.ToolExecution(taskSupport))
            .build();
    }
}
