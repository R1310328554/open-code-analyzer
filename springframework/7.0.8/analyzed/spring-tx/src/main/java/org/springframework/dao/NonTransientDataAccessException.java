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
 * 被视为非瞬态的数据访问异常层次结构的根类——
 * 除非修正异常原因，否则重试相同操作仍会失败。
 *
 * @author Thomas Risberg
 * @since 2.5
 * @see java.sql.SQLNonTransientException
 */
@SuppressWarnings("serial")
public abstract class NonTransientDataAccessException extends DataAccessException {

	/**
	 * NonTransientDataAccessException 构造函数。
	 * @param msg 详细消息
	 */
	public NonTransientDataAccessException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * NonTransientDataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param cause 根因（通常来自底层数据访问 API，如 JDBC）
	 */
	public NonTransientDataAccessException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}