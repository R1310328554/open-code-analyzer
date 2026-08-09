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

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * 当我们无法使用 JDBC 连接到 RDBMS 时抛出致命异常。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class CannotGetJdbcConnectionException extends DataAccessResourceFailureException {

	/**
	 * {@code CannotGetJdbcConnectionException} 的构造函数。
	 * @param msg 详细消息
	 * @since 5.0
	 */
	public CannotGetJdbcConnectionException(String msg) {
		super(msg);
	}

	/**
	 * {@code CannotGetJdbcConnectionException} 的构造函数。
	 * @param msg 详细消息
	 * @param ex 根本原因 SQLException
	 */
	public CannotGetJdbcConnectionException(String msg, @Nullable SQLException ex) {
		super(msg, ex);
	}

	/**
	 * {@code CannotGetJdbcConnectionException} 的构造函数。
	 * @param msg 详细消息
	 * @param ex 根本原因 IllegalStateException
	 * @since 5.3.22
	 */
	public CannotGetJdbcConnectionException(String msg, IllegalStateException ex) {
		super(msg, ex);
	}

}
