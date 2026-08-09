/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.web.error;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * 控制 {@code ErrorAttributes} 内容的选项。
 *
 * @author Scott Frederick
 * @author Phillip Webb
 * @since 2.3.0
 */
public final class ErrorAttributeOptions {

	private final Set<Include> includes;

	private ErrorAttributeOptions(Set<Include> includes) {
		this.includes = includes;
	}

	/**
	 * 获取是否在错误响应中包含指定属性的选项。
	 *
	 * @param include error attribute to get 待查询的错误属性
	 * @return {@code true} if the {@code Include} attribute is included in the error
	 * response, {@code false} otherwise 若错误响应包含该 {@code Include} 属性则为 {@code true}，否则为 {@code false}
	 */
	public boolean isIncluded(Include include) {
		return this.includes.contains(include);
	}

	/**
	 * 获取错误响应中包含属性的全部选项。
	 *
	 * @return the options 选项集合
	 */
	public Set<Include> getIncludes() {
		return this.includes;
	}

	/**
	 * 返回包含指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。
	 *
	 * @param includes error attributes to include 要包含的错误属性
	 * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例
	 */
	public ErrorAttributeOptions including(Include... includes) {
		EnumSet<Include> updated = copyIncludes();
		updated.addAll(Arrays.asList(includes));
		return new ErrorAttributeOptions(Collections.unmodifiableSet(updated));
	}

	/**
	 * 返回排除指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。
	 *
	 * @param excludes error attributes to exclude 要排除的错误属性
	 * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例
	 */
	public ErrorAttributeOptions excluding(Include... excludes) {
		EnumSet<Include> updated = copyIncludes();
		Arrays.stream(excludes).forEach(updated::remove);
		return new ErrorAttributeOptions(Collections.unmodifiableSet(updated));
	}

	/**
	 * 从此选项集合中未包含的项会从给定 map 中移除。
	 *
	 * @param map the map to update 待更新的 map
	 * @since 3.2.7
	 */
	public void retainIncluded(Map<String, @Nullable Object> map) {
		for (Include candidate : Include.values()) {
			if (!this.includes.contains(candidate)) {
				map.remove(candidate.key);
			}
		}
	}

	private EnumSet<Include> copyIncludes() {
		return (this.includes.isEmpty()) ? EnumSet.noneOf(Include.class) : EnumSet.copyOf(this.includes);
	}

	/**
	 * 创建带默认值的 {@code ErrorAttributeOptions}。
	 *
	 * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例
	 */
	public static ErrorAttributeOptions defaults() {
		return of(Include.PATH, Include.STATUS, Include.ERROR);
	}

	/**
	 * 创建包含指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。
	 *
	 * @param includes error attributes to include 要包含的错误属性
	 * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例
	 */
	public static ErrorAttributeOptions of(Include... includes) {
		return of(Arrays.asList(includes));
	}

	/**
	 * 创建包含指定 {@link Include} 选项的 {@code ErrorAttributeOptions}。
	 *
	 * @param includes error attributes to include 要包含的错误属性
	 * @return an {@code ErrorAttributeOptions} {@code ErrorAttributeOptions} 实例
	 */
	public static ErrorAttributeOptions of(Collection<Include> includes) {
		return new ErrorAttributeOptions(
				(includes.isEmpty()) ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(includes)));
	}

	/**
	 * 可包含在错误响应中的错误属性。
	 */
	public enum Include {

		/**
		 * 包含异常类名属性。
		 */
		EXCEPTION("exception"),

		/**
		 * 包含堆栈跟踪属性。
		 */
		STACK_TRACE("trace"),

		/**
		 * 包含消息属性。
		 */
		MESSAGE("message"),

		/**
		 * 包含绑定错误属性。
		 */
		BINDING_ERRORS("errors"),

		/**
		 * 包含 HTTP 状态码。
		 * @since 3.2.7
		 */
		STATUS("status"),

		/**
		 * 包含 HTTP 错误描述。
		 * @since 3.2.7
		 */
		ERROR("error"),

		/**
		 * 包含请求路径。
		 * @since 3.3.0
		 */
		PATH("path");

		private final String key;

		Include(String key) {
			this.key = key;
		}

	}

}
