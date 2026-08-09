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
 * Java 类型与数据库类型不匹配时抛出的异常：
 * 例如尝试在 RDBMS 列中设置错误类型的对象。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class TypeMismatchDataAccessException extends InvalidDataAccessResourceUsageException {

	/**
	 * TypeMismatchDataAccessException 的构造方法。
	 * @param msg 详细消息
	 */
	public TypeMismatchDataAccessException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * TypeMismatchDataAccessException 的构造方法。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public TypeMismatchDataAccessException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}
