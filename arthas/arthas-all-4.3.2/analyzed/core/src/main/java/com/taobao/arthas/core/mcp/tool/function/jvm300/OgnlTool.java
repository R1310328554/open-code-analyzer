package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * OGNL MCP Tool：在目标 JVM 上下文中执行 OGNL 表达式。
 * <p>
 * 对应 {@code ognl} 命令；可指定 ClassLoader 与结果展开深度（-x）。
 * 常用于动态查看对象属性或调用方法（需开启 unsafe 等选项时谨慎使用）。
 */
public class OgnlTool extends AbstractArthasTool {

    @Tool(
        name = "ognl",
        description = "OGNL 诊断工具: 执行 OGNL 表达式，对应 Arthas 的 ognl 命令。"
    )
    public String ognl(
            @ToolParam(description = "OGNL 表达式")
            String expression,

            @ToolParam(description = "ClassLoader的hashcode（16进制），用于指定特定的ClassLoader", required = false)
            String classLoaderHash,

            @ToolParam(description = "ClassLoader的完整类名，如sun.misc.Launcher$AppClassLoader，可替代hashcode", required = false)
            String classLoaderClass,

            @ToolParam(description = "结果对象展开层次 (-x)，默认 1", required = false)
            Integer expandLevel,

            ToolContext toolContext
    ) {
        StringBuilder cmd = buildCommand("ognl");

        if (classLoaderHash != null && !classLoaderHash.trim().isEmpty()) {
            addParameter(cmd, "-c", classLoaderHash);
        } else if (classLoaderClass != null && !classLoaderClass.trim().isEmpty()) {
            addParameter(cmd, "--classLoaderClass", classLoaderClass);
        }

        // -x 控制返回对象属性遍历深度，默认由命令侧为 1
        if (expandLevel != null && expandLevel > 0) {
            cmd.append(" -x ").append(expandLevel);
        }

        addParameter(cmd, expression);

        return executeSync(toolContext, cmd.toString());
    }
}
