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

import org.springframework.beans.factory.NamedBean;
import org.springframework.util.Assert;

/**
 * 给定 Bean 名称与 Bean 实例的简单持有者。
 *
 * @author Juergen Hoeller
 * @since 4.3.3
 * @param <T> Bean 类型
 * @see AutowireCapableBeanFactory#resolveNamedBean(Class)
 */
public class NamedBeanHolder<T> implements NamedBean {

	/** Bean 名称。 */
	private final String beanName;

	/** 对应的 Bean 实例。 */
	private final T beanInstance;


	/**
	 * 为给定的 Bean 名称与实例创建新的持有者。
	 * @param beanName Bean 名称
	 * @param beanInstance 对应的 Bean 实例
	 */
	public NamedBeanHolder(String beanName, T beanInstance) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanName = beanName;
		this.beanInstance = beanInstance;
	}


	/**
	 * 返回 Bean 名称。
	 */
	@Override
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * 返回对应的 Bean 实例。
	 */
	public T getBeanInstance() {
		return this.beanInstance;
	}

}
