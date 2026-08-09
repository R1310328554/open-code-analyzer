package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;

/**
 * Jvm MCP Tool：查看当前 JVM 运行时概览信息。
 * <p>
 * 对应 {@code jvm} 命令，输出内存、GC、类加载、编译等 JVM 级指标。
 */
public class JvmTool extends AbstractArthasTool {

    @Tool(
        name = "jvm",
        description = "Jvm 诊断工具: 查看当前 JVM 运行时信息。对应 Arthas 的 jvm 命令。"
    )
    /** 同步执行 jvm 命令并返回诊断结果 JSON */
    public String jvm(ToolContext toolContext) {
        return executeSync(toolContext, "jvm");
    }
}
