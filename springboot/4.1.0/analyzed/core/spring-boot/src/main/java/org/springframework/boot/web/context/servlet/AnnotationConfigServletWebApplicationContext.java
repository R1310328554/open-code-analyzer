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

package org.springframework.boot.web.context.servlet;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * 接受带注解类作为输入的 {@link GenericWebApplicationContext}——
 * 尤其是 {@link Configuration @Configuration} 注解类，也包括普通
 * {@link Component @Component} 类以及使用 {@code javax.inject} 注解的 JSR-330 兼容类。
 * 支持逐个注册类（以类名作为配置位置），也支持类路径扫描（以基础包作为配置位置）。
 * <p>
 * 注意：存在多个 {@code @Configuration} 类时，后加载文件中的 {@code @Bean}
 * 定义会覆盖先加载的定义。可通过额外的 Configuration 类有意覆盖某些 Bean 定义。
 *
 * @author Stephane Nicoll
 * @since 2.2.0
 * @see #register(Class...)
 * @see #scan(String...)
 */
public class AnnotationConfigServletWebApplicationContext extends GenericWebApplicationContext
		implements AnnotationConfigRegistry {

	private final AnnotatedBeanDefinitionReader reader;

	private final ClassPathBeanDefinitionScanner scanner;

	private final Set<Class<?>> annotatedClasses = new LinkedHashSet<>();

	private String @Nullable [] basePackages;

	/**
	 * 创建新的 {@link AnnotationConfigServletWebApplicationContext}，
	 * 需通过 {@link #register} 调用填充后手动 {@linkplain #refresh 刷新}。
	 */
	public AnnotationConfigServletWebApplicationContext() {
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 使用给定 {@code DefaultListableBeanFactory} 创建新的 {@link AnnotationConfigServletWebApplicationContext}。
	 * 需通过 {@link #register} 调用填充后手动 {@linkplain #refresh 刷新}。
	 *
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context 此上下文使用的 DefaultListableBeanFactory 实例
	 */
	public AnnotationConfigServletWebApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 创建新的 {@link AnnotationConfigServletWebApplicationContext}，
	 * 从给定带注解类派生 Bean 定义并自动刷新上下文。
	 *
	 * @param annotatedClasses one or more annotated classes, e.g. {@code @Configuration}
	 * classes 一个或多个带注解类，例如 {@code @Configuration} 类
	 */
	public AnnotationConfigServletWebApplicationContext(Class<?>... annotatedClasses) {
		this();
		register(annotatedClasses);
		refresh();
	}

	/**
	 * 创建新的 {@link AnnotationConfigServletWebApplicationContext}，
	 * 扫描给定包中的 Bean 定义并自动刷新上下文。
	 *
	 * @param basePackages the packages to check for annotated classes 待扫描带注解类的基础包
	 */
	public AnnotationConfigServletWebApplicationContext(String... basePackages) {
		this();
		scan(basePackages);
		refresh();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 将给定环境委托给底层的 {@link AnnotatedBeanDefinitionReader} 与
	 * {@link ClassPathBeanDefinitionScanner} 成员。
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * 为 {@link AnnotatedBeanDefinitionReader} 和/或
	 * {@link ClassPathBeanDefinitionScanner} 提供自定义 {@link BeanNameGenerator}（若有）。
	 * <p>
	 * 默认为 {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}。
	 * <p>
	 * 必须在调用 {@link #register(Class...)} 和/或 {@link #scan(String...)} 之前调用此方法。
	 *
	 * @param beanNameGenerator the bean name generator Bean 名称生成器
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}

	/**
	 * 设置用于检测到的 Bean 类的 {@link ScopeMetadataResolver}。
	 * <p>
	 * 默认为 {@link AnnotationScopeMetadataResolver}。
	 * <p>
	 * 必须在调用 {@link #register(Class...)} 和/或 {@link #scan(String...)} 之前调用此方法。
	 *
	 * @param scopeMetadataResolver the scope metadata resolver 作用域元数据解析器
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}

	/**
	 * 注册一个或多个待处理的带注解类。注意必须调用 {@link #refresh()} 上下文才能完全处理新类。
	 * <p>
	 * 对 {@code #register} 的调用是幂等的；重复添加同一带注解类不会产生额外效果。
	 *
	 * @param annotatedClasses one or more annotated classes, e.g. {@code @Configuration}
	 * classes 一个或多个带注解类，例如 {@code @Configuration} 类
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	@Override
	public final void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "'annotatedClasses' must not be empty");
		this.annotatedClasses.addAll(Arrays.asList(annotatedClasses));
	}

	/**
	 * 在指定基础包内执行扫描。注意必须调用 {@link #refresh()} 上下文才能完全处理新类。
	 *
	 * @param basePackages the packages to check for annotated classes 待扫描带注解类的基础包
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	@Override
	public final void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "'basePackages' must not be empty");
		this.basePackages = basePackages;
	}

	@Override
	protected void prepareRefresh() {
		this.scanner.clearCache();
		super.prepareRefresh();
	}

	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.postProcessBeanFactory(beanFactory);
		if (!ObjectUtils.isEmpty(this.basePackages)) {
			this.scanner.scan(this.basePackages);
		}
		if (!this.annotatedClasses.isEmpty()) {
			this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));
		}
	}

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass, @Nullable Supplier<T> supplier,
			BeanDefinitionCustomizer... customizers) {
		this.reader.registerBean(beanClass, beanName, supplier, customizers);
	}

}
