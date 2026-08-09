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
 * 当 JDBC 更新影响意外数量的行时引发异常。通常，我们预计更新会影响单行，这意味着如果它影响多行，则会出现错误。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class JdbcUpdateAffectedIncorrectNumberOfRowsException extends IncorrectUpdateSemanticsDataAccessException {

	/**
	 */
	private final int expected;

	/**
	 */
	private final int actual;


	/**
	 * JdbcUpdateAffectedIn CorrectNumberOfRowsException 的构造函数。
	 * @param sql 我们试图执行的 SQL
	 * @param expected 预计受影响的行数
	 * @param actual 实际受影响的行数
	 */
	public JdbcUpdateAffectedIncorrectNumberOfRowsException(String sql, int expected, int actual) {
		super("SQL update '" + sql + "' affected " + actual + " rows, not " + expected + " as expected");
		this.expected = expected;
		this.actual = actual;
	}


	/**
	 * 返回应该受到影响的行数。
	 */
	public int getExpectedRowsAffected() {
		return this.expected;
	}

	/**
	 * 返回实际受到影响的行数。
	 */
	public int getActualRowsAffected() {
		return this.actual;
	}

	/**
	 * 方法 `wasDataUpdated`：完成本类中与「was Data Updated」相关的职责。
	 */
	@Override
	public boolean wasDataUpdated() {
		return (getActualRowsAffected() > 0);
	}

}
