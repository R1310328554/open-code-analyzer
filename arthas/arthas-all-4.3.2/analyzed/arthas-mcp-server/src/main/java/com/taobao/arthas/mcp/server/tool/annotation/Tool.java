package com.taobao.arthas.mcp.server.tool.annotation;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema.TaskSupportMode;

import java.lang.annotation.*;

/**
 * 标记可被 MCP 服务器暴露为工具的 Java 方法。
 * <p>
 * 配合 {@link ToolParam} 描述入参，由 {@code ToolDefinitions} 反射生成 {@code ToolDefinition}。
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {

    /** 工具在 MCP tools/list 中显示的名称；为空则使用方法名。 */
    String name() default "";

    /** 工具的人类可读描述，供 LLM 理解用途与调用时机。 */
    String description() default "";

    /** 是否支持流式输出（例如长时间运行的 Arthas 命令逐行推送结果）。 */
    boolean streamable() default false;
    
    /**
     * 任务支持模式，决定客户端能否以 MCP Task 异步方式调用该工具。
     *
     * <ul>
     *   <li>{@link TaskSupportMode#FORBIDDEN FORBIDDEN} - 不支持任务（默认）</li>
     *   <li>{@link TaskSupportMode#OPTIONAL OPTIONAL} - 可选支持任务</li>
     *   <li>{@link TaskSupportMode#REQUIRED REQUIRED} - 必须以任务模式调用</li>
     * </ul>
     *
     * @return 任务支持模式
     * @see <a href="https://modelcontextprotocol.io/specification/2025-11-25/basic/utilities/tasks">MCP Tasks Specification</a>
     */
    TaskSupportMode taskSupport() default TaskSupportMode.FORBIDDEN;

}
