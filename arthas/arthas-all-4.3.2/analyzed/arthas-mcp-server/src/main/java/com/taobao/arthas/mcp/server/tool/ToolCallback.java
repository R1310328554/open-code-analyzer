package com.taobao.arthas.mcp.server.tool;

import com.taobao.arthas.mcp.server.tool.definition.ToolDefinition;

/**
 * MCP 工具回调接口，定义工具元数据查询与同步/异步执行入口。
 * <p>
 * 实现类负责将 JSON 入参反序列化、调用底层方法，并将返回值序列化为 MCP 协议可消费的字符串。
 */
public interface ToolCallback {

    /** 返回当前工具的名称、描述、入参 Schema 及任务/流式能力等元数据。 */
    ToolDefinition getToolDefinition();

    /** 在无额外上下文时执行工具，{@code toolInput} 为 JSON 字符串。 */
    String call(String toolInput);

    /**
     * 在携带 {@link ToolContext} 时执行工具，便于访问会话、任务 ID、传输层上下文等运行时信息。
     *
     * @param toolInput 工具入参 JSON
     * @param toolContext 执行上下文，可为 {@code null}
     */
    String call(String toolInput, ToolContext toolContext);
}
