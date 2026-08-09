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

package org.springframework.boot.context.properties.source;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 当配置属性值无效时抛出的异常。
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@SuppressWarnings("serial")
public class InvalidConfigurationPropertyValueException extends RuntimeException {

	private final String name;

	private final @Nullable Object value;

	private final @Nullable String reason;

	/**
	 * 为指定属性 {@code name} 与 {@code value} 创建新实例，并包含说明值无效的 {@code reason}。
	 *
	 * @param name 规范格式的属性名
	 * @param value 属性值，可为 {@code null}
	 * @param reason 描述值无效的可读文本。以大写字母开头并以句号结尾。允许多句与多行。
	 */
	public InvalidConfigurationPropertyValueException(String name, @Nullable Object value, @Nullable String reason) {
		this(name, value, reason, null);
	}

	/**
	 * 为指定属性 {@code name} 与 {@code value} 创建新实例，并包含说明值无效的 {@code reason}。
	 *
	 * @param name 规范格式的属性名
	 * @param value 属性值，可为 {@code null}
	 * @param reason 描述值无效的可读文本。以大写字母开头并以句号结尾。允许多句与多行。
	 * @param cause 异常原因或 {@code null}
	 * @since 4.1.0
	 */
	public InvalidConfigurationPropertyValueException(String name, @Nullable Object value, @Nullable String reason,
			@Nullable Throwable cause) {
		super("Property " + name + " with value '" + value + "' is invalid: " + reason, cause);
		Assert.notNull(name, "'name' must not be null");
		this.name = name;
		this.value = value;
		this.reason = reason;
	}

	/**
	 * 返回属性名。
	 *
	 * @return 属性名
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 返回无效值，可为 {@code null}。
	 *
	 * @return 无效值
	 */
	public @Nullable Object getValue() {
		return this.value;
	}

	/**
	 * 返回值无效的原因。
	 *
	 * @return 原因
	 */
	public @Nullable String getReason() {
		return this.reason;
	}

}
