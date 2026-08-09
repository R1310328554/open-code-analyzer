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

package org.springframework.context.expression;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.jspecify.annotations.Nullable;

import org.springframework.core.KotlinDetector;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;

/**
 * 基于方法的 {@link org.springframework.expression.EvaluationContext}，
 * 为基于方法的调用提供显式支持。
 *
 * <p>通过以下别名暴露实际方法参数：
 * <ol>
 * <li>{@code pX}，其中 X 为参数索引（{@code p0} 表示第一个参数）</li>
 * <li>{@code aX}，其中 X 为参数索引（{@code a1} 表示第二个参数）</li>
 * <li>由可配置的 {@link ParameterNameDiscoverer} 发现的参数名</li>
 * </ol>
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 4.2
 */
public class MethodBasedEvaluationContext extends StandardEvaluationContext {

	private final Method method;

	private final @Nullable Object[] arguments;

	private final ParameterNameDiscoverer parameterNameDiscoverer;

	private boolean argumentsLoaded = false;


	public MethodBasedEvaluationContext(@Nullable Object rootObject, Method method, @Nullable Object[] arguments,
			ParameterNameDiscoverer parameterNameDiscoverer) {

		super(rootObject);
		this.method = method;
		this.arguments = (KotlinDetector.isSuspendingFunction(method) ?
				Arrays.copyOf(arguments, arguments.length - 1) : arguments);
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}


	@Override
	public @Nullable Object lookupVariable(String name) {
		Object variable = super.lookupVariable(name);
		if (variable != null) {
			return variable;
		}
		if (!this.argumentsLoaded) {
			lazyLoadArguments();
			this.argumentsLoaded = true;
			variable = super.lookupVariable(name);
		}
		return variable;
	}

	/**
	 * 仅在需要时加载参数信息。
	 */
	protected void lazyLoadArguments() {
		// Shortcut if no args need to be loaded
		if (ObjectUtils.isEmpty(this.arguments)) {
			return;
		}

		// Expose indexed variables as well as parameter names (if discoverable)
		@Nullable String[] paramNames = this.parameterNameDiscoverer.getParameterNames(this.method);
		int paramCount = (paramNames != null ? paramNames.length : this.method.getParameterCount());
		int argsCount = this.arguments.length;

		for (int i = 0; i < paramCount; i++) {
			Object value = null;
			if (argsCount > paramCount && i == paramCount - 1) {
				// Expose remaining arguments as vararg array for last parameter
				value = Arrays.copyOfRange(this.arguments, i, argsCount);
			}
			else if (argsCount > i) {
				// Actual argument found - otherwise left as null
				value = this.arguments[i];
			}
			setVariable("a" + i, value);
			setVariable("p" + i, value);
			if (paramNames != null && paramNames[i] != null) {
				setVariable(paramNames[i], value);
			}
		}
	}

}
