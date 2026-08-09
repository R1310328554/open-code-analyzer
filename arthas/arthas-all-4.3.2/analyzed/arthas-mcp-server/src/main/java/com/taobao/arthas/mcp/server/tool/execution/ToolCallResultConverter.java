package com.taobao.arthas.mcp.server.tool.execution;

import java.lang.reflect.Type;

/**
 * 工具调用结果转换器：将 Java 方法返回值转为可回传给 AI 模型的 JSON 字符串。
 * <p>
 * 实现可通过 SPI 或依赖注入替换，以支持自定义类型（如二进制、领域对象）的序列化策略。
 */
@FunctionalInterface
public interface ToolCallResultConverter {

	/**
	 * 根据声明的返回类型，将工具执行结果转换为字符串。
	 *
	 * @param result 方法实际返回值，void 时为 {@code null}
	 * @param returnType 方法声明的返回类型，含基本类型 void
	 */
	String convert(Object result, Type returnType);

}
