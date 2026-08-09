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

/**
 * 需要在 {@link BeanFactory} 设置完所有属性后作出响应的 Bean 所实现的接口：
 * 例如执行自定义初始化，或仅检查所有必填属性是否已设置。
 *
 * <p>不实现 {@code InitializingBean} 的另一种方式是指定自定义 init 方法，
 * 例如在 XML Bean 定义中声明。完整的 Bean 生命周期方法列表见
 * {@link BeanFactory BeanFactory javadocs}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DisposableBean
 * @see org.springframework.beans.factory.config.BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getInitMethodName()
 */
public interface InitializingBean {

	/**
	 * 由所属 {@code BeanFactory} 在设置完所有 Bean 属性，并满足
	 * {@link BeanFactoryAware}、{@code ApplicationContextAware} 等之后调用。
	 * <p>本方法允许 Bean 实例在所有属性设置完毕后，执行整体配置校验与最终初始化。
	 * @throws Exception 配置错误时（例如未能设置必要属性），
	 * 或因其他原因初始化失败时
	 */
	void afterPropertiesSet() throws Exception;

}
