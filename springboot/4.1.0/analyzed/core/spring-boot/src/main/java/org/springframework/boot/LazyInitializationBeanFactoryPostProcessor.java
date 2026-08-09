/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.Ordered;

/**
 * 对未被 {@link LazyInitializationExcludeFilter 排除} 且尚未显式设置值的
 * Bean 定义设置 {@code lazy-init} 的 {@link BeanFactoryPostProcessor}。
 * <p>
 * 注意：{@link SmartInitializingSingleton SmartInitializingSingletons} 会自动排除在
 * 懒加载之外，以确保其
 * {@link SmartInitializingSingleton#afterSingletonsInstantiated() 回调方法} 被调用。
 * <p>
 * 角色为 {@link BeanDefinition#ROLE_INFRASTRUCTURE 基础设施} 的 Bean
 * 也会自动排除在懒加载之外。
 *
 * @author Andy Wilkinson
 * @author Madhura Bhave
 * @author Tyler Van Gorder
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 2.2.0
 * @see LazyInitializationExcludeFilter
 */
public final class LazyInitializationBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Collection<LazyInitializationExcludeFilter> filters = getFilters(beanFactory);
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
			if (beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition) {
				postProcess(beanFactory, filters, beanName, abstractBeanDefinition);
			}
		}
	}

	private Collection<LazyInitializationExcludeFilter> getFilters(ConfigurableListableBeanFactory beanFactory) {
		// Take care not to force the eager init of factory beans when getting filters
		ArrayList<LazyInitializationExcludeFilter> filters = new ArrayList<>(
				beanFactory.getBeansOfType(LazyInitializationExcludeFilter.class, false, false).values());
		filters.add(LazyInitializationExcludeFilter.forBeanTypes(SmartInitializingSingleton.class));
		filters.add(new InfrastructureRoleLazyInitializationExcludeFilter());
		return filters;
	}

	private void postProcess(ConfigurableListableBeanFactory beanFactory,
			Collection<LazyInitializationExcludeFilter> filters, String beanName,
			AbstractBeanDefinition beanDefinition) {
		Boolean lazyInit = beanDefinition.getLazyInit();
		if (lazyInit != null) {
			return;
		}
		Class<?> beanType = getBeanType(beanFactory, beanName);
		if (!isExcluded(filters, beanName, beanDefinition, beanType)) {
			beanDefinition.setLazyInit(true);
		}
	}

	private @Nullable Class<?> getBeanType(ConfigurableListableBeanFactory beanFactory, String beanName) {
		try {
			return beanFactory.getType(beanName, false);
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}

	private boolean isExcluded(Collection<LazyInitializationExcludeFilter> filters, String beanName,
			AbstractBeanDefinition beanDefinition, @Nullable Class<?> beanType) {
		if (beanType != null) {
			for (LazyInitializationExcludeFilter filter : filters) {
				if (filter.isExcluded(beanName, beanDefinition, beanType)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	/**
	 * 将所有具有基础设施角色的 {@link BeanDefinition Bean 定义}
	 * 排除在懒加载之外。
	 */
	private static final class InfrastructureRoleLazyInitializationExcludeFilter
			implements LazyInitializationExcludeFilter {

		@Override
		public boolean isExcluded(String beanName, BeanDefinition beanDefinition, Class<?> beanType) {
			return beanDefinition.getRole() == BeanDefinition.ROLE_INFRASTRUCTURE;
		}

	}

}
