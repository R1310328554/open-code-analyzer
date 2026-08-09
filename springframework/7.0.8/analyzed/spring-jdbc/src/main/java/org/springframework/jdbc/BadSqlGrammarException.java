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

package org.springframework.jdbc;

import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * 当指定 SQL 无效时抛出的异常。此类异常始终以
 * {@code java.sql.SQLException} 为根因。
 *
 * <p>可为「无此表」「无此列」等定义子类。
 * 自定义 SQLExceptionTranslator 可创建更具体的异常，
 * 而不影响使用本类的代码。
 *
 * @author Rod Johnson
 * @see InvalidResultSetAccessException
 */
@SuppressWarnings("serial")
public class BadSqlGrammarException extends InvalidDataAccessResourceUsageException {

	private final String sql;


	/**
	 * BadSqlGrammarException 构造器。
	 * @param task 当前任务名称
	 * @param sql 有问题的 SQL 语句
	 * @param ex 根因
	 */
	public BadSqlGrammarException(String task, String sql, SQLException ex) {
		super(task + "; bad SQL grammar [" + sql + "]", ex);
		this.sql = sql;
	}


	/**
	 * 返回包装的 SQLException。
	 */
	public @Nullable SQLException getSQLException() {
		return (SQLException) getCause();
	}

	/**
	 * 返回导致问题的 SQL。
	 */
	public String getSql() {
		return this.sql;
	}

}
