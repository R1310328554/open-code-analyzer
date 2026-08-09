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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

import org.springframework.util.ClassUtils;

/**
 * {@link Method Method} 的描述符，持有方法的
 * {@linkplain #declaringClass 声明类}、{@linkplain #methodName 名称}
 * 以及 {@linkplain #parameterTypes 参数类型} 的引用。
 *
 * @author Sam Brannen
 * @since 6.0.11
 * @param declaringClass 方法的声明类
 * @param methodName 方法名称
 * @param parameterTypes 方法接受的参数类型
 */
record MethodDescriptor(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {

	/**
	 * 为给定的 Bean 类和方法名创建 {@link MethodDescriptor}。
	 * <p>提供的 {@code methodName} 可以是 {@linkplain Method#getName() 简单方法名}，
	 * 也可以是 {@linkplain ClassUtils#getQualifiedMethodName(Method) 全限定方法名}。
	 * <p>若方法名为全限定形式，本工具会从全限定名中解析方法名及其声明类，
	 * 并尝试使用所提供 {@code beanClass} 的 {@link ClassLoader} 加载声明类。
	 * 否则，返回的描述符将引用所提供的 {@code beanClass} 与 {@code methodName}。
	 * @param beanName 工厂中的 Bean 名称（用于调试）
	 * @param beanClass Bean 类
	 * @param methodName 方法名称
	 * @return 新的 {@code MethodDescriptor}；永不为 {@code null}
	 */
	static MethodDescriptor create(String beanName, Class<?> beanClass, String methodName) {
		try {
			Class<?> declaringClass = beanClass;
			String methodNameToUse = methodName;

			// 必要时解析全限定方法名
			int indexOfDot = methodName.lastIndexOf('.');
			if (indexOfDot > 0) {
				String className = methodName.substring(0, indexOfDot);
				methodNameToUse = methodName.substring(indexOfDot + 1);
				if (!beanClass.getName().equals(className)) {
					declaringClass = ClassUtils.forName(className, beanClass.getClassLoader());
				}
			}
			return new MethodDescriptor(declaringClass, methodNameToUse);
		}
		catch (Exception | LinkageError ex) {
			throw new BeanDefinitionValidationException(
					"Could not create MethodDescriptor for method '%s' on bean with name '%s': %s"
						.formatted(methodName, beanName, ex.getMessage()));
		}
	}

}
