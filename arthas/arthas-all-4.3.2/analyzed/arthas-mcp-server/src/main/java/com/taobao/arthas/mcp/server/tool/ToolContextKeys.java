package com.taobao.arthas.mcp.server.tool;

/**
 * {@link ToolContext} 中使用的键名常量，避免魔法字符串散落各处。
 */
public final class ToolContextKeys {

    /** MCP 请求/响应交换对象，用于访问传输层与会话信息。 */
    public static final String EXCHANGE = "exchange";

    /** Arthas 命令会话上下文，承载 attach 后的命令执行环境。 */
    public static final String COMMAND_CONTEXT = "commandContext";

    /** MCP 传输层上下文，例如 HTTP 头、客户端标识等。 */
    public static final String MCP_TRANSPORT_CONTEXT = "mcpTransportContext";

    /** 进度通知令牌，用于向客户端推送长时间运行工具的进度。 */
    public static final String PROGRESS_TOKEN = "progressToken";

    /** 异步任务 ID，与 MCP Tasks 规范中的 task 标识对应。 */
    public static final String TASK_ID = "taskId";

    /** 创建任务时的上下文对象，供工具在 task 模式下回调任务状态。 */
    public static final String CREATE_TASK_CONTEXT = "createTaskContext";

    private ToolContextKeys() {
    }
}
