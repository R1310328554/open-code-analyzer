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
 * 由 {@link BeanFactory} 提供的 prototype Bean 支持的
 * {@link org.springframework.aop.aspectj.AspectInstanceFactory}，强制 prototype 语义。
 *
 * <p>注意，这可能多次实例化，通常无法得到预期语义。
 * 可用 {@link LazySingletonAspectInstanceFactoryDecorator} 包装，
 * 确保仅返回一个新切面实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see LazySingletonAspectInstanceFactoryDecorator
 */
@SuppressWarnings("serial")
public class PrototypeAspectInstanceFactory extends BeanFactoryAspectInstanceFactory implements Serializable {

	/**
	 * 创建 PrototypeAspectInstanceFactory。AspectJ 将内省
	 * BeanFactory 中给定 Bean 名称对应的类型以创建 AJType 元数据。
	 * @param beanFactory 获取实例的 BeanFactory
	 * @param name Bean 名称
	 */
	public PrototypeAspectInstanceFactory(BeanFactory beanFactory, String name) {
		super(beanFactory, name);
		if (!beanFactory.isPrototype(name)) {
			throw new IllegalArgumentException(
					"Cannot use PrototypeAspectInstanceFactory with bean named '" + name + "': not a prototype");
		}
	}

}
