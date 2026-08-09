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

package org.springframework.context.annotation;

import java.util.Arrays;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.Assert;

/**
 * 独立的应用上下文，接受<em>组件类</em>作为输入 &mdash;
 * 特别是带 {@link Configuration @Configuration} 注解的类，
 * 也包括普通的 {@link org.springframework.stereotype.Component @Component} 类型
 * 以及使用 {@code jakarta.inject} 注解的 JSR-330 兼容类。
 *
 * <p>支持通过 {@link #register(Class...)} 逐个注册类，
 * 以及通过 {@link #scan(String...)} 进行类路径扫描。
 *
 * <p>若有多个 {@code @Configuration} 类，后定义类中的 {@link Bean @Bean} 方法
 * 将覆盖先定义类中的方法。可利用额外的 {@code @Configuration} 类
 * 有意覆盖某些 Bean 定义。
 *
 * <p>用法示例参见 {@link Configuration @Configuration} 的 javadoc。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see #register
 * @see #scan
 * @see AnnotatedBeanDefinitionReader
 * @see ClassPathBeanDefinitionScanner
 * @see org.springframework.context.support.GenericXmlApplicationContext
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	/** 注解驱动的 Bean 定义读取器。 */
	private final AnnotatedBeanDefinitionReader reader;

	/** 类路径 Bean 定义扫描器。 */
	private final ClassPathBeanDefinitionScanner scanner;


	/**
	 * 创建新的 AnnotationConfigApplicationContext，需通过 {@link #register} 填充，
	 * 然后手动 {@linkplain #refresh refreshed}。
	 */
	public AnnotationConfigApplicationContext() {
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 使用给定 DefaultListableBeanFactory 创建新的 AnnotationConfigApplicationContext。
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 创建新的 AnnotationConfigApplicationContext，从给定组件类派生 Bean 定义，
	 * 并自动刷新上下文。
	 * @param componentClasses one or more component classes &mdash; for example,
	 * {@link Configuration @Configuration} classes
	 */
	public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
		this();
		register(componentClasses);
		refresh();
	}

	/**
	 * 创建新的 AnnotationConfigApplicationContext，扫描给定包中的组件，
	 * 为这些组件注册 Bean 定义，并自动刷新上下文。
	 * @param basePackages the packages to scan for component classes
	 */
	public AnnotationConfigApplicationContext(String... basePackages) {
		this();
		scan(basePackages);
		refresh();
	}


	/**
	 * 将给定自定义 {@code Environment} 传播到底层
	 * {@link AnnotatedBeanDefinitionReader} 和 {@link ClassPathBeanDefinitionScanner}。
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * 为 {@link AnnotatedBeanDefinitionReader} 和/或 {@link ClassPathBeanDefinitionScanner}
	 * 提供自定义 {@link BeanNameGenerator}。
	 * <p>默认为 {@code AnnotationBeanNameGenerator}。
	 * <p>处理 {@link Configuration @Configuration} 类时，
	 * {@link ConfigurationBeanNameGenerator}（如
	 * {@link FullyQualifiedConfigurationBeanNameGenerator}）还决定
	 * 无显式 {@code name} 属性的 {@link Bean @Bean} 方法的默认名称。
	 * <p>必须在调用 {@link #register(Class...)} 和/或 {@link #scan(String...)} 之前调用。
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 * @see AnnotationBeanNameGenerator
	 * @see FullyQualifiedAnnotationBeanNameGenerator
	 * @see FullyQualifiedConfigurationBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}

	/**
	 * 设置用于已注册组件类的 {@link ScopeMetadataResolver}。
	 * <p>默认为 {@link AnnotationScopeMetadataResolver}。
	 * <p>必须在调用 {@link #register(Class...)} 和/或 {@link #scan(String...)} 之前调用。
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}


	//---------------------------------------------------------------------
	// Implementation of AnnotationConfigRegistry
	//---------------------------------------------------------------------

	/**
	 * 注册一个或多个待处理的组件类。
	 * <p>注意，必须调用 {@link #refresh()} 才能使上下文完全处理新类。
	 * @param componentClasses one or more component classes &mdash; for example,
	 * {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	@Override
	public void register(Class<?>... componentClasses) {
		Assert.notEmpty(componentClasses, "At least one component class must be specified");
		StartupStep registerComponentClass = getApplicationStartup().start("spring.context.component-classes.register")
				.tag("classes", () -> Arrays.toString(componentClasses));
		this.reader.register(componentClasses);
		registerComponentClass.end();
	}

	/**
	 * 在指定基础包内执行扫描。
	 * <p>注意，必须调用 {@link #refresh()} 才能使上下文完全处理新类。
	 * @param basePackages the packages to scan for component classes
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	@Override
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		StartupStep scanPackages = getApplicationStartup().start("spring.context.base-packages.scan")
				.tag("packages", () -> Arrays.toString(basePackages));
		this.scanner.scan(basePackages);
		scanPackages.end();
	}


	//---------------------------------------------------------------------
	// Adapt superclass registerBean calls to AnnotatedBeanDefinitionReader
	//---------------------------------------------------------------------

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
			@Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		this.reader.registerBean(beanClass, beanName, supplier, customizers);
	}

}
