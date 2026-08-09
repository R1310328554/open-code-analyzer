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
 * 拦截通往目标对象途中对接口方法的调用，拦截器嵌套在目标之上。
 *
 * <p>用户应实现 {@link #invoke(MethodInvocation)} 以修改原始行为。
 * 例如，以下类实现跟踪拦截器（跟踪被拦截方法上的所有调用）：
 *
 * <pre class=code>
 * class TracingInterceptor implements MethodInterceptor {
 *   Object invoke(MethodInvocation i) throws Throwable {
 *     System.out.println("method "+i.getMethod()+" is called on "+
 *                        i.getThis()+" with args "+i.getArguments());
 *     Object ret=i.proceed();
 *     System.out.println("method "+i.getMethod()+" returns "+ret);
 *     return ret;
 *   }
 * }
 * </pre>
 *
 * @author Rod Johnson
 */
@FunctionalInterface
public interface MethodInterceptor extends Interceptor {

	/**
	 * 实现本方法以在调用前后执行额外处理。
	 * 规范的实现应调用 {@link Joinpoint#proceed()}。
	 * @param invocation 方法调用连接点
	 * @return 调用 {@link Joinpoint#proceed()} 的结果；可能被拦截器改写
	 * @throws Throwable 若拦截器或目标对象抛出异常
	 */
	@Nullable Object invoke(MethodInvocation invocation) throws Throwable;

}
