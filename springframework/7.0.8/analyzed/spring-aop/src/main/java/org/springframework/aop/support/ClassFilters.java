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
import java.util.Arrays;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于编写 {@link ClassFilter ClassFilters} 的静态实用方法。
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 11.11.2003
 * @see MethodMatchers
 * @see Pointcuts
 */
public abstract class ClassFilters {

	/**
	 * 匹配给定 ClassFilters 的 <i> 或 </i>（或两者）匹配的所有类。
	 * @param cf1 第一个类过滤器
	 * @param cf2 第二个类过滤器
	 * @return 与给定 ClassFilter 匹配的所有类相匹配的不同 ClassFilter
	 */
	public static ClassFilter union(ClassFilter cf1, ClassFilter cf2) {
		Assert.notNull(cf1, "First ClassFilter must not be null");
		Assert.notNull(cf2, "Second ClassFilter must not be null");
		return new UnionClassFilter(new ClassFilter[] {cf1, cf2});
	}

	/**
	 * 匹配给定 ClassFilters 的 <i> 或 </i>（或全部）匹配的所有类。
	 * @param classFilters 要匹配的 ClassFilters
	 * @return 与给定 ClassFilter 匹配的所有类相匹配的不同 ClassFilter
	 */
	public static ClassFilter union(ClassFilter[] classFilters) {
		Assert.notEmpty(classFilters, "ClassFilter array must not be empty");
		return new UnionClassFilter(classFilters);
	}

	/**
	 * 匹配给定 ClassFilters 的 <i>both</i> 匹配的所有类。
	 * @param cf1 第一个类过滤器
	 * @param cf2 第二个类过滤器
	 * @return 与给定 ClassFilter 匹配的所有类相匹配的不同 ClassFilter
	 */
	public static ClassFilter intersection(ClassFilter cf1, ClassFilter cf2) {
		Assert.notNull(cf1, "First ClassFilter must not be null");
		Assert.notNull(cf2, "Second ClassFilter must not be null");
		return new IntersectionClassFilter(new ClassFilter[] {cf1, cf2});
	}

	/**
	 * 匹配给定 ClassFilters 的 <i>all</i> 匹配的所有类。
	 * @param classFilters 要匹配的 ClassFilters
	 * @return 与给定 ClassFilter 匹配的所有类相匹配的不同 ClassFilter
	 */
	public static ClassFilter intersection(ClassFilter[] classFilters) {
		Assert.notEmpty(classFilters, "ClassFilter array must not be empty");
		return new IntersectionClassFilter(classFilters);
	}

	/**
	 * 返回表示指定过滤器实例的逻辑非的类过滤器。
	 * @param classFilter 要否定的 {@link ClassFilter}
	 * @return 表示指定过滤器的逻辑非的过滤器
	 * @since 6.1
	 */
	public static ClassFilter negate(ClassFilter classFilter) {
		Assert.notNull(classFilter, "ClassFilter must not be null");
		return new NegateClassFilter(classFilter);
	}


	/**
	 * 给定 ClassFilter 联合的 ClassFilter 实现。
	 */
	@SuppressWarnings("serial")
	private static class UnionClassFilter implements ClassFilter, Serializable {

		private final ClassFilter[] filters;

		UnionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			for (ClassFilter filter : this.filters) {
				if (filter.matches(clazz)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof UnionClassFilter that &&
					ObjectUtils.nullSafeEquals(this.filters, that.filters)));
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(this.filters);
		}

		@Override
		public String toString() {
			return getClass().getName() + ": " + Arrays.toString(this.filters);
		}
	}


	/**
	 * 给定 ClassFilter 交集的 ClassFilter 实现。
	 */
	@SuppressWarnings("serial")
	private static class IntersectionClassFilter implements ClassFilter, Serializable {

		private final ClassFilter[] filters;

		IntersectionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			for (ClassFilter filter : this.filters) {
				if (!filter.matches(clazz)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof IntersectionClassFilter that &&
					ObjectUtils.nullSafeEquals(this.filters, that.filters)));
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(this.filters);
		}

		@Override
		public String toString() {
			return getClass().getName() + ": " + Arrays.toString(this.filters);
		}
	}


	/**
	 * ClassFilter 实现给定 ClassFilter 的逻辑非。
	 */
	@SuppressWarnings("serial")
	private static class NegateClassFilter implements ClassFilter, Serializable {

		private final ClassFilter original;

		NegateClassFilter(ClassFilter original) {
			this.original = original;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			return !this.original.matches(clazz);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof NegateClassFilter that &&
					this.original.equals(that.original)));
		}

		@Override
		public int hashCode() {
			return Objects.hash(getClass(), this.original);
		}

		@Override
		public String toString() {
			return "Negate " + this.original;
		}
	}

}
