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
 * 尝试写入不可写属性时抛出的异常。
 * <p>通常是因为没有对应的 setter 方法。
 *
 * @author Rod Johnson
 * @author Alef Arendsen
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class NotWritablePropertyException extends InvalidPropertyException {

	/** 与无效属性名相近的合法属性名建议（可能为 {@code null}）。 */
	private final String @Nullable [] possibleMatches;


	/**
	 * 创建新的 {@code NotWritablePropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 */
	public NotWritablePropertyException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName,
				"Bean property '" + propertyName + "' is not writable or has an invalid setter method: " +
				"Does the return type of the getter match the parameter type of the setter?");
		this.possibleMatches = null;
	}

	/**
	 * 创建新的 {@code NotWritablePropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 * @param msg 详细消息
	 */
	public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
		this.possibleMatches = null;
	}

	/**
	 * 创建新的 {@code NotWritablePropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 * @param msg 详细消息
	 * @param cause 根因
	 */
	public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg, Throwable cause) {
		super(beanClass, propertyName, msg, cause);
		this.possibleMatches = null;
	}

	/**
	 * 创建新的 {@code NotWritablePropertyException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 * @param msg 详细消息
	 * @param possibleMatches 与无效属性名相近的合法 bean 属性名建议
	 */
	public NotWritablePropertyException(Class<?> beanClass, String propertyName, String msg, String[] possibleMatches) {
		super(beanClass, propertyName, msg);
		this.possibleMatches = possibleMatches;
	}


	/**
	 * 返回与无效属性名相近的合法 bean 属性名建议（若有）。
	 */
	public String @Nullable [] getPossibleMatches() {
		return this.possibleMatches;
	}

}
