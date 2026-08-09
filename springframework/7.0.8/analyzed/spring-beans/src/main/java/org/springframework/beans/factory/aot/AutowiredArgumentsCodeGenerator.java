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

package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

import org.springframework.javapoet.CodeBlock;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 用于应用 {@link AutowiredArguments} 的代码生成器。
 *
 * <p>生成形如 {@code args.get(0), args.get(1)} 或
 * {@code args.get(0, String.class), args.get(1, Integer.class)} 的代码。
 *
 * <p>仅当目标方法或构造器无歧义时才使用更简单的形式。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public class AutowiredArgumentsCodeGenerator {

	/** 目标类。 */
	private final Class<?> target;

	/** 目标可执行元素（构造器或方法）。 */
	private final Executable executable;


	public AutowiredArgumentsCodeGenerator(Class<?> target, Executable executable) {
		this.target = target;
		this.executable = executable;
	}


	public CodeBlock generateCode(Class<?>[] parameterTypes) {
		return generateCode(parameterTypes, 0, "args");
	}

	public CodeBlock generateCode(Class<?>[] parameterTypes, int startIndex) {
		return generateCode(parameterTypes, startIndex, "args");
	}

	public CodeBlock generateCode(Class<?>[] parameterTypes, int startIndex, String variableName) {
		Assert.notNull(parameterTypes, "'parameterTypes' must not be null");
		Assert.notNull(variableName, "'variableName' must not be null");
		boolean ambiguous = isAmbiguous();
		CodeBlock.Builder code = CodeBlock.builder();
		for (int i = startIndex; i < parameterTypes.length; i++) {
			code.add(i > startIndex ? ", " : "");
			if (!ambiguous) {
				code.add("$L.get($L)", variableName, i);
			}
			else {
				code.add("$L.get($L, $T.class)", variableName, i, parameterTypes[i]);
			}
		}
		return code.build();
	}

	private boolean isAmbiguous() {
		if (this.executable instanceof Constructor<?> constructor) {
			return Arrays.stream(this.target.getDeclaredConstructors())
					.filter(Predicate.not(constructor::equals))
					.anyMatch(this::hasSameParameterCount);
		}
		if (this.executable instanceof Method method) {
			return Arrays.stream(ReflectionUtils.getAllDeclaredMethods(this.target))
					.filter(Predicate.not(method::equals))
					.filter(candidate -> candidate.getName().equals(method.getName()))
					.anyMatch(this::hasSameParameterCount);
		}
		return true;
	}

	private boolean hasSameParameterCount(Executable executable) {
		return this.executable.getParameterCount() == executable.getParameterCount();
	}

}
