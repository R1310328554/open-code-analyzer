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

package org.springframework.beans.factory.config;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 不可变占位类，表示属性值为对工厂中另一 bean 的引用，在运行时解析。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.BeanFactory#getBean(String)
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
 */
public class RuntimeBeanReference implements BeanReference {

	/** 目标 bean 名称（按类型解析时为全限定类名）。 */
	private final String beanName;

	/** 按类型解析时的目标 bean 类型。 */
	private final @Nullable Class<?> beanType;

	/** 是否显式引用父工厂中的 bean。 */
	private final boolean toParent;

	/** 配置元数据来源对象。 */
	private @Nullable Object source;


	/**
	 * 为给定 bean 名称创建新的 RuntimeBeanReference。
	 * @param beanName 目标 bean 名称
	 */
	public RuntimeBeanReference(String beanName) {
		this(beanName, false);
	}

	/**
	 * 为给定 bean 名称创建新的 RuntimeBeanReference，
	 * 并可标记为对父工厂中 bean 的显式引用。
	 * @param beanName 目标 bean 名称
	 * @param toParent 是否为对父工厂中 bean 的显式引用
	 */
	public RuntimeBeanReference(String beanName, boolean toParent) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
		this.beanType = null;
		this.toParent = toParent;
	}

	/**
	 * 为给定类型的 bean 创建新的 RuntimeBeanReference。
	 * @param beanType 目标 bean 类型
	 * @since 5.2
	 */
	public RuntimeBeanReference(Class<?> beanType) {
		this(beanType, false);
	}

	/**
	 * 为给定类型的 bean 创建新的 RuntimeBeanReference，
	 * 并可标记为对父工厂中 bean 的显式引用。
	 * @param beanType 目标 bean 类型
	 * @param toParent 是否为对父工厂中 bean 的显式引用
	 * @since 5.2
	 */
	public RuntimeBeanReference(Class<?> beanType, boolean toParent) {
		Assert.notNull(beanType, "'beanType' must not be null");
		this.beanName = beanType.getName();
		this.beanType = beanType;
		this.toParent = toParent;
	}

	/**
	 * 为给定类型的 bean 创建新的 RuntimeBeanReference。
	 * @param beanName 目标 bean 名称
	 * @param beanType 目标 bean 类型
	 * @since 7.0
	 */
	public RuntimeBeanReference(String beanName, Class<?> beanType) {
		this(beanName, beanType, false);
	}

	/**
	 * 为给定类型的 bean 创建新的 RuntimeBeanReference，
	 * 并可标记为对父工厂中 bean 的显式引用。
	 * @param beanName 目标 bean 名称
	 * @param beanType 目标 bean 类型
	 * @param toParent 是否为对父工厂中 bean 的显式引用
	 * @since 7.0
	 */
	public RuntimeBeanReference(String beanName, Class<?> beanType, boolean toParent) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		Assert.notNull(beanType, "'beanType' must not be null");
		this.beanName = beanName;
		this.beanType = beanType;
		this.toParent = toParent;
	}


	/**
	 * 返回请求的 bean 名称；按类型解析时返回全限定类型名。
	 * @see #getBeanType()
	 */
	@Override
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * 若要求按类型解析，返回请求的 bean 类型。
	 * @since 5.2
	 */
	public @Nullable Class<?> getBeanType() {
		return this.beanType;
	}

	/**
	 * 返回是否为对父工厂中 bean 的显式引用。
	 */
	public boolean isToParent() {
		return this.toParent;
	}

	/**
	 * 设置本元数据元素的配置来源 {@code Object}。
	 * <p>对象的具体类型取决于所使用的配置机制。
	 */
	public void setSource(@Nullable Object source) {
		this.source = source;
	}

	@Override
	public @Nullable Object getSource() {
		return this.source;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof RuntimeBeanReference that &&
				this.beanName.equals(that.beanName) && this.beanType == that.beanType &&
				this.toParent == that.toParent));
	}

	@Override
	public int hashCode() {
		int result = this.beanName.hashCode();
		result = 29 * result + (this.toParent ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return '<' + getBeanName() + '>';
	}

}
