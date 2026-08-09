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

import java.lang.reflect.Constructor;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;

/**
 * {@link InstantiationAwareBeanPostProcessor} 的扩展接口，
 * 增加了预测处理后 bean 最终类型的回调。
 *
 * <p><b>注意：</b>本接口为特殊用途接口，主要用于框架内部。
 * 一般而言，应用提供的后置处理器应直接实现普通 {@link BeanPostProcessor} 接口。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	/**
	 * 预测本处理器 {@link #postProcessBeforeInstantiation} 回调最终将返回的 bean 类型。
	 * <p>默认实现返回 {@code null}。具体实现应基于已知/已缓存信息尽可能预测 bean 类型，
	 * 无需额外处理步骤。
	 * @param beanClass bean 的原始类
	 * @param beanName bean 名称
	 * @return bean 类型，不可预测时返回 {@code null}
	 * @throws org.springframework.beans.BeansException 出错时
	 */
	default @Nullable Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * 确定本处理器 {@link #postProcessBeforeInstantiation} 回调最终将返回的 bean 类型。
	 * <p>默认实现原样返回给定 bean 类。具体实现应完整评估处理步骤，
	 * 以便预先创建/初始化潜在的代理类。
	 * @param beanClass bean 的原始类
	 * @param beanName bean 名称
	 * @return bean 类型（永不为 {@code null}）
	 * @throws org.springframework.beans.BeansException 出错时
	 * @since 6.0
	 */
	default Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return beanClass;
	}

	/**
	 * 确定给定 bean 要使用的候选构造器。
	 * <p>默认实现返回 {@code null}。
	 * @param beanClass bean 的原始类（永不为 {@code null}）
	 * @param beanName bean 名称
	 * @return 候选构造器，未指定时返回 {@code null}
	 * @throws org.springframework.beans.BeansException 出错时
	 */
	default Constructor<?> @Nullable [] determineCandidateConstructors(Class<?> beanClass, String beanName)
			throws BeansException {

		return null;
	}

	/**
	 * 获取对指定 bean 的早期访问引用，通常用于解决循环引用。
	 * <p>本回调允许后置处理器提前暴露包装器——即在目标 bean 实例完全初始化之前。
	 * 暴露的对象应与 {@link #postProcessBeforeInitialization} /
	 * {@link #postProcessAfterInitialization} 否则会暴露的对象等价。注意，除非后置处理器
	 * 从上述后置处理回调返回不同包装器，本方法返回的对象将作为 bean 引用使用。
	 * 换言之，那些后置处理回调可能最终暴露相同引用，或从后续回调返回原始 bean 实例
	 * （若受影响 bean 的包装器已为调用本方法而构建，默认将作为最终 bean 引用暴露）。
	 * <p>默认实现原样返回给定 {@code bean}。
	 * @param bean 原始 bean 实例
	 * @param beanName bean 名称
	 * @return 作为 bean 引用暴露的对象（通常为传入的 bean 实例）
	 * @throws org.springframework.beans.BeansException 出错时
	 */
	default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
