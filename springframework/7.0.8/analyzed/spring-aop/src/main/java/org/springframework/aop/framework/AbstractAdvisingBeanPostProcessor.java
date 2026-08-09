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

package org.springframework.aop.framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.SmartClassLoader;

/**
 * 将 Spring AOP {@link Advisor} 应用于特定 bean 的 {@link BeanPostProcessor} 实现的基类。
 * @author Juergen Hoeller
 * @since 3.2
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport
		implements SmartInstantiationAwareBeanPostProcessor {

	/** 通知器相关状态（`advisor`）。 */
	protected @Nullable Advisor advisor;

	/** `false`：该类的成员状态。 */
	protected boolean beforeExistingAdvisors = false;

	private final Map<Class<?>, Boolean> eligibleBeans = new ConcurrentHashMap<>(256);


	/**
	 * 设置当遇到预先建议的对象时，后处理器的顾问程序是否应该在现有顾问程序之前应用。 <p>Default 为“false”，在现有顾问程序之后应用顾问程序，即尽可能接近目标方法。将
	 * 其切换为“true”，以便该后处理器的顾问程序也包装现有的顾问程序。 <p>注意：检查具体后处理器的 javadoc 是否可能默认更改此标志，具体取决于其顾问程序的性质。
	 */
	public void setBeforeExistingAdvisors(boolean beforeExistingAdvisors) {
		this.beforeExistingAdvisors = beforeExistingAdvisors;
	}


	/**
	 * 方法 `determineBeanType`：完成本类中与「determine Bean Type」相关的职责。
	 */
	@Override
	public Class<?> determineBeanType(Class<?> beanClass, String beanName) {
		if (this.advisor != null && isEligible(beanClass)) {
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.copyFrom(this);
			proxyFactory.setTargetClass(beanClass);

			if (!proxyFactory.isProxyTargetClass()) {
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
			proxyFactory.addAdvisor(this.advisor);
			customizeProxyFactory(proxyFactory);

			// 如果 bean 类未在重写类加载器中本地加载，则使用原始 ClassLoader
			ClassLoader classLoader = getProxyClassLoader();
			if (classLoader instanceof SmartClassLoader smartClassLoader &&
					classLoader != beanClass.getClassLoader()) {
				classLoader = smartClassLoader.getOriginalClassLoader();
			}
			return proxyFactory.getProxyClass(classLoader);
		}

		return beanClass;
	}

	/**
	 * 方法 `postProcessAfterInitialization`：完成本类中与「post Process After Initialization」相关的职责。
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (this.advisor == null || bean instanceof AopInfrastructureBean) {
			// 忽略 AOP 基础设施，例如作用域代理。
			return bean;
		}

		if (bean instanceof Advised advised) {
			if (!advised.isFrozen() && isEligible(AopUtils.getTargetClass(bean))) {
				// 将我们的本地 Advisor 添加到现有代理的 Advisor 链中。
				if (this.beforeExistingAdvisors) {
					advised.addAdvisor(0, this.advisor);
				}
				else if (advised.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE &&
						advised.getAdvisorCount() > 0) {
					// 没有目标，保留最后一个顾问并在之前添加新顾问。
					advised.addAdvisor(advised.getAdvisorCount() - 1, this.advisor);
					return bean;
				}
				else {
					advised.addAdvisor(this.advisor);
				}
				return bean;
			}
		}

		if (isEligible(bean, beanName)) {
			ProxyFactory proxyFactory = prepareProxyFactory(bean, beanName);
			if (!proxyFactory.isProxyTargetClass() && !proxyFactory.hasUserSuppliedInterfaces()) {
				evaluateProxyInterfaces(bean.getClass(), proxyFactory);
			}
			proxyFactory.addAdvisor(this.advisor);
			customizeProxyFactory(proxyFactory);
			proxyFactory.setFrozen(isFrozen());
			proxyFactory.setPreFiltered(true);

			// 如果 bean 类未在重写类加载器中本地加载，则使用原始 ClassLoader
			ClassLoader classLoader = getProxyClassLoader();
			if (classLoader instanceof SmartClassLoader smartClassLoader &&
					classLoader != bean.getClass().getClassLoader()) {
				classLoader = smartClassLoader.getOriginalClassLoader();
			}
			return proxyFactory.getProxy(classLoader);
		}

		// 无需代理。
		return bean;
	}

	/**
	 * 检查给定的 bean 是否有资格使用此后处理器的 {@link Advisor} 提供建议。 <p>D委托 {@link #isEligible(Class)}
	 * 进行目标类检查。可以被覆盖，例如，通过名称专门排除某些 bean。 <p>注意：仅调用常规 bean 实例，但不调用实现 {@link Advised} 并允许将本地
	 * {@link Advisor} 添加到现有代理的 {@link Advisor} 链的现有代理实例。对于后者，直接调用 {@link
	 * #isEligible(Class)}，实际目标类位于现有代理后面（由 {@link AopUtils#getTargetClass(Object)} 确定）。
	 * @param bean Bean实例
	 * @param beanName 豆子的名字
	 * @see #isEligible(Class)
	 */
	protected boolean isEligible(Object bean, String beanName) {
		return isEligible(bean.getClass());
	}

	/**
	 * 检查给定的类是否有资格使用此后处理器的 {@link Advisor} 提供建议。 <p>I实现每个 bean 目标类的 {@code canApply} 结果的缓存。
	 * @param targetClass 要检查的类
	 * @see AopUtils#canApply(Advisor, Class)
	 */
	protected boolean isEligible(Class<?> targetClass) {
		Boolean eligible = this.eligibleBeans.get(targetClass);
		if (eligible != null) {
			return eligible;
		}
		if (this.advisor == null) {
			return false;
		}
		eligible = AopUtils.canApply(this.advisor, targetClass);
		this.eligibleBeans.put(targetClass, eligible);
		return eligible;
	}

	/**
	 * 为给定的 bean 准备 {@link ProxyFactory}。 <p>子类可以自定义目标实例的处理，特别是目标类的公开。随后将应用非目标类代理接口的默认内省和配置的顾问程
	 * 序； {@link #customizeProxyFactory} 允许在代理创建之前对这些部分进行后期自定义。
	 * @param bean 要为其创建代理的 bean 实例
	 * @param beanName 对应的bean名称
	 * @return ProxyFactory，使用该处理器的 {@link ProxyConfig} 设置和指定的 bean 进行初始化
	 * @since 4.2.3
	 * @see #customizeProxyFactory
	 */
	protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);
		proxyFactory.setFrozen(false);
		proxyFactory.setTarget(bean);
		return proxyFactory;
	}

	/**
	 * 子类可以选择实现这一点：例如，更改公开的接口。 <p>默认实现为空。
	 * @param proxyFactory 已经配置了目标、顾问和接口的 ProxyFactory，将用于在此方法返回后立即创建代理
	 * @since 4.2.3
	 * @see #prepareProxyFactory
	 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
	}

}
