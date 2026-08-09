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

package org.springframework.scheduling.annotation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

/**
 * 透传 {@code Future} 句柄，可用于声明 {@code Future} 返回类型
 * 以支持异步执行的方法签名。
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.0
 * @param <V> 值类型
 * @see Async
 * @see #forValue(Object)
 * @see #forExecutionException(Throwable)
 * @deprecated 自 6.0 起，请使用 {@link CompletableFuture}
 */
@Deprecated(since = "6.0")
public class AsyncResult<V> implements Future<V> {

	private final @Nullable V value;

	private final @Nullable Throwable executionException;


	/**
	 * 创建新的 AsyncResult 持有者。
	 * @param value 要透传的值
	 */
	public AsyncResult(@Nullable V value) {
		this(value, null);
	}

	/**
	 * Create a new AsyncResult holder.
	 * @param value the value to pass through
	 */
	private AsyncResult(@Nullable V value, @Nullable Throwable ex) {
		this.value = value;
		this.executionException = ex;
	}


	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public @Nullable V get() throws ExecutionException {
		if (this.executionException != null) {
			throw (this.executionException instanceof ExecutionException execEx ? execEx :
					new ExecutionException(this.executionException));
		}
		return this.value;
	}

	@Override
	public @Nullable V get(long timeout, TimeUnit unit) throws ExecutionException {
		return get();
	}


	/**
	 * 创建新的异步结果，从 {@link Future#get()} 暴露给定值。
	 * @param value 要暴露的值
	 * @since 4.2
	 * @see Future#get()
	 */
	public static <V> Future<V> forValue(V value) {
		return new AsyncResult<>(value, null);
	}

	/**
	 * 创建新的异步结果，从 {@link Future#get()} 将给定异常
	 * 作为 {@link ExecutionException} 暴露。
	 * @param ex 要暴露的异常（可为预构建的 {@link ExecutionException}
	 * 或将被包装为 {@link ExecutionException} 的原因）
	 * @since 4.2
	 * @see ExecutionException
	 */
	public static <V> Future<V> forExecutionException(Throwable ex) {
		return new AsyncResult<>(null, ex);
	}

}
