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
 * 沿合法嵌套属性路径导航时遇到 {@code NullPointerException} 而抛出的异常。
 *
 * <p>例如，访问 {@code "spouse.age"} 可能失败，因为目标对象上的
 * {@code spouse} 属性值为 {@code null}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class NullValueInNestedPathException extends InvalidPropertyException {

	/**
	 * 创建新的 {@code NullValueInNestedPathException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 */
	public NullValueInNestedPathException(Class<?> beanClass, String propertyName) {
		super(beanClass, propertyName, "Value of nested property '" + propertyName + "' is null");
	}

	/**
	 * 创建新的 {@code NullValueInNestedPathException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 * @param msg 详细消息
	 */
	public NullValueInNestedPathException(Class<?> beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
	}

	/**
	 * 创建新的 {@code NullValueInNestedPathException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param propertyName 出问题的属性名
	 * @param msg 详细消息
	 * @param cause 根因
	 * @since 4.3.2
	 */
	public NullValueInNestedPathException(Class<?> beanClass, String propertyName, String msg, Throwable cause) {
		super(beanClass, propertyName, msg, cause);
	}

}
