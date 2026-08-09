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
 * 无法检索到预期数据时抛出，例如通过已知标识符查找特定数据失败。
 * 本异常由 O/R 映射工具或 DAO 实现抛出。
 *
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
@SuppressWarnings("serial")
public class DataRetrievalFailureException extends NonTransientDataAccessException {

	/**
	 * DataRetrievalFailureException 构造函数。
	 * @param msg 详细消息
	 */
	public DataRetrievalFailureException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * DataRetrievalFailureException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public DataRetrievalFailureException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}