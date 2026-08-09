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

package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;

import org.springframework.beans.factory.BeanFactory;

/**
 * {@link org.springframework.aop.aspectj.AspectInstanceFactory} 由 {@link BeanFactory}
 * 提供的原型支持，强制执行原型语义。
 * <p>请注意，这可能会实例化多次，这可能不会给出您期望的语义。使用 {@link LazySingletonAspectInstanceFactoryDecorator} 来包
 * 装它以确保只返回一个新方面。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see LazySingletonAspectInstanceFactoryDecorator
 */
@SuppressWarnings("serial")
public class PrototypeAspectInstanceFactory extends BeanFactoryAspectInstanceFactory implements Serializable {

	/**
	 * 创建一个 PrototypeAspectInstanceFactory。将调用 AspectJ 进行内省，以使用从 BeanFactory 为给定 bean
	 * 名称返回的类型来创建 AJType 元数据。
	 * @param beanFactory 从中获取实例的 BeanFactory
	 * @param name 豆子的名字
	 */
	public PrototypeAspectInstanceFactory(BeanFactory beanFactory, String name) {
		super(beanFactory, name);
		if (!beanFactory.isPrototype(name)) {
			throw new IllegalArgumentException(
					"Cannot use PrototypeAspectInstanceFactory with bean named '" + name + "': not a prototype");
		}
	}

}
