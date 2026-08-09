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

package org.springframework.beans.factory.support;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.AutowiredPropertyMarker;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

/**
 * 使用建造者模式以编程方式构造
 * {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinition} 的工具类。
 * 主要用于实现 Spring 2.0 的
 * {@link org.springframework.beans.factory.xml.NamespaceHandler NamespaceHandler}。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Yanming Zhou
 * @since 2.0
 */
public final class BeanDefinitionBuilder {

	/**
	 * 创建用于构造 {@link GenericBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 */
	public static BeanDefinitionBuilder genericBeanDefinition() {
		return new BeanDefinitionBuilder(new GenericBeanDefinition());
	}

	/**
	 * 创建用于构造 {@link GenericBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClassName 要创建定义的 Bean 的类名
	 */
	public static BeanDefinitionBuilder genericBeanDefinition(String beanClassName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new GenericBeanDefinition());
		builder.beanDefinition.setBeanClassName(beanClassName);
		return builder;
	}

	/**
	 * 创建用于构造 {@link GenericBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClass 要创建定义的 Bean 的 {@code Class}
	 */
	public static BeanDefinitionBuilder genericBeanDefinition(Class<?> beanClass) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new GenericBeanDefinition());
		builder.beanDefinition.setBeanClass(beanClass);
		return builder;
	}

	/**
	 * 创建用于构造 {@link GenericBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClass 要创建定义的 Bean 的 {@code Class}
	 * @param instanceSupplier 创建 Bean 实例的回调
	 * @since 5.0
	 */
	public static <T> BeanDefinitionBuilder genericBeanDefinition(Class<T> beanClass, Supplier<T> instanceSupplier) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new GenericBeanDefinition());
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setInstanceSupplier(instanceSupplier);
		return builder;
	}

	/**
	 * 创建用于构造 {@link RootBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClassName 要创建定义的 Bean 的类名
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName) {
		return rootBeanDefinition(beanClassName, null);
	}

	/**
	 * 创建用于构造 {@link RootBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClassName 要创建定义的 Bean 的类名
	 * @param factoryMethodName 用于构造 Bean 实例的工厂方法名
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName, @Nullable String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new RootBeanDefinition());
		builder.beanDefinition.setBeanClassName(beanClassName);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}

	/**
	 * 创建用于构造 {@link RootBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClass 要创建定义的 Bean 的 {@code Class}
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass) {
		return rootBeanDefinition(beanClass, (String) null);
	}

	/**
	 * 创建用于构造 {@link RootBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClass 要创建定义的 Bean 的 {@code Class}
	 * @param factoryMethodName 用于构造 Bean 实例的工厂方法名
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass, @Nullable String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new RootBeanDefinition());
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}

	/**
	 * 创建用于构造 {@link RootBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanType 要创建定义的 Bean 的 {@link ResolvableType 类型}
	 * @param instanceSupplier 创建 Bean 实例的回调
	 * @since 5.3.9
	 */
	public static <T> BeanDefinitionBuilder rootBeanDefinition(ResolvableType beanType, Supplier<T> instanceSupplier) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(beanType);
		beanDefinition.setInstanceSupplier(instanceSupplier);
		return new BeanDefinitionBuilder(beanDefinition);
	}

	/**
	 * 创建用于构造 {@link RootBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param beanClass 要创建定义的 Bean 的 {@code Class}
	 * @param instanceSupplier 创建 Bean 实例的回调
	 * @since 5.3.9
	 * @see #rootBeanDefinition(ResolvableType, Supplier)
	 */
	public static <T> BeanDefinitionBuilder rootBeanDefinition(Class<T> beanClass, Supplier<T> instanceSupplier) {
		return rootBeanDefinition(ResolvableType.forClass(beanClass), instanceSupplier);
	}

	/**
	 * 创建用于构造 {@link ChildBeanDefinition} 的新 {@code BeanDefinitionBuilder}。
	 * @param parentName 父 Bean 的名称
	 */
	public static BeanDefinitionBuilder childBeanDefinition(String parentName) {
		return new BeanDefinitionBuilder(new ChildBeanDefinition(parentName));
	}


	/** 正在创建的 {@code BeanDefinition} 实例。 */
	private final AbstractBeanDefinition beanDefinition;

	/** 当前构造函数参数索引位置。 */
	private int constructorArgIndex;


	/**
	 * 强制通过工厂方法创建实例。
	 */
	private BeanDefinitionBuilder(AbstractBeanDefinition beanDefinition) {
		this.beanDefinition = beanDefinition;
	}

	/**
	 * 返回当前 BeanDefinition 对象的原始（未校验）形式。
	 * @see #getBeanDefinition()
	 */
	public AbstractBeanDefinition getRawBeanDefinition() {
		return this.beanDefinition;
	}

	/**
	 * 校验并返回已创建的 BeanDefinition 对象。
	 */
	public AbstractBeanDefinition getBeanDefinition() {
		this.beanDefinition.validate();
		return this.beanDefinition;
	}


	/**
	 * 设置此 Bean 定义的父定义名称。
	 */
	public BeanDefinitionBuilder setParentName(String parentName) {
		this.beanDefinition.setParentName(parentName);
		return this;
	}

	/**
	 * 设置用于此定义的静态工厂方法名，将在本 Bean 的类上调用。
	 */
	public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	/**
	 * 设置用于此定义的非静态工厂方法名，包括要调用该方法的工厂实例的 Bean 名称。
	 * @param factoryMethod 工厂方法名
	 * @param factoryBean 要调用指定工厂方法的 Bean 名称
	 * @since 4.3.6
	 */
	public BeanDefinitionBuilder setFactoryMethodOnBean(String factoryMethod, String factoryBean) {
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		this.beanDefinition.setFactoryBeanName(factoryBean);
		return this;
	}

	/**
	 * 添加带索引的构造函数参数值。内部跟踪当前索引，所有添加均发生在当前位置。
	 */
	public BeanDefinitionBuilder addConstructorArgValue(@Nullable Object value) {
		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				this.constructorArgIndex++, value);
		return this;
	}

	/**
	 * 添加对命名 Bean 的引用作为构造函数参数。
	 * @see #addConstructorArgValue(Object)
	 */
	public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				this.constructorArgIndex++, new RuntimeBeanReference(beanName));
		return this;
	}

	/**
	 * 在指定属性名下添加提供的属性值。
	 */
	public BeanDefinitionBuilder addPropertyValue(String name, @Nullable Object value) {
		this.beanDefinition.getPropertyValues().add(name, value);
		return this;
	}

	/**
	 * 在指定属性下添加对指定 Bean 名称的引用。
	 * @param name 要添加引用的属性名
	 * @param beanName 被引用的 Bean 名称
	 */
	public BeanDefinitionBuilder addPropertyReference(String name, String beanName) {
		this.beanDefinition.getPropertyValues().add(name, new RuntimeBeanReference(beanName));
		return this;
	}

	/**
	 * 为指定 Bean 的指定属性添加自动装配标记。
	 * @param name 要标记为自动装配的属性名
	 * @since 5.2
	 * @see AutowiredPropertyMarker
	 */
	public BeanDefinitionBuilder addAutowiredProperty(String name) {
		this.beanDefinition.getPropertyValues().add(name, AutowiredPropertyMarker.INSTANCE);
		return this;
	}

	/**
	 * 设置此定义的初始化方法。
	 */
	public BeanDefinitionBuilder setInitMethodName(@Nullable String methodName) {
		this.beanDefinition.setInitMethodName(methodName);
		return this;
	}

	/**
	 * 设置此定义的销毁方法。
	 */
	public BeanDefinitionBuilder setDestroyMethodName(@Nullable String methodName) {
		this.beanDefinition.setDestroyMethodName(methodName);
		return this;
	}


	/**
	 * 设置此定义的作用域。
	 * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_SINGLETON
	 * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_PROTOTYPE
	 */
	public BeanDefinitionBuilder setScope(@Nullable String scope) {
		this.beanDefinition.setScope(scope);
		return this;
	}

	/**
	 * 设置此定义是否为抽象定义。
	 */
	public BeanDefinitionBuilder setAbstract(boolean flag) {
		this.beanDefinition.setAbstract(flag);
		return this;
	}

	/**
	 * 设置此定义对应的 Bean 是否应懒加载初始化。
	 */
	public BeanDefinitionBuilder setLazyInit(boolean lazy) {
		this.beanDefinition.setLazyInit(lazy);
		return this;
	}

	/**
	 * 设置此定义的自动装配模式。
	 */
	public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
		this.beanDefinition.setAutowireMode(autowireMode);
		return this;
	}

	/**
	 * 设置此定义的依赖检查模式。
	 */
	public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
		this.beanDefinition.setDependencyCheck(dependencyCheck);
		return this;
	}

	/**
	 * 将指定 Bean 名称追加到此定义所依赖的 Bean 列表。
	 */
	public BeanDefinitionBuilder addDependsOn(String beanName) {
		if (this.beanDefinition.getDependsOn() == null) {
			this.beanDefinition.setDependsOn(beanName);
		}
		else {
			String[] added = ObjectUtils.addObjectToArray(this.beanDefinition.getDependsOn(), beanName);
			this.beanDefinition.setDependsOn(added);
		}
		return this;
	}

	/**
	 * 设置此 Bean 是否为主要的自动装配候选者。
	 * @since 5.1.11
	 */
	public BeanDefinitionBuilder setPrimary(boolean primary) {
		this.beanDefinition.setPrimary(primary);
		return this;
	}

	/**
	 * 设置此 Bean 是否为后备自动装配候选者。
	 * @since 6.2
	 */
	public BeanDefinitionBuilder setFallback(boolean fallback) {
		this.beanDefinition.setFallback(fallback);
		return this;
	}

	/**
	 * 设置此定义的角色。
	 */
	public BeanDefinitionBuilder setRole(int role) {
		this.beanDefinition.setRole(role);
		return this;
	}

	/**
	 * 设置此 Bean 是否为"合成"Bean，即非由应用本身定义。
	 * @since 5.3.9
	 */
	public BeanDefinitionBuilder setSynthetic(boolean synthetic) {
		this.beanDefinition.setSynthetic(synthetic);
		return this;
	}

	/**
	 * 将给定定制器应用于底层 Bean 定义。
	 * @since 5.0
	 */
	public BeanDefinitionBuilder applyCustomizers(BeanDefinitionCustomizer... customizers) {
		for (BeanDefinitionCustomizer customizer : customizers) {
			customizer.customize(this.beanDefinition);
		}
		return this;
	}

}
