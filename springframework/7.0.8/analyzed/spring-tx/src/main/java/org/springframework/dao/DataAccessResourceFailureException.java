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
 * 资源完全不可用时抛出的数据访问异常，
 * 例如无法通过 JDBC 连接数据库。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 */
@SuppressWarnings("serial")
public class DataAccessResourceFailureException extends NonTransientDataAccessResourceException {

	/**
	 * DataAccessResourceFailureException 构造函数。
	 * @param msg 详细消息
	 */
	public DataAccessResourceFailureException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * DataAccessResourceFailureException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public DataAccessResourceFailureException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}