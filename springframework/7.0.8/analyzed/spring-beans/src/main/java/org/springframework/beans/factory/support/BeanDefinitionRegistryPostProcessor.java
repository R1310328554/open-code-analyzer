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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 标准 {@link BeanFactoryPostProcessor} SPI 的扩展，允许在常规
 * BeanFactoryPostProcessor 检测启动<i>之前</i>注册更多 Bean 定义。
 * 具体而言，BeanDefinitionRegistryPostProcessor 可注册更多 Bean 定义，
 * 而这些定义又可声明 BeanFactoryPostProcessor 实例。
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * 在应用上下文内部 Bean 定义注册表完成标准初始化后对其进行修改。
	 * 所有常规 Bean 定义此时已加载，但尚未实例化任何 Bean。
	 * 这允许在下一阶段后处理启动之前添加更多 Bean 定义。
	 * @param registry 应用上下文使用的 Bean 定义注册表
	 * @throws org.springframework.beans.BeansException 出错时
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

	/**
	 * {@link BeanFactoryPostProcessor#postProcessBeanFactory} 的空实现，
	 * 因为自定义 {@code BeanDefinitionRegistryPostProcessor} 实现通常
	 * 仅提供 {@link #postProcessBeanDefinitionRegistry} 方法。
	 * @since 6.1
	 */
	@Override
	default void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

}
