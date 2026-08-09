package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * VMOption MCP Tool：查看或更新 JVM 诊断 VM 选项（如 PrintGCDetails）。
 * <p>
 * 对应 {@code vmoption} 命令；仅 key 为查询，key + value 为运行时更新（若 JVM 允许）。
 */
public class VMOptionTool extends AbstractArthasTool {

    @Tool(
        name = "vmoption",
        description = "VMOption 诊断工具: 查看或更新 JVM VM options，对应 Arthas 的 vmoption 命令。"
    )
    public String vmoption(
            @ToolParam(description = "VM 选项名称，如 PrintGCDetails", required = false)
            String key,

            @ToolParam(description = "更新值，仅在更新时使用", required = false)
            String value,

            ToolContext toolContext
    ) {
        StringBuilder cmd = buildCommand("vmoption");
        // 提供 value 时尝试 set；否则只读 get
        if (key != null && !key.trim().isEmpty()) {
            cmd.append(" ").append(key.trim());
            if (value != null && !value.trim().isEmpty()) {
                cmd.append(" ").append(value.trim());
            }
        }
        return executeSync(toolContext, cmd.toString());
    }
}
