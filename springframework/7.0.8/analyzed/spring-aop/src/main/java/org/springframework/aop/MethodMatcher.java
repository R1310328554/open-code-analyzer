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

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * {@link Pointcut} 的部分：检查目标方法是否符合建议条件。 <p>A {@code MethodMatcher} 可以静态评估 <b></b> 或在
 * <b>runtime</b>（动态）。静态匹配涉及方法和（可能）方法属性。动态匹配还使特定调用的参数可用，以及运行连接点的先前建议的任何效果。 <p> 如果从 {@link
 * #isRuntime()} 方法返回 {@code false} 实现，则可以静态执行评估，并且对于此方法的所有调用，无论其参数如何，结果都将相同。这意味着如果 {@link
 * #isRuntime()} 方法返回 {@code false}，则永远不会调用 3-arg {@link #matches(Method, Class, Object[])}
 * 方法。 <p>如果从其 2-arg 实现{@link #matches(Method, Class)} 方法返回 {@code true} 及其 {@link
 * #isRuntime()} 方法返回 {@code true}，则在每次可能执行相关建议 </i> 之前，将立即调用 3-arg {@link #matches(Method,
 * Class, Object[])} 方法 <i> 之前以决定是否应运行该建议。所有先前的建议（例如拦截器链中的早期拦截器）都将运行，因此它们在参数或 {@code
 * ThreadLocal} 状态中产生的任何状态更改都将在评估时可用。 <p><strong>WARNING</strong>：此接口的具体实现必须提供 {@link
 * Object#equals(Object)}、{@link Object#has hCode()} 和{@link Object#toString()}
 * 的正确实现，便于允许在缓存场景中使用匹配器 - 例如，在 CGLIB 生成的代理中。从 Spring Framework 6.0.13 开始，{@code
 * toString()} 实现必须生成与实现 {@code equals()} 的逻辑一致的唯一字符串表示形式。有关示例，请参见框架内此接口的具体实现。
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
public interface MethodMatcher {

	/**
	 * <p>如果此方法返回 {@code false} 或 {@link #isRuntime()} 返回 {@code false}，则不会进行运行时检查（即不进行 {@link
	 * #matches(Method, Class, Object[])} 调用）。
	 * @param method 候选方法
	 * @param targetClass 目标类别
	 * @return
	 */
	boolean matches(Method method, Class<?> targetClass);

	/**
	 * 这个 {@code MethodMatcher} 是动态的吗？也就是说，即使 {@link #matches(Method, Class)} 返回 {@code
	 * true}，也必须在运行时通过 {@link #matches(Method, Class, Object[])} 方法进行最终检查吗？
	 * <p>可以在创建AOP代理时调用，不需要在每次方法调用之前再次调用。
	 * @return {@link #matches(Method, Class, Object[])} 进行运行时匹配
	 */
	boolean isRuntime();

	/**
	 * 检查此方法是否存在运行时（动态）匹配，该方法必须静态匹配。 <p> 仅当 {@link #matches(Method, Class)} 返回给定方法和目标类的 {@code 
	 * true}，并且 {@link #isRuntime()} 返回 {@code true} 时，才会调用此方法。 <p> 在建议链中较早的任何建议运行之后，在可能运行建议之前立
	 *  即调用。
	 * @param method 候选方法
	 * @param targetClass 目标类别
	 * @param args 方法的参数
	 * @return
	 * @see #matches(Method, Class)
	 */
	boolean matches(Method method, Class<?> targetClass, @Nullable Object... args);


	/**
	 * 匹配所有方法的 {@code MethodMatcher} 规范实例。
	 */
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}
