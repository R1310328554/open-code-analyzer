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

import org.springframework.dao.DataRetrievalFailureException;

/**
 * 当结果集列数不正确时抛出的数据访问异常，
 * 例如期望单列却得到 0 列或多于 1 列。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.dao.IncorrectResultSizeDataAccessException
 */
@SuppressWarnings("serial")
public class IncorrectResultSetColumnCountException extends DataRetrievalFailureException {

	private final int expectedCount;

	private final int actualCount;


	/**
	 * IncorrectResultSetColumnCountException 构造器。
	 * @param expectedCount 期望列数
	 * @param actualCount 实际列数
	 */
	public IncorrectResultSetColumnCountException(int expectedCount, int actualCount) {
		super("Incorrect column count: expected " + expectedCount + ", actual " + actualCount);
		this.expectedCount = expectedCount;
		this.actualCount = actualCount;
	}

	/**
	 * IncorrectResultCountDataAccessException 构造器。
	 * @param msg 详细消息
	 * @param expectedCount 期望列数
	 * @param actualCount 实际列数
	 */
	public IncorrectResultSetColumnCountException(String msg, int expectedCount, int actualCount) {
		super(msg);
		this.expectedCount = expectedCount;
		this.actualCount = actualCount;
	}


	/**
	 * 返回期望列数。
	 */
	public int getExpectedCount() {
		return this.expectedCount;
	}

	/**
	 * 返回实际列数。
	 */
	public int getActualCount() {
		return this.actualCount;
	}

}
