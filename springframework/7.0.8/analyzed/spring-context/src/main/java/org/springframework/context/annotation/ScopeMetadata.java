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

package org.springframework.context.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.Assert;

/**
 * 描述 Spring 管理 Bean 的作用域特征，包括作用域名称与作用域代理行为。
 *
 * <p>默认作用域为 {@code singleton}，默认<i>不</i>创建作用域代理。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see ScopeMetadataResolver
 * @see ScopedProxyMode
 */
public class ScopeMetadata {

	/** 作用域名称，默认为单例。 */
	private String scopeName = BeanDefinition.SCOPE_SINGLETON;

	/** 作用域代理模式，默认不创建代理。 */
	private ScopedProxyMode scopedProxyMode = ScopedProxyMode.NO;


	/**
	 * 设置作用域名称。
	 */
	public void setScopeName(String scopeName) {
		Assert.notNull(scopeName, "'scopeName' must not be null");
		this.scopeName = scopeName;
	}

	/**
	 * 获取作用域名称。
	 */
	public String getScopeName() {
		return this.scopeName;
	}

	/**
	 * 设置应用于作用域实例的代理模式。
	 */
	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		Assert.notNull(scopedProxyMode, "'scopedProxyMode' must not be null");
		this.scopedProxyMode = scopedProxyMode;
	}

	/**
	 * 获取应用于作用域实例的代理模式。
	 */
	public ScopedProxyMode getScopedProxyMode() {
		return this.scopedProxyMode;
	}

}
