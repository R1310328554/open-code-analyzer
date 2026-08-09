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

package org.springframework.cache.interceptor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;

/**
 * 缓存专用的 SpEL 求值上下文：以惰性方式将方法参数暴露为 SpEL 变量，
 * 避免在不需要时解析字节码以发现参数名。
 *
 * <p>同时维护一组「不可用变量」——一旦访问即抛出异常。
 * 这有助于在并非所有潜在变量都已就绪时，仍能验证 condition 表达式不应匹配。
 *
 * <p>为减少对象创建，使用包级可见构造器（而非单独的延迟执行闭包类）。
 *
 * @author Costin Leau
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 3.1
 */
class CacheEvaluationContext extends MethodBasedEvaluationContext {

	/** 标记为不可用的 SpEL 变量名集合；访问时抛出 {@link VariableNotAvailableException}。 */
	private final Set<String> unavailableVariables = new HashSet<>(1);


	CacheEvaluationContext(@Nullable Object rootObject, Method method, @Nullable Object[] arguments,
			ParameterNameDiscoverer parameterNameDiscoverer) {

		super(rootObject, method, arguments, parameterNameDiscoverer);
	}


	/**
	 * 将指定变量名标记为不可用。
	 * <p>任何试图访问该变量的表达式都应导致求值失败。
	 * <p>这允许在变量尚未可用时，仍能校验可能引用该变量的表达式。
	 * @param name the variable name
	 */
	public void addUnavailableVariable(String name) {
		this.unavailableVariables.add(name);
	}


	/**
	 * 仅在需要时才加载方法参数信息（惰性求值）。
	 */
	@Override
	public @Nullable Object lookupVariable(String name) {
		if (this.unavailableVariables.contains(name)) {
			throw new VariableNotAvailableException(name);
		}
		return super.lookupVariable(name);
	}

}
