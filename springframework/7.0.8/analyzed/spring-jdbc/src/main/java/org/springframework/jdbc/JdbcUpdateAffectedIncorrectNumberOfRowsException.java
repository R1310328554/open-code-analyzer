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

import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;

/**
 * 当 JDBC 更新影响意外行数时抛出的异常。
 * 通常期望更新只影响一行；若影响多行则视为错误。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class JdbcUpdateAffectedIncorrectNumberOfRowsException extends IncorrectUpdateSemanticsDataAccessException {

	/** 本应受影响的行数。 */
	private final int expected;

	/** 实际受影响的行数。 */
	private final int actual;


	/**
	 * JdbcUpdateAffectedIncorrectNumberOfRowsException 构造器。
	 * @param sql 尝试执行的 SQL
	 * @param expected 期望受影响行数
	 * @param actual 实际受影响行数
	 */
	public JdbcUpdateAffectedIncorrectNumberOfRowsException(String sql, int expected, int actual) {
		super("SQL update '" + sql + "' affected " + actual + " rows, not " + expected + " as expected");
		this.expected = expected;
		this.actual = actual;
	}


	/**
	 * 返回本应受影响的行数。
	 */
	public int getExpectedRowsAffected() {
		return this.expected;
	}

	/**
	 * 返回实际受影响的行数。
	 */
	public int getActualRowsAffected() {
		return this.actual;
	}

	@Override
	public boolean wasDataUpdated() {
		return (getActualRowsAffected() > 0);
	}

}
