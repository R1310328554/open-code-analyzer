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
import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StopWatch;

/**
 * 简单的AOP联盟{@code MethodInterceptor}用于性能监控。该拦截器对拦截的方法调用没有任何影响。
 * <p> 使用 {@code StopWatch} 进行实际性能测量。
 * @author Rod Johnson
 * @author Dmitriy Kopylenko
 * @author Rob Harrop
 * @see org.springframework.util.StopWatch
 */
@SuppressWarnings("serial")
public class PerformanceMonitorInterceptor extends AbstractMonitoringInterceptor {

	/**
	 * 使用静态记录器创建一个新的 PerformanceMonitorInterceptor。
	 */
	public PerformanceMonitorInterceptor() {
	}

	/**
	 * 根据给定的标志，使用动态或静态记录器创建一个新的 PerformanceMonitorInterceptor。
	 * @param useDynamicLogger 是否使用动态记录器或静态记录器
	 * @see #setUseDynamicLogger
	 */
	public PerformanceMonitorInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}


	/**
	 * 调用：Under Trace（方法 `invokeUnderTrace`）。
	 */
	@Override
	protected @Nullable Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
		String name = createInvocationTraceName(invocation);
		StopWatch stopWatch = new StopWatch(name);
		stopWatch.start(name);
		try {
			return invocation.proceed();
		}
		finally {
			stopWatch.stop();
			writeToLog(logger, stopWatch.shortSummary());
		}
	}

}
