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
 * {@link Pointcut} 的组成部分：检查目标方法是否符合 advice 条件。
 *
 * <p>{@code MethodMatcher} 可在<b>静态</b>或<b>运行时</b>（动态）评估。
 * 静态匹配涉及方法及（可能的）方法属性；动态匹配还会提供具体调用的参数，
 * 以及先前作用于该连接点的 advice 所产生的影响。
 *
 * <p>若实现的 {@link #isRuntime()} 返回 {@code false}，
 * 则可静态评估，且无论参数如何，对该方法的所有调用结果相同。
 * 这意味着 {@link #isRuntime()} 为 {@code false} 时，
 * 三参数 {@link #matches(Method, Class, Object[])} 永远不会被调用。
 *
 * <p>若两参数 {@link #matches(Method, Class)} 返回 {@code true}
 * 且 {@link #isRuntime()} 返回 {@code true}，则三参数
 * {@link #matches(Method, Class, Object[])} 会在<i>每次可能执行相关 advice 之前</i>被调用，
 * 以决定是否运行 advice。此前所有 advice（例如拦截器链中较早的拦截器）均已执行，
 * 因此它们在参数或 {@code ThreadLocal} 状态中产生的变更在评估时可用。
 *
 * <p><strong>警告</strong>：本接口的具体实现必须正确实现
 * {@link Object#equals(Object)}、{@link Object#hashCode()} 与 {@link Object#toString()}，
 * 以便在缓存场景（例如 CGLIB 生成的代理）中使用。
 * 自 Spring Framework 6.0.13 起，{@code toString()} 必须生成与 {@code equals()} 逻辑一致的唯一字符串表示。
 * 可参考框架内本接口的具体实现示例。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
public interface MethodMatcher {

	/**
	 * 静态检查给定方法是否匹配。
	 * <p>若本方法返回 {@code false}，或 {@link #isRuntime()} 返回 {@code false}，
	 * 则不会进行运行时检查（即不会调用 {@link #matches(Method, Class, Object[])}）。
	 * @param method 候选方法
	 * @param targetClass 目标类
	 * @return 该方法是否静态匹配
	 */
	boolean matches(Method method, Class<?> targetClass);

	/**
	 * 本 {@code MethodMatcher} 是否为动态匹配，即即使 {@link #matches(Method, Class)}
	 * 返回 {@code true}，是否仍须在运行时通过 {@link #matches(Method, Class, Object[])} 做最终检查？
	 * <p>可在创建 AOP 代理时调用，无需在每次方法调用前再次调用。
	 * @return 若静态匹配通过，是否仍需运行时匹配
	 */
	boolean isRuntime();

	/**
	 * 检查该方法是否存在运行时（动态）匹配；该方法必须已通过静态匹配。
	 * <p>仅当 {@link #matches(Method, Class)} 对给定方法与目标类返回 {@code true}
	 * 且 {@link #isRuntime()} 返回 {@code true} 时调用。
	 * <p>在 advice 链中较早的 advice 执行完毕后、可能运行 advice 之前立即调用。
	 * @param method 候选方法
	 * @param targetClass 目标类
	 * @param args 方法参数
	 * @return 是否存在运行时匹配
	 * @see #matches(Method, Class)
	 */
	boolean matches(Method method, Class<?> targetClass, @Nullable Object... args);


	/**
	 * 匹配所有方法的 {@code MethodMatcher} 规范实例。
	 */
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}
