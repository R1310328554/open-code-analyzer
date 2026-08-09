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

package org.springframework.aop.scope;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;

/**
 * 用于创建作用域代理的实用程序类。
 * <p> 由 ScopedProxyBeanDefinitionDecorator 和 ClassPathBeanDefinitionScanner 使用。
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 2.5
 */
public abstract class ScopedProxyUtils {

	private static final String TARGET_NAME_PREFIX = "scopedTarget.";

	/**
	 * 方法 `length`：完成本类中与「length」相关的职责。
	 */
	private static final int TARGET_NAME_PREFIX_LENGTH = TARGET_NAME_PREFIX.length();


	/**
	 * 为提供的目标 bean 生成作用域代理，使用内部名称注册目标 bean 并在作用域代理上设置“targetBeanName”。
	 * @param definition 原始的bean定义
	 * @param registry bean 定义注册表
	 * @param proxyTargetClass 是否创建目标类代理
	 * @return 范围代理定义
	 * @see #getTargetBeanName(String)
	 * @see #getOriginalBeanName(String)
	 */
	public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition,
			BeanDefinitionRegistry registry, boolean proxyTargetClass) {

		String originalBeanName = definition.getBeanName();
		BeanDefinition targetDefinition = definition.getBeanDefinition();
		String targetBeanName = getTargetBeanName(originalBeanName);

		// 为原始 bean 名称创建一个作用域代理定义，
		// 在内部目标定义中“隐藏”目标 bean。
		RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
		proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, targetBeanName));
		proxyDefinition.setOriginatingBeanDefinition(targetDefinition);
		proxyDefinition.setSource(definition.getSource());
		proxyDefinition.setRole(targetDefinition.getRole());

		proxyDefinition.getPropertyValues().add("targetBeanName", targetBeanName);
		if (proxyTargetClass) {
			targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
			// ScopedProxyFactoryBean 的“proxyTargetClass”默认为 TRUE，因此我们不需要在这里显式设置它。
		}
		else {
			proxyDefinition.getPropertyValues().add("proxyTargetClass", Boolean.FALSE);
		}

		// 从原始 bean 定义复制自动装配设置。
		proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
		proxyDefinition.setPrimary(targetDefinition.isPrimary());
		proxyDefinition.setFallback(targetDefinition.isFallback());
		if (targetDefinition instanceof AbstractBeanDefinition abd) {
			proxyDefinition.setDefaultCandidate(abd.isDefaultCandidate());
			proxyDefinition.copyQualifiersFrom(abd);
		}

		// 应忽略目标 bean，以支持作用域代理。
		targetDefinition.setAutowireCandidate(false);
		targetDefinition.setPrimary(false);
		targetDefinition.setFallback(false);
		if (targetDefinition instanceof AbstractBeanDefinition abd) {
			abd.setDefaultCandidate(false);
		}

		// 在工厂中将目标 bean 注册为单独的 bean。
		registry.registerBeanDefinition(targetBeanName, targetDefinition);

		// 返回作用域代理定义作为主 bean 定义
		// （可能是一个内部 bean）。
		return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
	}

	/**
	 * 生成在范围代理内使用的 bean 名称以引用目标 bean。
	 * @param originalBeanName 豆的原始名称
	 * @return 生成的 bean 用于引用目标 bean
	 * @see #getOriginalBeanName(String)
	 */
	public static String getTargetBeanName(String originalBeanName) {
		return TARGET_NAME_PREFIX + originalBeanName;
	}

	/**
	 * 获取所提供的 {@linkplain #getTargetBeanName target bean name} 的原始 bean 名称。
	 * @param targetBeanName 作用域代理的目标 bean 名称
	 * @return 原豆名
	 * @throws IllegalArgumentException 如果提供的 bean 名称不引用作用域代理的目标
	 * @since 5.1.10
	 * @see #getTargetBeanName(String)
	 * @see #isScopedTarget(String)
	 */
	public static String getOriginalBeanName(@Nullable String targetBeanName) {
		Assert.isTrue(isScopedTarget(targetBeanName), () -> "bean name '" +
				targetBeanName + "' does not refer to the target of a scoped proxy");
		return targetBeanName.substring(TARGET_NAME_PREFIX_LENGTH);
	}

	/**
	 * 确定 {@code beanName} 是否是引用作用域代理内的目标 bean 的 bean 的名称。
	 * @since 4.1.4
	 */
	@Contract("null -> false")
	public static boolean isScopedTarget(@Nullable String beanName) {
		return (beanName != null && beanName.startsWith(TARGET_NAME_PREFIX));
	}

}
