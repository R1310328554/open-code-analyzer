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

package org.springframework.beans.factory.support;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 在无注解支持时使用的 {@link AutowireCandidateResolver} 实现。
 * 本实现仅检查 Bean 定义。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 */
public class SimpleAutowireCandidateResolver implements AutowireCandidateResolver {

	/**
	 * {@code SimpleAutowireCandidateResolver} 的共享实例。
	 * @since 5.2.7
	 */
	public static final SimpleAutowireCandidateResolver INSTANCE = new SimpleAutowireCandidateResolver();

	/**
	 * 本实现直接返回 {@code this}。
	 * @see #INSTANCE
	 */
	@Override
	public AutowireCandidateResolver cloneIfNecessary() {
		return this;
	}


	/**
	 * 解析给定类型的全部 Bean 映射，同时包含祖先 Bean 工厂中定义的 Bean；
	 * 条件是各 Bean 必须具有自动装配候选状态。这与本 {@link AutowireCandidateResolver}
	 * 策略实现的简单注入点解析一致，包含未标记为默认候选的 Bean，
	 * 但排除未标记为自动装配候选的 Bean。
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @return 匹配 Bean 实例的映射，若无则返回空映射
	 * @throws BeansException 若 Bean 无法创建
	 * @since 6.2.3
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 * @see org.springframework.beans.factory.config.BeanDefinition#isAutowireCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate()
	 */
	public static <T> Map<String, T> resolveAutowireCandidates(ConfigurableListableBeanFactory lbf, Class<T> type) {
		return resolveAutowireCandidates(lbf, type, true, true);
	}

	/**
	 * 解析给定类型的全部 Bean 映射，同时包含祖先 Bean 工厂中定义的 Bean；
	 * 条件是各 Bean 必须具有自动装配候选状态。这与本 {@link AutowireCandidateResolver}
	 * 策略实现的简单注入点解析一致，包含未标记为默认候选的 Bean，
	 * 但排除未标记为自动装配候选的 Bean。
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @param includeNonSingletons 是否包含原型或作用域 Bean，或仅单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 是否为类型检查初始化<i>懒加载单例</i>以及
	 * <i>由 FactoryBean 创建的对象</i>（或带有 "factory-bean" 引用的工厂方法）。
	 * 注意 FactoryBean 需被急切初始化以确定其类型：因此传入 {@code true}
	 * 将初始化 FactoryBean 与 "factory-bean" 引用。
	 * @return 匹配 Bean 实例的映射，若无则返回空映射
	 * @throws BeansException 若 Bean 无法创建
	 * @since 6.2.5
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 * @see org.springframework.beans.factory.config.BeanDefinition#isAutowireCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate()
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> resolveAutowireCandidates(ConfigurableListableBeanFactory lbf, Class<T> type,
			boolean includeNonSingletons, boolean allowEagerInit) {

		Map<String, T> candidates = new LinkedHashMap<>();
		for (String beanName : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(lbf, type,
				includeNonSingletons, allowEagerInit)) {
			if (AutowireUtils.isAutowireCandidate(lbf, beanName)) {
				Object beanInstance = lbf.getBean(beanName);
				if (!(beanInstance instanceof NullBean)) {
					candidates.put(beanName, (T) beanInstance);
				}
			}
		}
		return candidates;
	}

}
