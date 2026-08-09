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

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

/**
 * 可引入拦截器链以向 Logger 显示被拦截调用详细信息的
 * AOP Alliance {@code MethodInterceptor}。
 *
 * <p>在方法进入和退出时记录完整调用细节，
 * 包括调用参数和调用计数。仅用于调试目的；
 * 纯跟踪目的请使用 {@code SimpleTraceInterceptor}
 * 或 {@code CustomizableTraceInterceptor}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleTraceInterceptor
 * @see CustomizableTraceInterceptor
 */
@SuppressWarnings("serial")
public class DebugInterceptor extends SimpleTraceInterceptor {

	private volatile long count;


	/**
	 * 使用静态 Logger 创建新的 DebugInterceptor。
	 */
	public DebugInterceptor() {
	}

	/**
	 * 根据给定标志使用动态或静态 Logger 创建新的 DebugInterceptor。
	 * @param useDynamicLogger 是否使用动态 Logger 或静态 Logger
	 * @see #setUseDynamicLogger
	 */
	public DebugInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}


	@Override
	public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
		synchronized (this) {
			this.count++;
		}
		return super.invoke(invocation);
	}

	@Override
	protected String getInvocationDescription(MethodInvocation invocation) {
		return invocation + "; count=" + this.count;
	}


	/**
	 * 返回本拦截器被调用的次数。
	 */
	public long getCount() {
		return this.count;
	}

	/**
	 * 将调用计数重置为零。
	 */
	public synchronized void resetCount() {
		this.count = 0;
	}

}
