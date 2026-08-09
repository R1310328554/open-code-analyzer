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
 * 允许扩展 Spring AOP 框架的接口，以允许处理新的 Advisor 和 Advice 类型。
 * <p>Implementing 对象可以从自定义通知类型创建 AOP 联盟拦截器，使这些通知类型能够在 Spring AOP 框架中使用，该框架在幕后使用拦截。
 * <p>大多数Spring用户没有必要实现这个接口；仅当您需要向 Spring 引入更多 Advisor 或 Advice 类型时才这样做。
 * @author Rod Johnson
 */
public interface AdvisorAdapter {

	/**
	 * 该适配器是否理解该建议对象？使用包含此建议作为参数的 Advisor 调用 {@code getInterceptors} 方法是否有效？
	 * @param advice 诸如 BeforeAdvice 之类的建议
	 * @return 该适配器理解给定的建议对象
	 * @see #getInterceptor(org.springframework.aop.Advisor)
	 * @see org.springframework.aop.BeforeAdvice
	 */
	boolean supportsAdvice(Advice advice);

	/**
	 * 返回一个 AOP Alliance MethodInterceptor，将给定建议的行为公开给基于拦截的 AOP 框架。
	 * <p>不用担心Advisor中包含的任何Pointcut； AOP 框架将负责检查切入点。
	 * @param advisor 顾问。对此对象的supportsAdvice()方法必须返回true
	 * @return AOP 联盟针对该 Advisor 的拦截器。无需为了效率而缓存实例，因为 AOP 框架会缓存建议链。
	 */
	MethodInterceptor getInterceptor(Advisor advisor);

}
