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
import java.util.Collections;
import java.util.List;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.Errors;

/**
 * 方法校验结果容器，底层库的校验错误适配为 {@link MessageSourceResolvable}，
 * 并按方法参数分组为 {@link ParameterValidationResult}。
 * 对于存在嵌套校验错误的方法参数，校验结果类型为 {@link ParameterErrors} 并实现 {@link Errors}。
 *
 * @author Rossen Stoyanchev
 * @since 6.1
 */
public interface MethodValidationResult {

	/**
	 * 返回应用校验的方法调用的目标对象。
	 */
	Object getTarget();

	/**
	 * 返回应用校验的方法。
	 */
	Method getMethod();

	/**
	 * 违例是否针对返回值。
	 * 若为 true，违例来自返回值校验；若为 false，违例来自方法参数校验。
	 */
	boolean isForReturnValue();

	/**
	 * 结果是否包含任何校验错误。
	 */
	default boolean hasErrors() {
		return !getParameterValidationResults().isEmpty();
	}

	/**
	 * 返回包含所有校验结果中全部错误的单一列表。
	 * @see #getParameterValidationResults()
	 * @see ParameterValidationResult#getResolvableErrors()
	 */
	default List<? extends MessageSourceResolvable> getAllErrors() {
		return getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream())
				.toList();
	}

	/**
	 * 返回每个方法参数的所有校验结果，包括 {@link #getValueResults()} 和 {@link #getBeanResults()}。
	 * <p>使用 {@link #getCrossParameterValidationResults()} 访问跨参数校验的错误。
	 * @since 6.2
	 * @see #getValueResults()
	 * @see #getBeanResults()
	 */
	List<ParameterValidationResult> getParameterValidationResults();

	/**
	 * 返回 {@link #getParameterValidationResults() allValidationResults} 的子集，
	 * 包含方法参数值上直接存在校验错误的方法参数。
	 * 排除字段和属性上存在嵌套错误的 {@link #getBeanResults() beanResults}。
	 */
	default List<ParameterValidationResult> getValueResults() {
		return getParameterValidationResults().stream()
				.filter(result -> !(result instanceof ParameterErrors))
				.toList();
	}

	/**
	 * 返回 {@link #getParameterValidationResults() allValidationResults} 的子集，
	 * 包含字段和属性上存在嵌套错误的 Object 方法参数。
	 * 排除方法参数上直接存在校验错误的 {@link #getValueResults() valueResults}。
	 */
	default List<ParameterErrors> getBeanResults() {
		return getParameterValidationResults().stream()
				.filter(ParameterErrors.class::isInstance)
				.map(result -> (ParameterErrors) result)
				.toList();
	}

	/**
	 * 返回跨参数校验的错误。
	 * @since 6.2
	 */
	List<MessageSourceResolvable> getCrossParameterValidationResults();


	/**
	 * 创建 {@link MethodValidationResult} 实例的工厂方法。
	 * @param target 目标对象
	 * @param method 目标方法
	 * @param results 方法校验结果，预期非空
	 * @return 创建的实例
	 */
	static MethodValidationResult create(Object target, Method method, List<ParameterValidationResult> results) {
		return create(target, method, results, Collections.emptyList());
	}

	/**
	 * Factory method to create a {@link MethodValidationResult} instance.
	 * @param target the target Object
	 * @param method the target method
	 * @param results method validation results, expected to be non-empty
	 * @param crossParameterErrors 跨参数校验错误
	 * @return the created instance
	 * @since 6.2
	 */
	static MethodValidationResult create(
			Object target, Method method, List<ParameterValidationResult> results,
			List<MessageSourceResolvable> crossParameterErrors) {

		return new DefaultMethodValidationResult(target, method, results, crossParameterErrors);
	}

	/**
	 * 创建含 0 个错误的 {@link MethodValidationResult} 实例的工厂方法，
	 * 适合用作常量。不支持目标对象或方法的 getter。
	 */
	static MethodValidationResult emptyResult() {
		return new EmptyMethodValidationResult();
	}

}
