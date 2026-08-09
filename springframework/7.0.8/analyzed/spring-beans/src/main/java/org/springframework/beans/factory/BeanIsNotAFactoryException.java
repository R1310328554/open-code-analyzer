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

/**
 * 当 bean 本身不是工厂，但用户试图获取该名称对应的工厂时抛出的异常。
 * 是否为工厂取决于该 bean 是否实现了 FactoryBean 接口。
 *
 * @author Rod Johnson
 * @since 10.03.2003
 * @see org.springframework.beans.factory.FactoryBean
 */
@SuppressWarnings("serial")
public class BeanIsNotAFactoryException extends BeanNotOfRequiredTypeException {

	/**
	 * 创建一个新的 {@code BeanIsNotAFactoryException}。
	 * @param name 被请求的 bean 名称
	 * @param actualType 实际返回的类型，与期望类型不符
	 */
	public BeanIsNotAFactoryException(String name, Class<?> actualType) {
		super(name, FactoryBean.class, actualType);
	}

}
