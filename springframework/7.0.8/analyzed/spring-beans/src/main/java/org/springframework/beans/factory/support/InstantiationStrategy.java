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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

/**
 * 负责根据根 Bean 定义创建实例的接口。
 *
 * <p>抽取为策略接口是因为存在多种实现方式，
 * 包括使用 CGLIB 动态创建子类以支持方法注入。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public interface InstantiationStrategy {

	/**
	 * 在本工厂中返回具有给定名称的 Bean 实例。
	 * @param bd Bean 定义
	 * @param beanName 在本上下文中创建 Bean 时的名称。
	 * 若自动装配的 Bean 不属于该工厂，名称可为 {@code null}
	 * @param owner 所属的 BeanFactory
	 * @return 该 Bean 定义的 Bean 实例
	 * @throws BeansException 若实例化失败
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner)
			throws BeansException;

	/**
	 * 在本工厂中通过给定构造器创建并返回具有给定名称的 Bean 实例。
	 * @param bd Bean 定义
	 * @param beanName 在本上下文中创建 Bean 时的名称。
	 * 若自动装配的 Bean 不属于该工厂，名称可为 {@code null}
	 * @param owner 所属的 BeanFactory
	 * @param ctor 要使用的构造器
	 * @param args 要应用的构造器参数
	 * @return 该 Bean 定义的 Bean 实例
	 * @throws BeansException 若实例化失败
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			Constructor<?> ctor, Object... args) throws BeansException;

	/**
	 * 在本工厂中通过给定工厂方法创建并返回具有给定名称的 Bean 实例。
	 * @param bd Bean 定义
	 * @param beanName 在本上下文中创建 Bean 时的名称。
	 * 若自动装配的 Bean 不属于该工厂，名称可为 {@code null}
	 * @param owner 所属的 BeanFactory
	 * @param factoryBean 要调用工厂方法的工厂 Bean 实例，
	 * 静态工厂方法时为 {@code null}
	 * @param factoryMethod 要使用的工厂方法
	 * @param args 要应用的工厂方法参数
	 * @return 该 Bean 定义的 Bean 实例
	 * @throws BeansException 若实例化失败
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Object factoryBean, Method factoryMethod, @Nullable Object... args)
			throws BeansException;

	/**
	 * 确定给定 Bean 定义在运行时实际实例化的类。
	 * @since 6.0
	 */
	default Class<?> getActualBeanClass(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		return bd.getBeanClass();
	}

}
