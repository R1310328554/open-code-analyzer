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

/**
 * 当我们无法确定比“处理 SQL 脚本时出现问题”更具体的内容时抛出：例如，我们无法更精确地查明来自 JDBC 的 {@link java.sql.SQLException}。
 * @author Sam Brannen
 * @since 4.0.3
 */
@SuppressWarnings("serial")
public class UncategorizedScriptException extends ScriptException {

	/**
	 * 创建一个新的 {@code UncategorizedScriptException}。
	 * @param message 详细留言
	 */
	public UncategorizedScriptException(String message) {
		super(message);
	}

	/**
	 * 创建一个新的 {@code UncategorizedScriptException}。
	 * @param message 详细留言
	 * @param cause 根本原因
	 */
	public UncategorizedScriptException(String message, Throwable cause) {
		super(message, cause);
	}

}
