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
import org.springframework.core.convert.TypeDescriptor;

/**
 * 定义类型转换方法的接口。通常（但并非必须）与
 * {@link PropertyEditorRegistry} 接口一并实现。
 *
 * <p><b>注意：</b>TypeConverter 实现通常基于非线程安全的
 * {@link java.beans.PropertyEditor PropertyEditor}，
 * 因此 TypeConverter 本身也<em>不应</em>视为线程安全。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleTypeConverter
 * @see BeanWrapperImpl
 */
public interface TypeConverter {

	/**
	 * 将值转换为所需类型（必要时从 String 转换）。
	 * <p>从 String 到任意类型的转换通常使用 PropertyEditor 的 {@code setAsText} 方法，
	 * 或 ConversionService 中的 Spring Converter。
	 * @param value 要转换的值
	 * @param requiredType 必须转换到的类型
	 * （若未知可为 {@code null}，例如集合元素场景）
	 * @return 新值，可能已是类型转换的结果
	 * @throws TypeMismatchException 类型转换失败时
	 * @see java.beans.PropertyEditor#setAsText(String)
	 * @see java.beans.PropertyEditor#getValue()
	 * @see org.springframework.core.convert.ConversionService
	 * @see org.springframework.core.convert.converter.Converter
	 */
	<T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType) throws TypeMismatchException;

	/**
	 * 将值转换为所需类型（必要时从 String 转换）。
	 * <p>从 String 到任意类型的转换通常使用 PropertyEditor 的 {@code setAsText} 方法，
	 * 或 ConversionService 中的 Spring Converter。
	 * @param value 要转换的值
	 * @param requiredType 必须转换到的类型
	 * （若未知可为 {@code null}，例如集合元素场景）
	 * @param methodParam 转换目标所对应的方法参数
	 * （用于分析泛型类型；可为 {@code null}）
	 * @return 新值，可能已是类型转换的结果
	 * @throws TypeMismatchException 类型转换失败时
	 * @see java.beans.PropertyEditor#setAsText(String)
	 * @see java.beans.PropertyEditor#getValue()
	 * @see org.springframework.core.convert.ConversionService
	 * @see org.springframework.core.convert.converter.Converter
	 */
	<T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable MethodParameter methodParam) throws TypeMismatchException;

	/**
	 * 将值转换为所需类型（必要时从 String 转换）。
	 * <p>从 String 到任意类型的转换通常使用 PropertyEditor 的 {@code setAsText} 方法，
	 * 或 ConversionService 中的 Spring Converter。
	 * @param value 要转换的值
	 * @param requiredType 必须转换到的类型
	 * （若未知可为 {@code null}，例如集合元素场景）
	 * @param field 转换目标所对应的反射字段
	 * （用于分析泛型类型；可为 {@code null}）
	 * @return 新值，可能已是类型转换的结果
	 * @throws TypeMismatchException 类型转换失败时
	 * @see java.beans.PropertyEditor#setAsText(String)
	 * @see java.beans.PropertyEditor#getValue()
	 * @see org.springframework.core.convert.ConversionService
	 * @see org.springframework.core.convert.converter.Converter
	 */
	<T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable Field field)
			throws TypeMismatchException;

	/**
	 * 将值转换为所需类型（必要时从 String 转换）。
	 * <p>从 String 到任意类型的转换通常使用 PropertyEditor 的 {@code setAsText} 方法，
	 * 或 ConversionService 中的 Spring Converter。
	 * @param value 要转换的值
	 * @param requiredType 必须转换到的类型
	 * （若未知可为 {@code null}，例如集合元素场景）
	 * @param typeDescriptor 要使用的类型描述符（可为 {@code null}）
	 * @return 新值，可能已是类型转换的结果
	 * @throws TypeMismatchException 类型转换失败时
	 * @since 5.1.4
	 * @see java.beans.PropertyEditor#setAsText(String)
	 * @see java.beans.PropertyEditor#getValue()
	 * @see org.springframework.core.convert.ConversionService
	 * @see org.springframework.core.convert.converter.Converter
	 */
	default <T> @Nullable T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {

		throw new UnsupportedOperationException("TypeDescriptor resolution not supported");
	}

}
