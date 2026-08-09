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

import java.io.Serializable;

import jakarta.inject.Provider;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.beans.factory.FactoryBean} 实现，返回 JSR-330
 * {@link jakarta.inject.Provider}，其 {@code get()} 从
 * {@link org.springframework.beans.factory.BeanFactory} 获取目标 bean。
 *
 * <p>本质上是 Spring 传统 {@link ObjectFactoryCreatingFactoryBean} 的 JSR-330 兼容变体。
 * 可用于面向 {@code jakarta.inject.Provider} 类型属性或构造参数的
 * 传统外部依赖注入配置，作为 JSR-330 {@code @Inject} 注解驱动方式的替代。
 *
 * @author Juergen Hoeller
 * @since 3.0.2
 * @see jakarta.inject.Provider
 * @see ObjectFactoryCreatingFactoryBean
 */
public class ProviderCreatingFactoryBean extends AbstractFactoryBean<Provider<Object>> {

	/** 目标 bean 的名称。 */
	private @Nullable String targetBeanName;


	/**
	 * 设置目标 bean 的名称。
	 * <p>目标 bean 不必是非单例，但实际使用中几乎总是非单例（若目标为单例，
	 * 可直接注入依赖对象，无需本工厂提供的额外间接层）。
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(this.targetBeanName, "Property 'targetBeanName' is required");
		super.afterPropertiesSet();
	}


	@Override
	public Class<?> getObjectType() {
		return Provider.class;
	}

	@Override
	protected Provider<Object> createInstance() {
		BeanFactory beanFactory = getBeanFactory();
		Assert.state(beanFactory != null, "No BeanFactory available");
		Assert.state(this.targetBeanName != null, "No target bean name specified");
		return new TargetBeanProvider(beanFactory, this.targetBeanName);
	}


	/**
	 * 独立内部类，用于序列化。
	 */
	@SuppressWarnings("serial")
	private static class TargetBeanProvider implements Provider<Object>, Serializable {

		private final BeanFactory beanFactory;

		private final String targetBeanName;

		public TargetBeanProvider(BeanFactory beanFactory, String targetBeanName) {
			this.beanFactory = beanFactory;
			this.targetBeanName = targetBeanName;
		}

		@Override
		public Object get() throws BeansException {
			return this.beanFactory.getBean(this.targetBeanName);
		}
	}

}
