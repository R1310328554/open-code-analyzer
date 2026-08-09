package com.taobao.arthas.mcp.server.tool;

/**
 * 工具回调提供者：向 MCP 服务器注册一组可被发现与调用的 {@link ToolCallback}。
 * <p>
 * 典型实现通过扫描 {@code @Tool} 注解方法或手动组装 {@link DefaultToolCallback} 实例。
 */
public interface ToolCallbackProvider {

	/** 返回当前提供者暴露的全部工具回调实例。 */
	ToolCallback[] getToolCallbacks();

}
