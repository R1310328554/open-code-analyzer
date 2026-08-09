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

package org.springframework.transaction;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 遇到一般性事务系统错误（例如提交或回滚时）时抛出的异常。
 *
 * @author Juergen Hoeller
 * @since 24.03.2003
 */
@SuppressWarnings("serial")
public class TransactionSystemException extends TransactionException {

	private @Nullable Throwable applicationException;


	/**
	 * TransactionSystemException 的构造方法。
	 * @param msg 详细消息
	 */
	public TransactionSystemException(String msg) {
		super(msg);
	}

	/**
	 * TransactionSystemException 的构造方法。
	 * @param msg 详细消息
	 * @param cause 所用事务 API 的根因
	 */
	public TransactionSystemException(String msg, Throwable cause) {
		super(msg, cause);
	}


	/**
	 * 设置在此事务异常之前抛出的应用异常，
	 * 尽管覆盖了 TransactionSystemException，仍保留原始异常。
	 * @param ex 应用异常
	 * @throws IllegalStateException 若此 TransactionSystemException 已持有应用异常
	 */
	public void initApplicationException(Throwable ex) {
		Assert.notNull(ex, "Application exception must not be null");
		if (this.applicationException != null) {
			throw new IllegalStateException("Already holding an application exception: " + this.applicationException);
		}
		this.applicationException = ex;
	}

	/**
	 * 返回在此事务异常之前抛出的应用异常（若有）。
	 * @return 应用异常，未设置则返回 {@code null}
	 */
	public final @Nullable Throwable getApplicationException() {
		return this.applicationException;
	}

	/**
	 * 返回失败事务中首先抛出的异常：
	 * 即应用异常（若有），否则为 TransactionSystemException 自身的 cause。
	 * @return 原始异常，若无则返回 {@code null}
	 */
	public @Nullable Throwable getOriginalException() {
		return (this.applicationException != null ? this.applicationException : getCause());
	}

	@Override
	public boolean contains(@Nullable Class<?> exType) {
		return super.contains(exType) || (exType != null && exType.isInstance(this.applicationException));
	}

}
