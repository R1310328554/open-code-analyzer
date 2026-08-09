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

package org.springframework.beans.factory.wiring;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

/**
 * 持有某一特定类的 Bean 装配元数据，与
 * {@link org.springframework.beans.factory.annotation.Configurable} 注解及
 * AspectJ {@code AnnotationBeanConfigurerAspect} 配合使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWiringInfoResolver
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 * @see org.springframework.beans.factory.annotation.Configurable
 */
public class BeanWiringInfo {

	/**
	 * 表示按名称自动装配 Bean 属性的常量。
	 * @see #BeanWiringInfo(int, boolean)
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_NAME
	 */
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

	/**
	 * 表示按类型自动装配 Bean 属性的常量。
	 * @see #BeanWiringInfo(int, boolean)
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_TYPE
	 */
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;


	/** 目标 Bean 定义的名称；为 null 时表示自动装配模式。 */
	private @Nullable String beanName;

	/** 是否为建议的默认 Bean 名（未必对应工厂中的实际定义）。 */
	private boolean isDefaultBeanName = false;

	/** 自动装配模式，默认为不自动装配。 */
	private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;

	/** 自动装配后是否执行依赖检查。 */
	private boolean dependencyCheck = false;


	/**
	 * 创建默认 BeanWiringInfo，仅建议执行 Bean 类可能期望的工厂与后处理器回调初始化。
	 */
	public BeanWiringInfo() {
	}

	/**
	 * 创建指向给定 Bean 名的 BeanWiringInfo。
	 * @param beanName 用于获取属性值的 Bean 定义名
	 * @throws IllegalArgumentException 若 beanName 为 {@code null}、空或仅含空白
	 */
	public BeanWiringInfo(String beanName) {
		this(beanName, false);
	}

	/**
	 * 创建指向给定 Bean 名的 BeanWiringInfo。
	 * @param beanName 用于获取属性值的 Bean 定义名
	 * @param isDefaultBeanName 给定名称是否为建议的默认 Bean 名（未必对应实际 Bean 定义）
	 * @throws IllegalArgumentException 若 beanName 为 {@code null}、空或仅含空白
	 */
	public BeanWiringInfo(String beanName, boolean isDefaultBeanName) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
		this.isDefaultBeanName = isDefaultBeanName;
	}

	/**
	 * 创建表示自动装配的 BeanWiringInfo。
	 * @param autowireMode 常量之一：{@link #AUTOWIRE_BY_NAME} / {@link #AUTOWIRE_BY_TYPE}
	 * @param dependencyCheck 自动装配后是否对 Bean 实例中的对象引用执行依赖检查
	 * @throws IllegalArgumentException 若 autowireMode 不是允许的值
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 */
	public BeanWiringInfo(int autowireMode, boolean dependencyCheck) {
		if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("Only constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE supported");
		}
		this.autowireMode = autowireMode;
		this.dependencyCheck = dependencyCheck;
	}


	/**
	 * 返回本 BeanWiringInfo 是否表示自动装配（即未指定 beanName）。
	 */
	public boolean indicatesAutowiring() {
		return (this.beanName == null);
	}

	/**
	 * 返回本 BeanWiringInfo 指向的 Bean 名（若有）。
	 */
	public @Nullable String getBeanName() {
		return this.beanName;
	}

	/**
	 * 返回指定 Bean 名是否为建议的默认名，未必对应工厂中的实际 Bean 定义。
	 */
	public boolean isDefaultBeanName() {
		return this.isDefaultBeanName;
	}

	/**
	 * 若表示自动装配，返回 {@link #AUTOWIRE_BY_NAME} 或 {@link #AUTOWIRE_BY_TYPE} 之一。
	 */
	public int getAutowireMode() {
		return this.autowireMode;
	}

	/**
	 * 返回自动装配后是否对 Bean 实例中的对象引用执行依赖检查。
	 */
	public boolean getDependencyCheck() {
		return this.dependencyCheck;
	}

}
