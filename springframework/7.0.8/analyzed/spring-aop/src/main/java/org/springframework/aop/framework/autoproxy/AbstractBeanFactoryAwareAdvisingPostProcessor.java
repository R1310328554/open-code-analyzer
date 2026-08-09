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

package org.springframework.aop.framework.autoproxy;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * {@link AbstractAdvisingBeanPostProcessor} 的扩展实现了 {@link BeanFactoryAware}，为每个代理 bean
 * ({@link AutoProxyUtils#ORIGINAL_TARGET_CLASS_ATTRIBUTE}) 添加了原始目标类的公开，并参与任何给定 bean
 * ({@link AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE}) 的外部强制目标类模式。因此，该后处理器与 {@link
 * AbstractAutoProxyCreator} 保持一致。
 * @author Juergen Hoeller
 * @since 4.2.3
 * @see AutoProxyUtils#shouldProxyTargetClass
 * @see AutoProxyUtils#determineTargetClass
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanFactoryAwareAdvisingPostProcessor extends AbstractAdvisingBeanPostProcessor
		implements BeanFactoryAware {

	/** 底层 BeanFactory 引用。 */
	protected @Nullable ConfigurableListableBeanFactory beanFactory;


	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = (beanFactory instanceof ConfigurableListableBeanFactory clbf ? clbf : null);
		AutoProxyUtils.applyDefaultProxyConfig(this, beanFactory);
	}

	/**
	 * 准备：Proxy Factory（方法 `prepareProxyFactory`）。
	 */
	@Override
	protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
		if (this.beanFactory != null) {
			AutoProxyUtils.exposeTargetClass(this.beanFactory, beanName, bean.getClass());
		}

		ProxyFactory proxyFactory = super.prepareProxyFactory(bean, beanName);
		if (this.beanFactory != null) {
			if (AutoProxyUtils.shouldProxyTargetClass(this.beanFactory, beanName)) {
				proxyFactory.setProxyTargetClass(true);
			}
			else {
				Class<?>[] ifcs = AutoProxyUtils.determineExposedInterfaces(this.beanFactory, beanName);
				if (ifcs != null) {
					proxyFactory.setProxyTargetClass(false);
					for (Class<?> ifc : ifcs) {
						proxyFactory.addInterface(ifc);
					}
				}
			}
		}
		return proxyFactory;
	}

	/**
	 * 判断是否 Eligible。
	 */
	@Override
	protected boolean isEligible(Object bean, String beanName) {
		return (!AutoProxyUtils.isOriginalInstance(beanName, bean.getClass()) &&
				super.isEligible(bean, beanName));
	}

}
