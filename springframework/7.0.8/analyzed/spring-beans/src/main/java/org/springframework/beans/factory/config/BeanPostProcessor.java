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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;

/**
 * 工厂钩子，允许自定义修改新 bean 实例 &mdash;
 * 例如检查标记接口或用代理包装 bean。
 *
 * <p>通常，通过标记接口等方式填充 bean 的后处理器会实现
 * {@link #postProcessBeforeInitialization}，
 * 而用代理包装 bean 的后处理器通常实现
 * {@link #postProcessAfterInitialization}。
 *
 * <h3>注册</h3>
 * <p>{@code ApplicationContext} 可在其 bean 定义中自动检测 {@code BeanPostProcessor} bean，
 * 并将这些后处理器应用于随后创建的任何 bean。
 * 普通的 {@code BeanFactory} 允许以编程方式注册后处理器，
 * 将其应用于通过该 bean 工厂创建的所有 bean。
 *
 * <p>在 {@code @Configuration} 类中通过 {@code @Bean} 方法注册 {@code BeanPostProcessor} 时，
 * 应使用 {@code static} 方法，且最好不依赖其他 bean，以避免过早初始化导致其他 bean
 * 无法获得完整后处理。详见 {@link org.springframework.context.annotation.Bean @Bean} 的 JavaDoc 中
 * "返回 BeanPostProcessor 的 {@code @Bean} 方法" 一节及示例。
 *
 * <h3>排序</h3>
 * <p>在 {@code ApplicationContext} 中自动检测到的 {@code BeanPostProcessor} bean
 * 将按照 {@link org.springframework.core.PriorityOrdered} 和
 * {@link org.springframework.core.Ordered} 语义排序。
 * 相比之下，通过 {@code BeanFactory} 以编程方式注册的 {@code BeanPostProcessor} bean
 * 将按注册顺序应用；通过实现 {@code PriorityOrdered} 或 {@code Ordered} 接口表达的排序语义
 * 对编程注册的处理器将被忽略。此外，{@link org.springframework.core.annotation.Order @Order}
 * 注解对 {@code BeanPostProcessor} bean 不生效。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * 在任何 bean 初始化回调（如 InitializingBean 的 {@code afterPropertiesSet}
	 * 或自定义 init-method）<i>之前</i>，将此 {@code BeanPostProcessor} 应用于给定的新 bean 实例。
	 * bean 此时已填充属性值。返回的 bean 实例可能是原始实例的包装。
	 * <p>默认实现原样返回给定的 {@code bean}。
	 * @param bean 新的 bean 实例
	 * @param beanName bean 的名称
	 * @return 要使用的 bean 实例，可以是原始实例或包装后的实例；
	 * 若为 {@code null}，则不再调用后续 BeanPostProcessor
	 * @throws org.springframework.beans.BeansException 发生错误时
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	default @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * 在任何 bean 初始化回调（如 InitializingBean 的 {@code afterPropertiesSet}
	 * 或自定义 init-method）<i>之后</i>，将此 {@code BeanPostProcessor} 应用于给定的新 bean 实例。
	 * bean 此时已填充属性值。返回的 bean 实例可能是原始实例的包装。
	 * <p>对于 FactoryBean，此回调会同时作用于 FactoryBean 实例及其创建的对象。
	 * 后处理器可通过 {@code bean instanceof FactoryBean} 检查来决定
	 * 是否应用于 FactoryBean、其创建的对象，或两者。
	 * <p>与所有其他 {@code BeanPostProcessor} 回调不同，在
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} 方法
	 * 触发短路后，此回调仍会被调用。
	 * <p>默认实现原样返回给定的 {@code bean}。
	 * @param bean 新的 bean 实例
	 * @param beanName bean 的名称
	 * @return 要使用的 bean 实例，可以是原始实例或包装后的实例；
	 * 若为 {@code null}，则不再调用后续 BeanPostProcessor
	 * @throws org.springframework.beans.BeansException 发生错误时
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	default @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
