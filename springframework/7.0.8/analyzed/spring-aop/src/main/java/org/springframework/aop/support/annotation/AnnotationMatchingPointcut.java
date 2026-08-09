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

package org.springframework.aop.support.annotation;

import java.lang.annotation.Annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * 简单的 {@link Pointcut}，查找
 * {@linkplain #forClassAnnotation 类}或{@linkplain #forMethodAnnotation 方法}上
 * 是否存在特定注解。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 * @see AnnotationClassFilter
 * @see AnnotationMethodMatcher
 */
public class AnnotationMatchingPointcut implements Pointcut {

	private final ClassFilter classFilter;

	private final MethodMatcher methodMatcher;


	/**
	 * 为给定注解类型创建新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 类级别要查找的注解类型
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType) {
		this(classAnnotationType, false);
	}

	/**
	 * 为给定注解类型创建新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 类级别要查找的注解类型
	 * @param checkInherited 是否还检查超类、接口及元注解
	 * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType, boolean checkInherited) {
		this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 为给定注解类型创建新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 类级别要查找的注解类型
	 * （可为 {@code null}）
	 * @param methodAnnotationType 方法级别要查找的注解类型
	 * （可为 {@code null}）
	 */
	public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType,
			@Nullable Class<? extends Annotation> methodAnnotationType) {

		this(classAnnotationType, methodAnnotationType, false);
	}

	/**
	 * 为给定注解类型创建新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 类级别要查找的注解类型
	 * （可为 {@code null}）
	 * @param methodAnnotationType 方法级别要查找的注解类型
	 * （可为 {@code null}）
	 * @param checkInherited 是否还检查超类、接口及元注解
	 * @since 5.0
	 * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)
	 * @see AnnotationMethodMatcher#AnnotationMethodMatcher(Class, boolean)
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType,
			@Nullable Class<? extends Annotation> methodAnnotationType, boolean checkInherited) {

		Assert.isTrue((classAnnotationType != null || methodAnnotationType != null),
				"Either Class annotation type or Method annotation type needs to be specified (or both)");

		if (classAnnotationType != null) {
			this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		}
		else {
			this.classFilter = new AnnotationCandidateClassFilter(methodAnnotationType);
		}

		if (methodAnnotationType != null) {
			this.methodMatcher = new AnnotationMethodMatcher(methodAnnotationType, checkInherited);
		}
		else {
			this.methodMatcher = MethodMatcher.TRUE;
		}
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
		return (this == other || (other instanceof AnnotationMatchingPointcut otherPointcut &&
				this.classFilter.equals(otherPointcut.classFilter) &&
				this.methodMatcher.equals(otherPointcut.methodMatcher)));
	}

	@Override
	public int hashCode() {
		return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
	}

	@Override
	public String toString() {
		return "AnnotationMatchingPointcut: " + this.classFilter + ", " + this.methodMatcher;
	}

	/**
	 * 创建在类级别匹配指定注解的 AnnotationMatchingPointcut 的工厂方法。
	 * @param annotationType 类级别要查找的注解类型
	 * @return 对应的 AnnotationMatchingPointcut
	 */
	public static AnnotationMatchingPointcut forClassAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return new AnnotationMatchingPointcut(annotationType);
	}

	/**
	 * 创建在方法级别匹配指定注解的 AnnotationMatchingPointcut 的工厂方法。
	 * @param annotationType 方法级别要查找的注解类型
	 * @return 对应的 AnnotationMatchingPointcut
	 */
	public static AnnotationMatchingPointcut forMethodAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return new AnnotationMatchingPointcut(null, annotationType);
	}


	/**
	 * 委托给 {@link AnnotationUtils#isCandidateClass} 的 {@link ClassFilter}，
	 * 用于过滤其方法不值得搜索的类。
	 * @since 5.2
	 */
	private static class AnnotationCandidateClassFilter implements ClassFilter {

		private final Class<? extends Annotation> annotationType;

		AnnotationCandidateClassFilter(Class<? extends Annotation> annotationType) {
			this.annotationType = annotationType;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			return AnnotationUtils.isCandidateClass(clazz, this.annotationType);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof AnnotationCandidateClassFilter that &&
					this.annotationType.equals(that.annotationType)));
		}

		@Override
		public int hashCode() {
			return this.annotationType.hashCode();
		}

		@Override
		public String toString() {
			return getClass().getName() + ": " + this.annotationType;
		}

	}

}
