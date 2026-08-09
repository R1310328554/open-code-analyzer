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
 * 在 {@link BeanFactory} 引导过程中、单例预实例化阶段结束时触发的回调接口。
 * 单例 Bean 可实现本接口，以便在常规单例实例化算法完成之后执行某些初始化，
 * 从而避免意外过早初始化带来的副作用
 * （例如来自 {@link ListableBeanFactory#getBeansOfType} 的调用）。
 * 从这个意义上说，它是 {@link InitializingBean} 的一种替代：
 * 后者在 Bean 本地构造阶段一结束就会被触发。
 *
 * <p>本回调变体与 {@link org.springframework.context.event.ContextRefreshedEvent}
 * 有些类似，但不要求实现 {@link org.springframework.context.ApplicationListener}，
 * 也无需在上下文层次结构中过滤上下文引用等。
 * 它对 {@code beans} 包的依赖也更小；独立的 {@link ListableBeanFactory} 实现
 * 同样会兑现该回调，而不仅限于
 * {@link org.springframework.context.ApplicationContext} 环境。
 *
 * <p><b>注意：</b>若打算启动/管理异步任务，更推荐实现
 * {@link org.springframework.context.Lifecycle}：它提供更丰富的运行时管理模型，
 * 并支持分阶段启动/关闭。
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
 */
public interface SmartInitializingSingleton {

	/**
	 * 在单例预实例化阶段即将结束时调用，
	 * 并保证此时所有常规单例 Bean 都已创建完毕。
	 * 在本方法内调用 {@link ListableBeanFactory#getBeansOfType}
	 * 不会在引导过程中触发意外副作用。
	 * <p><b>注意：</b>对于在 {@link BeanFactory} 引导之后按需惰性初始化的单例 Bean，
	 * 以及任何其他作用域的 Bean，都不会触发本回调。
	 * 请仅在具有预期引导语义的 Bean 上谨慎使用。
	 */
	void afterSingletonsInstantiated();

}
