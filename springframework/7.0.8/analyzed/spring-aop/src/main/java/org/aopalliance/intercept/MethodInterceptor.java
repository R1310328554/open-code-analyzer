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

package org.aopalliance.intercept;

import org.jspecify.annotations.Nullable;

/**
 * <p>用户应实现{@link #invoke(MethodInvocation)}方法来修改原始行为。例如，以下类实现跟踪拦截器（跟踪拦截方法上的所有被调用）： <pre
 * class=code> 类 TracingInterceptor 实现 MethodInterceptor { 对象调用(MethodInitation i) throws
 * Throwable { System.out.println("方法"+i.getMethod()+" 在 "+ i.getThis()+" 上调用，参数为
 * "+i.getArguments());object ret=i.proceed();
 * System.out.println("方法"+i.getMethod()+"返回"+ret);返回ret； </pre>
 * @author Rod Johnson
 */
@FunctionalInterface
public interface MethodInterceptor extends Interceptor {

	/**
	 * 实现此方法以在调用之前和之后执行额外的处理。礼貌的实现肯定会调用 {@link Joinpoint#proceed()}。
	 * @param invocation 方法调用连接点
	 * @return {@link Joinpoint#proceed()} 的结果；可能会被拦截器拦截
	 * @throws Throwable 如果拦截器或目标对象抛出异常
	 */
	@Nullable Object invoke(MethodInvocation invocation) throws Throwable;

}
