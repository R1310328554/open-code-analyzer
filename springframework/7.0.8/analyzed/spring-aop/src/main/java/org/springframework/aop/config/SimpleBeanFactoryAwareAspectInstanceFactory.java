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

package org.springframework.aop.config;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.aspectj.AspectInstanceFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link AspectInstanceFactory} 的实现，使用配置的 bean 名称从 {@link
 * org.springframework.beans.factory.BeanFactory} 中定位方面。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class SimpleBeanFactoryAwareAspectInstanceFactory implements AspectInstanceFactory, BeanFactoryAware {

	/** 名称相关状态（`aspectBeanName`）。 */
	private @Nullable String aspectBeanName;

	/** 底层 BeanFactory 引用。 */
	private @Nullable BeanFactory beanFactory;


	/**
	 * 设置方面 bean 的名称。这是调用 {@link #getAspectInstance()} 时返回的 bean。
	 */
	public void setAspectBeanName(String aspectBeanName) {
		this.aspectBeanName = aspectBeanName;
	}

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		Assert.notNull(this.aspectBeanName, "'aspectBeanName' is required");
	}


	/**
	 * 从 {@link BeanFactory} 查找方面 bean 并返回它。
	 * @see #setAspectBeanName
	 */
	@Override
	public Object getAspectInstance() {
		Assert.state(this.beanFactory != null, "No BeanFactory set");
		Assert.state(this.aspectBeanName != null, "No 'aspectBeanName' set");
		return this.beanFactory.getBean(this.aspectBeanName);
	}

	/**
	 * 获取 Aspect Class Loader（`AspectClassLoader`）。
	 */
	@Override
	public @Nullable ClassLoader getAspectClassLoader() {
		if (this.beanFactory instanceof ConfigurableBeanFactory cbf) {
			return cbf.getBeanClassLoader();
		}
		else {
			return ClassUtils.getDefaultClassLoader();
		}
	}

	/**
	 * 获取 Order（`Order`）。
	 */
	@Override
	public int getOrder() {
		if (this.beanFactory != null && this.aspectBeanName != null &&
				this.beanFactory.isSingleton(this.aspectBeanName) &&
				this.beanFactory.isTypeMatch(this.aspectBeanName, Ordered.class)) {
			return ((Ordered) this.beanFactory.getBean(this.aspectBeanName)).getOrder();
		}
		return Ordered.LOWEST_PRECEDENCE;
	}

}
