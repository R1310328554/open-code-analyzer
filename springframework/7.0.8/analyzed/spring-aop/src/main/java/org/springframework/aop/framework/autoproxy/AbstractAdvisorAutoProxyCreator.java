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
 * 通用自动代理创建器，根据检测到的每个 bean 的 Advisor 为特定 bean 构建 AOP 代理。
 * <p>子类可以重写 {@link #findCandidateAdvisors()} 方法以返回适用于任何对象的自定义顾问列表。子类还可以重写继承的 {@link #shoul
 * dSkip} 方法，以从自动代理中排除某些对象。
 * <p> 需要订购的顾问或建议应使用 {@link org.springframework.core.annotation.Order @Order} 进行注释或实现
 * {@link org.springframework.core.Ordered} 接口。此类使用 {@link AnnotationAwareOrderComparator}
 * 对顾问进行排序。未使用 {@code @Order} 注解或未实现 {@code Ordered} 接口的 Advisor
 * 将被视为无序；它们将以未定义的顺序出现在顾问链的末尾。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	/** 通知器相关状态（`advisorRetrievalHelper`）。 */
	private @Nullable BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;


	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
			throw new IllegalArgumentException(
					"AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		initBeanFactory(clbf);
	}

	/**
	 * 方法 `initBeanFactory`：完成本类中与「init Bean Factory」相关的职责。
	 */
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}


	/**
	 * 获取 Advices And Advisors For Bean（`AdvicesAndAdvisorsForBean`）。
	 */
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
	 * 查找所有符合自动代理此类的顾问。
	 * @param beanClass 寻找顾问的克拉兹
	 * @param beanName 当前代理 bean 的名称
	 * @return 如果没有切入点或拦截器，则为空列表，而不是 {@code null}
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
	 * 查找所有候选顾问以在自动代理中使用。
	 * @return 候选顾问名单
	 */
	protected List<Advisor> findCandidateAdvisors() {
		Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * 搜索给定的候选 Advisor 以查找可应用于指定 bean 的所有 Advisor。
	 * @param candidateAdvisors 候选人顾问
	 * @param beanClass 目标的 Bean 类
	 * @param beanName 目标的 bean 名称
	 * @return 适用顾问名单
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
	 * 首先返回具有给定名称的 Advisor bean 是否有资格进行代理。
	 * @param beanName Advisor bean 的名称
	 * @return 该豆子符合条件
	 */
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * 根据顺序对顾问进行排序。子类可以选择重写此方法来自定义排序策略。
	 * @param advisors 顾问来源名单
	 * @return 排序后的顾问名单
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.annotation.Order
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * 考虑到迄今为止获得的排序顾问，子类可以覆盖该扩展钩子以注册其他顾问。 <p>默认实现为空。 <p>通常用于添加顾问，以公开某些后续顾问所需的上下文信息。
	 * @param candidateAdvisors 已被识别为适用于给定 bean 的 Advisor
	 */
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	 * 此自动代理创建器始终返回预先过滤的顾问。
	 */
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * BeanFactoryAdvisorRetrievalHelper 的子类，委托给周围的 AbstractAdvisorAutoProxyCreator 设施。
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
