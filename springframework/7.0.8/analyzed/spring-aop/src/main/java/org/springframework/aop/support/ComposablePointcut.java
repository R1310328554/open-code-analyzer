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
 * 用于建立切入点的便捷类。
 * <p>All 方法返回 {@code ComposablePointcut}，因此我们可以使用简洁的习惯用法，如下例所示。
 * <pre class="code">Pointcut pc = new ComposablePointcut() .union(classFilter)
 * .intersection(methodMatcher) .intersection(pointcut);</pre>
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 11.11.2003
 * @see Pointcuts
 */
public class ComposablePointcut implements Pointcut, Serializable {

	/**
	 */
	private static final long serialVersionUID = -2743223737633663832L;

	/** 类相关状态（`classFilter`）。 */
	@SuppressWarnings("serial")
	private ClassFilter classFilter;

	/** 方法相关状态（`methodMatcher`）。 */
	@SuppressWarnings("serial")
	private MethodMatcher methodMatcher;


	/**
	 * 使用 {@code ClassFilter.TRUE} 和 {@code MethodMatcher.TRUE} 创建默认的 ComposablePointcut。
	 */
	public ComposablePointcut() {
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 根据给定的切入点创建一个 ComposablePointcut。
	 * @param pointcut 原始切入点
	 */
	public ComposablePointcut(Pointcut pointcut) {
		Assert.notNull(pointcut, "Pointcut must not be null");
		this.classFilter = pointcut.getClassFilter();
		this.methodMatcher = pointcut.getMethodMatcher();
	}

	/**
	 * 使用 {@code MethodMatcher.TRUE} 为给定的 ClassFilter 创建 ComposablePointcut。
	 * @param classFilter 要使用的 ClassFilter
	 */
	public ComposablePointcut(ClassFilter classFilter) {
		Assert.notNull(classFilter, "ClassFilter must not be null");
		this.classFilter = classFilter;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 使用 {@code ClassFilter.TRUE} 为给定的 MethodMatcher 创建 ComposablePointcut。
	 * @param methodMatcher 要使用的 MethodMatcher
	 */
	public ComposablePointcut(MethodMatcher methodMatcher) {
		Assert.notNull(methodMatcher, "MethodMatcher must not be null");
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = methodMatcher;
	}

	/**
	 * 为给定的 ClassFilter 和 MethodMatcher 创建一个 ComposablePointcut。
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
	 * 应用与给定 ClassFilter 的并集。
	 * @param other 应用联合的 ClassFilter
	 * @return 可组合切入点（用于调用链）
	 */
	public ComposablePointcut union(ClassFilter other) {
		this.classFilter = ClassFilters.union(this.classFilter, other);
		return this;
	}

	/**
	 * 应用与给定 ClassFilter 的交集。
	 * @param other 要应用交集的 ClassFilter
	 * @return 可组合切入点（用于调用链）
	 */
	public ComposablePointcut intersection(ClassFilter other) {
		this.classFilter = ClassFilters.intersection(this.classFilter, other);
		return this;
	}

	/**
	 * 应用与给定 MethodMatcher 的并集。
	 * @param other 应用联合的 MethodMatcher
	 * @return 可组合切入点（用于调用链）
	 */
	public ComposablePointcut union(MethodMatcher other) {
		this.methodMatcher = MethodMatchers.union(this.methodMatcher, other);
		return this;
	}

	/**
	 * 应用与给定 MethodMatcher 的交集。
	 * @param other 应用交集的 MethodMatcher
	 * @return 可组合切入点（用于调用链）
	 */
	public ComposablePointcut intersection(MethodMatcher other) {
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other);
		return this;
	}

	/**
	 * 应用给定切入点的并集。 <p>注意，对于切入点联合，方法仅在其原始 ClassFilter（来自原始切点）也匹配时才匹配。来自不同切入点的方法匹配器和类过滤器永远不会相互交错。
	 * @param other 应用联合的切入点
	 * @return 可组合切入点（用于调用链）
	 */
	public ComposablePointcut union(Pointcut other) {
		this.methodMatcher = MethodMatchers.union(
				this.methodMatcher, this.classFilter, other.getMethodMatcher(), other.getClassFilter());
		this.classFilter = ClassFilters.union(this.classFilter, other.getClassFilter());
		return this;
	}

	/**
	 * 应用与给定切点的交集。
	 * @param other 应用相交的切点
	 * @return 可组合切入点（用于调用链）
	 */
	public ComposablePointcut intersection(Pointcut other) {
		this.classFilter = ClassFilters.intersection(this.classFilter, other.getClassFilter());
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other.getMethodMatcher());
		return this;
	}


	/**
	 * 获取 Class Filter（`ClassFilter`）。
	 */
	@Override
	public ClassFilter getClassFilter() {
		return this.classFilter;
	}

	/**
	 * 获取 Method Matcher（`MethodMatcher`）。
	 */
	@Override
	public MethodMatcher getMethodMatcher() {
		return this.methodMatcher;
	}

	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof ComposablePointcut otherPointcut &&
				this.classFilter.equals(otherPointcut.classFilter) &&
				this.methodMatcher.equals(otherPointcut.methodMatcher)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": " + this.classFilter + ", " + this.methodMatcher;
	}

}
