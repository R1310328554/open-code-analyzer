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

package org.springframework.boot.autoconfigure;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.lang.Contract;

/**
 * 提供对自动配置注解处理器写入的元数据的访问。
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
public interface AutoConfigurationMetadata {

	/**
	 * 若指定类名已被注解处理器处理，则返回 {@code true}。
	 * @param className 源类
	 * @return 是否已处理
	 */
	boolean wasProcessed(String className);

	/**
	 * 从元数据中获取 {@link Integer} 值。
	 * @param className 源类
	 * @param key 元数据键
	 * @return 元数据值，或 {@code null}
	 */
	@Nullable Integer getInteger(String className, String key);

	/**
	 * 从元数据中获取 {@link Integer} 值。
	 * @param className 源类
	 * @param key 元数据键
	 * @param defaultValue 默认值
	 * @return 元数据值，或 {@code defaultValue}
	 */
	@Contract("_, _, !null -> !null")
	@Nullable Integer getInteger(String className, String key, @Nullable Integer defaultValue);

	/**
	 * 从元数据中获取 {@link Set} 值。
	 * @param className 源类
	 * @param key 元数据键
	 * @return 元数据值，或 {@code null}
	 */
	@Nullable Set<String> getSet(String className, String key);

	/**
	 * 从元数据中获取 {@link Set} 值。
	 * @param className 源类
	 * @param key 元数据键
	 * @param defaultValue 默认值
	 * @return 元数据值，或 {@code defaultValue}
	 */
	@Contract("_, _, !null -> !null")
	@Nullable Set<String> getSet(String className, String key, @Nullable Set<String> defaultValue);

	/**
	 * 从元数据中获取 {@link String} 值。
	 * @param className 源类
	 * @param key 元数据键
	 * @return 元数据值，或 {@code null}
	 */
	@Nullable String get(String className, String key);

	/**
	 * 从元数据中获取 {@link String} 值。
	 * @param className 源类
	 * @param key 元数据键
	 * @param defaultValue 默认值
	 * @return 元数据值，或 {@code defaultValue}
	 */
	@Contract("_, _, !null -> !null")
	@Nullable String get(String className, String key, @Nullable String defaultValue);

}
