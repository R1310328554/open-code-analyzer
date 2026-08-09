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
 * {@link BeanPostProcessor} 的子接口，增加了 Bean 销毁前的回调。
 *
 * <p>典型用法是针对特定 Bean 类型调用自定义销毁回调，与对应的初始化回调相匹配。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * 在 Bean 销毁前应用本 BeanPostProcessor，例如调用自定义销毁回调。
	 * <p>与 {@link org.springframework.beans.factory.DisposableBean#destroy()} 及自定义 destroy 方法类似，
	 * 本回调仅适用于容器完全管理其生命周期的 Bean。通常包括单例和作用域 Bean。
	 * @param bean 即将被销毁的 Bean 实例
	 * @param beanName Bean 名称
	 * @throws org.springframework.beans.BeansException 出错时
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setDestroyMethodName(String)
	 */
	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

	/**
	 * 判断给定 Bean 实例是否需要由本后置处理器执行销毁。
	 * <p>默认实现返回 {@code true}。若 5.0 之前的 {@code DestructionAwareBeanPostProcessor}
	 * 实现未提供本方法的具体实现，Spring 也会静默假定为 {@code true}。
	 * @param bean 待检查的 Bean 实例
	 * @return 若最终需要对本 Bean 实例调用 {@link #postProcessBeforeDestruction} 则为 {@code true}，
	 * 否则为 {@code false}
	 * @since 4.3
	 */
	default boolean requiresDestruction(Object bean) {
		return true;
	}

}
