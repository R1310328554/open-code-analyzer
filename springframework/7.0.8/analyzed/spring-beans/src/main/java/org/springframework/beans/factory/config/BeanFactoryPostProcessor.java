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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * 工厂钩子，允许自定义修改应用上下文的 bean 定义，
 * 适配上下文底层 bean 工厂的 bean 属性值。
 *
 * <p>适用于面向系统管理员的自定义配置文件，用于覆盖应用上下文中配置的 bean 属性。
 * 参见 {@link PropertyResourceConfigurer} 及其具体实现，了解此类配置需求的现成方案。
 *
 * <p>{@code BeanFactoryPostProcessor} 可与 bean 定义交互并修改它们，但绝不能操作 bean 实例。
 * 否则可能导致 bean 过早实例化，破坏容器并引发意外副作用。
 * 若需要与 bean 实例交互，请考虑实现 {@link BeanPostProcessor}。
 *
 * <h3>注册</h3>
 * <p>{@code ApplicationContext} 会在其 bean 定义中自动检测 {@code BeanFactoryPostProcessor}
 * bean，并在创建任何其他 bean 之前应用它们。
 * 也可通过 {@code ConfigurableApplicationContext} 以编程方式注册 {@code BeanFactoryPostProcessor}。
 *
 * <p>在 {@code @Configuration} 类中通过 {@code @Bean} 方法注册 {@code BeanFactoryPostProcessor} 时，
 * 应使用 {@code static} 方法，以避免配置类中其他 bean 被过早初始化。
 * 详见 {@link org.springframework.context.annotation.Bean @Bean} 的 JavaDoc 中
 * "返回 BeanFactoryPostProcessor 的 {@code @Bean} 方法" 一节及示例。
 *
 * <h3>排序</h3>
 * <p>在 {@code ApplicationContext} 中自动检测到的 {@code BeanFactoryPostProcessor} bean
 * 将按照 {@link org.springframework.core.PriorityOrdered} 和
 * {@link org.springframework.core.Ordered} 语义排序。
 * 相比之下，通过 {@code ConfigurableApplicationContext} 以编程方式注册的
 * {@code BeanFactoryPostProcessor} bean 将按注册顺序应用；
 * 通过实现 {@code PriorityOrdered} 或 {@code Ordered} 接口表达的排序语义
 * 对编程注册的处理器将被忽略。此外，{@link org.springframework.core.annotation.Order @Order}
 * 注解对 {@code BeanFactoryPostProcessor} bean 不生效。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
@FunctionalInterface
public interface BeanFactoryPostProcessor {

	/**
	 * 在应用上下文内部 bean 工厂完成标准初始化后对其进行修改。
	 * 此时所有 bean 定义均已加载，但尚未实例化任何 bean。
	 * 这允许覆盖或添加属性，甚至作用于急切初始化的 bean。
	 * @param beanFactory 应用上下文使用的 bean 工厂
	 * @throws org.springframework.beans.BeansException 发生错误时
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
