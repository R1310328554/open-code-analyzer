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

package org.springframework.beans.factory.parsing;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;

/**
 * 解析过程中已注册别名的表示。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ReaderEventListener#aliasRegistered(AliasDefinition)
 */
public class AliasDefinition implements BeanMetadataElement {

	/** Bean 的规范名称。 */
	private final String beanName;

	/** 为 Bean 注册的别名。 */
	private final String alias;

	/** 来源对象。 */
	private final @Nullable Object source;


	/**
	 * 创建新的 {@link AliasDefinition}。
	 * @param beanName Bean 的规范名称
	 * @param alias 为 Bean 注册的别名
	 */
	public AliasDefinition(String beanName, String alias) {
		this(beanName, alias, null);
	}

	/**
	 * 创建新的 {@link AliasDefinition}。
	 * @param beanName Bean 的规范名称
	 * @param alias 为 Bean 注册的别名
	 * @param source 来源对象（可为 {@code null}）
	 */
	public AliasDefinition(String beanName, String alias, @Nullable Object source) {
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(alias, "Alias must not be null");
		this.beanName = beanName;
		this.alias = alias;
		this.source = source;
	}


	/**
	 * 返回 Bean 的规范名称。
	 */
	public final String getBeanName() {
		return this.beanName;
	}

	/**
	 * 返回为 Bean 注册的别名。
	 */
	public final String getAlias() {
		return this.alias;
	}

	@Override
	public final @Nullable Object getSource() {
		return this.source;
	}

}
