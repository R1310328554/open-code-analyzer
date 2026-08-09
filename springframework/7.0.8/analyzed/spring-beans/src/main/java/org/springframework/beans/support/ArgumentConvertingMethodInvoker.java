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

package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ReflectionUtils;

/**
 * {@link MethodInvoker} 的子类，尝试通过 {@link TypeConverter} 将给定参数
 * 转换为实际目标方法所需的类型。
 *
 * <p>支持灵活的参数类型转换，尤其适用于调用特定的重载方法。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.BeanWrapperImpl#convertIfNecessary
 */
public class ArgumentConvertingMethodInvoker extends MethodInvoker {

	private @Nullable TypeConverter typeConverter;

	private boolean useDefaultConverter = true;


	/**
	 * 设置用于参数类型转换的 TypeConverter。
	 * <p>默认为 {@link org.springframework.beans.SimpleTypeConverter}。
	 * 可替换为任意 TypeConverter 实现，通常为预配置的 SimpleTypeConverter
	 * 或 BeanWrapperImpl 实例。
	 * @see org.springframework.beans.SimpleTypeConverter
	 * @see org.springframework.beans.BeanWrapperImpl
	 */
	public void setTypeConverter(@Nullable TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
		this.useDefaultConverter = (typeConverter == null);
	}

	/**
	 * 返回用于参数类型转换的 TypeConverter。
	 * <p>若当前 TypeConverter 实际实现了 PropertyEditorRegistry 接口，
	 * 可强制转换为 {@link org.springframework.beans.PropertyEditorRegistry}，
	 * 以便直接访问底层 PropertyEditor。
	 */
	public @Nullable TypeConverter getTypeConverter() {
		if (this.typeConverter == null && this.useDefaultConverter) {
			this.typeConverter = getDefaultTypeConverter();
		}
		return this.typeConverter;
	}

	/**
	 * 获取此 MethodInvoker 的默认 TypeConverter。
	 * <p>在未显式指定 TypeConverter 时调用。
	 * 默认实现会构建一个 {@link org.springframework.beans.SimpleTypeConverter}。
	 * 子类可覆盖。
	 */
	protected TypeConverter getDefaultTypeConverter() {
		return new SimpleTypeConverter();
	}

	/**
	 * 为给定类型的所有属性注册自定义 PropertyEditor。
	 * <p>通常与默认的 {@link org.springframework.beans.SimpleTypeConverter} 配合使用；
	 * 也适用于实现了 PropertyEditorRegistry 接口的任意 TypeConverter。
	 * @param requiredType 属性类型
	 * @param propertyEditor 要注册的编辑器
	 * @see #setTypeConverter
	 * @see org.springframework.beans.PropertyEditorRegistry#registerCustomEditor
	 */
	public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
		TypeConverter converter = getTypeConverter();
		if (!(converter instanceof PropertyEditorRegistry registry)) {
			throw new IllegalStateException(
					"TypeConverter does not implement PropertyEditorRegistry interface: " + converter);
		}
		registry.registerCustomEditor(requiredType, propertyEditor);
	}


	/**
	 * 查找参数类型匹配的方法。
	 * @see #doFindMatchingMethod
	 */
	@Override
	protected @Nullable Method findMatchingMethod() {
		Method matchingMethod = super.findMatchingMethod();
		// 第二轮：查找可通过类型转换匹配参数的方法
		if (matchingMethod == null) {
			// 将参数数组视为独立的方法参数
			matchingMethod = doFindMatchingMethod(getArguments());
		}
		if (matchingMethod == null) {
			// 将参数数组视为单个数组类型的方法参数
			matchingMethod = doFindMatchingMethod(new Object[] {getArguments()});
		}
		return matchingMethod;
	}

	/**
	 * 实际查找参数类型匹配的方法，即每个参数值均可赋值给对应参数类型。
	 * @param arguments 用于与方法参数匹配的参数值
	 * @return 匹配的方法，若无则返回 {@code null}
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	protected @Nullable Method doFindMatchingMethod(@Nullable Object[] arguments) {
		TypeConverter converter = getTypeConverter();
		if (converter != null) {
			String targetMethod = getTargetMethod();
			Method matchingMethod = null;
			int argCount = arguments.length;
			Class<?> targetClass = getTargetClass();
			Assert.state(targetClass != null, "No target class set");
			Method[] candidates = ReflectionUtils.getAllDeclaredMethods(targetClass);
			int minTypeDiffWeight = Integer.MAX_VALUE;
			@Nullable Object[] argumentsToUse = null;
			for (Method candidate : candidates) {
				if (candidate.getName().equals(targetMethod)) {
					// 检查候选方法的参数个数是否正确
					int parameterCount = candidate.getParameterCount();
					if (parameterCount == argCount) {
						Class<?>[] paramTypes = candidate.getParameterTypes();
						@Nullable Object[] convertedArguments = new Object[argCount];
						boolean match = true;
						for (int j = 0; j < argCount && match; j++) {
							// 验证提供的参数是否可赋值给方法参数类型
							try {
								convertedArguments[j] = converter.convertIfNecessary(arguments[j], paramTypes[j]);
							}
							catch (TypeMismatchException ex) {
								// 忽略 —— 表示不匹配
								match = false;
							}
						}
						if (match) {
							int typeDiffWeight = getTypeDifferenceWeight(paramTypes, convertedArguments);
							if (typeDiffWeight < minTypeDiffWeight) {
								minTypeDiffWeight = typeDiffWeight;
								matchingMethod = candidate;
								argumentsToUse = convertedArguments;
							}
						}
					}
				}
			}
			if (matchingMethod != null) {
				setArguments(argumentsToUse);
				return matchingMethod;
			}
		}
		return null;
	}

}
