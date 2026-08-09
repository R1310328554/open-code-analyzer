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

package org.springframework.boot.logging.structured;

import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.json.JsonWriter.Members;
import org.springframework.core.env.Environment;

/**
 * 可注入 {@link StructuredLogFormatter} 实现以定制 {@link JsonWriter} {@link Members} 的 customizer。
 * <p>
 * 可通过 {@code logging.structured.json.customizer} 属性提供实现，
 * 或在 {@code META-INF/spring.factories} 中注册，键为
 * {@code org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer}。
 * <p>
 * 实现类可在构造函数中声明以下参数类型：
 * <ul>
 * <li>{@link Environment}</li>
 * </ul>
 * 使用 Logback 时，还可在构造函数中使用：
 * <ul>
 * <li>{@code ch.qos.logback.classic.pattern.ThrowableProxyConverter}</li>
 * </ul>
 *
 * @param <T> the type being written 被写入的类型
 * @author Phillip Webb
 * @since 3.4.0
 * @see JsonWriterStructuredLogFormatter
 */
@FunctionalInterface
public interface StructuredLoggingJsonMembersCustomizer<T> {

	/**
	 * Customize the given {@link Members} instance.
	 * @param members the members instance to customize
	 */
	void customize(JsonWriter.Members<T> members);

	/**
	 * Builder that can be injected into a {@link StructuredLogFormatter} to build the
	 * {@link StructuredLoggingJsonMembersCustomizer} when specific settings are required.
	 *
	 * @param <T> the type being written
	 * @since 3.5.4
	 */
	interface Builder<T> {

		/**
		 * Use nested fields when adding JSON from user defined properties.
		 * @return this builder
		 */
		default Builder<T> nested() {
			return nested(true);
		}

		/**
		 * Set if nested fields should be used when adding JSON from user defined
		 * properties.
		 * @param nested if nested fields are to be used
		 * @return this builder
		 */
		Builder<T> nested(boolean nested);

		/**
		 * Build the {@link StructuredLoggingJsonMembersCustomizer}.
		 * @return the built customizer
		 */
		StructuredLoggingJsonMembersCustomizer<T> build();

	}

}
