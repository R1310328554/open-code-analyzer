package com.taobao.arthas.mcp.server.util;

import java.util.Collection;
import java.util.Map;

/**
 * 参数校验断言工具类，不满足条件时抛出 {@link IllegalArgumentException}。
 * <p>
 * 提供对 null、空字符串、空集合/映射及布尔条件的快速校验，供 MCP 服务端各模块复用。
 */
public final class Assert {

	/** 工具类禁止实例化。 */
	private Assert() {
	}

	/**
	 * 断言对象非 null。
	 * @param object 待校验对象
	 * @param message 校验失败时的异常消息
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言字符串非 null 且去除空白后非空。
	 * @param text 待校验字符串
	 * @param message 校验失败时的异常消息
	 */
	public static void hasText(String text, String message) {
		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言集合非 null 且至少包含一个元素。
	 * @param collection 待校验集合
	 * @param message 校验失败时的异常消息
	 */
	public static void notEmpty(Collection<?> collection, String message) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言映射非 null 且至少包含一个键值对。
	 * @param map 待校验映射
	 * @param message 校验失败时的异常消息
	 */
	public static void notEmpty(Map<?, ?> map, String message) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言布尔条件为 true。
	 * @param condition 待校验条件
	 * @param message 条件为 false 时的异常消息
	 */
	public static void isTrue(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}

}
