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
 * 数据访问操作本身成功，但后续清理失败时抛出。
 *
 * <p>例如，JDBC Connection 使用成功后无法关闭时，
 * 可能抛出本异常或其子类。
 *
 * <p>注意：数据访问代码可能在 {@code finally} 块中执行资源清理，
 * 因此可能记录清理失败而非重新抛出，以保留原始数据访问异常（若有）。
 *
 * @author Rod Johnson
 * @deprecated as of 6.0.3 since it is not in use within core JDBC/ORM support
 */
@Deprecated(since = "6.0.3")
@SuppressWarnings("serial")
public class CleanupFailureDataAccessException extends NonTransientDataAccessException {

	/**
	 * CleanupFailureDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param cause 底层数据访问 API（如 JDBC）的根因
	 */
	public CleanupFailureDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}