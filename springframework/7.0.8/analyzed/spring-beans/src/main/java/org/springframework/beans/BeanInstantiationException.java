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

package org.springframework.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * Bean 实例化失败时抛出的异常。
 * 携带出问题的 bean 类型。
 *
 * @author Juergen Hoeller
 * @since 1.2.8
 */
@SuppressWarnings("serial")
public class BeanInstantiationException extends FatalBeanException {

	/** 出问题的 bean 类型 */
	private final Class<?> beanClass;

	/** 出问题的构造器（若可知） */
	private final @Nullable Constructor<?> constructor;

	/** 用于构造 bean 的委托方法（若可知） */
	private final @Nullable Method constructingMethod;


	/**
	 * 创建一个新的 {@code BeanInstantiationException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param msg 详细消息
	 */
	public BeanInstantiationException(Class<?> beanClass, String msg) {
		this(beanClass, msg, null);
	}

	/**
	 * 创建一个新的 {@code BeanInstantiationException}。
	 * @param beanClass 出问题的 bean 类型
	 * @param msg 详细消息
	 * @param cause 根因
	 */
	public BeanInstantiationException(Class<?> beanClass, String msg, @Nullable Throwable cause) {
		super("Failed to instantiate [" + beanClass.getName() + "]: " + msg, cause);
		this.beanClass = beanClass;
		this.constructor = null;
		this.constructingMethod = null;
	}

	/**
	 * 创建一个新的 {@code BeanInstantiationException}。
	 * @param constructor 出问题的构造器
	 * @param msg 详细消息
	 * @param cause 根因
	 * @since 4.3
	 */
	public BeanInstantiationException(Constructor<?> constructor, @Nullable String msg, @Nullable Throwable cause) {
		super("Failed to instantiate [" + constructor.getDeclaringClass().getName() + "]: " + msg, cause);
		this.beanClass = constructor.getDeclaringClass();
		this.constructor = constructor;
		this.constructingMethod = null;
	}

	/**
	 * 创建一个新的 {@code BeanInstantiationException}。
	 * @param constructingMethod 用于构造 bean 的委托方法
	 * （通常但不一定是静态工厂方法）
	 * @param msg 详细消息
	 * @param cause 根因
	 * @since 4.3
	 */
	public BeanInstantiationException(Method constructingMethod, @Nullable String msg, @Nullable Throwable cause) {
		super("Failed to instantiate [" + constructingMethod.getReturnType().getName() + "]: " + msg, cause);
		this.beanClass = constructingMethod.getReturnType();
		this.constructor = null;
		this.constructingMethod = constructingMethod;
	}


	/**
	 * 返回出问题的 bean 类型（永不为 {@code null}）。
	 * @return 本应被实例化的类型
	 */
	public Class<?> getBeanClass() {
		return this.beanClass;
	}

	/**
	 * 返回出问题的构造器（若可知）。
	 * @return 正在使用的构造器；若走工厂方法或默认实例化，则为 {@code null}
	 * @since 4.3
	 */
	public @Nullable Constructor<?> getConstructor() {
		return this.constructor;
	}

	/**
	 * 返回用于构造 bean 的委托方法（若可知）。
	 * @return 正在使用的方法（通常是静态工厂方法）；
	 * 若走基于构造器的实例化，则为 {@code null}
	 * @since 4.3
	 */
	public @Nullable Method getConstructingMethod() {
		return this.constructingMethod;
	}

}
