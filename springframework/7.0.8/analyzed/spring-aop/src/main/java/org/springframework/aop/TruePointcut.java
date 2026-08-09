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

/**
 * 始终匹配的规范切入点实例。
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
final class TruePointcut implements Pointcut, Serializable {

	/**
	 * 创建 `TruePointcut` 的新实例。
	 */
	public static final TruePointcut INSTANCE = new TruePointcut();

	/**
	* 强制执行单例模式。
	*/
	private TruePointcut() {
	}

	/**
	 * 获取类过滤器（`ClassFilter`）。
	 */
	@Override
	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	/**
	 * 获取方法匹配器（`MethodMatcher`）。
	 */
	@Override
	public MethodMatcher getMethodMatcher() {
		return MethodMatcher.TRUE;
	}

	/**
	* 需要支持序列化。替换反序列化时的规范实例，保护单例模式。替代 {@code equals()}。
	*/
	private Object readResolve() {
		return INSTANCE;
	}

	/**
	* 返回字符串表示。
	*/
	@Override
	public String toString() {
		return "Pointcut.TRUE";
	}

}
