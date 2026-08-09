/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.validation.beanvalidation;

import java.lang.annotation.Annotation;

import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;

/**
 * 从方法校验中排除类型的过滤器。
 *
 * @author Andy Wilkinson
 * @since 2.4.0
 * @see FilteredMethodValidationPostProcessor
 */
public interface MethodValidationExcludeFilter {

	/**
	 * 判断是否将给定 {@code type} 从方法校验中排除。
	 *
	 * @param type the type to evaluate 待评估的类型
	 * @return {@code true} to exclude the type from method validation, otherwise
	 * {@code false}. 排除则为 {@code true}，否则 {@code false}
	 */
	boolean isExcluded(Class<?> type);

	/**
	 * 工厂方法：创建按注解排除类的 {@link MethodValidationExcludeFilter}，
	 * 使用 {@link SearchStrategy#INHERITED_ANNOTATIONS inherited annotations search strategy} 查找注解。
	 *
	 * @param annotationType the annotation to check 要检查的注解类型
	 * @return a {@link MethodValidationExcludeFilter} instance 过滤器实例
	 */
	static MethodValidationExcludeFilter byAnnotation(Class<? extends Annotation> annotationType) {
		return byAnnotation(annotationType, SearchStrategy.INHERITED_ANNOTATIONS);
	}

	/**
	 * 工厂方法：创建按注解排除类的 {@link MethodValidationExcludeFilter}，
	 * 使用给定搜索策略查找注解。
	 *
	 * @param annotationType the annotation to check 要检查的注解类型
	 * @param searchStrategy the annotation search strategy 注解搜索策略
	 * @return a {@link MethodValidationExcludeFilter} instance 过滤器实例
	 */
	static MethodValidationExcludeFilter byAnnotation(Class<? extends Annotation> annotationType,
			SearchStrategy searchStrategy) {
		return (type) -> MergedAnnotations.from(type, searchStrategy).isPresent(annotationType);
	}

}
