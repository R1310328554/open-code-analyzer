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

package org.springframework.aop.framework.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;

/**
 * 允许扩展 Spring AOP 框架以处理新 Advisor 与 Advice 类型的接口。
 *
 * <p>实现类可将自定义 Advice 类型转换为 AOP Alliance 拦截器，
 * 使这些 Advice 类型可在 Spring AOP 框架（底层基于拦截）中使用。
 *
 * <p>大多数 Spring 用户无需实现此接口；
 * 仅当需要向 Spring 引入更多 Advisor 或 Advice 类型时才需实现。
 *
 * @author Rod Johnson
 */
public interface AdvisorAdapter {

	/**
	 * 本适配器是否理解该 Advice 对象？
	 * 是否可传入包含此 Advice 的 Advisor 调用 {@code getInterceptors} 方法？
	 * @param advice 如 BeforeAdvice 之类的 Advice
	 * @return 本适配器是否理解给定 Advice 对象
	 * @see #getInterceptor(org.springframework.aop.Advisor)
	 * @see org.springframework.aop.BeforeAdvice
	 */
	boolean supportsAdvice(Advice advice);

	/**
	 * 返回 AOP Alliance MethodInterceptor，将给定 Advice 的行为
	 * 暴露给基于拦截的 AOP 框架。
	 * <p>无需关心 Advisor 中的 Pointcut；
	 * AOP 框架会负责检查切入点。
	 * @param advisor Advisor；supportsAdvice() 对此对象必须已返回 true
	 * @return 本 Advisor 对应的 AOP Alliance 拦截器。
	 * 无需为效率缓存实例，AOP 框架会缓存 Advice 链。
	 */
	MethodInterceptor getInterceptor(Advisor advisor);

}
