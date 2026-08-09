package com.taobao.arthas.mcp.server.tool.execution;

/**
 * 工具执行异常处理器：决定是将 {@link ToolExecutionException} 转为错误消息字符串返回模型，
 * 还是重新抛出由 MCP 框架统一处理。
 */
@FunctionalInterface
public interface ToolExecutionExceptionProcessor {

	/**
	 * 处理工具执行异常。
	 *
	 * @param exception 包含工具定义与根因的异常
	 * @return 返回给模型的错误描述；若实现选择抛出则不会正常返回
	 */
	String process(ToolExecutionException exception);

}
