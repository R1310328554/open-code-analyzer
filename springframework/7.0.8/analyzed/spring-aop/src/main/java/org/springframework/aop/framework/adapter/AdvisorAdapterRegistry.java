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
 * Advisor 适配器注册表接口。
 *
 * <p><i>此为 SPI 接口，Spring 用户不应实现。</i>
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */
public interface AdvisorAdapterRegistry {

	/**
	 * 返回包装给定 Advice 的 {@link Advisor}。
	 * <p>默认至少应支持
	 * {@link org.aopalliance.intercept.MethodInterceptor}、
	 * {@link org.springframework.aop.MethodBeforeAdvice}、
	 * {@link org.springframework.aop.AfterReturningAdvice}、
	 * {@link org.springframework.aop.ThrowsAdvice}。
	 * @param advice 应为 Advice 的对象
	 * @return 包装给定 Advice 的 Advisor（永不为 {@code null}；
	 * 若 advice 参数本身为 Advisor，则原样返回）
	 * @throws UnknownAdviceTypeException 若无已注册适配器可包装该 Advice
	 */
	Advisor wrap(Object advice) throws UnknownAdviceTypeException;

	/**
	 * 返回 AOP Alliance MethodInterceptor 数组，
	 * 使给定 Advisor 可用于基于拦截的框架。
	 * <p>无需关心 {@link Advisor} 关联的切入点（若为
	 * {@link org.springframework.aop.PointcutAdvisor}）：直接返回拦截器即可。
	 * @param advisor 要查找拦截器的 Advisor
	 * @return 暴露本 Advisor 行为的 MethodInterceptor 数组
	 * @throws UnknownAdviceTypeException 若无已注册 AdvisorAdapter 理解该 Advisor 类型
	 */
	MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException;

	/**
	 * 注册给定 {@link AdvisorAdapter}。
	 * 注意：AOP Alliance Interceptor 或 Spring Advice 无需注册适配器；
	 * {@code AdvisorAdapterRegistry} 实现必须自动识别它们。
	 * @param adapter 理解特定 Advisor 或 Advice 类型的 AdvisorAdapter
	 */
	void registerAdvisorAdapter(AdvisorAdapter adapter);

}
