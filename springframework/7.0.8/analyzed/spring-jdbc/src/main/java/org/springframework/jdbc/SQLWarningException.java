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

import java.sql.SQLWarning;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * 当我们不忽略 {@link java.sql.SQLWarning SQLWarnings} 时抛出异常。
 * <p>如果报告 SQLWarning，则操作已完成，因此如果我们在查看警告时不满意，则需要显式回滚它。我们可能会选择忽略（并记录）警告，或者将其包装并以 SQLWarningE
 * xception 的形式抛出。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.jdbc.core.JdbcTemplate#setIgnoreWarnings
 */
@SuppressWarnings("serial")
public class SQLWarningException extends UncategorizedDataAccessException {

	/**
	 * SQLWarningException 的构造函数。
	 * @param msg 详细消息
	 * @param ex JDBC 警告
	 */
	public SQLWarningException(String msg, SQLWarning ex) {
		super(msg, ex);
	}


	/**
	 * 返回底层 {@link SQLWarning}。
	 * @since 5.3.29
	 */
	public @Nullable SQLWarning getSQLWarning() {
		return (SQLWarning) getCause();
	}

	/**
	 * 返回底层 {@link SQLWarning}。
	 * @deprecated 5.3.29，支持 {@link #getSQLWarning()}
	 */
	@Deprecated(since = "5.3.29")
	public @Nullable SQLWarning SQLWarning() {
		return getSQLWarning();
	}

}
