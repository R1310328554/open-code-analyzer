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

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.ResolvableType;

/**
 * BeanDefinition 描述一个 bean 实例，包含属性值、构造器参数值，
 * 以及由具体实现提供的其他信息。
 *
 * <p>这只是一个最小接口：主要目的是允许 {@link BeanFactoryPostProcessor}
 * 内省并修改属性值及其他 bean 元数据。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	/**
	 * 标准单例作用域的标识符：{@value}。
	 * <p>注意扩展的 bean 工厂可能支持更多作用域。
	 * @see #setScope
	 * @see ConfigurableBeanFactory#SCOPE_SINGLETON
	 */
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

	/**
	 * 标准原型作用域的标识符：{@value}。
	 * <p>注意扩展的 bean 工厂可能支持更多作用域。
	 * @see #setScope
	 * @see ConfigurableBeanFactory#SCOPE_PROTOTYPE
	 */
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;


	/**
	 * 角色提示：表示 {@code BeanDefinition} 是应用的主要组成部分。
	 * 通常对应用户定义的 bean。
	 */
	int ROLE_APPLICATION = 0;

	/**
	 * 角色提示：表示 {@code BeanDefinition} 是某更大配置的辅助部分，
	 * 通常是外层 {@link org.springframework.beans.factory.parsing.ComponentDefinition}。
	 * {@code SUPPORT} bean 在仔细查看特定
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition} 时值得关注，
	 * 但在查看应用整体配置时不必关注。
	 */
	int ROLE_SUPPORT = 1;

	/**
	 * 角色提示：表示 {@code BeanDefinition} 仅提供完全后台角色，与最终用户无关。
	 * 此提示用于注册完全属于
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition}
	 * 内部运作的 bean。
	 */
	int ROLE_INFRASTRUCTURE = 2;


	// 可修改属性

	/**
	 * 设置此 bean 定义的父定义名称（如有）。
	 */
	void setParentName(@Nullable String parentName);

	/**
	 * 返回此 bean 定义的父定义名称（如有）。
	 */
	@Nullable String getParentName();

	/**
	 * 指定此 bean 定义的 bean 类名。
	 * <p>类名可在 bean 工厂后处理期间修改，通常将原始类名替换为解析后的变体。
	 * @see #setParentName
	 * @see #setFactoryBeanName
	 * @see #setFactoryMethodName
	 */
	void setBeanClassName(@Nullable String beanClassName);

	/**
	 * 返回此 bean 定义的当前 bean 类名。
	 * <p>注意这不一定是运行时使用的实际类名，子定义可能从父定义覆盖/继承类名。
	 * 此外，这可能只是调用工厂方法的类，甚至可能是工厂 bean 引用上调用方法时的空值。
	 * 因此，<i>不要</i>将其视为运行时的最终 bean 类型，而仅用于单个 bean 定义级别的解析。
	 * @see #getParentName()
	 * @see #getFactoryBeanName()
	 * @see #getFactoryMethodName()
	 */
	@Nullable String getBeanClassName();

	/**
	 * 覆盖此 bean 的目标作用域，指定新的作用域名称。
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	void setScope(@Nullable String scope);

	/**
	 * 返回此 bean 当前目标作用域的名称，若尚不确定则为 {@code null}。
	 */
	@Nullable String getScope();

	/**
	 * 设置此 bean 是否应延迟初始化。
	 * <p>若为 {@code false}，执行单例急切初始化的 bean 工厂将在启动时实例化该 bean。
	 */
	void setLazyInit(boolean lazyInit);

	/**
	 * 返回此 bean 是否应延迟初始化，即不在启动时急切实例化。
	 * 仅适用于单例 bean。
	 */
	boolean isLazyInit();

	/**
	 * 设置此 bean 依赖的、必须先初始化的 bean 名称。
	 * bean 工厂将保证这些 bean 先被初始化。
	 * <p>注意依赖通常通过 bean 属性或构造器参数表达。
	 * 此属性仅用于其他类型的依赖，如静态变量（*唉*）或启动时的数据库准备。
	 */
	void setDependsOn(String @Nullable ... dependsOn);

	/**
	 * 返回此 bean 依赖的 bean 名称。
	 */
	String @Nullable [] getDependsOn();

	/**
	 * 设置此 bean 是否可作为自动装配候选注入到其他 bean。
	 * <p>注意此标志仅影响基于类型的自动装配。
	 * 不影响按名称的显式引用，即使指定 bean 未标记为自动装配候选，按名称解析仍会注入。
	 * 因此，按名称自动装配在名称匹配时仍会注入 bean。
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 * 返回此 bean 是否可作为自动装配候选注入到其他 bean。
	 */
	boolean isAutowireCandidate();

	/**
	 * 设置此 bean 是否为主要自动装配候选。
	 * <p>若在多个匹配候选中恰好有一个 bean 的此值为 {@code true}，它将作为决胜因素。
	 * @see #setFallback
	 */
	void setPrimary(boolean primary);

	/**
	 * 返回此 bean 是否为主要自动装配候选。
	 */
	boolean isPrimary();

	/**
	 * 设置此 bean 是否为后备自动装配候选。
	 * <p>若在多个匹配候选中除一个外所有 bean 的此值均为 {@code true}，将选择剩余的那个 bean。
	 * @since 6.2
	 * @see #setPrimary
	 */
	void setFallback(boolean fallback);

	/**
	 * 返回此 bean 是否为后备自动装配候选。
	 * @since 6.2
	 */
	boolean isFallback();

	/**
	 * 指定要使用的工厂 bean（如有）。
	 * 这是要调用指定工厂方法的 bean 名称。
	 * <p>工厂 bean 名称仅对基于实例的工厂方法必要。
	 * 对于静态工厂方法，方法将从 bean 类派生。
	 * @see #setFactoryMethodName
	 * @see #setBeanClassName
	 */
	void setFactoryBeanName(@Nullable String factoryBeanName);

	/**
	 * 返回工厂 bean 名称（如有）。
	 * <p>对于静态工厂方法，此值为 {@code null}，方法将从 bean 类派生。
	 * @see #getFactoryMethodName()
	 * @see #getBeanClassName()
	 */
	@Nullable String getFactoryBeanName();

	/**
	 * 指定工厂方法（如有）。将使用构造器参数调用此方法，无参数时则无参调用。
	 * 将在指定的工厂 bean 上调用，否则作为本地 bean 类的静态方法调用。
	 * @see #setFactoryBeanName
	 * @see #setBeanClassName
	 */
	void setFactoryMethodName(@Nullable String factoryMethodName);

	/**
	 * 返回工厂方法（如有）。
	 * @see #getFactoryBeanName()
	 * @see #getBeanClassName()
	 */
	@Nullable String getFactoryMethodName();

	/**
	 * 返回此 bean 的构造器参数值。
	 * <p>返回的实例可在 bean 工厂后处理期间修改。
	 * @return ConstructorArgumentValues 对象（永不为 {@code null}）
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * 返回此 bean 是否定义了构造器参数值。
	 * @since 5.0.2
	 * @see #getConstructorArgumentValues()
	 */
	default boolean hasConstructorArgumentValues() {
		return !getConstructorArgumentValues().isEmpty();
	}

	/**
	 * 返回要应用于 bean 新实例的属性值。
	 * <p>返回的实例可在 bean 工厂后处理期间修改。
	 * @return MutablePropertyValues 对象（永不为 {@code null}）
	 */
	MutablePropertyValues getPropertyValues();

	/**
	 * 返回此 bean 是否定义了属性值。
	 * @since 5.0.2
	 * @see #getPropertyValues()
	 */
	default boolean hasPropertyValues() {
		return !getPropertyValues().isEmpty();
	}

	/**
	 * 设置初始化方法的名称。
	 * @since 5.1
	 */
	void setInitMethodName(@Nullable String initMethodName);

	/**
	 * 返回初始化方法的名称。
	 * @since 5.1
	 */
	@Nullable String getInitMethodName();

	/**
	 * 设置销毁方法的名称。
	 * @since 5.1
	 */
	void setDestroyMethodName(@Nullable String destroyMethodName);

	/**
	 * 返回销毁方法的名称。
	 * @since 5.1
	 */
	@Nullable String getDestroyMethodName();

	/**
	 * 设置此 {@code BeanDefinition} 的角色提示。
	 * 角色提示为框架和工具提供特定 {@code BeanDefinition} 的角色和重要性指示。
	 * @since 5.1
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	void setRole(int role);

	/**
	 * 获取此 {@code BeanDefinition} 的角色提示。
	 * 角色提示为框架和工具提供特定 {@code BeanDefinition} 的角色和重要性指示。
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	int getRole();

	/**
	 * 设置此 bean 定义的人类可读描述。
	 * @since 5.1
	 */
	void setDescription(@Nullable String description);

	/**
	 * 返回此 bean 定义的人类可读描述。
	 */
	@Nullable String getDescription();


	// 只读属性

	/**
	 * 返回此 bean 定义的可解析类型，基于 bean 类或其他特定元数据。
	 * <p>通常在运行时合并的 bean 定义上完全解析，但配置时定义实例上不一定。
	 * @return 可解析类型（可能为 {@link ResolvableType#NONE}）
	 * @since 5.2
	 * @see ConfigurableBeanFactory#getMergedBeanDefinition
	 */
	ResolvableType getResolvableType();

	/**
	 * 返回此 bean 是否为<b>单例</b>，即所有调用返回同一共享实例。
	 * @see #SCOPE_SINGLETON
	 */
	boolean isSingleton();

	/**
	 * 返回此 bean 是否为<b>原型</b>，即每次调用返回独立实例。
	 * @since 3.0
	 * @see #SCOPE_PROTOTYPE
	 */
	boolean isPrototype();

	/**
	 * 返回此 bean 是否为"抽象"的，即不打算自身实例化，
	 * 仅作为具体子 bean 定义的父定义。
	 */
	boolean isAbstract();

	/**
	 * 返回此 bean 定义来源资源的描述（用于错误时显示上下文）。
	 */
	@Nullable String getResourceDescription();

	/**
	 * 返回原始 BeanDefinition，无则为 {@code null}。
	 * <p>允许检索被装饰的 bean 定义（如有）。
	 * <p>注意此方法返回直接来源。需遍历来源链以找到用户定义的原始 BeanDefinition。
	 */
	@Nullable BeanDefinition getOriginatingBeanDefinition();

}
