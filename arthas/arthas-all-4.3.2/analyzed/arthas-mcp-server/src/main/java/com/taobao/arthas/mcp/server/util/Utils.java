package com.taobao.arthas.mcp.server.util;

import java.util.Collection;
import java.util.Map;

/**
 * MCP 服务端通用辅助工具：字符串判空、集合判空及基本类型与包装类之间的类型兼容判断。
 */
public final class Utils {

	/**
	 * 判断字符串是否包含非空白字符。
	 * @param str 待检查字符串
	 * @return 非 null 且 trim 后非空时返回 true
	 */
	public static boolean hasText(String str) {
		return str != null && !str.trim().isEmpty();
	}

	/** 判断集合是否为 null 或空。 */
	public static boolean isEmpty(Collection<?> collection) {
		return (collection == null || collection.isEmpty());
	}

	/** 判断映射是否为 null 或空。 */
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}

	/**
	 * 判断 sourceType 能否赋值给 targetType（含基本类型与包装类互转）。
	 * @param targetType 目标类型
	 * @param sourceType 源类型
	 */
	public static boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
		if (targetType == null || sourceType == null) {
			return false;
		}

		if (targetType.equals(sourceType)) {
			return true;
		}

		if (targetType.isAssignableFrom(sourceType)) {
			return true;
		}

		// 目标为基本类型时，尝试将源包装类解析为对应基本类型
		if (targetType.isPrimitive()) {
			Class<?> resolvedPrimitive = getPrimitiveClassForWrapper(sourceType);
			return resolvedPrimitive != null && targetType.equals(resolvedPrimitive);
		}
		else if (sourceType.isPrimitive()) {
			Class<?> resolvedWrapper = getWrapperClassForPrimitive(sourceType);
			return resolvedWrapper != null && targetType.equals(resolvedWrapper);
		}

		return false;
	}

	/**
	 * 将包装类映射为对应的基本类型 Class。
	 * @param wrapperClass 包装类，如 {@link Integer}.class
	 * @return 基本类型 Class，无法映射时返回 null
	 */
	public static Class<?> getPrimitiveClassForWrapper(Class<?> wrapperClass) {
		if (Boolean.class.equals(wrapperClass)) return boolean.class;
		if (Byte.class.equals(wrapperClass)) return byte.class;
		if (Character.class.equals(wrapperClass)) return char.class;
		if (Double.class.equals(wrapperClass)) return double.class;
		if (Float.class.equals(wrapperClass)) return float.class;
		if (Integer.class.equals(wrapperClass)) return int.class;
		if (Long.class.equals(wrapperClass)) return long.class;
		if (Short.class.equals(wrapperClass)) return short.class;
		if (Void.class.equals(wrapperClass)) return void.class;
		return null;
	}

	/**
	 * 将基本类型 Class 映射为对应包装类。
	 * @param primitiveClass 基本类型 Class，如 int.class
	 * @return 包装类 Class，无法映射时返回 null
	 */
	public static Class<?> getWrapperClassForPrimitive(Class<?> primitiveClass) {
		if (boolean.class.equals(primitiveClass)) return Boolean.class;
		if (byte.class.equals(primitiveClass)) return Byte.class;
		if (char.class.equals(primitiveClass)) return Character.class;
		if (double.class.equals(primitiveClass)) return Double.class;
		if (float.class.equals(primitiveClass)) return Float.class;
		if (int.class.equals(primitiveClass)) return Integer.class;
		if (long.class.equals(primitiveClass)) return Long.class;
		if (short.class.equals(primitiveClass)) return Short.class;
		if (void.class.equals(primitiveClass)) return Void.class;
		return null;
	}

}
