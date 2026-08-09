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
 * 将 Spring AOP {@link Advisor} 应用于特定 Bean 的
 * {@link BeanPostProcessor} 实现基类。
 *
 * @author Juergen Hoeller
 * @since 3.2
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport
		implements SmartInstantiationAwareBeanPostProcessor {

	protected @Nullable Advisor advisor;

	protected boolean beforeExistingAdvisors = false;

	private final Map<Class<?>, Boolean> eligibleBeans = new ConcurrentHashMap<>(256);


	/**
	 * 设置遇到已预通知对象时，本后置处理器的通知器是否应排在现有通知器之前。
	 * <p>默认为 "false"，即在现有通知器之后应用，尽量靠近目标方法。
	 * 设为 "true" 时，本后置处理器的通知器也会包裹现有通知器。
	 * <p>注意：请查阅具体后置处理器的 JavaDoc，其可能根据通知器性质默认修改此标志。
	 */
	public void setBeforeExistingAdvisors(boolean beforeExistingAdvisors) {
		this.beforeExistingAdvisors = beforeExistingAdvisors;
	}


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

			// 若 Bean 类未在覆盖类加载器中本地加载，则使用原始 ClassLoader
			ClassLoader classLoader = getProxyClassLoader();
			if (classLoader instanceof SmartClassLoader smartClassLoader &&
					classLoader != beanClass.getClassLoader()) {
				classLoader = smartClassLoader.getOriginalClassLoader();
			}
			return proxyFactory.getProxyClass(classLoader);
		}

		return beanClass;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (this.advisor == null || bean instanceof AopInfrastructureBean) {
			// 忽略 AOP 基础设施，例如作用域代理。
			return bean;
		}

		if (bean instanceof Advised advised) {
			if (!advised.isFrozen() && isEligible(AopUtils.getTargetClass(bean))) {
				// 将本地通知器加入现有代理的通知器链。
				if (this.beforeExistingAdvisors) {
					advised.addAdvisor(0, this.advisor);
				}
				else if (advised.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE &&
						advised.getAdvisorCount() > 0) {
					// 无目标对象，保留末尾通知器，并在其前插入新通知器。
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

			// Use original ClassLoader if bean class not locally loaded in overriding class loader
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
	 * 检查给定 Bean 是否适合用本后置处理器的 {@link Advisor} 进行通知。
	 * <p>目标类检查委托给 {@link #isEligible(Class)}。
	 * 可覆盖，例如按名称排除特定 Bean。
	 * <p>注意：仅对普通 Bean 实例调用，不对已实现 {@link Advised}、
	 * 允许将本地 {@link Advisor} 加入现有代理 {@link Advisor} 链的代理实例调用。
	 * 后者直接调用 {@link #isEligible(Class)}，
	 * 使用现有代理背后的实际目标类（由 {@link AopUtils#getTargetClass(Object)} 确定）。
	 * @param bean Bean 实例
	 * @param beanName Bean 名称
	 * @see #isEligible(Class)
	 */
	protected boolean isEligible(Object bean, String beanName) {
		return isEligible(bean.getClass());
	}

	/**
	 * 检查给定类是否适合用本后置处理器的 {@link Advisor} 进行通知。
	 * <p>按 Bean 目标类缓存 {@code canApply} 结果。
	 * @param targetClass 待检查的类
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
	 * 为给定 Bean 准备 {@link ProxyFactory}。
	 * <p>子类可定制目标实例处理，尤其是目标类的暴露方式。
	 * 之后将应用非 target-class 代理的默认接口内省及已配置的通知器；
	 * {@link #customizeProxyFactory} 可在创建代理前对这些部分做最后定制。
	 * @param bean 待创建代理的 Bean 实例
	 * @param beanName 对应的 Bean 名称
	 * @return 已用本处理器的 {@link ProxyConfig} 设置及指定 Bean 初始化的 ProxyFactory
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
	 * 子类可选择实现本方法，例如修改暴露的接口。
	 * <p>默认实现为空。
	 * @param proxyFactory 已配置目标、通知器与接口、
	 * 本方法返回后将立即用于创建代理的 ProxyFactory
	 * @since 4.2.3
	 * @see #prepareProxyFactory
	 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
	}

}
