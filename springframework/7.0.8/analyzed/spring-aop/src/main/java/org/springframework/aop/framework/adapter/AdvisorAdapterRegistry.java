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

import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;

/**
 * Advisor 适配器注册表的接口。
 * <p><i>这是一个SPI接口，任何Spring用户都不能实现。</i>
 * @author Rod Johnson
 * @author Rob Harrop
 */
public interface AdvisorAdapterRegistry {

	/**
	 * 返回包含给定建议的 {@link Advisor}。 <p>默认情况下至少应该支持{@link
	 * org.aopalliance.intercept.MethodInterceptor}、{@link
	 * org.springframework.aop.MethodBeforeAdvice}、{@link
	 * org.springframework.aop.AfterReturningAdvice}、{@link
	 * org.springframework.aop.ThrowsAdvice}。
	 * @param advice 一个应该是建议的对象
	 * @return Advisor 包装给定的建议（绝不是 {@code null}；如果建议参数是 Advisor，则按原样返回）
	 * @throws UnknownAdviceTypeException 如果没有注册的顾问适配器可以包装假定的建议
	 */
	Advisor wrap(Object advice) throws UnknownAdviceTypeException;

	/**
	 * 返回 AOP Alliance MethodInterceptors 数组，以允许在基于拦截的框架中使用给定的 Advisor。 <p>不要担心与{@link
	 * Advisor}关联的切入点，如果它是{@link org.springframework.aop.PointcutAdvisor}：只需返回一个拦截器。
	 * @param advisor Advisor 寻找拦截器
	 * @return 用于公开此 Advisor 行为的 MethodInterceptor 数组
	 * @throws UnknownAdviceTypeException 如果任何注册的 AdvisorAdapter 都不理解 Advisor 类型
	 */
	MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException;

	/**
	 * 注册给定的 {@link AdvisorAdapter}。请注意，没有必要为 AOP 联盟拦截器或 Spring 建议注册适配器：这些必须由 {@code
	 * AdvisorAdapterRegistry} 实现自动识别。
	 * @param adapter 理解特定 Advisor 或 Advice 类型的 AdvisorAdapter
	 */
	void registerAdvisorAdapter(AdvisorAdapter adapter);

}
