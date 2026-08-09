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

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.MethodMatcher;

/**
 * 静态方法匹配器的方便抽象超类，它不关心运行时的参数。
 * @author Rod Johnson
 */
public abstract class StaticMethodMatcher implements MethodMatcher {

	/**
	 * 判断是否 Runtime。
	 */
	@Override
	public final boolean isRuntime() {
		return false;
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public final boolean matches(Method method, Class<?> targetClass, @Nullable Object... args) {
		// 永远不应该被调用，因为 isRuntime() 返回 false
		throw new UnsupportedOperationException("Illegal MethodMatcher usage");
	}

}
