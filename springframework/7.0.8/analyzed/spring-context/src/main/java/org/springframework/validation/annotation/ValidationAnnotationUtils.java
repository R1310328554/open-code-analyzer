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

package org.springframework.validation.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 处理校验注解的工具类。
 *
 * <p>主要供框架内部使用。
 *
 * @author Christoph Dreis
 * @author Juergen Hoeller
 * @since 5.3.7
 */
public abstract class ValidationAnnotationUtils {

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];


	/**
	 * 确定给定注解的校验提示。
	 * <p>本实现检查 Spring 的
	 * {@link org.springframework.validation.annotation.Validated}、
	 * {@code @jakarta.validation.Valid}，以及名称以 "Valid" 开头的自定义注解，
	 * 后者可通过 "value" 属性可选地声明校验提示。
	 * @param ann 注解（可能是校验注解）
	 * @return 要应用的校验提示（可能为空数组），
	 * 若本注解不触发任何校验则返回 {@code null}
	 */
	public static Object @Nullable [] determineValidationHints(Annotation ann) {
		// Direct presence of @Validated ?
		if (ann instanceof Validated validated) {
			return validated.value();
		}
		// Direct presence of @Valid ?
		Class<? extends Annotation> annotationType = ann.annotationType();
		if ("jakarta.validation.Valid".equals(annotationType.getName())) {
			return EMPTY_OBJECT_ARRAY;
		}
		// Meta presence of @Validated ?
		Validated validatedAnn = AnnotationUtils.findAnnotation(annotationType, Validated.class);
		if (validatedAnn != null) {
			return validatedAnn.value();
		}
		// Custom validation annotation ?
		if (annotationType.getSimpleName().startsWith("Valid")) {
			return convertValidationHints(AnnotationUtils.getValue(ann));
		}
		// No validation triggered
		return null;
	}

	private static Object[] convertValidationHints(@Nullable Object hints) {
		if (hints == null) {
			return EMPTY_OBJECT_ARRAY;
		}
		return (hints instanceof Object[] objectHints ? objectHints : new Object[] {hints});
	}

	/**
	 * 从方法上的
	 * {@link org.springframework.validation.annotation.Validated @Validated}
	 * 注解、方法所属目标类，或对于无目标对象的 AOP 代理（行为全在 advisor 中）
	 * 还检查被代理接口，确定适用的校验分组。
	 * @since 7.0.4
	 */
	public static Class<?>[] determineValidationGroups(Object target, Method method) {
		Validated validatedAnn = AnnotationUtils.findAnnotation(method, Validated.class);
		if (validatedAnn == null) {
			if (AopUtils.isAopProxy(target)) {
				for (Class<?> type : AopProxyUtils.proxiedUserInterfaces(target)) {
					validatedAnn = AnnotationUtils.findAnnotation(type, Validated.class);
					if (validatedAnn != null) {
						break;
					}
				}
			}
			else {
				validatedAnn = AnnotationUtils.findAnnotation(target.getClass(), Validated.class);
			}
		}
		return (validatedAnn != null ? validatedAnn.value() : EMPTY_CLASS_ARRAY);
	}

}
