package com.taobao.arthas.mcp.server.tool.definition;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.util.JsonSchemaGenerator;
import com.taobao.arthas.mcp.server.util.Assert;

import java.lang.reflect.Method;

/**
 * 从 Java {@link Method} 及其 {@link Tool} 注解反射构建 {@link ToolDefinition} 的工具类。
 */
public class ToolDefinitions {

	/**
	 * 根据方法上的 {@code @Tool} 注解与参数上的 {@code @ToolParam} 生成 Builder，
	 * 自动填充名称、描述、入参 Schema、流式与任务支持标志。
	 */
	public static ToolDefinition.Builder builder(Method method) {
		Assert.notNull(method, "method cannot be null");
		return ToolDefinition.builder()
			.name(getToolName(method))
			.description(getToolDescription(method))
			.inputSchema(JsonSchemaGenerator.generateForMethodInput(method))
			.streamable(isStreamable(method))
            .taskSupport(getTaskSupport(method));
	}

	/** 一步构建完整的 {@link ToolDefinition} 实例。 */
	public static ToolDefinition from(Method method) {
		return builder(method).build();
	}

	/** 解析工具名称：优先使用注解 {@code name}，否则回退到方法名。 */
	public static String getToolName(Method method) {
		Assert.notNull(method, "method cannot be null");
		Tool tool = method.getAnnotation(Tool.class);
		if (tool == null) {
			return method.getName();
		}
		return tool.name() != null ? tool.name() : method.getName();
	}

	/** 解析工具描述：优先使用注解 {@code description}，否则回退到方法名。 */
	public static String getToolDescription(Method method) {
		Assert.notNull(method, "method cannot be null");
		Tool tool = method.getAnnotation(Tool.class);
		if (tool == null) {
			return method.getName();
		}
		return tool.description() != null ? tool.description() : method.getName();
	}

	/** 判断方法是否标记为流式工具（{@code @Tool(streamable = true)}）。 */
	public static boolean isStreamable(Method method) {
		Assert.notNull(method, "method cannot be null");
		Tool tool = method.getAnnotation(Tool.class);
		if (tool == null) {
			return false;
		}
		return tool.streamable();
	}

    /** 读取任务支持模式；无 {@code @Tool} 注解时返回 {@link McpSchema.TaskSupportMode#FORBIDDEN}。 */
    public static McpSchema.TaskSupportMode getTaskSupport(Method method) {
        Assert.notNull(method, "method cannot be null");
        Tool tool = method.getAnnotation(Tool.class);
        if (tool == null) {
            return McpSchema.TaskSupportMode.FORBIDDEN;
        }
        return tool.taskSupport();
    }

}
