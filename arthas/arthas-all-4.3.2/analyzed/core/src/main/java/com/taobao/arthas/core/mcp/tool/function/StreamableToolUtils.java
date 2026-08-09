package com.taobao.arthas.core.mcp.tool.function;

import com.taobao.arthas.core.command.model.*;
import com.taobao.arthas.mcp.server.session.ArthasCommandContext;
import com.taobao.arthas.mcp.server.protocol.server.McpNettyServerExchange;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流式（Streamable）MCP 工具的工具类。
 * <p>
 * 提供异步命令结果的轮询收集、进度通知、错误检测与取消支持；
 * 供 {@link AbstractArthasTool#executeStreamable} 及各类需持续输出的 MCP 工具调用。
 * 
 * @author Yeaury
 */
public final class StreamableToolUtils {

    private static final Logger logger = LoggerFactory.getLogger(StreamableToolUtils.class);

    private static final int DEFAULT_POLL_INTERVAL_MS = 100;    // 默认轮询间隔100ms

    private static final int ERROR_RETRY_INTERVAL_MS = 500;     // 错误重试间隔500ms

    public static final long DEFAULT_TIMEOUT_MS = 30000L;      // 默认超时时间30秒

    private static final int MAX_ERROR_RETRIES = 10;            // 最大错误重试次数

    /** 命令完成判定：连续检测到 {@code ALLOW_INPUT} 的最小次数 */
    public static final int MIN_ALLOW_INPUT_COUNT_TO_COMPLETE = 2;

    /**
     * 取消检查器函数式接口。
     *
     * <p>用于在轮询循环中定期检查任务是否已被取消。
     */
    @FunctionalInterface
    public interface CancellationChecker {

        boolean isCancelled();
    }

    /** 工具类不可实例化 */
    private StreamableToolUtils() {
    }

    /**
     * 同步执行命令并收集所有结果，支持进度通知
     *
     * @param exchange MCP交换器，用于发送进度通知
     * @param commandContext 命令上下文
     * @param expectedResultCount 预期结果数量
     * @param intervalMs 轮询间隔
     * @param timeoutMs 超时时间(毫秒)
     * @param progressToken 进度令牌
     * @return 包含所有结果的Map，如果执行失败返回null
     */
    public static Map<String, Object> executeAndCollectResults(McpNettyServerExchange exchange,
                                                             ArthasCommandContext commandContext,
                                                             Integer expectedResultCount, Integer intervalMs,
                                                             Integer timeoutMs,
                                                             String progressToken) {
        return executeAndCollectResults(exchange, commandContext, expectedResultCount, intervalMs, timeoutMs, progressToken, null);
    }

    /**
     * 同步执行命令并收集所有结果，支持进度通知和取消检查
     *
     * @param exchange MCP交换器，用于发送进度通知
     * @param commandContext 命令上下文
     * @param expectedResultCount 预期结果数量
     * @param intervalMs 轮询间隔
     * @param timeoutMs 超时时间(毫秒)
     * @param progressToken 进度令牌
     * @param cancellationChecker 取消检查器，为 null 时不检查取消
     * @return 包含所有结果的Map，取消时返回带 cancelled 标记的结果，执行失败返回null
     */
    public static Map<String, Object> executeAndCollectResults(McpNettyServerExchange exchange,
                                                             ArthasCommandContext commandContext,
                                                             Integer expectedResultCount, Integer intervalMs,
                                                             Integer timeoutMs,
                                                             String progressToken,
                                                             CancellationChecker cancellationChecker) {
        List<Object> allResults = new ArrayList<>();
        int errorRetries = 0;
        int allowInputCount = 0;
        int totalResultCount = 0;
        
        // 轮询间隔使用命令执行间隙的 1/10,事件驱动则在命令中自定义默认轮询间隔
        // 工具中默认轮询间隔为200ms
        int pullIntervalMs = (intervalMs != null && intervalMs > 0) ? intervalMs : DEFAULT_POLL_INTERVAL_MS;
        
        // 计算截止时间
        // 如果没有指定超时时间，则使用默认超时时间
        long executionTimeoutMs = (timeoutMs != null && timeoutMs > 0) ? timeoutMs : DEFAULT_TIMEOUT_MS;
        long deadline = System.currentTimeMillis() + executionTimeoutMs;
        boolean timedOut = false;

        try {
            while (System.currentTimeMillis() < deadline) {
                // 检查任务是否已被取消
                if (cancellationChecker != null && cancellationChecker.isCancelled()) {
                    logger.info("Task cancellation detected, stopping command execution");
                    return createCancelledResult(allResults, totalResultCount);
                }

                try {
                    Map<String, Object> results = commandContext.pullResults();
                    if (results == null) {
                        Thread.sleep(pullIntervalMs);
                        continue;
                    }
                    errorRetries = 0;

                    // 检查是否有错误消息
                    String errorMessage = checkForErrorMessages(results);
                    if (errorMessage != null) {
                        logger.warn("Command execution failed with error: {}", errorMessage);
                        return createErrorResponseWithResults(errorMessage, allResults, totalResultCount);
                    }

                    Map<String, Object> filteredResults = filterCommandSpecificResults(results);
                    List<Object> currentBatchResults = getCommandSpecificResults(filteredResults);
                    
                    if (currentBatchResults != null && !currentBatchResults.isEmpty()) {
                        allResults.addAll(currentBatchResults);
                        totalResultCount += currentBatchResults.size();
                        logger.debug("Collected {} results, total: {}", currentBatchResults.size(), totalResultCount);

                        if (exchange != null) {
                            sendProgressNotification(exchange, totalResultCount, 
                                                    expectedResultCount != null ? expectedResultCount : totalResultCount, 
                                                    progressToken);
                        }
                    }

                    boolean commandCompleted = checkCommandCompletion(results, allowInputCount);
                    if (commandCompleted) {
                        allowInputCount++;
                    }

                    String jobStatus = (String) results.get("jobStatus");
                    
                    // 判断是否应该结束
                    // 如果是TERMINATED状态，或者命令已完成且允许输入次数大于等于2，或者实际结果数量达到预期结果数量
                    boolean hasExpectedResultCount = (expectedResultCount != null);
                    boolean reachedExpectedResultCount = hasExpectedResultCount && totalResultCount >= expectedResultCount;
                    boolean allowInputCompletion = !hasExpectedResultCount
                            && commandCompleted
                            && allowInputCount >= MIN_ALLOW_INPUT_COUNT_TO_COMPLETE;

                    if ("TERMINATED".equals(jobStatus) || allowInputCompletion || reachedExpectedResultCount) {
                        logger.info("Command completed. Total results collected: {}, Expected: {}", totalResultCount, expectedResultCount);
                        break;
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Command execution interrupted");
                    return null;
                } catch (Exception e) {
                    if (++errorRetries >= MAX_ERROR_RETRIES) {
                        logger.error("Maximum error retries exceeded", e);
                        return null;
                    }
                    
                    try {
                        Thread.sleep(ERROR_RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
            
            // 检查是否超时
            if (System.currentTimeMillis() >= deadline) {
                timedOut = true;
            }

            return createFinalResult(allResults, totalResultCount, timedOut, executionTimeoutMs);
            
        } catch (Exception e) {
            logger.error("Error in command execution", e);
            return null;
        }
    }

    /** 检测命令是否进入“允许输入”完成态（{@link InputStatus#ALLOW_INPUT}） */
    private static boolean checkCommandCompletion(Map<String, Object> results, int currentAllowInputCount) {
        if (results == null) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        List<Object> resultList = (List<Object>) results.get("results");
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }

        for (Object result : resultList) {
            // 直接类型判断，避免反射开销
            if (result instanceof InputStatusModel) {
                InputStatusModel inputStatusModel = (InputStatusModel) result;
                InputStatus inputStatus = inputStatusModel.getInputStatus();
                if (inputStatus == InputStatus.ALLOW_INPUT) {
                    logger.debug("Command completion detected: ALLOW_INPUT (count: {})", currentAllowInputCount + 1);
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 检查结果中是否包含错误消息
     */
    private static String checkForErrorMessages(Map<String, Object> results) {
        if (results == null) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        List<Object> resultList = (List<Object>) results.get("results");
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }

        for (Object result : resultList) {
            String message = null;
            
            // 直接类型判断各类消息模型，提取错误文本
            if (result instanceof MessageModel) {
                message = ((MessageModel) result).getMessage();
            } else if (result instanceof EnhancerModel) {
                message = ((EnhancerModel) result).getMessage();
            } else if (result instanceof StatusModel) {
                message = ((StatusModel) result).getMessage();
            } else if (result instanceof CommandRequestModel) {
                message = ((CommandRequestModel) result).getMessage();
            }
            
            if (message != null && isErrorMessage(message)) {
                return message;
            }
        }
        
        return null;
    }
    
    /** 根据关键字与异常类名启发式判断消息是否为错误 */
    private static boolean isErrorMessage(String message) {
        return message.matches(".*\\b(failed|error|exception)\\b.*") || 
               message.contains("Malformed OGNL expression") || 
               message.contains("ParseException") || 
               message.contains("ExpressionSyntaxException") ||
               message.matches(".*Exception.*") ||
               message.matches(".*Error.*");
    }

    /** 过滤掉辅助性模型（状态/欢迎/会话等），只保留命令业务结果 */
    private static Map<String, Object> filterCommandSpecificResults(Map<String, Object> results) {
        if (results == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> filteredResults = new HashMap<>(results);
        @SuppressWarnings("unchecked")
        List<Object> resultList = (List<Object>) results.get("results");
        
        if (resultList == null || resultList.isEmpty()) {
            return filteredResults;
        }
        
        // 过滤辅助模型类型（直接类型判断）
        List<Object> filteredResultList = resultList.stream()
            .filter(result -> !isAuxiliaryModel(result))
            .collect(Collectors.toList());
        
        filteredResults.put("results", filteredResultList);
        filteredResults.put("resultCount", filteredResultList.size());
        
        return filteredResults;
    }
    
    /** 判断结果对象是否为应过滤的辅助模型 */
    private static boolean isAuxiliaryModel(Object result) {
        return result instanceof InputStatusModel
            || result instanceof StatusModel
            || result instanceof WelcomeModel
            || result instanceof MessageModel
            || result instanceof CommandRequestModel
            || result instanceof SessionModel
            || result instanceof EnhancerModel;
    }

    /** 从过滤后的结果 Map 中提取 results 列表 */
    private static List<Object> getCommandSpecificResults(Map<String, Object> filteredResults) {
        if (filteredResults == null) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Object> resultList = (List<Object>) filteredResults.get("results");
        return resultList != null ? resultList : new ArrayList<>();
    }

    /**
     * 发送进度通知
     */
    private static void sendProgressNotification(McpNettyServerExchange exchange, int currentResultCount, 
                                               int totalExpected, String progressToken) {
        try {
            if (progressToken != null && !progressToken.trim().isEmpty()) {
                exchange.progressNotification(new McpSchema.ProgressNotification(
                        progressToken,
                        currentResultCount,
                        (double) totalExpected
                )).join();
            }
            
        } catch (Exception e) {
            logger.error("Error sending progress notification", e);
        }
    }

    /** 构造标准错误响应 Map（{@code error=true, stage=final}） */
    public static Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        response.put("status", "error");
        response.put("stage", "final");
        return response;
    }

    /** 构造带已收集结果的错误响应 */
    public static Map<String, Object> createErrorResponseWithResults(String message, List<Object> collectedResults, int resultCount) {
        Map<String, Object> response = createErrorResponse(message);
        response.put("results", collectedResults != null ? collectedResults : new ArrayList<>());
        response.put("resultCount", resultCount);
        return response;
    }

    /** 构造用户取消任务时的结果 Map */
    private static Map<String, Object> createCancelledResult(List<Object> allResults, int totalResultCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("results", allResults);
        result.put("resultCount", totalResultCount);
        result.put("status", "cancelled");
        result.put("stage", "final");
        result.put("cancelled", true);
        result.put("message", "Task was cancelled by user");
        return result;
    }

    /** 构造命令正常结束（或超时）的最终结果 Map */
    private static Map<String, Object> createFinalResult(List<Object> allResults, int totalResultCount, boolean timedOut, long timeoutMs) {
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("results", allResults);
        finalResult.put("resultCount", totalResultCount);
        finalResult.put("status", "completed");
        finalResult.put("stage", "final");
        finalResult.put("timedOut", timedOut);
        
        if (timedOut) {
            logger.warn("Command execution timed out after {} ms", timeoutMs);
            finalResult.put("warning", "Command execution timed out after " + timeoutMs + " ms.");
        }
        
        return finalResult;
    }

    /** 构造成功完成响应，合并轮询收集的 results */
    public static Map<String, Object> createCompletedResponse(String message, Map<String, Object> results) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "completed");
        response.put("message", message);
        response.put("stage", "final");
        
        if (results != null) {
            response.putAll(results);
        }
        
        return response;
    }
}
