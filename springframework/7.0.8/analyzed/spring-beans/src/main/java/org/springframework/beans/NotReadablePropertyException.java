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

/**
 * 尝试读取不可读属性时抛出的异常。
 * <p>通常是因为没有对应的 getter 方法。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
@SuppressWarnings("serial")
public class NotReadablePropertyException extends InvalidPropertyException {

	/**
	 * 创建新的 {@code NotReadablePropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 */
	public NotReadablePropertyException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName,
				"Bean property '" + propertyName + "' is not readable or has an invalid getter method: " +
				"Does the return type of the getter match the parameter type of the setter?");
	}

	/**
	 * 创建新的 {@code NotReadablePropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 * @param msg 详细消息
	 */
	public NotReadablePropertyException(Class<?> beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
	}

	/**
	 * 创建新的 {@code NotReadablePropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 * @param msg 详细消息
	 * @param cause 根因
	 * @since 4.0.9
	 */
	public NotReadablePropertyException(Class<?> beanClass, String propertyName, String msg, Throwable cause) {
		super(beanClass, propertyName, msg, cause);
	}

}
