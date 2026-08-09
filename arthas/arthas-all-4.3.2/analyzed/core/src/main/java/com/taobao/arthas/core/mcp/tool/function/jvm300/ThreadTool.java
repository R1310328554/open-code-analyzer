package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * Thread MCP Tool：查看线程状态、堆栈及阻塞关系。
 * <p>
 * 对应 {@code thread} 命令；支持按线程 ID、最忙 TopN（-n）、阻塞分析（-b）
 * 及显示全部匹配线程（--all）。
 */
public class ThreadTool extends AbstractArthasTool {

    /**
     * thread 诊断工具: 查看线程信息及堆栈
     * 支持:
     * - threadId: 线程 ID，required=false
     * - topN: 最忙前 N 个线程并打印堆栈 (-n)，required=false
     * - blocking: 是否查找阻塞其他线程的线程 (-b)，required=false
     * - all: 是否显示所有匹配线程 (--all)，required=false
     */
    @Tool(
        name = "thread",
        description = "Thread 诊断工具: 查看线程信息及堆栈，对应 Arthas 的 thread 命令。一次性输出结果。"
    )
    public String thread(
            @ToolParam(description = "线程 ID", required = false)
            Long threadId,

            @ToolParam(description = "最忙前 N 个线程并打印堆栈 (-n)", required = false)
            Integer topN,

            @ToolParam(description = "是否查找阻塞其他线程的线程 (-b)", required = false)
            Boolean blocking,

            @ToolParam(description = "是否显示所有匹配线程 (--all)", required = false)
            Boolean all,

            ToolContext toolContext
    ) {
        StringBuilder cmd = buildCommand("thread");

        addFlag(cmd, "-b", blocking);
        // -n：按 CPU 时间排序输出最忙的前 N 个线程
        if (topN != null && topN > 0) {
            cmd.append(" -n ").append(topN);
        }
        addFlag(cmd, "--all", all);
        // 末尾附加具体线程 ID，精确查看单线程堆栈
        if (threadId != null && threadId > 0) {
            cmd.append(" ").append(threadId);
        }

        logger.info("Executing thread command: {}", cmd.toString());
        return executeSync(toolContext, cmd.toString());
    }
}
