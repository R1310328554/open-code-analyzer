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
 * 不可变占位类，表示属性值为对工厂中另一 bean 名称的引用，在运行时解析。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see RuntimeBeanReference
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public class RuntimeBeanNameReference implements BeanReference {

	/** 目标 bean 名称。 */
	private final String beanName;

	/** 配置元数据来源对象。 */
	private @Nullable Object source;


	/**
	 * 为给定 bean 名称创建新的 RuntimeBeanNameReference。
	 * @param beanName 目标 bean 名称
	 */
	public RuntimeBeanNameReference(String beanName) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
	}

	@Override
	public String getBeanName() {
		return this.beanName;
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
		return (this == other || (other instanceof RuntimeBeanNameReference that &&
				this.beanName.equals(that.beanName)));
	}

	@Override
	public int hashCode() {
		return this.beanName.hashCode();
	}

	@Override
	public String toString() {
		return '<' + getBeanName() + '>';
	}

}
