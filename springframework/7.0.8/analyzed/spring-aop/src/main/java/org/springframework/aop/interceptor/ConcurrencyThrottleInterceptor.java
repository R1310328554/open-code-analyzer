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

package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.util.ConcurrencyThrottleSupport;

/**
 * 用于限制并发访问的拦截器，如果达到指定的并发限制则阻止调用。
 * <p>可以应用于涉及大量使用系统资源的本地服务的方法，在限制特定服务的并发性而不是限制整个线程池（例如，Web容器的线程池）的场景中更有效。
 * <p> 此拦截器的默认并发限制为 1。指定“concurrencyLimit”bean 属性来更改此值。
 * @author Juergen Hoeller
 * @since 11.02.2004
 * @see #setConcurrencyLimit
 */
@SuppressWarnings("serial")
public class ConcurrencyThrottleInterceptor extends ConcurrencyThrottleSupport
		implements MethodInterceptor, Serializable {

	/**
	 * 创建并发限制为 1 的默认 {@code ConcurrencyThrottleInterceptor}。
	 */
	public ConcurrencyThrottleInterceptor() {
		this(1);
	}

	/**
	 * 创建具有给定并发限制的 {@code ConcurrencyThrottleInterceptor}。
	 * @since 7.0
	 */
	public ConcurrencyThrottleInterceptor(int concurrencyLimit) {
		setConcurrencyLimit(concurrencyLimit);
	}


	/**
	 * 调用（方法 `invoke`）。
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation methodInvocation) throws Throwable {
		beforeAccess();
		try {
			return methodInvocation.proceed();
		}
		finally {
			afterAccess();
		}
	}

}
