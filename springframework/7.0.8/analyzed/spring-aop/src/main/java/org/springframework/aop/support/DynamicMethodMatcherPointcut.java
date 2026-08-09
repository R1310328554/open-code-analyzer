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
 * 便捷超类：强制子类实现 MethodMatcher 接口，
 * 同时子类本身作为切入点。
 * 可覆盖 getClassFilter() 方法以自定义 ClassFilter 行为。
 *
 * @author Rod Johnson
 */
public abstract class DynamicMethodMatcherPointcut extends DynamicMethodMatcher implements Pointcut {

	@Override
	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	@Override
	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}
