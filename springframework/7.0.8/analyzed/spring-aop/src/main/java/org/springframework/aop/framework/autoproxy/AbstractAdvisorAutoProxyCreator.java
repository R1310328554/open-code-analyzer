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

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;

/**
 * 通用自动代理创建器：根据检测到的 Advisor
 * 为特定 Bean 构建 AOP 代理。
 *
 * <p>子类可覆盖 {@link #findCandidateAdvisors()} 方法，
 * 返回适用于任意对象的自定义 Advisor 列表。
 * 子类也可覆盖继承的 {@link #shouldSkip} 方法，
 * 将特定对象排除在自动代理之外。
 *
 * <p>需要排序的 Advisor 或 Advice 应标注
 * {@link org.springframework.core.annotation.Order @Order} 或实现
 * {@link org.springframework.core.Ordered} 接口。本类使用
 * {@link AnnotationAwareOrderComparator} 排序 Advisor。
 * 未标注 {@code @Order} 或未实现 {@code Ordered} 接口的 Advisor
 * 视为无序，将以未定义顺序出现在 Advisor 链末尾。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	private @Nullable BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
			throw new IllegalArgumentException(
					"AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		initBeanFactory(clbf);
	}

	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}


	@Override
	protected Object @Nullable [] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {

		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * 查找适用于自动代理本类的所有合格 Advisor。
	 * @param beanClass 要查找 Advisor 的类
	 * @param beanName 当前被代理 Bean 的名称
	 * @return 若无切入点或拦截器则返回空 List（非 {@code null}）
	 * @see #findCandidateAdvisors
	 * @see #sortAdvisors
	 * @see #extendAdvisors
	 */
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		extendAdvisors(eligibleAdvisors);
		if (!eligibleAdvisors.isEmpty()) {
			try {
				eligibleAdvisors = sortAdvisors(eligibleAdvisors);
			}
			catch (BeanCreationException ex) {
				throw new AopConfigException("Advisor sorting failed with unexpected bean creation, probably due " +
						"to custom use of the Ordered interface. Consider using the @Order annotation instead.", ex);
			}
		}
		return eligibleAdvisors;
	}

	/**
	 * 查找用于自动代理的所有候选 Advisor。
	 * @return 候选 Advisor 列表
	 */
	protected List<Advisor> findCandidateAdvisors() {
		Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * 在候选 Advisor 中搜索所有可应用于指定 Bean 的 Advisor。
	 * @param candidateAdvisors 候选 Advisor
	 * @param beanClass 目标 Bean 类
	 * @param beanName 目标 Bean 名称
	 * @return 适用的 Advisor 列表
	 * @see ProxyCreationContext#getCurrentProxiedBeanName()
	 */
	protected List<Advisor> findAdvisorsThatCanApply(
			List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * 返回给定名称的 Advisor Bean 是否具备代理资格。
	 * @param beanName Advisor Bean 名称
	 * @return 该 Bean 是否合格
	 */
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * 按排序规则对 Advisor 排序。子类可覆盖以自定义排序策略。
	 * @param advisors 源 Advisor 列表
	 * @return 排序后的 Advisor 列表
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.annotation.Order
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * 扩展钩子：子类可覆盖以注册额外 Advisor，
	 * 基于目前已排序的 Advisor。
	 * <p>默认实现为空。
	 * <p>通常用于添加暴露后续 Advisor 所需上下文信息的 Advisor。
	 * @param candidateAdvisors 已识别为适用于给定 Bean 的 Advisor
	 */
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	 * 本自动代理创建器始终返回预过滤的 Advisor。
	 */
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * BeanFactoryAdvisorRetrievalHelper 的子类，
	 * 委托给外围 AbstractAdvisorAutoProxyCreator 设施。
	 */
	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
		}
	}

}
