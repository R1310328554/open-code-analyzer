package com.taobao.arthas.core.mcp.tool.function.klass100;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * Redefine MCP Tool：在 JVM 运行时替换已加载类的字节码（热更新）。
 * <p>
 * 对应 {@code redefine} 命令；传入本地 .class 文件路径即可覆盖同名类，
 * 无需重启进程。通常与 {@code dump}/{@code mc} 组合使用。
 */
public class RedefineTool extends AbstractArthasTool {

    /**
     * redefine 热替换：用磁盘上的 class 字节码覆盖 JVM 内已加载类
     */
    @Tool(
            name = "redefine",
            description = "重新加载类的字节码，允许在JVM运行时，重新加载已存在的类的字节码，实现热更新"
    )
    public String redefine(
            @ToolParam(description = "要重新定义的.class文件路径，支持多个文件，用空格分隔")
            String classFilePaths,

            @ToolParam(description = "ClassLoader的hashcode（16进制），用于指定特定的ClassLoader", required = false)
            String classLoaderHash,

            @ToolParam(description = "指定执行表达式的ClassLoader的class name，如sun.misc.Launcher$AppClassLoader，可替代hashcode", required = false)
            String classLoaderClass,

            ToolContext toolContext) {
        StringBuilder cmd = buildCommand("redefine");

        addParameter(cmd, classFilePaths);

        // 多 ClassLoader 加载同名类时需指定目标 Loader
        if (classLoaderHash != null && !classLoaderHash.trim().isEmpty()) {
            addParameter(cmd, "-c", classLoaderHash);
        } else if (classLoaderClass != null && !classLoaderClass.trim().isEmpty()) {
            addParameter(cmd, "--classLoaderClass", classLoaderClass);
        }

        return executeSync(toolContext, cmd.toString());
    }
}
