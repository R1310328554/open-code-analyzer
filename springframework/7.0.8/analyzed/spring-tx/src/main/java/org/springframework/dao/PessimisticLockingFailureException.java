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
 * 发生悲观锁冲突时抛出的异常。
 * 若遇到对应的数据库错误，由 Spring 的 SQLException 转换机制抛出。
 *
 * <p>作为更具体异常（例如 {@link CannotAcquireLockException}）的父类。
 * 但通常建议直接处理 {@code PessimisticLockingFailureException}，
 * 而非依赖特定异常子类。
 *
 * @author Thomas Risberg
 * @since 1.2
 * @see OptimisticLockingFailureException
 */
@SuppressWarnings("serial")
public class PessimisticLockingFailureException extends ConcurrencyFailureException {

	/**
	 * PessimisticLockingFailureException 的构造方法。
	 * @param msg 详细消息
	 */
	public PessimisticLockingFailureException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * PessimisticLockingFailureException 的构造方法。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public PessimisticLockingFailureException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}
