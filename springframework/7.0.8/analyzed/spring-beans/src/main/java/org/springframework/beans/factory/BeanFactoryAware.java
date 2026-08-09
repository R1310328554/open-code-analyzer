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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * 希望感知自身所属 {@link BeanFactory} 的 bean 所实现的接口。
 *
 * <p>例如，bean 可通过工厂查找协作 bean（依赖查找）。
 * 注意：大多数 bean 更倾向于通过对应的 bean 属性或构造器参数
 * 接收协作 bean 的引用（依赖注入）。
 *
 * <p>完整的 bean 生命周期方法列表，参见
 * {@link BeanFactory BeanFactory javadocs}。
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 11.03.2003
 * @see BeanNameAware
 * @see BeanClassLoaderAware
 * @see InitializingBean
 * @see org.springframework.context.ApplicationContextAware
 */
public interface BeanFactoryAware extends Aware {

	/**
	 * 向 bean 实例提供所属工厂的回调。
	 * <p>在填充常规 bean 属性之后、
	 * {@link InitializingBean#afterPropertiesSet()} 或自定义 init-method
	 * 等初始化回调之前调用。
	 * @param beanFactory 所属 BeanFactory（永不为 {@code null}）。
	 * bean 可立即调用工厂上的方法。
	 * @throws BeansException 初始化出错时
	 * @see BeanInitializationException
	 */
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
