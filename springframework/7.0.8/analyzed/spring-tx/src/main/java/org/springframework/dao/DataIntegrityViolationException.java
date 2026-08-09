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
 * 执行 SQL 语句时无法正确映射给定数据而抛出，
 * 通常（但不限于）指 insert 或 update 违反完整性约束。
 * 注意：这并非纯粹的关系型概念；
 * 唯一主键等完整性约束为大多数数据库类型所要求。
 *
 * <p>作为更具体异常（如 {@link DuplicateKeyException}）的超类。
 * 但通常建议直接处理 {@code DataIntegrityViolationException}，
 * 而非依赖特定异常子类。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class DataIntegrityViolationException extends NonTransientDataAccessException {

	/**
	 * DataIntegrityViolationException 构造函数。
	 * @param msg 详细消息
	 */
	public DataIntegrityViolationException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * DataIntegrityViolationException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public DataIntegrityViolationException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}