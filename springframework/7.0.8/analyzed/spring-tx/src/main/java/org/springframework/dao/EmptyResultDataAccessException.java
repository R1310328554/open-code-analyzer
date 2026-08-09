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
 * 预期结果至少包含一行（或一个元素），但实际返回零行（或零元素）时抛出的数据访问异常。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see IncorrectResultSizeDataAccessException
 */
@SuppressWarnings("serial")
public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

	/**
	 * EmptyResultDataAccessException 构造函数。
	 * @param expectedSize 预期结果大小
	 */
	public EmptyResultDataAccessException(int expectedSize) {
		super(expectedSize, 0);
	}

	/**
	 * EmptyResultDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param expectedSize 预期结果大小
	 */
	public EmptyResultDataAccessException(@Nullable String msg, int expectedSize) {
		super(msg, expectedSize, 0);
	}

	/**
	 * EmptyResultDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param expectedSize 预期结果大小
	 * @param ex 被包装的异常
	 */
	public EmptyResultDataAccessException(@Nullable String msg, int expectedSize, Throwable ex) {
		super(msg, expectedSize, 0, ex);
	}

}