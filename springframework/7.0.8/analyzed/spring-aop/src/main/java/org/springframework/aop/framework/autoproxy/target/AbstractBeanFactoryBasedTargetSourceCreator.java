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
 * 需要创建原型 bean 的多个实例的 {@link
 * org.springframework.aop.framework.autoproxy.TargetSourceCreator} 实现的方便超类。
 * <p>U使用内部BeanFactory来管理目标实例，将原始bean定义复制到这个内部工厂。这是必要的，因为原始 BeanFactory 将只包含通过自动代理创建的代理实例。
 * <p> 需要在 {@link org.springframework.beans.factory.support.AbstractBeanFactory} 中运行。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public abstract class AbstractBeanFactoryBasedTargetSourceCreator
		implements TargetSourceCreator, BeanFactoryAware, DisposableBean {

	/**
	 * 获取 Log（`Log`）。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 底层 BeanFactory 引用。 */
	private @Nullable ConfigurableBeanFactory beanFactory;

	/**
	 */
	private final Map<String, DefaultListableBeanFactory> internalBeanFactories = new HashMap<>();


	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public final void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableBeanFactory clbf)) {
			throw new IllegalStateException("Cannot do auto-TargetSource creation with a BeanFactory " +
					"that doesn't implement ConfigurableBeanFactory: " + beanFactory.getClass());
		}
		this.beanFactory = clbf;
	}

	/**
	 * 返回此 TargetSourceCreators 在其中运行的 BeanFactory。
	 */
	protected final @Nullable BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * 获取 Configurable Bean Factory（`ConfigurableBeanFactory`）。
	 */
	private ConfigurableBeanFactory getConfigurableBeanFactory() {
		Assert.state(this.beanFactory != null, "BeanFactory not set");
		return this.beanFactory;
	}


	//---------------------------------------------------------------------
	// TargetSourceCreator 接口的实现
	//---------------------------------------------------------------------

	/**
	 * 获取 Target Source（`TargetSource`）。
	 */
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

		// 我们只需要重写这个 bean 定义，因为它可能引用其他 bean
		// 我们很乐意接受父母对这些的定义。
		// 如果需要，请始终使用原型范围。
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
	 * 返回用于指定 bean 的内部 BeanFactory。
	 * @param beanName 目标 bean 的名称
	 * @return 要使用的内部 BeanFactory
	 */
	protected DefaultListableBeanFactory getInternalBeanFactoryForBean(String beanName) {
		synchronized (this.internalBeanFactories) {
			return this.internalBeanFactories.computeIfAbsent(beanName,
					name -> buildInternalBeanFactory(getConfigurableBeanFactory()));
		}
	}

	/**
	 * 构建一个内部 BeanFactory 来解析目标 bean。
	 * @param containingFactory 最初定义 bean 的包含 BeanFactory
	 * @return 独立的内部 BeanFactory 来保存一些目标 bean 的副本
	 */
	protected DefaultListableBeanFactory buildInternalBeanFactory(ConfigurableBeanFactory containingFactory) {
		// 设置父级以便正确解析引用（向上容器层次结构）。
		DefaultListableBeanFactory internalBeanFactory = new DefaultListableBeanFactory(containingFactory);

		// 必需的，以便所有 BeanPostProcessor、作用域等变得可用。
		internalBeanFactory.copyConfigurationFrom(containingFactory);

		// 过滤掉属于 AOP 基础设施一部分的 BeanPostProcessor，
		// 因为这些仅适用于原始工厂中定义的 bean。
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
	// 由子类实现的模板方法
	//---------------------------------------------------------------------

	/**
	 * 返回此 TargetSourceCreator 是否基于原型。目标 bean 定义的范围将相应设置。 <p>默认为“true”。
	 * @see org.springframework.beans.factory.config.BeanDefinition#isSingleton()
	 */
	protected boolean isPrototypeBased() {
		return true;
	}

	/**
	 * 如果子类希望为此 bean 创建自定义 TargetSource，则必须实现此方法以返回新的
	 * AbstractPrototypeBasedTargetSource；如果子类不感兴趣，则必须实现此方法以返回 {@code
	 * null}，在这种情况下，不会创建特殊的目标源。子类不应在 AbstractPrototypeBasedTargetSource 上调用 {@code
	 * setTargetBeanName} 或 {@code setBeanFactory}：此类的 {@code getTargetSource()} 实现将执行此操作。
	 * @param beanClass 要为其创建 TargetSource 的 bean 类
	 * @param beanName 豆子的名字
	 * @return AbstractPrototypeBasedTargetSource，或 {@code null}（如果我们不匹配）
	 */
	protected abstract @Nullable AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(
			Class<?> beanClass, String beanName);

}
