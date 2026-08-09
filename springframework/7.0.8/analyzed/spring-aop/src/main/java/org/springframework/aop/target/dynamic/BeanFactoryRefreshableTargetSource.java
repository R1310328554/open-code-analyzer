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
 * 从 BeanFactory 获取新目标 Bean 的可刷新 TargetSource。
 *
 * <p>可子类化并覆盖 {@code requiresRefresh()} 以抑制不必要的刷新。
 * 默认在 "refreshCheckDelay" 间隔过后每次都会刷新。
 *
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

	private final BeanFactory beanFactory;

	private final String beanName;


	/**
	 * 为给定 BeanFactory 与 Bean 名称创建 BeanFactoryRefreshableTargetSource。
	 * <p>注意：传入的 BeanFactory 须已为给定 Bean 名称配置相应 Bean 定义。
	 * @param beanFactory 用于获取 Bean 的 BeanFactory
	 * @param beanName 目标 Bean 名称
	 */
	public BeanFactoryRefreshableTargetSource(BeanFactory beanFactory, String beanName) {
		Assert.notNull(beanFactory, "BeanFactory is required");
		Assert.notNull(beanName, "Bean name is required");
		this.beanFactory = beanFactory;
		this.beanName = beanName;
	}


	/**
	 * 获取新目标对象。
	 */
	@Override
	protected final Object freshTarget() {
		return obtainFreshBean(this.beanFactory, this.beanName);
	}

	/**
	 * 子类可覆盖的模板方法，为给定 BeanFactory 与 Bean 名称提供新目标对象。
	 * <p>默认实现从 BeanFactory 获取新目标 Bean 实例。
	 * @see org.springframework.beans.factory.BeanFactory#getBean
	 */
	protected Object obtainFreshBean(BeanFactory beanFactory, String beanName) {
		return beanFactory.getBean(beanName);
	}

}
