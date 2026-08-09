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
 * API 使用不当（如未在执行前“编译”需编译的查询对象）时抛出。
 *
 * <p>表示 Java 数据访问框架层面的问题，
 * 而非底层数据访问基础设施的问题。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class InvalidDataAccessApiUsageException extends NonTransientDataAccessException {

	/**
	 * InvalidDataAccessApiUsageException 构造函数。
	 * @param msg 详细消息
	 */
	public InvalidDataAccessApiUsageException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * InvalidDataAccessApiUsageException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public InvalidDataAccessApiUsageException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}