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

import org.jspecify.annotations.Nullable;

/**
 * 引用无效的 bean 属性时抛出的异常。
 * 携带出问题的 bean 类型与属性名。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
@SuppressWarnings("serial")
public class InvalidPropertyException extends FatalBeanException {

	/** 出问题的 bean 类型 */
	private final Class<?> beanClass;

	/** 出问题的属性名 */
	private final String propertyName;


	/**
	 * 创建一个新的 {@code InvalidPropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性
	 * @param msg 详细消息
	 */
	public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg) {
		this(beanClass, propertyName, msg, null);
	}

	/**
	 * 创建一个新的 {@code InvalidPropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性
	 * @param msg 详细消息
	 * @param cause 根因
	 */
	public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg, @Nullable Throwable cause) {
		super("Invalid property '" + propertyName + "' of bean class [" + beanClass.getName() + "]: " + msg, cause);
		this.beanClass = beanClass;
		this.propertyName = propertyName;
	}

	/**
	 * 返回出问题的 bean 类型。
	 */
	public Class<?> getBeanClass() {
		return this.beanClass;
	}

	/**
	 * 返回出问题的属性名。
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

}
