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

package org.springframework.aop;

/**
 * Spring 切入点核心抽象。
 *
 * <p>切入点由 {@link ClassFilter} 与 {@link MethodMatcher} 组成。
 * 这两个基本组件以及 Pointcut 本身均可组合构建更复杂的切入点
 * （例如通过 {@link org.springframework.aop.support.ComposablePointcut}）。
 *
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */
public interface Pointcut {

	/**
	 * 返回本切入点的 ClassFilter。
	 * @return ClassFilter（永不为 {@code null}）
	 */
	ClassFilter getClassFilter();

	/**
	 * 返回本切入点的 MethodMatcher。
	 * @return MethodMatcher（永不为 {@code null}）
	 */
	MethodMatcher getMethodMatcher();


	/**
	 * 始终匹配的 Pointcut 规范实例。
	 */
	Pointcut TRUE = TruePointcut.INSTANCE;

}
