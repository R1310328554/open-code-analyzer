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

package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.NamedBean;

/**
 * 创建顾问程序的便捷方法可以在自动代理使用 Spring IoC 容器创建的 bean 时使用，将 bean 名称绑定到当前调用。可以使用 AspectJ 支持 {@code b
 * ean()} 切入点指示符。
 * <p>通常用于 Spring 自动代理，其中 bean 名称在代理创建时已知。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.NamedBean
 */
public abstract class ExposeBeanNameAdvisors {

	/**
	 * 绑定当前在 ReflectiveMethodInitation userAttributes Map 中调用的 bean 的 bean 名称。
	 */
	private static final String BEAN_NAME_ATTRIBUTE = ExposeBeanNameAdvisors.class.getName() + ".BEAN_NAME";


	/**
	 * 查找当前调用的 bean 名称。假设一个 ExposeBeanNameAdvisor 已经包含在拦截器链中，并且调用是通过
	 * ExposeInitationInterceptor 公开的。
	 * @return bean 名称（绝不是 {@code null}）
	 * @throws IllegalStateException 如果bean名称尚未暴露
	 */
	public static String getBeanName() throws IllegalStateException {
		return getBeanName(ExposeInvocationInterceptor.currentInvocation());
	}

	/**
	 * 查找给定调用的 bean 名称。假设一个 ExposeBeanNameAdvisor 已包含在拦截器链中。
	 * @param mi 应该包含 bean 名称作为属性的 MethodInitation
	 * @return bean 名称（绝不是 {@code null}）
	 * @throws IllegalStateException 如果bean名称尚未暴露
	 */
	public static String getBeanName(MethodInvocation mi) throws IllegalStateException {
		if (!(mi instanceof ProxyMethodInvocation pmi)) {
			throw new IllegalArgumentException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
		}
		String beanName = (String) pmi.getUserAttribute(BEAN_NAME_ATTRIBUTE);
		if (beanName == null) {
			throw new IllegalStateException("Cannot get bean name; not set on MethodInvocation: " + mi);
		}
		return beanName;
	}

	/**
	 * 创建一个新的顾问程序，它将公开给定的 bean 名称，但无需介绍。
	 * @param beanName 要公开的 bean 名称
	 */
	public static Advisor createAdvisorWithoutIntroduction(String beanName) {
		return new DefaultPointcutAdvisor(new ExposeBeanNameInterceptor(beanName));
	}

	/**
	 * 创建一个新的顾问程序，它将公开给定的 bean 名称，引入 NamedBean 接口以使 bean 名称可访问，而无需强制目标对象了解此 Spring IoC 概念。
	 * @param beanName 要公开的 bean 名称
	 */
	public static Advisor createAdvisorIntroducingNamedBean(String beanName) {
		return new DefaultIntroductionAdvisor(new ExposeBeanNameIntroduction(beanName));
	}


	/**
	 * 将指定的 bean 名称公开为调用属性的拦截器。
	 */
	private static class ExposeBeanNameInterceptor implements MethodInterceptor {

		private final String beanName;

		public ExposeBeanNameInterceptor(String beanName) {
			this.beanName = beanName;
		}

		@Override
		public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
			if (!(mi instanceof ProxyMethodInvocation pmi)) {
				throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
			}
			pmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, this.beanName);
			return mi.proceed();
		}
	}


	/**
	 * 将指定的 bean 名称公开为调用属性的介绍。
	 */
	@SuppressWarnings("serial")
	private static class ExposeBeanNameIntroduction extends DelegatingIntroductionInterceptor implements NamedBean {

		private final String beanName;

		public ExposeBeanNameIntroduction(String beanName) {
			this.beanName = beanName;
		}

		@Override
		public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
			if (!(mi instanceof ProxyMethodInvocation pmi)) {
				throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
			}
			pmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, this.beanName);
			return super.invoke(mi);
		}

		@Override
		public String getBeanName() {
			return this.beanName;
		}
	}

}
