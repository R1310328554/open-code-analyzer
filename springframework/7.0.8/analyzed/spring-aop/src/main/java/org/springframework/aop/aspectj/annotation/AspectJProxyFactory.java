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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.aspectj.SimpleAspectInstanceFactory;
import org.springframework.aop.framework.ProxyCreatorSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 基于 AspectJ 的代理工厂，允许以编程方式构建包括 AspectJ 方面（代码样式以及注释样式）的代理。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 * @see #addAspect(Object)
 * @see #addAspect(Class)
 * @see #getProxy()
 * @see #getProxy(ClassLoader)
 * @see org.springframework.aop.framework.ProxyFactory
 */
@SuppressWarnings("serial")
public class AspectJProxyFactory extends ProxyCreatorSupport {

	/**
	 */
	private static final Map<Class<?>, Object> aspectCache = new ConcurrentHashMap<>();

	/**
	 * 方法 `ReflectiveAspectJAdvisorFactory`：完成本类中与「Reflective Aspect J Advisor Factory」相关的职责。
	 */
	private final AspectJAdvisorFactory aspectFactory = new ReflectiveAspectJAdvisorFactory();


	/**
	 * 创建一个新的 AspectJProxyFactory。
	 */
	public AspectJProxyFactory() {
	}

	/**
	 * 创建一个新的 AspectJProxyFactory。 <p>将代理给定目标实现的所有接口。
	 * @param target 被代理的目标对象
	 */
	public AspectJProxyFactory(Object target) {
		Assert.notNull(target, "Target object must not be null");
		setInterfaces(ClassUtils.getAllInterfaces(target));
		setTarget(target);
	}

	/**
	 * 创建一个新的 {@code AspectJProxyFactory}。没有目标，只有接口。必须添加拦截器。
	 */
	public AspectJProxyFactory(Class<?>... interfaces) {
		setInterfaces(interfaces);
	}


	/**
	 * 将提供的方面实例添加到链中。提供的方面实例的类型必须是单例方面。使用此方法时，不会遵循真正的单例生命周期 - 调用者负责管理以这种方式添加的任何方面的生命周期。
	 * @param aspectInstance AspectJ 方面实例
	 */
	public void addAspect(Object aspectInstance) {
		Class<?> aspectClass = aspectInstance.getClass();
		String aspectName = aspectClass.getName();
		AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
		if (am.getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON) {
			throw new IllegalArgumentException(
					"Aspect class [" + aspectClass.getName() + "] does not define a singleton aspect");
		}
		addAdvisorsFromAspectInstanceFactory(
				new SingletonMetadataAwareAspectInstanceFactory(aspectInstance, aspectName));
	}

	/**
	 * 将所提供类型的一个方面添加到建议链的末尾。
	 * @param aspectClass AspectJ 方面类
	 */
	public void addAspect(Class<?> aspectClass) {
		String aspectName = aspectClass.getName();
		AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
		MetadataAwareAspectInstanceFactory instanceFactory = createAspectInstanceFactory(am, aspectClass, aspectName);
		addAdvisorsFromAspectInstanceFactory(instanceFactory);
	}


	/**
	 * 将提供的 {@link MetadataAwareAspectInstanceFactory} 中的所有 {@link Advisor Advisors}
	 * 添加到当前链。如果需要，公开任何特殊用途的 {@link Advisor Advisors}。
	 * @see AspectJProxyUtils#makeAdvisorChainAspectJCapableIfNecessary(List)
	 */
	private void addAdvisorsFromAspectInstanceFactory(MetadataAwareAspectInstanceFactory instanceFactory) {
		List<Advisor> advisors = this.aspectFactory.getAdvisors(instanceFactory);
		Class<?> targetClass = getTargetClass();
		Assert.state(targetClass != null, "Unresolvable target class");
		advisors = AopUtils.findAdvisorsThatCanApply(advisors, targetClass);
		AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(advisors);
		AnnotationAwareOrderComparator.sort(advisors);
		addAdvisors(advisors);
	}

	/**
	 * 为提供的方面类型创建 {@link AspectMetadata} 实例。
	 */
	private AspectMetadata createAspectMetadata(Class<?> aspectClass, String aspectName) {
		AspectMetadata am = new AspectMetadata(aspectClass, aspectName);
		if (!am.getAjType().isAspect()) {
			throw new IllegalArgumentException("Class [" + aspectClass.getName() + "] is not a valid aspect type");
		}
		return am;
	}

	/**
	 * 为提供的方面类型创建 {@link MetadataAwareAspectInstanceFactory}。如果方面类型没有 per 子句，则返回 {@link
	 * SingletonMetadataAwareAspectInstanceFactory}，否则返回 {@link
	 * PrototypeAspectInstanceFactory}。
	 */
	private MetadataAwareAspectInstanceFactory createAspectInstanceFactory(
			AspectMetadata am, Class<?> aspectClass, String aspectName) {

		MetadataAwareAspectInstanceFactory instanceFactory;
		if (am.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
			// 创建共享方面实例。
			Object instance = getSingletonAspectInstance(aspectClass);
			instanceFactory = new SingletonMetadataAwareAspectInstanceFactory(instance, aspectName);
		}
		else {
			// 为独立方面实例创建工厂。
			instanceFactory = new SimpleMetadataAwareAspectInstanceFactory(aspectClass, aspectName);
		}
		return instanceFactory;
	}

	/**
	 * 获取所提供的方面类型的单例方面实例。如果在实例缓存中找不到实例，则会创建一个实例。
	 */
	private Object getSingletonAspectInstance(Class<?> aspectClass) {
		return aspectCache.computeIfAbsent(aspectClass,
				clazz -> new SimpleAspectInstanceFactory(clazz).getAspectInstance());
	}


	/**
	 * 根据该工厂中的设置创建一个新的代理。 <p>可以重复调用。如果我们添加或删除接口，效果会有所不同。可以添加和删除拦截器。 <p>U 使用默认类加载器：通常是线程上下文类加载器（
	 * 如果需要创建代理）。
	 * @return 新代理
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProxy() {
		return (T) createAopProxy().getProxy();
	}

	/**
	 * 根据该工厂中的设置创建一个新的代理。 <p>可以重复调用。如果我们添加或删除接口，效果会有所不同。可以添加和删除拦截器。 <p>U使用给定的类加载器（如果需要创建代理）。
	 * @param classLoader 用于创建代理的类加载器
	 * @return 新代理
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProxy(ClassLoader classLoader) {
		return (T) createAopProxy().getProxy(classLoader);
	}

}
