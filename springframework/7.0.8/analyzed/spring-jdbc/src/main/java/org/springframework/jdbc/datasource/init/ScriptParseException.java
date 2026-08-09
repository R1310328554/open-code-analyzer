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

package org.springframework.jdbc.datasource.init;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.support.EncodedResource;

/**
 * 如果无法正确解析 SQL 脚本，则由 {@link ScriptUtils} 抛出。
 * @author Sam Brannen
 * @since 4.0.3
 */
@SuppressWarnings("serial")
public class ScriptParseException extends ScriptException {

	/**
	 * 创建一个新的 {@code ScriptParseException}。
	 * @param message 详细留言
	 * @param resource 从中读取 SQL 脚本的资源
	 */
	public ScriptParseException(String message, @Nullable EncodedResource resource) {
		super(buildMessage(message, resource));
	}

	/**
	 * 创建一个新的 {@code ScriptParseException}。
	 * @param message 详细留言
	 * @param resource 从中读取 SQL 脚本的资源
	 * @param cause 失败的根本原因
	 */
	public ScriptParseException(String message, @Nullable EncodedResource resource, @Nullable Throwable cause) {
		super(buildMessage(message, resource), cause);
	}


	/**
	 * 构建：Message（方法 `buildMessage`）。
	 */
	private static String buildMessage(String message, @Nullable EncodedResource resource) {
		return String.format("Failed to parse SQL script from resource [%s]: %s",
				(resource == null ? "<unknown>" : resource), message);
	}

}
