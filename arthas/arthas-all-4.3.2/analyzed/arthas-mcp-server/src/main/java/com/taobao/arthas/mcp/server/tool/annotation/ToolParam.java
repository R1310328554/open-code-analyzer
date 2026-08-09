package com.taobao.arthas.mcp.server.tool.annotation;

import java.lang.annotation.*;

/**
 * 标记工具方法的单个入参，用于生成 JSON Schema 中的 properties 与 required 字段。
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolParam {

	/**
	 * 该参数是否为必填项；默认为 {@code true}。
	 */
	boolean required() default true;

	/**
	 * 参数说明，会写入 JSON Schema 的 {@code description} 字段供模型理解语义。
	 */
	String description() default "";

}
