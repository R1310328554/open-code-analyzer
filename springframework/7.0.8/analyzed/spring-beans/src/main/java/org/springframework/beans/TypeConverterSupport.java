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

package org.springframework.beans;

import java.lang.reflect.Field;

import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

/**
 * {@link TypeConverter} 接口的基础实现，通过包私有委托完成转换。
 * 主要作为 {@link BeanWrapperImpl} 的基类。
 *
 * @author Juergen Hoeller
 * @since 3.2
 * @see SimpleTypeConverter
 */
public abstract class TypeConverterSupport extends PropertyEditorRegistrySupport implements TypeConverter {

	/** 实际执行类型转换的委托对象。 */
	@Nullable TypeConverterDelegate typeConverterDelegate;


	/**
	 * 将值转换为所需类型（无额外类型上下文）。
	 */
	@Override
	public <T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType) throws TypeMismatchException {
		return convertIfNecessary(null, value, requiredType, TypeDescriptor.valueOf(requiredType));
	}

	/**
	 * 将值转换为所需类型，并利用方法参数分析泛型。
	 */
	@Override
	public <T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable MethodParameter methodParam) throws TypeMismatchException {

		return convertIfNecessary((methodParam != null ? methodParam.getParameterName() : null), value, requiredType,
				(methodParam != null ? new TypeDescriptor(methodParam) : TypeDescriptor.valueOf(requiredType)));
	}

	/**
	 * 将值转换为所需类型，并利用字段分析泛型。
	 */
	@Override
	public <T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable Field field)
			throws TypeMismatchException {

		return convertIfNecessary((field != null ? field.getName() : null), value, requiredType,
				(field != null ? new TypeDescriptor(field) : TypeDescriptor.valueOf(requiredType)));
	}

	/**
	 * 将值转换为所需类型，使用给定 TypeDescriptor。
	 */
	@Override
	public <T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {

		return convertIfNecessary(null, value, requiredType, typeDescriptor);
	}

	/**
	 * 委托 TypeConverterDelegate 执行实际转换，并将底层异常包装为 Beans 异常。
	 */
	private <T> @Nullable T convertIfNecessary(@Nullable String propertyName, @Nullable Object value,
			@Nullable Class<T> requiredType, @Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {

		Assert.state(this.typeConverterDelegate != null, "No TypeConverterDelegate");
		try {
			return this.typeConverterDelegate.convertIfNecessary(
					propertyName, null, value, requiredType, typeDescriptor);
		}
		catch (ConverterNotFoundException | IllegalStateException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		}
		catch (ConversionException | IllegalArgumentException ex) {
			throw new TypeMismatchException(value, requiredType, ex);
		}
	}

}
