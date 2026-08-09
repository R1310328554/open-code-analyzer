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

package org.springframework.aop;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * 返回后通知仅在正常方法返回时调用，如果抛出异常则不会调用。此类通知可以看到返回值，但不能更改它。
 * @author Rod Johnson
 * @see MethodBeforeAdvice
 * @see ThrowsAdvice
 */
public interface AfterReturningAdvice extends AfterAdvice {

	/**
	* 给定方法成功返回后的回调。
	* @param returnValue 该方法返回的值（如果有）
	* @param method 被调用的方法
	* @param args 该方法的参数
	* @param target 方法调用的目标。可能是 {@code null}。
	* @throws Throwable 如果该对象希望中止调用。如果方法签名允许，则抛出的任何异常都将返回给调用者。否则，异常将被包装为运行时异常。
	*/
	void afterReturning(@Nullable Object returnValue, Method method, @Nullable Object[] args, @Nullable Object target) throws Throwable;

}
