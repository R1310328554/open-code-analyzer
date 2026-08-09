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
 * 从 BeanFactory 检索标准 Spring Advisor 的辅助类，
 * 供自动代理使用。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AbstractAdvisorAutoProxyCreator
 */
public class BeanFactoryAdvisorRetrievalHelper {

	private static final Log logger = LogFactory.getLog(BeanFactoryAdvisorRetrievalHelper.class);

	private final ConfigurableListableBeanFactory beanFactory;

	private volatile String @Nullable [] cachedAdvisorBeanNames;


	/**
	 * 为给定 BeanFactory 创建新的 BeanFactoryAdvisorRetrievalHelper。
	 * @param beanFactory 要扫描的 ListableBeanFactory
	 */
	public BeanFactoryAdvisorRetrievalHelper(ConfigurableListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		this.beanFactory = beanFactory;
	}


	/**
	 * 查找当前 BeanFactory 中所有合格的 Advisor Bean，
	 * 忽略 FactoryBean 并排除正在创建中的 Bean。
	 * @return {@link org.springframework.aop.Advisor} Bean 列表
	 * @see #isEligibleBean
	 */
	public List<Advisor> findAdvisorBeans() {
		// 若尚未缓存，则确定 Advisor Bean 名称列表。
		String[] advisorNames = this.cachedAdvisorBeanNames;
		if (advisorNames == null) {
			// 此处不初始化 FactoryBean：需保持所有普通 Bean 未初始化，
			// 以便自动代理创建器对其生效！
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
								// 忽略：表示回引到正试图被增强的 Bean。
								// 我们要找的是除当前正在创建的 Bean 自身以外的 Advisor。
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
	 * 判断给定名称的切面 Bean 是否合格。
	 * <p>默认实现始终返回 {@code true}。
	 * @param beanName 切面 Bean 名称
	 * @return 该 Bean 是否合格
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
