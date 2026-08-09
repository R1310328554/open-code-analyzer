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

package org.springframework.context.support;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * 支持多次调用 {@link #refresh()} 的
 * {@link org.springframework.context.ApplicationContext} 实现基类，
 * 每次刷新都会创建新的内部 Bean 工厂实例。
 * 通常（但不一定）由一组配置位置驱动，从中加载 Bean 定义。
 *
 * <p>子类唯一需要实现的方法是 {@link #loadBeanDefinitions}，
 * 在每次刷新时调用。具体实现应将 Bean 定义加载到给定的
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory} 中，
 * 通常委托给一个或多个特定的 Bean 定义读取器。
 *
 * <p><b>注意：WebApplicationContext 有类似的基类。</b>
 * {@link org.springframework.web.context.support.AbstractRefreshableWebApplicationContext}
 * 提供相同的子类化策略，并额外预实现了 Web 环境的全部上下文功能。
 * 还为 Web 上下文预定义了接收配置位置的方式。
 *
 * <p>读取特定 Bean 定义格式的具体独立子类包括
 * {@link ClassPathXmlApplicationContext} 和 {@link FileSystemXmlApplicationContext}，
 * 二者均派生自公共基类 {@link AbstractXmlApplicationContext}；
 * {@link org.springframework.context.annotation.AnnotationConfigApplicationContext}
 * 支持以 {@code @Configuration} 注解类作为 Bean 定义来源。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.3
 * @see #loadBeanDefinitions
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
 * @see AbstractXmlApplicationContext
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	private @Nullable Boolean allowBeanDefinitionOverriding;

	private @Nullable Boolean allowCircularReferences;

	/** 本上下文的 Bean 工厂。 */
	private volatile @Nullable DefaultListableBeanFactory beanFactory;


	/**
	 * 创建无父上下文的 AbstractRefreshableApplicationContext。
	 */
	public AbstractRefreshableApplicationContext() {
	}

	/**
	 * 使用给定父上下文创建新的 AbstractRefreshableApplicationContext。
	 * @param parent 父上下文
	 */
	public AbstractRefreshableApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * 设置是否允许通过注册同名但不同定义来覆盖 Bean 定义，自动替换前者。
	 * 若不允许，将抛出异常。默认为 {@code true}。
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * 设置是否允许 Bean 之间的循环引用，并自动尝试解析。
	 * <p>默认为 {@code true}。关闭后遇到循环引用将抛出异常，完全禁止循环引用。
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 */
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}


	/**
	 * 本实现会实际刷新上下文底层的 Bean 工厂：关闭先前的 Bean 工厂（若有），
	 * 并为上下文生命周期的下一阶段初始化全新的 Bean 工厂。
	 */
	@Override
	protected final void refreshBeanFactory() throws BeansException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			beanFactory.setSerializationId(getId());
			beanFactory.setApplicationStartup(getApplicationStartup());
			customizeBeanFactory(beanFactory);
			loadBeanDefinitions(beanFactory);
			this.beanFactory = beanFactory;
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

	@Override
	protected void cancelRefresh(Throwable ex) {
		DefaultListableBeanFactory beanFactory = this.beanFactory;
		if (beanFactory != null) {
			beanFactory.setSerializationId(null);
		}
		super.cancelRefresh(ex);
	}

	@Override
	protected final void closeBeanFactory() {
		DefaultListableBeanFactory beanFactory = this.beanFactory;
		if (beanFactory != null) {
			beanFactory.setSerializationId(null);
			this.beanFactory = null;
		}
	}

	/**
	 * 判断本上下文当前是否持有 Bean 工厂，即至少已刷新一次且尚未关闭。
	 */
	protected final boolean hasBeanFactory() {
		return (this.beanFactory != null);
	}

	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		DefaultListableBeanFactory beanFactory = this.beanFactory;
		if (beanFactory == null) {
			throw new IllegalStateException("BeanFactory not initialized or already closed - " +
					"call 'refresh' before accessing beans via the ApplicationContext");
		}
		return beanFactory;
	}

	/**
	 * 覆盖为空操作：AbstractRefreshableApplicationContext 中
	 * {@link #getBeanFactory()} 已对活跃上下文提供强断言。
	 */
	@Override
	protected void assertBeanFactoryActive() {
	}

	/**
	 * 为本上下文创建内部 Bean 工厂。每次 {@link #refresh()} 尝试时调用。
	 * <p>默认实现创建 {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}，
	 * 以本上下文父级的 {@linkplain #getInternalParentBeanFactory() 内部 Bean 工厂} 为父工厂。
	 * 子类可覆盖，例如自定义 DefaultListableBeanFactory 的设置。
	 * @return 本上下文的 Bean 工厂
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowEagerClassLoading
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 */
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}

	/**
	 * 自定义本上下文使用的内部 Bean 工厂。每次 {@link #refresh()} 尝试时调用。
	 * <p>默认实现应用本上下文的
	 * {@linkplain #setAllowBeanDefinitionOverriding "allowBeanDefinitionOverriding"}
	 * 和 {@linkplain #setAllowCircularReferences "allowCircularReferences"} 设置（若已指定）。
	 * 子类可覆盖以自定义 {@link DefaultListableBeanFactory} 的任意设置。
	 * @param beanFactory 为本上下文新创建的 Bean 工厂
	 * @see DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see DefaultListableBeanFactory#setAllowCircularReferences
	 * @see DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 * @see DefaultListableBeanFactory#setAllowEagerClassLoading
	 */
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		if (this.allowBeanDefinitionOverriding != null) {
			beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		if (this.allowCircularReferences != null) {
			beanFactory.setAllowCircularReferences(this.allowCircularReferences);
		}
	}

	/**
	 * 将 Bean 定义加载到给定 Bean 工厂，通常委托给一个或多个 Bean 定义读取器。
	 * @param beanFactory 要加载 Bean 定义的 Bean 工厂
	 * @throws BeansException 若解析 Bean 定义失败
	 * @throws IOException 若加载 Bean 定义文件失败
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
			throws BeansException, IOException;

}
