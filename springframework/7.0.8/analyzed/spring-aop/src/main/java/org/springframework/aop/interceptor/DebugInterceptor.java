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
 * AOP 联盟 {@code MethodInterceptor} 可以在链中引入，以向记录器显示有关拦截的调用的详细信息。
 * <p>记录方法入口和方法出口的完整调用详细信息，包括调用参数和调用计数。这仅用于调试目的；使用 {@code SimpleTraceInterceptor} 或 {@code 
 * CustomizableTraceInterceptor} 进行纯粹的跟踪目的。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleTraceInterceptor
 * @see CustomizableTraceInterceptor
 */
@SuppressWarnings("serial")
public class DebugInterceptor extends SimpleTraceInterceptor {

	/** `count`：该类的成员状态。 */
	private volatile long count;


	/**
	 * 使用静态记录器创建一个新的 DebugInterceptor。
	 */
	public DebugInterceptor() {
	}

	/**
	 * 根据给定的标志，使用动态或静态记录器创建一个新的 DebugInterceptor。
	 * @param useDynamicLogger 是否使用动态记录器或静态记录器
	 * @see #setUseDynamicLogger
	 */
	public DebugInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}


	/**
	 * 调用（方法 `invoke`）。
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
		synchronized (this) {
			this.count++;
		}
		return super.invoke(invocation);
	}

	/**
	 * 获取 Invocation Description（`InvocationDescription`）。
	 */
	@Override
	protected String getInvocationDescription(MethodInvocation invocation) {
		return invocation + "; count=" + this.count;
	}


	/**
	 * 返回该拦截器被调用的次数。
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
