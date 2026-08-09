/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import org.jspecify.annotations.Nullable;

/**
 * 为按 {@link PropertyAccessor} 接口进行 bean 属性访问的类
 * 提供的工具方法。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 */
public abstract class PropertyAccessorUtils {

	/**
	 * 返回给定属性路径对应的实际属性名。
	 * @param propertyPath 用于确定属性名的属性路径
	 * （可包含属性键，例如用于指定 Map 条目）
	 * @return 实际属性名，不含任何键元素
	 */
	public static String getPropertyName(String propertyPath) {
		int separatorIndex = (propertyPath.endsWith(PropertyAccessor.PROPERTY_KEY_SUFFIX) ?
				propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) : -1);
		return (separatorIndex != -1 ? propertyPath.substring(0, separatorIndex) : propertyPath);
	}

	/**
	 * 检查给定属性路径是否表示索引属性或嵌套属性。
	 * @param propertyPath 要检查的属性路径
	 * @return 该路径是否表示索引属性或嵌套属性
	 */
	public static boolean isNestedOrIndexedProperty(@Nullable String propertyPath) {
		if (propertyPath == null) {
			return false;
		}
		for (int i = 0; i < propertyPath.length(); i++) {
			char ch = propertyPath.charAt(i);
			if (ch == PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR ||
					ch == PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 确定给定属性路径中第一个嵌套属性分隔符的位置，
	 * 忽略键中的点号（例如 {@code "map[my.key]"}）。
	 * @param propertyPath 要检查的属性路径
	 * @return 嵌套属性分隔符的索引；若不存在则返回 -1
	 */
	public static int getFirstNestedPropertySeparatorIndex(String propertyPath) {
		return getNestedPropertySeparatorIndex(propertyPath, false);
	}

	/**
	 * 确定给定属性路径中最后一个嵌套属性分隔符的位置，
	 * 忽略键中的点号（例如 {@code "map[my.key]"}）。
	 * @param propertyPath 要检查的属性路径
	 * @return 嵌套属性分隔符的索引；若不存在则返回 -1
	 */
	public static int getLastNestedPropertySeparatorIndex(String propertyPath) {
		return getNestedPropertySeparatorIndex(propertyPath, true);
	}

	/**
	 * 确定给定属性路径中第一个（或最后一个）嵌套属性分隔符的位置，
	 * 忽略键中的点号（例如 {@code "map[my.key]"}）。
	 * @param propertyPath 要检查的属性路径
	 * @param last 为 {@code true} 时返回最后一个分隔符，否则返回第一个
	 * @return 嵌套属性分隔符的索引；若不存在则返回 -1
	 */
	private static int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
		boolean inKey = false;
		int length = propertyPath.length();
		int i = (last ? length - 1 : 0);
		while (last ? i >= 0 : i < length) {
			switch (propertyPath.charAt(i)) {
				case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR, PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR -> {
					inKey = !inKey;
				}
				case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR -> {
					if (!inKey) {
						return i;
					}
				}
			}
			if (last) {
				i--;
			}
			else {
				i++;
			}
		}
		return -1;
	}

	/**
	 * 判断已注册路径是否与给定属性路径匹配：
	 * 既可表示属性本身，也可表示该属性的某个索引元素。
	 * @param registeredPath 已注册路径（可能含索引）
	 * @param propertyPath 属性路径（通常不含索引）
	 * @return 两条路径是否匹配
	 */
	public static boolean matchesProperty(String registeredPath, String propertyPath) {
		if (!registeredPath.startsWith(propertyPath)) {
			return false;
		}
		if (registeredPath.length() == propertyPath.length()) {
			return true;
		}
		if (registeredPath.charAt(propertyPath.length()) != PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
			return false;
		}
		return (registeredPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR, propertyPath.length() + 1) ==
				registeredPath.length() - 1);
	}

	/**
	 * 确定给定属性路径的规范名称。
	 * 会去除 Map 键两侧的引号：<br>
	 * {@code map['key']} &rarr; {@code map[key]}<br>
	 * {@code map["key"]} &rarr; {@code map[key]}
	 * @param propertyName bean 属性路径
	 * @return 属性路径的规范表示
	 */
	public static String canonicalPropertyName(@Nullable String propertyName) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder(propertyName);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = sb.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = sb.indexOf(
						PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					String key = sb.substring(keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length(), keyEnd);
					if (key.length() > 1 && ((key.startsWith("'") && key.endsWith("'")) ||
							(key.startsWith("\"") && key.endsWith("\"")))) {
						sb.delete(keyStart + 1, keyStart + 2);
						sb.delete(keyEnd - 2, keyEnd - 1);
						keyEnd = keyEnd - 2;
					}
					searchIndex = keyEnd + PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 确定给定一组属性路径的规范名称。
	 * @param propertyNames bean 属性路径（数组形式）
	 * @return 属性路径的规范表示（与入参数组等长的数组）
	 * @see #canonicalPropertyName(String)
	 */
	public static String @Nullable [] canonicalPropertyNames(String @Nullable [] propertyNames) {
		if (propertyNames == null) {
			return null;
		}
		String[] result = new String[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++) {
			result[i] = canonicalPropertyName(propertyNames[i]);
		}
		return result;
	}

}
