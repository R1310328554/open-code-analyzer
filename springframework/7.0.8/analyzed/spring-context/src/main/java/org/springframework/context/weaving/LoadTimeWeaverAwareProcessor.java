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

package org.springframework.context.weaving;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} 实现，
 * 将上下文默认 {@link LoadTimeWeaver} 注入实现了 {@link LoadTimeWeaverAware} 的 Bean。
 *
 * <p>{@link org.springframework.context.ApplicationContext ApplicationContext}
 * 在存在默认 {@code LoadTimeWeaver} 时会自动向底层 {@link BeanFactory} 注册本处理器。
 *
 * <p>应用代码不应直接使用本类。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see LoadTimeWeaverAware
 * @see org.springframework.context.ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME
 */
public class LoadTimeWeaverAwareProcessor implements BeanPostProcessor, BeanFactoryAware {

	/** 显式指定的织入器；为 {@code null} 时从 BeanFactory 自动获取。 */
	private @Nullable LoadTimeWeaver loadTimeWeaver;

	/** 用于自动获取 {@code loadTimeWeaver} Bean 的工厂。 */
	private @Nullable BeanFactory beanFactory;


	/**
	 * 创建处理器，自动从所在 {@link BeanFactory} 获取 {@link LoadTimeWeaver}，
	 * 期望 Bean 名为 {@link ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME "loadTimeWeaver"}。
	 */
	public LoadTimeWeaverAwareProcessor() {
	}

	/**
	 * 为给定 {@link LoadTimeWeaver} 创建处理器。
	 * <p>若 {@code loadTimeWeaver} 为 {@code null}，则从所在 {@link BeanFactory} 自动获取，
	 * 期望 Bean 名为 {@link ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME "loadTimeWeaver"}。
	 * @param loadTimeWeaver 要使用的具体 {@code LoadTimeWeaver}
	 */
	public LoadTimeWeaverAwareProcessor(@Nullable LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	/**
	 * 创建处理器，从给定 {@link BeanFactory} 自动获取 {@code LoadTimeWeaver}，
	 * 期望 Bean 名为 {@link ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME "loadTimeWeaver"}。
	 * @param beanFactory 用于获取 LoadTimeWeaver 的 BeanFactory
	 */
	public LoadTimeWeaverAwareProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	/** 在 Bean 初始化前，向 {@link LoadTimeWeaverAware} 实现注入织入器。 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof LoadTimeWeaverAware loadTimeWeaverAware) {
			LoadTimeWeaver ltw = this.loadTimeWeaver;
			if (ltw == null) {
				Assert.state(this.beanFactory != null,
						"BeanFactory required if no LoadTimeWeaver explicitly specified");
				ltw = this.beanFactory.getBean(
						ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME, LoadTimeWeaver.class);
			}
			loadTimeWeaverAware.setLoadTimeWeaver(ltw);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String name) {
		return bean;
	}

}
