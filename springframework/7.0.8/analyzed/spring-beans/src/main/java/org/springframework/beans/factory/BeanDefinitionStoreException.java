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
 * BeanFactory 遇到无效 bean 定义时抛出的异常：
 * 例如元数据不完整或自相矛盾。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
@SuppressWarnings("serial")
public class BeanDefinitionStoreException extends FatalBeanException {

	/** bean 定义来源资源的描述（若可知） */
	private final @Nullable String resourceDescription;

	/** 相关 bean 名称（若可知） */
	private final @Nullable String beanName;


	/**
	 * 创建一个新的 {@code BeanDefinitionStoreException}。
	 * @param msg 详细消息（原样用作异常消息）
	 */
	public BeanDefinitionStoreException(String msg) {
		super(msg);
		this.resourceDescription = null;
		this.beanName = null;
	}

	/**
	 * 创建一个新的 {@code BeanDefinitionStoreException}。
	 * @param msg 详细消息（原样用作异常消息）
	 * @param cause 根因（可为 {@code null}）
	 */
	public BeanDefinitionStoreException(String msg, @Nullable Throwable cause) {
		super(msg, cause);
		this.resourceDescription = null;
		this.beanName = null;
	}

	/**
	 * 创建一个新的 {@code BeanDefinitionStoreException}。
	 * @param resourceDescription bean 定义来源资源的描述
	 * @param msg 详细消息（原样用作异常消息）
	 */
	public BeanDefinitionStoreException(@Nullable String resourceDescription, String msg) {
		super(msg);
		this.resourceDescription = resourceDescription;
		this.beanName = null;
	}

	/**
	 * 创建一个新的 {@code BeanDefinitionStoreException}。
	 * @param resourceDescription bean 定义来源资源的描述
	 * @param msg 详细消息（原样用作异常消息）
	 * @param cause 根因（可为 {@code null}）
	 */
	public BeanDefinitionStoreException(@Nullable String resourceDescription, String msg, @Nullable Throwable cause) {
		super(msg, cause);
		this.resourceDescription = resourceDescription;
		this.beanName = null;
	}

	/**
	 * 创建一个新的 {@code BeanDefinitionStoreException}。
	 * @param resourceDescription bean 定义来源资源的描述
	 * @param beanName bean 名称
	 * @param msg 详细消息（会追加到指出资源与 bean 名称的引导语之后）
	 */
	public BeanDefinitionStoreException(@Nullable String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	/**
	 * 创建一个新的 {@code BeanDefinitionStoreException}。
	 * @param resourceDescription bean 定义来源资源的描述
	 * @param beanName bean 名称
	 * @param msg 详细消息（会追加到指出资源与 bean 名称的引导语之后）
	 * @param cause 根因（可为 {@code null}）
	 */
	public BeanDefinitionStoreException(
			@Nullable String resourceDescription, String beanName, @Nullable String msg, @Nullable Throwable cause) {

		super(msg == null ?
						"Invalid bean definition with name '" + beanName + "' defined in " + resourceDescription :
						"Invalid bean definition with name '" + beanName + "' defined in " + resourceDescription + ": " + msg,
				cause);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
	}


	/**
	 * 返回 bean 定义来源资源的描述（若可用）。
	 */
	public @Nullable String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * 返回 bean 名称（若可用）。
	 */
	public @Nullable String getBeanName() {
		return this.beanName;
	}

}
