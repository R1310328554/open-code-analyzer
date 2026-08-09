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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

/**
 * 用作简单 <b>cflow</b> 风格切入点的切入点与方法匹配器。
 *
 * <p>每个配置的方法名模式可为精确方法名或模式
 * （支持的模式风格见 {@link #isMatch(String, String)}）。
 *
 * <p>注意：评估此类切入点比普通切入点慢 10–15 倍，
 * 但在某些场景下很有用。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see NameMatchMethodPointcut
 * @see JdkRegexpMethodPointcut
 */
@SuppressWarnings("serial")
public class ControlFlowPointcut implements Pointcut, ClassFilter, MethodMatcher, Serializable {

	/**
	 * 用于匹配的类。
	 * @since 6.1
	 */
	protected final Class<?> clazz;

	/**
	 * 用于匹配的不重复方法名模式不可变列表。
	 * @since 6.1
	 */
	protected final List<String> methodNamePatterns;

	private final AtomicInteger evaluationCount = new AtomicInteger();


	/**
	 * 构造匹配给定类下所有控制流的新切入点。
	 * @param clazz 类
	 */
	public ControlFlowPointcut(Class<?> clazz) {
		this(clazz, (String) null);
	}

	/**
	 * 构造匹配给定类中符合方法名模式的方法下所有调用的新切入点。
	 * <p>若未给定方法名模式，则匹配给定类下所有控制流。
	 * @param clazz 类
	 * @param methodNamePattern 方法名模式（可为 {@code null}）
	 */
	public ControlFlowPointcut(Class<?> clazz, @Nullable String methodNamePattern) {
		Assert.notNull(clazz, "Class must not be null");
		this.clazz = clazz;
		this.methodNamePatterns = (methodNamePattern != null ?
				Collections.singletonList(methodNamePattern) : Collections.emptyList());
	}

	/**
	 * 构造匹配给定类中符合任一方法名模式的方法下所有调用的新切入点。
	 * <p>若未给定方法名模式，则匹配给定类下所有控制流。
	 * @param clazz 类
	 * @param methodNamePatterns 方法名模式（可能为空）
	 * @since 6.1
	 */
	public ControlFlowPointcut(Class<?> clazz, String... methodNamePatterns) {
		this(clazz, Arrays.asList(methodNamePatterns));
	}

	/**
	 * 构造匹配给定类中符合任一方法名模式的方法下所有调用的新切入点。
	 * <p>若未给定方法名模式，则匹配给定类下所有控制流。
	 * @param clazz 类
	 * @param methodNamePatterns 方法名模式列表（可能为空）
	 * @since 6.1
	 */
	public ControlFlowPointcut(Class<?> clazz, List<String> methodNamePatterns) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodNamePatterns, "List of method name patterns must not be null");
		Assert.noNullElements(methodNamePatterns, "List of method name patterns must not contain null elements");
		this.clazz = clazz;
		this.methodNamePatterns = methodNamePatterns.stream().distinct().toList();
	}


	/**
	 * 子类可覆盖以实现更强过滤（及更好性能）。
	 * <p>默认实现始终返回 {@code true}。
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		return true;
	}

	/**
	 * 若可过滤部分候选类，子类可覆盖。
	 * <p>默认实现始终返回 {@code true}。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return true;
	}

	@Override
	public boolean isRuntime() {
		return true;
	}

	@Override
	public boolean matches(Method method, Class<?> targetClass, @Nullable Object... args) {
		incrementEvaluationCount();

		for (StackTraceElement element : new Throwable().getStackTrace()) {
			if (element.getClassName().equals(this.clazz.getName())) {
				if (this.methodNamePatterns.isEmpty()) {
					return true;
				}
				String methodName = element.getMethodName();
				for (int i = 0; i < this.methodNamePatterns.size(); i++) {
					if (isMatch(methodName, i)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 获取 {@link #matches(Method, Class, Object...)} 被评估的次数。
	 * <p>便于优化与测试。
	 */
	public int getEvaluations() {
		return this.evaluationCount.get();
	}

	/**
	 * 递增 {@link #getEvaluations() 评估计数}。
	 * @since 6.1
	 * @see #matches(Method, Class, Object...)
	 */
	protected final void incrementEvaluationCount() {
		this.evaluationCount.incrementAndGet();
	}

	/**
	 * 判断给定方法名是否匹配指定索引处的方法名模式。
	 * <p>由 {@link #matches(Method, Class, Object...)} 调用。
	 * <p>默认实现从 {@link #methodNamePatterns} 取模式，
	 * 并委托给 {@link #isMatch(String, String)}。
	 * <p>子类可覆盖，例如以支持正则表达式。
	 * @param methodName 待检查的方法名
	 * @param patternIndex 方法名模式索引
	 * @return 方法名匹配指定索引处模式则 {@code true}
	 * @since 6.1
	 * @see #methodNamePatterns
	 * @see #isMatch(String, String)
	 * @see #matches(Method, Class, Object...)
	 */
	protected boolean isMatch(String methodName, int patternIndex) {
		String methodNamePattern = this.methodNamePatterns.get(patternIndex);
		return isMatch(methodName, methodNamePattern);
	}

	/**
	 * 判断给定方法名是否匹配方法名模式。
	 * <p>由 {@link #isMatch(String, int)} 调用。
	 * <p>默认实现检查直接相等及
	 * {@code xxx*}、{@code *xxx}、{@code *xxx*}、{@code xxx*yyy} 匹配。
	 * <p>子类可覆盖，例如以支持不同风格的简单模式匹配。
	 * @param methodName 待检查的方法名
	 * @param methodNamePattern 方法名模式
	 * @return 方法名匹配模式则 {@code true}
	 * @since 6.1
	 * @see #isMatch(String, int)
	 * @see PatternMatchUtils#simpleMatch(String, String)
	 */
	protected boolean isMatch(String methodName, String methodNamePattern) {
		return (methodName.equals(methodNamePattern) ||
				PatternMatchUtils.simpleMatch(methodNamePattern, methodName));
	}


	@Override
	public ClassFilter getClassFilter() {
		return this;
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return this;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof ControlFlowPointcut that &&
				this.clazz.equals(that.clazz)) && this.methodNamePatterns.equals(that.methodNamePatterns));
	}

	@Override
	public int hashCode() {
		int code = this.clazz.hashCode();
		code = 37 * code + this.methodNamePatterns.hashCode();
		return code;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": class = " + this.clazz.getName() + "; methodNamePatterns = " + this.methodNamePatterns;
	}

}
