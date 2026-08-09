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
 * 各类数据访问并发失败时抛出的异常。
 *
 * <p>本异常提供针对具体失败类型的子类，
 * 尤其是乐观锁与悲观锁相关失败。
 *
 * @author Thomas Risberg
 * @since 1.1
 * @see OptimisticLockingFailureException
 * @see PessimisticLockingFailureException
 */
@SuppressWarnings("serial")
public class ConcurrencyFailureException extends TransientDataAccessException {

	/**
	 * ConcurrencyFailureException 构造函数。
	 * @param msg 详细消息
	 */
	public ConcurrencyFailureException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * ConcurrencyFailureException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public ConcurrencyFailureException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}