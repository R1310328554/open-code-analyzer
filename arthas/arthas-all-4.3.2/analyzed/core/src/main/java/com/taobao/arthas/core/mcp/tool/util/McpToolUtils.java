package com.taobao.arthas.core.mcp.tool.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.arthas.mcp.server.protocol.server.McpServerFeatures;
import com.taobao.arthas.mcp.server.protocol.server.McpStatelessServerFeatures;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.tool.ToolCallback;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.ToolContextKeys;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * MCP 工具回调与 Arthas MCP 服务端 {@link McpServerFeatures} 规范之间的转换工具。
 * <p>
 * 支持 Streamable（有 Exchange、进度令牌）与 Stateless 两种部署模式；
 * 按工具名去重后包装为 {@link ToolCallback} 调用链，统一序列化参数与构造成功/失败结果。
 */
public final class McpToolUtils {

	/** 工具类，禁止实例化 */
	private McpToolUtils() {
	}

	/**
	 * 将 {@link ToolCallback} 列表转为 Streamable MCP 工具规范列表。
	 * @param tools 注册的 Arthas 工具回调，可为 null
	 * @return 去重后的工具规范；空输入返回不可变空列表
	 */
	public static List<McpServerFeatures.ToolSpecification> toStreamableToolSpecifications(
			List<ToolCallback> tools) {

		if (tools == null || tools.isEmpty()) {
			return Collections.emptyList();
		}

		// 按工具名去重，同名保留首次出现的定义
		return tools.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(
						tool -> tool.getToolDefinition().getName(), // Key: tool name
						tool -> tool,                               // Value: the tool itself
						(existing, replacement) -> existing          // On duplicate key, keep the existing tool
				))
				.values()
				.stream()
				.map(McpToolUtils::toToolSpecification)
				.collect(Collectors.toList());
	}

	/** 单个 {@link ToolCallback} 转为 Streamable 工具规范，含 Exchange 与认证上下文注入 */
	public static McpServerFeatures.ToolSpecification toToolSpecification(ToolCallback toolCallback) {
		McpSchema.Tool tool = new McpSchema.Tool(
				toolCallback.getToolDefinition().getName(),
				toolCallback.getToolDefinition().getDescription(),
				toolCallback.getToolDefinition().getInputSchema(),
				new McpSchema.ToolExecution(toolCallback.getToolDefinition().taskSupport())
		);

		McpServerFeatures.ToolCallFunction callFunction = (exchange, commandContext, request) -> {
			try {
				Map<String, Object> contextMap = new HashMap<>();
				contextMap.put(ToolContextKeys.EXCHANGE, exchange);
				contextMap.put(ToolContextKeys.COMMAND_CONTEXT, commandContext);
                contextMap.put(ToolContextKeys.PROGRESS_TOKEN, request.progressToken());
				// Streamable 模式：从 Exchange 注入传输上下文，供工具读取认证信息
				if (exchange != null && exchange.getTransportContext() != null) {
					contextMap.put(ToolContextKeys.MCP_TRANSPORT_CONTEXT, exchange.getTransportContext());
				}
				ToolContext toolContext = new ToolContext(contextMap);

				String requestJson = convertParametersToString(request.getArguments());

				String callResult = toolCallback.call(requestJson, toolContext);
				return CompletableFuture.completedFuture(createSuccessResult(callResult));
			} catch (Exception e) {
				return CompletableFuture.completedFuture(createErrorResult(e.getMessage()));
			}
		};
		return new McpServerFeatures.ToolSpecification(tool, callFunction);
	}


	/**
	 * 将工具回调列表转为 Stateless MCP 工具规范（无长连接 Exchange）。
	 * @param providerToolCallbacks 提供者注册的工具列表
	 */
	public static List<McpStatelessServerFeatures.ToolSpecification> toStatelessToolSpecifications(List<ToolCallback> providerToolCallbacks) {
		if (providerToolCallbacks == null || providerToolCallbacks.isEmpty()) {
			return Collections.emptyList();
		}

		// De-duplicate tools by their name, keeping the first occurrence of each tool name
		return providerToolCallbacks.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(
						tool -> tool.getToolDefinition().getName(), // Key: tool name
						tool -> tool,                               // Value: the tool itself
						(existing, replacement) -> existing          // On duplicate key, keep the existing tool
				))
				.values()
				.stream()
				.map(McpToolUtils::toStatelessToolSpecification)
				.collect(Collectors.toList());
	}

	/** Stateless 单工具包装：上下文仅含 MCP_TRANSPORT_CONTEXT 与 COMMAND_CONTEXT */
	public static McpStatelessServerFeatures.ToolSpecification toStatelessToolSpecification(ToolCallback toolCallback) {
		McpSchema.Tool tool = new McpSchema.Tool(
				toolCallback.getToolDefinition().getName(),
				toolCallback.getToolDefinition().getDescription(),
				toolCallback.getToolDefinition().getInputSchema(),
				new McpSchema.ToolExecution(toolCallback.getToolDefinition().taskSupport())
		);

		McpStatelessServerFeatures.ToolCallFunction callFunction = (context, commandContext, arguments) -> {
			try {
				Map<String, Object> contextMap = new HashMap<>();
				contextMap.put(ToolContextKeys.MCP_TRANSPORT_CONTEXT, context);
				contextMap.put(ToolContextKeys.COMMAND_CONTEXT, commandContext);
				ToolContext toolContext = new ToolContext(contextMap);

				String argumentsJson = convertParametersToString(arguments);
				String callResult = toolCallback.call(argumentsJson, toolContext);
				return CompletableFuture.completedFuture(createSuccessResult(callResult));
			} catch (Exception e) {
				return CompletableFuture.completedFuture(createErrorResult("Error executing tool: " + e.getMessage()));
			}
		};

		return new McpStatelessServerFeatures.ToolSpecification(tool, callFunction);
	}

	/** 将 MCP 工具参数字典序列化为 JSON 字符串，失败时回退为 Map#toString */
	private static String convertParametersToString(Map<String, Object> parameters) {
		if (parameters == null) {
			return "";
		}
		try {
			return new ObjectMapper().writeValueAsString(parameters);
		} catch (Exception e) {
			return parameters.toString();
		}
	}

	/** 构造 isError=false 的文本型 {@link McpSchema.CallToolResult}，空内容默认为 "{}" */
	private static McpSchema.CallToolResult createSuccessResult(String content) {
		List<McpSchema.Content> contents = new ArrayList<>();
		String safeContent = (content != null && !content.trim().isEmpty()) ? content : "{}";
		contents.add(new McpSchema.TextContent(safeContent));
        return McpSchema.CallToolResult.builder()
                .content(contents)
                .isError(false)
                .build();
	}

	/** 构造 isError=true 的错误结果，供客户端识别工具执行失败 */
	private static McpSchema.CallToolResult createErrorResult(String errorMessage) {
		List<McpSchema.Content> contents = new ArrayList<>();
		String safeErrorMessage = (errorMessage != null && !errorMessage.trim().isEmpty()) ? 
			errorMessage : "Unknown error occurred";
		contents.add(new McpSchema.TextContent(safeErrorMessage));
        return McpSchema.CallToolResult.builder()
                .content(contents)
                .isError(true)
                .build();
	}

}
