package com.taobao.arthas.mcp.server.tool.execution;

import com.taobao.arthas.mcp.server.tool.definition.ToolDefinition;

/**
 * 工具执行失败时抛出的运行时异常，携带失败工具的 {@link ToolDefinition} 便于日志与错误处理。
 */
public class ToolExecutionException extends RuntimeException {

	/** 发生异常的工具元数据，用于定位是哪个 MCP 工具调用失败。 */
	private final ToolDefinition toolDefinition;

	public ToolExecutionException(ToolDefinition toolDefinition, Throwable cause) {
		super(cause.getMessage(), cause);
		this.toolDefinition = toolDefinition;
	}

	public ToolDefinition getToolDefinition() {
		return this.toolDefinition;
	}

}
