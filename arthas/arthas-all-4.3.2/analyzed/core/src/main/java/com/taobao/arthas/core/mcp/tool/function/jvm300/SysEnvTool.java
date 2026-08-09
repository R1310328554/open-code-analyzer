package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * SysEnv MCP Tool：查看操作系统环境变量。
 * <p>
 * 对应 {@code sysenv} 命令；不传 envName 时列出全部，否则返回单个变量值。
 */
public class SysEnvTool extends AbstractArthasTool {

    @Tool(
        name = "sysenv",
        description = "SysEnv 诊断工具: 查看系统环境变量，对应 Arthas 的 sysenv 命令。"
    )
    public String sysenv(
            @ToolParam(description = "环境变量名。若为空或空字符串，则查看所有变量；否则查看单个变量值。", required = false)
            String envName,
            ToolContext toolContext
    ) {
        StringBuilder cmd = buildCommand("sysenv");
        // 可选单个环境变量名；省略则 dump 全部
        if (envName != null && !envName.trim().isEmpty()) {
            cmd.append(" ").append(envName.trim());
        }
        return executeSync(toolContext, cmd.toString());
    }
}
