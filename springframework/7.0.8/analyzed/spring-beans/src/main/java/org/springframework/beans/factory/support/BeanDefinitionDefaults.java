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

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * 用于持有 {@code BeanDefinition} 属性默认值的简单容器。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see AbstractBeanDefinition#applyDefaults
 */
public class BeanDefinitionDefaults {

	private @Nullable Boolean lazyInit;

	private int autowireMode = AbstractBeanDefinition.AUTOWIRE_NO;

	private int dependencyCheck = AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;

	private @Nullable String initMethodName;

	private @Nullable String destroyMethodName;


	/**
	 * 设置 Bean 是否默认采用懒加载。
	 * <p>若为 {@code false}，在会急切初始化单例的 Bean 工厂中，Bean 将在启动时实例化。
	 * @see AbstractBeanDefinition#setLazyInit
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * 返回 Bean 是否默认懒加载，即不在启动时急切实例化。仅适用于单例 Bean。
	 * @return 是否应用懒加载语义（默认为 {@code false}）
	 */
	public boolean isLazyInit() {
		return (this.lazyInit != null && this.lazyInit);
	}

	/**
	 * 返回 Bean 是否默认懒加载，即不在启动时急切实例化。仅适用于单例 Bean。
	 * @return 若显式设置则返回懒加载标志，否则为 {@code null}
	 * @since 5.2
	 */
	public @Nullable Boolean getLazyInit() {
		return this.lazyInit;
	}

	/**
	 * 设置自动装配模式。决定是否会自动检测并设置 Bean 引用。默认为 AUTOWIRE_NO，
	 * 即不会按名称或类型进行约定式自动装配（但仍可能有显式注解驱动的自动装配）。
	 * @param autowireMode 要设置的自动装配模式，必须是 {@link AbstractBeanDefinition} 中定义的常量之一
	 * @see AbstractBeanDefinition#setAutowireMode
	 */
	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	/**
	 * 返回默认的自动装配模式。
	 */
	public int getAutowireMode() {
		return this.autowireMode;
	}

	/**
	 * 设置依赖检查代码。
	 * @param dependencyCheck 要设置的代码，必须是 {@link AbstractBeanDefinition} 中定义的常量之一
	 * @see AbstractBeanDefinition#setDependencyCheck
	 */
	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	/**
	 * 返回默认的依赖检查代码。
	 */
	public int getDependencyCheck() {
		return this.dependencyCheck;
	}

	/**
	 * 设置默认初始化方法的名称。
	 * <p>注意：此方法不会强制应用于所有受影响的 Bean 定义，而是作为可选回调，仅在方法实际存在时调用。
	 * @see AbstractBeanDefinition#setInitMethodName
	 * @see AbstractBeanDefinition#setEnforceInitMethod
	 */
	public void setInitMethodName(@Nullable String initMethodName) {
		this.initMethodName = (StringUtils.hasText(initMethodName) ? initMethodName : null);
	}

	/**
	 * 返回默认初始化方法的名称。
	 */
	public @Nullable String getInitMethodName() {
		return this.initMethodName;
	}

	/**
	 * 设置默认销毁方法的名称。
	 * <p>注意：此方法不会强制应用于所有受影响的 Bean 定义，而是作为可选回调，仅在方法实际存在时调用。
	 * @see AbstractBeanDefinition#setDestroyMethodName
	 * @see AbstractBeanDefinition#setEnforceDestroyMethod
	 */
	public void setDestroyMethodName(@Nullable String destroyMethodName) {
		this.destroyMethodName = (StringUtils.hasText(destroyMethodName) ? destroyMethodName : null);
	}

	/**
	 * 返回默认销毁方法的名称。
	 */
	public @Nullable String getDestroyMethodName() {
		return this.destroyMethodName;
	}

}
