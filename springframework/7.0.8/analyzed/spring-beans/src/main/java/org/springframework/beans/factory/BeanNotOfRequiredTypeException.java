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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.util.ClassUtils;

/**
 * bean 与期望类型不符时抛出的异常。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class BeanNotOfRequiredTypeException extends BeansException {

	/** 类型不符的实例名称 */
	private final String beanName;

	/** 期望的类型 */
	private final Class<?> requiredType;

	/** 实际（不符）的类型 */
	private final Class<?> actualType;


	/**
	 * 创建一个新的 {@code BeanNotOfRequiredTypeException}。
	 * @param beanName 被请求的 bean 名称
	 * @param requiredType 期望的类型
	 * @param actualType 实际返回的类型，与期望类型不符
	 */
	public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Class<?> actualType) {
		super("Bean named '" + beanName + "' is expected to be of type '" + ClassUtils.getQualifiedName(requiredType) +
				"' but was actually of type '" + ClassUtils.getQualifiedName(actualType) + "'");
		this.beanName = beanName;
		this.requiredType = requiredType;
		this.actualType = actualType;
	}


	/**
	 * 返回类型不符的实例名称。
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * 返回 bean 的期望类型。
	 */
	public Class<?> getRequiredType() {
		return this.requiredType;
	}

	/**
	 * 返回实际找到的实例类型。
	 */
	public Class<?> getActualType() {
		return this.actualType;
	}

}
