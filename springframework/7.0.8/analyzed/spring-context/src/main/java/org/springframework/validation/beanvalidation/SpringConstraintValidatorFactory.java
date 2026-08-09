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

package org.springframework.validation.beanvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

/**
 * JSR-303 {@link ConstraintValidatorFactory} 实现，
 * 委托 Spring BeanFactory 创建可自动装配的 {@link ConstraintValidator} 实例。
 *
 * <p>注意，本类用于编程式使用，而非标准 {@code validation.xml} 文件中的声明式使用。
 * 在 Web 应用（如 JAX-RS 或 JAX-WS）中声明式使用请考虑
 * {@link org.springframework.web.bind.support.SpringWebConstraintValidatorFactory}。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean(Class)
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
public class SpringConstraintValidatorFactory implements ConstraintValidatorFactory {

	private final AutowireCapableBeanFactory beanFactory;

	private final @Nullable ConstraintValidatorFactory defaultConstraintValidatorFactory;


	/**
	 * 为给定 BeanFactory 创建新的 SpringConstraintValidatorFactory。
	 * @param beanFactory 目标 BeanFactory
	 */
	public SpringConstraintValidatorFactory(AutowireCapableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
		this.defaultConstraintValidatorFactory = null;
	}

	/**
	 * 为给定 BeanFactory 创建新的 SpringConstraintValidatorFactory。
	 * @param beanFactory 目标 BeanFactory
	 * @param defaultConstraintValidatorFactory 校验提供者暴露的默认 ConstraintValidatorFactory
	 *（用于创建模块路径设置中可能无法公开访问的提供者内部校验器实现）
	 * @since 7.0.3
	 */
	public SpringConstraintValidatorFactory(
			AutowireCapableBeanFactory beanFactory, ConstraintValidatorFactory defaultConstraintValidatorFactory) {

		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
		this.defaultConstraintValidatorFactory = defaultConstraintValidatorFactory;
	}


	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		if (this.defaultConstraintValidatorFactory != null) {
			// Create provider-internal validator implementations through default ConstraintValidatorFactory.
			String providerModuleName = this.defaultConstraintValidatorFactory.getClass().getModule().getName();
			if (providerModuleName != null && providerModuleName.equals(key.getModule().getName())) {
				return this.defaultConstraintValidatorFactory.getInstance(key);
			}
		}
		return this.beanFactory.createBean(key);
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		this.beanFactory.destroyBean(instance);
	}

}
