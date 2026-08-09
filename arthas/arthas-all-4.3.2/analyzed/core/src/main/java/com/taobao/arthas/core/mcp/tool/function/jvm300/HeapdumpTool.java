package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Heapdump MCP Tool：生成 JVM 堆转储（.hprof）文件。
 * <p>
 * 对应 {@code heapdump} 命令；支持 {@code --live} 仅 dump 存活对象，
 * 未指定路径时写入 {@code arthas-output/heapdump_<timestamp>.hprof}。
 */
public class HeapdumpTool extends AbstractArthasTool {

    /** 默认 dump 输出目录（当前工作目录下 arthas-output） */
    public static final String DEFAULT_DUMP_DIR = Paths.get("arthas-output").toAbsolutePath().toString().replace("\\", "/");

    /** 自动生成文件名时使用的时间戳格式 */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * heapdump 诊断工具
     * 支持:
     * - live: 是否只 dump 存活对象 (--live)
     * - filePath: 输出文件路径，若为空则使用默认临时文件
     */
    @Tool(
            name = "heapdump",
            description = "Heapdump 诊断工具: 生成 JVM heap dump，支持 --live 选项。对应 Arthas 的 heapdump 命令。"
    )
    public String heapdump(
            @ToolParam(description = "是否只 dump 存活对象 (--live)", required = false)
            Boolean live,

            @ToolParam(description = "指定输出文件路径，默认为当前工作目录下的arthas-output文件夹中的时间戳命名的.hprof文件", required = false)
            String filePath,

            ToolContext toolContext
    ) throws IOException {
        String finalFilePath;

        // 用户指定路径则规范化；否则创建默认目录并生成带时间戳的文件名
        if (filePath != null && !filePath.trim().isEmpty()) {
            finalFilePath = filePath.trim().replace("\\", "/");
        } else {
            Path defaultDir = Paths.get(DEFAULT_DUMP_DIR);
            if (!Files.exists(defaultDir)) {
                Files.createDirectories(defaultDir);
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String defaultFileName = String.format("heapdump_%s.hprof", timestamp);
            finalFilePath = Paths.get(DEFAULT_DUMP_DIR, defaultFileName).toString().replace("\\", "/");
        }

        StringBuilder cmd = buildCommand("heapdump");
        addFlag(cmd, "--live", live);
        cmd.append(" ").append(finalFilePath);

        return executeSync(toolContext, cmd.toString());
    }
}
