package com.taobao.arthas.core.mcp.tool.function.basic1000;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.util.JsonParser;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;

import static com.taobao.arthas.core.mcp.tool.function.StreamableToolUtils.createCompletedResponse;
import static com.taobao.arthas.core.mcp.tool.function.StreamableToolUtils.createErrorResponse;

/**
 * Stop MCP Tool：安全关闭 Arthas Agent。
 * <p>
 * 对应 Arthas {@code stop} 命令；为避免 MCP 客户端收不到响应，
 * 先同步返回 JSON 结果，再在后台守护线程延迟执行实际 stop。
 */
public class StopTool extends AbstractArthasTool {

    /** 默认延迟关闭毫秒数，给 MCP 响应留出传输时间 */
    public static final int DEFAULT_SHUTDOWN_DELAY_MS = 1000;

    @Tool(
        name = "stop",
        description = "彻底停止 Arthas。注意停止之后不能再调用任何 tool 了。为了确保 MCP client 能收到返回结果，本 tool 会先返回，再延迟执行 stop。"
    )
    public String stop(
            @ToolParam(description = "延迟执行 stop 的毫秒数，默认 1000ms。用于确保 MCP client 收到返回结果。", required = false)
            Integer delayMs,
            ToolContext toolContext) {
        try {
            int shutdownDelayMs = getDefaultValue(delayMs, DEFAULT_SHUTDOWN_DELAY_MS);

            // 非流式上下文：stop 只需一次性 JSON 响应
            ToolExecutionContext execContext = new ToolExecutionContext(toolContext, false);
            scheduleStop(execContext, shutdownDelayMs);

            Map<String, Object> result = new HashMap<>();
            result.put("command", "stop");
            result.put("scheduled", true);
            result.put("delayMs", shutdownDelayMs);
            result.put("note", "Arthas 将在返回结果后停止，MCP 连接会断开。");
            return JsonParser.toJson(createCompletedResponse("Stop scheduled", result));
        } catch (Exception e) {
            logger.error("Error scheduling stop", e);
            return JsonParser.toJson(createErrorResponse("Error scheduling stop: " + e.getMessage()));
        }
    }

    /** 启动守护线程：可选 sleep 后同步执行 stop 命令 */
    private void scheduleStop(ToolExecutionContext execContext, int delayMs) {
        Object authSubject = execContext.getAuthSubject();
        String userId = execContext.getUserId();

        // 后台线程持有认证上下文，确保 stop 命令权限与调用方一致
        Thread shutdownThread = new Thread(() -> {
            try {
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                execContext.getCommandContext().getCommandExecutor()
                        .executeSync("stop", 300000L, null, authSubject, userId);
            } catch (Throwable t) {
                logger.error("Error executing stop command in background thread", t);
            }
        }, "arthas-mcp-stop");
        // 守护线程：JVM 退出时不阻塞进程结束
        shutdownThread.setDaemon(true);
        shutdownThread.start();
    }
}
