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

package org.springframework.aop.target.dynamic;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

/**
 * 可刷新的 TargetSource，从 BeanFactory 获取新的目标 bean。
 * <p> 可以被子类化以覆盖 {@code requiresRefresh()} 以抑制不必要的刷新。默认情况下，每次“refreshCheckDelay”过去后都会执行刷新。
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see #requiresRefresh()
 * @see #setRefreshCheckDelay
 */
public class BeanFactoryRefreshableTargetSource extends AbstractRefreshableTargetSource {

	/** 底层 BeanFactory 引用。 */
	private final BeanFactory beanFactory;

	/** 名称相关状态（`beanName`）。 */
	private final String beanName;


	/**
	 * 为给定的 bean 工厂和 bean 名称创建一个新的 BeanFactoryRefreshableTargetSource。 <p>注意，传入的 BeanFactory
	 * 应该为给定的 bean 名称设置适当的 bean 定义。
	 * @param beanFactory 从中获取bean的 BeanFactory
	 * @param beanName 目标 bean 的名称
	 */
	public BeanFactoryRefreshableTargetSource(BeanFactory beanFactory, String beanName) {
		Assert.notNull(beanFactory, "BeanFactory is required");
		Assert.notNull(beanName, "Bean name is required");
		this.beanFactory = beanFactory;
		this.beanName = beanName;
	}


	/**
	 * 检索新的目标对象。
	 */
	@Override
	protected final Object freshTarget() {
		return obtainFreshBean(this.beanFactory, this.beanName);
	}

	/**
	 * 子类可以重写的模板方法，以为给定的 bean 工厂和 bean 名称提供新的目标对象。 <p>此默认实现从 bean 工厂获取新的目标 bean 实例。
	 * @see org.springframework.beans.factory.BeanFactory#getBean
	 */
	protected Object obtainFreshBean(BeanFactory beanFactory, String beanName) {
		return beanFactory.getBean(beanName);
	}

}
