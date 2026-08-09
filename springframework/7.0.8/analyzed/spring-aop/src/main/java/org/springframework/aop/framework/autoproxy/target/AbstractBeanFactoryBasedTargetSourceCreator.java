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

package org.springframework.aop.framework.autoproxy.target;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.util.Assert;

/**
 * 需要为原型 Bean 创建多个实例的
 * {@link org.springframework.aop.framework.autoproxy.TargetSourceCreator}
 * 实现的便捷超类。
 *
 * <p>使用内部 BeanFactory 管理目标实例，
 * 将原始 Bean 定义复制到该内部工厂。
 * 这是必要的，因为原始 BeanFactory 仅包含通过自动代理创建的代理实例。
 *
 * <p>需在 {@link org.springframework.beans.factory.support.AbstractBeanFactory} 中运行。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public abstract class AbstractBeanFactoryBasedTargetSourceCreator
		implements TargetSourceCreator, BeanFactoryAware, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable ConfigurableBeanFactory beanFactory;

	/** 内部使用的 DefaultListableBeanFactory 实例，按 Bean 名称索引。 */
	private final Map<String, DefaultListableBeanFactory> internalBeanFactories = new HashMap<>();


	@Override
	public final void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableBeanFactory clbf)) {
			throw new IllegalStateException("Cannot do auto-TargetSource creation with a BeanFactory " +
					"that doesn't implement ConfigurableBeanFactory: " + beanFactory.getClass());
		}
		this.beanFactory = clbf;
	}

	/**
	 * 返回本 TargetSourceCreator 运行的 BeanFactory。
	 */
	protected final @Nullable BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	private ConfigurableBeanFactory getConfigurableBeanFactory() {
		Assert.state(this.beanFactory != null, "BeanFactory not set");
		return this.beanFactory;
	}


	//---------------------------------------------------------------------
	// TargetSourceCreator 接口实现
	//---------------------------------------------------------------------

	@Override
	public final @Nullable TargetSource getTargetSource(Class<?> beanClass, String beanName) {
		AbstractBeanFactoryBasedTargetSource targetSource =
				createBeanFactoryBasedTargetSource(beanClass, beanName);
		if (targetSource == null) {
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Configuring AbstractBeanFactoryBasedTargetSource: " + targetSource);
		}

		DefaultListableBeanFactory internalBeanFactory = getInternalBeanFactoryForBean(beanName);

		// 仅需覆盖此 Bean 定义，因其可能引用其他 Bean，
		// 而那些 Bean 我们乐于采用父级定义。
		// 若要求则始终使用 prototype 作用域。
		BeanDefinition bd = getConfigurableBeanFactory().getMergedBeanDefinition(beanName);
		GenericBeanDefinition bdCopy = new GenericBeanDefinition(bd);
		if (isPrototypeBased()) {
			bdCopy.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		}
		internalBeanFactory.registerBeanDefinition(beanName, bdCopy);

		// 完成 PrototypeTargetSource 的配置。
		targetSource.setTargetBeanName(beanName);
		targetSource.setBeanFactory(internalBeanFactory);

		return targetSource;
	}

	/**
	 * 返回用于指定 Bean 的内部 BeanFactory。
	 * @param beanName 目标 Bean 名称
	 * @return 要使用的内部 BeanFactory
	 */
	protected DefaultListableBeanFactory getInternalBeanFactoryForBean(String beanName) {
		synchronized (this.internalBeanFactories) {
			return this.internalBeanFactories.computeIfAbsent(beanName,
					name -> buildInternalBeanFactory(getConfigurableBeanFactory()));
		}
	}

	/**
	 * 构建用于解析目标 Bean 的内部 BeanFactory。
	 * @param containingFactory 最初定义 Bean 的包含 BeanFactory
	 * @return 用于持有部分目标 Bean 副本的独立内部 BeanFactory
	 */
	protected DefaultListableBeanFactory buildInternalBeanFactory(ConfigurableBeanFactory containingFactory) {
		// 设置父级以便正确解析（向上容器层次）的引用。
		DefaultListableBeanFactory internalBeanFactory = new DefaultListableBeanFactory(containingFactory);

		// 以便所有 BeanPostProcessor、Scope 等可用。
		internalBeanFactory.copyConfigurationFrom(containingFactory);

		// 过滤属于 AOP 基础设施的 BeanPostProcessor，
		// 因为它们仅应应用于原始工厂中定义的 Bean。
		internalBeanFactory.getBeanPostProcessors().removeIf(AopInfrastructureBean.class::isInstance);

		return internalBeanFactory;
	}

	/**
	 * 在 TargetSourceCreator 关闭时销毁内部 BeanFactory。
	 * @see #getInternalBeanFactoryForBean
	 */
	@Override
	public void destroy() {
		synchronized (this.internalBeanFactories) {
			for (DefaultListableBeanFactory bf : this.internalBeanFactories.values()) {
				bf.destroySingletons();
			}
		}
	}


	//---------------------------------------------------------------------
	// 子类需实现的模板方法
	//---------------------------------------------------------------------

	/**
	 * 返回本 TargetSourceCreator 是否基于原型。
	 * 目标 Bean 定义的作用域将相应设置。
	 * <p>默认为 "true"。
	 * @see org.springframework.beans.factory.config.BeanDefinition#isSingleton()
	 */
	protected boolean isPrototypeBased() {
		return true;
	}

	/**
	 * 子类必须实现本方法：若要为该 Bean 创建自定义 TargetSource，
	 * 则返回新的 AbstractPrototypeBasedTargetSource；若无兴趣则 {@code null}，
	 * 此时不会创建特殊 TargetSource。
	 * 子类不应在 AbstractPrototypeBasedTargetSource 上调用
	 * {@code setTargetBeanName} 或 {@code setBeanFactory}：
	 * 本类的 {@code getTargetSource()} 实现会处理。
	 * @param beanClass 要创建 TargetSource 的 Bean 类
	 * @param beanName Bean 名称
	 * @return AbstractPrototypeBasedTargetSource，若不匹配则 {@code null}
	 */
	protected abstract @Nullable AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(
			Class<?> beanClass, String beanName);

}
