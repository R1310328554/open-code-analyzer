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

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.util.Assert;

/**
 * 能够读取 Spring {@link Environment} 实例属性的只读 SpEL {@link PropertyAccessor}。
 *
 * @author Chris Beams
 * @since 3.1
 */
public class EnvironmentAccessor implements PropertyAccessor {

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[] {Environment.class};
	}

	@Override
	public boolean canRead(EvaluationContext context, @Nullable Object target, String name) throws AccessException {
		return (target instanceof Environment);
	}

	/**
	 * 通过在给定目标 Environment 上解析属性名来访问目标对象。
	 */
	@Override
	public TypedValue read(EvaluationContext context, @Nullable Object target, String name) throws AccessException {
		Assert.state(target instanceof Environment, "Target must be of type Environment");
		return new TypedValue(((Environment) target).getProperty(name));
	}

	/**
	 * 只读：返回 {@code false}。
	 */
	@Override
	public boolean canWrite(EvaluationContext context, @Nullable Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public void write(EvaluationContext context, @Nullable Object target, String name, @Nullable Object newValue)
			throws AccessException {

		throw new AccessException("The Environment is read-only");
	}

}
