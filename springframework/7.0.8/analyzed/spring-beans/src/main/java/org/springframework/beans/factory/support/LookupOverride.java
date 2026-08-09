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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

/**
 * 表示对方法的覆盖：在同一 IoC 上下文中查找对象，
 * 可按 Bean 名称或按 Bean 类型（基于声明的方法返回类型）查找。
 *
 * <p>符合查找覆盖条件的方法可声明参数，给定参数将传递给 Bean 检索操作。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.factory.BeanFactory#getBean(String)
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
 * @see org.springframework.beans.factory.BeanFactory#getBean(String, Object...)
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class, Object...)
 * @see org.springframework.beans.factory.BeanFactory#getBeanProvider(ResolvableType)
 */
public class LookupOverride extends MethodOverride {

	/** 要查找并返回的 Bean 名称，为 {@code null} 时按类型检索。 */
	private final @Nullable String beanName;

	/** 要覆盖的方法引用（若通过 {@link Method} 指定）。 */
	private @Nullable Method method;


	/**
	 * 构造新的 {@code LookupOverride}。
	 * @param methodName 要覆盖的方法名
	 * @param beanName 当前 {@code BeanFactory} 中被覆盖方法应返回的 Bean 名称
	 * （按类型检索时可为 {@code null}）
	 */
	public LookupOverride(String methodName, @Nullable String beanName) {
		super(methodName);
		this.beanName = beanName;
	}

	/**
	 * 构造新的 {@code LookupOverride}。
	 * @param method 要覆盖的方法声明
	 * @param beanName 当前 {@code BeanFactory} 中被覆盖方法应返回的 Bean 名称
	 * （按类型检索时可为 {@code null}）
	 */
	public LookupOverride(Method method, @Nullable String beanName) {
		super(method.getName());
		this.method = method;
		this.beanName = beanName;
	}


	/**
	 * 返回本 {@code LookupOverride} 应查找的 Bean 名称。
	 */
	public @Nullable String getBeanName() {
		return this.beanName;
	}

	/**
	 * 通过 {@link Method} 引用或方法名匹配指定方法。
	 * <p>出于向后兼容，在存在同名重载非抽象方法时，
	 * 仅无参变体才会被转换为容器驱动的查找方法。
	 * <p>若提供了 {@link Method}，仅考虑精确匹配，通常由 {@code @Lookup} 注解标记。
	 */
	@Override
	public boolean matches(Method method) {
		if (this.method != null) {
			return method.equals(this.method);
		}
		else {
			return (method.getName().equals(getMethodName()) && (!isOverloaded() ||
					Modifier.isAbstract(method.getModifiers()) || method.getParameterCount() == 0));
		}
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (other instanceof LookupOverride that && super.equals(other) &&
				ObjectUtils.nullSafeEquals(this.method, that.method) &&
				ObjectUtils.nullSafeEquals(this.beanName, that.beanName));
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.beanName);
	}

	@Override
	public String toString() {
		return "LookupOverride for method '" + getMethodName() + "'";
	}

}
