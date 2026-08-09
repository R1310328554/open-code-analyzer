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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

/**
 * 简单的 {@link org.springframework.aop.MethodMatcher
 * MethodMatcher}，查找方法上存在的特定注释（检查调用的接口上的方法（如果有）以及目标类上的相应方法）。
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 * @see AnnotationMatchingPointcut
 */
public class AnnotationMethodMatcher extends StaticMethodMatcher {

	/** 类型相关状态（`annotationType`）。 */
	private final Class<? extends Annotation> annotationType;

	/** `checkInherited`：该类的成员状态。 */
	private final boolean checkInherited;


	/**
	 * 为给定的注释类型创建一个新的 AnnotationClassFilter。
	 * @param annotationType 要查找的注释类型
	 */
	public AnnotationMethodMatcher(Class<? extends Annotation> annotationType) {
		this(annotationType, false);
	}

	/**
	 * 为给定的注释类型创建一个新的 AnnotationClassFilter。
	 * @param annotationType 要查找的注释类型
	 * @param checkInherited 是否还检查超类和接口以及注释类型的元注释（即是否使用 {@link AnnotatedElementUtils#hasAnnotation} 语义而不是标准 Java {@link Method#isAnnotationPresent}）
	 * @since 5.0
	 */
	public AnnotationMethodMatcher(Class<? extends Annotation> annotationType, boolean checkInherited) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		this.annotationType = annotationType;
		this.checkInherited = checkInherited;
	}



	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if (matchesMethod(method)) {
			return true;
		}
		// 代理类在其重新声明的方法上永远不会有注释。
		if (Proxy.isProxyClass(targetClass)) {
			return false;
		}
		// 该方法可能位于接口上，因此我们也检查目标类。
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
		return (specificMethod != method && matchesMethod(specificMethod));
	}

	/**
	 * 匹配：es Method（方法 `matchesMethod`）。
	 */
	private boolean matchesMethod(Method method) {
		return (this.checkInherited ? AnnotatedElementUtils.hasAnnotation(method, this.annotationType) :
				method.isAnnotationPresent(this.annotationType));
	}

	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AnnotationMethodMatcher otherMm &&
				this.annotationType.equals(otherMm.annotationType) &&
				this.checkInherited == otherMm.checkInherited));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return this.annotationType.hashCode();
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": " + this.annotationType;
	}

}
