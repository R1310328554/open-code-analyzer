package com.taobao.arthas.core.mcp.tool.function.basic1000;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;

/**
 * Version MCP Tool：查询当前 JVM 中运行的 Arthas 版本号。
 * <p>
 * 封装 {@code version} 命令，通过 {@link AbstractArthasTool#executeSync} 同步返回版本信息。
 */
public class VersionTool extends AbstractArthasTool {

    @Tool(
            name = "version",
            description = "Version 诊断工具: 查看当前 JVM 内运行的 Arthas 版本，对应 Arthas 的 version 命令。"
    )
    /**
     * 执行 version 命令并返回 JSON 格式结果。
     * @param toolContext MCP 工具上下文，含命令执行器与认证信息
     */
    public String version(ToolContext toolContext) {
        return executeSync(toolContext, "version");
    }
}
