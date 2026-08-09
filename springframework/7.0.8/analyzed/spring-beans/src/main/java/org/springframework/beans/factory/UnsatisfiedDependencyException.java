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
import org.springframework.util.StringUtils;

/**
 * 当 Bean 依赖其他 Bean 或简单属性，而这些依赖在 BeanFactory 定义中
 * 并未指定，但依赖检查却已启用时抛出。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 03.09.2003
 */
@SuppressWarnings("serial")
public class UnsatisfiedDependencyException extends BeanCreationException {

	/** 未能满足的注入点（字段或方法/构造器参数）；未知则为 {@code null} */
	private final @Nullable InjectionPoint injectionPoint;


	/**
	 * 创建新的 UnsatisfiedDependencyException。
	 * @param resourceDescription Bean 定义来源资源的描述
	 * @param beanName 所请求 Bean 的名称
	 * @param propertyName 未能满足的 Bean 属性名
	 * @param msg 详细消息
	 */
	public UnsatisfiedDependencyException(
			@Nullable String resourceDescription, @Nullable String beanName, String propertyName, @Nullable String msg) {

		super(resourceDescription, beanName,
				"Unsatisfied dependency expressed through bean property '" + propertyName + "'" +
				(StringUtils.hasLength(msg) ? ": " + msg : ""));
		this.injectionPoint = null;
	}

	/**
	 * 创建新的 UnsatisfiedDependencyException。
	 * @param resourceDescription Bean 定义来源资源的描述
	 * @param beanName 所请求 Bean 的名称
	 * @param propertyName 未能满足的 Bean 属性名
	 * @param ex 指示依赖未满足的 Bean 创建异常
	 */
	public UnsatisfiedDependencyException(
			@Nullable String resourceDescription, @Nullable String beanName, String propertyName, BeansException ex) {

		this(resourceDescription, beanName, propertyName, ex.getMessage());
		initCause(ex);
	}

	/**
	 * 创建新的 UnsatisfiedDependencyException。
	 * @param resourceDescription Bean 定义来源资源的描述
	 * @param beanName 所请求 Bean 的名称
	 * @param injectionPoint 注入点（字段或方法/构造器参数）
	 * @param msg 详细消息
	 * @since 4.3
	 */
	public UnsatisfiedDependencyException(
			@Nullable String resourceDescription, @Nullable String beanName, @Nullable InjectionPoint injectionPoint, @Nullable String msg) {

		super(resourceDescription, beanName,
				"Unsatisfied dependency expressed through " + injectionPoint +
				(StringUtils.hasLength(msg) ? ": " + msg : ""));
		this.injectionPoint = injectionPoint;
	}

	/**
	 * 创建新的 UnsatisfiedDependencyException。
	 * @param resourceDescription Bean 定义来源资源的描述
	 * @param beanName 所请求 Bean 的名称
	 * @param injectionPoint 注入点（字段或方法/构造器参数）
	 * @param ex 指示依赖未满足的 Bean 创建异常
	 * @since 4.3
	 */
	public UnsatisfiedDependencyException(
			@Nullable String resourceDescription, @Nullable String beanName, @Nullable InjectionPoint injectionPoint, BeansException ex) {

		this(resourceDescription, beanName, injectionPoint, ex.getMessage());
		initCause(ex);
	}


	/**
	 * 返回注入点（字段或方法/构造器参数），若已知。
	 * @since 4.3
	 */
	public @Nullable InjectionPoint getInjectionPoint() {
		return this.injectionPoint;
	}

}
