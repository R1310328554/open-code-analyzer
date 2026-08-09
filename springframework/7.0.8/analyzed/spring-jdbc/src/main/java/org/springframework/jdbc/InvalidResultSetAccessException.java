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
 * 以无效方式访问 ResultSet 时抛出异常。此类异常始终有 {@code java.sql.SQLException} 根本原因。
 * <p> 当指定了无效的 ResultSet 列索引或名称时，通常会发生这种情况。也由断开连接的 SqlRowSets 引发。
 * @author Juergen Hoeller
 * @since 1.2
 * @see BadSqlGrammarException
 * @see org.springframework.jdbc.support.rowset.SqlRowSet
 */
@SuppressWarnings("serial")
public class InvalidResultSetAccessException extends InvalidDataAccessResourceUsageException {

	/** `sql`：该类的成员状态。 */
	private final @Nullable String sql;


	/**
	 * InvalidResultSetAccessException 的构造函数。
	 * @param task 当前任务名称
	 * @param sql 有问题的 SQL 语句
	 * @param ex 根本原因
	 */
	public InvalidResultSetAccessException(String task, String sql, SQLException ex) {
		super(task + "; invalid ResultSet access for SQL [" + sql + "]", ex);
		this.sql = sql;
	}

	/**
	 * InvalidResultSetAccessException 的构造函数。
	 * @param ex 根本原因
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
	 * @return 违规 SQL（如果已知）
	 */
	public @Nullable String getSql() {
		return this.sql;
	}

}
