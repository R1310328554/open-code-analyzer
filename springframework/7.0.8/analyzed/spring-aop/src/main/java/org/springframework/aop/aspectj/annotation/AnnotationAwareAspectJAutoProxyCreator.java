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

package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

/**
 * {@link AspectJAwareAdvisorAutoProxyCreator} 子类，处理当前应用程序上下文中的所有 AspectJ 注释方面以及 Spring
 * Advisor。
 * <p>Any AspectJ 注释类将被自动识别，并且如果 Spring AOP 的基于代理的模型能够应用它们的建议，则会应用它们。这涵盖了方法执行连接点。
 * <p>如果<aop:include>使用 element 时，只有名称与包含模式匹配的 @AspectJ bean 才会被视为定义用于 Spring 自动代理的切面。
 * <p> Spring Advisors 的处理遵循 {@link
 * org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator} 中建立的规则。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

	/** `includePatterns`：该类的成员状态。 */
	private @Nullable List<Pattern> includePatterns;

	/** 工厂相关状态（`aspectJAdvisorFactory`）。 */
	private @Nullable AspectJAdvisorFactory aspectJAdvisorFactory;

	/** 通知器相关状态（`aspectJAdvisorsBuilder`）。 */
	private @Nullable BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * 设置正则表达式模式列表，匹配合格的 @AspectJ bean 名称。 <p>Default 是将所有 @AspectJ beans 视为合格。
	 */
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	/**
	 * 设置 Aspect J Advisor Factory（`AspectJAdvisorFactory`）。
	 */
	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	/**
	 * 方法 `initBeanFactory`：完成本类中与「init Bean Factory」相关的职责。
	 */
	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.initBeanFactory(beanFactory);
		if (this.aspectJAdvisorFactory == null) {
			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}


	/**
	 * 查找：Candidate Advisors（方法 `findCandidateAdvisors`）。
	 */
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// 添加根据超类规则找到的所有 Spring Advisor。
		List<Advisor> advisors = super.findCandidateAdvisors();
		// 为 bean 工厂中的所有 AspectJ 方面构建 Advisor。
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}

	/**
	 * 判断是否 Infrastructure Class。
	 */
	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// 之前我们在构造函数中 setProxyTargetClass(true) ，但这也太
		// 影响广泛。相反，我们现在重写 isInfrastructureClass 以避免代理
		// 方面。我对此并不完全满意，因为没有充分的理由不这样做
		// 向方面提供建议，但它会导致建议调用经过
		// 代理，如果切面实现了 Ordered 接口，那么它将是
		// 由该接口代理并在运行时失败，因为建议方法不是
		// 定义在接口上。我们可能会放宽以下限制
		// 不建议未来的方面。
		return (super.isInfrastructureClass(beanClass) ||
				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
	}

	/**
	 * 检查给定的方面 bean 是否符合自动代理的条件。 <p>如果没有<aop:include>如果使用了元素，那么“includePatterns”将是 {@code
	 * null} 并且所有 bean 都被包含在内。如果“includePatterns”非空，则模式之一必须匹配。
	 */
	protected boolean isEligibleAspectBean(String beanName) {
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	/**
	 * BeanFactoryAspectJAdvisorsBuilderAdapter 的子类，委托给周围的 AnnotationAwareAspectJAutoProxyCreat
	 * or 设施。
	 */
	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

		public BeanFactoryAspectJAdvisorsBuilderAdapter(
				ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {

			super(beanFactory, advisorFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
		}
	}

}
