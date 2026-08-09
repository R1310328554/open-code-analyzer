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
 * 发生乐观锁冲突时抛出。
 *
 * <p>本异常由 O/R 映射工具或自定义 DAO 实现抛出。
 * 乐观锁失败通常<i>不会</i>由数据库本身检测。
 *
 * @author Rod Johnson
 * @see PessimisticLockingFailureException
 */
@SuppressWarnings("serial")
public class OptimisticLockingFailureException extends ConcurrencyFailureException {

	/**
	 * OptimisticLockingFailureException 构造函数。
	 * @param msg 详细消息
	 */
	public OptimisticLockingFailureException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * OptimisticLockingFailureException 构造函数。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public OptimisticLockingFailureException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}