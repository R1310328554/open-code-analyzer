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
 * 请求为已标记为 abstract 的 bean 定义创建实例时抛出的异常。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setAbstract
 */
@SuppressWarnings("serial")
public class BeanIsAbstractException extends BeanCreationException {

	/**
	 * 创建一个新的 {@code BeanIsAbstractException}。
	 * @param beanName 被请求的 bean 名称
	 */
	public BeanIsAbstractException(String beanName) {
		super(beanName, "Bean definition is abstract");
	}

}
