package com.taobao.arthas.core.mcp.tool.function.klass100;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

import java.nio.file.Paths;

/**
 * Memory Compiler（mc）MCP Tool：在目标 JVM 进程内编译 .java 源文件。
 * <p>
 * 对应 {@code mc} 命令；编译产物可配合 {@code redefine} 做热替换。
 * 未指定输出目录时使用工作目录下 {@code arthas-output}。
 */
public class MemoryCompilerTool extends AbstractArthasTool {

    /** 默认 .class 输出目录（绝对路径） */
    public static final String DEFAULT_DUMP_DIR = Paths.get("arthas-output").toAbsolutePath().toString();

    /**
     * mc 内存编译器：将磁盘上的 Java 源文件编译为字节码
     */
    @Tool(
            name = "mc",
            description = "Memory Compiler/内存编译器，编译.java文件生成.class"
    )
    public String mc(
            @ToolParam(description = "要编译的.java文件路径，支持多个文件，用空格分隔")
            String javaFilePaths,

            @ToolParam(description = "ClassLoader的hashcode（16进制），用于指定特定的ClassLoader", required = false)
            String classLoaderHash,

            @ToolParam(description = "ClassLoader的完整类名，如sun.misc.Launcher$AppClassLoader，可替代hashcode", required = false)
            String classLoaderClass,

            @ToolParam(description = "指定输出目录，默认为工作目录下arthas-output文件夹", required = false)
            String outputDir,

            ToolContext toolContext) {
        StringBuilder cmd = buildCommand("mc");

        addParameter(cmd, javaFilePaths);

        // 编译 classpath 依赖目标 ClassLoader
        if (classLoaderHash != null && !classLoaderHash.trim().isEmpty()) {
            addParameter(cmd, "-c", classLoaderHash);
        } else if (classLoaderClass != null && !classLoaderClass.trim().isEmpty()) {
            addParameter(cmd, "--classLoaderClass", classLoaderClass);
        }

        // 未指定 -d 时写入默认 arthas-output
        if (outputDir != null && !outputDir.trim().isEmpty()) {
            addParameter(cmd, "-d", outputDir);
        } else {
            cmd.append(" -d ").append(DEFAULT_DUMP_DIR);
        }

        return executeSync(toolContext, cmd.toString());
    }
}
