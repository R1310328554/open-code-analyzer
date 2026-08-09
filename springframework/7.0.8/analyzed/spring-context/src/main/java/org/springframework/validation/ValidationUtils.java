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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 提供调用 {@link Validator} 与拒绝空字段便捷方法的工具类。
 *
 * <p>在 {@code Validator} 实现中，使用 {@link #rejectIfEmpty} 或
 * {@link #rejectIfEmptyOrWhitespace} 可将空字段检查简化为一行代码。
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @since 06.05.2003
 * @see Validator
 * @see Errors
 */
public abstract class ValidationUtils {

	private static final Log logger = LogFactory.getLog(ValidationUtils.class);


	/**
	 * 对给定对象与 {@link Errors} 实例调用给定 {@link Validator}。
	 * @param validator 要调用的 {@code Validator}
	 * @param target 要绑定参数的对象
	 * @param errors 应存储错误的 {@link Errors} 实例
	 * @throws IllegalArgumentException 若 {@code Validator} 或 {@code Errors} 参数为 {@code null}，
	 * 或给定 {@code Validator} 不 {@link Validator#supports(Class) 支持} 给定对象类型的校验
	 */
	public static void invokeValidator(Validator validator, Object target, Errors errors) {
		invokeValidator(validator, target, errors, (Object[]) null);
	}

	/**
	 * 对给定对象与 {@link Errors} 实例调用给定 {@link Validator}/{@link SmartValidator}。
	 * @param validator 要调用的 {@code Validator}
	 * @param target 要绑定参数的对象
	 * @param errors 应存储错误的 {@link Errors} 实例
	 * @param validationHints 传递给校验引擎的一个或多个提示对象
	 * @throws IllegalArgumentException 若 {@code Validator} 或 {@code Errors} 参数为 {@code null}，
	 * 或给定 {@code Validator} 不 {@link Validator#supports(Class) 支持} 给定对象类型的校验
	 */
	public static void invokeValidator(
			Validator validator, Object target, Errors errors, Object @Nullable ... validationHints) {

		Assert.notNull(validator, "Validator must not be null");
		Assert.notNull(target, "Target object must not be null");
		Assert.notNull(errors, "Errors object must not be null");

		if (logger.isDebugEnabled()) {
			logger.debug("Invoking validator [" + validator + "]");
		}
		if (!validator.supports(target.getClass())) {
			throw new IllegalArgumentException(
					"Validator [" + validator.getClass() + "] does not support [" + target.getClass() + "]");
		}

		if (!ObjectUtils.isEmpty(validationHints) && validator instanceof SmartValidator smartValidator) {
			smartValidator.validate(target, errors, validationHints);
		}
		else {
			validator.validate(target, errors);
		}

		if (logger.isDebugEnabled()) {
			if (errors.hasErrors()) {
				logger.debug("Validator found " + errors.getErrorCount() + " errors");
			}
			else {
				logger.debug("Validator found no errors");
			}
		}
	}


	/**
	 * 若值为空，以给定错误码拒绝给定字段。
	 * <p>此处的“空”指 {@code null} 或空字符串 ""。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode) {
		rejectIfEmpty(errors, field, errorCode, null, null);
	}

	/**
	 * 若值为空，以给定错误码与默认消息拒绝给定字段。
	 * <p>此处的“空”指 {@code null} 或空字符串 ""。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 * @param defaultMessage 后备默认消息
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode, String defaultMessage) {
		rejectIfEmpty(errors, field, errorCode, null, defaultMessage);
	}

	/**
	 * 若值为空，以给定错误码与错误参数拒绝给定字段。
	 * <p>此处的“空”指 {@code null} 或空字符串 ""。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode, Object[] errorArgs) {
		rejectIfEmpty(errors, field, errorCode, errorArgs, null);
	}

	/**
	 * 若值为空，以给定错误码、错误参数与默认消息拒绝给定字段。
	 * <p>此处的“空”指 {@code null} 或空字符串 ""。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）
	 * @param defaultMessage 后备默认消息
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode,
			Object @Nullable [] errorArgs, @Nullable String defaultMessage) {

		Assert.notNull(errors, "Errors object must not be null");
		Object value = errors.getFieldValue(field);
		if (value == null || !StringUtils.hasLength(value.toString())) {
			errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
		}
	}

	/**
	 * 若值为空或仅含空白，以给定错误码拒绝给定字段。
	 * <p>此处的“空”指 {@code null}、空字符串 "" 或全为空白字符。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 */
	public static void rejectIfEmptyOrWhitespace(Errors errors, String field, String errorCode) {
		rejectIfEmptyOrWhitespace(errors, field, errorCode, null, null);
	}

	/**
	 * 若值为空或仅含空白，以给定错误码与默认消息拒绝给定字段。
	 * <p>此处的“空”指 {@code null}、空字符串 "" 或全为空白字符。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 * @param defaultMessage 后备默认消息
	 */
	public static void rejectIfEmptyOrWhitespace(
			Errors errors, String field, String errorCode, String defaultMessage) {

		rejectIfEmptyOrWhitespace(errors, field, errorCode, null, defaultMessage);
	}

	/**
	 * 若值为空或仅含空白，以给定错误码与错误参数拒绝给定字段。
	 * <p>此处的“空”指 {@code null}、空字符串 "" 或全为空白字符。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）
	 */
	public static void rejectIfEmptyOrWhitespace(
			Errors errors, String field, String errorCode, Object @Nullable [] errorArgs) {

		rejectIfEmptyOrWhitespace(errors, field, errorCode, errorArgs, null);
	}

	/**
	 * 若值为空或仅含空白，以给定错误码、错误参数与默认消息拒绝给定字段。
	 * <p>此处的“空”指 {@code null}、空字符串 "" 或全为空白字符。
	 * <p>无需传入被校验字段所属对象，
	 * 因 {@link Errors} 实例可自行解析字段值
	 * （通常持有对目标对象的内部引用）。
	 * @param errors 要注册错误的 {@code Errors} 实例
	 * @param field 要检查的字段名
	 * @param errorCode 错误码，可解释为消息键
	 * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）
	 * @param defaultMessage 后备默认消息
	 */
	public static void rejectIfEmptyOrWhitespace(
			Errors errors, String field, String errorCode, Object @Nullable [] errorArgs, @Nullable String defaultMessage) {

		Assert.notNull(errors, "Errors object must not be null");
		Object value = errors.getFieldValue(field);
		if (value == null || !StringUtils.hasText(value.toString())) {
			errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
		}
	}

}
