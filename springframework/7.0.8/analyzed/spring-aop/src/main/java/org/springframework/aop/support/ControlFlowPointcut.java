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
 * 切入点和方法匹配器用作简单的 <b>cflow</b> 样式切入点。
 * <p>E每个配置的方法名称模式可以是精确的方法名称或模式（有关支持的模式样式的详细信息，请参阅 {@link #isMatch(String, String)}）。
 * <p>请注意，评估此类切入点比评估普通切入点慢 10-15 倍，但它们在某些情况下很有用。
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
	 * 要匹配的类。
	 * @since 6.1
	 */
	protected final Class<?> clazz;

	/**
	 * 要匹配的不同方法名称模式的不可变列表。
	 * @since 6.1
	 */
	protected final List<String> methodNamePatterns;

	/**
	 * 方法 `AtomicInteger`：完成本类中与「Atomic Integer」相关的职责。
	 */
	private final AtomicInteger evaluationCount = new AtomicInteger();


	/**
	 * 构造一个与给定类下面的所有控制流匹配的新切入点。
	 * @param clazz 班级
	 */
	public ControlFlowPointcut(Class<?> clazz) {
		this(clazz, (String) null);
	}

	/**
	 * 构造一个新的切入点，该切入点与给定类中与给定方法名称模式匹配的方法下面的所有调用相匹配。 <p>如果没有给出方法名称模式，则切入点匹配给定类下面的所有控制流。
	 * @param clazz 班级
	 * @param methodNamePattern 方法名称模式（可能是 {@code null}）
	 */
	public ControlFlowPointcut(Class<?> clazz, @Nullable String methodNamePattern) {
		Assert.notNull(clazz, "Class must not be null");
		this.clazz = clazz;
		this.methodNamePatterns = (methodNamePattern != null ?
				Collections.singletonList(methodNamePattern) : Collections.emptyList());
	}

	/**
	 * 构造一个新的切入点，该切入点与与给定类中给定方法名称模式之一匹配的方法下面的所有调用相匹配。 <p>如果没有给出方法名称模式，则切入点匹配给定类下面的所有控制流。
	 * @param clazz 班级
	 * @param methodNamePatterns 方法名称模式（可能为空）
	 * @since 6.1
	 */
	public ControlFlowPointcut(Class<?> clazz, String... methodNamePatterns) {
		this(clazz, Arrays.asList(methodNamePatterns));
	}

	/**
	 * 构造一个新的切入点，该切入点与与给定类中给定方法名称模式之一匹配的方法下面的所有调用相匹配。 <p>如果没有给出方法名称模式，则切入点匹配给定类下面的所有控制流。
	 * @param clazz 班级
	 * @param methodNamePatterns 方法名称模式（可能为空）
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
	 * 子类可以覆盖它以获得更好的过滤（和性能）。 <p>默认实现始终返回{@code true}。
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		return true;
	}

	/**
	 * 如果可以过滤掉一些候选类，子类可以覆盖它。 <p>默认实现始终返回{@code true}。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return true;
	}

	/**
	 * 判断是否 Runtime。
	 */
	@Override
	public boolean isRuntime() {
		return true;
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
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
	 * 获取 {@link #matches(Method, Class, Object...)} 已评估的次数。 <p> 对于优化和测试目的很有用。
	 */
	public int getEvaluations() {
		return this.evaluationCount.get();
	}

	/**
	 * 增加 {@link #getEvaluations() evaluation count}。
	 * @since 6.1
	 * @see #matches(Method, Class, Object...)
	 */
	protected final void incrementEvaluationCount() {
		this.evaluationCount.incrementAndGet();
	}

	/**
	 * 确定给定的方法名称是否与指定索引处的方法名称模式匹配。 <p>该方法由{@link #matches(Method, Class, Object...)}调用。 <p> 默认实
	 * 现从 {@link #methodNamePatterns} 检索方法名称模式并委托给 {@link #isMatch(String, String)}。 <p>可以在子类中被
	 * 重写——例如，支持正则表达式。
	 * @param methodName 要检查的方法名称
	 * @param patternIndex 方法名称模式的索引
	 * @return true} 如果方法名称与指定索引处的模式匹配
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
	 * 确定给定的方法名称是否与方法名称模式匹配。 <p>该方法由{@link #isMatch(String, int)}调用。 <p>默认实现检查直接相等以及 {@code xxx
	 * *}、{@code *xxx}、{@code *xxx*} 和 {@code xxx*yyy} 匹配。 <p>可以在子类中被重写——例如，支持不同风格的简单模式匹配。
	 * @param methodName 要检查的方法名称
	 * @param methodNamePattern 方法名称模式
	 * @return true} 如果方法名称与模式匹配
	 * @since 6.1
	 * @see #isMatch(String, int)
	 * @see PatternMatchUtils#simpleMatch(String, String)
	 */
	protected boolean isMatch(String methodName, String methodNamePattern) {
		return (methodName.equals(methodNamePattern) ||
				PatternMatchUtils.simpleMatch(methodNamePattern, methodName));
	}


	/**
	 * 获取 Class Filter（`ClassFilter`）。
	 */
	@Override
	public ClassFilter getClassFilter() {
		return this;
	}

	/**
	 * 获取 Method Matcher（`MethodMatcher`）。
	 */
	@Override
	public MethodMatcher getMethodMatcher() {
		return this;
	}


	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof ControlFlowPointcut that &&
				this.clazz.equals(that.clazz)) && this.methodNamePatterns.equals(that.methodNamePatterns));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		int code = this.clazz.hashCode();
		code = 37 * code + this.methodNamePatterns.hashCode();
		return code;
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": class = " + this.clazz.getName() + "; methodNamePatterns = " + this.methodNamePatterns;
	}

}
