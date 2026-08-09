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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 接受注解类作为输入的 {@link ConfigurableReactiveWebApplicationContext} 实现。
 * 支持 {@link Configuration @Configuration}、{@link Component @Component}
 * 以及 JSR-330 {@code javax.inject} 注解类；可逐个注册类或按基包扫描。
 * <p>
 * 注意：多个 {@code @Configuration} 类时，后加载的 {@code @Bean} 定义会覆盖先加载的，
 * 可通过额外 Configuration 类 deliberately 覆盖 Bean 定义。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 2.0.0
 * @see AnnotationConfigApplicationContext
 */
public class AnnotationConfigReactiveWebApplicationContext extends AnnotationConfigApplicationContext
		implements ConfigurableReactiveWebApplicationContext {

	/**
	 * 创建新的 AnnotationConfigReactiveWebApplicationContext，
	 * 需通过 {@link #register} 注册后手动 {@linkplain #refresh refreshed}。
	 */
	public AnnotationConfigReactiveWebApplicationContext() {
	}

	/**
	 * 使用给定 DefaultListableBeanFactory 创建新的 AnnotationConfigApplicationContext。
	 *
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context 本上下文使用的 BeanFactory
	 * @since 2.2.0
	 */
	public AnnotationConfigReactiveWebApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	/**
	 * 根据给定注解类推导 Bean 定义并自动刷新的 AnnotationConfigApplicationContext。
	 *
	 * @param annotatedClasses one or more annotated classes, e.g.
	 * {@link Configuration @Configuration} classes 一个或多个注解类
	 * @since 2.2.0
	 */
	public AnnotationConfigReactiveWebApplicationContext(Class<?>... annotatedClasses) {
		super(annotatedClasses);
	}

	/**
	 * 扫描给定包中的 Bean 定义并自动刷新的 AnnotationConfigApplicationContext。
	 *
	 * @param basePackages the packages to check for annotated classes 要扫描的包
	 * @since 2.2.0
	 */
	public AnnotationConfigReactiveWebApplicationContext(String... basePackages) {
		super(basePackages);
	}

	@Override
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardReactiveWebEnvironment();
	}

	@Override
	protected Resource getResourceByPath(String path) {
		// 须避免暴露类路径资源
		return new FilteredReactiveWebContextResource(path);
	}

}
