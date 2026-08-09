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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * 匹配所有方法的规范 MethodMatcher 实例。
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
final class TrueMethodMatcher implements MethodMatcher, Serializable {

	/**
	 * 创建 `TrueMethodMatcher` 的新实例。
	 */
	public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();


	/**
	* 强制执行单例模式。
	*/
	private TrueMethodMatcher() {
	}


	/**
	 * 判断是否为Runtime。
	 */
	@Override
	public boolean isRuntime() {
		return false;
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return true;
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass, @Nullable Object... args) {
		// 不应该被调用，因为 isRuntime 返回 false。
		throw new UnsupportedOperationException();
	}


	/**
	* 返回字符串表示。
	*/
	@Override
	public String toString() {
		return "MethodMatcher.TRUE";
	}

	/**
	* 需要支持序列化。替换反序列化时的规范实例，保护单例模式。替代 {@code equals()}。
	*/
	private Object readResolve() {
		return INSTANCE;
	}

}
