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

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * 针对特定 {@link Method} 抛出的 {@link Throwable} 判断是否重试的谓词。
 *
 * @author Juergen Hoeller
 * @since 7.0
 * @see MethodRetrySpec#predicate()
 */
@FunctionalInterface
public interface MethodRetryPredicate {

	/**
	 * 判断给定 {@code Method} 在抛出给定 {@code Throwable} 后是否应重试。
	 * @param method 可能重试的方法
	 * @param throwable 遇到的异常
	 */
	boolean shouldRetry(Method method, Throwable throwable);

	/**
	 * 为给定方法构建用于测试异常的 {@code Predicate}。
	 * @param method 要构建谓词的方法
	 */
	default Predicate<Throwable> forMethod(Method method) {
		return (t -> shouldRetry(method, t));
	}

}
