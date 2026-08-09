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

import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.util.Assert;

/**
 * 构建切入点的便捷类。
 *
 * <p>所有方法返回 {@code ComposablePointcut}，可使用如下简洁写法：
 *
 * <pre class="code">Pointcut pc = new ComposablePointcut()
 *                      .union(classFilter)
 *                      .intersection(methodMatcher)
 *                      .intersection(pointcut);</pre>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 11.11.2003
 * @see Pointcuts
 */
public class ComposablePointcut implements Pointcut, Serializable {

	/** 使用 Spring 1.2 的 serialVersionUID 以保持互操作性。 */
	private static final long serialVersionUID = -2743223737633663832L;

	@SuppressWarnings("serial")
	private ClassFilter classFilter;

	@SuppressWarnings("serial")
	private MethodMatcher methodMatcher;


	/**
	 * 创建默认 ComposablePointcut，
	 * 使用 {@code ClassFilter.TRUE} 与 {@code MethodMatcher.TRUE}。
	 */
	public ComposablePointcut() {
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 基于给定 Pointcut 创建 ComposablePointcut。
	 * @param pointcut 原始 Pointcut
	 */
	public ComposablePointcut(Pointcut pointcut) {
		Assert.notNull(pointcut, "Pointcut must not be null");
		this.classFilter = pointcut.getClassFilter();
		this.methodMatcher = pointcut.getMethodMatcher();
	}

	/**
	 * 为给定 ClassFilter 创建 ComposablePointcut，
	 * 使用 {@code MethodMatcher.TRUE}。
	 * @param classFilter 要使用的 ClassFilter
	 */
	public ComposablePointcut(ClassFilter classFilter) {
		Assert.notNull(classFilter, "ClassFilter must not be null");
		this.classFilter = classFilter;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 为给定 MethodMatcher 创建 ComposablePointcut，
	 * 使用 {@code ClassFilter.TRUE}。
	 * @param methodMatcher 要使用的 MethodMatcher
	 */
	public ComposablePointcut(MethodMatcher methodMatcher) {
		Assert.notNull(methodMatcher, "MethodMatcher must not be null");
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = methodMatcher;
	}

	/**
	 * 为给定 ClassFilter 与 MethodMatcher 创建 ComposablePointcut。
	 * @param classFilter 要使用的 ClassFilter
	 * @param methodMatcher 要使用的 MethodMatcher
	 */
	public ComposablePointcut(ClassFilter classFilter, MethodMatcher methodMatcher) {
		Assert.notNull(classFilter, "ClassFilter must not be null");
		Assert.notNull(methodMatcher, "MethodMatcher must not be null");
		this.classFilter = classFilter;
		this.methodMatcher = methodMatcher;
	}


	/**
	 * 与给定 ClassFilter 求并。
	 * @param other 要求并的 ClassFilter
	 * @return 本可组合切入点（用于链式调用）
	 */
	public ComposablePointcut union(ClassFilter other) {
		this.classFilter = ClassFilters.union(this.classFilter, other);
		return this;
	}

	/**
	 * 与给定 ClassFilter 求交。
	 * @param other 要求交的 ClassFilter
	 * @return 本可组合切入点（用于链式调用）
	 */
	public ComposablePointcut intersection(ClassFilter other) {
		this.classFilter = ClassFilters.intersection(this.classFilter, other);
		return this;
	}

	/**
	 * 与给定 MethodMatcher 求并。
	 * @param other 要求并的 MethodMatcher
	 * @return 本可组合切入点（用于链式调用）
	 */
	public ComposablePointcut union(MethodMatcher other) {
		this.methodMatcher = MethodMatchers.union(this.methodMatcher, other);
		return this;
	}

	/**
	 * 与给定 MethodMatcher 求交。
	 * @param other 要求交的 MethodMatcher
	 * @return 本可组合切入点（用于链式调用）
	 */
	public ComposablePointcut intersection(MethodMatcher other) {
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other);
		return this;
	}

	/**
	 * 与给定 Pointcut 求并。
	 * <p>注意：Pointcut 并集下，方法仅当其原始 ClassFilter
	 * （来自源 Pointcut）也匹配时才匹配。
	 * 不同 Pointcut 的 MethodMatcher 与 ClassFilter 不会相互交错。
	 * @param other 要求并的 Pointcut
	 * @return 本可组合切入点（用于链式调用）
	 */
	public ComposablePointcut union(Pointcut other) {
		this.methodMatcher = MethodMatchers.union(
				this.methodMatcher, this.classFilter, other.getMethodMatcher(), other.getClassFilter());
		this.classFilter = ClassFilters.union(this.classFilter, other.getClassFilter());
		return this;
	}

	/**
	 * 与给定 Pointcut 求交。
	 * @param other 要求交的 Pointcut
	 * @return 本可组合切入点（用于链式调用）
	 */
	public ComposablePointcut intersection(Pointcut other) {
		this.classFilter = ClassFilters.intersection(this.classFilter, other.getClassFilter());
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other.getMethodMatcher());
		return this;
	}


	@Override
	public ClassFilter getClassFilter() {
		return this.classFilter;
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return this.methodMatcher;
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof ComposablePointcut otherPointcut &&
				this.classFilter.equals(otherPointcut.classFilter) &&
				this.methodMatcher.equals(otherPointcut.methodMatcher)));
	}

	@Override
	public int hashCode() {
		return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.classFilter + ", " + this.methodMatcher;
	}

}
