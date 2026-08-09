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

package org.springframework.aop.aspectj.autoproxy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.PartialOrder.PartialComparable;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.aspectj.ShadowMatchUtils;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}
 * 子类，公开 AspectJ 的调用上下文，并在多个建议来自同一方面时理解 AspectJ 的建议优先级规则。
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJAwareAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator
		implements SmartInitializingSingleton, DisposableBean {

	/**
	 * 方法 `AspectJPrecedenceComparator`：完成本类中与「Aspect J Precedence Comparator」相关的职责。
	 */
	private static final Comparator<Advisor> DEFAULT_PRECEDENCE_COMPARATOR = new AspectJPrecedenceComparator();


	/**
	 * 根据 AspectJ 优先级对提供的 {@link Advisor} 实例进行排序。 <p>如果两条建议来自同一方面，则它们将具有相同的顺序。然后根据以下规则进一步对来自同一方
	 * 面的建议进行排序： <ul> <li> 如果在 </em> 建议之后一对中的任何一个是 <em>，则最后声明的建议具有最高优先级（即最后运行）。</li> <li>否则首先声明
	 * 的建议具有最高优先级（即运行）首先).</li> </ul> <p><b>I重要：</b> Advisor 按优先级顺序排序，从最高优先级到最低优先级。在“进入”连接点时，优先
	 * 级最高的顾问程序应首先运行。在连接点“退出”时，优先级最高的顾问程序应该最后运行。
	 */
	@Override
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		List<PartiallyComparableAdvisorHolder> partiallyComparableAdvisors = new ArrayList<>(advisors.size());
		for (Advisor advisor : advisors) {
			partiallyComparableAdvisors.add(
					new PartiallyComparableAdvisorHolder(advisor, DEFAULT_PRECEDENCE_COMPARATOR));
		}
		List<PartiallyComparableAdvisorHolder> sorted = PartialOrder.sort(partiallyComparableAdvisors);
		if (sorted != null) {
			List<Advisor> result = new ArrayList<>(advisors.size());
			for (PartiallyComparableAdvisorHolder pcAdvisor : sorted) {
				result.add(pcAdvisor.getAdvisor());
			}
			return result;
		}
		else {
			return super.sortAdvisors(advisors);
		}
	}

	/**
	 * 将 {@link ExposeInvocationInterceptor} 添加到建议链的开头。 <p> 使用 AspectJ 切入点表达式和使用 AspectJ
	 * 样式建议时需要此附加建议。
	 */
	@Override
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
		AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(candidateAdvisors);
	}

	/**
	 * 方法 `shouldSkip`：完成本类中与「should Skip」相关的职责。
	 */
	@Override
	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		for (Advisor advisor : candidateAdvisors) {
			if (advisor instanceof AspectJPointcutAdvisor pointcutAdvisor &&
					pointcutAdvisor.getAspectName().equals(beanName)) {
				return true;
			}
		}
		return super.shouldSkip(beanClass, beanName);
	}

	/**
	 * 在…之后回调：Singletons Instantiated（方法 `afterSingletonsInstantiated`）。
	 */
	@Override
	public void afterSingletonsInstantiated() {
		ShadowMatchUtils.clearCache();
	}

	/**
	 * 销毁（方法 `destroy`）。
	 */
	@Override
	public void destroy() {
		ShadowMatchUtils.clearCache();
	}


	/**
	 * 实现 AspectJ 的 {@link PartialComparable} 接口来定义部分排序。
	 */
	private static class PartiallyComparableAdvisorHolder implements PartialComparable {

		private final Advisor advisor;

		private final Comparator<Advisor> comparator;

		public PartiallyComparableAdvisorHolder(Advisor advisor, Comparator<Advisor> comparator) {
			this.advisor = advisor;
			this.comparator = comparator;
		}

		@Override
		public int compareTo(Object obj) {
			Advisor otherAdvisor = ((PartiallyComparableAdvisorHolder) obj).advisor;
			return this.comparator.compare(this.advisor, otherAdvisor);
		}

		@Override
		public int fallbackCompareTo(Object obj) {
			return 0;
		}

		public Advisor getAdvisor() {
			return this.advisor;
		}

		@Override
		public String toString() {
			Advice advice = this.advisor.getAdvice();
			StringBuilder sb = new StringBuilder(ClassUtils.getShortName(advice.getClass()));
			boolean appended = false;
			if (this.advisor instanceof Ordered ordered) {
				sb.append(": order = ").append(ordered.getOrder());
				appended = true;
			}
			if (advice instanceof AbstractAspectJAdvice ajAdvice) {
				sb.append(!appended ? ": " : ", ");
				sb.append("aspect name = ");
				sb.append(ajAdvice.getAspectName());
				sb.append(", declaration order = ");
				sb.append(ajAdvice.getDeclarationOrder());
			}
			return sb.toString();
		}
	}

}
