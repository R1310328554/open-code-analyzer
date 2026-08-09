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
 * 简单的 {@link Pointcut}，用于查找 {@linkplain #forClassAnnotation class} 或 {@linkplain
 * #forMethodAnnotation method} 上存在的特定注释。
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 * @see AnnotationClassFilter
 * @see AnnotationMethodMatcher
 */
public class AnnotationMatchingPointcut implements Pointcut {

	/** 类相关状态（`classFilter`）。 */
	private final ClassFilter classFilter;

	/** 方法相关状态（`methodMatcher`）。 */
	private final MethodMatcher methodMatcher;


	/**
	 * 为给定的注释类型创建一个新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 在类级别查找的注释类型
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType) {
		this(classAnnotationType, false);
	}

	/**
	 * 为给定的注释类型创建一个新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 在类级别查找的注释类型
	 * @param checkInherited 是否还检查超类和接口以及注释类型的元注释
	 * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType, boolean checkInherited) {
		this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 为给定的注释类型创建一个新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 在类级别查找的注释类型（可以是 {@code null}）
	 * @param methodAnnotationType 在方法级别查找的注释类型（可以是 {@code null}）
	 */
	public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType,
			@Nullable Class<? extends Annotation> methodAnnotationType) {

		this(classAnnotationType, methodAnnotationType, false);
	}

	/**
	 * 为给定的注释类型创建一个新的 AnnotationMatchingPointcut。
	 * @param classAnnotationType 在类级别查找的注释类型（可以是 {@code null}）
	 * @param methodAnnotationType 在方法级别查找的注释类型（可以是 {@code null}）
	 * @param checkInherited 是否还检查超类和接口以及注释类型的元注释
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
		return (this == other || (other instanceof AnnotationMatchingPointcut otherPointcut &&
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
		return "AnnotationMatchingPointcut: " + this.classFilter + ", " + this.methodMatcher;
	}

	/**
	 * AnnotationMatchingPointcut 的工厂方法，该方法与类级别的指定注释相匹配。
	 * @param annotationType 在类级别查找的注释类型
	 * @return 对应的AnnotationMatchingPointcut
	 */
	public static AnnotationMatchingPointcut forClassAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return new AnnotationMatchingPointcut(annotationType);
	}

	/**
	 * AnnotationMatchingPointcut 的工厂方法，该方法与方法级别的指定注释相匹配。
	 * @param annotationType 在方法级别查找的注释类型
	 * @return 对应的AnnotationMatchingPointcut
	 */
	public static AnnotationMatchingPointcut forMethodAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return new AnnotationMatchingPointcut(null, annotationType);
	}


	/**
	 * {@link ClassFilter} 委托 {@link AnnotationUtils#isCandidateClass} 来过滤其方法不值得一开始搜索的类。
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
