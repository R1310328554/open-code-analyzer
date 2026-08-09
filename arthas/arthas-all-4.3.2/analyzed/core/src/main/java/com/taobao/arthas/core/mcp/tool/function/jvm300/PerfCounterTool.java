package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * PerfCounter MCP Tool：查看 JVM 内部 Perf Counter 统计。
 * <p>
 * 对应 {@code perfcounter} 命令；{@code -d} 可输出更详细的 counter 信息。
 */
public class PerfCounterTool extends AbstractArthasTool {

    @Tool(
        name = "perfcounter",
        description = "PerfCounter 诊断工具: 查看 JVM Perf Counter 信息，对应 Arthas 的 perfcounter 命令。"
    )
    public String perfcounter(
            @ToolParam(description = "是否打印更多详情 (-d)", required = false)
            Boolean detailed,
            ToolContext toolContext
    ) {
        StringBuilder cmd = buildCommand("perfcounter");
        // -d：打印完整 perf counter 详情
        addFlag(cmd, "-d", detailed);
        return executeSync(toolContext, cmd.toString());
    }
}
