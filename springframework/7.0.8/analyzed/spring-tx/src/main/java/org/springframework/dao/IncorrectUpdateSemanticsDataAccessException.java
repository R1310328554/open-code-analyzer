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

/**
 * update 操作出现非预期情况但事务尚未回滚时抛出的数据访问异常。
 * 例如，在 RDBMS 中预期更新 1 行却实际更新了 3 行。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class IncorrectUpdateSemanticsDataAccessException extends InvalidDataAccessResourceUsageException {

	/**
	 * IncorrectUpdateSemanticsDataAccessException 构造函数。
	 * @param msg 详细消息
	 */
	public IncorrectUpdateSemanticsDataAccessException(String msg) {
		super(msg);
	}

	/**
	 * IncorrectUpdateSemanticsDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param cause 底层 API（如 JDBC）的根因
	 */
	public IncorrectUpdateSemanticsDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}


	/**
	 * 返回数据是否已被更新。
	 * 若返回 {@code false}，则无需回滚。
	 * <p>默认实现始终返回 {@code true}；
	 * 子类可覆盖。
	 */
	public boolean wasDataUpdated() {
		return true;
	}

}