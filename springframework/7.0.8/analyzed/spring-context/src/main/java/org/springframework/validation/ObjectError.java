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

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.Assert;

/**
 * 封装对象错误，即拒绝整个对象的全局原因。
 *
 * <p>关于 {@code ObjectError} 消息码列表的构建方式，
 * 详见 {@link DefaultMessageCodesResolver} 的 JavaDoc。
 *
 * @author Juergen Hoeller
 * @since 10.03.2003
 * @see FieldError
 * @see DefaultMessageCodesResolver
 */
@SuppressWarnings("serial")
public class ObjectError extends DefaultMessageSourceResolvable {

	private final String objectName;

	private transient @Nullable Object source;


	/**
	 * 创建新的 ObjectError 实例。
	 * @param objectName 受影响对象的名称
	 * @param defaultMessage 用于解析本消息的默认消息
	 */
	public ObjectError(String objectName, @Nullable String defaultMessage) {
		this(objectName, null, null, defaultMessage);
	}

	/**
	 * 创建新的 ObjectError 实例。
	 * @param objectName 受影响对象的名称
	 * @param codes 用于解析本消息的 codes
	 * @param arguments 用于解析本消息的参数数组
	 * @param defaultMessage 用于解析本消息的默认消息
	 */
	public ObjectError(
			String objectName, String @Nullable [] codes, Object @Nullable [] arguments, @Nullable String defaultMessage) {

		super(codes, arguments, defaultMessage);
		Assert.notNull(objectName, "Object name must not be null");
		this.objectName = objectName;
	}


	/**
	 * 返回受影响对象的名称。
	 */
	public String getObjectName() {
		return this.objectName;
	}

	/**
	 * 保留本错误背后的源对象：可能是 {@link Exception}
	 * （通常为 {@link org.springframework.beans.PropertyAccessException}）
	 * 或 Bean Validation 的 {@link jakarta.validation.ConstraintViolation}。
	 * <p>注意：此类源对象以 transient 存储，
	 * 即不会成为序列化错误表示的一部分。
	 * @param source 源对象
	 * @since 5.0.4
	 */
	public void wrap(Object source) {
		if (this.source != null) {
			throw new IllegalStateException("Already wrapping " + this.source);
		}
		this.source = source;
	}

	/**
	 * 解包本错误背后的源对象：可能是 {@link Exception}
	 * （通常为 {@link org.springframework.beans.PropertyAccessException}）
	 * 或 Bean Validation 的 {@link jakarta.validation.ConstraintViolation}。
	 * <p>也会内省最外层异常的 cause，
	 * 例如底层转换异常或 setter 抛出的异常
	 * （无需再逐层解包 {@code PropertyAccessException}）。
	 * @return 给定类型的源对象
	 * @throws IllegalArgumentException 若无可用源对象
	 * （即未指定或反序列化后不再可用）
	 * @since 5.0.4
	 */
	public <T> T unwrap(Class<T> sourceType) {
		if (sourceType.isInstance(this.source)) {
			return sourceType.cast(this.source);
		}
		else if (this.source instanceof Throwable throwable) {
			Throwable cause = throwable.getCause();
			if (sourceType.isInstance(cause)) {
				return sourceType.cast(cause);
			}
		}
		throw new IllegalArgumentException("No source object of the given type available: " + sourceType);
	}

	/**
	 * 检查本错误背后的源对象：可能是 {@link Exception}
	 * （通常为 {@link org.springframework.beans.PropertyAccessException}）
	 * 或 Bean Validation 的 {@link jakarta.validation.ConstraintViolation}。
	 * <p>也会内省最外层异常的 cause，
	 * 例如底层转换异常或 setter 抛出的异常
	 * （无需再逐层解包 {@code PropertyAccessException}）。
	 * @return 本错误是否由给定类型的源对象引起
	 * @since 5.0.4
	 */
	public boolean contains(Class<?> sourceType) {
		return (sourceType.isInstance(this.source) ||
				(this.source instanceof Throwable throwable && sourceType.isInstance(throwable.getCause())));
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || other.getClass() != getClass() || !super.equals(other)) {
			return false;
		}
		ObjectError otherError = (ObjectError) other;
		return getObjectName().equals(otherError.getObjectName());
	}

	@Override
	public int hashCode() {
		return (29 * super.hashCode() + getObjectName().hashCode());
	}

	@Override
	public String toString() {
		return "Error in object '" + this.objectName + "': " + resolvableToString();
	}

}
