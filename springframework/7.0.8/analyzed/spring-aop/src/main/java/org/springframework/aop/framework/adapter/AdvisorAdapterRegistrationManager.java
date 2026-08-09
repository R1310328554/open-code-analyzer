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

package org.springframework.aop.framework.adapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * BeanPostProcessor，使用 {@link AdvisorAdapterRegistry}（默认为 {@link GlobalAdvisorAdapterRegis
 * try}）在 BeanFactory 中注册 {@link AdvisorAdapter} bean。
 * <p> 工作的唯一要求是它需要与需要被 Spring 的 AOP 框架“识别”的“非本机”Spring AdvisorAdapter 一起在应用程序上下文中定义。
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 27.02.2004
 * @see #setAdvisorAdapterRegistry
 * @see AdvisorAdapter
 */
public class AdvisorAdapterRegistrationManager implements BeanPostProcessor {

	/**
	 * 获取 Instance（`Instance`）。
	 */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();


	/**
	 * 指定 AdvisorAdapterRegistry 来注册 AdvisorAdapter bean。默认是全局 AdvisorAdapterRegistry。
	 * @see GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}


	/**
	 * 方法 `postProcessBeforeInitialization`：完成本类中与「post Process Before Initialization」相关的职责。
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * 方法 `postProcessAfterInitialization`：完成本类中与「post Process After Initialization」相关的职责。
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AdvisorAdapter advisorAdapter) {
			this.advisorAdapterRegistry.registerAdvisorAdapter(advisorAdapter);
		}
		return bean;
	}

}
