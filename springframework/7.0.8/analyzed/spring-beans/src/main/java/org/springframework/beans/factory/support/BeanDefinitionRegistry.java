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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

/**
 * 持有 Bean 定义（如 RootBeanDefinition 和 ChildBeanDefinition 实例）的注册表接口。
 * 通常由内部使用 AbstractBeanDefinition 层次结构的 BeanFactory 实现。
 *
 * <p>这是 Spring Bean 工厂包中唯一封装 Bean 定义<i>注册</i>的接口。
 * 标准 BeanFactory 接口仅涵盖对<i>已完全配置的工厂实例</i>的访问。
 *
 * <p>Spring 的 Bean 定义读取器期望在此接口的实现上工作。
 * Spring 核心中的已知实现包括 DefaultListableBeanFactory 和 GenericApplicationContext。
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public interface BeanDefinitionRegistry extends AliasRegistry {

	/**
	 * 向本注册表注册新的 Bean 定义。
	 * 必须支持 RootBeanDefinition 和 ChildBeanDefinition。
	 * @param beanName 要注册的 Bean 实例名称
	 * @param beanDefinition 要注册的 Bean 实例定义
	 * @throws BeanDefinitionStoreException BeanDefinition 无效时
	 * @throws BeanDefinitionOverrideException 指定 Bean 名称已存在 BeanDefinition
	 * 且不允许覆盖时
	 * @see GenericBeanDefinition
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;

	/**
	 * 移除给定名称的 BeanDefinition。
	 * @param beanName 要移除的 Bean 实例名称
	 * @throws NoSuchBeanDefinitionException 不存在该 Bean 定义时
	 */
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 返回给定 Bean 名称的 BeanDefinition。
	 * @param beanName 要查找定义的 Bean 名称
	 * @return 给定名称的 BeanDefinition（永不为 {@code null}）
	 * @throws NoSuchBeanDefinitionException 不存在该 Bean 定义时
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 检查本注册表是否包含给定名称的 Bean 定义。
	 * @param beanName 要查找的 Bean 名称
	 * @return 本注册表是否包含给定名称的 Bean 定义
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 返回本注册表中定义的所有 Bean 的名称。
	 * @return 本注册表中定义的所有 Bean 名称，无定义时返回空数组
	 */
	String[] getBeanDefinitionNames();

	/**
	 * 返回注册表中定义的 Bean 数量。
	 * @return 注册表中定义的 Bean 数量
	 */
	int getBeanDefinitionCount();

	/**
	 * 判断给定名称的 Bean 定义是否可覆盖，
	 * 即 {@link #registerBeanDefinition} 对同名已有定义是否会成功。
	 * <p>默认实现返回 {@code true}。
	 * @param beanName 要检查的名称
	 * @return 给定 Bean 名称的定义是否可覆盖
	 * @since 6.1
	 */
	default boolean isBeanDefinitionOverridable(String beanName) {
		return true;
	}

	/**
	 * 判断给定 Bean 名称是否已在本注册表中使用，
	 * 即是否已有本地 Bean 或别名注册在该名称下。
	 * @param beanName 要检查的名称
	 * @return 给定 Bean 名称是否已在使用中
	 */
	boolean isBeanNameInUse(String beanName);

}
