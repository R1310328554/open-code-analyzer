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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;

/**
 * 当向 {@code BeanFactory} 请求某个 Bean 实例，却找不到对应定义时抛出。
 * 可能表示 Bean 不存在、Bean 不唯一，
 * 或存在手动注册的单例实例却没有关联的 Bean 定义。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @see BeanFactory#getBean(String)
 * @see BeanFactory#getBean(Class)
 * @see NoUniqueBeanDefinitionException
 */
@SuppressWarnings("serial")
public class NoSuchBeanDefinitionException extends BeansException {

	/** 按名称查找失败时缺失的 Bean 名称 */
	private final @Nullable String beanName;

	/** 按类型查找失败时所需的可解析类型 */
	private final @Nullable ResolvableType resolvableType;


	/**
	 * 创建新的 {@code NoSuchBeanDefinitionException}。
	 * @param name 缺失 Bean 的名称
	 */
	public NoSuchBeanDefinitionException(String name) {
		super("No bean named '" + name + "' available");
		this.beanName = name;
		this.resolvableType = null;
	}

	/**
	 * 创建新的 {@code NoSuchBeanDefinitionException}。
	 * @param name 缺失 Bean 的名称
	 * @param message 描述问题的详细消息
	 */
	public NoSuchBeanDefinitionException(String name, String message) {
		super("No bean named '" + name + "' available: " + message);
		this.beanName = name;
		this.resolvableType = null;
	}

	/**
	 * 创建新的 {@code NoSuchBeanDefinitionException}。
	 * @param type 缺失 Bean 的所需类型
	 */
	public NoSuchBeanDefinitionException(Class<?> type) {
		this(ResolvableType.forClass(type));
	}

	/**
	 * 创建新的 {@code NoSuchBeanDefinitionException}。
	 * @param type 缺失 Bean 的所需类型
	 * @param message 描述问题的详细消息
	 */
	public NoSuchBeanDefinitionException(Class<?> type, String message) {
		this(ResolvableType.forClass(type), message);
	}

	/**
	 * 创建新的 {@code NoSuchBeanDefinitionException}。
	 * @param type 缺失 Bean 的完整类型声明
	 * @since 4.3.4
	 */
	public NoSuchBeanDefinitionException(ResolvableType type) {
		super("No qualifying bean of type '" + type + "' available");
		this.beanName = null;
		this.resolvableType = type;
	}

	/**
	 * 创建新的 {@code NoSuchBeanDefinitionException}。
	 * @param type 缺失 Bean 的完整类型声明
	 * @param message 描述问题的详细消息
	 * @since 4.3.4
	 */
	public NoSuchBeanDefinitionException(ResolvableType type, String message) {
		super("No qualifying bean of type '" + type + "' available: " + message);
		this.beanName = null;
		this.resolvableType = type;
	}


	/**
	 * 若是按<em>名称</em>查找失败，则返回缺失 Bean 的名称。
	 */
	public @Nullable String getBeanName() {
		return this.beanName;
	}

	/**
	 * 若是按<em>类型</em>查找失败，则返回缺失 Bean 的所需类型。
	 */
	public @Nullable Class<?> getBeanType() {
		return (this.resolvableType != null ? this.resolvableType.resolve() : null);
	}

	/**
	 * 若是按<em>类型</em>查找失败，则返回缺失 Bean 所需的 {@link ResolvableType}。
	 * @since 4.3.4
	 */
	public @Nullable ResolvableType getResolvableType() {
		return this.resolvableType;
	}

	/**
	 * 返回在期望仅有一个匹配 Bean 时实际找到的 Bean 数量。
	 * 对于普通的 NoSuchBeanDefinitionException，该值始终为 0。
	 * @see NoUniqueBeanDefinitionException
	 */
	public int getNumberOfBeansFound() {
		return 0;
	}

}
