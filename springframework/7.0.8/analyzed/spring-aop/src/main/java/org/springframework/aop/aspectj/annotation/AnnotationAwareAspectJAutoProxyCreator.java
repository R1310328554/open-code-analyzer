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
 * 处理当前应用上下文中所有 AspectJ 注解切面及 Spring 通知器的
 * {@link AspectJAwareAdvisorAutoProxyCreator} 子类。
 *
 * <p>任意带 AspectJ 注解的类将被自动识别，
 * 若 Spring AOP 基于代理的模型能应用其通知则予以应用。
 * 这涵盖方法执行连接点。
 *
 * <p>若使用 &lt;aop:include&gt; 元素，
 * 仅名称匹配 include 模式的 @AspectJ Bean 才被视为用于 Spring 自动代理的切面定义。
 *
 * <p>Spring 通知器的处理遵循
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator} 中的规则。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

	private @Nullable List<Pattern> includePatterns;

	private @Nullable AspectJAdvisorFactory aspectJAdvisorFactory;

	private @Nullable BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * 设置正则模式列表，匹配符合条件的 @AspectJ Bean 名称。
	 * <p>默认将所有 @AspectJ Bean 视为符合条件。
	 */
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.initBeanFactory(beanFactory);
		if (this.aspectJAdvisorFactory == null) {
			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}


	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// 按超类规则添加找到的所有 Spring 通知器。
		List<Advisor> advisors = super.findCandidateAdvisors();
		// 为 Bean 工厂中所有 AspectJ 切面构建通知器。
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}

	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// 此前在构造器中 setProxyTargetClass(true)，但影响过广。
		// 现改为覆盖 isInfrastructureClass 以避免代理切面。
		// 对此并不完全满意——并非没有充分理由去通知切面，
		// 只是会导致通知调用经代理进行；若切面实现 Ordered 等接口，
		// 将按该接口代理并在运行时失败，因接口上未定义通知方法。
		// 未来或可放宽不通知切面的限制。
		return (super.isInfrastructureClass(beanClass) ||
				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
	}

	/**
	 * 检查给定切面 Bean 是否符合自动代理条件。
	 * <p>若未使用 &lt;aop:include&gt; 元素，则 "includePatterns" 为
	 * {@code null}，所有 Bean 均包含。
	 * 若 "includePatterns" 非 null，则须匹配其中一个模式。
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
	 * BeanFactoryAspectJAdvisorsBuilderAdapter 的子类，
	 * 委托给外围 AnnotationAwareAspectJAutoProxyCreator 设施。
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
