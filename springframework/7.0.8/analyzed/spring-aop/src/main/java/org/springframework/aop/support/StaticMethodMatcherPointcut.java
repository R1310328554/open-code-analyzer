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

package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * 当我们想要强制子类实现 {@link MethodMatcher} 接口但子类想要成为切入点时，方便的超类。
 * <p>{@link #setClassFilter "classFilter"} 属性可以设置为自定义 {@link ClassFilter} 行为。默认为{@link
 * ClassFilter#TRUE}。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher implements Pointcut {

	private ClassFilter classFilter = ClassFilter.TRUE;


	/**
	 * 设置用于此切入点的 {@link ClassFilter}。默认为 {@link ClassFilter#TRUE}。
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.classFilter = classFilter;
	}

	/**
	 * 获取 Class Filter（`ClassFilter`）。
	 */
	@Override
	public ClassFilter getClassFilter() {
		return this.classFilter;
	}


	/**
	 * 获取 Method Matcher（`MethodMatcher`）。
	 */
	@Override
	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}
