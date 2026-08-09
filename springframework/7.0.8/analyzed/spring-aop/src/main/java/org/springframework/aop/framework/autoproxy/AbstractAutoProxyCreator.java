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

/* ===== [OCA 中文解析] =====
文件意图总览

Spring AOP 自动代理创建器抽象基类：作为 BeanPostProcessor 在容器初始化阶段为符合条件的 Bean 包装 AOP 代理，协调通用/特定拦截器、TargetSourceCreator 与 Advisor 适配注册表。
===== [OCA 中文解析结束] ===== */
package org.springframework.aop.framework.autoproxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.SmartClassLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/* ===== [OCA 中文解析] =====
class AbstractAutoProxyCreator — 意图说明

AOP 自动代理的核心抽象：在 Bean 实例化/属性填充阶段决定是否创建代理、装配 Advisor 与 TargetSource，是 DefaultAdvisorAutoProxyCreator、BeanNameAutoProxyCreator 等的共同父类。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} 实现，用 AOP 代理包装每个符合条件的 Bean，在调用目标 Bean 之前先经过指定拦截器链。
 * <p>区分「通用」拦截器（所有代理共享）与「特定」拦截器（每个 Bean 实例独有）。通用拦截器通过 {@link #setInterceptorNames interceptorNames} 配置；与 {@link org.springframework.aop.framework.ProxyFactoryBean} 一样使用工厂内 Bean 名称而非引用，以正确处理原型 Advisor/Interceptor（如 stateful mixin）。
 * <p>当大量 Bean 需委托相同拦截器时，注册单个后处理器即可替代重复的代理定义。
 * <p>子类决定代理策略（按类型、名称、定义细节等），并可返回仅作用于特定 Bean 的附加拦截器。典型实现如 {@link BeanNameAutoProxyCreator}。
 * <p>可配置 {@link TargetSourceCreator} 创建自定义 TargetSource（如池化原型）；即使无 Advice，只要指定了自定义 {@link org.springframework.aop.TargetSource} 也会代理。未匹配时默认使用 {@link org.springframework.aop.target.SingletonTargetSource}。
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 13.10.2003
 * @see #setInterceptorNames
 * @see #getAdvicesAndAdvisorsForBean
 * @see BeanNameAutoProxyCreator
 * @see DefaultAdvisorAutoProxyCreator
 */
@SuppressWarnings("serial")
public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport
		implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {

	/**
	 * 子类便捷常量：表示「不创建代理」的返回值。
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	protected static final Object @Nullable [] DO_NOT_PROXY = null;

	/**
	 * 子类的便利常量：“没有附加拦截器的代理，只有常见的拦截器”的返回值。
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];


	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/**
	 */
	private String[] interceptorNames = new String[0];

	/** 布尔配置标志。 */
	private boolean applyCommonInterceptorsFirst = true;

	/** 来源相关状态（`customTargetSourceCreators`）。 */
	private TargetSourceCreator @Nullable [] customTargetSourceCreators;

	/** 底层 BeanFactory 引用。 */
	private @Nullable BeanFactory beanFactory;

	/**
	 * 执行 newKeySet 方法的核心逻辑。
	 */
	private final Set<String> targetSourcedBeans = ConcurrentHashMap.newKeySet(16);

	private final Map<Object, Object> earlyBeanReferences = new ConcurrentHashMap<>(16);

	private final Map<Object, Class<?>> proxyTypes = new ConcurrentHashMap<>(16);

	private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<>(256);


	/**
	 * 指定要使用的 {@link AdvisorAdapterRegistry}。
	 * <p>默认为全局 {@link AdvisorAdapterRegistry}。
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * 设置要按此顺序应用的自定义 {@code TargetSourceCreators}。如果列表为空，或者它们都返回 null，则将为每个 bean 创建一个 {@link
	 * SingletonTargetSource}。 <p>请注意，即使对于未找到建议或顾问的目标 bean，TargetSourceCreators 也会启动。如果 {@code
	 * TargetSourceCreator} 返回特定 bean 的 {@link TargetSource}，则该 bean 在任何情况下都将被代理。仅当在 {@link
	 * BeanFactory} 中使用此后处理器并且触发其 {@link BeanFactoryAware} 回调时，才能调用 <p>{@code
	 * TargetSourceCreators}。
	 * @param targetSourceCreators {@code TargetSourceCreators} 列表。排序很重要：将使用从第一个匹配的 {@code TargetSourceCreator}（即第一个返回非空值）返回的 {@code TargetSource}。
	 */
	public void setCustomTargetSourceCreators(TargetSourceCreator... targetSourceCreators) {
		this.customTargetSourceCreators = targetSourceCreators;
	}

	/**
	 * 设置常用的拦截器。这些必须是当前工厂中的 bean 名称。它们可以是 Spring 支持的任何建议或顾问类型。 <p>如果未设置此属性，则公共拦截器将为零。如果我们想要的只是“
	 * 特定”拦截器（例如匹配顾问），那么这是完全有效的。
	 */
	public void setInterceptorNames(String... interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	/**
	 * 设置是否应在特定于 Bean 的拦截器之前应用公共拦截器。默认为“true”；否则，将首先应用特定于 Bean 的拦截器。
	 */
	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
	}

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		AutoProxyUtils.applyDefaultProxyConfig(this, beanFactory);
	}

	/**
	 * 返回所属的 {@link BeanFactory}。可能是 {@code null}，因为该后处理器不需要属于 bean 工厂。
	 */
	protected @Nullable BeanFactory getBeanFactory() {
		return this.beanFactory;
	}


	/**
	 * 执行 predictBeanType 方法的核心逻辑。
	 */
	@Override
	public @Nullable Class<?> predictBeanType(Class<?> beanClass, String beanName) {
		if (this.proxyTypes.isEmpty()) {
			return null;
		}
		Object cacheKey = getCacheKey(beanClass, beanName);
		return this.proxyTypes.get(cacheKey);
	}

	/**
	 * 执行 determineBeanType 方法的核心逻辑。
	 */
	@Override
	public Class<?> determineBeanType(Class<?> beanClass, String beanName) {
		Object cacheKey = getCacheKey(beanClass, beanName);
		Class<?> proxyType = this.proxyTypes.get(cacheKey);
		if (proxyType == null) {
			TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
			if (targetSource != null) {
				if (StringUtils.hasLength(beanName)) {
					this.targetSourcedBeans.add(beanName);
				}
			}
			else {
				targetSource = EmptyTargetSource.forClass(beanClass);
			}
			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
			if (specificInterceptors != DO_NOT_PROXY) {
				this.advisedBeans.put(cacheKey, Boolean.TRUE);
				proxyType = createProxyClass(beanClass, beanName, specificInterceptors, targetSource);
				this.proxyTypes.put(cacheKey, proxyType);
			}
		}
		return (proxyType != null ? proxyType : beanClass);
	}

	/**
	 * 执行 determineCandidateConstructors 方法的核心逻辑。
	 */
	@Override
	public Constructor<?> @Nullable [] determineCandidateConstructors(Class<?> beanClass, String beanName) {
		return null;
	}

	/**
	 * 获取 Early Bean Reference（`EarlyBeanReference`）。
	 */
	@Override
	public Object getEarlyBeanReference(Object bean, String beanName) {
		Object cacheKey = getCacheKey(bean.getClass(), beanName);
		this.earlyBeanReferences.put(cacheKey, bean);
		return wrapIfNecessary(bean, beanName, cacheKey);
	}

	/**
	 * 执行 postProcessBeforeInstantiation 方法的核心逻辑。
	 */
	@Override
	public @Nullable Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
		Object cacheKey = getCacheKey(beanClass, beanName);

		if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
			if (this.advisedBeans.containsKey(cacheKey)) {
				return null;
			}
			if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
				this.advisedBeans.put(cacheKey, Boolean.FALSE);
				return null;
			}
		}

		// 如果我们有自定义 TargetSource，请在此处创建代理。
		// 抑制目标 bean 的不必要的默认实例化：
		// TargetSource 将以自定义方式处理目标实例。
		TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
		if (targetSource != null) {
			if (StringUtils.hasLength(beanName)) {
				this.targetSourcedBeans.add(beanName);
			}
			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
			Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		return null;
	}

	/**
	 * 执行 postProcessProperties 方法的核心逻辑。
	 */
	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
		return pvs;  // skip postProcessPropertyValues
	}

	/**
	 * 如果该 bean 被子类识别为要代理的 bean，则使用配置的拦截器创建一个代理。
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	@Override
	public @Nullable Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
		if (bean != null) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (this.earlyBeanReferences.remove(cacheKey) != bean) {
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
	}


	/**
	 * 为给定的 bean 类和 bean 名称构建缓存键。 <p>注意：从 7.0.2 开始，此实现返回 bean 类加上 bean 名称的组合缓存键；或者如果未指定 bean 名称
	 * ，则按原样给定 bean {@code Class}。
	 * @param beanClass 豆类
	 * @param beanName 豆的名字
	 * @return 给定类和名称的缓存键
	 */
	protected Object getCacheKey(Class<?> beanClass, @Nullable String beanName) {
		if (StringUtils.hasLength(beanName)) {
			return new ComposedCacheKey(beanClass, beanName);
		}
		else {
			return beanClass;
		}
	}

	/**
	 * 如有必要，即如果它有资格被代理，请包装给定的 bean。
	 * @param bean 原始bean实例
	 * @param beanName 豆子的名字
	 * @param cacheKey 用于元数据访问的缓存键
	 * @return 代理包装 bean，或原样的原始 bean 实例
	 */
	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
		if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}

		// 如果我们有建议，请创建代理。
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		if (specificInterceptors != DO_NOT_PROXY) {
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
			Object proxy = createProxy(
					bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
	}

	/**
	 * 返回给定的 bean 类是否代表永远不应该被代理的基础结构类。 <p>默认实现将Advices、Advisors和AopInfrastructionBeans视为基础设施类。
	 * @param beanClass 豆类
	 * @return bean 代表基础设施类
	 * @see org.aopalliance.aop.Advice
	 * @see org.springframework.aop.Advisor
	 * @see org.springframework.aop.framework.AopInfrastructureBean
	 * @see #shouldSkip
	 */
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
				Pointcut.class.isAssignableFrom(beanClass) ||
				Advisor.class.isAssignableFrom(beanClass) ||
				AopInfrastructureBean.class.isAssignableFrom(beanClass);
		if (retVal && logger.isTraceEnabled()) {
			logger.trace("Did not attempt to auto-proxy infrastructure class [" + beanClass.getName() + "]");
		}
		return retVal;
	}

	/**
	 * 如果此后处理器不应考虑给定 bean 进行自动代理，则子类应重写此方法以返回 {@code true}。 <p>有时我们需要能够避免这种情况的发生，例如，如果它会导致循环引用或
	 * 者如果需要保留现有的目标实例。除非 bean 名称根据 {@code AutowireCapableBeanFactory} 约定指示“原始实例”，否则此实现将返回 {@cod
	 * e false}。
	 * @param beanClass 豆类
	 * @param beanName 豆子的名字
	 * @return 跳过给定的 bean
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX
	 */
	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
		return AutoProxyUtils.isOriginalInstance(beanName, beanClass);
	}

	/**
	 * 为 bean 实例创建目标源。如果设置，则使用任何 TargetSourceCreators。如果不应使用自定义 TargetSource，则返回 {@code null}。
	 * <p>此实现使用“customTargetSourceCreators”属性。子类可以重写此方法以使用不同的机制。
	 * @param beanClass 要为其创建 TargetSource 的 bean 类
	 * @param beanName 豆子的名字
	 * @return 该 bean 的 TargetSource
	 * @see #setCustomTargetSourceCreators
	 */
	protected @Nullable TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
		// 我们无法为直接注册的单例创建花哨的目标源。
		if (this.customTargetSourceCreators != null &&
				this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
			for (TargetSourceCreator tsc : this.customTargetSourceCreators) {
				TargetSource ts = tsc.getTargetSource(beanClass, beanName);
				if (ts != null) {
					// 找到匹配的 TargetSource。
					if (logger.isTraceEnabled()) {
						logger.trace("TargetSourceCreator [" + tsc +
								"] found custom TargetSource for bean with name '" + beanName + "'");
					}
					return ts;
				}
			}
		}

		// 未找到自定义 TargetSource。
		return null;
	}

	/**
	 * 为给定的 bean 创建 AOP 代理。
	 * @param beanClass 豆类
	 * @param beanName 豆子的名字
	 * @param specificInterceptors 特定于此 bean 的拦截器集（可以为空，但不为 null）
	 * @param targetSource 代理的 TargetSource，已预先配置为访问 bean
	 * @return bean 的 AOP 代理
	 * @see #buildAdvisors
	 */
	protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
			Object @Nullable [] specificInterceptors, TargetSource targetSource) {

		return buildProxy(beanClass, beanName, specificInterceptors, targetSource, false);
	}

	/**
	 * 创建 AOP 代理类（createProxyClass）。
	 */
	private Class<?> createProxyClass(Class<?> beanClass, @Nullable String beanName,
			Object @Nullable [] specificInterceptors, TargetSource targetSource) {

		return (Class<?>) buildProxy(beanClass, beanName, specificInterceptors, targetSource, true);
	}

	/**
	 * 组装并返回 AOP 代理实例（buildProxy）。
	 */
	private Object buildProxy(Class<?> beanClass, @Nullable String beanName,
			Object @Nullable [] specificInterceptors, TargetSource targetSource, boolean classOnly) {

		if (this.beanFactory instanceof ConfigurableListableBeanFactory clbf) {
			AutoProxyUtils.exposeTargetClass(clbf, beanName, beanClass);
		}

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);
		proxyFactory.setFrozen(false);

		if (shouldProxyTargetClass(beanClass, beanName)) {
			proxyFactory.setProxyTargetClass(true);
		}
		else {
			Class<?>[] ifcs = (this.beanFactory instanceof ConfigurableListableBeanFactory clbf ?
					AutoProxyUtils.determineExposedInterfaces(clbf, beanName) : null);
			if (ifcs != null) {
				proxyFactory.setProxyTargetClass(false);
				for (Class<?> ifc : ifcs) {
					proxyFactory.addInterface(ifc);
				}
			}
			if (ifcs != null ? ifcs.length == 0 : !proxyFactory.isProxyTargetClass()) {
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
		}

		if (proxyFactory.isProxyTargetClass()) {
			// 显式处理 JDK 代理目标和 lambda（用于介绍建议场景）
			if (Proxy.isProxyClass(beanClass) || ClassUtils.isLambdaClass(beanClass)) {
				// 必须允许介绍；不能只将接口设置为代理的接口。
				for (Class<?> ifc : beanClass.getInterfaces()) {
					proxyFactory.addInterface(ifc);
				}
			}
		}

		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		proxyFactory.addAdvisors(advisors);
		proxyFactory.setTargetSource(targetSource);
		customizeProxyFactory(proxyFactory);

		proxyFactory.setFrozen(isFrozen());
		if (advisorsPreFiltered()) {
			proxyFactory.setPreFiltered(true);
		}

		// 如果 bean 类未在重写类加载器中本地加载，则使用原始 ClassLoader
		ClassLoader classLoader = getProxyClassLoader();
		if (classLoader instanceof SmartClassLoader smartClassLoader && classLoader != beanClass.getClassLoader()) {
			classLoader = smartClassLoader.getOriginalClassLoader();
		}
		return (classOnly ? proxyFactory.getProxyClass(classLoader) : proxyFactory.getProxy(classLoader));
	}

	/**
	 * 确定给定 bean 是否应使用其目标类而不是其接口进行代理。 <p>检查相应bean定义的{@link
	 * AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass" attribute}。
	 * @param beanClass 豆类
	 * @param beanName 豆子的名字
	 * @return 给定的 bean 应该用它的目标类来代理
	 * @see AutoProxyUtils#shouldProxyTargetClass
	 */
	protected boolean shouldProxyTargetClass(Class<?> beanClass, @Nullable String beanName) {
		return (this.beanFactory instanceof ConfigurableListableBeanFactory clbf &&
				AutoProxyUtils.shouldProxyTargetClass(clbf, beanName));
	}

	/**
	 * 返回子类返回的 Advisor 是否已预先过滤以匹配 bean 的目标类，从而允许在为 AOP 调用构建 Advisor 链时跳过 ClassFilter 检查。 <p>默认为
	 *  {@code false}。如果子类始终返回预先过滤的 Advisor，则它们可以覆盖此设置。
	 * @return 顾问已预先过滤
	 * @see #getAdvicesAndAdvisorsForBean
	 * @see org.springframework.aop.framework.Advised#setPreFiltered
	 */
	protected boolean advisorsPreFiltered() {
		return false;
	}

	/**
	 * 确定给定 bean 的 Advisor，包括特定拦截器和通用拦截器，所有拦截器均适用于 Advisor 接口。
	 * @param beanName 豆子的名字
	 * @param specificInterceptors 特定于此 bean 的拦截器集（可以为空，但不为 null）
	 * @return 给定 bean 的顾问列表
	 */
	protected Advisor[] buildAdvisors(@Nullable String beanName, Object @Nullable [] specificInterceptors) {
		// 正确处理原型...
		Advisor[] commonInterceptors = resolveInterceptorNames();

		List<Object> allInterceptors = new ArrayList<>();
		if (specificInterceptors != null) {
			if (specificInterceptors.length > 0) {
				// SpecificInterceptors 可能等于 PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
				allInterceptors.addAll(Arrays.asList(specificInterceptors));
			}
			if (commonInterceptors.length > 0) {
				if (this.applyCommonInterceptorsFirst) {
					allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
				}
				else {
					allInterceptors.addAll(Arrays.asList(commonInterceptors));
				}
			}
		}
		if (logger.isTraceEnabled()) {
			int nrOfCommonInterceptors = commonInterceptors.length;
			int nrOfSpecificInterceptors = (specificInterceptors != null ? specificInterceptors.length : 0);
			logger.trace("Creating implicit proxy for bean '" + beanName + "' with " + nrOfCommonInterceptors +
					" common interceptors and " + nrOfSpecificInterceptors + " specific interceptors");
		}

		Advisor[] advisors = new Advisor[allInterceptors.size()];
		for (int i = 0; i < allInterceptors.size(); i++) {
			advisors[i] = this.advisorAdapterRegistry.wrap(allInterceptors.get(i));
		}
		return advisors;
	}

	/**
	 * 将指定的拦截器名称解析为 Advisor 对象。
	 * @see #setInterceptorNames
	 */
	private Advisor[] resolveInterceptorNames() {
		BeanFactory bf = this.beanFactory;
		ConfigurableBeanFactory cbf = (bf instanceof ConfigurableBeanFactory _cbf ? _cbf : null);
		List<Advisor> advisors = new ArrayList<>();
		for (String beanName : this.interceptorNames) {
			if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
				Assert.state(bf != null, "BeanFactory required for resolving interceptor names");
				Object next = bf.getBean(beanName);
				advisors.add(this.advisorAdapterRegistry.wrap(next));
			}
		}
		return advisors.toArray(new Advisor[0]);
	}

	/**
	 * 子类可以选择实现这一点：例如，更改公开的接口。 <p>默认实现为空。
	 * @param proxyFactory 已配置 TargetSource 和接口的 ProxyFactory，将用于在此方法返回后立即创建代理
	 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
	}


	/**
	 * 返回给定的 bean 是否要被代理、需要应用哪些附加建议（例如，AOP 联盟拦截器）和顾问。
	 * @param beanClass 要建议的 bean 的类别
	 * @param beanName 豆子的名字
	 * @param customTargetSource {@link #getCustomTargetSource} 方法返回的 TargetSource：可以被忽略。如果没有使用自定义目标源，则将为 {@code null}。
	 * @return 特定 bean 的附加拦截器数组；如果没有额外的拦截器而只是常见的拦截器，则为空数组；或者 {@code null} 如果根本没有代理，即使使用常见的拦截器也是如此。请参阅常量 DO_NOT_PROXY 和 PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS。
	 * @throws BeansException 如果出现错误
	 * @see #DO_NOT_PROXY
	 * @see #PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
	 */
	protected abstract Object @Nullable [] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
			@Nullable TargetSource customTargetSource) throws BeansException;


	/**
	 * bean 类加上 bean 名称组成的缓存键。
	 * @see #getCacheKey(Class, String)
	 */
	private record ComposedCacheKey(Class<?> beanClass, String beanName) {
	}

}
