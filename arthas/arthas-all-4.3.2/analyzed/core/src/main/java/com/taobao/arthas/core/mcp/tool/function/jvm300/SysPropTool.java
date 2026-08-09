package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * SysProp MCP Tool：查看或修改 JVM 系统属性（-D）。
 * <p>
 * 对应 {@code sysprop} 命令；仅 propertyName 为查看，同时传 value 则尝试更新。
 */
public class SysPropTool extends AbstractArthasTool {

    @Tool(
        name = "sysprop",
        description = "SysProp 诊断工具: 查看或修改系统属性，对应 Arthas 的 sysprop 命令。"
    )
    public String sysprop(
            @ToolParam(description = "属性名", required = false)
            String propertyName,

            @ToolParam(description = "属性值；若指定则修改，否则查看", required = false)
            String propertyValue,

            ToolContext toolContext
    ) {
        StringBuilder cmd = buildCommand("sysprop");
        // name + value 组合触发写属性；仅 name 为只读查询
        if (propertyName != null && !propertyName.trim().isEmpty()) {
            cmd.append(" ").append(propertyName.trim());
            if (propertyValue != null && !propertyValue.trim().isEmpty()) {
                cmd.append(" ").append(propertyValue.trim());
            }
        }
        return executeSync(toolContext, cmd.toString());
    }
}
