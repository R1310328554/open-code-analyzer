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

package org.springframework.validation.method;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;

/**
 * 应用方法校验并处理结果的契约。
 * 暴露返回 {@link MethodValidationResult} 的方法，
 * 以及处理结果的方法，默认抛出 {@link MethodValidationException}。
 *
 * @author Rossen Stoyanchev
 * @since 6.1
 */
public interface MethodValidator {

	/**
	 * 确定适用的校验分组。默认从方法或类级的
	 * {@link org.springframework.validation.annotation.Validated @Validated}
	 * 注解获取。
	 * @param target 目标对象
	 * @param method 目标方法
	 * @return 适用的校验分组，以 {@code Class} 数组形式
	 * @deprecated 推荐使用
	 * {@link org.springframework.validation.annotation.ValidationAnnotationUtils#determineValidationGroups(Object, Method)}
	 */
	@Deprecated(since = "7.0.4", forRemoval = true)
	Class<?>[] determineValidationGroups(Object target, Method method);

	/**
	 * 校验给定方法参数并返回校验结果。
	 * @param target 目标对象
	 * @param method 目标方法
	 * @param parameters 参数（若已创建且可用）
	 * @param arguments 要校验的候选参数值
	 * @param groups validation groups from
	 * {@link org.springframework.validation.annotation.ValidationAnnotationUtils#determineValidationGroups(Object, Method)}
	 * @return 校验结果
	 */
	MethodValidationResult validateArguments(
			Object target, Method method, MethodParameter @Nullable [] parameters,
			@Nullable Object[] arguments, Class<?>[] groups);

	/**
	 * 委托 {@link #validateArguments} 并处理校验结果，
	 * 默认在出错时抛出 {@link MethodValidationException}。
	 * 实现可提供替代处理方式，例如将 {@link org.springframework.validation.Errors} 注入方法。
	 * @throws MethodValidationException 存在未处理错误时
	 */
	default void applyArgumentValidation(
			Object target, Method method, MethodParameter @Nullable [] parameters,
			@Nullable Object[] arguments, Class<?>[] groups) {

		MethodValidationResult result = validateArguments(target, method, parameters, arguments, groups);
		if (result.hasErrors()) {
			throw new MethodValidationException(result);
		}
	}

	/**
	 * 校验给定返回值并返回校验结果。
	 * @param target 目标对象
	 * @param method 目标方法
	 * @param returnType 返回参数（若已创建且可用）
	 * @param returnValue 要校验的返回值
	 * @param groups validation groups from
	 * {@link org.springframework.validation.annotation.ValidationAnnotationUtils#determineValidationGroups(Object, Method)}
	 * @return the result of validation
	 */
	MethodValidationResult validateReturnValue(
			Object target, Method method, @Nullable MethodParameter returnType,
			@Nullable Object returnValue, Class<?>[] groups);

	/**
	 * 委托 {@link #validateReturnValue} 并处理校验结果，
	 * 默认在出错时抛出 {@link MethodValidationException}。
	 * 实现可提供替代处理方式。
	 * @throws MethodValidationException 存在未处理错误时
	 */
	default void applyReturnValueValidation(
			Object target, Method method, @Nullable MethodParameter returnType,
			@Nullable Object returnValue, Class<?>[] groups) {

		MethodValidationResult result = validateReturnValue(target, method, returnType, returnValue, groups);
		if (result.hasErrors()) {
			throw new MethodValidationException(result);
		}
	}

}
