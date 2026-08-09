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

package org.springframework.aop.config;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * 用于处理 AOP 自动代理创建者注册的实用程序类。
 * OCAJAVA0DCO仅应注册单个自动代理创建者，但可以使用多个具体实现。此类提供了一个简单的升级协议，允许调用者请求特定的自动代理创建者，并知道该创建者 OCAJAVA1DO
 *  或其功能更强大的变体 </i> 将被注册为后处理器。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see AopNamespaceUtils
 */
public abstract class AopConfigUtils {

	/**
	 * 内部管理的自动代理创建者的 bean 名称。
	 */
	public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
			"org.springframework.aop.config.internalAutoProxyCreator";

	/**
	 * 按升级顺序存储自动代理创建者类。
	 */
	private static final List<Class<?>> APC_PRIORITY_LIST = new ArrayList<>(3);

	static {
		// 设置升级列表...
		APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);
	}


	/**
	 * 注册：Auto Proxy Creator If Necessary（方法 `registerAutoProxyCreatorIfNecessary`）。
	 */
	public static @Nullable BeanDefinition registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAutoProxyCreatorIfNecessary(registry, null);
	}

	/**
	 * 注册：Auto Proxy Creator If Necessary（方法 `registerAutoProxyCreatorIfNecessary`）。
	 */
	public static @Nullable BeanDefinition registerAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(InfrastructureAdvisorAutoProxyCreator.class, registry, source);
	}

	/**
	 * 注册：Aspect J Auto Proxy Creator If Necessary（方法 `registerAspectJAutoProxyCreatorIfNecessary`）。
	 */
	public static @Nullable BeanDefinition registerAspectJAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAspectJAutoProxyCreatorIfNecessary(registry, null);
	}

	/**
	 * 注册：Aspect J Auto Proxy Creator If Necessary（方法 `registerAspectJAutoProxyCreatorIfNecessary`）。
	 */
	public static @Nullable BeanDefinition registerAspectJAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(AspectJAwareAdvisorAutoProxyCreator.class, registry, source);
	}

	/**
	 * 注册：Aspect J Annotation Auto Proxy Creator If Necessary（方法 `registerAspectJAnnotationAutoProxyCreatorIfNecessary`）。
	 */
	public static @Nullable BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
	}

	/**
	 * 注册：Aspect J Annotation Auto Proxy Creator If Necessary（方法 `registerAspectJAnnotationAutoProxyCreatorIfNecessary`）。
	 */
	public static @Nullable BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
	}

	/**
	 * 方法 `forceAutoProxyCreatorToUseClassProxying`：完成本类中与「force Auto Proxy Creator To Use Class Proxying」相关的职责。
	 */
	public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
		defaultProxyConfig(registry).getPropertyValues().add("proxyTargetClass", Boolean.TRUE);
	}

	/**
	 * 方法 `forceAutoProxyCreatorToExposeProxy`：完成本类中与「force Auto Proxy Creator To Expose Proxy」相关的职责。
	 */
	public static void forceAutoProxyCreatorToExposeProxy(BeanDefinitionRegistry registry) {
		defaultProxyConfig(registry).getPropertyValues().add("exposeProxy", Boolean.TRUE);
	}

	/**
	 * 方法 `defaultProxyConfig`：完成本类中与「default Proxy Config」相关的职责。
	 */
	private static BeanDefinition defaultProxyConfig(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AutoProxyUtils.DEFAULT_PROXY_CONFIG_BEAN_NAME)) {
			return registry.getBeanDefinition(AutoProxyUtils.DEFAULT_PROXY_CONFIG_BEAN_NAME);
		}
		RootBeanDefinition beanDefinition = new RootBeanDefinition(ProxyConfig.class);
		beanDefinition.setSource(AopConfigUtils.class);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		registry.registerBeanDefinition(AutoProxyUtils.DEFAULT_PROXY_CONFIG_BEAN_NAME, beanDefinition);
		return beanDefinition;
	}

	/**
	 * 注册：Or Escalate Apc As Required（方法 `registerOrEscalateApcAsRequired`）。
	 */
	private static @Nullable BeanDefinition registerOrEscalateApcAsRequired(
			Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			if (!cls.getName().equals(beanDefinition.getBeanClassName())) {
				int currentPriority = findPriorityForClass(beanDefinition.getBeanClassName());
				int requiredPriority = findPriorityForClass(cls);
				if (currentPriority < requiredPriority) {
					beanDefinition.setBeanClassName(cls.getName());
				}
			}
			return null;
		}

		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.setSource(source);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
		return beanDefinition;
	}

	/**
	 * 查找：Priority For Class（方法 `findPriorityForClass`）。
	 */
	private static int findPriorityForClass(Class<?> clazz) {
		return APC_PRIORITY_LIST.indexOf(clazz);
	}

	/**
	 * 查找：Priority For Class（方法 `findPriorityForClass`）。
	 */
	private static int findPriorityForClass(@Nullable String className) {
		for (int i = 0; i < APC_PRIORITY_LIST.size(); i++) {
			Class<?> clazz = APC_PRIORITY_LIST.get(i);
			if (clazz.getName().equals(className)) {
				return i;
			}
		}
		throw new IllegalArgumentException(
				"Class name [" + className + "] is not a known auto-proxy creator class");
	}

}
