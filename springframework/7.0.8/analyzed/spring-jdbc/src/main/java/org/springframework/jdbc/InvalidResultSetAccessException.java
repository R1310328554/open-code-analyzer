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
 * 以无效方式访问 ResultSet 时抛出的异常。
 * 此类异常始终以 {@code java.sql.SQLException} 为根因。
 *
 * <p>通常因指定无效的 ResultSet 列索引或列名导致。
 * 断开连接的 SqlRowSet 也会抛出。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see BadSqlGrammarException
 * @see org.springframework.jdbc.support.rowset.SqlRowSet
 */
@SuppressWarnings("serial")
public class InvalidResultSetAccessException extends InvalidDataAccessResourceUsageException {

	private final @Nullable String sql;


	/**
	 * InvalidResultSetAccessException 构造器。
	 * @param task 当前任务名称
	 * @param sql 有问题的 SQL 语句
	 * @param ex 根因
	 */
	public InvalidResultSetAccessException(String task, String sql, SQLException ex) {
		super(task + "; invalid ResultSet access for SQL [" + sql + "]", ex);
		this.sql = sql;
	}

	/**
	 * InvalidResultSetAccessException 构造器。
	 * @param ex 根因
	 */
	public InvalidResultSetAccessException(SQLException ex) {
		super(ex.getMessage(), ex);
		this.sql = null;
	}


	/**
	 * 返回包装的 SQLException。
	 */
	public @Nullable SQLException getSQLException() {
		return (SQLException) getCause();
	}

	/**
	 * 返回导致问题的 SQL。
	 * @return 有问题的 SQL（若已知）
	 */
	public @Nullable String getSql() {
		return this.sql;
	}

}
