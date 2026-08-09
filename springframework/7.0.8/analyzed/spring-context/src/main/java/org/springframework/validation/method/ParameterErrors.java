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

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * {@link ParameterValidationResult} 的扩展，
 * 为属性上存在嵌套错误的 Object 方法参数或返回值创建。
 *
 * <p>基类方法 {@link #getResolvableErrors()} 返回
 * {@link Errors#getAllErrors()}，但本子类以 {@link FieldError} 形式提供相同访问。
 *
 * @author Rossen Stoyanchev
 * @since 6.1
 */
public class ParameterErrors extends ParameterValidationResult implements Errors {

	private final Errors errors;


	/**
	 * 创建 {@code ParameterErrors}。
	 */
	public ParameterErrors(
			MethodParameter parameter, @Nullable Object argument, Errors errors,
			@Nullable Object container, @Nullable Integer index, @Nullable Object key) {

		super(parameter, argument, errors.getAllErrors(),
				container, index, key, (error, sourceType) -> ((FieldError) error).unwrap(sourceType));

		this.errors = errors;
	}


	// Errors implementation

	@Override
	public String getObjectName() {
		return this.errors.getObjectName();
	}

	@Override
	public void setNestedPath(String nestedPath) {
		this.errors.setNestedPath(nestedPath);
	}

	@Override
	public String getNestedPath() {
		return this.errors.getNestedPath();
	}

	@Override
	public void pushNestedPath(String subPath) {
		this.errors.pushNestedPath(subPath);
	}

	@Override
	public void popNestedPath() throws IllegalStateException {
		this.errors.popNestedPath();
	}

	@Override
	public void reject(String errorCode) {
		this.errors.reject(errorCode);
	}

	@Override
	public void reject(String errorCode, String defaultMessage) {
		this.errors.reject(errorCode, defaultMessage);
	}

	@Override
	public void reject(String errorCode, Object @Nullable [] errorArgs, @Nullable String defaultMessage) {
		this.errors.reject(errorCode, errorArgs, defaultMessage);
	}

	@Override
	public void rejectValue(@Nullable String field, String errorCode) {
		this.errors.rejectValue(field, errorCode);
	}

	@Override
	public void rejectValue(@Nullable String field, String errorCode, String defaultMessage) {
		this.errors.rejectValue(field, errorCode, defaultMessage);
	}

	@Override
	public void rejectValue(@Nullable String field, String errorCode,
			Object @Nullable [] errorArgs, @Nullable String defaultMessage) {

		this.errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
	}

	@Override
	public void addAllErrors(Errors errors) {
		this.errors.addAllErrors(errors);
	}

	@Override
	public boolean hasErrors() {
		return this.errors.hasErrors();
	}

	@Override
	public int getErrorCount() {
		return this.errors.getErrorCount();
	}

	@Override
	public List<ObjectError> getAllErrors() {
		return this.errors.getAllErrors();
	}

	@Override
	public boolean hasGlobalErrors() {
		return this.errors.hasGlobalErrors();
	}

	@Override
	public int getGlobalErrorCount() {
		return this.errors.getGlobalErrorCount();
	}

	@Override
	public List<ObjectError> getGlobalErrors() {
		return this.errors.getGlobalErrors();
	}

	@Override
	public @Nullable ObjectError getGlobalError() {
		return this.errors.getGlobalError();
	}

	@Override
	public boolean hasFieldErrors() {
		return this.errors.hasFieldErrors();
	}

	@Override
	public int getFieldErrorCount() {
		return this.errors.getFieldErrorCount();
	}

	@Override
	public List<FieldError> getFieldErrors() {
		return this.errors.getFieldErrors();
	}

	@Override
	public @Nullable FieldError getFieldError() {
		return this.errors.getFieldError();
	}

	@Override
	public boolean hasFieldErrors(String field) {
		return this.errors.hasFieldErrors(field);
	}

	@Override
	public int getFieldErrorCount(String field) {
		return this.errors.getFieldErrorCount(field);
	}

	@Override
	public List<FieldError> getFieldErrors(String field) {
		return this.errors.getFieldErrors(field);
	}

	@Override
	public @Nullable FieldError getFieldError(String field) {
		return this.errors.getFieldError(field);
	}

	@Override
	public @Nullable Object getFieldValue(String field) {
		return this.errors.getFieldError(field);
	}

	@Override
	public @Nullable Class<?> getFieldType(String field) {
		return this.errors.getFieldType(field);
	}

}
