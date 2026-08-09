package com.taobao.arthas.mcp.server.tool;

import java.util.Collections;
import java.util.Map;

/**
 * 工具执行上下文：封装一次工具调用所需的键值对运行时数据。
 * <p>
 * 键名约定见 {@link ToolContextKeys}，例如会话、任务 ID、MCP 交换对象等。
 */
public final class ToolContext {

	/** 不可变上下文映射，构造后不允许外部修改。 */
	private final Map<String, Object> context;

	/**
	 * 使用给定映射创建上下文；内部会复制为不可变视图以保证线程安全。
	 *
	 * @param context 上下文键值对，键建议使用 {@link ToolContextKeys} 中的常量
	 */
	public ToolContext(Map<String, Object> context) {
		this.context = Collections.unmodifiableMap(context);
	}

	/** 返回只读上下文映射，供工具实现按需读取运行时对象。 */
	public Map<String, Object> getContext() {
		return this.context;
	}

}
