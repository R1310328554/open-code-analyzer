package com.taobao.arthas.mcp.server.tool;

import com.taobao.arthas.mcp.server.protocol.spec.McpError;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.protocol.server.McpTransportContext;
import com.taobao.arthas.mcp.server.session.ArthasCommandContext;
import com.taobao.arthas.mcp.server.task.CreateTaskContext;
import com.taobao.arthas.mcp.server.task.CreateTaskHandler;
import com.taobao.arthas.mcp.server.util.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static com.taobao.arthas.mcp.server.tool.ToolContextKeys.*;

/**
 * 将 {@link ToolCallback} 适配为 {@link CreateTaskHandler} 的通用桥接器。
 * <p>
 * 负责创建 MCP Task、在独立 Arthas 会话中异步执行工具、处理取消与失败，并回写任务终态。
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2025-11-25/basic/utilities/tasks">MCP Tasks Specification</a>
 */
public class ToolCallbackCreateTaskHandler implements CreateTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(ToolCallbackCreateTaskHandler.class);
    
    /** 被包装的实际工具回调，执行具体的 Arthas 命令逻辑。 */
    private final ToolCallback toolCallback;

    // 专用 executor，避免 I/O 密集型任务污染 ForkJoinPool.commonPool
    private final Executor taskExecutor;

    /**
     * @param toolCallback 要异步执行的工具实例
     * @param taskExecutor 后台任务线程池，与 MCP 层并发限制配合使用
     */
    public ToolCallbackCreateTaskHandler(ToolCallback toolCallback, Executor taskExecutor) {
        this.toolCallback = toolCallback;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public CompletableFuture<McpSchema.CreateTaskResult> createTask(
            Map<String, Object> args,
            CreateTaskContext context) {

        logger.debug("Creating task for tool: {}", toolCallback.getToolDefinition().getName());

        // 前置检查：在创建 Task 之前判断并发限制，避免产生孤儿 Task。
        // 此时请求已经被应用层的并发限制，客户端可立刻感知并重试。
        if (context.isAtConcurrencyLimit()) {
            logger.warn("Concurrent task session limit reached, rejecting tool: {}",
                    toolCallback.getToolDefinition().getName());
            CompletableFuture<McpSchema.CreateTaskResult> rejected = new CompletableFuture<>();
            rejected.completeExceptionally(
                    McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
                            .message("concurrent task session limit reached")
                            .data("Server is at max concurrent task capacity. Retry after an existing task completes.")
                            .build()
            );
            return rejected;
        }

        return context.createTask(opts -> {
            // 使用默认配置，工具可以通过注解自定义 pollInterval 等
        }).thenCompose(task -> {
            String taskId = task.getTaskId();

            logger.info("Task created: {}, starting async tool execution", taskId);

            // 将 Task 提交到专用线程池（SynchronousQueue）。
            // 若 executor 拒绝（理论上不应发生，前置检查已拦截），
            // 作为安全兜底捕获并标记任务失败，避免孤儿 Task。
            try {
                CompletableFuture.runAsync(() -> {
                    executeToolAndUpdateTaskStatus(taskId, args, context);
                }, taskExecutor);
            } catch (RejectedExecutionException e) {
                logger.error("Task executor rejected task: {} (should not happen after pre-check)", taskId, e);
                context.failTask(taskId, new McpSchema.CallToolResult(
                        "Task rejected: executor at capacity", true, null))
                        .exceptionally(ex -> {
                            logger.error("Failed to mark rejected task as failed: {}", taskId, ex);
                            return null;
                        });
                // 返回已创建但即将失败的 Task，让客户端能感知
                return CompletableFuture.completedFuture(
                        new McpSchema.CreateTaskResult(task, null));
            }

            return CompletableFuture.completedFuture(
                new McpSchema.CreateTaskResult(task, null)
            );
        });
    }

    /**
     * 在后台线程执行工具，并根据结果或异常更新任务为完成或失败。
     * 
     * @param taskId 任务 ID
     * @param args 工具参数
     * @param context 任务上下文
     */
    private void executeToolAndUpdateTaskStatus(String taskId, Map<String, Object> args, CreateTaskContext context) {
        ArthasCommandContext isolatedContext = null;
        try {
            // 执行前检查任务是否已被取消
            Boolean alreadyCancelled = context.isCancellationRequested(taskId).join();
            if (Boolean.TRUE.equals(alreadyCancelled)) {
                logger.info("Task {} was cancelled before execution started, skipping", taskId);
                return;
            }

            logger.debug("Executing tool: {} for task: {}",
                toolCallback.getToolDefinition().getName(), taskId);

            // 为 task 创建独立的 session
            isolatedContext = context.createIsolatedTaskSession(taskId);
            logger.debug("Created isolated session for task: {}, arthasSessionId: {}",
                       taskId, isolatedContext.getArthasSessionId());

            // 使用独立的 context 构建工具上下文
            ToolContext enhancedContext = buildEnhancedToolContext(taskId, context, isolatedContext);

            // 调用工具方法（工具内部的轮询循环会检查取消状态）
            String toolInput = JsonParser.toJson(args);
            String resultJson = toolCallback.call(toolInput, enhancedContext);

            // 执行完成后再次检查取消状态
            Boolean cancelledAfter = context.isCancellationRequested(taskId).join();
            if (Boolean.TRUE.equals(cancelledAfter)) {
                logger.info("Task {} was cancelled during execution, interrupting job", taskId);
                interruptJob(isolatedContext);
                return;
            }

            // 解析结果为 CallToolResult
            McpSchema.CallToolResult result = parseToolResult(resultJson);

            // 检查工具返回的结果是否标记为 cancelled（由 StreamableToolUtils 设置）
            if (isResultCancelled(resultJson)) {
                logger.info("Task {} execution detected cancellation, interrupting job", taskId);
                interruptJob(isolatedContext);
                return;
            }

            // 根据结果类型更新任务状态
            if (Boolean.TRUE.equals(result.getIsError())) {
                // 工具返回错误结果，标记任务为失败
                String errorMessage = extractErrorMessage(result);
                context.failTask(taskId, new McpSchema.CallToolResult(errorMessage, true, null))
                    .exceptionally(ex -> {
                        logger.error("Failed to mark task as failed: {}", taskId, ex);
                        return null;
                    });
                logger.warn("Tool execution returned error for task: {}", taskId);
            } else {
                // 工具执行成功，完成任务
                context.completeTask(taskId, result)
                    .thenRun(() -> {
                        logger.info("Task completed successfully: {}", taskId);
                    })
                    .exceptionally(ex -> {
                        logger.error("Failed to update task completion: {}", taskId, ex);
                        return null;
                    });
            }

        } catch (Exception e) {
            logger.error("Tool execution failed for task: {}", taskId, e);

            // 标记任务失败（如果任务已被取消，updateTaskStatus 会静默忽略终态任务）
            context.failTask(taskId, new McpSchema.CallToolResult("Tool execution failed: " + e.getMessage(), true, null))
                .exceptionally(ex -> {
                    logger.error("Failed to update task failure: {}", taskId, ex);
                    return null;
                });
        } finally {
            // 清理独立的 session
            cleanupTaskSession(taskId, context);
        }
    }

    /** 中断正在运行的 Arthas 作业，通常在客户端取消任务时调用。 */
    private void interruptJob(ArthasCommandContext commandContext) {
        try {
            if (commandContext != null) {
                commandContext.interruptJob();
            }
        } catch (Exception e) {
            logger.warn("Failed to interrupt job: {}", e.getMessage());
        }
    }

    /** 解析工具返回 JSON 中的 {@code cancelled} 标志。 */
    @SuppressWarnings("unchecked")
    private boolean isResultCancelled(String resultJson) {
        try {
            Map<String, Object> resultMap = JsonParser.fromJson(resultJson, Map.class);
            return Boolean.TRUE.equals(resultMap.get("cancelled"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 清理 task 的独立 session，释放 attach 资源。
     */
    private void cleanupTaskSession(String taskId, CreateTaskContext context) {
        try {
            context.cleanupTaskSession(taskId);
            logger.debug("Cleaned up task session: {}", taskId);
        } catch (Exception e) {
            logger.warn("Failed to cleanup task session: {}, error={}", taskId, e.getMessage());
        }
    }

    /**
     * 构建增强的 {@link ToolContext}，注入任务 ID、隔离会话与传输层信息。
     */
    private ToolContext buildEnhancedToolContext(
            String taskId, 
            CreateTaskContext context, 
            ArthasCommandContext isolatedContext) {
        
        Map<String, Object> contextMap = new HashMap<>();

        contextMap.put(CREATE_TASK_CONTEXT, context);
        contextMap.put(TASK_ID, taskId);

        contextMap.put(COMMAND_CONTEXT, isolatedContext);

        if (context.exchange() != null) {
            contextMap.put(EXCHANGE, context.exchange());

            McpTransportContext transportContext = context.exchange().getTransportContext();
            if (transportContext != null) {
                contextMap.put(MCP_TRANSPORT_CONTEXT, transportContext);
            }
        }
        
        return new ToolContext(contextMap);
    }

    /** 将工具返回字符串解析为 {@link McpSchema.CallToolResult}；非 JSON 时包装为纯文本内容。 */
    private McpSchema.CallToolResult parseToolResult(String resultJson) {
        try {
            Map<String, Object> resultMap = JsonParser.fromJson(resultJson, Map.class);

            if (resultMap.containsKey("content")) {
                return JsonParser.fromJson(resultJson, McpSchema.CallToolResult.class);
            }

            McpSchema.TextContent textContent = new McpSchema.TextContent(resultJson);
            return new McpSchema.CallToolResult(
                java.util.Collections.singletonList(textContent),
                false,
                null
            );
            
        } catch (Exception e) {
            logger.debug("Failed to parse tool result as JSON, treating as plain text", e);
            
            McpSchema.TextContent textContent = new McpSchema.TextContent(resultJson);
            return new McpSchema.CallToolResult(
                java.util.Collections.singletonList(textContent),
                false,
                null
            );
        }
    }

    /** 从错误型 {@link McpSchema.CallToolResult} 中提取首条文本消息。 */
    private String extractErrorMessage(McpSchema.CallToolResult result) {
        if (result.getContent() != null && !result.getContent().isEmpty()) {
            McpSchema.Content firstContent = result.getContent().get(0);
            if (firstContent instanceof McpSchema.TextContent) {
                return ((McpSchema.TextContent) firstContent).getText();
            }
        }
        return "Tool execution failed";
    }
}
