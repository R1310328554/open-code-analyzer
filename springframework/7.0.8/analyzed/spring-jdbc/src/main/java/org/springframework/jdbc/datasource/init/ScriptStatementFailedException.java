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

import org.springframework.core.io.support.EncodedResource;

/**
 * 当 SQL 脚本中的某条语句在目标数据库上执行失败时，由 {@link ScriptUtils} 抛出。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0.5
 */
@SuppressWarnings("serial")
public class ScriptStatementFailedException extends ScriptException {

	/**
	 * 构造新的 {@code ScriptStatementFailedException}。
	 * @param stmt 执行失败的实际 SQL 语句
	 * @param stmtNumber SQL 脚本中的语句序号（即资源中第 n 条语句）
	 * @param encodedResource 读取 SQL 语句的资源
	 * @param cause 失败的根本原因
	 */
	public ScriptStatementFailedException(String stmt, int stmtNumber, EncodedResource encodedResource, Throwable cause) {
		super(buildErrorMessage(stmt, stmtNumber, encodedResource), cause);
	}


	/**
	 * 根据给定参数构建 SQL 脚本执行失败的错误消息。
	 * @param stmt 执行失败的实际 SQL 语句
	 * @param stmtNumber SQL 脚本中的语句序号（即资源中第 n 条语句）
	 * @param encodedResource 读取 SQL 语句的资源
	 * @return 适用于异常<em>详细消息</em>或日志记录的错误消息
	 * @since 4.2
	 */
	public static String buildErrorMessage(String stmt, int stmtNumber, EncodedResource encodedResource) {
		return String.format("Failed to execute SQL script statement #%s of %s: %s", stmtNumber, encodedResource, stmt);
	}

}
