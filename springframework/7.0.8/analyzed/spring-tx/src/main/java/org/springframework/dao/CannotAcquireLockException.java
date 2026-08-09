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
 * 更新过程中获取锁失败时抛出，
 * 例如在 "select for update" 语句执行期间。
 *
 * <p>建议改为处理通用的 {@link PessimisticLockingFailureException}，
 * 其语义涵盖更广的锁相关失败。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class CannotAcquireLockException extends PessimisticLockingFailureException {

	/**
	 * CannotAcquireLockException 构造函数。
	 * @param msg 详细消息
	 */
	public CannotAcquireLockException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * CannotAcquireLockException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public CannotAcquireLockException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}