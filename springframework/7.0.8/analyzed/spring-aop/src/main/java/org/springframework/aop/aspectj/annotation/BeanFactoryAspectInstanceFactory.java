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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 由 Spring {@link org.springframework.beans.factory.BeanFactory} 支持的 {@link
 * org.springframework.aop.aspectj.AspectInstanceFactory} 实现。
 * <p>注意，如果使用原型，这可能会实例化多次，这可能不会给出您期望的语义。使用 {@link LazySingletonAspectInstanceFactoryDecorat
 * or} 来包装它以确保只返回一个新方面。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see LazySingletonAspectInstanceFactoryDecorator
 */
@SuppressWarnings("serial")
public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory, Serializable {

	/** 底层 BeanFactory 引用。 */
	private final BeanFactory beanFactory;

	/** 名称相关状态（`name`）。 */
	private final String name;

	/** `aspectMetadata`：该类的成员状态。 */
	private final AspectMetadata aspectMetadata;


	/**
	 * 创建 BeanFactoryAspectInstanceFactory。将调用 AspectJ 进行内省，以使用从 BeanFactory 为给定 bean
	 * 名称返回的类型来创建 AJType 元数据。
	 * @param beanFactory 从中获取实例的 BeanFactory
	 * @param name 豆子的名字
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name) {
		this(beanFactory, name, null);
	}

	/**
	 * 创建 BeanFactoryAspectInstanceFactory，提供 AspectJ 应内省以创建 AJType 元数据的类型。如果 BeanFactory
	 * 可能认为该类型是子类（如使用 CGLIB 时），并且信息应与超类相关，则使用。
	 * @param beanFactory 从中获取实例的 BeanFactory
	 * @param name 豆子的名字
	 * @param type AspectJ 应该内省的类型（{@code null} 表示通过 {@link BeanFactory#getType} 通过 bean 名称进行解析）
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name, @Nullable Class<?> type) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		Assert.notNull(name, "Bean name must not be null");
		this.beanFactory = beanFactory;
		this.name = name;
		Class<?> resolvedType = type;
		if (resolvedType == null) {
			resolvedType = beanFactory.getType(name);
			Assert.notNull(resolvedType, "Unresolvable bean type - explicitly specify the aspect class");
		}
		this.aspectMetadata = new AspectMetadata(resolvedType, name);
	}


	/**
	 * 获取 Aspect Instance（`AspectInstance`）。
	 */
	@Override
	public Object getAspectInstance() {
		return this.beanFactory.getBean(this.name);
	}

	/**
	 * 获取 Aspect Class Loader（`AspectClassLoader`）。
	 */
	@Override
	public @Nullable ClassLoader getAspectClassLoader() {
		return (this.beanFactory instanceof ConfigurableBeanFactory cbf ?
				cbf.getBeanClassLoader() : ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 获取 Aspect Metadata（`AspectMetadata`）。
	 */
	@Override
	public AspectMetadata getAspectMetadata() {
		return this.aspectMetadata;
	}

	/**
	 * 获取 Aspect Creation Mutex（`AspectCreationMutex`）。
	 */
	@Override
	public @Nullable Object getAspectCreationMutex() {
		if (this.beanFactory.isSingleton(this.name)) {
			// 依赖工厂提供的单例语义 -> 无本地锁。
			return null;
		}
		else {
			// 工厂没有单例保证 -> 让我们在本地锁定。
			return this;
		}
	}

	/**
	 * 确定此工厂的目标方面的顺序，可以是通过实现 {@link org.springframework.core.Ordered} 接口表达的特定于实例的顺序（仅检查单例 bean）
	 * ，也可以是通过类级别的 {@link org.springframework.core.annotation.Order} 注释表达的顺序。
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.annotation.Order
	 */
	@Override
	public int getOrder() {
		Class<?> type = this.beanFactory.getType(this.name);
		if (type != null) {
			if (Ordered.class.isAssignableFrom(type) && this.beanFactory.isSingleton(this.name)) {
				try {
					return this.beanFactory.getBean(this.name, Ordered.class).getOrder();
				}
				catch (BeanNotOfRequiredTypeException ex) {
					// 实际上没有实现 Ordered -> 可能是 NullBean。
				}
			}
			return OrderUtils.getOrder(type, Ordered.LOWEST_PRECEDENCE);
		}
		return Ordered.LOWEST_PRECEDENCE;
	}


	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": bean name '" + this.name + "'";
	}

}
