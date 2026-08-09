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

import java.beans.PropertyDescriptor;

/**
 * Spring 底层 JavaBeans 基础设施的核心接口。
 *
 * <p>通常不会直接使用，而是通过 {@link org.springframework.beans.factory.BeanFactory}
 * 或 {@link org.springframework.validation.DataBinder} 间接发挥作用。
 *
 * <p>提供对标准 JavaBeans 的分析与操作：可单独或批量读写属性值、
 * 获取属性描述符，以及查询属性是否可读/可写。
 *
 * <p>本接口支持 <b>嵌套属性</b>，可对子属性路径无限深度地设值。
 *
 * <p>BeanWrapper 上 {@code extractOldValueForEditor} 的默认值为 {@code false}，
 * 以免调用 getter 产生副作用。若需把当前属性值交给自定义编辑器，可将其设为 {@code true}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.BeanPropertyBindingResult
 * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
 */
public interface BeanWrapper extends ConfigurablePropertyAccessor {

	/**
	 * 设置数组与集合自动扩容的上限。
	 * <p>普通 BeanWrapper 默认无上限。
	 * @since 4.1
	 */
	void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

	/**
	 * 返回数组与集合自动扩容的上限。
	 * @since 4.1
	 */
	int getAutoGrowCollectionLimit();

	/**
	 * 返回本对象所包装的 bean 实例。
	 */
	Object getWrappedInstance();

	/**
	 * 返回所包装 bean 实例的类型。
	 */
	Class<?> getWrappedClass();

	/**
	 * 获取被包装对象的全部 PropertyDescriptor
	 * （由标准 JavaBeans 内省确定）。
	 * @return 被包装对象的 PropertyDescriptor 数组
	 */
	PropertyDescriptor[] getPropertyDescriptors();

	/**
	 * 获取被包装对象上指定属性的 PropertyDescriptor。
	 * @param propertyName 要获取描述符的属性名
	 * （可以是嵌套路径，但不能是索引/映射属性）
	 * @return 指定属性的 PropertyDescriptor
	 * @throws InvalidPropertyException 若不存在该属性
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

}
