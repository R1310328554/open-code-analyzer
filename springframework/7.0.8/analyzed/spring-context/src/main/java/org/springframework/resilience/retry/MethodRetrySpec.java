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

package org.springframework.resilience.retry;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

import org.springframework.util.ExceptionTypeFilter;

/**
 * 给定方法重试尝试的规范，组合常见重试特征。
 * 大致对应 {@link org.springframework.resilience.annotation.Retryable} 的注解属性。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 7.0
 * @param includes 应尝试重试的适用异常类型
 * @param excludes 应避免重试的不适用异常类型
 * @param predicate 过滤适用方法异常的谓词
 * @param maxRetries 最大重试次数
 * @param timeout 初始调用及后续重试（含延迟）允许的最大耗时
 * @param delay 初始调用后的基础延迟
 * @param jitter 下次重试的抖动值
 * @param multiplier 下次重试延迟的乘数
 * @param maxDelay 任意重试尝试的最大延迟
 * @see AbstractRetryInterceptor#getRetrySpec
 * @see SimpleRetryInterceptor#SimpleRetryInterceptor(MethodRetrySpec)
 * @see org.springframework.resilience.annotation.Retryable
 */
public record MethodRetrySpec(
		Collection<Class<? extends Throwable>> includes,
		Collection<Class<? extends Throwable>> excludes,
		MethodRetryPredicate predicate,
		long maxRetries,
		Duration timeout,
		Duration delay,
		Duration jitter,
		double multiplier,
		Duration maxDelay) {

	/**
	 * 使用给定参数构造新的 {@code MethodRetrySpec}。
	 */
	public MethodRetrySpec(MethodRetryPredicate predicate, long maxRetries, Duration delay) {
		this(predicate, maxRetries, delay, Duration.ZERO, 1.0, Duration.ofMillis(Long.MAX_VALUE));
	}

	/**
	 * 使用给定参数构造新的 {@code MethodRetrySpec}。
	 */
	public MethodRetrySpec(MethodRetryPredicate predicate, long maxRetries, Duration delay,
			Duration jitter, double multiplier, Duration maxDelay) {

		this(Collections.emptyList(), Collections.emptyList(), predicate, maxRetries, Duration.ZERO,
				delay, jitter, multiplier, maxDelay);
	}

	/**
	 * 使用给定参数构造新的 {@code MethodRetrySpec}。
	 * @deprecated 自 Spring Framework 7.0.2 起，请改用
	 * {@link #MethodRetrySpec(Collection, Collection, MethodRetryPredicate, long, Duration, Duration, Duration, double, Duration)}
	 */
	@Deprecated(since = "7.0.2", forRemoval = true)
	public MethodRetrySpec(Collection<Class<? extends Throwable>> includes,
			Collection<Class<? extends Throwable>> excludes, MethodRetryPredicate predicate,
			long maxRetries, Duration delay, Duration jitter, double multiplier, Duration maxDelay) {

		this(includes, excludes, predicate, maxRetries, Duration.ZERO, delay, jitter, multiplier, maxDelay);
	}


	MethodRetryPredicate combinedPredicate() {
		ExceptionTypeFilter exceptionFilter = new ExceptionTypeFilter(this.includes, this.excludes);
		return (method, throwable) -> exceptionFilter.match(throwable, true) &&
				this.predicate.shouldRetry(method, throwable);
	}

}
