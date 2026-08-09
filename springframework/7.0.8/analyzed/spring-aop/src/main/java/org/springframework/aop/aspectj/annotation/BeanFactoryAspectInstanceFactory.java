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
 * 由 Spring {@link org.springframework.beans.factory.BeanFactory} 支持的
 * {@link org.springframework.aop.aspectj.AspectInstanceFactory} 实现。
 *
 * <p>若使用 prototype 作用域，可能多次实例化，
 * 语义可能不符合预期。
 * 请用 {@link LazySingletonAspectInstanceFactoryDecorator} 包装，
 * 确保只返回一个新切面。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see LazySingletonAspectInstanceFactoryDecorator
 */
@SuppressWarnings("serial")
public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory, Serializable {

	private final BeanFactory beanFactory;

	private final String name;

	private final AspectMetadata aspectMetadata;


	/**
	 * 创建 BeanFactoryAspectInstanceFactory。
	 * AspectJ 将内省 BeanFactory 为给定 Bean 名称返回的类型以创建 AJType 元数据。
	 * @param beanFactory 获取实例的 BeanFactory
	 * @param name Bean 名称
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name) {
		this(beanFactory, name, null);
	}

	/**
	 * 创建 BeanFactoryAspectInstanceFactory，提供 AspectJ 应内省以创建 AJType 元数据的类型。
	 * 当 BeanFactory 可能将类型视为子类（如使用 CGLIB）且信息应关联超类时使用。
	 * @param beanFactory 获取实例的 BeanFactory
	 * @param name Bean 名称
	 * @param type AspectJ 应内省的类型
	 * （{@code null} 表示通过 Bean 名称经 {@link BeanFactory#getType} 解析）
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


	@Override
	public Object getAspectInstance() {
		return this.beanFactory.getBean(this.name);
	}

	@Override
	public @Nullable ClassLoader getAspectClassLoader() {
		return (this.beanFactory instanceof ConfigurableBeanFactory cbf ?
				cbf.getBeanClassLoader() : ClassUtils.getDefaultClassLoader());
	}

	@Override
	public AspectMetadata getAspectMetadata() {
		return this.aspectMetadata;
	}

	@Override
	public @Nullable Object getAspectCreationMutex() {
		if (this.beanFactory.isSingleton(this.name)) {
			// 依赖工厂提供的单例语义 -> 无需本地锁。
			return null;
		}
		else {
			// 工厂无单例保证 -> 本地加锁。
			return this;
		}
	}

	/**
	 * 确定本工厂目标切面的顺序：
	 * 要么通过实现 {@link org.springframework.core.Ordered} 接口表达的实例级顺序
	 * （仅检查单例 Bean），
	 * 要么通过类级 {@link org.springframework.core.annotation.Order} 注解表达。
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
					// 实际未实现 Ordered -> 可能是 NullBean。
				}
			}
			return OrderUtils.getOrder(type, Ordered.LOWEST_PRECEDENCE);
		}
		return Ordered.LOWEST_PRECEDENCE;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + ": bean name '" + this.name + "'";
	}

}
