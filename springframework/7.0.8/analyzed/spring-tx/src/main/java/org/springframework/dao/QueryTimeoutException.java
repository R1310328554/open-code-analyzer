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
 * 查询超时时抛出的异常。具体原因取决于所用数据库 API，
 * 最可能是在数据库中断或停止尚未完成的查询处理时抛出。
 *
 * <p>本异常可由捕获原生数据库异常的用户代码抛出，
 * 也可由异常转换机制抛出。
 *
 * @author Thomas Risberg
 * @since 3.1
 */
@SuppressWarnings("serial")
public class QueryTimeoutException extends TransientDataAccessException {

	/**
	 * QueryTimeoutException 的构造方法。
	 * @param msg 详细消息
	 */
	public QueryTimeoutException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * QueryTimeoutException 的构造方法。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public QueryTimeoutException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}
