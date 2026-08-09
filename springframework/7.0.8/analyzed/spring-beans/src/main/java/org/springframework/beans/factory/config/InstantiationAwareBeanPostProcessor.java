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
import org.springframework.beans.PropertyValues;

/**
 * {@link BeanPostProcessor} 的子接口，增加了实例化前回调，
 * 以及实例化之后、显式属性设置或自动装配发生之前的回调。
 *
 * <p>通常用于抑制特定目标 Bean 的默认实例化，例如创建带有特殊 TargetSource 的代理
 *（池化目标、延迟初始化目标等），或实现额外的注入策略（如字段注入）。
 *
 * <p><b>注意：</b>本接口是专用接口，主要用于框架内部。
 * 建议尽可能实现普通的 {@link BeanPostProcessor} 接口。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.2
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * 在目标 Bean <i>实例化之前</i>应用本 BeanPostProcessor。
	 * 返回的 Bean 对象可作为目标 Bean 的代理，从而有效抑制目标 Bean 的默认实例化。
	 * <p>若本方法返回非 {@code null} 对象，Bean 创建流程将被短路。
	 * 后续仅会应用已配置 {@link BeanPostProcessor BeanPostProcessors} 的
	 * {@link #postProcessAfterInitialization} 回调。
	 * <p>本回调会应用于带有 Bean 类的 Bean 定义，也会应用于工厂方法定义——
	 * 此时此处传入的是返回的 Bean 类型。
	 * <p>后置处理器可实现扩展的 {@link SmartInstantiationAwareBeanPostProcessor} 接口，
	 * 以预测此处将要返回的 Bean 对象类型。
	 * <p>默认实现返回 {@code null}。
	 * @param beanClass 即将实例化的 Bean 类
	 * @param beanName Bean 名称
	 * @return 用于替代目标 Bean 默认实例的 Bean 对象，或 {@code null} 以继续默认实例化
	 * @throws org.springframework.beans.BeansException 出错时
	 * @see #postProcessAfterInstantiation
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getBeanClass()
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getFactoryMethodName()
	 */
	default @Nullable Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * 在 Bean 通过构造器或工厂方法实例化之后、Spring 属性填充
	 *（来自显式属性或自动装配）发生之前执行操作。
	 * <p>这是在 Spring 自动装配生效之前，对给定 Bean 实例执行自定义字段注入的理想回调。
	 * <p>默认实现返回 {@code true}。
	 * @param bean 已创建的 Bean 实例，属性尚未设置
	 * @param beanName Bean 名称
	 * @return 若应为 Bean 设置属性则为 {@code true}；若应跳过属性填充则为 {@code false}。
	 * 常规实现应返回 {@code true}。返回 {@code false} 还会阻止后续
	 * InstantiationAwareBeanPostProcessor 实例对本 Bean 实例的调用。
	 * @throws org.springframework.beans.BeansException 出错时
	 * @see #postProcessBeforeInstantiation
	 */
	default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	/**
	 * 在工厂将属性值应用到给定 Bean 之前，对属性值进行后置处理。
	 * <p>默认实现原样返回给定的 {@code pvs}。
	 * @param pvs 工厂即将应用的属性值（永不为 {@code null}）
	 * @param bean 已创建但属性尚未设置的 Bean 实例
	 * @param beanName Bean 名称
	 * @return 要应用到给定 Bean 的实际属性值（可为传入的 PropertyValues 实例），
	 * 或 {@code null} 以跳过属性填充
	 * @throws org.springframework.beans.BeansException 出错时
	 * @since 5.1
	 */
	default @Nullable PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
			throws BeansException {

		return pvs;
	}

}
