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

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

/**
 * 定义共享 bean 实例注册表的接口。
 * 可由 {@link org.springframework.beans.factory.BeanFactory} 实现，
 * 以统一方式暴露其单例管理能力。
 *
 * <p>{@link ConfigurableBeanFactory} 接口扩展了本接口。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ConfigurableBeanFactory
 * @see org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public interface SingletonBeanRegistry {

	/**
	 * 将给定已存在对象以单例形式注册到 bean 注册表，使用指定 bean 名称。
	 * <p>给定实例应已完全初始化；注册表不会执行任何初始化回调（尤其不会调用
	 * InitializingBean 的 {@code afterPropertiesSet} 方法）。给定实例也不会收到
	 * 任何销毁回调（如 DisposableBean 的 {@code destroy} 方法）。
	 * <p>在完整 BeanFactory 中运行时：<b>若 bean 需要接收初始化和/或销毁回调，
	 * 应注册 bean 定义而非已存在实例。</b>
	 * <p>通常在注册表配置期间调用，也可用于运行时注册单例。因此注册表实现应同步
	 * 单例访问；若支持 BeanFactory 的单例懒初始化，本就需要这样做。
	 * @param beanName bean 名称
	 * @param singletonObject 已存在的单例对象
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.DisposableBean#destroy
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#registerBeanDefinition
	 */
	void registerSingleton(String beanName, Object singletonObject);

	/**
	 * 添加回调：当指定单例在 bean 注册表中可用时触发。
	 * @param beanName bean 名称
	 * @param singletonConsumer 响应新注册/创建的单例实例的回调（用于其他调用方
	 * 主动使用 bean 之前的后续步骤，而非修改给定单例实例本身）
	 * @since 6.2
	 */
	void addSingletonCallback(String beanName, Consumer<Object> singletonConsumer);

	/**
	 * 返回以给定名称注册的（原始）单例对象。
	 * <p>仅检查已实例化的单例；对尚未实例化的单例 bean 定义不返回 Object。
	 * <p>本方法主要用于访问手动注册的单例（见 {@link #registerSingleton}）。
	 * 也可用于以原始方式访问已由 bean 定义创建的单例。
	 * <p><b>注意：</b>本查找方法不感知 FactoryBean 前缀或别名。获取单例实例前
	 * 需先解析规范 bean 名称。
	 * @param beanName 要查找的 bean 名称
	 * @return 已注册的单例对象，未找到时返回 {@code null}
	 * @see ConfigurableListableBeanFactory#getBeanDefinition
	 */
	@Nullable Object getSingleton(String beanName);

	/**
	 * 检查本注册表是否包含以给定名称的单例实例。
	 * <p>仅检查已实例化的单例；对尚未实例化的单例 bean 定义不返回 {@code true}。
	 * <p>本方法主要用于检查手动注册的单例（见 {@link #registerSingleton}）。
	 * 也可用于检查由 bean 定义定义的单例是否已创建。
	 * <p>要检查 bean 工厂是否包含给定名称的 bean 定义，使用 ListableBeanFactory 的
	 * {@code containsBeanDefinition}。同时调用 {@code containsBeanDefinition} 与
	 * {@code containsSingleton} 可判断特定 bean 工厂是否包含该名称的本地 bean 实例。
	 * <p>使用 BeanFactory 的 {@code containsBean} 进行通用检查（工厂是否知晓该名称的 bean，
	 * 无论是手动注册的单例还是由 bean 定义创建），并检查祖先工厂。
	 * <p><b>注意：</b>本查找方法不感知 FactoryBean 前缀或别名。检查单例状态前
	 * 需先解析规范 bean 名称。
	 * @param beanName 要查找的 bean 名称
	 * @return 本 bean 工厂是否包含以给定名称的单例实例
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
	 * @see org.springframework.beans.factory.BeanFactory#containsBean
	 */
	boolean containsSingleton(String beanName);

	/**
	 * 返回本注册表中已注册的单例 bean 名称。
	 * <p>仅检查已实例化的单例；对尚未实例化的单例 bean 定义不返回名称。
	 * <p>本方法主要用于检查手动注册的单例（见 {@link #registerSingleton}）。
	 * 也可用于检查由 bean 定义定义的单例中哪些已创建。
	 * @return 名称列表（String 数组，永不为 {@code null}）
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionNames
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames
	 */
	String[] getSingletonNames();

	/**
	 * 返回本注册表中已注册的单例 bean 数量。
	 * <p>仅统计已实例化的单例；不统计尚未实例化的单例 bean 定义。
	 * <p>本方法主要用于检查手动注册的单例（见 {@link #registerSingleton}）。
	 * 也可用于统计由 bean 定义定义且已创建的单例数量。
	 * @return 单例 bean 数量
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionCount
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionCount
	 */
	int getSingletonCount();

	/**
	 * 返回本注册表使用的单例互斥锁（供外部协作者使用）。
	 * @return 互斥对象（永不为 {@code null}）
	 * @since 4.2
	 * @deprecated as of 6.2, in favor of lenient singleton locking
	 * (with this method returning an arbitrary object to lock on)
	 */
	@Deprecated(since = "6.2")
	Object getSingletonMutex();

}
