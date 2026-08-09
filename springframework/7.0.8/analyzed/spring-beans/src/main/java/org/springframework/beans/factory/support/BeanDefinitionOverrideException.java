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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * {@link BeanDefinitionStoreException} 的子类，表示无效的 Bean 定义覆盖尝试：
 * 通常在 {@link DefaultListableBeanFactory#isAllowBeanDefinitionOverriding()} 为 {@code false} 时，
 * 为同一 Bean 名称注册新定义而抛出。
 *
 * @author Juergen Hoeller
 * @since 5.1
 * @see DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
 * @see DefaultListableBeanFactory#registerBeanDefinition
 */
@SuppressWarnings("serial")
public class BeanDefinitionOverrideException extends BeanDefinitionStoreException {

	private final BeanDefinition beanDefinition;

	private final BeanDefinition existingDefinition;


	/**
	 * 为给定的新定义与已有定义创建 BeanDefinitionOverrideException。
	 * @param beanName Bean 名称
	 * @param beanDefinition 新注册的 Bean 定义
	 * @param existingDefinition 同名的已有 Bean 定义
	 */
	public BeanDefinitionOverrideException(
			String beanName, BeanDefinition beanDefinition, BeanDefinition existingDefinition) {

		super(beanDefinition.getResourceDescription(), beanName,
				"Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName +
				"' since there is already [" + existingDefinition + "] bound.");
		this.beanDefinition = beanDefinition;
		this.existingDefinition = existingDefinition;
	}

	/**
	 * 为给定的新定义与已有定义创建 BeanDefinitionOverrideException。
	 * @param beanName Bean 名称
	 * @param beanDefinition 新注册的 Bean 定义
	 * @param existingDefinition 同名的已有 Bean 定义
	 * @param msg 要包含的详细消息
	 * @since 6.2.1
	 */
	public BeanDefinitionOverrideException(
			String beanName, BeanDefinition beanDefinition, BeanDefinition existingDefinition, String msg) {

		super(beanDefinition.getResourceDescription(), beanName, msg);
		this.beanDefinition = beanDefinition;
		this.existingDefinition = existingDefinition;
	}


	/**
	 * 返回 Bean 定义来源资源的描述。
	 */
	@Override
	public String getResourceDescription() {
		return String.valueOf(super.getResourceDescription());
	}

	/**
	 * 返回 Bean 名称。
	 */
	@Override
	public String getBeanName() {
		return String.valueOf(super.getBeanName());
	}

	/**
	 * 返回新注册的 Bean 定义。
	 * @see #getBeanName()
	 */
	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

	/**
	 * 返回同名的已有 Bean 定义。
	 * @see #getBeanName()
	 */
	public BeanDefinition getExistingDefinition() {
		return this.existingDefinition;
	}

}
