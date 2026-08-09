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

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 封装字段错误，即拒绝特定字段值的原因。
 *
 * <p>关于 {@code FieldError} 消息码列表的构建方式，
 * 详见 {@link DefaultMessageCodesResolver} 的 JavaDoc。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 10.03.2003
 * @see DefaultMessageCodesResolver
 */
@SuppressWarnings("serial")
public class FieldError extends ObjectError {

	private final String field;

	private final @Nullable Object rejectedValue;

	private final boolean bindingFailure;


	/**
	 * 创建新的 FieldError 实例。
	 * @param objectName 受影响对象的名称
	 * @param field 受影响对象的字段
	 * @param defaultMessage 用于解析本消息的默认消息
	 */
	public FieldError(String objectName, String field, String defaultMessage) {
		this(objectName, field, null, false, null, null, defaultMessage);
	}

	/**
	 * 创建新的 FieldError 实例。
	 * @param objectName 受影响对象的名称
	 * @param field 受影响对象的字段
	 * @param rejectedValue 被拒绝的字段值
	 * @param bindingFailure 本错误是否表示绑定失败（如类型不匹配）；
	 * 否则为校验失败
	 * @param codes 用于解析本消息的 codes
	 * @param arguments 用于解析本消息的参数数组
	 * @param defaultMessage 用于解析本消息的默认消息
	 */
	public FieldError(String objectName, String field, @Nullable Object rejectedValue, boolean bindingFailure,
			String @Nullable [] codes, Object @Nullable [] arguments, @Nullable String defaultMessage) {

		super(objectName, codes, arguments, defaultMessage);
		Assert.notNull(field, "Field must not be null");
		this.field = field;
		this.rejectedValue = rejectedValue;
		this.bindingFailure = bindingFailure;
	}


	/**
	 * 返回受影响对象的字段。
	 */
	public String getField() {
		return this.field;
	}

	/**
	 * 返回被拒绝的字段值。
	 */
	public @Nullable Object getRejectedValue() {
		return this.rejectedValue;
	}

	/**
	 * 返回本错误是否表示绑定失败（如类型不匹配）；
	 * 否则为校验失败。
	 */
	public boolean isBindingFailure() {
		return this.bindingFailure;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other)) {
			return false;
		}
		return (other instanceof FieldError otherError && getField().equals(otherError.getField()) &&
				ObjectUtils.nullSafeEquals(getRejectedValue(), otherError.getRejectedValue()) &&
				isBindingFailure() == otherError.isBindingFailure());
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		hashCode = 29 * hashCode + getField().hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getRejectedValue());
		hashCode = 29 * hashCode + (isBindingFailure() ? 1 : 0);
		return hashCode;
	}

	@Override
	public String toString() {
		// We would preferably use ObjectUtils.nullSafeConciseToString(rejectedValue) here but
		// keep including the full nullSafeToString representation for backwards compatibility.
		return "Field error in object '" + getObjectName() + "' on field '" + this.field +
				"': rejected value [" + ObjectUtils.nullSafeToString(this.rejectedValue) + "]; " +
				resolvableToString();
	}

}
