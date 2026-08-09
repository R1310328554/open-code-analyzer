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

package org.springframework.aop.framework.autoproxy;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Conventions;
import org.springframework.util.StringUtils;

/**
 * 自动代理感知组件的工具类。
 * 主要用于框架内部。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see AbstractAutoProxyCreator
 * @see AbstractBeanFactoryAwareAdvisingPostProcessor
 */
public abstract class AutoProxyUtils {

	/**
	 * 内部管理的自动代理创建器的 Bean 名称。
	 * @since 7.0
	 */
	public static final String DEFAULT_PROXY_CONFIG_BEAN_NAME =
			"org.springframework.aop.framework.autoproxy.defaultProxyConfig";

	/**
	 * Bean 定义属性，可指示要代理的接口
	 * （若该 Bean 首先会被代理）。值为单个接口 {@code Class}
	 * 或 {@code Class} 数组；空数组明确表示需代理所有已实现接口。
	 * @since 7.0
	 * @see #determineExposedInterfaces
	 */
	public static final String EXPOSED_INTERFACES_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "exposedInterfaces");

	/**
	 * 专门用于表示需代理所有已实现接口的属性值
	 * （通过空 {@code Class} 数组）。
	 * @since 7.0
	 * @see #EXPOSED_INTERFACES_ATTRIBUTE
	 */
	public static final Object ALL_INTERFACES_ATTRIBUTE_VALUE = new Class<?>[0];

	/**
	 * Bean 定义属性，可指示给定 Bean 是否应以目标类代理
	 * （若该 Bean 首先会被代理）。值为 {@code Boolean.TRUE} 或 {@code Boolean.FALSE}。
	 * <p>若代理工厂为特定 Bean 构建了目标类代理，
	 * 并希望该 Bean 始终可转型为目标类
	 * （即使通过自动代理应用了 AOP 通知），可设置此属性。
	 * @see #shouldProxyTargetClass
	 */
	public static final String PRESERVE_TARGET_CLASS_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "preserveTargetClass");

	/**
	 * Bean 定义属性，指示自动代理 Bean 的原始目标类，
	 * 例如用于内省基于接口的代理背后的目标类上的注解。
	 * @since 4.2.3
	 * @see #determineTargetClass
	 */
	public static final String ORIGINAL_TARGET_CLASS_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "originalTargetClass");


	/**
	 * 如有必要，将默认 ProxyConfig 设置应用到给定 ProxyConfig 实例。
	 * @param proxyConfig 当前 ProxyConfig 实例
	 * @param beanFactory 用于获取默认 ProxyConfig 的 BeanFactory
	 * @since 7.0
	 * @see #DEFAULT_PROXY_CONFIG_BEAN_NAME
	 * @see ProxyConfig#copyDefault
	 */
	static void applyDefaultProxyConfig(ProxyConfig proxyConfig, BeanFactory beanFactory) {
		if (beanFactory.containsBean(DEFAULT_PROXY_CONFIG_BEAN_NAME)) {
			ProxyConfig defaultProxyConfig = beanFactory.getBean(DEFAULT_PROXY_CONFIG_BEAN_NAME, ProxyConfig.class);
			proxyConfig.copyDefault(defaultProxyConfig);
		}
	}

	/**
	 * 确定代理给定 Bean 的特定接口（若有）。
	 * 检查对应 Bean 定义的 {@link #EXPOSED_INTERFACES_ATTRIBUTE "exposedInterfaces" 属性}。
	 * @param beanFactory 包含的 ConfigurableListableBeanFactory
	 * @param beanName Bean 名称
	 * @return 给定 Bean 是否应以目标类代理
	 * @since 7.0
	 * @see #EXPOSED_INTERFACES_ATTRIBUTE
	 */
	static Class<?> @Nullable [] determineExposedInterfaces(
			ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {

		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
			Object interfaces = bd.getAttribute(EXPOSED_INTERFACES_ATTRIBUTE);
			if (interfaces instanceof Class<?>[] ifcs) {
				return ifcs;
			}
			else if (interfaces instanceof Class<?> ifc) {
				return new Class<?>[] {ifc};
			}
		}
		return null;
	}

	/**
	 * 确定给定 Bean 是否应以目标类而非接口代理。
	 * 检查对应 Bean 定义的
	 * {@link #PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass" 属性}。
	 * @param beanFactory 包含的 ConfigurableListableBeanFactory
	 * @param beanName Bean 名称
	 * @return 给定 Bean 是否应以目标类代理
	 * @see #PRESERVE_TARGET_CLASS_ATTRIBUTE
	 */
	public static boolean shouldProxyTargetClass(
			ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {

		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
			return Boolean.TRUE.equals(bd.getAttribute(PRESERVE_TARGET_CLASS_ATTRIBUTE));
		}
		return false;
	}

	/**
	 * 尽可能确定指定 Bean 的原始目标类，
	 * 否则回退到常规 {@code getType} 查找。
	 * @param beanFactory 包含的 ConfigurableListableBeanFactory
	 * @param beanName Bean 名称
	 * @return 存储在 Bean 定义中的原始目标类（若有）
	 * @since 4.2.3
	 * @see org.springframework.beans.factory.BeanFactory#getType(String)
	 */
	public static @Nullable Class<?> determineTargetClass(
			ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {

		if (beanName == null) {
			return null;
		}
		if (beanFactory.containsBeanDefinition(beanName)) {
			BeanDefinition bd = beanFactory.getMergedBeanDefinition(beanName);
			Class<?> targetClass = (Class<?>) bd.getAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE);
			if (targetClass != null) {
				return targetClass;
			}
		}
		return beanFactory.getType(beanName);
	}

	/**
	 * 尽可能为指定 Bean 暴露给定目标类。
	 * @param beanFactory 包含的 ConfigurableListableBeanFactory
	 * @param beanName Bean 名称
	 * @param targetClass 对应的目标类
	 * @since 4.2.3
	 */
	static void exposeTargetClass(
			ConfigurableListableBeanFactory beanFactory, @Nullable String beanName, Class<?> targetClass) {

		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
			beanFactory.getMergedBeanDefinition(beanName).setAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE, targetClass);
		}
	}

	/**
	 * 根据 {@link AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX}
	 * 判断给定 Bean 名称是否表示「原始实例」，从而跳过对其的代理尝试。
	 * @param beanName Bean 名称
	 * @param beanClass 对应 Bean 类
	 * @since 5.1
	 * @see AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX
	 */
	static boolean isOriginalInstance(String beanName, Class<?> beanClass) {
		if (!StringUtils.hasLength(beanName) || beanName.length() !=
				beanClass.getName().length() + AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX.length()) {
			return false;
		}
		return (beanName.startsWith(beanClass.getName()) &&
				beanName.endsWith(AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX));
	}

}
