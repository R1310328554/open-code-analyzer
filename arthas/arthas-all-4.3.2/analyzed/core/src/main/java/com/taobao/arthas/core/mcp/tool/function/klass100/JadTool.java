package com.taobao.arthas.core.mcp.tool.function.klass100;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * Jad MCP Tool：反编译 JVM 中已加载类的字节码为 Java 源码。
 * <p>
 * 对应 {@code jad} 命令；支持通配/正则匹配、指定 ClassLoader、
 * 控制行号与 dump 目录等，便于排查线上实际运行的类实现。
 */
public class JadTool extends AbstractArthasTool {

    /**
     * jad 反编译工具：将运行中的 class 还原为可读 Java 代码
     */
    @Tool(
            name = "jad",
            description = "反编译指定已加载类的源码，将JVM中实际运行的class的bytecode反编译成java代码"
    )
    public String jad(
            @ToolParam(description = "类名表达式匹配，如java.lang.String或demo.MathGame")
            String classPattern,

            @ToolParam(description = "ClassLoader的hashcode（16进制），用于指定特定的ClassLoader", required = false)
            String classLoaderHash,

            @ToolParam(description = "ClassLoader的完整类名，如sun.misc.Launcher$AppClassLoader，可替代hashcode", required = false)
            String classLoaderClass,

            @ToolParam(description = "反编译时只显示源代码，默认false", required = false)
            Boolean sourceOnly,

            @ToolParam(description = "反编译时不显示行号，默认false", required = false)
            Boolean noLineNumber,

            @ToolParam(description = "开启正则表达式匹配，默认为通配符匹配，默认false", required = false)
            Boolean useRegex,

            @ToolParam(description = "指定dump class文件目录，默认会dump到logback.xml中配置的log目录下", required = false)
            String dumpDirectory,

            ToolContext toolContext) {
        
        StringBuilder cmd = buildCommand("jad");

        addParameter(cmd, classPattern);

        if (classLoaderHash != null && !classLoaderHash.trim().isEmpty()) {
            addParameter(cmd, "-c", classLoaderHash);
        } else if (classLoaderClass != null && !classLoaderClass.trim().isEmpty()) {
            addParameter(cmd, "--classLoaderClass", classLoaderClass);
        }

        addFlag(cmd, "--source-only", sourceOnly);
        addFlag(cmd, "-E", useRegex);
        
        // 显式关闭行号输出（jad 默认带行号）
        if (Boolean.TRUE.equals(noLineNumber)) {
            cmd.append(" --lineNumber false");
        }

        addParameter(cmd, "-d", dumpDirectory);
        
        return executeSync(toolContext, cmd.toString());
    }
}
