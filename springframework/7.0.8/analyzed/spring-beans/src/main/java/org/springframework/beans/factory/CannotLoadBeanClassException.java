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

import org.springframework.beans.FatalBeanException;

/**
 * BeanFactory 无法加载给定 bean 的指定类时抛出的异常。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class CannotLoadBeanClassException extends FatalBeanException {

	/** bean 定义来源资源的描述（若可知） */
	private final @Nullable String resourceDescription;

	/** 被请求的 bean 名称 */
	private final String beanName;

	/** 尝试加载的 bean 类名（若可知） */
	private final @Nullable String beanClassName;


	/**
	 * 创建一个新的 {@code CannotLoadBeanClassException}。
	 * @param resourceDescription bean 定义来源资源的描述
	 * @param beanName 被请求的 bean 名称
	 * @param beanClassName bean 类名
	 * @param cause 根因
	 */
	public CannotLoadBeanClassException(@Nullable String resourceDescription, String beanName,
			@Nullable String beanClassName, ClassNotFoundException cause) {

		super("Cannot find class [" + beanClassName + "] for bean with name '" + beanName + "'" +
				(resourceDescription != null ? " defined in " + resourceDescription : ""), cause);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
		this.beanClassName = beanClassName;
	}

	/**
	 * 创建一个新的 {@code CannotLoadBeanClassException}。
	 * @param resourceDescription bean 定义来源资源的描述
	 * @param beanName 被请求的 bean 名称
	 * @param beanClassName bean 类名
	 * @param cause 根因
	 */
	public CannotLoadBeanClassException(@Nullable String resourceDescription, String beanName,
			@Nullable String beanClassName, LinkageError cause) {

		super("Error loading class [" + beanClassName + "] for bean with name '" + beanName + "'" +
				(resourceDescription != null ? " defined in " + resourceDescription : "") +
				": problem with class file or dependent class", cause);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
		this.beanClassName = beanClassName;
	}


	/**
	 * 返回 bean 定义来源资源的描述。
	 */
	public @Nullable String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * 返回被请求的 bean 名称。
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * 返回我们尝试加载的类名。
	 */
	public @Nullable String getBeanClassName() {
		return this.beanClassName;
	}

}
