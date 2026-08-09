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

package org.springframework.validation;

import org.jspecify.annotations.Nullable;

/**
 * {@link Validator} 接口的扩展变体，增加对校验“提示（hints）”的支持。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 */
public interface SmartValidator extends Validator {

	/**
	 * 校验提供的 {@code target} 对象，其类型须为 {@link #supports(Class)} 通常返回 {@code true} 的类型。
	 * <p>提供的 {@link Errors errors} 实例可用于报告产生的校验错误。
	 * <p><b>本 {@code validate()} 变体支持校验提示，
	 * 例如针对 JSR-303 提供者的校验组</b>（此时提供的 hint 对象须为 {@code Class} 类型的注解参数）。
	 * <p>注意：实际目标 {@code Validator} 可能忽略校验提示，
	 * 此时本方法行为应与常规 {@link #validate(Object, Errors)} 相同。
	 * @param target 要校验的对象
	 * @param errors 校验过程的上下文状态
	 * @param validationHints 要传递给校验引擎的一个或多个 hint 对象
	 * @see jakarta.validation.Validator#validate(Object, Class[])
	 */
	void validate(Object target, Errors errors, Object... validationHints);

	/**
	 * 校验目标类型上指定字段的给定值，
	 * 报告与将该值绑定到目标类实例字段时相同的校验错误。
	 * @param targetType 目标类型
	 * @param fieldName 字段名
	 * @param value 候选值
	 * @param errors 校验过程的上下文状态
	 * @param validationHints 要传递给校验引擎的一个或多个 hint 对象
	 * @since 5.1
	 * @see jakarta.validation.Validator#validateValue(Class, String, Object, Class[])
	 */
	default void validateValue(
			Class<?> targetType, @Nullable String fieldName, @Nullable Object value, Errors errors, Object... validationHints) {

		throw new IllegalArgumentException("Cannot validate individual value for " + targetType);
	}

	/**
	 * 返回指定类型的内部校验器实例，必要时逐层解包。
	 * @param type 要返回的对象的 Class
	 * @param <T> 要返回的对象类型
	 * @return 指定类型的校验器实例；无嵌套校验器时返回 {@code null}；
	 * 指定校验器类型不匹配时可能抛出异常。
	 * @since 6.1
	 */
	default <T> @Nullable T unwrap(@Nullable Class<T> type) {
		return null;
	}

}
