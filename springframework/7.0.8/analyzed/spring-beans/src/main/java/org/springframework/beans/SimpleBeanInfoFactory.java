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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.springframework.core.Ordered;

/**
 * {@link BeanInfoFactory} 实现：绕过标准 {@link java.beans.Introspector} 以加快内省，
 * 仅做 Spring 中常见的基本属性判定。
 *
 * <p>6.0 起默认由 {@link CachedIntrospectionResults} 直接调用。
 * 也可通过 {@code META-INF/spring.factories} 配置下列内容，
 * 以覆盖其他自定义的 {@code org.springframework.beans.BeanInfoFactory} 声明：
 *
 * <p>{@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.SimpleBeanInfoFactory}
 *
 * <p>排序为 {@code Ordered.LOWEST_PRECEDENCE - 1}，以便在必要时覆盖
 * {@link ExtendedBeanInfoFactory}（5.3 中默认注册），同时仍允许其他用户定义的
 * {@link BeanInfoFactory} 优先。
 *
 * @author Juergen Hoeller
 * @since 5.3.24
 * @see ExtendedBeanInfoFactory
 * @see CachedIntrospectionResults
 */
class SimpleBeanInfoFactory implements BeanInfoFactory, Ordered {

	/**
	 * 通过基本属性判定为给定 bean 类构建 BeanInfo。
	 */
	@Override
	public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		PropertyDescriptor[] pds = PropertyDescriptorUtils.determineBasicProperties(beanClass)
				.toArray(PropertyDescriptorUtils.EMPTY_PROPERTY_DESCRIPTOR_ARRAY);

		return new SimpleBeanInfo() {
			@Override
			public BeanDescriptor getBeanDescriptor() {
				return new BeanDescriptor(beanClass);
			}
			@Override
			public PropertyDescriptor[] getPropertyDescriptors() {
				return pds;
			}
		};
	}

	/**
	 * 返回本工厂的排序值（略高于最低优先级）。
	 */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

}
