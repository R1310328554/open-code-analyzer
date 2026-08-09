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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 简单的 {@link BeanFactoryPostProcessor} 实现，向所包含的
 * {@link ConfigurableBeanFactory} 注册自定义 {@link Scope 作用域}。
 *
 * <p>会将所有通过 {@link #setScopes(java.util.Map)} 提供的作用域注册到
 * 传入 {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)} 方法的
 * {@link ConfigurableListableBeanFactory} 中。
 *
 * <p>本类支持以<i>声明式</i>方式注册自定义作用域。
 * 也可考虑实现自定义 {@link BeanFactoryPostProcessor}，
 * 以编程方式调用 {@link ConfigurableBeanFactory#registerScope}。
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 */
public class CustomScopeConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

	/** 待注册的自定义作用域映射（名称 → 实例/类名/类）。 */
	private @Nullable Map<String, Object> scopes;

	/** 执行顺序，默认为最低优先级。 */
	private int order = Ordered.LOWEST_PRECEDENCE;

	/** Bean 类加载器。 */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * 指定要注册的自定义作用域。
	 * <p>键为作用域名称（{@code String} 类型）；每个值应为对应的自定义
	 * {@link Scope} 实例或类名。
	 */
	public void setScopes(Map<String, Object> scopes) {
		this.scopes = scopes;
	}

	/**
	 * 将给定作用域添加到本配置器的作用域映射中。
	 * @param scopeName 作用域名称
	 * @param scope 作用域实现
	 * @since 4.1.1
	 */
	public void addScope(String scopeName, Scope scope) {
		if (this.scopes == null) {
			this.scopes = new LinkedHashMap<>(1);
		}
		this.scopes.put(scopeName, scope);
	}


	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.scopes != null) {
			this.scopes.forEach((scopeKey, value) -> {
				if (value instanceof Scope scope) {
					// 直接注册 Scope 实例
					beanFactory.registerScope(scopeKey, scope);
				}
				else if (value instanceof Class<?> scopeClass) {
					// 通过 Class 对象实例化后注册
					Assert.isAssignable(Scope.class, scopeClass, "Invalid scope class");
					beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
				}
				else if (value instanceof String scopeClassName) {
					// 通过类名字符串解析并实例化后注册
					Class<?> scopeClass = ClassUtils.resolveClassName(scopeClassName, this.beanClassLoader);
					Assert.isAssignable(Scope.class, scopeClass, "Invalid scope class");
					beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
				}
				else {
					throw new IllegalArgumentException("Mapped value [" + value + "] for scope key [" +
							scopeKey + "] is not an instance of required type [" + Scope.class.getName() +
							"] or a corresponding Class or String value indicating a Scope implementation");
				}
			});
		}
	}

}
