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

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.PropertyAccessException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 默认 {@link BindingErrorProcessor} 实现。
 *
 * <p>对缺失字段错误，使用 "required" 错误码与字段名解析消息码。
 *
 * <p>对每个 {@code PropertyAccessException} 创建 {@code FieldError}，
 * 使用异常的 errorCode（"typeMismatch"、"methodInvocation"）解析消息码。
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @since 1.2
 * @see #MISSING_FIELD_ERROR_CODE
 * @see DataBinder#setBindingErrorProcessor
 * @see BeanPropertyBindingResult#addError
 * @see BeanPropertyBindingResult#resolveMessageCodes
 * @see org.springframework.beans.PropertyAccessException#getErrorCode
 * @see org.springframework.beans.TypeMismatchException#ERROR_CODE
 * @see org.springframework.beans.MethodInvocationException#ERROR_CODE
 */
public class DefaultBindingErrorProcessor implements BindingErrorProcessor {

	/**
	 * 缺失字段错误（即必填字段未出现在属性值列表中）注册时使用的错误码："required"。
	 */
	public static final String MISSING_FIELD_ERROR_CODE = "required";


	@Override
	public void processMissingFieldError(String missingField, BindingResult bindingResult) {
		// Create field error with code "required".
		String fixedField = bindingResult.getNestedPath() + missingField;
		String[] codes = bindingResult.resolveMessageCodes(MISSING_FIELD_ERROR_CODE, missingField);
		Object[] arguments = getArgumentsForBindError(bindingResult.getObjectName(), fixedField);
		bindingResult.addError(new BindingFieldError(
				bindingResult.getObjectName(), fixedField, "", codes, arguments));
	}

	@Override
	public void processPropertyAccessException(PropertyAccessException ex, BindingResult bindingResult) {
		// Create field error with the code of the exception, for example, "typeMismatch".
		String field = ex.getPropertyName();
		Assert.state(field != null, "No field in exception");
		String[] codes = bindingResult.resolveMessageCodes(ex.getErrorCode(), field);
		Object[] arguments = getArgumentsForBindError(bindingResult.getObjectName(), field);
		Object rejectedValue = ex.getValue();
		if (ObjectUtils.isArray(rejectedValue)) {
			rejectedValue = StringUtils.arrayToCommaDelimitedString(ObjectUtils.toObjectArray(rejectedValue));
		}
		bindingResult.addError(new BindingFieldError(
				bindingResult.getObjectName(), field, rejectedValue, codes, arguments, ex));
	}

	/**
	 * 返回给定字段绑定错误的 FieldError 参数。
	 * 对每个缺失必填字段与类型不匹配都会调用。
	 * <p>默认实现返回单个参数表示字段名
	 * （类型为 DefaultMessageSourceResolvable，codes 为 "objectName.field" 与 "field"）。
	 * @param objectName 目标对象名称
	 * @param field 导致绑定错误的字段
	 * @return 表示 FieldError 参数的 Object 数组
	 * @see org.springframework.validation.FieldError#getArguments
	 * @see org.springframework.context.support.DefaultMessageSourceResolvable
	 */
	protected Object[] getArgumentsForBindError(String objectName, String field) {
		String[] codes = new String[] {objectName + Errors.NESTED_PATH_SEPARATOR + field, field};
		return new Object[] {new DefaultMessageSourceResolvable(codes, field)};
	}


	/**
	 * 采用 Spring 风格默认消息渲染的 {@code FieldError} 子类。
	 */
	@SuppressWarnings("serial")
	private static class BindingFieldError extends FieldError implements Serializable {

		public BindingFieldError(String objectName, String field, @Nullable Object rejectedValue, String[] codes,
				Object[] arguments) {

			super(objectName, field, rejectedValue, true, codes, arguments,
					"Field '" + field + "' is required");
		}

		public BindingFieldError(String objectName, String field, @Nullable Object rejectedValue, String[] codes,
				Object[] arguments, PropertyAccessException ex) {

			super(objectName, field, rejectedValue, true, codes, arguments, ex.getLocalizedMessage());
			wrap(ex);
		}

		@Override
		public boolean shouldRenderDefaultMessage() {
			return false;
		}
	}

}
