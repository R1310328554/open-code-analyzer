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
 * 在方法调用前触发的 advice。除非抛出 Throwable，
 * 否则无法阻止方法继续执行。
 *
 * @author Rod Johnson
 * @see AfterReturningAdvice
 * @see ThrowsAdvice
 */
public interface MethodBeforeAdvice extends BeforeAdvice {

	/**
	 * 给定方法被调用前的回调。
	 * @param method 被调用的方法
	 * @param args 方法参数
	 * @param target 方法调用的目标对象，可为 {@code null}
	 * @throws Throwable 若本对象希望中止调用。
	 * 若方法签名允许，抛出的异常会返回给调用方；否则会被包装为运行时异常。
	 */
	void before(Method method, @Nullable Object[] args, @Nullable Object target) throws Throwable;

}
