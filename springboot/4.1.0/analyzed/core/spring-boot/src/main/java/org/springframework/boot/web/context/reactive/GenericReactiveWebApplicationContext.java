/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.web.context.reactive;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;

/**
 * 适用于响应式 Web 环境的 {@link GenericApplicationContext} 子类。
 *
 * @author Stephane Nicoll
 * @author Brian Clozel
 * @since 2.0.0
 */
public class GenericReactiveWebApplicationContext extends GenericApplicationContext
		implements ConfigurableReactiveWebApplicationContext {

	/**
	 * 创建新的 {@link GenericReactiveWebApplicationContext}。
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericReactiveWebApplicationContext() {
	}

	/**
	 * 使用给定 DefaultListableBeanFactory 创建新的 {@link GenericReactiveWebApplicationContext}。
	 *
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context 此上下文使用的 DefaultListableBeanFactory 实例
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericReactiveWebApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	@Override
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardReactiveWebEnvironment();
	}

	@Override
	protected Resource getResourceByPath(String path) {
		// 必须小心避免暴露类路径资源
		return new FilteredReactiveWebContextResource(path);
	}

}
