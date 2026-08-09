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
 * insert 或 update 违反主键或唯一约束时抛出。
 * 注意：这并非必然为纯粹的关系型概念；
 * 唯一主键为大多数数据库类型所要求。
 *
 * <p>建议改为处理通用的 {@link DataIntegrityViolationException}，
 * 其语义涵盖更广的约束违反。
 *
 * @author Thomas Risberg
 */
@SuppressWarnings("serial")
public class DuplicateKeyException extends DataIntegrityViolationException {

	/**
	 * DuplicateKeyException 构造函数。
	 * @param msg 详细消息
	 */
	public DuplicateKeyException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * DuplicateKeyException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public DuplicateKeyException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}