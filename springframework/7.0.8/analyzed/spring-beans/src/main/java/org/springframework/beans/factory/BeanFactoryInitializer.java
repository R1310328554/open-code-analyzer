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
 * 在进入单例预实例化阶段之前，用于初始化 Spring {@link ListableBeanFactory}
 * 的回调接口。可用于在常规单例之前触发特定 bean 的早期初始化。
 *
 * <p>可程序化地应用到 {@code ListableBeanFactory} 实例上。
 * 在 {@code ApplicationContext} 中，类型为 {@code BeanFactoryInitializer}
 * 的 bean 会被自动检测并应用到底层 bean 工厂。
 *
 * @author Juergen Hoeller
 * @since 6.2
 * @param <F> bean 工厂类型
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
 */
public interface BeanFactoryInitializer<F extends ListableBeanFactory> {

	/**
	 * 初始化给定的 bean 工厂。
	 * @param beanFactory 要引导的 bean 工厂
	 */
	void initialize(F beanFactory);

}
