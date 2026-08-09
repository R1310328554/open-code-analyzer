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

import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 在运行时对<i>已合并</i> Bean 定义进行后处理的回调接口。
 * {@link BeanPostProcessor} 实现可扩展此子接口，以便后处理 Spring {@code BeanFactory}
 * 用于创建 Bean 实例的合并 Bean 定义（原始 Bean 定义的已处理副本）。
 *
 * <p>{@link #postProcessMergedBeanDefinition} 方法可例如内省 Bean 定义，
 * 以便在实际 Bean 实例后处理之前准备某些缓存元数据。也允许修改 Bean 定义，
 * 但<i>仅</i>限于确实需要并发修改的定义属性。本质上，这只适用于
 * {@link RootBeanDefinition} 自身定义的操作，而不适用于其基类的属性。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getMergedBeanDefinition
 */
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	/**
	 * 对指定 Bean 的已合并 Bean 定义进行后处理。
	 * @param beanDefinition 该 Bean 的已合并 Bean 定义
	 * @param beanType 受管 Bean 实例的实际类型
	 * @param beanName Bean 名称
	 * @see AbstractAutowireCapableBeanFactory#applyMergedBeanDefinitionPostProcessors
	 */
	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

	/**
	 * 通知指定名称的 Bean 定义已被重置，
	 * 本后处理器应清除受影响 Bean 的相关元数据。
	 * <p>默认实现为空。
	 * @param beanName Bean 名称
	 * @since 5.1
	 * @see DefaultListableBeanFactory#resetBeanDefinition
	 */
	default void resetBeanDefinition(String beanName) {
	}

}
