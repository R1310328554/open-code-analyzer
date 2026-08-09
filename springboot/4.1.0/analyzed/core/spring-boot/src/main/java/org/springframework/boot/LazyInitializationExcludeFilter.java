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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * 用于将 Bean 定义排除在 {@link LazyInitializationBeanFactoryPostProcessor}
 * 自动设置 {@link AbstractBeanDefinition#setLazyInit(boolean) lazy-init} 之外的过滤器。
 * <p>
 * 主要供下游项目处理难以支持懒加载的边界情况
 * （例如在 DSL 中动态创建额外 Bean 的场景）。
 * 向应用上下文添加此过滤器的实例即可处理这些边界情况。
 * <p>
 * 典型示例如下： <pre>
 * &#64;Bean
 * public static LazyInitializationExcludeFilter integrationLazyInitializationExcludeFilter() {
 *   return LazyInitializationExcludeFilter.forBeanTypes(IntegrationFlow.class);
 * }
 * </pre>
 * <p>
 * 注意：此类型的 Bean 会在 Spring 应用生命周期极早期实例化，
 * 因此通常应声明为 static 且不依赖其他 Bean。
 *
 * @author Tyler Van Gorder
 * @author Philip Webb
 * @since 2.2.0
 */
@FunctionalInterface
public interface LazyInitializationExcludeFilter {

	/**
	 * 若指定的 Bean 定义应排除在自动设置 {@code lazy-init} 之外，则返回 {@code true}。
	 *
	 * @param beanName Bean 名称
	 * @param beanDefinition Bean 定义
	 * @param beanType Bean 类型
	 * @return 若不应自动设置 {@code lazy-init} 则为 {@code true}
	 */
	boolean isExcluded(String beanName, BeanDefinition beanDefinition, Class<?> beanType);

	/**
	 * 为给定 Bean 类型创建过滤器的工厂方法。
	 *
	 * @param types 要过滤的类型
	 * @return 新的过滤器实例
	 */
	static LazyInitializationExcludeFilter forBeanTypes(Class<?>... types) {
		return (beanName, beanDefinition, beanType) -> {
			for (Class<?> type : types) {
				if (type.isAssignableFrom(beanType)) {
					return true;
				}
			}
			return false;
		};
	}

}
