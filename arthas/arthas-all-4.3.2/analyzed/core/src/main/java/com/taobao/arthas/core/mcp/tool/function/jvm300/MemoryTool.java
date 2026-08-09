package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;

/**
 * Memory MCP Tool：查看 JVM 各内存区域使用情况。
 * <p>
 * 对应 {@code memory} 命令，展示 heap/non-heap 及各内存池占用。
 */
public class MemoryTool extends AbstractArthasTool {

    @Tool(
        name = "memory",
        description = "Memory 诊断工具: 查看 JVM 内存使用情况，对应 Arthas 的 memory 命令。"
    )
    /** 同步执行 memory 命令 */
    public String memory(ToolContext toolContext) {
        return executeSync(toolContext, "memory");
    }
}
