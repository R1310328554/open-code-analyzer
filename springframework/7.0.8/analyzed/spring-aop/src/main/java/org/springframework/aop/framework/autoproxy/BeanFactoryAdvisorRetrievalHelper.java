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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

/**
 * 用于从 BeanFactory 检索标准 Spring Advisor 的帮助程序，用于自动代理。
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AbstractAdvisorAutoProxyCreator
 */
public class BeanFactoryAdvisorRetrievalHelper {

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(BeanFactoryAdvisorRetrievalHelper.class);

	/** 底层 BeanFactory 引用。 */
	private final ConfigurableListableBeanFactory beanFactory;

	/** 名称相关状态（`cachedAdvisorBeanNames`）。 */
	private volatile String @Nullable [] cachedAdvisorBeanNames;


	/**
	 * Create a new BeanFactoryAdvisorRetrievalHelper for the given BeanFactory.
	 * @param beanFactory 要扫描的 ListableBeanFactory
	 */
	public BeanFactoryAdvisorRetrievalHelper(ConfigurableListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		this.beanFactory = beanFactory;
	}


	/**
	 * 查找当前 bean 工厂中所有符合条件的 Advisor bean，忽略 FactoryBeans 并排除当前正在创建的 bean。
	 * @return {@link org.springframework.aop.Advisor} bean 列表
	 * @see #isEligibleBean
	 */
	public List<Advisor> findAdvisorBeans() {
		// 确定 Advisor bean 名称列表（如果尚未缓存）。
		String[] advisorNames = this.cachedAdvisorBeanNames;
		if (advisorNames == null) {
			// 不要在这里初始化 FactoryBeans：我们需要保留所有常规 bean
			// 未初始化，让自动代理创建者应用到它们！
			advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
					this.beanFactory, Advisor.class, true, false);
			this.cachedAdvisorBeanNames = advisorNames;
		}
		if (advisorNames.length == 0) {
			return new ArrayList<>();
		}

		List<Advisor> advisors = new ArrayList<>();
		for (String name : advisorNames) {
			if (isEligibleBean(name)) {
				if (this.beanFactory.isCurrentlyInCreation(name)) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skipping currently created advisor '" + name + "'");
					}
				}
				else {
					try {
						advisors.add(this.beanFactory.getBean(name, Advisor.class));
					}
					catch (BeanCreationException ex) {
						Throwable rootCause = ex.getMostSpecificCause();
						if (rootCause instanceof BeanCurrentlyInCreationException bce) {
							String bceBeanName = bce.getBeanName();
							if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
								if (logger.isTraceEnabled()) {
									logger.trace("Skipping advisor '" + name +
											"' with dependency on currently created bean: " + ex.getMessage());
								}
								// 忽略：指示返回到我们试图建议的 bean 的引用。
								// 我们想要找到除当前创建的 bean 本身之外的顾问程序。
								continue;
							}
						}
						throw ex;
					}
				}
			}
		}
		return advisors;
	}

	/**
	 * 确定具有给定名称的方面 bean 是否合格。 <p>默认实现始终返回{@code true}。
	 * @param beanName 方面 bean 的名称
	 * @return 该豆子符合条件
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
