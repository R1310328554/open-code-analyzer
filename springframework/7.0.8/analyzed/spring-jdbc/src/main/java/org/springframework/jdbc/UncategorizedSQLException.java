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

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * 无法将 SQLException 归类为通用数据访问异常时抛出。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class UncategorizedSQLException extends UncategorizedDataAccessException {

	/** 导致问题的 SQL。 */
	private final @Nullable String sql;


	/**
	 * UncategorizedSQLException 构造器。
	 * @param task 当前任务名称
	 * @param sql 有问题的 SQL 语句
	 * @param ex 根因
	 */
	public UncategorizedSQLException(String task, @Nullable String sql, SQLException ex) {
		super(task + "; uncategorized SQLException" + (sql != null ? " for SQL [" + sql + "]" : "") +
				"; SQL state [" + ex.getSQLState() + "]; error code [" + ex.getErrorCode() + "]; " +
				ex.getMessage(), ex);
		this.sql = sql;
	}


	/**
	 * 返回底层 SQLException。
	 */
	public @Nullable SQLException getSQLException() {
		return (SQLException) getCause();
	}

	/**
	 * 返回导致问题的 SQL（若已知）。
	 */
	public @Nullable String getSql() {
		return this.sql;
	}

}
