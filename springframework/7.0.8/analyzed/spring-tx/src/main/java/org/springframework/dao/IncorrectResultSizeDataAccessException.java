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

package org.springframework.dao;

import org.jspecify.annotations.Nullable;

/**
 * 结果大小与预期不符时抛出的数据访问异常，
 * 例如预期单行却得到 0 行或多于 1 行。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.0.2
 * @see EmptyResultDataAccessException
 */
@SuppressWarnings("serial")
public class IncorrectResultSizeDataAccessException extends DataRetrievalFailureException {

	private final int expectedSize;

	private final int actualSize;


	/**
	 * IncorrectResultSizeDataAccessException 构造函数。
	 * @param expectedSize 预期结果大小
	 */
	public IncorrectResultSizeDataAccessException(int expectedSize) {
		super("Incorrect result size: expected " + expectedSize);
		this.expectedSize = expectedSize;
		this.actualSize = -1;
	}

	/**
	 * IncorrectResultSizeDataAccessException 构造函数。
	 * @param expectedSize 预期结果大小
	 * @param actualSize 实际结果大小（未知时为 -1）
	 */
	public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
		super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
		this.expectedSize = expectedSize;
		this.actualSize = actualSize;
	}

	/**
	 * IncorrectResultSizeDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param expectedSize 预期结果大小
	 */
	public IncorrectResultSizeDataAccessException(String msg, int expectedSize) {
		super(msg);
		this.expectedSize = expectedSize;
		this.actualSize = -1;
	}

	/**
	 * IncorrectResultSizeDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param expectedSize 预期结果大小
	 * @param ex 被包装的异常
	 */
	public IncorrectResultSizeDataAccessException(@Nullable String msg, int expectedSize, @Nullable Throwable ex) {
		super(msg, ex);
		this.expectedSize = expectedSize;
		this.actualSize = -1;
	}

	/**
	 * IncorrectResultSizeDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param expectedSize 预期结果大小
	 * @param actualSize 实际结果大小（未知时为 -1）
	 */
	public IncorrectResultSizeDataAccessException(@Nullable String msg, int expectedSize, int actualSize) {
		super(msg);
		this.expectedSize = expectedSize;
		this.actualSize = actualSize;
	}

	/**
	 * IncorrectResultSizeDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param expectedSize 预期结果大小
	 * @param actualSize 实际结果大小（未知时为 -1）
	 * @param ex 被包装的异常
	 */
	public IncorrectResultSizeDataAccessException(@Nullable String msg, int expectedSize, int actualSize, @Nullable Throwable ex) {
		super(msg, ex);
		this.expectedSize = expectedSize;
		this.actualSize = actualSize;
	}


	/**
	 * 返回预期结果大小。
	 */
	public int getExpectedSize() {
		return this.expectedSize;
	}

	/**
	 * 返回实际结果大小（未知时为 -1）。
	 */
	public int getActualSize() {
		return this.actualSize;
	}

}