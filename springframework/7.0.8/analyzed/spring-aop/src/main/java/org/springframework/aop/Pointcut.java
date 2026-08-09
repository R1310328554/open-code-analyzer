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
 * <p>A 切入点由 {@link ClassFilter} 和 {@link MethodMatcher} 组成。这些术语和切入点本身基本都组合起来构建组合（例如，可以通过
 * {@link org.springframework.aop.support.ComposablePointcut}）。
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */
public interface Pointcut {

	/**
	 * 返回此切入点的ClassFilter。
	 * @return {@code null}）
	 */
	ClassFilter getClassFilter();

	/**
	 * 返回此切入点的MethodMatcher。
	 * @return {@code null}）
	 */
	MethodMatcher getMethodMatcher();


	/**
	* 始终匹配的规范切入点实例。
	*/
	Pointcut TRUE = TruePointcut.INSTANCE;

}
